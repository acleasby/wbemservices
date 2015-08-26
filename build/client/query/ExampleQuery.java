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
import java.util.Stack;

import javax.wbem.cim.*;
import javax.wbem.client.*;
import javax.wbem.client.UserPrincipal;
import javax.wbem.client.PasswordCredential;

/**
 * This is an example on how to use the execQuery functionality.  It will
 * create a test class with several properties and create some test instances of
 * that class.  It will then execute some querys on that class and display the 
 * results.  At the end, it will remove the class and instances that were 
 * created.
 */
public class ExampleQuery {

    // instances will keep track of the instances created and need to be removed
    // at the cleanup stage
    Stack instances;

    public ExampleQuery(String args[]) {
	CIMClient cc = null;
	// If no transfer protocol is given, HTTP is the default 
	String protocol = CIMClient.CIM_XML;
	if (args.length < 3) {
	    System.out.println("Usage: ExampleQuery hostname user passwd " +
			       "[rmi|http]"); 
	    System.exit(1);
	    }
	try {
	    // created a CIMNameSpace object with the hostname given.  It
	    // will default to the root/cimv2 namespace.
	    CIMNameSpace cns = new CIMNameSpace(args[0]);

	    // create UserPrincipal and PasswordCredential objects with
	    // the username and password given.  If this user does not
	    // have write privileges to the root/cimv2 namespace, this
	    // example program will not work
	    UserPrincipal up = new UserPrincipal(args[1]);
	    PasswordCredential pc = new PasswordCredential(args[2]);

	    // if 4 arguments are passed on the command line and the last
	    // argument is rmi, connect using the rmi protocol
	    if (args.length == 4 && args[3].equalsIgnoreCase("rmi")) {
	    	protocol = CIMClient.CIM_RMI;
	    }

	    // create a CIMClient object 
	    cc = new CIMClient(cns, up, pc, protocol);

	    // initialize the instances stack (used to save list of instances
	    // created and need to be deleted at end)
	    instances = new Stack();

	    // create a test class on which we will run queries on 
	    CIMClass testClass = createTestClass(cc);
	    // create test instances of the test class
	    createTestInstances(cc, testClass);
	    // run queries on the test class nd display resulting instances
	    performQueryTests(cc);
	    // remove test class and test instances
	    cleanup(cc);
	    // if we have error an creating CIMClient, display trace
	} catch (CIMException e) {
	    e.printStackTrace();
	}

	// close session.
	if (cc != null) {
	    try {
		cc.close();
	    } catch (CIMException e) {
		System.out.println("Error closing CIMClient"); 
	    }
	}
    }

    /**
     * Create a test class with several properties in which we will run 
     * queries against.  
     *
     * @param cc    CIMClient object
     * @return      CIMClass created by this method 
     */
    private CIMClass createTestClass(CIMClient cc) {
	CIMClass cClass = null;
	try { 
	    cClass = new CIMClass("Test_QueryTest");
	    // create a property called KeyProperty and set a "key" qualifier
	    // to it
	    CIMProperty keyProp = new CIMProperty("KeyProperty");
	    keyProp.setType(new CIMDataType(CIMDataType.UINT16));
	    CIMQualifier cq = new CIMQualifier("key");
	    cq.setValue(new CIMValue(new Boolean(true)));
	    keyProp.addQualifier(cq);
	    cClass.addProperty(keyProp);

	    // create a couple of extra properties
	    CIMProperty testProp = new CIMProperty("TestProperty");
	    testProp.setType(new CIMDataType(CIMDataType.UINT16));
	    cClass.addProperty(testProp);
	    CIMProperty anotherProp = new CIMProperty("AnotherProperty");
	    anotherProp.setType(new CIMDataType(CIMDataType.BOOLEAN));
	    cClass.addProperty(anotherProp);

	    // call createClass to add this class to the CIM Object Manager
	    cc.createClass(new CIMObjectPath("Test_QueryTest"), cClass); 
	    
	} catch (CIMException e) {
	    System.out.println("Could not create class"); 
	    e.printStackTrace();
	}
	return cClass;
    }

    /**
     * Create test instances of the test class
     *
     * @param cc      CIMClient object
     * @param cClass  The test CIMClass 
     */
    private void createTestInstances(CIMClient cc, CIMClass cClass) {
	addInstance(cc, cClass, 23, 33, true);
	addInstance(cc, cClass, 11, 88, false);
	addInstance(cc, cClass, 55, 4, true);
	addInstance(cc, cClass, 122, 9, false);
	addInstance(cc, cClass, 1, 32, true);
	addInstance(cc, cClass, 77, 21, false);
	addInstance(cc, cClass, 7, 7, true);
    }
    
    /**
     * Perform a set of query examples.
     *
     * @param cc      CIMClient object
     */
    private void performQueryTests(CIMClient cc) {
	System.out.println("Find all instances of Test_QueryTest");
	execQuery(cc, "select * from Test_QueryTest");
	System.out.println("Find all instances of Test_QueryTest where " +
			   "KeyProperty > 50");
	execQuery(cc, "select * from Test_QueryTest where " +
		  "KeyProperty > 50");
    }

    /**
     * Perform the execQuery call and displays the results
     *
     * @param cc      CIMClient object
     * @param query   The query string to be used in the execQuery call 
     */
    private void execQuery(CIMClient cc, String query) {
	System.out.println("Query string is: " + query);
	try {
	    // call the execQuery method
	    Enumeration e = cc.execQuery(new CIMObjectPath(), query,
	  				 CIMClient.WQL);
	    System.out.println("resulting instances are:"); 
	    // print out the resulting instances
	    while(e.hasMoreElements()) {
		CIMInstance ci = (CIMInstance)e.nextElement();
		System.out.println(ci);
	    }
	} catch (CIMException exc) {
	    System.out.println("error doing execQuery");
	    exc.printStackTrace();
	}
	System.out.println(""); 
    } 

    /**
     * Adds instances of the test class.
     *
     * @param cc            CIMClient object
     * @param cClass        The test CIMClass 
     * @param keyValue      int value to set to the KeyValue property    
     * @param testValue     int value to set to the TestValue property    
     * @param anotherValue  boolean value to set to the AnotherValue property    
     */
    private void addInstance(CIMClient cc, CIMClass cClass, int keyValue, 
			     int testValue, boolean anotherValue) {
	try {
	    // create instance of test class and set it's properties
	    CIMInstance ci = cClass.newInstance();
	    ci.setProperty("KeyProperty", new CIMValue(new Integer(keyValue)));
	    ci.setProperty("TestProperty", new CIMValue(new Integer(testValue)));
	    ci.setProperty("AnotherProperty", 
			   new CIMValue(new Boolean(anotherValue)));
	    CIMObjectPath op = cc.createInstance(new CIMObjectPath(), ci);
	    if (op != null) {
		// we are saving the CIMObject path of the created instances
		// so we can delete the instances before we exit.  We need
		// to get the relative ObjectPath of the instance to be 
		// able to delete it
		CIMObjectPath relOP = new CIMObjectPath();
		relOP.setObjectName(ci.getClassName());
		relOP.setKeys(ci.getKeys());
		instances.push(relOP);
	    }
	} catch (CIMException exc) {
	    System.out.println("Error creating instance!");
	    exc.printStackTrace();
	} 
    }

    /**
     * Remove temporary instances and the class that were created 
     * for this example
     *
     * @param cc      CIMClient object
     */
    private void cleanup(CIMClient cc) {
	try {
	    // get all instances from list and delete each one 
	    while (!instances.empty()) {
		CIMObjectPath op = (CIMObjectPath)instances.pop();
		cc.deleteInstance(op);
	    }
	    // delete test class
	    cc.deleteClass(new CIMObjectPath("Test_QueryTest"));
	} catch (CIMException exc) {
	    System.out.println("Error cleaning up instances");
	    exc.printStackTrace();
	}
    }

    public static void main(String args[]) {
	new ExampleQuery(args);
    }
}







