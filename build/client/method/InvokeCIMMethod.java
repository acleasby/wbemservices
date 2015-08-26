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
 * Invoke the method in the class specified in the command line. 
 * Works in the default namespace root/cimv2.
 */

public class InvokeCIMMethod {
    public static void main(String args[]) throws CIMException {
	CIMClient cc = null;
	CIMObjectPath cop = null;
	String protocol = CIMClient.CIM_XML;
	if (args.length < 6) {
	    System.out.println("Usage: InvokeCIMMethod host user passwd classname " +
			       "keyname=key[,keyname=key] test_case [rmi|http]");
	    System.exit(1);
	}
	try {
	    CIMNameSpace cns = new CIMNameSpace(args[0]);
	    UserPrincipal up = new UserPrincipal(args[1]);
	    PasswordCredential pc = new PasswordCredential(args[2]);
	    if (args.length == 7 && args[6].equalsIgnoreCase("rmi")) {
	    	protocol = CIMClient.CIM_RMI;
	    }
	    cc = new CIMClient(cns, up, pc, protocol);

	    // Get the instance name from the command line
	    String ClassName = args[3];
	    StringTokenizer stKey = new StringTokenizer(args[4], "=");
	    String keyName = stKey.nextToken();
	    String key = stKey.nextToken();
	    cop = new CIMObjectPath(ClassName);
	    cop.addKey(keyName, new CIMValue(key));

	    // The testcase is the method name
	    String TestCase = args[5];
	    String methodName = TestCase;

	    CIMValue returnCode = null;

	if (TestCase.equalsIgnoreCase("Sum")) {
	    System.out.println("invokeMethod(objectpath, methodName, inArgs, outArgs) returns");
	    System.out.println("   the sum of two inParam integers in outArgs");

	    // Allocate inArgs and initialize its elements
	    CIMArgument inArgs[] = new CIMArgument[2];
	    CIMValue number1 = new CIMValue(new Integer(2));
	    CIMValue number2 = new CIMValue(new Integer(5));
	    // According to the specification, order cannot be assumed
	    // To demonstrate, place number2 as the 1st element
	    inArgs[0] = new CIMArgument("number2", number2);
	    inArgs[1] = new CIMArgument("number1", number1);
	    System.out.println("inArgs: " + inArgs[0] + "=" + inArgs[0].getValue() +
	    	"," + inArgs[1] + "=" + inArgs[1].getValue());

	    // Allocate outArgs, but do not initialize any elements
	    CIMArgument[] outArgs = new CIMArgument[1];

	    returnCode = cc.invokeMethod(cop, methodName, inArgs, outArgs);
	    System.out.println("outArgs: " + outArgs[0] + "=" + outArgs[0].getValue());
	    System.out.println("+++++");

	} else if (TestCase.equalsIgnoreCase("NextTwo")) {
	    System.out.println("invokeMethod(objectpath, methodName, inArgs, outArgs) returns");
	    System.out.println("   the next two integers after inParam in outArgs");

	    // Allocate inArgs and initialize its elements
	    CIMArgument inArgs[] = new CIMArgument[1];
	    CIMValue start = new CIMValue(new Integer(2));
	    inArgs[0] = new CIMArgument("start", start);
	    System.out.println("inArgs: " + inArgs[0] + "=" + inArgs[0].getValue());

	    // Allocate outArgs, but do not initialize any elements
	    CIMArgument[] outArgs = new CIMArgument[2];

	    returnCode = cc.invokeMethod(cop, methodName, inArgs, outArgs);

	    // According to the specification, order cannot be assumed.
	    // So check CIMArgument.name
	    int length = outArgs.length;
	    int first = 0;
	    int second = 0;
	    for (int i = 0; i < length; i++) {
		if (outArgs[i].getName().equalsIgnoreCase("first")) {
		    first = i;
		} else if (outArgs[i].getName().equalsIgnoreCase("second")) {
		    second = i;
		}
	    }
	    System.out.println("outArgs: " + outArgs[first] + "=" + outArgs[first].getValue() +
	    	"," + outArgs[second] + "=" + outArgs[second].getValue());
	    System.out.println("+++++");

	} else if (TestCase.equalsIgnoreCase("Greeting")) {
	    System.out.println("invokeMethod(objectpath, methodName, inArgs, outArgs) returns");
	    System.out.println("   a greeting string in outArgs");

	    // Since no input arguments are defined, allocate a zero-length inArgs
	    CIMArgument inArgs[] = new CIMArgument[0];

	    // Allocate outArgs, but do not initialize any elements
	    CIMArgument[] outArgs = new CIMArgument[1];

	    returnCode = cc.invokeMethod(cop, methodName, inArgs, outArgs);

	    System.out.println("outArgs: " + outArgs[0] + "=" + outArgs[0].getValue());
	    System.out.println("+++++");

	} else if (TestCase.equalsIgnoreCase("Reference")) {
	    System.out.println("invokeMethod(objectpath, methodName, inArgs, outArgs)");
	    System.out.println("   uses REF in inArgs, no outArgs");

	    // Allocate inArgs and initialize its elements
	    CIMArgument inArgs[] = new CIMArgument[1];
	    CIMObjectPath inop = new CIMObjectPath("Class_N");
	    CIMValue key_n = new CIMValue("N1.1");
	    inop.addKey("keyN", key_n);
	    CIMValue class_n = new CIMValue(inop);
	    inArgs[0] = new CIMArgument("class_n", class_n);
	    System.out.println("inArgs: " + inArgs[0] + "=" + inArgs[0].getValue());

	    // Since no output arguments are defined, allocate a zero-length outArgs
	    CIMArgument[] outArgs = new CIMArgument[0];

	    returnCode = cc.invokeMethod(cop, methodName, inArgs, outArgs);

	    System.out.println("outArgs: none to return");
	    System.out.println("+++++");

	} else {
	    System.out.println("Unknown test case");
	} // if/then/else

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
} // InvokeMethod
