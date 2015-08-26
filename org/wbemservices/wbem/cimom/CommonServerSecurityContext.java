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

/**
  * The CommonServerSecurityContext interface defines a small set
  * of accessor methods which can be called by the CIMOM server
  * to return security information to providers.  This interface
  * will be implemented by both the WBEM ServerSecurity and
  * Viper ServerSecurityContext infrastructure server objects.
  * Each infrastructure will set the current session's server security
  * instance reference caste to this interface into a ThreadLocal
  * variable in the XXXX class in this same WBEM server Java package.
  * This thread local variable must be set for every remote method
  * call from a WBEM client, and for every intra-server call from
  * the Viper server (or any of its services).
  * <p>
  * The XXXX class contains a static method to return the reference
  * to the server security instance implementing this interface.
  * Per session information can then be retrieved from the interface
  * accessor methods.  Additional WBEM security information can be
  * obtained by casting this reference to the WBEM ServerSecurity
  * instance (if the caste fails, its a Viper ServerSecurityContext
  * instance and cannot be used for further information).
  *
  * @version    1.2 03/01/01
  * @author	Sun Microsystems, Inc.
  */
public interface CommonServerSecurityContext {

	// ================================================================
	//
	// Public accessor methods
	//
	// ================================================================

	/**
	 * The getUserName method returns the user name of the client
	 * authenticated on this secure session.
	 *
	 * @return The client user name
	 *
	 */
	public String getUserName();

	/**
	 * The getRoleName method returns the role name of the role identity
	 * assumed by the client on this authenticated on this secure session.
	 * If no role was assumed, a null reference is returned.
	 *
	 * @return The client role name
	 *
	 */
	public String getRoleName();

	/**
	 * The getClientHostName method returns the name of the client host.
	 *
	 * @return The client host name
	 *
	 */
	public String getClientHostName();

	/**
	 * The getAuditId method returns a mostly unique identifier value
	 * for the authenticated session.  It can be used to identify the
         * secure session for auditing purposes.
	 *
	 * @return The session audit identifier
	 */
	public int getAuditId();

} // CommonServerSecurityContext
