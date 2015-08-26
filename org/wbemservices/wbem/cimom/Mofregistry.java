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

package org.wbemservices.wbem.cimom;

import java.util.Collections;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.io.*;

import javax.wbem.cim.CIMException;
import javax.wbem.client.Debug;

import org.wbemservices.wbem.compiler.mofc.CIM_Mofc;

/**
 * This class handles all the actions required to perform MOF registration
 * and unregistration. It is a singleton class that is used by the CIMOM.
 *
 * @author      Sun Microsystems, Inc.
 * @since       WBEM 2.5
 */


class Mofregistry {

    private static final String UNREGSUFFIX = ".unreg";

    // parent directory for all mof information.
    String parentDir;
    // sub directory containing registry info
    String regDirParent;
    // sub directory containing unregistry info
    String unregDirParent;
    // sub directory containing failed reg/unreg info
    String failDirParent;
    // sub directory containing public reg info
    String preRegDirParent;
    // sub directory containing public unreg info
    String preUnregDirParent;
    
    private static Mofregistry mofreg = null;
    private CIMOMImpl cimom;

    /*
     * Mofregistry constructor
     * @param cimom     handle to the CIMOM. The MofregClient which helps
     *                  process the MOF files needs a handle back to the
     *                  CIMOM.
     * @param parentDir The parent directory for all the mof reg information
     * @param regDir Directory containing all the registry information. This
     *               is used privately by mofreg to maintain various registry
     *               states.
     * @param unregDir Directory containing all the unregistry information. This
     *                 is used privately by mofreg to maintain states for
     *                 unregistry
     * @param preRegDir Directory containing registry info that will be placed
     *                  into regDir. This directory is the public area where 
     *                  applications can place their registry information.
     * @param preUnregDir Directory containing registry info that will be placed
     *                    into regDir. This directory is the public area where 
     *                    applications can place their unregister information.
     * Note: regDir, unregDir, preReg and preUnreg are relative to parentDir.
     */
    private Mofregistry(CIMOMImpl cimom, 
			String parentDir, String regDir, 
			String unregDir, String failDir,
			String preRegDir, String preUnregDir) 
			throws IOException {
	this.parentDir = parentDir;
	this.regDirParent = parentDir + File.separatorChar + regDir;
	this.unregDirParent = parentDir + File.separatorChar + unregDir;
	this.failDirParent = parentDir +File.separatorChar + failDir;
	this.preRegDirParent = parentDir +File.separatorChar + preRegDir;
	this.preUnregDirParent = parentDir +File.separatorChar + preUnregDir;
	// Create the directories if they dont exist.
	File f = new File(failDirParent);
	f.mkdirs();
	f = new File(regDirParent);
	f.mkdirs();
	f = new File(unregDirParent);
	f.mkdirs();
	f = new File(preRegDirParent);
	f.mkdirs();
	f = new File(preUnregDirParent);
	f.mkdirs();

	this.cimom = cimom;
    }

    // Method to return the singleton mofreg. Look at the constructor
    // parameters for details of the input parameters.
    synchronized static Mofregistry getMofregistry(CIMOMImpl cimom, 
						   String parentDir, 
						   String regDir, 
						   String unregDir,
						   String failDir,
						   String preRegDir,
						   String preUnregDir) 
						   throws IOException {
	if (mofreg == null) {
	    mofreg = new Mofregistry(cimom, parentDir, regDir, 
				     unregDir, failDir, preRegDir, preUnregDir);
	}
	return mofreg;
    }

    // This method logs the exception that has occured.
    void logException(Exception e) {
	Debug.trace2("Got a mofreg error", e);
	CIMOMLogService ls = (CIMOMLogService)
	ServiceRegistry.getService(CIMOMLogService.DEFAULT);
	if (ls == null) {
	    return;
	}
	try {
	    ls.writeLog("Mofreg", "MOFREG_ERROR", "MOFREG_ERROR", null,
	    e.toString(), false, CIMOMLogService.SYSTEM_LOG,
	    CIMOMLogService.ERROR, null);
	} catch (Exception le) {
	    // Ignore the exception
	    Debug.trace2("logging error", le);
	}
    }

    // This method takes care of performing registry related operations.
    void doRegistry(File regDir) throws Exception {
	File[] fArray = regDir.listFiles();

	// Since regDir is a directory, fArray cannot be null.
	if (fArray.length == 0) {
	    // No reg file present. This shouldnt happen.
	    throw new CIMException(CIMException.CIM_ERR_NOT_FOUND, 
	    regDir+".reg");
	}

	OutputStream fos = null;

	// Get the name of the reg dir excluding the parent dirs.
	String regDirName = regDir.getName();
	try {
	    int mode = CIM_Mofc.REGMODE;

	    // Create the directory to store the unreg files
	    // It is stored underneath the unregDirParent, using the name
	    // of the directory containing regfile. For e.g. if the reg
	    // directory is in regDirParent/regDirName, then the unreg 
	    // directory 
	    // will be made in unregDirParent/regDirName
	    String unregDirString = unregDirParent + File.separatorChar +
	    regDirName;

	    File unregDir = new File(unregDirString);
	    if (unregDir.exists()) {
		// This means that this registry has already been performed.
		// We will not do it again. 
		Debug.trace2(unregDir +" already exists");
		// Move this to the failed directory
		String failDirBackupString = failDirParent + 
		File.separatorChar + regDirName + File.separatorChar + "reg";
		File failDirBackup = new File(failDirBackupString);
		Debug.trace3("Cleaning up old backup if any "+
	    	failDirBackupString);
		// Clear out the directory if it exists.
		recursiveDelete(failDirBackup);
		failDirBackup.mkdirs();
		regDir.renameTo(failDirBackup);
		// Must log this.
		return;
	    }
	    // Create the unreg directory for this registry
	    unregDir.mkdir();

	    // This is the directory where we will move everything once reg is 
	    // complete
	    String regDirBackupString =
	    unregDirString + File.separatorChar + regDirName;

	    // The unreg file is created within the unreg directory as 
	    // regDirName.UNREGSUFFIX
	    fos = new FileOutputStream(regDirBackupString + UNREGSUFFIX);

	    Debug.trace3("The input file is "+fArray[0]);
	    Debug.trace3("The output file is "+
	    regDirBackupString + UNREGSUFFIX);

	    UnregHandler uh = new UnregHandler(fos);
	    CIM_Mofc.parseMOF(new String[] {"-v", fArray[0].getPath()}, 
	    mode, new MofregClient(cimom, mode, uh));
	    uh.outputUnreg();
	    fos.close();

	    // Registry complete. Now we need to cleanup.
	    // Move all the registry info into the unreg directory.

	    Debug.trace3("Cleaning up old backup if any "+
	    regDirBackupString);
	    File regDirBackup = new File(regDirBackupString);
	    recursiveDelete(regDirBackup);

	    // Now move the current reg directory to the backup
	    regDir.renameTo(regDirBackup);

	} catch (Exception e) {
	    logException(e);
	    // Got an exception. Move the reg info to the failed directory
	    String failDirBackupString = failDirParent + File.separatorChar + 
					regDirName + File.separatorChar + "reg";

	    File failDirBackup = new File(failDirBackupString);
	    Debug.trace3("Cleaning up old backup if any "+
	    failDirBackupString);
	    // Clear out the directory if it exists.
	    recursiveDelete(failDirBackup);

	    // Clean up any unreg directory that may have been created
	    // for this registry
	    // First close the unreg out file.
	    if (fos != null) {
		fos.close();
	    }
	    File unregDir = new File(unregDirParent + File.separatorChar +
					regDirName);
	    Debug.trace3("Recursively deleting unreg "+unregDir);
	    if (unregDir.exists()) {
		recursiveDelete(unregDir);
	    }

	    // Now move the current reg directory to the backup
	    failDirBackup.mkdirs();
	    regDir.renameTo(failDirBackup);

	    throw e;
	} finally {
	    // Need to clean up open files
	    if (fos != null) {
		fos.close();
	    }
	}
    }

    // recursively delete a directory
    private void recursiveDelete(File dir) {
	if (!dir.isDirectory()) {
	    // This is a file, just delete it
	    Debug.trace3("rec: Deleting file "+dir);
	    dir.delete();
	} else {
	    // This is a directory. Find all its files
	    File[] fArray = dir.listFiles();
	    for (int i = 0; i < fArray.length; i++) {
		// delete all the entries.
		recursiveDelete(fArray[i]);
	    }
	    // Now delete the directory.
	    Debug.trace3("rec: Deleting dir "+dir);
	    dir.delete();
	}
    }

    void doUnregistry(File unregDir) throws Exception {
	// First make sure that a registry has been done
	String regDirName = unregDir.getName();
	// Get the directory where unregistry info is stored when the CIMOM
	// does a registry.
	String unregDirString = unregDirParent + File.separatorChar +
	    regDirName;
	File regInfo = new File(unregDirString);
	if (!regInfo.exists()) {
	    // This means that a corresponding registry has not been done.
	    // We'll ignore this unregister request.
	    Debug.trace2(unregDir+" not registered");
	    // Must log this
	    // Move to the fail directory
	    String failDirBackupString = failDirParent + 
	    File.separatorChar + regDirName + File.separatorChar + "unreg";
	    File failDirBackup = new File(failDirBackupString);
	    Debug.trace3("Cleaning up old backup if any "+
	    failDirBackupString);
	    recursiveDelete(failDirBackup);
	    failDirBackup.mkdirs();
	    unregDir.renameTo(failDirBackup);
	    return;
	}

	// Find the file which contains the unreg info
	File unregFile;
	// We'll first check in the unreg directory
	File[] fArray = unregDir.listFiles();

	if (fArray.length == 0) {
	    // No unreg file present. We must use the default one that was
	    // created in regInfo
	    String unregFileName = unregDirString + 
	    File.separatorChar + regDirName + UNREGSUFFIX;
	    unregFile = new File(unregFileName);
	} else {
	    unregFile = fArray[0];
	}

	try {
	    int mode = CIM_Mofc.UNREGMODE;
	    Debug.trace3("Parsing testunreg.mof");
	    // We do not pass an unreg handler to MofregClient since this
	    // is an unregistry operation, which we will not undo.
	    CIM_Mofc.parseMOF(new String[] {"-v", unregFile.getPath()}, 
	    mode, new MofregClient(cimom, mode, null));

	    // Ok now we must clean all the registry info.

	    Debug.trace3("Cleaning up reg info for "+regInfo);
	    recursiveDelete(regInfo);
	    // Now remove the unreg directory also
	    Debug.trace3("Removing "+unregDir);
	    recursiveDelete(unregDir);

	} catch (Exception e) {
	    logException(e);
	    // failure in unreg - copy to failDir
	    String failDirBackupString = failDirParent + 
	    File.separatorChar + regDirName + File.separatorChar + "unreg";

	    Debug.trace3("Failed - renaming to "+failDirBackupString);
	    File failDirBackup = new File(failDirBackupString);
	    Debug.trace3("Cleaning up old backup if any "+
	    failDirBackupString);

	    recursiveDelete(failDirBackup);
	    failDirBackup.mkdirs();
	    unregDir.renameTo(failDirBackup);
	    // Remove the unreg directory for this registry
	    Debug.trace3("Cleaning up reg info for "+regInfo);
	    recursiveDelete(regInfo);

	    unregFile.renameTo(new File(failDirBackupString +
	    unregFile.getName()));
	}
    }

    // Utility method to sort files in 1 or 2 directories.
    private List getSortedFileList(File dir1, File dir2) {

	File[] fArray = dir1.listFiles();
	List fList = new ArrayList();
	for (int i = 0; i < fArray.length; i++) {
	    fList.add(fArray[i]);
	}

	// If a second directory exists, then get the entries in that directory
	// too.
	if (dir2 != null) {
	    fArray = dir2.listFiles();
	    for (int i = 0; i < fArray.length; i++) {
		fList.add(fArray[i]);
	    }
	}

	Collections.sort(fList,
	    // This comparator compares files according to their last modified
	    // time. If their modified times are the same, the sort algo
	    // determines the order. Since Collections.sort is a stable sort, 
	    // it will be based on the order in which the file list was 
	    // returned.
	    new Comparator() {
		public int compare(Object fin1, Object fin2) {
		    // fin1, fin2 must be class File
		    File f1, f2;
		    f1 = (File)fin1;
		    f2 = (File)fin2;
		    // Not checking for null because sort shouldnt be passing
		    // in nulls.
		    long f1lm = f1.lastModified();
		    long f2lm = f2.lastModified();
		    if (f1lm > f2lm) return 1;
		    if (f1lm < f2lm) return -1;
		    return 0;
		}
	    } // End comparator class
	); // End sort call
	return fList;
    }

    // Synchronized method. Only one register/unregister operation should be 
    // allowed at any given time.
    // This method takes care of the registration process. 
    //
    // It goes through the preReg and preUnreg directories in ascending order 
    // of last modification time. It is in these directories that the
    // mofreg command and other applications store registry and unregistry info.
    // All info in the preReg directory is used for register operations and
    // all info in the unreg directory is used for unregister operations.
    // These operations are processed in modification time order.
    //
    synchronized void mofReg() throws Exception {
	Debug.trace3("Looking for reg info in "+preRegDirParent+
	" and "+preUnregDirParent);

	File preRegDirP = new File(preRegDirParent);
	File preUnregDirP = new File(preUnregDirParent);
	// Phase 1
	// First thing we do is sort the entries in the preRegDir and 
	// preUnregDir in ascending order of modification time. This is the
	// order in which their entries will be processed. The entries in these
	// directories should themselves be directories.
	List fl = getSortedFileList(preRegDirP, preUnregDirP);
	Iterator i = fl.iterator();
	Debug.trace3("Processing pre reg entries");
	while (i.hasNext()) {
	    File dirEntry = (File)i.next();
	    // names of prereg/preunreg entries cannot end with .unreg
	    if (dirEntry.getName().endsWith(UNREGSUFFIX)) {
		// ignore this entry for now
		recursiveDelete(dirEntry);
		continue;
	    }

	    if (dirEntry.getParentFile().equals(preRegDirP)) {
		// Since this entry is under the prereg directory, this
		// is a registry action.
		Debug.trace3("Handling preReg for "+dirEntry);
		try {
		    doRegistry(dirEntry);
		} catch (Exception e) {
		    Debug.trace1("Registry "+dirEntry+" failed", e);
		    // Must log this
		    logException(e);
		}
	    } else {
		// Since this entry is under the prereg directory, this
		// is an unregistry action.
		Debug.trace3("Handling preUnReg for "+dirEntry);
		try {
		    doUnregistry(dirEntry);
		} catch (Exception e) {
		    Debug.trace1("UnRegistry "+dirEntry+" failed", e);
		    // Must log this
		    logException(e);
		}
	    }
	}

	Debug.trace3("Done Processing reg/unreg entries");
    }
}
