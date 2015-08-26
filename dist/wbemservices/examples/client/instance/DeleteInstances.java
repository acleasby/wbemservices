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

import javax.wbem.cim.*;
import javax.wbem.client.CIMClient;
import javax.wbem.client.UserPrincipal;
import javax.wbem.client.PasswordCredential;

import java.util.Enumeration;

/**
 * This example program takes command-line arguments and deletes all
 * instances of the specified class and its subclasses. The user must 
 * specify the username and password of the administrative account
 * for the CIM Object Manager repository.
 * 
 */
public class DeleteInstances {
    public static void main(String args[]) throws CIMException {

	// set default protocol to HTTP
	String protocol = CIMClient.CIM_XML;

    	 // Initialize an instance of the CIM Client class
	CIMClient cc = null;
	
	// Requires 4 command-line arguments.
	// If not all entered, prints command string.	
	if (args.length < 4) {
	    System.out.println
	        ("Usage: DeleteInstance host className username password " + 
		 "[rmi|http]");
	    System.exit(1);
	}
	try {
	
	    /**
	     * Creates a name space object (cns), which stores the host name
	     * (args[0]) from the command line.
	     */ 
	     
	    CIMNameSpace cns = new CIMNameSpace(args[0]);
    
	    /**
	     * Creates the user principal and password credential for
	     * authenticating the user to the CIMOM.
	     */
	    UserPrincipal up = new UserPrincipal(args[2]);
	    PasswordCredential pc = new PasswordCredential(args[3]);

	    /**
	     * Check if user wants to use rmi protocol
	     */
	    if (args.length == 5 && args[4].equalsIgnoreCase("rmi")) {
	    	protocol = CIMClient.CIM_RMI;
	    }

	    /** 
	     * Connects to the CIM Object Manager, and passes it the
	     * name space object (cns), the user principal,
	     * password credential and transport protocol.
	     */

	    cc = new CIMClient(cns, up, pc, protocol);

	    /**
	     * Construct an object containing the CIM object path
	     * of the class to delete (args[1]) from the command line.
	     */
            
	    CIMObjectPath cop = new CIMObjectPath(args[1]);
	    
	    /**
	     * Do a deep enumeration (true) of the class,
	     * which will print all the subclasses of the class.
	     */
	     
	    Enumeration e = cc.enumerateInstanceNames(cop);
	    
	    /**
	     * Iterate through the instances in the enumeration.
	     * Construct an object to store the object path of each
	     * enumerated instance, print the instance, and then 
	     * delete it.
	     */
	     
	    while (e.hasMoreElements()) {
		CIMObjectPath op = (CIMObjectPath)e.nextElement();
		System.out.println(op);
		cc.deleteInstance(op);
	    }
	}
	catch (Exception e) {
	    System.out.println("Exception: "+e);
	}
	if (cc != null) {
	    cc.close();
	}
    }
}

