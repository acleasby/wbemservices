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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * A LogHandler represents snapshots and update records as serializable
 * objects.
 * <p>
 * This implementation does not know how to create an initial snaphot or
 * apply an update to a snapshot.  The client must specify these methods
 * via a subclass.
 *
 * @see ReliableLog
 */
public abstract class LogHandler {

    /**
     * Creates a LogHandler for a ReliableLog.
     */
    public LogHandler() {}
    
    /**
     * Writes the snapshot to a stream.  This callback is invoked when
     * the client calls the snaphot method of ReliableLog.
     *
     * @param out the output stream
     *
     * @exception Exception can raise any exception
     */
    public abstract void snapshot(OutputStream out) throws Exception;
    
    /**
     * Read the snapshot from a stream.  This callback is invoked when
     * the client calls the recover method of ReliableLog.  
     *
     * @param in the input stream
     *
     * @exception Exception can raise any exception
     */
    
    public abstract void recover(InputStream in) throws Exception;
    
    /**
     * Writes the representation (a serializable object) of an update 
     * to a stream.  This callback is invoked when the client calls the 
     * update method of ReliableLog.
     *
     * @param out the output stream
     * @param value the update object
     *
     * @exception Exception can raise any exception
     */
    public void writeUpdate(OutputStream out, Object value) throws Exception {
	ObjectOutputStream s = new ObjectOutputStream(out);
 	s.writeObject(value);
	s.flush();
    }
    
    /**
     * Reads a stably logged update (a serializable object) from a
     * stream.  This callback is invoked during recovery, once for
     * every record in the log.  After reading the update, this method
     * invokes the applyUpdate (abstract) method in order to execute
     * the update.
     *
     * @param in the input stream
     *
     * @exception Exception can raise any exception
     */
    public void readUpdate(InputStream in) throws Exception {
	ObjectInputStream  s = new ObjectInputStream(in);
	applyUpdate(s.readObject());
    }

    /**
     * Reads a stably logged update (a serializable object) from a stream.  
     * This callback is invoked during recovery, once for every record in the 
     * log.  After reading the update, this method is invoked in order to
     * execute the update.
     *
     * @param update the update object
     *
     * @exception Exception can raise any exception
     */
    public abstract void applyUpdate(Object update) throws Exception;
    
}
