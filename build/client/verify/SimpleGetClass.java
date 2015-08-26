/*
 *EXHIBIT A - Sun Industry Standards Source License
 *
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

import javax.wbem.client.CIMClient;
import javax.wbem.client.UserPrincipal;
import javax.wbem.client.PasswordCredential;
import javax.wbem.cim.CIMNameSpace;
import javax.wbem.cim.CIMObjectPath;
import javax.wbem.cim.CIMClass;
import javax.wbem.cim.CIMException;

/* 
 * This verification program gets the definition of
 * a class; i.e. if the CIMOM is running properly,
 * the MOF description of the class will be displayed
 * Command line arguments are:
 * args[0] - hostname CIMOM is running on
 * args[1] - class name to get description for
 * args[2] - optional transfer protocol (default is http)
 */

public class SimpleGetClass {
    public static void main(String args[]) throws CIMException {
	CIMClient cc = null;
	CIMObjectPath cop = null;
	CIMClass cclass = null;
	String protocol = CIMClient.CIM_XML;
	if (args.length < 4) {
	    System.out.println("Usage: GetClass hostname user password classname " +
			       "[rmi|http]");
	    System.exit(1);
    }
	try {
	    CIMNameSpace cns = new CIMNameSpace(args[0]);
	    UserPrincipal lup = new UserPrincipal(args[1]);
	    PasswordCredential lpc = new PasswordCredential(args[2]);
	    if (args.length == 5 && args[4].equalsIgnoreCase("rmi")) {
	    	protocol = CIMClient.CIM_RMI;
	    }
	    cc = new CIMClient(cns, lup, lpc, protocol);

	    // Get the class name from the command line
	    cop = new CIMObjectPath(args[3]);
		// Get class definition
		cclass = cc.getClass(cop, false);
		// Print out the class definition
		System.out.println(cclass);
	} // try
	catch (Exception e) {
	    System.out.println("Exception: "+e);
	    e.printStackTrace();
	} // catch

	// close session.
	if (cc != null) {
	    cc.close();
	} // if
    } // main
} // GetClass
