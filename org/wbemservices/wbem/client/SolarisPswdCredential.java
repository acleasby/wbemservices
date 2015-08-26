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

package org.wbemservices.wbem.client;

import javax.wbem.client.PasswordCredential;

/** 
 * 
 * This class implements a password based credential for a Solaris
 * user authentication.  It should be used in conjunction with the
 * SolarisUserPrincipal instance.  It contains a user login password
 * and optionally contains a role login password.
 * 
 * @author	Sun Microsystems, Inc. 
 * @version 	1.3, 04/06/01
 * @since	WBEM 2.0
 */
 
public class SolarisPswdCredential extends PasswordCredential {

    // Private instance attributes
    private byte [] rolepswd;	// Solaris role password

    // Constructors

    /**
     * Empty constructor.
     */
    public SolarisPswdCredential() {

	super();
	rolepswd = new byte[MAX_PASSWORD_SIZE];
	super.zap(rolepswd);

    }

    /**
     * This constructor accepts the Solaris user login password.
     *
     * @param userPassword	The Solaris user login password.
     *
     */
    public SolarisPswdCredential(String userPassword) {

	super(userPassword);
	rolepswd = new byte[MAX_PASSWORD_SIZE];
	super.zap(rolepswd);

    }

    /**
     * This constructor accepts the Solaris user login password and the
     * password of the Solaris role that the user will assume.
     *
     * @param userPassword	The Solaris user login password.
     * @param rolePassword	The Solaris role password.
     *
     */
    public SolarisPswdCredential(String userPassword,
	String rolePassword) {

	super(userPassword);
	rolepswd = new byte[MAX_PASSWORD_SIZE];
        if ((rolePassword != null) && (rolePassword.trim().length() > 0)) {
            super.encode(rolepswd, rolePassword);
        } else {
            this.zap(rolepswd);
        }

    }

    // ==============================================================

    // Accessor methods

    /**
     * Return the password for the Solaris role that the principal
     * is assuming on the Solaris server.
     *
     * @return	The Solaris role password.
     */
    public String getRolePassword() {

        String str;
        if ((rolepswd[0] == 0) && (rolepswd[1] == 0)) {
            str = "";
        } else {
            str = super.decode(rolepswd);
        }
        return (str);

    }

    // Mutator methods

    /**
     * Set the password for the Solaris role that the principal
     * is assuming on the Solaris server.
     *
     * @param rolePassword	The Solaris role password.
     *
     */
    public void setRolePassword(String rolePassword) {

        if ((rolePassword != null) && (rolePassword.trim().length() > 0)) {
            super.encode(rolepswd, rolePassword);
        } else {
            this.zap(rolepswd);
        }

    }

    /**
     * Clear the role password.
     *
     */
    public void clearRolePassword() {

	this.zap(rolepswd);

    }

} // SolarisPswdCredential
