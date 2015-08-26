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
 * Enumerates the objectpaths to association classes that refer to the class  
 * specified in the command line. Works in the default namespace root/cimv2.
 */
public class ReferenceNames {

    public static void main(String args[]) throws CIMException {
	CIMClient cc = null;
	CIMObjectPath cop = null;
	String protocol = CIMClient.CIM_XML;
	if (args.length < 6) {
	    System.out.println("Usage: ReferenceNames host user passwd classname " +
		"test_case assocClass|resultClass|role|resultRole [rmi|http]");
	    System.exit(1);
	    }
	try {
	    Enumeration e = null;
	    CIMNameSpace cns = new CIMNameSpace(args[0]);
	    UserPrincipal up = new UserPrincipal(args[1]);
	    PasswordCredential pc = new PasswordCredential(args[2]);
	    if (args.length == 7 && args[6].equalsIgnoreCase("rmi")) {
	    	protocol = CIMClient.CIM_RMI;
	    }
	    cc = new CIMClient(cns, up, pc, protocol);

	    // Get the class name from the command line
	    String ClassName = args[3];
	    String TestCase = args[4];
	    cop = new CIMObjectPath(ClassName);

	if (TestCase.equalsIgnoreCase("all_null")) {
	    System.out.println("referenceNames(objectpath, resultClass=null, role=null) returns");
	    System.out.println("   object paths of association classes and subclasses");
	    System.out.println("   that associate to " + ClassName);
	    e = cc.referenceNames(cop, null, null);
	    for (; e.hasMoreElements(); System.out.println(e.nextElement()));
	    System.out.println("+++++");

	} else if (TestCase.equalsIgnoreCase("resultClass")) {
	    String resultClass = args[5];
	    System.out.println("referenceNames(objectpath, resultClass, role=null) returns");
	    System.out.println("   object paths of association classes and subclasses");
	    System.out.println("   that associate to " + ClassName);
	    System.out.println("   with the name " + resultClass);
	    e = cc.referenceNames(cop, resultClass, null);
	    for (; e.hasMoreElements(); System.out.println(e.nextElement()));
	    System.out.println("+++++");

	} else if (TestCase.equalsIgnoreCase("role")) {
	    String role = args[5];
	    System.out.println("referenceNames(objectpath, resultClass=null, role) returns");
	    System.out.println("   object paths of association classes and subclasses");
	    System.out.println("   that associate to " + ClassName);
	    System.out.println("   as the " + role + " reference");
	    e = cc.referenceNames(cop, null, role);
	    for (; e.hasMoreElements(); System.out.println(e.nextElement()));
	    System.out.println("+++++");

/* NOT IMPLEMENTED YET
	} else if (TestCase.equalsIgnoreCase("objectPath")) {
	    System.out.println("associatorNames(objectpath) returns");
	    System.out.println("   object paths of association classes and subclasses");
	    System.out.println("   that associate to " + ClassName);
	    e = cc.associatorNames(cop);
	    for (; e.hasMoreElements(); System.out.println(e.nextElement()));
	    System.out.println("+++++");
*/
	} else {
	    System.out.println("Unknown test case");
	} // if/then/else

	} // try
	catch (Exception e) {
	    System.out.println("Exception: "+e);
	} // catch

	// close session.
	if (cc != null) {
	    cc.close();
	}
    }
}

