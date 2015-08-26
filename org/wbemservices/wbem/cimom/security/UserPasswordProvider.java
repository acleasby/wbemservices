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

/**
 * This interface is provided to have a platform independent version for user
 * authentication. The reference implementation uses this interface to perform 
 * authentication checks. The name of the class which implements this interface
 * is provided in the WBEM configuration properties.
 */

public interface UserPasswordProvider {

    /**
     * The property used to define the user password provider
     * implementation class.
     */
    public static final String PSWD_PROV_PROP = "org.wbemservices.wbem.cimom.pswdprov";

    /**
     * The user to be authenticated may be of any user type.
     */
    public static final int ANY_USER_TYPE = 0;

    /**
     * The user to be authenticated must be a normal user who can
     * log into the operating system on the server.
     */
    public static final int NORMAL_USER = 1;

    /**
     * The user to be authenticated must be an RBAC role type.
     */
    public static final int ROLE_USER = 2;

    /**
     * Get the Unix DES encrypted password for the specified user.
     * The user type is one of the define constants in this interface.
     * A null String reference is returned if the user does not exist
     * as a valid user identity or the user exists but is not of the
     * specified type.  If the user exists but does not have a password,
     * a password hash is generated with a default salt and an empty
     * string.  Authenticating users without passwords is checked in
     * the authenticateUser method.  This method should always return
     * a password hash if the user is valid.
     *
     * @param username The user name.
     * @param usertype The type of user.
     * @return DES encrypted string.
     */
    public String getEncryptedPassword(String username, int usertype)
	throws CIMException;

    /**
     * Write a local authenticator into a file in the WBEM security
     * directory and return the unique file name.  The file mode is
     * set such that only the specified user can read the file.
     *
     * @param username  The user being authenticated.
     * @param dirname   The security directory to create the file in.
     * @param value     The authenticator value to be written to the file.
     *
     * @return          The name of the file containing the authenticator
     */
    public String writeLocalAuthenticator(String username, String dirname,
	String value) throws CIMException;

    /**
     * Authenticate the specified user with the specified clear text
     * password.  If the user does not exist, or the password does
     * not match false is returned.
     * WBEM requires that a user must have a non-empty password.
     *
     * @param username	The name of the user being authenticated
     * @param password	The clear text password for that user
     * @return true if the user is authenticated; false otherwise
     */
    public boolean authenticateUser(String username, String password)
	throws CIMException;

    /**
     * Authenticate the specified role with the specified clear text
     * password and the user that is assumiming this role.
     * If the role does not exist, user is not in this role,
     * or the password does not match, false is returned.
     * WBEM requires that a user must have a non-empty password.
     *
     * @param rolename	The name of the role being authenticated
     * @param password	The clear text password for that role
     * @param username	The user assuming this role
     * @return true if the role is authenticated; false otherwise
     */
    public boolean authenticateRole(String rolename, String password,
	String username)
	throws CIMException;
    /**
     * Write an audit record for the login attempt.
     * success is one of
     *
     * org.wbemservices.wbem.cimom.ServerSecurity.AUDIT_NO_SUCH_USER
     * org.wbemservices.wbem.cimom.ServerSecurity.AUDIT_BAD_PASSWD
     * org.wbemservices.wbem.cimom.ServerSecurity.AUDIT_SUCCESS
     *
     * @param clientHostName	The name of the remote client host
     * @param username	The name of the user being authenticated
     * @param success	Indication of authentication results
     */
    public void auditLogin(String clientHostName, String username, 
	long success) throws CIMException;

} // UserPasswordProvider
