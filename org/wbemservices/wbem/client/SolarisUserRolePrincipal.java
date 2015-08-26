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

import javax.wbem.client.UserPrincipal;




/** 
 * 
 * This class implements a Java security Principal identity for
 * a client user identity that authenticates with a Solaris username
 * and login password.  An optional Solaris role name may be
 * specified.
 * 
 * @author	Sun Microsystems, Inc. 
 * @version 	1.3 04/06/01 
 * @since	WBEM 2.0
 */
 
public class SolarisUserRolePrincipal extends UserPrincipal {

    // Private instance attributes
    private String rolename;	// Solaris role name

    // Constructors

    /**
     * Empty constructor.
     *
     */
    public SolarisUserRolePrincipal() {

	super();
	rolename = null;

    }

    /**
     * This constructor accepts the Solaris user name.
     *
     */
    public SolarisUserRolePrincipal(String userName) {

	super(userName);
	rolename = null;

    }

    /**
     * This constructor accepts the Solaris user name and the
     * Solaris role name that the user will assume.
     *
     * @param userName	The Solaris user login name.
     * @param roleName	The name of the Solaris role to be assumed.
     *
     */
    public SolarisUserRolePrincipal(String userName, String roleName) {

	super(userName);
	rolename = roleName;

    }

    // ==============================================================

    // Methods defined in the Principal interface

    /**
     * Return the name of this principal identity.  The user name
     * is returned, even if a role name is being assumed.
     *
     * @return	The name of this principal identity.
     *
     */
    public String getName() {

	return (super.getUserName());

    }

    /**
     * The equals method checks if the specified object is the same
     * principal as this object.  The principals are equal if the
     * specified object is an instance of SolarisUserRolePrincipal and
     * the user name, role name, and authentication host name are
     * the same.
     *
     * @param otherPrincipal	Principal instance to compare for equality
     *
     * @return	The name of this principal identity.
     *
     */
    public boolean equals(Object otherPrincipal) {

	// See if other principal is our type!
	SolarisUserRolePrincipal op = null;
	try {
	    op = (SolarisUserRolePrincipal) otherPrincipal;
	} catch (Exception ex) {
	    return (false);
	}

	// Use a dummy do-while for more structured code.
	// We do not call our superclass equals method, as the
	// hash code will likely not be equal if a role name exists.
	// Only go through once.
	boolean bool = false;
	do {

	    int ohash = op.hashCode();
	    if (this.hashCode() != ohash) {
		break;
            }

	    String ouser = op.getUserName();
	    String tuser = super.getUserName();
	    if ((tuser == null) && (ouser != null)) {
		break;
            }
	    if ((tuser != null) && (ouser == null)) {
		break;
            }
	    if ((tuser != null) && (! tuser.equals(ouser))) {
		break;
            }

	    String orole = op.getRoleName();
	    if ((rolename == null) && (orole != null)) {
		break;
            }
	    if ((rolename != null) && (orole == null)) {
		break;
            }
	    if ((rolename != null) && (! rolename.equals(orole))) {
		break;
            }

	    String ohost = op.getHostName();
	    String thost = super.getHostName();
	    if ((thost == null) && (ohost != null)) {
		break;
            }
	    if ((thost != null) && (ohost == null)) {
		break;
            }
	    if ((thost != null) && (! thost.equals(ohost))) {
		break;
            }

	    bool = true;

	} while (false);

	return (bool);

    }

    /**
     * The toString method returns a string representation of the
     * principal suitable for displaying in messages.  It should
     * not be used for making authorization checks, however.
     * The format of the returned string is "user (as role) @ host".
     *
     * @return	A printable string form of the principal identity.
     *
     */
    public String toString() {

	String name = "";
	String tuser = super.getUserName();
	String thost = super.getHostName();
	if (tuser != null) {
	    name = name + tuser;
        }
	if (rolename != null) {
	    name = name + " (as " + rolename + ")";
        }
	if (thost != null) {
	    name = name + " @ " + thost;
        }
	return (name);

    }

    /**
     * The hashCode method returns an integer hash code to represent
     * this principal.  It can be used to test for non-equality, or
     * as an index key in a hash table.
     *
     * @return	An integer hash code representing the principal.
     *
     */
    public int hashCode() {

	int result = 0;
	String tuser = super.getUserName();
	String thost = super.getHostName();
	if (tuser != null) {
	    result += tuser.hashCode();
        }
	if (rolename != null) {
	    result += rolename.hashCode();
        }
	if (thost != null) {
	    result += thost.hashCode();
        }
	return (result);

    }

    // Extended accessor methods

    /**
     * Return the Solaris role name this principal is assuming.
     *
     * @return	The Solaris role name.
     *
     */
    public String getRoleName() {

	return (rolename);
    }

    /**
     * Returns true if a role has been assumed.
     *
     */
    public boolean isRoleAssumed() {

	return (rolename == null);

    }

    // Extend mutator methods

    /**
     * Set the Solaris role name this principal will assume when
     * authenticated to a Solaris server.
     *
     * @param roleName	The Solaris role name.
     *
     */
    public void setRoleName(String roleName) {

	rolename = roleName;

    }

} // SolarisUserRolePrincipal
