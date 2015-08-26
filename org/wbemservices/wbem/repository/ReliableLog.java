/*
 *EXHIBIT A - Sun Industry Standards Source License
 *
 *"The contents of this file are subject to the Sun Industry
 *Standards Source License Version 1.2 (the "License");
 *You may not use this file except in compliance with the
 *License. You may obtain a copy of the 
 *License at http://wbemservices.sourceforge.net/license.html
 *
 *Software distributed under the License is distributed on
 *an "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either
 *express or implied. See the License for the specific
 *language governing rights and limitations under the License.
 *
 *The Original Code is WBEM Services.
 *
 *The Initial Developer of the Original Code is:
 *Sun Microsystems, Inc.
 *
 *Portions created by: Sun Microsystems, Inc.
 *are Copyright © 2001 Sun Microsystems, Inc.
 *
 *All Rights Reserved.
 *
 *Contributor(s): _______________________________________
*/

package org.wbemservices.wbem.repository;

import java.io.*;

/**
 * This class is a simple implementation of a reliable Log.  The
 * client of a ReliableLog must provide a set of callbacks (via a
 * LogHandler) that enables a ReliableLog to read and write
 * checkpoints and log records.  This implementation ensures that the
 * data stored (via a ReliableLog) is recoverable after a system crash.
 * The implementation is unsynchronized; the client must synchronize
 * externally. <p>
 *
 * The secondary storage strategy is to record values in files using a
 * representation of the caller's choosing.  Two sorts of files are
 * kept: snapshots and logs.  At any instant, one snapshot is current.
 * The log consists of a sequence of updates that have occurred since
 * the current snapshot was taken.  The current stable state is the
 * value of the snapshot, as modified by the sequence of updates in
 * the log.  From time to time, the client of a ReliableLog instructs
 * the package to make a new snapshot and clear the log.  A ReliableLog
 * arranges disk writes such that updates are stable (as long as the
 * changes are force-written to disk) and atomic: no update is lost,
 * and each update either is recorded completely in the log or not at
 * all.  Making a new snapshot is also atomic. <p>
 *
 * Normal use for maintaining the recoverable store is as follows: The
 * client maintains the relevant data structure in virtual memory.  As
 * updates happen to the structure, the client informs the ReliableLog
 * (call it "log") by calling log.update.  Periodically, the client
 * calls log.snapshot to provide the current value of the data
 * structure.  On restart, the client calls log.recover to obtain the
 * latest snapshot and the following sequences of updates; the client
 * applies the updates to the snapshot to obtain the state that
 * existed before the crash. <p>
 *
 * This implementation only does one force-write (fsync) to disk
 * during an update.  Therefore, while reading log update records,
 * a corrupted record at the end of the log is possible.  This record
 * is detected and thrown away (instead of throwing an exception). <p>
 *
 * @see LogHandler
 *
 * @author Ann Wollrath
 * */

public class ReliableLog {

    private static final String snapshotPrefix = "Snapshot.";
    private static final String logfilePrefix = "Logfile.";
    private static final String versionFile = "Version_Number";
    private static final String newVersionFile = "New_Version_Number";
    private static final int intBytes = 4;

    private String dirPath = null;
    private File dir;			// base directory
    private int version = 0;		// current snapshot and log version
    private String logName = null;
    private RandomAccessFile log = null;
    private FileDescriptor logFD;
    private long snapshotBytes = 0;
    private long logBytes = 0;
    private int logEntries = 0;
    private long lastSnapshot = 0;
    private long lastLog = 0;
    private LogHandler handler;
 
    /**
     * Creates a ReliableLog to handle checkpoints and logging in a
     * stable storage directory.
     *
     * @param dirPath path to the stable storage directory
     * @param handler the handler for log callbacks
     *
     * @exception LogException If a directory creation error has occurred.
     * @exception IOException If other I/O error has occurred.
     */
    public ReliableLog(String dirPath, LogHandler handler)
	throws IOException, LogException
    {
	super();
	dir = new File(dirPath);
	if (!(dir.exists() && dir.isDirectory())) {
	    // create directory
	    if (!dir.mkdir()) {
		throw new LogException("could not create directory for log: " +
				       dirPath);
	    }
	}
        this.dirPath = dirPath;
	this.handler = handler;
	lastSnapshot = 0;
	lastLog = 0;
	getVersion();
    }

    /* public methods */
    
    /**
     * Retrieves the contents of the snapshot file by calling the client
     * supplied callback "recover" and then subsequently invoking
     * the "readUpdate" callback to apply any logged updates to the state.
     *
     * @exception LogException If recovery fails due to serious log corruption,
     * read update failure, or if an exception occurs during the recover
     * callback.
     * @exception IOException If other I/O error has occurred.
     */
    public void recover() throws IOException, LogException {
	if (version == 0) 
	    return;
	
	String fname = versionName(snapshotPrefix);
	File snapshotFile = new File(fname);
	FileInputStream in = new FileInputStream(snapshotFile);
	BufferedInputStream bufIn = new BufferedInputStream(in);
	
	try {
	    try {
		handler.recover(bufIn);
	    }
	    catch (Exception e) {
		throw new LogException("recovery failed", e);
	    }
	    snapshotBytes = snapshotFile.length();
	} finally {
	    in.close();
	}
	
	recoverUpdates();
    }
    
    /**
     * Records this update in the log file (does not force update to disk).
     * The update is recorded by calling the client's "writeUpdate" callback.
     * This method must not be called until this log's recover method has
     * been invoked (and completed).
     *
     * @param value the object representing the update
     *
     * @exception LogException If an exception occurred during a
     * writeUpdate callback.
     * @exception IOException If other I/O error has occurred.
     */
    public void update(Object value) throws IOException, LogException {
	update(value, false);
    }
    
    /**
     * Records this update in the log file.  The update is recorded by
     * calling the client's writeUpdate callback.  This method must not be
     * called until this log's recover method has been invoked
     * (and completed).
     *
     * @param value the object representing the update
     * @param forceToDisk If true, changes are forced to disk, otherwise
     * updates are buffered.
     *
     * @exception LogException If force-write to log failed or an exception
     * occurred during a writeUpdate callback.
     * @exception IOException If other I/O error has occurred.
     */
    public void update(Object value, boolean forceToDisk)
	throws IOException, LogException
    {
	long entryStart = log.getFilePointer();
	log.writeInt(0);
	
	try {
	    handler.writeUpdate(new LogOutputStream(log), value);
	    if (forceToDisk) {
	        try {
		    logFD.sync();
	        } catch (SyncFailedException sfe) {
		    throw new LogException("sync log failed", sfe);
	        }
	    }
	} catch (Exception e) {
	    throw new LogException("write update failed", e);
	}
	long entryEnd = log.getFilePointer();
	long updateLen = (entryEnd - entryStart) - intBytes;
	    
	/* write update record length before update in file */
	log.seek(entryStart);
	log.writeInt((int)updateLen);
	log.seek(entryEnd);

	logBytes = entryEnd;

	if (forceToDisk) {
	    try {
		logFD.sync();
	    } catch (SyncFailedException sfe) {
		throw new LogException("sync log failed", sfe);
	    }
	}
	
	lastLog = System.currentTimeMillis();
	logEntries++;
    }
    
    /**
     * Records the client-defined current snapshot by invoking the client
     * supplied "snapshot" callback and then empties the log.
     *
     * @exception LogException If an exception occurred during the
     * snapshot callback.
     * @exception IOException If other I/O error has occurred.
     */
    public void snapshot() throws IOException, LogException {
	int oldVersion = version;
	incrVersion();

	String fname = versionName(snapshotPrefix);
	File snapshotFile = new File(fname);
	FileOutputStream out = new FileOutputStream(snapshotFile);
	try {
	    try {
		handler.snapshot(out);
	    } catch (Exception e) {
		throw new LogException("snapshot failed", e);
	    }
	    snapshotBytes = snapshotFile.length();
	    lastSnapshot = System.currentTimeMillis();
	} finally {
	    out.close();
	}

	openLogFile(true);
	logBytes = 0;
	logEntries = 0;
	writeVersionFile(true);
	commitToNewVersion();
	deleteSnapshot(oldVersion);
	deleteLogFile(oldVersion);
    }
    
    /**
     * Close the stable storage directory in an orderly manner.
     *
     * @exception IOException If an I/O error has occurred.
     */
    public void close() throws IOException {
	if (log == null) return;
	try {
	    log.close();
	} finally {
	    log = null;
	}
    }
    
    /**
     * Close the random access log file, remove all ReliableLog-related
     * files from the log directory, and delete the directory.
     */
    public void deletePersistentStore() {

        try {
	    close();
	} catch (IOException e) {
        }
	try {
            deleteLogFile(version);
	} catch (IOException e) {
	}
	try {
            deleteSnapshot(version);
	} catch (IOException e) {
	}
	try {
            deleteFile(fName(versionFile));
	} catch (IOException e) {
	}
	try {
            deleteNewVersionFile();
	} catch (LogException e) {
	} catch (IOException e) {
	}
	try {
	    /* Delete the directory. The following call to the delete method
             * will fail only if the directory is not empty or if the Security
             * Manager's checkDelete() method throws a SecurityException. 
             * (The Security Manager will throw such an exception if it
             * determines that the current application is not allowed to
             * delete the directory.) For either case, upon un-successful
             * deletion of the directory, take no further action.
             */
	    dir.delete();
	} catch (SecurityException e) {
	}
    }

    /**
     * Returns the size of the snapshot file in bytes;
     */
    public long snapshotSize() { return snapshotBytes; }
    
    /**
     * Returns the size of the log file in bytes;
     */
    public long logSize() { return logBytes; }

    /* private methods */

    /**
     * Generates a filename prepended with the stable storage directory path.
     *
     * @param name the leaf name of the file
     */
    private String fName(String name) {
	return dir.getPath() + File.separator + name;
    }

    /**
     * Generates a version 0 filename prepended with the stable storage
     * directory path
     *
     * @param name version file name
     */
    private String versionName(String name) {
	return versionName(name, 0);
    }
    
    /**
     * Generates a version filename prepended with the stable storage
     * directory path with the version number as a suffix.
     *
     * @param ver version file name
     * @thisversion a version number
     */
    private String versionName(String prefix, int ver) {
	ver = (ver == 0) ? version : ver;
	return fName(prefix) + String.valueOf(ver);
    }

    /**
     * Increments the directory version number.
     */
    private void incrVersion() {
	do { version++; } while (version==0);
    }

    /**
     * Delete a file.
     *
     * @param name the name of the file
     * @exception LogException If new version file couldn't be removed.
     */
    private void deleteFile(String name) throws LogException {

	File f = new File(name);
	if (!f.delete())
	    throw new LogException("couldn't remove file: " + name); 
    }
    
    /**
     * Removes the new version number file.
     *
     * @exception IOException If an I/O error has occurred.
     */
    private void deleteNewVersionFile() throws IOException {
	deleteFile(fName(newVersionFile));
    }

    /**
     * Removes the snapshot file.
     *
     * @param ver the version to remove
     *
     * @exception IOException If an I/O error has occurred.
     */
    private void deleteSnapshot(int ver) throws IOException {
	if (ver == 0) return;
	deleteFile(versionName(snapshotPrefix, ver));
    }

    /**
     * Removes the log file.
     *
     * @param ver the version to remove
     *
     * @exception IOException If an I/O error has occurred.
     */
    private void deleteLogFile(int ver) throws IOException {
	if (ver == 0) return;
	deleteFile(versionName(logfilePrefix, ver));
    }

    /**
     * Opens the log file in read/write mode.  If file does not exist, it is
     * created.
     *
     * @param truncate if true and file exists, file is truncated to zero
     * length
     *
     * @exception IOException If an I/O error has occurred.
     */
    private void openLogFile(boolean truncate) throws IOException {
	try {
	    close();
	} catch (IOException e) { /* assume this is okay */
	    } 
	
	logName = versionName(logfilePrefix);
	log = new RandomAccessFile(logName, "rw");
	logFD = log.getFD();

	if (truncate) {
	    log.setLength(0);
	}
    }
    
    
    /**
     * Writes out version number to file.
     *
     * @param newVersion if true, writes to a new version file
     *
     * @exception IOException If an I/O error has occurred.
     */
    private void writeVersionFile(boolean newVersion) throws IOException {
	String name;
	if (newVersion) {
	    name = newVersionFile;
	} else {
	    name = versionFile;
	}
	DataOutputStream out =
	    new DataOutputStream(new FileOutputStream(fName(name)));
	out.writeInt(version);
	out.close();
    }
    
    /**
     * Creates the initial version file
     *
     * @exception IOException If an I/O error has occurred.
     */
    private void createFirstVersion() throws IOException {
	version = 0;
	writeVersionFile(false);
    }
    
    /**
     * Commits (atomically) the new version.
     *
     * @exception IOException If an I/O error has occurred.
     */
    private void commitToNewVersion() throws IOException {
	writeVersionFile(false);
	deleteNewVersionFile();
    }

    /**
     * Reads version number from a file.
     *
     * @param name the name of the version file
     *
     * @return the version
     *
     * @exception IOException If an I/O error has occurred.
     */
    private int readVersion(String name) throws IOException {
	DataInputStream in = new DataInputStream(new FileInputStream(name));
	try {
	    return in.readInt();
	} finally {
	    in.close();
	}
    }
    
    /**
     * Sets the version.  If version file does not exist, the initial
     * version file is created.
     *
     * @exception IOException If an I/O error has occurred.
     */
    private void getVersion() throws IOException {
	try {
	    version = readVersion(fName(newVersionFile));
	    commitToNewVersion();
	} catch (IOException e) {
	    try {
		deleteNewVersionFile();
	    }
	    catch (IOException ex) {
	    }
	    
	    try {
		version = readVersion(fName(versionFile));
	    }
	    catch (IOException ex) {
		createFirstVersion();
	    }
	}
    }

    /**
     * Applies outstanding updates to the snapshot.
     *
     * @exception LogException If serious log corruption is detected or if an
     * exception occurred during a readUpdate callback.
     * @exception IOException If other I/O error has occurred.
     */
    private void recoverUpdates() throws IOException, LogException {
	if (version == 0) return;
	
	String fname = versionName(logfilePrefix);
	FileInputStream in = new FileInputStream(fname);
	BufferedInputStream bufIn = new BufferedInputStream(in);
	DataInputStream dataIn = new DataInputStream(bufIn);
	
	try {
	    while (true) {
		int updateLen = 0;
		
		try {
		    updateLen = dataIn.readInt();
		} catch (EOFException e) {
		    break;
		}
		if (updateLen == 0) /* crashed while writing last log entry */
		    break;
		if (updateLen < 0)  /* serious corruption */
		    throw new LogException("corrupted log: bad update length");

		if (bufIn.available() < updateLen)
		    /* corrupted record at end of log (can happen since we
		     * do only one fsync) */
		    break;

		try {
		    handler.readUpdate(new LogInputStream(bufIn, updateLen));
		} catch (Exception e) {
		    e.printStackTrace();
		    throw new LogException("read update failed", e);
		}
		logBytes += (intBytes + updateLen);
		logEntries++;
	    } /* while */
	} finally {
	    in.close();
	}

	/* reopen log file at end */
	openLogFile(false);

	log.seek(logBytes);
	log.setLength(logBytes);
    }
}
