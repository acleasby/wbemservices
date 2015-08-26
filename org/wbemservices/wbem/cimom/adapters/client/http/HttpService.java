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
 *Contributor(s):   WBEM Solutions, Inc.
*/

package org.wbemservices.wbem.cimom.adapters.client.http;

import java.net.InetAddress;
import java.util.Vector;

import javax.wbem.cim.CIMValue;
import javax.wbem.cim.CIMClass;
import javax.wbem.cim.CIMInstance;
import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMObjectPath;
import javax.wbem.cim.UnsignedInt16;
import javax.wbem.cim.UnsignedInt32;
import javax.wbem.client.CIMOMHandle;
import javax.wbem.client.Debug;
import javax.wbem.client.adapter.http.transport.HttpServerEndpoint;

import org.wbemservices.wbem.cimom.CIMOMServer;
import org.wbemservices.wbem.cimom.DeliveryHandler;

import org.wbemservices.wbem.cimom.adapter.client.ClientProtocolAdapterIF;


/** A simple HTTP server, for serving up jar and class files. */
public class HttpService implements ClientProtocolAdapterIF {

    private final static String PORT = "PortNumber";
    private final String XMLHTTPDELIVERYCLASS = 
				"CIM_IndicationHandlerCIMXML";
    private final String XMLHTTLISTENERDESTINATIONCLASS = 
				"CIM_ListenerDestinationCIMXML";
    private final String CIMXMLCOMMUNICATIONMECHANISM =
				"CIM_CIMXMLCommunicationMechanism";
    private final String DEFAULTNS = "/interop";
    private final String SYSCREATIONCLASSNAME = "SystemCreationClassName";
    private final String SYSNAME = "SystemName";
    private final String CREATIONCLASSNAME = "CreationClassName";
    private final String COMMUNICATIONMECHANISM = "CommunicationMechanism";
    private final String NAME = "Name";
    private final String WBEMSERVICES_OBJECTMANAGER = 
				"WBEMServices_ObjectManager";
    private final String PROFILESSUPPORTED = "FunctionalProfilesSupported";
    private final String MULTIPLEOPERATIONSSUPPORTED =
				"MultipleOperationsSupported";
    private final String QUERYLANGUAGESSUPPORTED = "QueryLanguagesSupported";
    private final String AUTHMECHANISMSSUPPORTED = 
				"AuthenticationMechanismsSupported";
    private final String XMLPROTOCOLVERSION = "CIMXMLProtocolVersion";
    private final String VALIDATED = "CIMValidated";
    private final String WQL1 = "WBEMSQL1";
    protected String communicationMechanismName = "cim-xml";
    private String servername;
    private CIMOMServer cimom = null;
    private CIMOMHandle ch = null;
    private int port;
    private HttpServerEndpoint serverEndPoint;
    private CIMObjectPath commMechOp;
    boolean useSSL = false;

    /**
     * Construct a server.  Use the start method to run it.
     */
    public HttpService() throws Exception {
	try {
	    servername = InetAddress.getLocalHost().getHostName();	    
	} catch (Exception ex) {
	    servername = "??";
	}
    }

    public HttpService(CIMOMServer cimomHandle) throws Exception {
	this();
	this.cimom = cimomHandle;
    }

    public void initialize(CIMOMServer cimomHandle, CIMOMHandle ch) {
        this.cimom = cimomHandle;
	this.ch = ch;
	DeliveryHandler.registerIndicationHandler(
	    XMLHTTPDELIVERYCLASS, new HttpIndicationHandler());
	DeliveryHandler.registerIndicationHandler(
	    XMLHTTLISTENERDESTINATIONCLASS, new HttpIndicationHandler());
    }

    public int startService(CIMInstance cpa) {
        Debug.trace1("CIM-XML StartService Called");
	// Create an instance of XML communication mechanism as part of
	// starting up.
	try {
	    CIMObjectPath cimomClassOp =
	    new CIMObjectPath(WBEMSERVICES_OBJECTMANAGER, DEFAULTNS);
	    // Get the instance of CIMOM - the communication mechanism
	    // is week to the system that the CIMOM is running on.
	    CIMInstance cimomInstance =
		(CIMInstance)ch.enumerateInstances(cimomClassOp,
		true, false, true, true, null).nextElement();

	    CIMObjectPath classOp = 
	    new CIMObjectPath(CIMXMLCOMMUNICATIONMECHANISM, DEFAULTNS);
	    CIMClass cc = ch.getClass(classOp, false, true, true, null);
	    CIMInstance ci = cc.newInstance();
	    // populate the properties
	    ci.setProperty(SYSCREATIONCLASSNAME, 
	    cimomInstance.getProperty(SYSCREATIONCLASSNAME).getValue());
	    ci.setProperty(SYSNAME, 
	    cimomInstance.getProperty(SYSNAME).getValue());
	    ci.setProperty(CREATIONCLASSNAME, 
	    new CIMValue(classOp.getObjectName()));
	    ci.setProperty(NAME, new CIMValue("cim-xml"));
	    ci.setProperty(COMMUNICATIONMECHANISM,
	    new CIMValue(new UnsignedInt16(2)));

	    Vector profilesSupported = new Vector(8);
	    profilesSupported.addElement(new UnsignedInt16(2));
	    profilesSupported.addElement(new UnsignedInt16(3));
	    profilesSupported.addElement(new UnsignedInt16(4));
	    profilesSupported.addElement(new UnsignedInt16(5));
	    profilesSupported.addElement(new UnsignedInt16(6));
	    profilesSupported.addElement(new UnsignedInt16(7));
	    profilesSupported.addElement(new UnsignedInt16(8));
	    profilesSupported.addElement(new UnsignedInt16(9));
	    CIMValue profileVal = new CIMValue(profilesSupported);
	    ci.setProperty(PROFILESSUPPORTED, profileVal);
	    ci.setProperty(MULTIPLEOPERATIONSSUPPORTED, CIMValue.TRUE);

	    /* The following is removed until the QueryLanguage is
	       added back to the CIM2.7 MOF. IT was removed until
	       the DMTF get's a query spec in company review
		
	    Vector queryLanguages = new Vector(1);
	    queryLanguages.addElement(WQL1);
	    CIMValue queryLanguagesVal = new CIMValue(queryLanguages);
	    ci.setProperty(QUERYLANGUAGESSUPPORTED, queryLanguagesVal);
	    */
            Debug.trace2("Set AuthMech");

	    Vector authMechs = new Vector(1);
	    authMechs.addElement(new UnsignedInt16(3));
	    CIMValue authMechsVal = new CIMValue(authMechs);
	    ci.setProperty(AUTHMECHANISMSSUPPORTED, authMechsVal);
	    ci.setProperty(XMLPROTOCOLVERSION, 
		new CIMValue(new UnsignedInt16(1)));
	    ci.setProperty(VALIDATED, CIMValue.FALSE);

	    // Create the instance
            Debug.trace2("createInstance called");
	    ch.createInstance(classOp, ci);
            Debug.trace2("After createInstance called");
	    // Store the reference to this instance. We'll delete it when
	    // we stop.
	    commMechOp = ci.getObjectPath();
            Debug.trace2("commMechOp");
	    commMechOp.setNameSpace(classOp.getNameSpace());
            Debug.trace2("commMechOp 2 ");

	} catch (CIMException ce) {
	    // ignore the exception
	    Debug.trace2("Ignore Error: Exception creating CIMXML_CommunicationMechanism",
	    ce);
	}
	try {
	    CIMValue cv = cpa.getProperty(PORT).getValue();
	    port = ((UnsignedInt32)cv.getValue()).intValue();
	} catch (NullPointerException ex) {
	    Debug.trace2("Got exception getting the HTTP port number", ex);
	}
	try {
	    CIMRequestHandler handler =
		new CIMRequestHandler(cimom, servername);
            serverEndPoint = new HttpServerEndpoint(servername, port, useSSL);
	    serverEndPoint.listen(handler);
	} catch (Exception ex) {
	    Debug.trace2("Got exception when starting Http service", ex);
	    return -1;
	}
        return 0;
    }

    public int stopService() {
	try {
	    ch.deleteInstance(commMechOp);
	} catch (CIMException e) {
	    // Just ignore it
	    Debug.trace2("Got exception deleting comm mech instance", e);
	}
	return 0;
    }
}
