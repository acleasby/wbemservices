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
import java.util.Vector;
import java.util.StringTokenizer;

import javax.wbem.client.*;
import javax.wbem.cim.*;

import javax.wbem.client.UserPrincipal;
import javax.wbem.client.PasswordCredential;


/**
 * Enumerates the objectpaths to classes associated to the class specified 
 * in the command line. Works in the default namespace root/cimv2.
 */
public class Associators {

    public static void main(String args[]) throws CIMException {
	CIMClient cc = null;
	CIMObjectPath cop = null;
	String protocol = CIMClient.CIM_XML;
	if (args.length < 6) {
	    System.out.println("Usage: Associators host user passwd classname test_case " +
		"assocClass|resultClass|role|resultRole|resultClass,propertyName [rmi|http]");
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
	    System.out.println("associators(objectpath, assocClass=null, ");
	    System.out.println("   resultClass=null, role=null, resultRole=null, ");
	    System.out.println("   !includeQualifier, !includeClassOrigin, ");
	    System.out.println("   propertyList[]=null) returns");
	    System.out.println("   descriptions of classes and subclasses ");
	    System.out.println("   associated to " + ClassName);
	    System.out.println("   with all inherited and local properties");
	    System.out.println("   no qualifiers");
	    System.out.println("   no class origin attributes");
	    e = cc.associators(cop, null, null, null, null, false, false, null);
	    for (; e.hasMoreElements(); System.out.println(e.nextElement()));
	    System.out.println("+++++");

	} else if (TestCase.equalsIgnoreCase("assocClass")) {
	    String assocClass = args[5];
 	    System.out.println("associators(objectpath, assocClass, resultClass=null, ");
	    System.out.println("   role=null, resultRole=null, !includeQualifier, ");
	    System.out.println("   !includeClassOrigin, propertyList[]=null) returns");
	    System.out.println("   descriptions of classes and subclasses associated ");
	    System.out.println("   to " + ClassName + " via association " + assocClass );
	    System.out.println("   with all inherited and local properties");
	    System.out.println("   no qualifiers");
	    System.out.println("   no class origin attributes");
	    e = cc.associators(cop, assocClass, null, null, null, false, false, null);
	    for (; e.hasMoreElements(); System.out.println(e.nextElement()));
	    System.out.println("+++++");

	} else if (TestCase.equalsIgnoreCase("resultClass")) {
	    String resultClass = args[5];
	    System.out.println("associators(objectpath, assocClass=null, resultClass, ");
	    System.out.println("   role=null, resultRole=null, !includeQualifier, ");
	    System.out.println("   !includeClassOrigin, propertyList[]=null) returns");
	    System.out.println("   descriptions of classes and subclasses of ");
	    System.out.println("   " + resultClass + " associated to " + ClassName );
	    System.out.println("   with all inherited and local properties");
	    System.out.println("   no qualifiers");
	    System.out.println("   no class origin attributes");
	    e = cc.associators(cop, null, resultClass, null, null, false, false, null);
	    for (; e.hasMoreElements(); System.out.println(e.nextElement()));
	    System.out.println("+++++");

	} else if (TestCase.equalsIgnoreCase("role")) {
	    String role = args[5];
	    System.out.println("associators(objectpath, assocClass=null, resultClass=null, ");
	    System.out.println("   role, resultRole=null, !includeQualifier, ");
	    System.out.println("   !includeClassOrigin, propertyList[]=null) returns");
	    System.out.println("   descriptions of classes and subclasses associated to ");
	    System.out.println("   " + ClassName + " where " + ClassName + " is the " + role);
	    System.out.println("   with all inherited and local properties");
	    System.out.println("   no qualifiers");
	    System.out.println("   no class origin attributes");
	    e = cc.associators(cop, null, null, role, null, false, false, null);
	    for (; e.hasMoreElements(); System.out.println(e.nextElement()));
	    System.out.println("+++++");

	} else if (TestCase.equalsIgnoreCase("resultRole")) {
	    String resultRole = args[5];
	    System.out.println("associators(objectpath, assocClass=null, resultClass=null, ");
	    System.out.println("   role=null, resultRole, !includeQualifier, ");
	    System.out.println("   !includeClassOrigin, propertyList[]=null) returns");
	    System.out.println("   descriptions of classes and subclasses associated to ");
	    System.out.println("   " + ClassName + " that are " + resultRole + " references");
	    System.out.println("   with all inherited and local properties");
	    System.out.println("   no qualifiers");
	    System.out.println("   no class origin attributes");
	    e = cc.associators(cop, null, null, null, resultRole, false, false, null);
	    for (; e.hasMoreElements(); System.out.println(e.nextElement()));
	    System.out.println("+++++");

	} else if (TestCase.equalsIgnoreCase("includeQualifier")) {
	    String includeQualifier = args[5];
	    System.out.println("associators(objectpath, assocClass=null, resultClass=null, ");
	    System.out.println("   role=null, resultRole=null, includeQualifier, ");
	    System.out.println("   !includeClassOrigin, propertyList[]=null) returns");
	    System.out.println("   descriptions of classes and subclasses associated ");
	    System.out.println("   to " + ClassName);
	    System.out.println("   with all inherited and local properties");
	    System.out.println("   all qualifiers");
	    System.out.println("   no class origin attributes");
	    e = cc.associators(cop, null, null, null, null, true, false, null);
	    for (; e.hasMoreElements(); System.out.println(e.nextElement()));
	    System.out.println("+++++");

	} else if (TestCase.equalsIgnoreCase("includeClassOrigin")) {
	    String includeClassOrigin = args[5];
	    System.out.println("associators(objectpath, assocClass=null, resultClass=null, ");
	    System.out.println("   role=null, resultRole=null, !includeQualifier, ");
	    System.out.println("   includeClassOrigin, propertyList[]=null) returns");
	    System.out.println("   descriptions of classes and subclasses associated ");
	    System.out.println("   to " + ClassName);
	    System.out.println("   with all inherited and local properties");
	    System.out.println("   no qualifiers");
	    System.out.println("   class origin attributes");
	    e = cc.associators(cop, null, null, null, null, false, true, null);
	    for (; e.hasMoreElements(); System.out.println(e.nextElement()));
	    System.out.println("+++++");

	} else if (TestCase.equalsIgnoreCase("propertyList")) {
	    StringTokenizer st = new StringTokenizer(args[5], ",");
	    String resultClass = st.nextToken();
	    String property = st.nextToken();
	    System.out.println("associators(objectpath, assocClass=null, resultClass, ");
	    System.out.println("   role=null, resultRole=null, includeQualifier, ");
	    System.out.println("   !includeClassOrigin, propertyList[]=" + property + ")");
	    System.out.println("   returns descriptions of classes and subclasses of ");
	    System.out.println("   " + resultClass + " associated to " + ClassName );
	    System.out.println("   with all inherited and local properties");
	    System.out.println("   all qualifiers");
	    System.out.println("   no class origin attributes");
	    System.out.println("   where property name=" + property);
	    Vector properties = new Vector();
	    properties.addElement(property);
	    String[] propertyList = new String[properties.size()];
	    properties.toArray(propertyList);
	    e = cc.associators(cop, null, resultClass, null, null, true, false, propertyList);
	    for (; e.hasMoreElements(); System.out.println(e.nextElement()));
	    System.out.println("+++++");

/* NOT IMPLEMENTED YET
	} else if (TestCase.equalsIgnoreCase("objectPath")) {
	    System.out.println("associators(objectpath) returns");
	    System.out.println("   descriptions of classes and subclasses ");
	    System.out.println("   associated to " + ClassName);
	    System.out.println("   with all inherited and local properties");
	    System.out.println("   all qualifiers");
	    System.out.println("   no class origin attributes");
	    e = cc.associators(cop);
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

