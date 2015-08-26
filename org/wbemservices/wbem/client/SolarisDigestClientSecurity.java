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

import java.security.Principal;

import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMNameSpace;

/**
 * This class implements a Solaris specific challenge-response
 * authentication mechanism.  It adds the ability to authenticate
 * a Solaris role after the client user identity has been authenticated.
 * Currently, it assumes the communications service has a method to
 * deliver the role authentication exchange to the remote CIMOM server.
 *
 * @author 	Sun Microsystems, Inc.
 * @since	WBEM 1.0
 */
public class SolarisDigestClientSecurity extends SunDigestClientSecurity {

    // Our security mechanism name.
    // Must align with the mechanism name in the WbemDefaults properties!
    private static final String mechanism = "solarisdigest";

    private String roleName;
    private byte[] rolePswd;

    public SolarisDigestClientSecurity()
	throws java.security.NoSuchAlgorithmException {

	super();
	this.roleName = null;
	this.rolePswd = new byte[0];
    }

    public SolarisDigestClientSecurity(CIMNameSpace ns, Principal prin,
	Object cred) throws java.security.NoSuchAlgorithmException,
	CIMException {

	super();
	this.setNameSpace(ns);
	this.setPrincipal(prin);
	this.setCredential(cred);
    }

    public String getMechanism() {
        return SolarisDigestClientSecurity.mechanism;
    }

    public void setPrincipal(Principal prin) throws CIMException {
	super.setPrincipal(prin);
	this.roleName = null;
	if (prin instanceof org.wbemservices.wbem.client.SolarisUserRolePrincipal) {
	    this.roleName = ((SolarisUserRolePrincipal) prin).getRoleName();
	}
    }

    public void setCredential(Object cred) throws CIMException {

	super.setCredential(cred);
	if (cred instanceof org.wbemservices.wbem.client.SolarisPswdCredential) {
	    String tPswd = ((SolarisPswdCredential) cred).getRolePassword();
	    if (tPswd != null) {

		// Convert string characters to byte array explicitly
		// to avoid byte converter on local system.  Only support
		// ISO-Latin (8-bit) password.
		int len = tPswd.length();
		rolePswd = new byte[len];
		for (int i = 0; i < len; i++) {
		    char c = tPswd.charAt(i);
		    rolePswd[i] = (byte)(c);
		}
	    }
	}
    }

    public void dispose() {
	super.dispose();
	this.roleName = null;
	java.util.Arrays.fill(this.rolePswd, (byte)0x00);
	this.rolePswd = new byte[0];
    }

    public String getRoleName() {
	return roleName;
    }

    public String getRolePassword() {
	// Only support passwords of ISO-Latin 8-bit characters.
	// We build the characters explicitly to avoid the default
	// character converter on the system.
	String tPswd = "";
	if (rolePswd.length > 0) {
	    char [] ca = new char[rolePswd.length];
	    for (int i = 0; i < rolePswd.length; i++) {
		ca[i] = (char) (0 | (rolePswd[i] & 0xff));
	    }
	    tPswd = new String(ca);
	}
	return tPswd;
    }
}
