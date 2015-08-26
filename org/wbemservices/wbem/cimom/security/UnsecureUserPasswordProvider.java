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
package org.wbemservices.wbem.cimom.security;

import javax.wbem.cim.CIMException;
import javax.wbem.client.Debug;

import java.io.File;
import java.io.FileOutputStream;

/**
 * The UnsecureUserPasswordProvider class is an implementation of
 * the user security provider which allows all user access without
 * checking for valid password values.
 *
 * @author      Sun Microsystems, Inc.
 * @version     1.6, 01/28/02
 * @since       WBEM 2.5
 */

public final class UnsecureUserPasswordProvider
		implements UserPasswordProvider {

    private final static String EMPTYHASH = "AA0iBY3PDwjYo";
    private final static String FILE_PREFIX = "loc";
    private final static byte[] BAD_VALUE = { 11, 10, 13 };

    public String getEncryptedPassword(String username, int usertype)
	throws CIMException {
	// Needs to be implemented by the user
	// for platforms other then Solaris (Linux, Nt) 
	// If roles are not supported, return null for a role type.
	if (usertype == UserPasswordProvider.ROLE_USER) {
	    return (null);
	}
	Debug.trace1("Unsecure pswd provider: returning empty hash for " +
				username);
	return (EMPTYHASH);
    }

    public String writeLocalAuthenticator(String username, String dirname,
	String value) throws CIMException {

	// Needs to be implemented by the user.
	// Since we are really not supporting a secure connection,
	// we write into a file via Java I/O.  Unfortunately, we cannot
	// control the ownership or security settings of the file.

	String filename = null;
	File fd1 = null;
	File fd2 = null;
	FileOutputStream fos = null;

	// Get the value as a byte array.  If null or empty, assume
	// special hexadecimal bad value.
	byte[] vb = null;
	try {
	    vb = value.getBytes("UTF-8");
	} catch (Exception ex) {
	}
	if ((vb == null) || (vb.length == 0)) {
	    vb = BAD_VALUE;
	}

	// Get a temporary file in the given directory, which must exist.
	// Write the value to the file.
	try {
	    fd1 = new File(dirname);
	    fd2 = File.createTempFile(FILE_PREFIX, null, fd1);
	    filename = fd2.getName();
	    fos = new FileOutputStream(fd2);
	    fos.write(vb);
	    fos.close();
	} catch (Exception ex) {
	    if (fos != null) {
		try {
		    fos.close();
		} catch (Exception e) {
		}
	    }
	    throw new CIMException(CIMException.CIM_ERR_FAILED,
				"CANNOT_WRITE_AUTHENTICATOR");
	}

	return (filename);

    }

    public boolean authenticateUser(String username, String password) {
        // Needs to be implemented by the user
        // for platforms other then Solaris (Linux, Nt)
	Debug.trace1("Unsecure pswd provider: allowing authentication for " +
				username);
	return (true);
    }

    public boolean authenticateRole(String rolename, String password,
	String roleuser) {
        // Needs to be implemented by the user
        // for platforms other then Solaris (Linux, Nt)
	Debug.trace1(
	    "Unsecure pswd provider: allowing authentication for role " +
				rolename);
	return (true);
    }

    public void auditLogin(String clientHostName, String username,
	long success) throws CIMException {
	// Optionally implemented to log audit trail
    }

} // UnsecureUserPasswordProvider
