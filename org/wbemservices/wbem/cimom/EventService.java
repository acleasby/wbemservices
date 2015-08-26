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

package org.wbemservices.wbem.cimom;

import javax.wbem.cim.CIMInstance;
import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMObjectPath;
import javax.wbem.cim.CIMProperty;
import javax.wbem.cim.CIMClass;
import javax.wbem.cim.CIMValue;
import javax.wbem.cim.CIMNameSpace;
import javax.wbem.cim.CIMDataType;
import javax.wbem.client.CIMEvent;
import javax.wbem.client.ProviderCIMOMHandle;
import javax.wbem.client.CIMSecurityException;
import javax.wbem.client.CIMProviderException;
import javax.wbem.client.Debug;
import javax.wbem.provider.CIMInstanceProvider;
import javax.wbem.provider.Authorizable;
import javax.wbem.provider.CIMProvider;
import javax.wbem.query.SelectList;
import javax.wbem.query.AttributeExp;

import java.net.InetAddress;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Properties;
import java.util.StringTokenizer;

//XXX: This is a BUG. 
//     The Event service should not have any knowledge
//	of the persistent store, only the CIMOM.
import org.wbemservices.wbem.repository.PSRlogImpl;

/*
 * Class which deals with event service functionality. It handles creation
 * of subscriptions, event delivery, etc. It keeps track of what subscriptions
 * are active and to which classes they apply. The CIM object manager creates
 * a single instance of this class to deal with all the evernt services. It
 * serves as the single point of contact between filter activations and
 * event delivery.
 */
// We probably need an interface for this later
public class EventService implements 
InternalProviderAdapter.InternalServiceProvider {

    // We should use the interface here when it is defined
    PSRlogImpl ps = null;
    CIMOMUtils cu = null;
    CIMOMImpl cimom = null;
    private IndicationPoller indicationPoller = null;
    javax.wbem.client.ProviderCIMOMHandle pch = null;
    // XXX factory
    //ProviderChecker provCheck = null;
    // Map of all active filters
    Map filterActivations = new TreeMap();
    // internal provider classes for event handling. This probably belongs
    // in a property file. especially the solaris provider.
    // 10 minutes ping time
    private static int pingTime = 600000;
    static int pollInterval = 3000;
    final static String FILTERDELIVERYASSOC = "cim_indicationsubscription";
    //    final static String DELIVERYCLASS = "cim_indicationhandler";
    final static String DELIVERYCLASS = "cim_listenerdestination";
    public final static String RMIDELIVERYCLASS = "solaris_rmidelivery";
    public final static String JRMIDELIVERYCLASS = "solaris_javaxrmidelivery";
    final static String CIMFILTERCLASS = "cim_indicationfilter";
    final static String FILTERPROPERTY = "filter";
    final static String DELIVERYPROPERTY = "handler";
    public final static String HANDLERNAMEPROP = "name";
    final static String FILTERNAMEPROP = "name";
    final static String CCNPROP = "creationclassname";
    final static String QUERYPROP = "query";
    final static String QUERYLANGPROP = "querylanguage";
    final static String TARGETNSPROP = "sourceNameSpace";
    final static String SYSTEMCCNPROP = "systemcreationclassname";
    final static String SYSTEMNAMEPROP = "systemname";
    final static String SUBSCRIPTIONALIASPROP = "subscriptionalias";
    final static String CSNAME = "solaris_computersystem";

    public static EventService eventService = null;

    CIMInstanceProvider cim_filterdelivery = new CIM_FilterDelivery();
    CIMProvider cim_filter = new CIM_Filter();
    CIMInstanceProvider solaris_rmidelivery = new Solaris_RMIDelivery();
    String hostName;
    TaskManager tm = null;
    TaskManager.Task task;

    // This interface must be made public. It will enable third parties to
    // plug in their own handlers.
    public interface IndicationHandler {
	public void deliverEvent(CIMEvent e, CIMInstance handlerInstance) 
	throws CIMException;
	void ping(CIMInstance handlerInstance) throws CIMException;
	CommonServerSecurityContext 
	getSecurityContext(CIMInstance handlerInstance);
    }

    // This method removes deliveries which cause errors. We should
    // remove all these deliveries so that their corresponding filters
    // stop being evaluated. Otherwise we'll consume resources needlessly.
    private void removeDelivery(CIMObjectPath delivery, 
	CIMObjectPath assocOp) {
	Vector tv = null;
	try {
	    LogFile.add(LogFile.WARNING, "REMOVING_HANDLER", 
		    delivery.toString());
	} catch (CIMException e) {
	    // Ignore
	}
	try {
	    tv = ps.referenceNames(assocOp, delivery, "");
	} catch (CIMException e) {
	    Debug.trace2("Remove delivery failed", e);
	    // Shouldnt happen ordinarily - usually happens if multiple 
	    // threads are trying to remove the same delivery. 
	    // ignore and return;
	    return;
	}

	// Clean up all this delivery's associations to the filters
	Enumeration enum = tv.elements();
	while (enum.hasMoreElements()) {
	    CIMObjectPath assoc = (CIMObjectPath)enum.nextElement();
	    // The delete instance should take care of deactivating the
	    // filters
	    try {
		pch.deleteInstance(assoc);
	    } catch (CIMException e) {
                Debug.trace2("Remove delivery associator deletion failed", e);
		// Shouldnt happen, ignore
	    }
	}

	// Clean up this delivery
	try {
	    pch.deleteInstance(delivery);
	} catch (CIMException e) {
            Debug.trace2("Remove delivery delivery instance deletion failed", e);
	    // Shouldnt happen, ignore
	}
    }

    private class HandlerChecker extends Thread {
	private void loop() {
	    // For now we will use the active filter list. In the future
	    // we could have an active handler list too.
	    List activeFilters = new ArrayList();
	    // Copy the contents to a list and then do the ping. We dont
	    // want to keep filterActivations locked up. Could have used
	    // clone, but I didnt want to use up the extra space for a
	    // new map.
	    synchronized(filterActivations) {
		Set s = filterActivations.entrySet(); 
		Iterator i = s.iterator();
		while (i.hasNext()) {
		    Map.Entry e = (Map.Entry)i.next();
		    activeFilters.add(e.getValue());
		}
	    }
	    Iterator i = activeFilters.iterator();
	    Map handlerMap = new TreeMap();
	    CIMObjectPath assocOp = new CIMObjectPath(FILTERDELIVERYASSOC);
	    while (i.hasNext()) {
		FilterActivation fa = (FilterActivation)i.next();
		CIMObjectPath filterOp = fa.getFilterOp();
		assocOp.setNameSpace(filterOp.getNameSpace());
		// Find the associated handlers
		Vector tv = null;
		try {
		    tv = ps.referenceNames(assocOp, filterOp, "");
		} catch (CIMException e) {
		    // Ignore for now

                    Debug.trace2("Ignoring CIMException in HandlerChecker.loop", e);
		    continue;
		}

		if (tv.size() != 0) {
		    Enumeration deliveryEnum = tv.elements();
		    while (deliveryEnum.hasMoreElements()) {
			CIMObjectPath filterDeliveryOp = 
			(CIMObjectPath) deliveryEnum.nextElement();
			CIMObjectPath deliveryOp = null;
			Enumeration keyEnum = 
			filterDeliveryOp.getKeys().elements();
			while (keyEnum.hasMoreElements()) {
			    CIMProperty cp = (CIMProperty)keyEnum.nextElement();
			    if (cp.getName().
			    equalsIgnoreCase(DELIVERYPROPERTY)) {
				// found the delivery object path
				try {
				    deliveryOp = (CIMObjectPath)
				    cp.getValue().getValue();
				} catch (NullPointerException e) {
				    // Ignore
                                    Debug.trace2("Ignoring NullPointerException in HandlerChecker.loop", e);
				}
			    }
			}

			String key = deliveryOp.toString();
			if (handlerMap.get(key) == null) {
			    // Havent ping'd this delivery yet
			    handlerMap.put(key, "");
			    CIMInstance ci = null;
			    try {
				ci = ps.getInstance(deliveryOp);
			    } catch (CIMException e) {
				// Ignore
                                Debug.trace2("Ignoring CIMException in HandlerChecker.loop", e);
				continue;
			    }
			    IndicationHandler ih =
				DeliveryHandler.getIndicationHandler(
				ci.getClassName());
			    try {
				ih.ping(ci);
			    } catch (CIMException e) {
				// Ping failed, remove this handler
				synchronized (cim_filterdelivery) {
				    removeDelivery(deliveryOp, assocOp);
				}
			    }
			}
		    }
		}
	    }
	}
	public void run() {
	    while (true) {
		try {
		Thread.sleep(pingTime);
		} catch (InterruptedException e) {
		// ignore
		}
		loop();
	    }
	}
    }

    /*
     * Takes care of associating a filter with a delivery object. If a
     * filter is associated with at least one delivery object, processing
     * for the filter must commence. Similarly, when the last association
     * is removed, processing must stop
     */
    private class CIM_FilterDelivery implements CIMInstanceProvider, 
    Authorizable {

	public CIM_FilterDelivery() {
	}

	public CIMObjectPath[] enumerateInstanceNames(CIMObjectPath op, 
						      CIMClass cc)
	throws CIMException {
	    Vector v = ps.enumerateInstances(op, false);
	    CIMObjectPath[] copArray = new CIMObjectPath[v.size()];
	    v.copyInto(copArray);
	    return copArray;
	}

	public CIMInstance[] enumerateInstances(CIMObjectPath op, 
						boolean localOnly,
						boolean includeQualifiers,
						boolean includeClassOrigin,
						String[] propList,
                				CIMClass cc)
	throws CIMException {
	    Vector v = ps.enumerateInstances(op, false, localOnly);
	    CIMInstance[] ciArray = new CIMInstance[v.size()];
	    v.copyInto(ciArray);
	    return ciArray;
	}

	public CIMInstance getInstance(CIMObjectPath op, 
				       boolean localOnly, 
				       boolean includeQualifiers, 
				       boolean includeClassOrigin, 
			    	       String[] propList,	
				       CIMClass cc)
	throws CIMException {
	    CIMInstance ci = ps.getInstance(op);
	    if (localOnly) {
		//XXX: Why not a ci = ci.localElements();
		ci = cu.getLocal(ci);
	    }
	    return ci.filterProperties(propList, includeQualifiers,
				       includeClassOrigin);
	}

	// Returns true if there was a change
	private boolean fillNameSpace(CIMObjectPath currentns, 
	CIMObjectPath op) {
	    if (op.getNameSpace() == null || 
	    op.getNameSpace().length() == 0) {
		op.setNameSpace(currentns.getNameSpace());
		return true;
	    }
	    // Check for relative path
	    if (!op.getNameSpace().startsWith("/")) {
		op.setNameSpace(currentns.getNameSpace()+'/'+
		op.getNameSpace());
		return true;
	    }
	    return false;
	}

	// This method creates the instance of the association. If this is
	// the first association from the filter to any delivery object,
	// we have to activate the filter.
	public synchronized CIMObjectPath createInstance(CIMObjectPath op, 
	CIMInstance ci) throws CIMException {
	    CIMObjectPath filterOp = null;
	    CIMObjectPath deliveryOp = null;
	    try {
		filterOp = (CIMObjectPath)ci.getProperty(FILTERPROPERTY).
		getValue().getValue();
		deliveryOp = (CIMObjectPath)ci.getProperty(DELIVERYPROPERTY).
		getValue().getValue();
	    } catch (Exception e) {
		// something wrong with the params
                Debug.trace2("Caught Exception in CIM_FilterDelivery.createInstance", e);
		throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER);
	    }
	    // Bug 4389046: The repository does not allow null association props
	    // Will remove this once the bug is fixed.
	    try {
		ci.setProperty(SUBSCRIPTIONALIASPROP, new CIMValue(""));
	    } catch (Exception tex) {
		// ignore if the property is absent
                Debug.trace2("Ignoring Exception in CIMFilterDelivery.createInstance", tex);
	    }

	    boolean keychanged = false;
	    // Make sure that the proper namespace value is set
	    boolean b1 = fillNameSpace(op, filterOp);
	    boolean b2 = fillNameSpace(op, deliveryOp);
	    keychanged = b1 | b2;
	    CIMInstance filterInstance = ps.getInstance(filterOp);
	    CIMInstance deliveryInstance = ps.getInstance(deliveryOp);

	    if (filterInstance == null) {
		throw new CIMException(CIMException.CIM_ERR_NOT_FOUND, 
		filterOp.toString());
	    }
	    if (deliveryInstance == null) {
		throw new CIMException(CIMException.CIM_ERR_NOT_FOUND, 
		deliveryOp.toString());
	    }
	    CIMObjectPath returnOp = new CIMObjectPath();
	    returnOp.setObjectName(ci.getClassName());
	    returnOp.setNameSpace(op.getNameSpace());
	    // Look for delivery instances associated to the filter
	    Vector tv = ps.associatorNames(returnOp, filterOp,
		    DELIVERYCLASS, "", "");

	    FilterActivation fa = null;
	    if (tv.size() == 0) {
		// This means that we have the first association to
		// a delivery object. We need to activate the filter.
		// handle the filterInstance. 
		fa = new FilterActivation(filterOp, filterInstance, 
		EventService.this);
		fa.run();
		fa.getPollInfo(deliveryInstance, ci);
		fa.activate(deliveryInstance, ci);
	    } else {
		// Must be already activated
	    }

	    // If the activation succeeded, add the filter to the currently
	    // active list
	    CIMObjectPath tempOp = 
	    new CIMObjectPath(filterOp.getObjectName().toLowerCase());
	    tempOp.setNameSpace(filterOp.getNameSpace().toLowerCase());
	    tempOp.setKeys(filterInstance.getKeys());
	    if (fa != null) {
		synchronized(filterActivations) {
		    filterActivations.put(tempOp.toString(), fa);
		}
	    } else {
		// authorize this guy
		synchronized(filterActivations) {
		    fa = 
		    (FilterActivation)filterActivations.get(tempOp.toString());
		}
		fa.activateSubscription(deliveryInstance, ci);
	    }
	    ps.addCIMElement(op.getNameSpace(), ci);
	    if (keychanged) {
		returnOp.setKeys(ci.getKeys());
		return returnOp;
	    } else {
		return null;
	    }
	}

	// We will not support updating the associations for now.
	public void setInstance(CIMObjectPath op, CIMInstance ci,
	boolean includeQualifier, String[] propertyList) 
	throws CIMException {
	    throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
	}

	// If this is the last association to the filter, then we must
	// deactivate it.
	public synchronized void deleteInstance(CIMObjectPath op) throws 
	CIMException {

	    Enumeration e = op.getKeys().elements();
	    CIMObjectPath filterOp = null;
	    CIMObjectPath handlerOp = null;
	    while (e.hasMoreElements()) {
		CIMProperty cp = (CIMProperty)e.nextElement();

		if (cp.getName().equalsIgnoreCase(FILTERPROPERTY)) {
		    try {
			filterOp = (CIMObjectPath)cp.getValue().getValue();
		    } catch (NullPointerException ex) {
			// No value set
                        Debug.trace2("Caught NullPointerException in CIMFilterDelivery.setInstance", ex);

			throw new CIMException(
			CIMException.CIM_ERR_INVALID_PARAMETER);
		    }
		}

		if (cp.getName().equalsIgnoreCase(DELIVERYPROPERTY)) {
		    try {
			handlerOp = (CIMObjectPath)cp.getValue().getValue();
		    } catch (NullPointerException ex) {
			// No value set
                        Debug.trace2("Caught NullPointerException in CIMFilterDelivery.setInstance", ex);
			throw new CIMException(
			CIMException.CIM_ERR_INVALID_PARAMETER);
		    }
		}
	    }


	    Vector tv = ps.associatorNames(op, filterOp,
		    DELIVERYCLASS, "", "");
	    CIMInstance filterInstance = ps.getInstance(filterOp);

	    CIMInstance handlerInstance = ps.getInstance(handlerOp);
	    CIMInstance subscriptionInstance = ps.getInstance(op);

	    ps.deleteInstance(op);

	    CIMObjectPath tempOp = 
	    new CIMObjectPath(filterOp.getObjectName().toLowerCase());
	    tempOp.setNameSpace(filterOp.getNameSpace().toLowerCase());
	    tempOp.setKeys(filterInstance.getKeys());
	    FilterActivation fa = null;

	    if (tv.size() != 1) {
		// This means that we there are more deliveries associated
		// with the filter. No need to completely deactivate.
		synchronized(filterActivations) {
		    fa = (FilterActivation)
		    filterActivations.get(tempOp.toString());
		}
		fa.deactivateSubscription(handlerInstance, 
		subscriptionInstance);
		return;
	    }

	    // Ok - bid farewell to the activation
	    synchronized(filterActivations) {
		fa = (FilterActivation)
		filterActivations.remove(tempOp.toString());
	    }

	    // deactivate the filter
	    fa.deactivate(handlerInstance, subscriptionInstance);
	    Debug.trace3("Deactivation complete fasize "+
	    filterActivations.size()+" cfmap size "+classFilterMap.size());
	}

	public void initialize(javax.wbem.client.CIMOMHandle ch) {
	    // nothing to do yet, we may set up deliveries on startup here.
	}

	public void cleanup() {
	    // nothing to do yet.
	}

	public CIMInstance[] execQuery(CIMObjectPath op, String query, 
				       String ql, CIMClass cc) 
	throws CIMException {
	    Vector v =  ps.execQuery(op, query, ql, cc);
	    CIMInstance[] ciArray = new CIMInstance[v.size()];
	    v.copyInto(ciArray);
	    return ciArray;
	}

    }

    private class CIM_Filter implements CIMInstanceProvider, Authorizable {
	public CIM_Filter() {
	}

	public CIMObjectPath[] enumerateInstanceNames(CIMObjectPath op, 
						      CIMClass cc)
	throws CIMException {
	    Vector v = ps.enumerateInstances(op, false);
	    CIMObjectPath[] copArray = new CIMObjectPath[v.size()];
	    v.copyInto(copArray);
	    return copArray;
	}

	public CIMInstance[] enumerateInstances(CIMObjectPath op, 
						boolean localOnly,
						boolean includeQualifiers,
						boolean includeClassOrigin,
						String[] propList,
                				CIMClass cc)
	throws CIMException {
	    Vector v = ps.enumerateInstances(op, false, localOnly);
	    CIMInstance[] ciArray = new CIMInstance[v.size()];
	    v.copyInto(ciArray);
	    return ciArray;
	}

	public CIMInstance getInstance(CIMObjectPath op, 
				       boolean localOnly, 
				       boolean includeQualifiers, 
				       boolean includeClassOrigin, 
			    	       String[] propList,	
				       CIMClass cc)
	throws CIMException {
	    CIMInstance ci = ps.getInstance(op);
	    if (localOnly) {
		//XXX: Why not a ci = ci.localElements();
		ci = cu.getLocal(ci);
	    }
	    return ci.filterProperties(propList, includeQualifiers,
				       includeClassOrigin);
	}

	// This method assigns a unique id to the filter if required
	public CIMObjectPath createInstance(CIMObjectPath op, CIMInstance ci) 
	throws CIMException {

	    String value = null;
	    String targetNS = null;
	    try {
		value = (String)ci.getProperty(FILTERNAMEPROP).
		getValue().getValue();
	    } catch (NullPointerException e) {
		// ignore
                Debug.trace2("Ignoring NullPointerException in CIM_Filter.createInstance", e);
	    }

	    try {
		targetNS = (String)ci.getProperty(TARGETNSPROP).
		getValue().getValue();
	    } catch (NullPointerException e) {
		// ignore
                Debug.trace2("Ignoring NullPointerException in CIM_Filter.createInstance", e);
	    }

	    CIMObjectPath rcop = null;
	    if (value == null) {
		ci.setProperty(FILTERNAMEPROP, 
		new CIMValue(CIMOMUtils.getUniqueString()));
	    }
	    ci.setProperty(CCNPROP, new CIMValue(CIMFILTERCLASS));
	    ci.setProperty(SYSTEMCCNPROP, new CIMValue(CSNAME));
	    ci.setProperty(SYSTEMNAMEPROP, new CIMValue(hostName));
	    rcop = new CIMObjectPath(ci.getClassName(), 
	    ci.getKeys());
	    rcop.setNameSpace(op.getNameSpace());

	    if (targetNS != null) {
		// Get it into our standard form
		targetNS = (new CIMNameSpace(".", "/"+targetNS)).getNameSpace();
		ci.setProperty(TARGETNSPROP, 
		new CIMValue(targetNS.toLowerCase()));
	    }
	
	    ps.addCIMElement(op.getNameSpace(), ci);
	    return rcop;
	}

	// We will not support updating the filter for now.
	public void setInstance(CIMObjectPath op, CIMInstance ci, 
	boolean includeQualifier, String[] propertyList) 
	throws CIMException {
	    // Do nothing
	    throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
	}

	public void deleteInstance(CIMObjectPath op) throws CIMException {
	    ps.deleteInstance(op);
	}

	public void initialize(javax.wbem.client.CIMOMHandle ch) {
	    // nothing to do yet, we may set up deliveries on startup here.
	}

	public void cleanup() {
	    // nothing to do yet.
	}

        public CIMInstance[] execQuery(CIMObjectPath op, String query, 
				       String ql, CIMClass cc) 
	throws CIMException {
	    Vector v = ps.execQuery(op, query, ql, cc);
	    CIMInstance[] ciArray = new CIMInstance[v.size()];
	    v.copyInto(ciArray);
	    return ciArray;
	}
    }

    private class Solaris_RMIDelivery implements 
    CIMInstanceProvider, Authorizable {
	public Solaris_RMIDelivery() {
	}

	public CIMObjectPath[] enumerateInstanceNames(CIMObjectPath op, 
						      CIMClass cc)
	throws CIMException {
	    Vector v = ps.enumerateInstances(op, false);
	    CIMObjectPath[] copArray = new CIMObjectPath[v.size()];
	    v.copyInto(copArray);
	    return copArray;
	}

	public CIMInstance[] enumerateInstances(CIMObjectPath op, 
						boolean localOnly,
						boolean includeQualifiers,
						boolean includeClassOrigin,
						String[] propList,
                				CIMClass cc)
	throws CIMException {
	    Vector v = ps.enumerateInstances(op, false, localOnly);
	    CIMInstance[] ciArray = new CIMInstance[v.size()];
	    v.copyInto(ciArray);
	    return ciArray;
	}

	public CIMInstance getInstance(CIMObjectPath op, 
				       boolean localOnly, 
				       boolean includeQualifiers, 
				       boolean includeClassOrigin, 
			    	       String[] propList,	
				       CIMClass cc)
	throws CIMException {
	    CIMInstance ci = ps.getInstance(op);
	    if (localOnly) {
		//XXX: Why not a ci = ci.localElements();
		ci = cu.getLocal(ci);
	    }
	    return ci.filterProperties(propList, includeQualifiers,
				       includeClassOrigin);
	}

	// This method assigns a unique id and associates the delivery
	// instance with the sessionid. In this way the CIMOM can get
	// the remotehandle when it wishes to deliver the event. 
	// The RMI delivery is keyed off the sessionID, hence only one
	// rmi instance per session will be allowed.
	public CIMObjectPath createInstance(CIMObjectPath op, CIMInstance ci) 
	throws CIMException {

	    String sessionID=null;
	    try {
		sessionID = 
		(String)ci.getProperty(HANDLERNAMEPROP).getValue().getValue();
	    } catch (NullPointerException e) {
		// sessionID is null;
                Debug.trace2("Ignoring NullPointerException in SOLARIS_RMIDelivery.createInstance", e);
	    }

	    ServerSecurity ss = null;
	    try {
		ss = (ServerSecurity)
		(ServerSecurity.getRequestSession());
	    } catch (ClassCastException e) {
		// This is most likely if Viper tries this - we dont handle
		// this case. Only wbem clients can receive RMI event
		// callback
	    }

	    if (sessionID != null) {
		ss = (ServerSecurity)
		    DeliveryHandler.getIndicationHandler(ci.getClassName()).
		    getSecurityContext(ci);
	    }

	    if (ss == null) {
		// Trying to set a delivery for a session that does not
		// exist
		throw new CIMSecurityException(
		CIMSecurityException.NO_SUCH_SESSION, sessionID);
	    }

	    if (ss.getListener() == null) {
		// This client does not have a listener set up. So it should
		// not be able to create an RMI event listener.
		throw new CIMException(CIMException.CIM_ERR_FAILED);
	    }

	    ci.setProperty(HANDLERNAMEPROP, 
	    new CIMValue(new String(ss.getSessionId()), 
			CIMDataType.getPredefinedType(CIMDataType.STRING)));
	    ci.setProperty(CCNPROP, 
	    new CIMValue(ci.getClassName().toLowerCase()));
	    ci.setProperty(SYSTEMCCNPROP, new CIMValue(CSNAME));
	    ci.setProperty(SYSTEMNAMEPROP, new CIMValue(hostName));
	
	    ps.addCIMElement(op.getNameSpace(), ci);
	    CIMObjectPath rcop = new CIMObjectPath(ci.getClassName(), 
	    ci.getKeys());
	    rcop.setNameSpace(op.getNameSpace());
	    return rcop;
	}

	// We will not support updating the associations for now.
	public void setInstance(CIMObjectPath op, CIMInstance ci,
	boolean includeQualifier, String[] propertyList) 
	throws CIMException {
	    throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
	}

	public void deleteInstance(CIMObjectPath op) throws CIMException {
	    ps.deleteInstance(op);
	}

	public void initialize(javax.wbem.client.CIMOMHandle ch) {
	    // nothing to do yet, we may set up deliveries on startup here.
	}

	public void cleanup() {
	    // nothing to do yet.
	}

	public CIMInstance[] execQuery(CIMObjectPath op, String query, 
					String ql, CIMClass cc)
	throws CIMException {
	    Vector v = ps.execQuery(op, query, ql, cc);
	    CIMInstance[] ciArray = new CIMInstance[v.size()];
	    v.copyInto(ciArray);
	    return ciArray;
	}
    }

    // InternalServiceProvider interface method. See InternalProviderAdapter.
    public String[] getProviderNames() {
	return new String[] { FILTERDELIVERYASSOC, 
	RMIDELIVERYCLASS, JRMIDELIVERYCLASS, CIMFILTERCLASS };
    }

    // This method will be called by the CIMOM when it is first initializing
    // its services. It uses this method to get the appropriate providers
    // for filter, filterdelivery, rmidelivery etc.
    // InternalServiceProvider interface method. See InternalProviderAdapter.
    public CIMProvider getProvider(String className) throws CIMException {
	if (className.equals(FILTERDELIVERYASSOC)) {
	    return cim_filterdelivery;
	}

	if (className.equals(RMIDELIVERYCLASS)) {
	    return solaris_rmidelivery;
	}

	if (className.equals(JRMIDELIVERYCLASS)) {
	    // For now we use solaris_rmidelivery for this also, since there
	    // are no new properties to handle.
	    return solaris_rmidelivery;
	}

	if (className.equals(CIMFILTERCLASS)) {
	    return cim_filter;
	}

	throw new CIMProviderException(
	CIMProviderException.NO_INSTANCE_PROVIDER, className);
    }

    IndicationPoller getIndicationPoller() {
	return indicationPoller;
    }

    // We maintain a mapping between a class and the filters active on that 
    // class. This is populated by the individual SubActivation. The event 
    // delivery can find out all active filters for a given class and check if 
    // any event on that class matches the available filters.

    // This lock maintainces a single writer multiple writer lock on the
    // class filters. The event delivery threads could be readers while a 
    // subactivation tries to update.
    private ReadersWriter cfl = new ReadersWriter();
    private Map classFilterMap = new HashMap();
    private final static int LCLISTSIZE = 8;

    private int getOffset(int eventType, boolean polled) {
	if (eventType < 0) {
	    return 0;
	} else {
	    if (eventType < FilterActivation.INSTANCEREADTYPE) {
		if (polled) {
		    return eventType*2;
		} else {
		    return eventType*2+1;
		}
	    } else {
		return eventType+3;
	    }
	}
    }

    // Performance issue:
    // Currently the classFilterMap is being locked wholesale. We could employ
    // finer grained locking, say at a class entry level to if performance
    // turns out to be an issue. My reckoning is that the write operations,
    // i.e. new subscriptions or subscription removal will not be a frequent
    // operation, and only readers will be present.

    // Invoked by FilterActivation.SubActivation.
    // For each class the filterList is a List of lists. Each sublist
    // has filters pertaining to instance creation, deletion and modification.
    // Returns true if the class is just being activated for the given
    // event type.

    // Note: We could make the sublist a set if speed is an issue in the
    // future
    // Performance issue: We do not need to create a separate SubActivation
    // per class. We should have a class map which has associated to it
    // its activations, its providers, etc.

    boolean addClassFilter(String className, FilterActivation.SubActivation sa, 
				boolean polled) {
	boolean returnFlag = false;
	cfl.writeLock();
	try {
	    List filterList = (List)classFilterMap.get(className);
	    List filterSubList;
	    int offset = getOffset(sa.getEventType(), polled);
	    if (filterList == null) {
		filterList = new ArrayList();
		// Totally 8 lists, polled/unpolled list for instance addition
		// modification, deletion and one each for read and method
		// invocation
		for (int i = 0; i < LCLISTSIZE; i++) {
		    filterList.add(new ArrayList());
		}
		filterSubList =
		(List)filterList.get(offset);
		filterSubList.add(sa);
		classFilterMap.put(className, filterList);
		returnFlag = true;
	    } else {
		filterSubList = 
		(List)filterList.get(offset);
		if (filterSubList.size() == 0) {
		    // This eventType is inactive for the class
		    returnFlag = true;
		}
		filterSubList.add(sa);
	    }
	} finally {
	    cfl.writeUnlock();
	}
	return returnFlag;
    }

    // Invoked by FilterActivation.SubActivation.
    // Remove the filter from the given class. Returns true if the class
    // has no more activations for a given event type.
    boolean removeClassFilter(String className, 
    FilterActivation.SubActivation sa, boolean polled) {
	boolean returnFlag = false;
	cfl.writeLock();
	try {
	    int offset = getOffset(sa.getEventType(), polled);
	    List filterList = (List)classFilterMap.get(className);
	    List filterSubList;
	    if (filterList == null) {
		// This really shouldnt happen. We'll ignore it for now.
		return false;
	    } else {
		filterSubList =
		(List)filterList.get(offset);
		filterSubList.remove(sa);
		if (filterSubList.size() == 0) {
		    // This means that the given class has no more
		    // activations for the given event type.
		    returnFlag = true;
		}

		// find out if all the sublists are empty
		int i;
		for (i = 0; i < LCLISTSIZE; i++) {
		    if (((List)filterList.get(i)).size() != 0) {
			break;
                    }
		}

		if (i == LCLISTSIZE) {
		    // All the subfilters are empty, dont need
		    // the class anymore
		    classFilterMap.remove(className);
		}
	    }
	} finally {
	    cfl.writeUnlock();
	}
	return returnFlag;
    }

    // this is invoked by the event dispatcher to determine which filters
    // must be processed for the given class.
    List getClassFilters(String className, String eventTypeString,
    boolean polled) {
	cfl.readLock();
	className = className.toLowerCase();
	int eventType = FilterActivation.determineEventType(eventTypeString);
	// event type is -1 for process indications, so only one list is
	// valid
	try {
	    List filterList = (List)classFilterMap.get(className);
	    if (filterList == null) {
		return null;
	    } else {
		int offset = getOffset(eventType, polled);
		return (List)filterList.get(offset);
	    }
	} finally {
	    cfl.readUnlock();
	}
    }
    
    private void activateFilters() throws CIMException {

	// Get all the namespaces
	Enumeration enum = ps.enumerateNameSpace("", true).elements();
	// for each namespace, find the instances of filter
	CIMObjectPath assocOp = new CIMObjectPath(FILTERDELIVERYASSOC);
	while (enum.hasMoreElements()) {
	    CIMObjectPath op = new CIMObjectPath("", 
	    (String)enum.nextElement());
	    op.setObjectName(CIMFILTERCLASS);
	    assocOp.setNameSpace(op.getNameSpace());

	    // Handle each filter instance
	    Enumeration instenum = null;
	    try {
		instenum = ps.enumerateInstances(op, false, false).elements();
	    } catch (CIMException e) {
		// Ignore this, most likely no filter classes have been defined
		// go to the next namespace
                Debug.trace2("Ignoring CIMException in EventService.activateFilters", e);

		continue;
	    }
	    while (instenum.hasMoreElements()) {
		CIMInstance filterInstance = 
		(CIMInstance)instenum.nextElement();

		CIMObjectPath filterOp = 
		new CIMObjectPath(filterInstance.getClassName().toLowerCase());
		filterOp.setKeys(filterInstance.getKeys());
		filterOp.setNameSpace(op.getNameSpace().toLowerCase());

		// Find out all associated deliveries
		Vector tv = null;
		try {
		    tv = ps.referenceNames(assocOp, filterOp, "");
		} catch (CIMException e) {
		    // Ignore for now
		    // continue or break and go to the next namespace?
                    Debug.trace2("Ignoring CIMException in EventService.activateFilters", e);

		    continue;
		}
		List handlerSubscriptionList = new ArrayList();
		if (tv.size() != 0) {
		    boolean mustActivate = false;
		    Enumeration deliveryEnum = tv.elements();
		    while (deliveryEnum.hasMoreElements()) {
			CIMObjectPath filterDeliveryOp = 
			(CIMObjectPath) deliveryEnum.nextElement();
			CIMObjectPath deliveryOp = null;
			Enumeration keyEnum = 
			filterDeliveryOp.getKeys().elements();
			while (keyEnum.hasMoreElements()) {
			    CIMProperty cp = (CIMProperty)keyEnum.nextElement();
			    if (cp.getName().
			    equalsIgnoreCase(DELIVERYPROPERTY)) {
				// found the delivery object path
				try {
				    deliveryOp = (CIMObjectPath)
				    cp.getValue().getValue();
				} catch (NullPointerException e) {
				    // Ignore
                                    Debug.trace2("Ignoring NullPointerException in EventService.activateFilters", e);
				}
			    }
			}

			if (DeliveryHandler.isTransient(deliveryOp)) {
			    // Ok this delivery is a transient delivery like
			    // RMI. It was set up in the previous run of 
			    // the CIMOM and should be cleaned up.

			    // Remove the delivery and its association to
			    // the filter.
			    ps.deleteInstance(filterDeliveryOp);
			    try {
				ps.deleteInstance(deliveryOp);
			    } catch (CIMException oe) {
				// this means that this delivery still has
				// other filters to it, so we leave it.
                                Debug.trace2("Ignoring CIMException in EventService.activateFilters", oe);
			    }
			} else {
			    handlerSubscriptionList.add(
			    ps.getInstance(deliveryOp));
			    handlerSubscriptionList.add(
			    ps.getInstance(filterDeliveryOp));
			    mustActivate = true;
			}
		    }

		    if (!mustActivate) {
			// All the deliveries for the filter were transient
			// Lets go to the next one.
			continue;
		    }

		    Iterator i = handlerSubscriptionList.iterator();
		    CIMInstance handlerInstance = (CIMInstance)i.next();
		    CIMInstance subscriptionInstance = (CIMInstance)i.next();
		    // ok there is an associated delivery, we must handle it.
		    FilterActivation fa =
			new FilterActivation(filterOp, filterInstance, this);
		    fa.run();
		    // No need to authorize, since all deliveries would
		    // have been authorized when they were created.
		    fa.getPollInfo(handlerInstance, subscriptionInstance);
		    fa.activate(handlerInstance, subscriptionInstance);
		    // No need to synchronize here, since this is the only
		    // thread running - during startup.
                    filterActivations.put(filterOp.toString(), fa);
		    // Now activate the rest of the subscriptions
		    while (i.hasNext()) {
			handlerInstance = (CIMInstance)i.next();
			subscriptionInstance = (CIMInstance)i.next();
			fa.activateSubscription(handlerInstance,
			subscriptionInstance);
		    }
		}
	    }
	}
    }

    // cimom is passed in for FilterActivations to do delayedCapability
    // checks. We should probably encapsulate this check into a utility
    // method.
    public EventService(PSRlogImpl ps, CIMOMUtils cu, CIMOMImpl cimom,
    			ProviderCIMOMHandle pch,
			// XXX factory
			//ProviderChecker provCheck,
    			Properties props) throws Exception {
	this.ps = ps;
	this.cu = cu;
	this.cimom = cimom;
	this.pch = pch;
	// XXX factory
	//this.provCheck = provCheck;
	this.indicationPoller = new IndicationPoller(pch);

	String pollIntervalString = 
	props.getProperty("org.wbemservices.wbem.cimom.pollInterval");
	if (pollIntervalString != null) {
	    pollInterval = Integer.parseInt(pollIntervalString)*1000;
	}

	// this method activates all the available filters
	try {
	    activateFilters();
	} catch (Exception e) {
	    // For now we will not fail fatally if we cannot activate filters
	    // Log and continue.
            Debug.trace2("Ignoring Exception in EventService constructor", e);

	    try {
		LogFile.add(LogFile.CRITICAL, "INDICATION_HANDLER_ERROR", 
		    e.toString());
	    } catch (Exception e2) {
		// What the @*$#?
		e2.printStackTrace();
	    }
	}

	try {
	    hostName = InetAddress.getLocalHost().getHostName();
	} catch (Exception e) {
            Debug.trace2("Caught Exception in EventService constructor", e);
	    hostName = "";
	}

	// Allocate TaskManager object to handle thread pool used for
	// event generation.
	try {
	    tm = new TaskManager();
	} catch (Exception e) {
	    throw new CIMException(CIMException.CIM_ERR_FAILED, e.toString());
	}

	// This enables any CIMOM code to access the event service.
	eventService = this;
	// Start thread which checks if remote listeners are alive. If they
	// are not, then remove their handlers and subscriptions
	(new HandlerChecker()).start();
    }

    private class IndicationDeliverer implements TaskManager.Task {
	CIMInstance ci;
	String namespace;
	boolean polled = false;
	CIMObjectPath[] matchedFilterOps = null;
	
	IndicationDeliverer(String namespace, CIMInstance ci) {
	    this.ci = ci;
	    // Make namespace into its canonical form
	    CIMObjectPath tOp = new CIMObjectPath();
	    tOp.setNameSpace(namespace);
	    this.namespace = tOp.getNameSpace();
	}

	IndicationDeliverer(String namespace, CIMInstance ci, boolean polled) {
	    this.ci = ci;
	    // Make namespace into its canonical form
	    CIMObjectPath tOp = new CIMObjectPath();
	    tOp.setNameSpace(namespace);
	    this.namespace = tOp.getNameSpace();
	    this.polled = polled;
	}

	// This constructor handles cases where the event deliverer tells
	// us what filters have matched.
	IndicationDeliverer(CIMInstance ci, CIMObjectPath[] matchedFilterOps) {
	    this.ci = ci;
	    this.matchedFilterOps = matchedFilterOps;
	}

	//
	public boolean runAfter(List tasks, int size) {

	    return false;
	}

	// This one should actually part of CIMInstance, but we dont want
	// to make this public for now.
	// This method looks at the input attribute name and recursively
	// goes through embedded instances if required. Once it finds the
	// required embedded value/instance, it puts the same value/instance
	// along with its enclosing instances into the output instance.
	private void getProperty(CIMInstance inci, CIMInstance outci,
	String attrName) {
	    Vector propVec = outci.getProperties();
	    outci.setClassName(inci.getClassName());
	    StringTokenizer st = null;
	    try {
		st = new StringTokenizer(attrName, ".");
	    } catch (Exception e) {
		// Null or zero length
                Debug.trace2("Caught Exception in IndicationDeliverer.getProperty", e);

		return;
	    }
	    String token1 = (String)st.nextElement();
	    String token2 = null;
	    try {
		token2 = (String)st.nextElement();
	    } catch (Exception e) {
		// only one token
                Debug.trace2("Ignoring Exception in IndicationDeliverer.getProperty", e);
	    }
	    if (token2 == null) {
		CIMProperty cp = inci.getProperty(token1);
		boolean isInstance = false;
		try {
		    Object o = cp.getValue().getValue();
		    if (o instanceof CIMInstance) {
			isInstance = true;
		    }
		} catch (Exception e) {
		    // do nothing
                    Debug.trace2("Ignoring Exception in IndicationDeliverer.getProperty", e);
		}
		int rindex = propVec.indexOf(cp);
		if (rindex == -1) {
		    propVec.add(cp);
		} else {
		    if (isInstance) {
			// This may be a partially populated instance, replace
			// it with the whole instance. 
			// We must retain the property ordering also
			propVec.remove(rindex);
			propVec.insertElementAt(cp, rindex);
		    }
		}
	    } else {
		int index = propVec.indexOf(new CIMProperty(token1));
		CIMInstance tci;
		if (index == -1) {
		    tci = new CIMInstance();
		    tci.setProperties(new Vector());
		    CIMProperty tcp = new CIMProperty(token1);
		    tcp.setValue(new CIMValue(tci));
		    propVec.add(tcp);
		} else {
		    try {
			tci = 
			(CIMInstance)((CIMProperty)propVec.get(index)).
			getValue().getValue();
		    } catch (Exception e) {
			// Ignore for now. Either it is not an embedded instance
			// or its value is null;
                        Debug.trace2("Ignoring Exception in IndicationDeliverer.getProperty", e);
			return;
		    }
		}
		try {
		    CIMInstance tinci = 
		    (CIMInstance)inci.getProperty(token1).getValue().getValue();
		    getProperty(tinci, tci, 
		    attrName.substring(attrName.indexOf(".")+1));
		} catch (Exception e) {
		    // Ignore for now. Either it is not an embedded instance
		    // or its value is null;
                    Debug.trace2("Ignoring Exception in IndicationDeliverer.getProperty", e);
		    return;
		}
	    }
	}

	// This should really be a separate iterator associated to SelectList
	// but dont want it to be a public API for now.
	private CIMInstance selectListIterator(SelectList sl, 
	CIMInstance inci) throws CIMException {
	    // copied from SelectList apply code.
	    Enumeration e = sl.elements();
	    CIMInstance ci = new CIMInstance();
	    ci.setProperties(new Vector());
	    while (e.hasMoreElements()) {
		String attr = 
		((AttributeExp)e.nextElement()).getAttributeName();
		if (attr.equals("*")) {
		    return inci;
		}
		getProperty(inci, ci, attr);
	    }
	    return ci;
	}

	// Performance issue: This whole thing can use thread pools rather
	// than delivering the indications serially
	public void deliverEvent(CIMInstance indication, List faList) 
	throws Exception {
	    Iterator i = faList.iterator();
	    while (i.hasNext()) {
		FilterActivation fa = (FilterActivation)i.next();
		SelectList sl = fa.getParsedExp().getSelectList();
		CIMInstance selectedIndication;
		if (sl == null) {
		    selectedIndication = new CIMInstance();
		} else {
		    selectedIndication = (CIMInstance)sl.apply(indication);
		}
		// This is really not true - once you apply a select list,
		// the resulting class is an 'unnamed' class
		selectedIndication.setClassName(indication.getClassName());

		CIMObjectPath filterOp = fa.getFilterOp();
		CIMObjectPath assocOp = new CIMObjectPath();
		assocOp.setObjectName(FILTERDELIVERYASSOC);
		assocOp.setNameSpace(filterOp.getNameSpace());
		Vector tv = ps.associatorNames(assocOp, filterOp,
		    DELIVERYCLASS, "", "");
		Enumeration e = tv.elements();
		while (e.hasMoreElements()) {
		    CIMObjectPath op = (CIMObjectPath)e.nextElement();
		    CIMInstance ci = ps.getInstance(op);
		    IndicationHandler ih =
		    DeliveryHandler.getIndicationHandler(op.getObjectName());
		    if (ih != null) {
			// If ih is null, then we Cannot find a handler.
			// Oh well - at least its been logged.
			try {
			    ih.deliverEvent(new CIMEvent(
				selectedIndication), ci);
			} catch (Exception ex) {
			    Debug.trace2("Delivery failed", ex);
			    // Don't want no more trouble, get this guy outta
			    // here!
			    synchronized (cim_filterdelivery) {
				removeDelivery(op, assocOp);
			    }
			}
		    } else {
			try {
			    LogFile.add(LogFile.WARNING, "NO_HANDLER", 
				    op.getObjectName());
			} catch (Exception ex) {
			    // Ignore
                            Debug.trace2("Ignoring Exception in IndicationDeliverer.deliverEvent", ex);
			}
		    }
		}
	    }
	}

	public void run() {
	    if (matchedFilterOps != null) {
		List matchedFilters = new ArrayList();
		// This means the event generator has given us the matching
		// filters. Find the corresponding activations.
		for (int i = 0; i < matchedFilterOps.length; i++) {
		    CIMObjectPath filterOp = matchedFilterOps[i];
		    if (filterOp == null) {
			Debug.trace2("Received a null filter op, ignoring it");
			continue;
		    }

		    CIMInstance ci = null;
		    try {
			ci = ps.getInstance(filterOp);
		    } catch (CIMException ce) {
			Debug.trace2("Exception retrieving filter", ce);
		    }
		    // An exception above will cause delivery to fail
		    if (ci == null) {
			Debug.trace2("Corresponding filter not found for op");
			Debug.trace2("Not delivering events");
			return;
		    }

		    CIMObjectPath tempOp = 
		    new CIMObjectPath(filterOp.getObjectName().toLowerCase());
		    tempOp.setNameSpace(filterOp.getNameSpace().toLowerCase());
		    tempOp.setKeys(ci.getKeys());
		    FilterActivation fa = 
		    (FilterActivation)filterActivations.get(tempOp.toString());

		    if (fa == null) {
			Debug.trace2("Cannot find activation for " + tempOp);
			return;
		    }

		    matchedFilters.add(fa);
		}

		try {
		    deliverEvent(ci, matchedFilters);
		} catch (Exception e) {
		    // Ignore for now
		    Debug.trace2("Deliver event failed", e);
		}

	    } else {
		// Event deliverer didnt provide us with any filter info,
		// find all the activations that match by evaluating their
		// conditionals.
		String className = ci.getClassName().toLowerCase();
		int eventType = 
		FilterActivation.determineEventType(className);

		if (eventType != -1) {
		    // This is a standard life cycle indication
		    CIMInstance sourceInstance = (CIMInstance)ci.getProperty(
		    "SourceInstance").getValue().getValue();
		    className = sourceInstance.getClassName().toLowerCase();
		}

		List l = getClassFilters(namespace+":"+className, 
				    ci.getClassName(), polled);
		if (l == null) {
		    try {
			LogFile.add(LogFile.WARNING, "INVALID_INDICATION", 
			namespace, className, ci.toString());
		    } catch (Exception e) {
			// Ignore
                        Debug.trace2("Ignoring Exception in IndicationDeliverer.run", e);
		    }
		    return;
		}
		Iterator i = l.iterator();
		// Performance issue: Can be a map. Space-time tradeoff here.
		List matchedFilters = new ArrayList();

		// This can be moved into a query optimizer class for pluggable
		// query optimization
		while (i.hasNext()) {
		    FilterActivation.SubActivation sa = 
		    (FilterActivation.SubActivation)i.next();
		    FilterActivation fa = sa.getParentActivation();
		    if (matchedFilters.contains(fa)) {
			// The parent filter has already been matched
			continue;
		    }
		    try {
			if (sa.getExpression() == null ||
			sa.getExpression().apply(ci)) {
			    // ok, the instance satisfies the expression.
			    // Currently our filter handling is such that if 
			    // any subexpression matches, then the parent expression
			    // also matches (since fa = sa1 OR sa2 OR sa3)
			    matchedFilters.add(fa);
			} else {
			}
		    } catch (CIMException e) {
			// Once we verify in FilterActivation that all the 
			// properties are ok - we should not have any errors.
			// Ignore for now
			Debug.trace2(
			"Expression instance mismatch, check filter", e);
		    }
		}

		try {
		    deliverEvent(ci, matchedFilters);
		} catch (Exception e) {
		    // Ignore for now
		    Debug.trace2("Deliver event failed", e);
		}
	    }
	}
    }

    // This method returns immediately to the caller.
    public void deliverEvent(String namespace, CIMInstance ci) {
	task = (TaskManager.Task)new IndicationDeliverer(namespace, ci);
	tm.add(task);
    }

    // This method returns immediately to the caller.
    public void deliverEvent(String namespace, CIMInstance ci, boolean polled) {
	task = (TaskManager.Task)new IndicationDeliverer(namespace, ci, polled);
	tm.add(task);
    }

    // This method returns immediately to the caller.
    public void deliverEvent(CIMInstance ci, CIMObjectPath[] matchedFilterOps) {
	task = (TaskManager.Task)new IndicationDeliverer(ci, matchedFilterOps);
	tm.add(task);
    }
}
