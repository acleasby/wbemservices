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
 * Gets the first instance of the class specified in the command line. 
 * Works in the default namespace root/cimv2.
 */
public class GetProperty {
    public static void main(String args[]) throws CIMException {
	CIMClient cc = null;
	CIMObjectPath cop = null;
	String protocol = CIMClient.CIM_XML;
	if (args.length < 6) {
	    System.out.println("Usage: GetProperty host user passwd classname " +
		"keyname=key[,keyname=key] property [rmi|http]");
	    System.exit(1);
	}
	try {
	    CIMValue cv = null;
	    CIMNameSpace cns = new CIMNameSpace(args[0]);
	    UserPrincipal up = new UserPrincipal(args[1]);
	    PasswordCredential pc = new PasswordCredential(args[2]);
	    if (args.length == 7 && args[6].equalsIgnoreCase("rmi")) {
	    	protocol = CIMClient.CIM_RMI;
	    }
	    cc = new CIMClient(cns, up, pc, protocol);

	    // Get the class name from the command line
	    String ClassName = args[3];
	    StringTokenizer stKey = new StringTokenizer(args[4], "=");
	    String keyName = stKey.nextToken();
	    String key = stKey.nextToken();
	    String property = args[5];

	    // Create objectpath to the instance
	    cop = new CIMObjectPath(ClassName);
	    cop.addKey(keyName, new CIMValue(key));

	    System.out.println("getProperty(objectpath, propertyName) ");
	    System.out.println("   returns property=" + property);
	    System.out.println("   from instance " + key + " of " + ClassName);
	    cv = cc.getProperty(cop, property);
	    System.out.println(cv);
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
} // GetProperty
