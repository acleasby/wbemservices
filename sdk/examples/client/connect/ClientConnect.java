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
 * Connects to the specified host using the specified protocol.
 * Works in the default namespace root/cimv2.
 */
public class ClientConnect {
    public static void main(String args[]) throws CIMException {
	CIMClient cc = null;
	String protocol = CIMClient.CIM_XML;
	if (args.length < 4) {
	    System.out.println("Usage: ClientConnect host user passwd test_case [rmi|http]");
	    System.exit(1);
	}
	try {
	    CIMNameSpace cns = new CIMNameSpace(args[0]);
	    UserPrincipal up = new UserPrincipal(args[1]);
	    PasswordCredential pc = new PasswordCredential(args[2]);
	    if (args.length == 5 && args[4].equalsIgnoreCase("rmi")) {
	    	protocol = CIMClient.CIM_RMI;
	    }

	    String TestCase = args[3];
	    
	if (TestCase.equalsIgnoreCase("!protocol")) {
	    System.out.println("CIMClient(objectpath, username, password) connects to");
	    System.out.println("   " + args[0] + " as user " + args[1] + " over RMI");
	    cc = new CIMClient(cns, up, pc);
	    
	} else if (TestCase.equalsIgnoreCase("protocol")) {
	    System.out.println("CIMClient(objectpath, username, password, protocol) connects to");
	    System.out.println("   " + args[0] + " as user " + args[1] + " over " + args[4]);
	    cc = new CIMClient(cns, up, pc, protocol);
	    
	} else {
	    System.out.println("Unknown test case");
	} // if/then/else


	} // try
	catch (Exception e) {
	    System.out.println("Exception: "+e);
	} // catch
	
	// Close the session.
	if (cc != null) {
	    cc.close();
	}	
    }
}

