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
package org.wbemservices.wbem.cimom.security;

import javax.wbem.cim.CIMException;
import javax.wbem.security.UserPasswordEncryptionProvider;
import org.wbemservices.wbem.cimom.CIMOMLibrary;
import org.wbemservices.wbem.client.SolarisPswdEncryptionProvider;

public final class SolarisUserPasswordProvider implements UserPasswordProvider {

    private final static String DFLT_SALT = "AA";
    private UserPasswordEncryptionProvider pep;

    public SolarisUserPasswordProvider() {

	pep = (UserPasswordEncryptionProvider)
			new SolarisPswdEncryptionProvider();

    }

    public String getEncryptedPassword(String username, int type)
	throws CIMException {

	// Lookup the user based on type in the underlying OS passwd
	// table.  If not found or wrong type, we get a null password hash.
	// Return the null to indicate no such principal.  If the hash
	// is empty, we have a user without a password.  Generate a hash
	// for an empty password.  The authenticateUser method will verify
	// if a user without a password is valid.  Note that the special
	// cases of "NP" and "LK" are treated as an empty password; the
	// user exists but we will fail later on the password check.
	String hash = CIMOMLibrary.getEncryptedPassword(username, type);
	if ((hash != null) && (hash.length() < 3)) {
	    hash = pep.encryptPassword(username, DFLT_SALT, "");
	}
	// System.out.println("PWP: user=" + username + "  hash=" + hash);
	return (hash);

    } // getEncryptedPassword

    public String writeLocalAuthenticator(String username, String dir,
	String value) throws CIMException {

	return CIMOMLibrary.writeLocalAuthenticator(username, dir, value);

    } // writeLocalAuthenticator

    public boolean authenticateUser(String username, String password
	) throws CIMException {

	// This provider requires a non-empty password.
	if ((password == null) || (password.trim().length() == 0)) {
	    return (false);
	}

	// Uses PAM

	return CIMOMLibrary.authenticateUser(username, password);

    } // authenticateUser

    public boolean authenticateRole(String rolename, String password,
	String roleuser) throws CIMException {

	// This provider requires a non-empty password.
	if ((password == null) || (password.trim().length() == 0) ||
	    (roleuser == null) || (roleuser.trim().length() == 0)) {
	    return (false);
	}

	// Uses PAM

	return CIMOMLibrary.authenticateRole(rolename, password, roleuser);

    } // authenticateUser

    public void auditLogin(String hostName, String userName, long success)
	throws CIMException {
	
	CIMOMLibrary.auditLogin(hostName, userName, success);

    } // auditLogin

} // SolarisUserPasswordProvider
