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

import javax.wbem.client.*;
import javax.wbem.cim.*;

import javax.wbem.client.UserPrincipal;
import javax.wbem.client.PasswordCredential;


/** 
 * This example enumerates classes and instances. It does deep and shallow
 * enumerations on a class that is passed from the command line
 */
public class ClientEnum {

    public static void main(String args[]) throws CIMException {
	CIMClient cc = null;
	CIMObjectPath cop = null;
	String protocol = CIMClient.CIM_XML;
	if (args.length < 4) {
	    System.out.println("Usage: ClientEnum host user passwd classname " +
			       "[rmi|http]");
	    System.exit(1);
	    }
	try {
	    CIMNameSpace cns = new CIMNameSpace(args[0]);
	    UserPrincipal up = new UserPrincipal(args[1]);
	    PasswordCredential pc = new PasswordCredential(args[2]);
	    if (args.length == 5 && args[4].equalsIgnoreCase("rmi")) {
	    	protocol = CIMClient.CIM_RMI;
	    }
	    cc = new CIMClient(cns, up, pc, protocol);


	    // Get the class name from the command line
	    cop = new CIMObjectPath(args[3]);
	    // Do a deep enumeration of the class
	    Enumeration e = cc.enumerateClasses(cop, true, true, true, true);
	    // Will print out all the subclasses of the class.
	    for (; e.hasMoreElements(); System.out.println(e.nextElement()));
	    System.out.println("+++++");
	    // Do a shallow enumeration of the class
	    e = cc.enumerateClasses(cop, false, true, true, true);
	    // Will print out the first level subclasses.
	    for (; e.hasMoreElements(); System.out.println(e.nextElement()));
	    System.out.println("+++++");
	    // Do a deep enumeration of the instances of the class
	    e = cc.enumerateInstances(cop, false, true, true, true, null);
	    // Will print out all the instances of the class and its subclasses.
	    for (; e.hasMoreElements(); System.out.println(e.nextElement()));
	    System.out.println("+++++");
	    // Do a shallow enumeration of the instances of the class
	    e = cc.enumerateInstances(cop, false, false, true, true, null);
	    // Will print out all the instances of the class.
	    for (; e.hasMoreElements(); System.out.println(e.nextElement()));
	    System.out.println("+++++");

	    e = cc.enumerateInstanceNames(cop);
	    for (; e.hasMoreElements(); System.out.println(e.nextElement()));
	    System.out.println("+++++");

	    e = cc.enumerateInstanceNames(cop);
	    while (e.hasMoreElements()) {
		CIMObjectPath opInstance = (CIMObjectPath)e.nextElement();
		CIMInstance ci = cc.getInstance(opInstance, false, 
						true, true, null);
		System.out.println(ci); 
	    }
	    System.out.println("+++++");

	}
	catch (Exception e) {
	    System.out.println("Exception: "+e);
	}

	// close session.
	if (cc != null) {
	    cc.close();
	}
    }
}

