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

import java.util.StringTokenizer;

import javax.wbem.client.*;
import javax.wbem.cim.*;

import javax.wbem.client.UserPrincipal;
import javax.wbem.client.PasswordCredential;

/**
 * Creates an instance of the class specified in the command line. 
 * works in the default namespace root/cimv2.
 */
public class CreateInstance {
    public static void main(String args[]) throws CIMException {
	CIMClient cc = null;
	CIMObjectPath cop = null;
	String protocol = CIMClient.CIM_XML;
	if (args.length < 5) {
	    System.out.println("Usage: CreateInstance host user passwd classname " +
		"keyname=key[,keyname=key] [rmi|http]");
	    System.exit(1);
	}
	try {
	    CIMClass cclass = null;
	    CIMNameSpace cns = new CIMNameSpace(args[0]);
	    UserPrincipal up = new UserPrincipal(args[1]);
	    PasswordCredential pc = new PasswordCredential(args[2]);
	    if (args.length == 6 && args[5].equalsIgnoreCase("rmi")) {
	    	protocol = CIMClient.CIM_RMI;
	    }
	    cc = new CIMClient(cns, up, pc, protocol);

	    // Get the class name from the command line
	    String ClassName = args[3];
	    StringTokenizer stKey = new StringTokenizer(args[4], "=");
	    String keyName = stKey.nextToken();
	    String key = stKey.nextToken();

	    // Get the class definition with inherited and local properties
	    cop = new CIMObjectPath(ClassName);
	    boolean localOnly = false;
	    cclass = cc.getClass(cop, localOnly);

	    // Create and initialize a new instance
	    CIMInstance ci = cclass.newInstance();
	    ci.setProperty(keyName, new CIMValue(key));

	    // Create objectpath to the new instance
	    cop.addKey(keyName, new CIMValue(key));

	    System.out.println("createInstance(objectpath, CIMInstance) creates");
	    System.out.println("   instance " + key + " of " + ClassName);
	    cc.createInstance(cop, ci);
	    System.out.println("+++++");

	} // try
	catch (Exception e) {
	    e.printStackTrace();
	    System.out.println("Exception: "+e);
	} // catch

	// close session.
	if (cc != null) {
	    cc.close();
	} // if
    } // main
} // CreateInstance
