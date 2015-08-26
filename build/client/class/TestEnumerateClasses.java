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
 * Enumerates the class specified in the command line. Works in the default
 * namespace root/cimv2.
 */
public class TestEnumerateClasses {

    public static void main(String args[]) throws CIMException {
	CIMClient cc = null;
	CIMObjectPath cop = null;
	String protocol = CIMClient.CIM_XML;
	if (args.length < 5) {
	    System.out.println("Usage: TestEnumerateClasses host user passwd class " +
			       " test_case [rmi|http]");
	    System.exit(1);
	    }
	try {
	    Enumeration e = null;
	    CIMNameSpace cns = new CIMNameSpace(args[0]);
	    UserPrincipal up = new UserPrincipal(args[1]);
	    PasswordCredential pc = new PasswordCredential(args[2]);
	    if (args.length == 6 && args[5].equalsIgnoreCase("rmi")) {
	    	protocol = CIMClient.CIM_RMI;
	    }
	    cc = new CIMClient(cns, up, pc, protocol);

	    // Get the class name from the command line
	    String ClassName = args[3];
	    String TestCase = args[4];
	    cop = new CIMObjectPath(ClassName);

	if (TestCase.equalsIgnoreCase("objectPath")) {
	    System.out.println("enumerateClasses(objectpath) returns:");
	    System.out.println("   only first level children of " + ClassName + " with");
	    System.out.println("   only local properties");
	    System.out.println("   all qualifiers");
	    System.out.println("   no class origin attributes");
	    e = cc.enumerateClasses(cop);
	    for (; e.hasMoreElements(); System.out.println(e.nextElement()));
	    System.out.println("+++++");

	} else if (TestCase.equalsIgnoreCase("deep")) {
	    System.out.println("enumerateClasses(objectpath, deep) returns");
	    System.out.println("   all subclasses of " + ClassName + " with");
	    System.out.println("   only local properties");
	    System.out.println("   all qualifiers");
	    System.out.println("   no class origin attributes");
	    e = cc.enumerateClasses(cop, true);
	    for (; e.hasMoreElements(); System.out.println(e.nextElement()));
	    System.out.println("+++++");

	} else if (TestCase.equalsIgnoreCase("localOnly")) {
	    System.out.println("enumerateClasses(objectpath, !deep, localOnly) returns");
	    System.out.println("   only first level children of " + ClassName + " with");
	    System.out.println("   only local properties");
	    System.out.println("   all qualifiers");
	    System.out.println("   no class origin attributes");
	    e = cc.enumerateClasses(cop, false, true);
	    for (; e.hasMoreElements(); System.out.println(e.nextElement()));
	    System.out.println("+++++");

	} else if (TestCase.equalsIgnoreCase("includeQualifiers")) {
	    System.out.println("enumerateClasses(objectpath, !deep, !localOnly, includeQualifiers) returns");
	    System.out.println("   only first level children of " + ClassName + " with");
	    System.out.println("   all inherited and local properties");
	    System.out.println("   all qualifiers");
	    System.out.println("   no class origin attributes");
	    e = cc.enumerateClasses(cop, false, false, true);
	    for (; e.hasMoreElements(); System.out.println(e.nextElement()));
	    System.out.println("+++++");

	} else if (TestCase.equalsIgnoreCase("includeClassOrigin")) {
	    System.out.println("enumerateClasses(cop, !deep, !localOnly, !includeQualifiers");
	    System.out.println("   includeClassOrigin) returns");
	    System.out.println("   only first level children of " + ClassName + " with");
	    System.out.println("   all inherited and local properties");
	    System.out.println("   no qualifiers");
	    System.out.println("   class origin attributes");
	    e = cc.enumerateClasses(cop, false, false, false, true);
	    for (; e.hasMoreElements(); System.out.println(e.nextElement()));
	    System.out.println("+++++");

	} else if (TestCase.equalsIgnoreCase("all_false")) {
	    System.out.println("enumerateClasses(objectpath, !deep, !localOnly, !includeQualifiers");
	    System.out.println("   !includeClassOrigin) returns");
	    System.out.println("   only first level children of " + ClassName + " with");
	    System.out.println("   all inherited and local properties");
	    System.out.println("   no qualifiers");
	    System.out.println("   no class origin attributes");
	    e = cc.enumerateClasses(cop, false, false, false, false);
	    for (; e.hasMoreElements(); System.out.println(e.nextElement()));
	    System.out.println("+++++");

	} else if (TestCase.equalsIgnoreCase("cimbrowser")) {
	    System.out.println("enumerateClasses(objectpath, deep, localOnly, includeQualifiers");
	    System.out.println("   !includeClassOrigin) returns");
	    System.out.println("   all subclasses of " + ClassName + " with");
	    System.out.println("   only local properties");
	    System.out.println("   all qualifiers");
	    System.out.println("   no class origin attributes");
	    cop = new CIMObjectPath();
	    System.out.println("cop = " + cop);
	    e = cc.enumerateClasses(cop, true, true, true, false);
	    for (; e.hasMoreElements(); System.out.println(e.nextElement()));
	    System.out.println("+++++");

	} else {
	    System.out.println("Unknown TestCase");
	} // if/then/else

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

