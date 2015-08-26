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

import javax.wbem.client.*;
import javax.wbem.client.CIMEvent;
import javax.wbem.client.CIMListener;
import javax.wbem.cim.*;

import javax.wbem.client.UserPrincipal;
import javax.wbem.client.PasswordCredential;


/**
 * Monitors the class specified in the command line for the specified intrinsic 
 * life-cycle event. Works in the default namespace root/cimv2.
 */
public class AddCIMListener {
    public static void main(String args[]) throws CIMException {
	CIMClient cc = null;
	Listener cl = null;
	String protocol = CIMClient.CIM_XML;
	String IndicationType = null;
	if (args.length < 6) {
	    System.out.println("Usage: AddCIMListener host user passwd classname " +
			       "test_case [rmi|http]");
	    System.exit(1);
	}
	try {
	    String host = args[0];
	    String user = args[1];
	    String passwd = args[2];
	    CIMNameSpace cns = new CIMNameSpace(host);
	    UserPrincipal up = new UserPrincipal(user);
	    PasswordCredential pc = new PasswordCredential(passwd);
	    if (args.length == 6 && args[5].equalsIgnoreCase("rmi")) {
	    	protocol = CIMClient.CIM_RMI;
	    }
	    cc = new CIMClient(cns, up, pc, protocol);

	    // Get the class name from the command line
	    String ClassName = args[3];
	    String TestCase = args[4];

	if (TestCase.equalsIgnoreCase("create")) {
	    IndicationType = "CIM_InstCreation";
	} else if (TestCase.equalsIgnoreCase("modify")) {
	    IndicationType = "CIM_InstModification";
	} else if (TestCase.equalsIgnoreCase("delete")) {
	    IndicationType = "CIM_InstDeletion";
	} else if (TestCase.equalsIgnoreCase("read")) {
	    IndicationType = "CIM_InstRead";
	} else if (TestCase.equalsIgnoreCase("method")) {
	    IndicationType = "CIM_InstMethodCall";
	} else {
	    System.out.println("Unknown test case");
	} // if/then/else

	    // Add this client as a listener for an event 
	    cl = new Listener();
	    cc.addCIMListener(cl);

	    // Create a CIM_IndicationFilter instance in the target namespace
	    String filterClassName = "CIM_IndicationFilter";
	    CIMClass filterClass = cc.getClass(new CIMObjectPath(filterClassName), 
		true, true, true, null);
	    CIMInstance filterInstance = filterClass.newInstance();
	    String filterString = "SELECT  * FROM " + IndicationType + " WHERE sourceInstance ISA " + ClassName;
	    filterInstance.setProperty("Query", new CIMValue(filterString));
	    filterInstance.setProperty("QueryLanguage", new CIMValue("WQL"));
	    // NOTE: Since empty CIMObjectPath is passed in, CIMObjectPath to 
	    // filterInstance is returned
	    CIMObjectPath fop = cc.createInstance(new CIMObjectPath(), filterInstance);

	    // Create a CIM_IndicationHandler subclass instance in the target namespace
	    CIMInstance handlerInstance = cc.getIndicationHandler(null);

	    // For some reason, getIndicationHandler does not set these keys for XML
	    if (protocol.equalsIgnoreCase(CIMClient.CIM_XML)) {
		handlerInstance.setProperty("Owner", new CIMValue(user));
		handlerInstance.setProperty("CreationClassName", new CIMValue("CIM_IndicationHandlerCIMXML"));
		handlerInstance.setProperty("SystemName", new CIMValue(host));
	    }

	    // NOTE: Since empty CIMObjectPath is passed in, CIMObjectPath to 
	    // handlerInstance is returned
	    CIMObjectPath hop = cc.createInstance(new CIMObjectPath(), handlerInstance);

	    // Create an instance of association CIM_IndicationSubscription in
	    // target namespace to bind the filter to its handler. Doing so causes
	    // CIMOM to invoke the target provider's activateFilter method; i.e. subscribe
	    String subscriptionClassName = "CIM_IndicationSubscription";
	    CIMClass subscriptionClass = cc.getClass(new CIMObjectPath(subscriptionClassName),
	        true, true, true, null);
	    CIMInstance subscriptionInstance = subscriptionClass.newInstance();
	    // Set association REFs to point to filter and handler instances
	    subscriptionInstance.setProperty("Filter", new CIMValue(fop));
	    subscriptionInstance.setProperty("Handler", new CIMValue(hop));
	    // NOTE: Since empty CIMObjectPath is passed in, CIMObjectPath to 
	    // subscriptionInstance is returned
	    CIMObjectPath sop = cc.createInstance(new CIMObjectPath(), subscriptionInstance);

	    // Wait for indication delivery
	    while (!cl.hasOccurred()) {
		System.out.println("Waiting for event " + IndicationType);
                Thread.sleep(10000);
	    }

	    // Delete the subscription instance in the target namespace to 
	    // unbind the filter from its handler. Doing so causes CIMOM to
	    // invoke the target provider's deActivateFilter method; i.e. unsubscribe
	    sop.setNameSpace("");
	    cc.deleteInstance(sop);

	    // Delete the filter and handler instances
	    hop.setNameSpace("");
	    cc.deleteInstance(hop);
	    fop.setNameSpace("");
	    cc.deleteInstance(fop);

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

} // addCIMListener

class Listener implements CIMListener {
        //
        // "CIMClient.addListener()" takes as an argument
        // a CIMListener object, which, in the code below,
        // is returned by the constructor of the CIMListener
        // interface.  The constructor is implemented inline.
        // In turn, the constructor internally implements the
        // CIMListener interface method "indicationOccurred()":
        // the CIMOM will call this method whenever an indication
        // for the event is ready for delivery.  The method takes
        // as an argument a CIMEvent object, which contains
        // the name of the event's source, ie, the object that
        // generated the event, plus methods for getting source
        // name and event indication.
        //
        boolean deliveredIndication = false;

        public boolean hasOccurred() {
            return deliveredIndication;
        }

        public void indicationOccured(CIMEvent event) {
            CIMInstance indicationInstance = event.getIndication();
            System.out.println("Received indication instance: " + indicationInstance);
            deliveredIndication = true;
        }
} // Listener

