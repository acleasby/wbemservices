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
 *are Copyright © 2003 Sun Microsystems, Inc.
 *
 *All Rights Reserved.
 *
 *Contributor(s): _______________________________________
*/

package org.wbemservices.wbem.cimom;

import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMObjectPath;
import javax.wbem.client.Debug;

// This class encapsulates the algorithm to issue requests to subactivations,
// collate their provider requests and return results to them. It takes care
// of situations where there may be exceptions, and unprocessed subactivations
// need to be notified.
class IndicationRequestCollator {
    List subActList;
    Callback cb;

    // The invoking client class has to pass in a callback object.
    interface Callback {
	FilterActivation.EventProviderRequest 
	doSubActivationOperation(FilterActivation.SubActivation sa) 
	throws CIMException;

	Object doProviderOperation(String[] filters, CIMObjectPath[] classPaths,
	String[] eventTypes) throws CIMException;

	void processSingleResult(FilterActivation.SubActivation sa, 
	Object result) throws CIMException;

	void processSingleException(FilterActivation.SubActivation sa, 
	CIMException ce) throws CIMException;

	void processSingleNoResult(FilterActivation.SubActivation sa)
	throws CIMException;
    }

    IndicationRequestCollator(List subActList, Callback cb) {
	this.subActList = subActList;
	this.cb = cb;
    }

    /**
     * This method actually does the processing of the subactivation list.
     * Every subactivation that wants an EventProviderRequest is kept in a
     * separate list. All EventProviderRequests and the corresponding
     * subactivations to the same provider are maintained in separate lists.
     * Next these requests are combined and passed to the provider. The results
     * from the provider are individually sent back to the subactivations. If
     * there was an exception, or if the operation was not done, the
     * subactivations are notified of this also.
     * 
     * @return <code>Object[]</code> which contains two values. The first one
     *         is an exception, will be null if there was no exception, the
     *         second one is the list of subactivations that have been
     *         processed. For type safety we should have defined a return
     *         class, but it is internally used, so we leave it for now.
     */
    Object[] processEventRequest() {
	// Holder for any exception that may occur.
	CIMException ce = null;
	Iterator subActivations = subActList.iterator();
	List finishedActivations = new ArrayList();

	Map requestMap = new HashMap();
	while (subActivations.hasNext()) {
	    FilterActivation.SubActivation sa = 
	    (FilterActivation.SubActivation) subActivations.next();
	    // This part could be multithreaded. The SubActivation
	    // updates the classFilter mapping.
	    FilterActivation.EventProviderRequest request = null;
	    try {
		request = cb.doSubActivationOperation(sa);
	    } catch (Throwable th) {
		Debug.trace2("Got an exception getting poll info", th);
		if (!(th instanceof CIMException)) {
		    ce = new CIMException(CIMException.CIM_ERR_FAILED,
		    th.toString());
		} else {
		    ce = (CIMException)th;
		}
		break;
	    }

	    if (request == null) {
		// This means that the subactivation has done its work,
		// no collation or provider contact is required.
		finishedActivations.add(sa);
	    } else {
		// This subactivation wants to get information from a
		// provider.
		String mapKey;
		request.getEventType();
		CIMObjectPath classPath = request.getClassPath();
		if ((classPath.getObjectName() == null) ||
		(classPath.getObjectName().length() == 0)) {
		    mapKey = classPath + ":" + request.getEventType();
		} else {
		    mapKey = classPath.toString();
		}
		Debug.trace2("Mapkey is "+mapKey);
		List activationList = (List)requestMap.get(mapKey);
		if (activationList == null) {
		    activationList = new ArrayList();
		    requestMap.put(mapKey, activationList);
		}
		// Add this request and its subactivation to the list
		// for the given class to be evented on.
		activationList.add(sa);
		activationList.add(request);
	    }
	}

	Iterator activationLists = requestMap.values().iterator();
	while ((ce == null) && (activationLists.hasNext())) {
	    List activationList = (List)activationLists.next();
	    Debug.trace2("The activation list is "+activationList);
	    Iterator i = activationList.iterator();
	    // All requests should be collated
	    List subFilterList = new ArrayList();
	    List cpList = new ArrayList();
	    List eventTypeList = new ArrayList();
	    List currentSubActs = new ArrayList();
	    // Setting up the request
	    while (i.hasNext()) {
		FilterActivation.SubActivation sa = 
		(FilterActivation.SubActivation)i.next();
		// Store the subactivation, we need to return the result
		// to it.
		currentSubActs.add(sa);
		FilterActivation.EventProviderRequest request = 
		(FilterActivation.EventProviderRequest)i.next();
		// construct the request
		subFilterList.add(request.getFilter().toString());
		cpList.add(request.getClassPath());
		eventTypeList.add(request.getEventType());
		Debug.trace2("sub filter list\n"+subFilterList);
	    }
	    // issue the request
	    try {
		String[] filters =
		(String [])
		subFilterList.toArray(new String[subFilterList.size()]);

		CIMObjectPath[] classPaths =
		(CIMObjectPath[])
		cpList.toArray(new CIMObjectPath[cpList.size()]);

		String[] eventTypes =
		(String[])
		eventTypeList.toArray(new String[eventTypeList.size()]);

		Debug.trace2("Issuing the request");
		Object actionResult = cb.doProviderOperation(filters,
		classPaths, eventTypes);

		// let each of the activations know their result
		Iterator csai = currentSubActs.iterator();
		while (csai.hasNext()) {
		    try {
			FilterActivation.SubActivation csa = 
			(FilterActivation.SubActivation)csai.next();
			cb.processSingleResult(csa, actionResult);
			finishedActivations.add(csa);
		    } catch (Throwable th) {
			// Should always be a CIMException here, but
			// dont want to take the chance.
			Debug.trace2("Got an exception getting poll info", th);
			if (!(th instanceof CIMException)) {
			    ce = new CIMException(CIMException.CIM_ERR_FAILED,
			    th.toString());
			} else {
			    ce = (CIMException)th;
			}
			// Tell each of the remaining subactivations that there 
			// was an exception
			while (csai.hasNext()) {
			    FilterActivation.SubActivation csa = 
			    (FilterActivation.SubActivation)csai.next();
			    try {
				cb.processSingleException(csa, ce);
			    } catch (Throwable pth) {
				// there shouldnt be an exception, but just
				// in case
				Debug.trace2("Got an exception", pth);
				// ignore it
			    }
			}
			// leave the loop and tell the rest of sub activations
			// that there was no result
			break;
		    }

		    if (ce != null) {
			// there was an exception, stop processing
			break;
		    }
		}

		// leave the loop and tell the rest of sub activations
		// that there was no result
		if (ce != null) {
		    break;
		}

	    } catch (Throwable th) {
		Debug.trace2("Got an exception getting poll info", th);
		if (!(th instanceof CIMException)) {
		    ce = new CIMException(CIMException.CIM_ERR_FAILED,
		    th.toString());
		} else {
		    ce = (CIMException)th;
		}
		// Tell each of the subactivations that there was an
		// exception
		Iterator csai = currentSubActs.iterator();
		while (csai.hasNext()) {
		    FilterActivation.SubActivation csa = 
		    (FilterActivation.SubActivation)csai.next();
		    try {
			cb.processSingleException(csa, ce);
		    } catch (Throwable pseth) {
			Debug.trace2("Got an exception", th);
			// Shouldnt happen - we'll ignore it if it does.
		    }
		}
		// leave the loop and tell the rest of sub activations
		// that there was no result
		break;
	    }
	}

	while (activationLists.hasNext()) {
	    // tell the rest of the sub activations that there was no result.
	    List activationList = (List)activationLists.next();
	    Iterator i = activationList.iterator();
	    while (i.hasNext()) {
		FilterActivation.SubActivation sa = 
		(FilterActivation.SubActivation)i.next();
		// Get rid of the poll request
		i.next();
		// Let the subactivation know that there was no result.
		try {
		    cb.processSingleNoResult(sa);
		} catch (Throwable th) {
		    // We shouldnt get any exception here, but just in case.
		    Debug.trace2("Got an exception", th);
		    // ignore it.
		}
	    }
	}

	// We have two results, the exception, if any and the list of
	// finished Activations.
	Object[] oArray = new Object[2];
	oArray[0] = ce;
	oArray[1] = finishedActivations;
	return oArray;
    }
}
