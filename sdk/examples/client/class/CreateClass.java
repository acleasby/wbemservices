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

import javax.wbem.client.*;
import javax.wbem.cim.*;

import javax.wbem.client.UserPrincipal;
import javax.wbem.client.PasswordCredential;

/**
 * Creates the class specified in the command line. Works in the default
 * namespace root/cimv2.
 */
public class CreateClass {
    public static void main(String args[]) throws CIMException {
	CIMClient cc = null;
	CIMObjectPath cop = null;
	String protocol = CIMClient.CIM_XML;
	if (args.length < 5) {
	    System.out.println("Usage: CreateClass host user passwd classname " +
			       "superClass [rmi|http]");
	    System.exit(1);
	}
	try {
	    CIMNameSpace cns = new CIMNameSpace(args[0]);
	    UserPrincipal up = new UserPrincipal(args[1]);
	    PasswordCredential pc = new PasswordCredential(args[2]);
	    if (args.length == 6 && args[5].equalsIgnoreCase("rmi")) {
	    	protocol = CIMClient.CIM_RMI;
	    }
	    cc = new CIMClient(cns, up, pc, protocol);

	    // Get the class name from the command line
	    String ClassName = args[3];
	    String SuperClassName = args[4];

	    // Set the superClass
	    CIMClass cclass = new CIMClass(ClassName);
	    cclass.setSuperClass(SuperClassName);

	    // Add a property
	    CIMProperty prop = new CIMProperty("uint32G3");
	    prop.setType(new CIMDataType(CIMDataType.UINT32));
	    cclass.addProperty(prop);

	    // Create the class
	    cop = new CIMObjectPath(ClassName);
	    cc.createClass(cop, cclass);

	} // try
	catch (Exception e) {
	    System.out.println("Exception: "+e);
	} // catch

	// close session.
	if (cc != null) {
	    cc.close();
	} // if
    } // main
} // GetClass
