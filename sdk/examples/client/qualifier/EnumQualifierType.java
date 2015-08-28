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
 * Enumerates the qualifiers defined in the specified namespace.
 */
public class EnumQualifierType {
    public static void main(String args[]) throws CIMException {
	CIMClient cc = null;
	String protocol = CIMClient.CIM_XML;
	if (args.length < 4) {
	    System.out.println("Usage: CreateQualifierType host user passwd parentNS" +
			       "[rmi|http]");
	    System.exit(1);
	}
	try {
	    String Host= args[0];
	    String ParentNameSpace = args[3];
	    CIMNameSpace cns = new CIMNameSpace(Host, ParentNameSpace);
	    UserPrincipal up = new UserPrincipal(args[1]);
	    PasswordCredential pc = new PasswordCredential(args[2]);
	    if (args.length == 4 && args[4].equalsIgnoreCase("rmi")) {
	    	protocol = CIMClient.CIM_RMI;
	    }
	    cc = new CIMClient(cns, up, pc, protocol);

	    // Enumerates all the qualifier types in the current namespace
	    System.out.println("enumQualifierTypes(objectpath) all the qualifiers");
	    System.out.println("   in namespace " + ParentNameSpace + " on " + Host);
	    CIMObjectPath qtop = new CIMObjectPath(Host);
	    Enumeration enumeration = cc.enumQualifierTypes(qtop);
	    while (enumeration.hasMoreElements()) {
		CIMQualifierType qt = (CIMQualifierType)enumeration.nextElement();
		System.out.println(qt); 
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

