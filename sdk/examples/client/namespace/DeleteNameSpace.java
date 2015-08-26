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

/**
 * This example program deletes the specified name space on the
 * specified host. The user must specify the username and 
 * password of the administrative account for the CIM Object Manager 
 * repository.
 */
public class DeleteNameSpace {

    public static void main(String args[]) throws CIMException {
    
        // Initialize an instance of the CIM Client class
	CIMClient cc = null;

        // Set default protocol to HTTP
	String protocol = CIMClient.CIM_XML;
	
	// Requires at least 5 command-line arguments.
	// If not all entered, prints command string.	
	if (args.length < 5) {
	    System.out.println("Usage: DeleteNameSpace host parentNS " +
			       "childNS username password [rmi|http]"); 
	    System.exit(1);
	}
	try {
	
	    /**
	     * Creates a name space object (cns), which stores the host 
	     * name and parent name space.
	     */ 
	     
	    CIMNameSpace cns = new CIMNameSpace(args[0], args[1]);
    
	    /**
	     * Creates the user principal and password credential used
	     * to authenticate the user to the CIMOM.
	     */
	    UserPrincipal up = new UserPrincipal(args[3]);
	    PasswordCredential pc = new PasswordCredential(args[4]);
	    if (args.length == 6 && args[5].equalsIgnoreCase("rmi")) {
	    	protocol = CIMClient.CIM_RMI;
	    }

	    /** 
	     * Connects to the CIM Object Manager, and passes it the
	     * namespace object (cns) and the user principal and password
	     * credential and protocol. 
	     */
	    cc = new CIMClient(cns, up, pc, protocol);


	    /**
	     * Creates another name space object (cop), which stores the
	     * a null string for the host name and a string for the
	     * child name space (from the command-line arguments).
	     */
	   
	    CIMNameSpace cop = new CIMNameSpace("", args[2]);
	    
	    /**
	     * Deletes the child name space under the parent name space.
	     */ 
	     
	    cc.deleteNameSpace(cop);
	}
	catch (Exception e) {
	    System.out.println("Exception: "+e);
	}

	// Close the session
	if (cc != null) {
	    cc.close();
	}
    }
}

