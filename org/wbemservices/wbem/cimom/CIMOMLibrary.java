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
 *are Copyright © 2003 Sun Microsystems, Inc.
 *
 *All Rights Reserved.
 *
 *Contributor(s): _______________________________________
*/

package org.wbemservices.wbem.cimom;

import javax.wbem.cim.CIMException;
import javax.wbem.client.CIMSecurityException;
import javax.wbem.client.Debug;
import org.wbemservices.wbem.client.WorkMod51;

/**
 * The CIMOMLibrary class contains common method interfaces
 * to native method implementations in the management server shared
 * library, libadmsvr.so. This has been copied from the seabreeze
 * CIMOMLibrary.java
 */
public class CIMOMLibrary {

    // Static define constants

    public final static String CIMOM_LIBRARY = "cimom";

    // Private attributes
    private    static    boolean    library_loaded = false;

   // Public methods

    /**
     * The getEncryptedPassword method retrieves the encrypted password
     * for the specified username from the Solaris shadow table in the
     * management server.  This function returns encrypted passwords for
     * Solaris user accounts by calling the getspnam function of the OS.
     * If the user does not exist, a null string is returned.
     *   
     * @param   username   The username of the administrator principal
     * @param	usertype   The type of user.  See define constants in
     * UserPasswordProvider
     *   
     * @return  Returns the encrypted password of the specified user
     *
     * @author     Sun Microsystems, Inc.
     */

    public static String getEncryptedPassword(String username, int usertype)
	throws CIMException {

	// Private attributes
	String encrypted_password = null;

	// All functions must check if the library is loaded...
	if (library_loaded) {
	    encrypted_password = getPassword(username, usertype);
	    if (encrypted_password == null) {
		return null;
	    }
	    if (encrypted_password.trim().length() < 2) {
		    return "";
            }
	} else {
	    // Log this error
	    Debug.trace1("Cannot find user password provider library");
	    throw new CIMSecurityException(
	    	CIMSecurityException.NO_SUCH_PRINCIPAL, username);
	}

	// Note that if sf is less than two bytes 
	// ArrayIndexOutOfBoundsException can be thrown and therefore
	// the test above for the length of encrypted_password
	// Also mod51Format requires the salt to have at least
	// two bytes as well or will throw ArrayIndexOutOfBoundsException
	// as well.

	// Check if the password matches that of an empty password
	// DES encrypted with a salt
	byte sf[] = encrypted_password.getBytes();
	byte[] salt = new byte[] { sf[0], sf[1] };
	byte [] tf = (new WorkMod51()).mod51Format(new byte[20], salt);
	String tfs = new String(tf);
	if (tfs.equals(encrypted_password)) {
	    return "";
	}
	return (encrypted_password);

    }

    /**
     * The authenticateUser method authenticates the user and password
     * using PAM (Pluggable Authentication Framework).
     *   
     * @param   username   The username of the administrator principal
     * @param	password   The password for this username
     *   
     * @return  Returns true if the user is authenticated, false if not.
     *
     * @author     Sun Microsystems, Inc.
     */
    public static boolean authenticateUser(String username, String password)
	throws CIMException {

	if (username == null || username.trim().length() == 0 ||
	    password == null || password.trim().length() == 0) {
	    return (false);
	}
	return (authenticateUserOrRole(username, password, null));
    }

    /**
     * The authenticateRole method authenticates the role and password
     * for the specified roleuser using PAM (Pluggable Authentication
     * Framework). roleuser cannot be null or "".
     *   
     * @param   rolename   The role of the administrator principal
     * @param	password   The password for this role
     * @param	roleuser   The user that will assume this role
     *   
     * @return  Returns true if the role is authenticated, false if not.
     *
     * @author     Sun Microsystems, Inc.
     */
    public static boolean authenticateRole(String rolename, String password,
	String roleuser)
	throws CIMException {

	if (password == null || password.trim().length() == 0 ||
	    rolename == null || rolename.trim().length() == 0 ||
	    roleuser == null || roleuser.trim().length() == 0) {
	    return (false);
	}
	return (authenticateUserOrRole(rolename, password, roleuser));
    }

    /* Uses PAM (Pluggable Authentication Framework) to authenticate
     * users or roles. roleUser is null when authenticating users
     * Callers must ensure that password is not "" since wbem
     * does not allow "" passwords.
     */
    private static boolean authenticateUserOrRole(String userOrRole,
	String password, String roleUser) 
	throws CIMException {

	// All functions must check if the library is loaded...
	if (library_loaded) {
	    if (password == null) {
		return false;
	    }
	    if (password.trim().length() == 0) {
		    password = "";
            }
	} else {
	    // Log this error
	    Debug.trace1("Cannot find user password provider library");
	    throw new CIMSecurityException(
		CIMSecurityException.NO_SUCH_PRINCIPAL, userOrRole);
	}

	boolean authenticated = false;
	// Catch and ignore exceptions other than sending the exception
	// to the debug log, and return false
	try {
	    authenticated = doAuthenticate(userOrRole, password, roleUser);
	} catch(Exception ex) {
	    /* If we get here we don't know if authErrmsg[0] is valid */
	    Debug.trace1(
		"Unexpected exception from native method doAuthenticate: ", ex);
	}

	if (!authenticated) {
	    if (roleUser != null) {
		Debug.trace1(
		    "CIMOMLibrary.authenticateUserOrRole : " +
		    "failed to authenticate role " + userOrRole + 
		    " for user " + roleUser);
	    } else {
		Debug.trace1(
		    "CIMOMLibrary.authenticateUserOrRole : " +
		    "failed to authenticate user " + userOrRole);
	    }
	}
	return (authenticated);
    }

    /**
     * Write the specified value byte array into a unique temporary file
     * in the specified directory.  Change the ownership of the file to
     * the specified user and change its mode so only that user can read
     * the file.  Return the name of the file.
     *
     * @param username  The name of the user being authenticated
     * @param dir       The name of the directory to write the file
     * @param value     The authenticator value to be written to the file
     *
     * @return  The name of the file that was created.
     */
    public static String writeLocalAuthenticator(String username, String dir,
	String value) throws CIMException {

	String filename = null;

	// All functions must check if the library is loaded...
	if (library_loaded) {
	    filename = writeLocalFile(username, dir, value);
	} else {
	    // Log this error
	    Debug.trace1("Cannot find passwd access library");
	    throw new CIMSecurityException(CIMException.CIM_ERR_FAILED);
	}
	return (filename);
    }

    /**
     * The writeToSyslog method sends the message from the log service to
     * syslogd(1M). The log service invokes this method when a writeRecord call
     * specifies that syslog should be notified. The message parameter contains
     * the detailed message from the log record or the summary message - if the
     * detailed message does not exist.
     * 
     * @param message The Log record detailed/summary message
     * @param identity Identity of the admin server
     * @param severity Severity of the log message (info/warning/error)
     */
    public static void writeToSyslog(String message, String identity, 
    int severity) throws CIMException {


	// All functions must check if the library is loaded...
	if (library_loaded) {
	    doSyslog(message, identity, severity);
	} else {
	    // Log this error
	    throw new CIMException("Cannot log");
	}
    }


    /**
     * The auditLogin method is used to log to the 
     * audit trail the fact that a login attempt was made. The success
     * or failure of this attempt is indicated via the success
     * parameter. One of:
     *
     * org.wbemservices.wbem.cimom.ServerSecurity.AUDIT_NO_SUCH_USER
     * org.wbemservices.wbem.cimom.ServerSecurity.AUDIT_BAD_PASSWD
     *
     * for failures and
     *
     * org.wbemservices.wbem.cimom.ServerSecurity.AUDIT_SUCCESS
     *
     * for success.
     *   
     * @param 	hostName The client's hostname.
     *
     * @param   userName The client user's id. 
     *
     * @param   success int indicating success of the
     * 		    login operation.
     *
     */
    public static synchronized void auditLogin(String hostName, 
					    String userName, 
					    long success) 
	throws CIMException {
		
	if ((hostName == null) || (userName == null)) {
	    return;
        }

	// All functions must check if the library is loaded...
	if (library_loaded) {
	    // extract all fields and seed the login call with
	    // info
	    setupAuditLogin(hostName, userName);
	    if (success == org.wbemservices.wbem.cimom.ServerSecurity.AUDIT_SUCCESS) {
		auditLoginSuccess(hostName, userName, (int)success);
	    } else {
		auditLoginFailure(hostName, userName, (int)success);
	    }
	} else {
	    // Log this error?
	    throw new CIMException(
	    		"Error loading library - cannot audit login");
	}
    }

    // Internal methods


    private	static native String getPassword(String username,
						int usertype);

    private	static native String writeLocalFile(String username,
						String dir,
						String value);

    private	static native void doSyslog(String message,
					String identity, 
					int severity);


    private	static native void setupAuditLogin(String hostName,
						String userName);

    private	static native void auditLoginSuccess(String hostName,
						String userName,
						long sessionId);

    private	static native void auditLoginFailure(String hostName,
						String userName,
						int failure_code);

    private	static native boolean doAuthenticate(String userOrRole,
						String password,
						String roleuser)
		throws Exception;

    // Static class section to load native shared library

    static {

	try {
	    System.loadLibrary(CIMOM_LIBRARY);
	    library_loaded = true;
	}
	catch (SecurityException exception) {
	    // Log this error
	    // Will throw an exception later when methods are accessed
	    Debug.trace1("Unexpected SecurityException: ", exception);
	}
	catch (UnsatisfiedLinkError exception) {
	    // Log this error
	    // Will throw an exception later when methods are accessed
	    Debug.trace1("Unexpected UnsatisfiedLinkError: ", exception);
	}

    }

}
