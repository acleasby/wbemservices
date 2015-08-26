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
 * Creates the specified child namespace under the specified parent namespace
 * on the specified host.
 */
public class TestCreateNameSpace {
    public static void main(String args[]) throws CIMException {
	CIMClient cc = null;
	String protocol = CIMClient.CIM_XML;
	if (args.length < 5) {
	    System.out.println("Usage: TestCreateNameSpace host user passwd parentNS childNS [rmi|http]");
	    System.exit(1);
	}
	try {
	    // Create the parent namespace object
	    String host = args[0];
	    String ParentNameSpace = args[3];
	    CIMNameSpace cns = new CIMNameSpace(host, ParentNameSpace);

	    UserPrincipal up = new UserPrincipal(args[1]);
	    PasswordCredential pc = new PasswordCredential(args[2]);
	    if (args.length == 6 && args[5].equalsIgnoreCase("rmi")) {
	    	protocol = CIMClient.CIM_RMI;
	    }

	    // Connect to the parent namespace	
	    cc = new CIMClient(cns, up, pc, protocol);

	    // Create the child namespace object by specifying a null hostname
	    String ChildNameSpace = args[4];
	    CIMNameSpace ns = new CIMNameSpace("", ChildNameSpace);
	    
	    System.out.println("createNameSpace(namespace) creates");
	    System.out.println("   child namespace " + ChildNameSpace + " under " + ParentNameSpace);
	    cc.createNameSpace(ns);
	}
	catch (Exception e) {
	    System.out.println("Exception: "+e);
	}

	// Close the session.
	if (cc != null) {
	    cc.close();
	}
    }
}

