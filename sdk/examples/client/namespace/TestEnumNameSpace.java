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

import java.util.Enumeration;

import javax.wbem.cim.*;
import javax.wbem.client.CIMClient;
import javax.wbem.client.UserPrincipal;
import javax.wbem.client.PasswordCredential;

/**
 * Enumerates the first level child namespaces under the specified parent namespace
 * on the specified host.
 */
public class TestEnumNameSpace {
    public static void main(String args[]) throws CIMException {
	CIMClient cc = null;
	String protocol = CIMClient.CIM_XML;
	if (args.length < 4) {
	    System.out.println("Usage: TestEnumNameSpace host user passwd parentNS [rmi|http]");
	    System.exit(1);
	}
	try {
	    // Create the parent namespace object
	    String host = args[0];
	    String ParentNameSpace = args[3];
	    CIMNameSpace cns = new CIMNameSpace(host, ParentNameSpace);

	    UserPrincipal up = new UserPrincipal(args[1]);
	    PasswordCredential pc = new PasswordCredential(args[2]);
	    if (args.length == 5 && args[4].equalsIgnoreCase("rmi")) {
	    	protocol = CIMClient.CIM_RMI;
	    }

	    // Connect to the parent namespace	
	    cc = new CIMClient(cns, up, pc, protocol);

	    // Create CIMObjectPath to parent namespace
	    CIMObjectPath cop = new CIMObjectPath(ParentNameSpace);

	    // NOTE: deep flag is going away	     
	    System.out.println("enumNameSpace(objectpath, deep) enumerates the");
	    System.out.println("   first level child namespaces under " + ParentNameSpace);
	    Enumeration e = cc.enumNameSpace(cop, false);
	    while (e.hasMoreElements()) {
		CIMObjectPath nsop = (CIMObjectPath)e.nextElement();
		System.out.println(nsop); 
	    }
    
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

