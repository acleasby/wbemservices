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
 *Contributor(s): AppIQ, Inc.____________________________
*/

package org.wbemservices.wbem.cimom;

import javax.wbem.provider.EventProvider;
import javax.wbem.provider.CIMIndicationProvider;
import javax.wbem.provider.CIMProvider;
import javax.wbem.provider.Authorizable;
import javax.wbem.provider.CIMInstanceProvider;
import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMInstance;
import javax.wbem.cim.CIMObjectPath;
import javax.wbem.cim.CIMClass;
import javax.wbem.cim.CIMQualifier;
import javax.wbem.cim.CIMValue;
import javax.wbem.cim.CIMClassException;
import javax.wbem.client.CIMProviderException;
import javax.wbem.client.Debug;
import javax.wbem.query.SelectExp;
import javax.wbem.query.QueryExp;
import javax.wbem.query.BinaryRelQueryExp;
import javax.wbem.query.Query;
import javax.wbem.query.AndQueryExp;
import javax.wbem.query.NonJoinExp;
import javax.wbem.query.AttributeExp;
import javax.wbem.query.QualifiedAttributeExp;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Set;
import java.util.TreeSet;

/*
 * This class takes in input filters to determine what classes must be evented
 * on. Once it determines what filters must be applied to what classes, it 
 * invokes the eventService with this information. 
 */
public class FilterActivation {

    // The three intrinsic instance level indications
    public final static String INSTANCEADDITION = "cim_instcreation";
    private final static int INSTANCEADDITIONTYPE = 0;
    public final static String INSTANCEDELETION = "cim_instdeletion";
    final static int INSTANCEDELETIONTYPE = 1;
    public final static String INSTANCEMODIFICATION = "cim_instmodification";
    final static int INSTANCEMODIFICATIONTYPE = 2;
    public final static String INSTANCEREAD = "cim_instread";
    final static int INSTANCEREADTYPE = 3;
    public final static String INSTANCEMETHODCALL = "cim_instmethodcall";
    public final static int INSTANCEMETHODCALLTYPE = 4;
    public final static String CLASSCREATION = "cim_classcreation";
    public final static String CLASSDELETION = "cim_classdeletion";
    public final static String CLASSMODIFICATION = "cim_classmodification";

    List result = null;
    CIMException exception = null;
    CIMInstance filterInstance = null;
    CIMObjectPath filterOp = null;
    CIMObjectPath targetNameSpace = null;
    SelectExp parsedExp = null;
    List canonizedExp = null;
    EventService eventService = null;

    // This is the base class for all requests to the event providers. Instances
    // of these classes are created by the SubActivation sub classes, when they
    // want to request information from a provider.
    static class EventProviderRequest {
	private SelectExp filter;
	private String eventType;
	private CIMObjectPath classPath;

	EventProviderRequest(SelectExp filter, String eventType, 
	CIMObjectPath classPath) {
	    this.filter = filter;
	    this.eventType = eventType;
	    this.classPath = classPath;
	}

	// accessors
	SelectExp getFilter() {
	    return filter;
	}

	String getEventType() {
	    return eventType;
	}

	CIMObjectPath getClassPath() {
	    return classPath;
	}
    }

    static final class PollInfoRequest extends EventProviderRequest {
	PollInfoRequest(SelectExp filter, String eventType, 
	CIMObjectPath classPath) {
	    super(filter, eventType, classPath);
	}
    }

    static final class AuthorizeRequest extends EventProviderRequest {
	private String owner;

	AuthorizeRequest(SelectExp filter, String eventType, 
	CIMObjectPath classPath, String owner) {
	    super(filter, eventType, classPath);
	    this.owner = owner;
	}

	String getOwner() {
	    return owner;
	}
    }

    static final class ActivateRequest extends EventProviderRequest {
	private boolean firstActivation;

	ActivateRequest(SelectExp filter, String eventType, 
	CIMObjectPath classPath, boolean firstActivation) {
	    super(filter, eventType, classPath);
	    this.firstActivation = firstActivation;
	}

	// accessors
	boolean getFirstActivation() {
	    return firstActivation;
	}
    }

    static final class DeactivateRequest extends EventProviderRequest {
	private boolean lastActivation;

	DeactivateRequest(SelectExp filter, String eventType, 
	CIMObjectPath classPath, boolean lastActivation) {
	    super(filter, eventType, classPath);
	    this.lastActivation = lastActivation;
	}

	// accessors
	boolean getLastActivation() {
	    return lastActivation;
	}
    }

    /*
     * This class contains all the subactivations that the parent filter has
     * been broken down into. If a request needs to be made to an event 
     * provider, the sub activation tells the parent activation what to ask 
     * the provider. The parent activation then lets the sub activation know 
     * the result through the process... methods. The reason for doing it this 
     * way, is that for smart event providers, the subactivation requests have 
     * to be merged together into one call.
     * In case there is no event provider, the subactivation does not need
     * to go through the above.
     *
     */
    abstract class SubActivation {
	QueryExp expression;
	String className;
	boolean authorizable = false;
	boolean polled = false;
	SelectExp subSelectExp;
	String subIndicationType;
	int eventType = -1;
	CIMProvider localep;

	SelectExp getSubSelectExp() {
	    return subSelectExp;
	}

	QueryExp getExpression() {
	    return expression;
	}

	String getClassName() {
	    return className;
	}

	FilterActivation getParentActivation() {
	    return FilterActivation.this;
	}

	String getIndicationType() {
	    return subIndicationType;
	}

	int getEventType() {
	    return eventType;
	}

	SubActivation(String indicationTypeString, QueryExp expression, 
	String className) {
	    this.expression = expression;
	    this.className = className;
	    subSelectExp = new SelectExp(
		parsedExp.getSelectList(), 
		new NonJoinExp(new 
		    QualifiedAttributeExp(indicationTypeString, null, null)), 
		expression);
	    this.subIndicationType = indicationTypeString;
	    eventType = determineEventType(subIndicationType);
	}

	// This method gets information to determine if the events are generated
	// by providers or need to be polled for. If providers need to be
	// asked, this method returns a PollInfoRequest which is passed to
	// providers. The result of the request is returned to processPollInfo.
	abstract PollInfoRequest getPollInfo() 
	throws CIMException; 

	// This method is invoked by the parent filter activation. It provides
	// the subactivation with the responses from the provider.
	void processPollInfo(boolean hasResult, boolean pollFlag, 
	CIMException ce) 
	throws CIMException {
	    // do nothing. Subclass can override.
	}

	// Method to get check if the subscription is authorized.
	// If providers need to be asked, this method returns a AuthorizeRequest
	// which is passed to providers. The result is returned to
	// processAuthorizeFailure.
	abstract AuthorizeRequest authorize(String owner) 
	throws CIMException; 

	// Called by the parent activation to indicate success or failure from
	// the provider. ce will be null if the call succeeded.
	void processAuthorizeFailure(boolean hasResult, CIMException ce) 
	throws CIMException {
	    // do nothing. Subclass can override.
	}

	// Method to activate the filter
	// If providers need to be asked, this method returns a ActivateRequest
	// which is passed to providers. The result is returned to
	// processActivateFailure.
	abstract ActivateRequest activate() throws CIMException;

	// Called by the parent activation to indicate success or failure from
	// the provider. ce will be null if the call succeeded.
	void processActivateFailure(boolean hasResult, CIMException ce) 
	throws CIMException {
	    // do nothing. Subclass can override.
	}

	// Method to reactivate the filter when a new subscription is made
	// to an already activated filter
	// If providers need to be asked, this method returns a ActivateRequest
	// which is passed to providers. No result is returned, because
	// currently we dont do anything with it.
	abstract ActivateRequest activateSubscription() throws CIMException;

	// Method to deactivate a subscription to a filter which has more
	// pending subscription.
	// If providers need to be asked, this method returns a 
	// DeactivateRequest. No result is passed back, we assume nothing 
	// further can/needs to be done in the event of a failure.
	abstract DeactivateRequest deactivateSubscription() throws CIMException;

	// Method to deactivate the filter
	// If providers need to be asked, this method returns a 
	// DeactivateRequest. No result is passed back, we assume nothing 
	// further can/needs to be done in the event of a failure.
	abstract DeactivateRequest deactivate() throws CIMException;

    }

    class PISubActivation extends SubActivation {

	boolean isSmartIndicationProvider = false;

	PISubActivation(String indicationTypeString, QueryExp expression, 
	String className) {
	    super(indicationTypeString, expression, className);
	}

	PollInfoRequest getPollInfo() throws CIMException {
	    CIMClass cc; 
	    boolean checkedForSmartIP = false;

	    // This is a process indication, which must be in the same
	    // namespace as the filter
	    cc = eventService.ps.getClass(
	    filterOp.getNameSpace(), subIndicationType);
	    // Get the event provider

	    try {
		localep = 
		CIMOMImpl.getProviderFactory().getEventProvider(
		filterOp.getNameSpace(), cc);
	    } catch (CIMException e) {
		// Ok look for the smart one
		try {
		    checkedForSmartIP = true;
		    localep = 
		    CIMOMImpl.getProviderFactory().getCIMIndicationProvider(
		    filterOp.getNameSpace(), cc);
		} catch (CIMException ce) {
		}
	    }

	    if (localep == null) {
		// For process indications, an event provider must
		// be present.
		throw new CIMProviderException(
		CIMProviderException.NO_EVENT_PROVIDER, subIndicationType,
		"");
	    }

	    if (checkedForSmartIP) {
		isSmartIndicationProvider = true;
	    }

	    // Lets see if this is authorizable
	    Authorizable tempAuth = null;

	    try {
		tempAuth = CIMOMImpl.getProviderFactory().
		getAuthorizableProvider(filterOp.getNameSpace(), cc);
	    } catch (CIMException ce) {
		// if we get a NOT_AUTHORIZABLE_PROVIDER exception, it
		// means the provider was found, but it doesnt want to do
		// authorization - so we do the default check. Otherwise,
		// we throw the exception.
		if (!ce.getID().equals(
		CIMProviderException.NOT_AUTHORIZABLE_PROVIDER)) {
		    throw ce;
		}
	    }

	    if (tempAuth != null) {
		authorizable = true;
	    } else {
		authorizable = false;
	    }

	    // We dont need to get anything from the provider
	    return null;
	}

	AuthorizeRequest authorize(String owner) 
	throws CIMException {
	    CIMObjectPath classPath;
	    classPath = new CIMObjectPath("", filterOp.getNameSpace());
	    if (authorizable) {
		// For now all filters are SelectExps
		// Let the parent know that this filter needs to be authorized.
		if (isSmartIndicationProvider) {
		    return new AuthorizeRequest(getSubSelectExp(), 
		    subIndicationType, classPath, owner);
		} else {
		    ((EventProvider)localep).authorizeFilter(getSubSelectExp(),
		    subIndicationType, classPath, owner);
		    return null;
		}
	    } else {
		// Do the default check. This is really ugly. We need
		// to move the capability check into CIMOMUtils.
		eventService.cimom.delayedCapabilityCheck((CIMClass) null,
			false, CIMOMImpl.READ,
			classPath.getNameSpace());
		// Dont need anything from the provider
		return null;
	    }
	}

	ActivateRequest activate() throws CIMException {
	    boolean firstFilter = eventService.addClassFilter(
	    filterOp.getNameSpace()+":"+ subIndicationType, this, polled);

	    CIMObjectPath classPath;
	    classPath = new CIMObjectPath("",
	    filterOp.getNameSpace());

	    if (isSmartIndicationProvider) {
		return new ActivateRequest(getSubSelectExp(),
		subIndicationType, classPath, firstFilter);
		// if activation fails, or is not done, 
		// processActivateFailure will be called
	    }

	    boolean failed = true;
	    EventProvider ep = (EventProvider)localep;
	    try {
		ep.activateFilter(getSubSelectExp(), 
		subIndicationType, classPath, firstFilter);
		failed = false;
		return null;
	    } finally {
		if (failed) {
		    processActivateFailure(false, null);
		}
	    }
	}

	void processActivateFailure(boolean hasResult, CIMException ce) 
	throws CIMException {
	    eventService.removeClassFilter(
	    filterOp.getNameSpace()+":"+ subIndicationType, this, 
	    polled);
	}

	ActivateRequest activateSubscription() throws CIMException {

	    CIMObjectPath classPath;
	    classPath = new CIMObjectPath("",
	    filterOp.getNameSpace());

	    // Only new providers need to do this
	    if (isSmartIndicationProvider) {
		return new ActivateRequest(getSubSelectExp(),
		subIndicationType, classPath, false);
		// if activation fails, or is not done, 
		// processActivateFailure will be called
	    } else {
		return null;
	    }
	}

	DeactivateRequest deactivateSubscription() throws CIMException {
	    if (isSmartIndicationProvider) {
		CIMObjectPath classPath;
		classPath = new CIMObjectPath("",
		filterOp.getNameSpace());
		return new DeactivateRequest(getSubSelectExp(),
		subIndicationType, classPath, false);
	    } else {
		return null;
	    }
	}

	DeactivateRequest deactivate() throws CIMException {
	    boolean lastFilter = eventService.removeClassFilter(
		filterOp.getNameSpace()+":"+ subIndicationType, this, polled);
	    CIMObjectPath classPath;
	    classPath = new CIMObjectPath("",
		filterOp.getNameSpace());
	    if (isSmartIndicationProvider) {
		return new DeactivateRequest(getSubSelectExp(),
		subIndicationType, classPath, lastFilter);
	    } else {
		((EventProvider)localep).deActivateFilter(getSubSelectExp(),
		subIndicationType, classPath, lastFilter);
		return null;
	    }
	}
    }

    // Currently this class just assumes that the repository handles class
    // indications. However, we may want to get the provider for this
    // from the provider checker in the future. Since the repository is the
    // provider, it is not a CIMEventProvider. We just deal with it as a
    // normal EventProvider.
    class CISubActivation extends SubActivation {
	EventProvider ep;

	CISubActivation(String indicationTypeString, QueryExp expression, 
	String className) {
	    super(indicationTypeString, expression, className);
	}

	PollInfoRequest getPollInfo() throws CIMException {
	    // This is not being polled, the repository should
	    // handle this.
	    ep = eventService.ps;
	    // We dont need to get anything from a provider.
	    return null;
	}

	AuthorizeRequest authorize(String owner) 
	throws CIMException {
	    // Do the default check. This is really ugly. We need
	    // to move the capability check into CIMOMUtils.
	    eventService.cimom.delayedCapabilityCheck((CIMClass) null,
			false, CIMOMImpl.READ,
			targetNameSpace.getNameSpace());
	    // Nothing needed from the provider
	    return null;
	}

	ActivateRequest activate() throws CIMException {

	    boolean firstFilter;

	    firstFilter = eventService.addClassFilter(
	    targetNameSpace.getNameSpace()+":"+ subIndicationType, 
	    this, polled);

	    CIMObjectPath classPath;
	    classPath = new CIMObjectPath("",
	    targetNameSpace.getNameSpace());

	    boolean failed = true;

	    try {
		ep.activateFilter(getSubSelectExp(),
		subIndicationType, classPath, firstFilter);
		failed = false;
	    } finally {
		if (failed) {
		    eventService.removeClassFilter(
		    targetNameSpace.getNameSpace()+":"+ subIndicationType, this,
		    polled);
		}
	    }

	    // Nothing needed from the provider
	    return null;
	}

	ActivateRequest activateSubscription() {
	    // Dont need to do anything here.
	    return null;
	}

	DeactivateRequest deactivateSubscription() throws CIMException {
	    return null;
	}

	DeactivateRequest deactivate() throws CIMException {
	    boolean lastFilter;
	    lastFilter = eventService.removeClassFilter(
		targetNameSpace.getNameSpace()+":"+ subIndicationType, this, 
		polled);
	    CIMObjectPath classPath;
	    classPath = new CIMObjectPath("",
		targetNameSpace.getNameSpace());
	    ep.deActivateFilter(getSubSelectExp(),
	    subIndicationType, classPath, lastFilter);
	    // Nothing needed from the provider
	    return null;
	}
    }

    class ILCSubActivation extends SubActivation {
	boolean hasIndicationProvider = false;
	boolean hasInstanceProvider = false;
	CIMInstanceProvider instanceProvider = null;
	boolean isSmartIndicationProvider = false;

	ILCSubActivation(String indicationTypeString, QueryExp expression, 
	String className) {
	    super(indicationTypeString, expression, className);
	}

	PollInfoRequest getPollInfo() throws CIMException {
	    boolean checkForSmartIP = false;

	    CIMClass cc = eventService.ps.getClass(
	    targetNameSpace.getNameSpace(), className);

            // <PJA> 1-August-2002
            // Indication Provider need not be an instance provider - then getInstanceProvider
            // will throw NOT_INSTANCE_PROVIDER rather than returning null
            try {
	        instanceProvider = CIMOMImpl.getProviderFactory().getInstanceProvider(
		targetNameSpace.getNameSpace(), cc);

	    if (instanceProvider != null) {
		hasInstanceProvider = true;
	    }
            }
            catch (CIMException ce) {
                if (!ce.getID().equals(CIMProviderException.NOT_INSTANCE_PROVIDER)) {
		    throw ce;
		}

                instanceProvider = null;
                hasInstanceProvider = false;
            }

	    try {
		localep = CIMOMImpl.getProviderFactory().getEventProvider(
		targetNameSpace.getNameSpace(), cc);
	    } catch (CIMException ce) {
		// if we get a NOT_EVENT_PROVIDER exception, it
		// means the provider was found, but it is not an event
		// provider. Thats ok for now, ep will remain as null.
		if (!ce.getID().equals(
		CIMProviderException.NOT_EVENT_PROVIDER)) {
		    throw ce;
		}
		try {
		    checkForSmartIP = true;
		    localep = 
		    CIMOMImpl.getProviderFactory().getCIMIndicationProvider(
		    targetNameSpace.getNameSpace(), cc);
		} catch (CIMException ce2) {
		    // if we get a NOT_INDICATION_PROVIDER exception, it
		    // means the provider was found, but it is not an event
		    // provider. Thats ok for now, ep will remain as null.
		    if (!ce2.getID().equals(
		    CIMProviderException.NOT_INDICATION_PROVIDER)) {
			throw ce2;
		    }
		}
	    }

	    if (localep != null) {
		hasIndicationProvider = true;
		if (checkForSmartIP) {
		    isSmartIndicationProvider = true;
		}
	    }

	    if (!hasInstanceProvider && !hasIndicationProvider) {
		// There is no provider
		LogFile.add(LogFile.DEVELOPMENT, "DEBUG_VALUE", "Handled by",
		"repository");
		if (eventType == INSTANCEMETHODCALLTYPE) {
		    // Method indications need a provider
		    throw new CIMProviderException(
		    CIMProviderException.NO_EVENT_PROVIDER, subIndicationType,
		    "");
		}
		// No need to ask the provider anything.
		return null;
	    } else {
		// ask the provider
		if (!hasIndicationProvider && (eventType == INSTANCEREADTYPE ||
		    eventType == INSTANCEMETHODCALLTYPE)) {
		    // Read, method indications cannot be polled for
		    throw new CIMProviderException(
			CIMProviderException.NOT_EVENT_PROVIDER, 
			subIndicationType,
			instanceProvider.getClass().getName());
		}

		if (!hasIndicationProvider) {
		    // There is no event provider, we must poll.
		    LogFile.add(LogFile.DEVELOPMENT, "DEBUG_VALUE", 
			    "Handled by", "poller, not an event provider");
		    polled = true;
		    // No need to ask the provider anything.
		    return null;
		} else {
		    // Lets see if this is the provider handles authorization.
		    Authorizable tempAuth = null;

		    try {
			tempAuth =
			CIMOMImpl.getProviderFactory().getAuthorizableProvider(
			filterOp.getNameSpace(), cc);
		    } catch (CIMException ce) {
			// if we get a NOT_AUTHORIZABLE_PROVIDER exception, it
			// means the provider was found, but it doesnt want to 
			// do authorization - so we do the default check. 
			// Otherwise, we throw the exception.
			if (!ce.getID().equals(
			CIMProviderException.NOT_AUTHORIZABLE_PROVIDER)) {
			    throw ce;
			}
		    }

		    if (tempAuth == null) {
			authorizable = true;
		    } else {
			authorizable = false;
		    }
		    CIMObjectPath classPath = new CIMObjectPath(className,
		    targetNameSpace.getNameSpace());

		    if ((eventType != INSTANCEREADTYPE) &&
		    (eventType != INSTANCEMETHODCALLTYPE)) {
			// Except for read/method events, we need to check if 
			// the provider wants us to poll. We cannot poll for 
			// reads/method calls.
			if (isSmartIndicationProvider) {
			    return new PollInfoRequest(getSubSelectExp(), 
			    getIndicationType(), classPath);
			    // The result will be returned in processPollInfo
			} else {
			    boolean pollResult =
			    ((EventProvider)localep).mustPoll(getSubSelectExp(),
			    getIndicationType(), classPath);
			    processPollInfo(true, pollResult, null);
			    return null;
			}
		    } else {
			// Dont need to ask the provider, we wont poll
			return null;
		    }
		}
	    }
	}

	void processPollInfo(boolean hasResult, boolean pollFlag, 
	CIMException ce) 
	throws CIMException {
	    if (!hasResult) {
		// There is no result to process. Just return.
		return;
	    }

	    if (ce != null) {
		// There was an exception, nothing for us to do then.
		return;
	    }

	    if (pollFlag) {
		polled = true;
		LogFile.add(LogFile.DEVELOPMENT, "DEBUG_VALUE", 
		"Handled by", "poller, event provider wants a poll");
		if (EventService.pollInterval < 0) {
		    // No polling allowed
		    throw new CIMException(CIMException.CIM_ERR_FAILED);
		}
		if (!hasInstanceProvider) {
		    throw new CIMProviderException(
		    CIMProviderException.NO_INSTANCE_PROVIDER,
		    targetNameSpace.getNameSpace()+":"+className);
		}
	    }
	}

    AuthorizeRequest authorize(String owner) 
    throws CIMException {
        CIMObjectPath classPath = new CIMObjectPath(className,
            targetNameSpace.getNameSpace());
        if (authorizable == true) {
            // For now all filters are SelectExps
            if (isSmartIndicationProvider) {
                return new AuthorizeRequest(getSubSelectExp(), 
                    getIndicationType(), classPath, owner);
                // No need to process the result.
            } else {
                ((EventProvider)localep).authorizeFilter(getSubSelectExp(),
                    getIndicationType(), classPath, owner);
                return null;
            }
        } else {
            if (polled) {
                // Ask the poller
                IndicationPoller ip = eventService.getIndicationPoller();
                    ip.authorizeFilter(getSubSelectExp(), getIndicationType(),
                    classPath, owner);
                // Nothing needed from the provider
                return null;
            } else {
                // Do the default check. This is really ugly. We need
                // to move the capability check into CIMOMUtils.
                eventService.cimom.delayedCapabilityCheck((CIMClass) null,
                    false, CIMOMImpl.READ,
                    classPath.getNameSpace());
                // Nothing needed from the provider
                return null;
            }
        }
    }

	ActivateRequest activate() throws CIMException {

	    boolean shouldActivate = eventService.addClassFilter(
	    targetNameSpace.getNameSpace()+":"+ className, this, polled);

	    boolean repository = (!hasIndicationProvider && 
	    !hasInstanceProvider);

	    if (!shouldActivate) {
		if (repository) {
		    // Repository has already been activated
		    return null;
		}

		if (polled) {
		    // Polling has already been activated
		    return null;
		}
	    }

	    boolean failed = true;

	    try {

		if (repository) { 
		    // No provider - repository will handle this one
		    CIMObjectPath classPath = new CIMObjectPath(className,
			targetNameSpace.getNameSpace());
		    switch (eventType) {
			case INSTANCEADDITIONTYPE:
			eventService.ps.additionTriggerActivate(
			targetNameSpace.getNameSpace(), className);
			break;
			case INSTANCEMODIFICATIONTYPE:
			eventService.ps.modificationTriggerActivate(
			targetNameSpace.getNameSpace(), className);
			break;
			case INSTANCEDELETIONTYPE:
			eventService.ps.deletionTriggerActivate(
			targetNameSpace.getNameSpace(), className);
			break;
			case INSTANCEREADTYPE:
			eventService.ps.activateFilter(getSubSelectExp(),
			getIndicationType(), classPath, shouldActivate);
			break;
		    }
		    failed = false;
		    // Nothing needed from the provider
		    return null;
		} else {

		    if (!polled) {
			// we've found an event provider
			CIMObjectPath classPath = new CIMObjectPath(className,
			targetNameSpace.getNameSpace());

			if (isSmartIndicationProvider) {
			    // For now we have not failed. We'll process the
			    // result in processActivateFailure.
			    failed = false;
			    return new ActivateRequest (getSubSelectExp(),
			    getIndicationType(), classPath, shouldActivate);
			} else {
			    ((EventProvider)localep).activateFilter(
			    getSubSelectExp(), getIndicationType(), classPath,
			    shouldActivate);
			    failed = false;
			    return null;
			}
		    }

		    IndicationPoller ip = eventService.getIndicationPoller();
		    switch (eventType) {
			case INSTANCEADDITIONTYPE:
			ip.additionTriggerActivate(this);
			break;
			case INSTANCEMODIFICATIONTYPE:
			ip.modificationTriggerActivate(this);
			break;
			case INSTANCEDELETIONTYPE:
			ip.deletionTriggerActivate(this);
			break;
			// Read events are not polled.
		    }
		    failed = false;
		    return null;
		}
	    } finally {
		if (failed) {
		    eventService.removeClassFilter(
		    targetNameSpace.getNameSpace()+":"+ className, this, 
		    polled);
		}
	    }
	}

	ActivateRequest activateSubscription() throws CIMException {
	    // Only new providers need to do this
	    boolean repository = (!hasIndicationProvider && 
	    !hasInstanceProvider);
	    if (repository || polled) {
		return null;
	    }

	    if (isSmartIndicationProvider) {
		CIMObjectPath classPath = new CIMObjectPath(className,
		targetNameSpace.getNameSpace());
		// For now we have not failed. We'll process the
		// result in processActivateFailure.
		return new ActivateRequest (getSubSelectExp(),
		getIndicationType(), classPath, false);
	    } else {
		return null;
	    }
	}

	void processActivateFailure(boolean hasResult, CIMException ce) 
	throws CIMException {
	    // We treat not having a result as well as an exception as a
	    // failure.
	    eventService.removeClassFilter(
	    targetNameSpace.getNameSpace()+":"+ className, this, 
	    polled);
	}

	DeactivateRequest deactivateSubscription() throws CIMException {
	    boolean repository = (!hasIndicationProvider && 
	    !hasInstanceProvider);
	    if (repository || polled) {
		return null;
	    }
	    if (isSmartIndicationProvider) {
		CIMObjectPath classPath = new CIMObjectPath(className,
		    targetNameSpace.getNameSpace());
		return new DeactivateRequest(
		getSubSelectExp(), getIndicationType(), classPath, 
		false);
	    } else {
		return null;
	    }
	}

	DeactivateRequest deactivate() throws CIMException {
	    boolean shouldDeActivate = eventService.removeClassFilter(
	    targetNameSpace.getNameSpace()+":"+ className, this, polled);
	    boolean repository = (!hasIndicationProvider && 
	    !hasInstanceProvider);
	    if (!shouldDeActivate) {
		if (repository) {
		    // Repository shouldnt be deactivated yet
		    return null;
		}

		if (polled) {
		    // Polling shouldnt be deactivated yet
		    return null;
		}
	    }

	    if (repository) {
		// No provider - repository will handle this one
		// eventService.ps.addTrigger(targetNameSpace.getNameSpace(),
		//   className, parsedExp.getFromClause());
		switch (eventType) {
		    case INSTANCEADDITIONTYPE:
		    eventService.ps.additionTriggerDeActivate(
		    targetNameSpace.getNameSpace(), className);
		    break;
		    case INSTANCEMODIFICATIONTYPE:
		    eventService.ps.modificationTriggerDeActivate(
		    targetNameSpace.getNameSpace(), className);
		    break;
		    case INSTANCEDELETIONTYPE:
		    eventService.ps.deletionTriggerDeActivate(
		    targetNameSpace.getNameSpace(), className);
		    break;
		}
		return null;
	    } else {
		// Stop polling
		if (!polled) {
		    // we've found an event provider
		    CIMObjectPath classPath = new CIMObjectPath(className,
			targetNameSpace.getNameSpace());
		    
		    if (isSmartIndicationProvider) {
			return new DeactivateRequest(
			getSubSelectExp(), getIndicationType(), classPath, 
			shouldDeActivate);
		    } else {
			((EventProvider)localep).deActivateFilter(
			getSubSelectExp(), getIndicationType(), classPath, 
			shouldDeActivate);
			return null;
		    }
		}

		IndicationPoller ip = eventService.getIndicationPoller();
		switch (eventType) {
		    case INSTANCEADDITIONTYPE:
		    ip.additionTriggerDeActivate(this);
		    break;
		    case INSTANCEMODIFICATIONTYPE:
		    ip.modificationTriggerDeActivate(this);
		    break;
		    case INSTANCEDELETIONTYPE:
		    ip.deletionTriggerDeActivate(this);
		    break;
		}
		// No provider to notify
		return null;
	    }
	}
    }

    FilterActivation(CIMObjectPath filterOp, CIMInstance filterInstance,
    EventService eventService) {
	this.filterInstance = filterInstance;
	this.filterOp = new CIMObjectPath();
	this.filterOp.setKeys(filterOp.getKeys());
	this.filterOp.setObjectName(filterOp.getObjectName());
	this.filterOp.setNameSpace(filterOp.getNameSpace().toLowerCase());
	this.eventService = eventService;
    }

    // will throw an exception if one is found. 
    List getSubActivations() throws CIMException {
	if (exception != null) {
	    throw exception;
	}
	return result;
    }

    SelectExp getParsedExp() {
	return parsedExp;
    }

    CIMInstance getFilterInstance() {
	return filterInstance;
    }

    CIMObjectPath getFilterOp() {
	return filterOp;
    }

    CIMObjectPath getTargetNameSpace() {
	return targetNameSpace;
    }

    /* We're using the run method with the same signature as Runnable here
     * Maybe we'll run it in a separate thread later when there is a better
     * locking mechanism.
     *
     * Description: For the given input filter, we go through all the candidate
     * providers and activate eventing on them. This may result in the providers
     * being polled for results, or the providers letting the Event service
     * know when changes occur.
     *
     */
    void run() {
	String query = null;
	try {
	    query = 
	    (String)filterInstance.getProperty(EventService.QUERYPROP).
	    getValue().getValue();
	} catch (NullPointerException e) {
	    // There is no filter!
	    exception = new 
	    CIMException(CIMException.CIM_ERR_INVALID_PARAMETER);
	}
	int in = 0;
	try {
	    /* Dont check this for now.
	    String n = (String)filterInstance.
	    getProperty(EventService.QUERYLANGPROP).getValue().getValue();
	    in = n.intValue();
	    */
	} catch (NullPointerException e) {
	    // we assume no value means WQL.
            Debug.trace2("Caught NullPointerException in FilterActivation.run", e);

	    in = 0;
	}
	if (in != 0) {
	    exception = new CIMException(
		CIMException.CIM_ERR_NOT_SUPPORTED, new Integer(in));
	    return;
	}

	try {
	    determineClasses(query);
	} catch (CIMException e) {
	    exception = e;
	} catch (Exception e) {
	    exception = new CIMException(CIMException.CIM_ERR_FAILED,
	    e);
	}

	return;
    }

    private CIMIndicationProvider getProvider(CIMObjectPath classPath, 
    String eventType) throws CIMException {
	// Find the provider
	CIMObjectPath cp = classPath;
	String className = cp.getObjectName();
	if ((className == null) || (className.length() == 0)) {
	    cp = new CIMObjectPath();
	    cp.setNameSpace(classPath.getNameSpace());
	    cp.setObjectName(eventType);
	}
	CIMClass cc = eventService.ps.getClass(
	cp.getNameSpace(), cp.getObjectName());
	// Get the event provider

	CIMIndicationProvider localep = 
	CIMOMImpl.getProviderFactory().getCIMIndicationProvider(
	filterOp.getNameSpace(), cc);

	if (localep == null) {
	    // Huh? no provider - shouldnt happen
	    throw new CIMProviderException(
	    CIMProviderException.NO_INDICATION_PROVIDER, cp.getObjectName(),
	    cp.getNameSpace());
	}

	return localep;
    }


    // Get polling info for each subactivation. Some activations may result
    // in the provider being polled, some may not.
    void getPollInfo(final CIMInstance handler, final CIMInstance subscription) 
    throws CIMException {
	IndicationRequestCollator irc = 
	new IndicationRequestCollator(
	getSubActivations(),
	new IndicationRequestCollator.Callback() {
	    int count = 0;
	    public FilterActivation.EventProviderRequest 
	    doSubActivationOperation(SubActivation sa) 
	    throws CIMException {
		return sa.getPollInfo();
	    }

	    public Object doProviderOperation(String[] filters, 
	    CIMObjectPath[] classPaths, String[] eventTypes) 
	    throws CIMException {
		// reset the count for the next set of mustPoll results.
		count = 0;
		CIMIndicationProvider localep = 
		getProvider(classPaths[0], eventTypes[0]);
		return localep.mustPoll(filterInstance, handler, subscription,
		filters, classPaths, eventTypes);
	    }

	    public void processSingleResult(FilterActivation.SubActivation sa, 
	    Object result) throws CIMException {
		boolean[] pollResult = (boolean[])result;
		sa.processPollInfo(true, pollResult[count], null);
		count++;
	    }

	    public void processSingleException(
	    FilterActivation.SubActivation sa, CIMException ce) 
	    throws CIMException {
		sa.processPollInfo(true, false, ce);
	    }

	    public void processSingleNoResult(FilterActivation.SubActivation sa)
	    throws CIMException {
		sa.processPollInfo(false, false, null);
	    }
	});
	Object[] retVal = irc.processEventRequest();
	if (retVal[0] != null) {
	    // there was an exception
	    throw ((CIMException)retVal[0]);
	}
    }

    // Make sure that owner is authorized to use this particular activation.
    // This is all or nothing. If any of the subactivations refuse, the owner
    // is not authorized.
    void authorize(final String owner, final CIMInstance handler, 
    final CIMInstance subscription) throws CIMException {
	IndicationRequestCollator irc = 
	new IndicationRequestCollator(
	getSubActivations(),
	new IndicationRequestCollator.Callback() {
	    public FilterActivation.EventProviderRequest 
	    doSubActivationOperation(SubActivation sa) 
	    throws CIMException {
		return sa.authorize(owner);
	    }

	    public Object doProviderOperation(String[] filters, 
	    CIMObjectPath[] classPaths, String[] eventTypes) 
	    throws CIMException {
		CIMIndicationProvider localep = 
		getProvider(classPaths[0], eventTypes[0]);
		localep.authorizeFilter(filterInstance, handler, subscription,
		filters, classPaths, eventTypes);
		// No result is expected.
		return null;
	    }

	    public void processSingleResult(FilterActivation.SubActivation sa, 
	    Object result) throws CIMException {
		// No result is expected for authorization
	    }

	    public void processSingleException(
	    FilterActivation.SubActivation sa, CIMException ce) 
	    throws CIMException {
		sa.processAuthorizeFailure(true, ce);
	    }

	    public void processSingleNoResult(FilterActivation.SubActivation sa)
	    throws CIMException {
		// Say there was no result
		sa.processAuthorizeFailure(false, null);
	    }
	});
	Object[] retVal = irc.processEventRequest();
	if (retVal[0] != null) {
	    // there was an exception
	    throw ((CIMException)retVal[0]);
	}
    }

    void commonActivation(final CIMInstance handler, 
    final CIMInstance subscription, final boolean isSubscription) 
    throws CIMException {
	IndicationRequestCollator irc = 
	new IndicationRequestCollator(
	getSubActivations(),
	new IndicationRequestCollator.Callback() {
	    public FilterActivation.EventProviderRequest 
	    doSubActivationOperation(SubActivation sa) 
	    throws CIMException {
		if (isSubscription) {
		    return sa.activateSubscription();
		} else {
		    return sa.activate();
		}
	    }

	    public Object doProviderOperation(String[] filters, 
	    CIMObjectPath[] classPaths, String[] eventTypes) 
	    throws CIMException {
		CIMIndicationProvider localep = 
		getProvider(classPaths[0], eventTypes[0]);
		localep.activateFilter(filterInstance, handler, subscription,
		filters, classPaths, eventTypes);
		// No result is expected.
		return null;
	    }

	    public void processSingleResult(FilterActivation.SubActivation sa, 
	    Object result) throws CIMException {
		// No result is expected for provider activation
	    }

	    public void processSingleException(
	    FilterActivation.SubActivation sa, CIMException ce) 
	    throws CIMException {
		if (!isSubscription) {
		    sa.processActivateFailure(true, ce);
		}
	    }

	    public void processSingleNoResult(FilterActivation.SubActivation sa)
	    throws CIMException {
		// Say there was no result
		if (!isSubscription) {
		    sa.processActivateFailure(false, null);
		}
	    }
	});

	Object[] retVal = irc.processEventRequest();
	if (retVal[0] != null) {
	    // there was an exception. We need to deactivate those activations
	    // which were processed.
	    commonDeactivation((List)retVal[1], handler, subscription, 
	    isSubscription);
	    // ok throw the exception
	    throw ((CIMException)retVal[0]);
	}
    }

    void activate(CIMInstance handler, CIMInstance subscription) throws 
    CIMException {
	commonActivation(handler, subscription, false);
    }

    void activateSubscription(CIMInstance handler, CIMInstance subscription)
    throws CIMException {
	commonActivation(handler, subscription, true);
    }

    private void commonDeactivation(List l, final CIMInstance handler, 
    final CIMInstance subscription, final boolean isSubscription) throws 
    CIMException {
	if (l == null) {
	    l = getSubActivations();
	} 
	IndicationRequestCollator irc = 
	new IndicationRequestCollator(
	l, new IndicationRequestCollator.Callback() {
	    public FilterActivation.EventProviderRequest 
	    doSubActivationOperation(SubActivation sa) 
	    throws CIMException {
		try {
		    if (isSubscription) {
			return sa.deactivateSubscription();
		    } else {
			return sa.deactivate();
		    }
		} catch (Throwable th) {
		    // There isnt anything we can do about it, we should
		    // let deactivation continue;
		    Debug.trace2("Got deactivate exception", th);
		    return null;
		}
	    }

	    public Object doProviderOperation(String[] filters, 
	    CIMObjectPath[] classPaths, String[] eventTypes) 
	    throws CIMException {
		try {
		    CIMIndicationProvider localep = 
		    getProvider(classPaths[0], eventTypes[0]);
		    localep.deActivateFilter(filterInstance, handler, 
		    subscription, filters, classPaths, eventTypes);
		} catch (Throwable th) {
		    // There isnt anything we can do about it, we should
		    // let deactivation continue;
		    Debug.trace2("Got deactivate exception", th);
		}
		// No result is expected.
		return null;
	    }

	    public void processSingleResult(FilterActivation.SubActivation sa, 
	    Object result) throws CIMException {
		// No result is expected for provider deactivation
	    }

	    public void processSingleException(
	    FilterActivation.SubActivation sa, CIMException ce) 
	    throws CIMException {
		// Nothing can be done if there is an exception
	    }

	    public void processSingleNoResult(FilterActivation.SubActivation sa)
	    throws CIMException {
		// No result is expected anyway
	    }
	});

	Object[] retVal = irc.processEventRequest();
	if (retVal[0] != null) {
	    // there was an exception. Nothing we can do about it.
	    Debug.trace2("Got exception during deactivation", 
	    (CIMException)retVal[0]);
	}
    }

    void deactivateSubscription(CIMInstance handler, CIMInstance subscription)
    throws CIMException {
	commonDeactivation(null, handler, subscription, true);
    }

    void deactivate(CIMInstance handler, CIMInstance subscription) 
    throws CIMException {
	commonDeactivation(null, handler, subscription, false);
    }

    private List removeIsas(List l) {
	Iterator i = l.iterator();
	List output = new ArrayList();
	while (i.hasNext()) {
	    BinaryRelQueryExp br = (BinaryRelQueryExp)i.next();
	    int operator = br.getOperator();
	    if (operator == Query.ISA || operator == Query.NISA) {
		continue;
	    }
	    output.add(br);
	}
	return output;
    }
    private QueryExp toQueryExp(List l) {
	if (l.size() == 0) {
	    return null;
	}
	Iterator i = l.iterator();
	if (l.size() == 1) {
	    return (BinaryRelQueryExp)i.next();
	}
	QueryExp lexp = (BinaryRelQueryExp)i.next();
	QueryExp rexp = (BinaryRelQueryExp)i.next();
	AndQueryExp finalExp = new AndQueryExp(lexp, rexp);
	while (i.hasNext()) {
	    finalExp = new AndQueryExp(finalExp, (BinaryRelQueryExp)i.next());
	}
	return finalExp;
    }

    static int determineEventType(String eventTypeString) {
	if (eventTypeString.equalsIgnoreCase(INSTANCEMODIFICATION)) {
	    return INSTANCEMODIFICATIONTYPE;
	} else if (eventTypeString.equalsIgnoreCase(INSTANCEDELETION)) {
	    return INSTANCEDELETIONTYPE;
	} else if (eventTypeString.equalsIgnoreCase(INSTANCEADDITION)) {
	    return INSTANCEADDITIONTYPE;
	} else if (eventTypeString.equalsIgnoreCase(INSTANCEMETHODCALL)) {
	    return INSTANCEMETHODCALLTYPE;
	} else if (eventTypeString.equalsIgnoreCase(INSTANCEREAD)) {
	    return INSTANCEREADTYPE;
	}
	return -1;
    }

    private boolean isAbstract(CIMClass cc) {
	CIMQualifier qe = 
	cc.getQualifier("abstract");
	if (qe == null) {
	    return false;
	}
	CIMValue Tmp = qe.getValue();
	if ((Tmp == null) || !Tmp.equals(CIMValue.TRUE)) {
	    return false;
	}
	return true;
    }

    private boolean isClassIndication(String eventType) {
	String testType = eventType.toLowerCase();
	if (testType.equals(CLASSDELETION)) {
	    return true;
	}
	if (testType.equals(CLASSCREATION)) {
	    return true;
	}
	if (testType.equals(CLASSMODIFICATION)) {
	    return true;
	}
	return false;
    }

    // This method and the method it calls could be moved to a query optimizer
    // class. That way we can have plugabble query optimization
    private void determineClasses(String query) throws Exception {
	parsedExp = new SelectExp(query);
	String eventTypeString = ((NonJoinExp)parsedExp.getFromClause()).
	getAttribute().getAttrClassName().toLowerCase();
	targetNameSpace = new CIMObjectPath();
	String targetNS = null;
	try {
	    targetNS = (String)filterInstance.getProperty(
	    EventService.TARGETNSPROP).getValue().getValue();
	} catch (NullPointerException e) {
	    // No value set
            Debug.trace2("Caught NullPointerException in FilterActivation.determineClasses", e);
	}

	if (targetNS == null) {
	    // Ok we are operating in the same namespace as the filter
	    targetNameSpace.setNameSpace(filterOp.getNameSpace());
	} else {
	    targetNameSpace.setNameSpace(targetNS.toLowerCase());
	}

	CIMClass indicationClass = eventService.ps.getClass(
	    filterOp.getNameSpace(), eventTypeString);
	if (indicationClass == null) {
	throw new CIMClassException(CIMException.CIM_ERR_NOT_FOUND,
					eventTypeString);
	}

	CIMQualifier qe = 
	indicationClass.getQualifier(ClassChecker.INDICATIONQUALIFIER);
	if (qe == null) {
	    throw new CIMClassException(CIMException.CIM_ERR_INVALID_CLASS,
					eventTypeString);
	}
	CIMValue Tmp = qe.getValue();
	if ((Tmp == null) || !Tmp.equals(CIMValue.TRUE)) {
	    throw new CIMClassException(CIMException.CIM_ERR_INVALID_CLASS,
					eventTypeString);
	}

	CIMObjectPath eventTypePath = new CIMObjectPath();
	eventTypePath.setNameSpace(filterOp.getNameSpace());
	eventTypePath.setObjectName(eventTypeString);
	Vector tempList = 
	eventService.ps.enumerateClasses(eventTypePath, true, false);
	List indicationList = new ArrayList();
	List intrinsicEventList = new ArrayList();

	// We need to collect all the concrete indication classes
	if (!isAbstract(indicationClass)) {
	    if (determineEventType(indicationClass.getName().toLowerCase())
	    != -1) {
		intrinsicEventList.add(indicationClass);
	    } else {
		indicationList.add(indicationClass);
	    }
	}

	Enumeration e = tempList.elements();
	while (e.hasMoreElements()) {
	    CIMClass tic = (CIMClass)e.nextElement();
	    if (!isAbstract(tic)) {
		if (determineEventType(tic.getName().toLowerCase()) != -1) {
		    intrinsicEventList.add(tic);
		} else {
		    indicationList.add(tic);
		}
	    }
	}

	// Must do: to verify if the properties specified in the clause are
	// valid
	QueryExp whereClause = parsedExp.getWhereClause(); 

	List cwc = null;
	if (whereClause != null) {
	    // In the future we should have an expression optimizer here.
	    cwc = whereClause.canonizeDOC();
	} else {
	    // empty expression
	    cwc = new ArrayList();
	    cwc.add(new ArrayList());
	}
	result = new ArrayList();
	// Must handle the process indications here
	Iterator ilIterator = indicationList.iterator();
	while (ilIterator.hasNext()) {
	    CIMClass tic = (CIMClass)ilIterator.next();
	    // These checks should be in a factory which creates the
	    // apropriate subactivation
	    SubActivation subAct;
	    if (isClassIndication(tic.getName())) {
		subAct = new CISubActivation(tic.getName().toLowerCase(), 
		whereClause, "");
	    } else {
		subAct = new PISubActivation(tic.getName().toLowerCase(), 
		whereClause, "");
	    }
	    result.add(subAct);
	}

	// Each sublist will result in a different set of classes being
	// 'evented' on
	Iterator i = cwc.iterator();
	canonizedExp = new ArrayList();


	if (intrinsicEventList.size() == 0) {
	    // No life cycle indications to handle
	    return;
	}

	while (i.hasNext()) {
	    List subExpression = (List)i.next();
	    Set s = determineSublistClasses(subExpression);

	    Iterator is = s.iterator();
	    subExpression = removeIsas(subExpression);
	    canonizedExp.add(subExpression);
	    QueryExp subQueryExp = toQueryExp(subExpression);
	    while (is.hasNext()) {
		String className = (String)is.next();
		Iterator tempIterator = intrinsicEventList.iterator();
		while (tempIterator.hasNext()) {
		    // Add a new subactivation for each life cycle indication 
		    // type
		    CIMClass tic = (CIMClass)tempIterator.next();
		    SubActivation subAct = new ILCSubActivation(
		    tic.getName().toLowerCase(), subQueryExp, 
		    className.toLowerCase());
		    result.add(subAct);
		}
	    }
	}
    }

    private Set determineSublistClasses(List l) throws Exception {
	Iterator i = l.iterator();
	// This set will contain the set of all classes that need to be
	// evented on
	Set s = new TreeSet();
	List intersection = new ArrayList();
	List difference = new ArrayList();
	while (i.hasNext()) {
	    BinaryRelQueryExp br = (BinaryRelQueryExp)i.next();
	    // Need to do semantic checking - for e.g. the lhs should be
	    // of type Object
	    switch (br.getOperator()) {
		case Query.ISA: AttributeExp aexp = (AttributeExp)br.
		getRightValue();
			String className = aexp.getAttributeName();
			intersection.add(className);
			break;
		case Query.NISA: aexp = (AttributeExp)br.
		getRightValue();
			className = aexp.getAttributeName();
			difference.add(className);
	    }
	}
	// We've collected the ISAs in intersection and NISAs in difference
	CIMObjectPath path = new CIMObjectPath();
	path.setNameSpace(targetNameSpace.getNameSpace());
	if (intersection.size() == 0) {
	    // There is no ISA. That means this expression deals with all
	    // classes in the namespace (excluding, the difference classes)
	    path.setObjectName("");
	    Enumeration e = eventService.ps.enumerateClasses(path, true).
								elements();
	    while (e.hasMoreElements()) {
		s.add(((CIMObjectPath)e.nextElement()).getObjectName());
	    }

	    if (s.size() == 0) {
		// There is nothing to be done, no classes to process
		return s;
	    }
	} else {
	    Iterator isaList = intersection.iterator();
	    // Get the classes for the first isa and then intersect
	    String className = (String)isaList.next();
	    path.setObjectName(className);
	    Enumeration e = eventService.ps.enumerateClasses(path, true).
								elements();
	    // add the class itself
	    s.add(className);
	    while (e.hasMoreElements()) {
		s.add(((CIMObjectPath)e.nextElement()).getObjectName());
	    }

	    while (s.size() > 0 && isaList.hasNext()) {
		String intersectCN = (String)isaList.next();
		path.setObjectName(intersectCN);
		Vector v = eventService.ps.enumerateClasses(path, true);
		// Add the class itself
		v.insertElementAt(new CIMObjectPath(intersectCN), 0);
		e = v.elements();
		// Go through all the classes and remove those that 
		// do not exist in both.
		while (e.hasMoreElements()) {
		    String intersectClass = 
		    ((CIMObjectPath)e.nextElement()).getObjectName();
		    if (!s.contains(intersectClass)) {
			s.remove(intersectClass);
		    }
		}
	    }
	    if (s.size() == 0) {
		return s;
	    }
	}
	// Now handle the difference list
	Iterator nisaList = difference.iterator();
	while (nisaList.hasNext() && s.size() != 0) {
	    // Get the classes for the nisa and then take difference
	    String diffClassName = (String)nisaList.next();
	    path.setObjectName(diffClassName);
	    Vector v = eventService.ps.enumerateClasses(path, true);
	    v.insertElementAt(new CIMObjectPath(diffClassName), 0);
	    Enumeration e = v.elements();
	    while (e.hasMoreElements()) {
		s.remove(((CIMObjectPath)e.nextElement()).getObjectName());
	    }
	}
	return s;
    }
}
