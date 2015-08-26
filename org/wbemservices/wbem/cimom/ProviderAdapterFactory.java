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

import java.util.ArrayList;
import java.util.Map;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import javax.wbem.cim.*;
import javax.wbem.client.Debug;
import javax.wbem.client.CIMProviderException;
import javax.wbem.client.ProviderCIMOMHandle;
import javax.wbem.provider.*;

import org.wbemservices.wbem.cimom.util.DynClassLoader;

import org.wbemservices.wbem.cimom.adapter.provider.ProviderProtocolAdapterIF;
import org.wbemservices.wbem.cimom.adapter.provider.ProviderAdapterIF;

/**
 * This class maintains a list of all the plugable protocol providers, as well
 * as all of the providers each has already loaded.
 *
 * @author  Jim M.
 */
public class ProviderAdapterFactory implements
InternalProviderAdapter.InternalServiceProvider {

    /**
     * Holds a list of adapters that have been registered
     * <I>This is a synchronized Map</I>
     */
    private Map mAdapters = null;

    /**
     * Holds a list of providers that have been loaded by adpaters
     * <I>This is a synchronized Map</I>
     */    
    private Map mProviders = null;
    
    /**
     * If any of the provider adapters are providers themselves, we want
     * the internal provider adapter to know.
     */
    private List serviceProviders = new ArrayList();

    /** 
     * ProviderCIMOMHandle instance 
     */
    private ProviderCIMOMHandle mCimom = null;

    /**
     * If this variable is 'true' the factory will simply return 'null' from
     * all the getXXXProvider functions. This is set via the
     * 'passAllProviderCheck' system property. Its primary use is by the
     * mof compiler and mofreg tools when they run the CIMOM stand-alone and
     * only need access to the repository
     */
    private boolean mShouldReturnNull = false;

    // CIM class that represents the provider protocol adapters
    private final static String PPACIMCLASS = 
    "WBEMServices_ObjectManagerProviderProtocolAdapter";
    // Association class that links protocol adapters to the CIMOM.
    private final static String PPA_ASSOC_CLASS = 
    "WBEMServices_ProviderProtocolAdapterForManager";
    // name of the start service method.
    private final static String STARTSERVICE = "StartService";
    // name of the stop service method.
    private final static String STOPSERVICE = "StopService";
    // name of the protocol type property
    private final static String PROTOCOLTYPE = "Name";
    // name of the implementation class property
    private final static String IMPLCLASS = "ImplClass";
    // name of the classpath property
    private final static String CLASSPATH = "classPath";
    // The internal provider
    AdapterProvider adapterProvider;
    AdapterAssocProvider assocProvider;

    // InternalServiceProvider interface method. See InternalProviderAdapter.
    public String[] getProviderNames() {
	// This class will be the provider for PPACIMCLASS
	serviceProviders.add(PPACIMCLASS);
	serviceProviders.add(PPA_ASSOC_CLASS);
	// Add the individual protocol adapters too.
	String[] tArray = new String[serviceProviders.size()];
	return (String[])serviceProviders.toArray(tArray);
    }

    // InternalServiceProvider interface method. See InternalProviderAdapter.
    public CIMProvider getProvider(String pName) throws CIMException {
	if (pName.equals(PPACIMCLASS)) {
	    return adapterProvider;
	} else if (pName.equals(PPA_ASSOC_CLASS)) {
	    return assocProvider;
	} else {
	    // This is a request for the protocol adapter's provider
	    return (CIMProvider)mAdapters.get(pName);
	}
    }

    /**
     * Creates new ProviderAdapterFactory
     * @param pch a ProviderCIMOMHandle instance
     * @Throws CIMException if an error occurs
     */
    public ProviderAdapterFactory(ProviderCIMOMHandle pch)
	throws CIMException {
	mCimom = pch;
	String passAllProvCheck =
		System.getProperty("passAllProviderCheck", "false");
	if ("true".equalsIgnoreCase(passAllProvCheck)) {
		mShouldReturnNull = true;
		Debug.trace3("'passAllProviderCheck' was true");
	} else {
		mShouldReturnNull = false;
		mAdapters = Collections.synchronizedMap(new HashMap(5));
		mProviders = Collections.synchronizedMap(new HashMap(100));
		adapterProvider = new AdapterProvider();
		assocProvider = new AdapterAssocProvider();
		Debug.trace3("Provider factory created and ready");
	}
    }

    /**
     * Registers a protocol provider with this factory
     * 
     * @param pAdapterType The key this provider protocol supports, indicated
     *                in the providers mof file
     * @param pAdapter an instance of the protocol provider
     * @param isAuto Is it a provider that should be automatically started
     */
    void RegisterProtocolProvider(String pAdapterType,
				  ProviderProtocolAdapterIF pAdapter,
				  boolean isAuto) {
	// Make sure if there is already a adaptertype registered
	// that we shut it down
	// XXX or should we ignore this register request??
	UnRegisterProtocolProvider(pAdapterType);
        pAdapter.initialize(mCimom);
	if (isAuto) {
	  pAdapter.start();
	}
        if (pAdapter.isValid()) {
	  Debug.trace3("adding an adapter for protocol: " + pAdapterType);
	  mAdapters.put(pAdapterType, pAdapter);
	  if (pAdapter instanceof CIMProvider) {
	    // ok this adapter itself is a provider which must
	    // be registered with the internal provider
	    Debug.trace3("The " + pAdapterType + " is a CIMProvider");
	    serviceProviders.add(pAdapterType);
	  }
	} else {
	  Debug.trace3("The " + pAdapterType + " says it is not valid");
	}
    }

    /**
     * Unloads the adpater and prevents future use
     * @param pAdapterType The type of protocol to stop
     */
    void UnRegisterProtocolProvider(String pAdapterType) {
        ProviderProtocolAdapterIF adapter = (ProviderProtocolAdapterIF)
		mAdapters.get(pAdapterType);
        if (adapter != null) {
	    Debug.trace3("Removing protocol adapter:" + pAdapterType);
            mAdapters.remove(pAdapterType);
            adapter.stop();
        }
    }

    /**
     * gets an instance provider for a class
     * @param pNamespace the namespace being used
     * @param pCIMClass The CIMClass for the provider
     * @return A CIMInstanceProvider interface, or null if the class
     * has no instance provider
     */
    public CIMInstanceProvider getInstanceProvider(String pNamespace,
			CIMClass pCIMClass) throws CIMException {
	/* Should we just ignore this request and return null? */
	if (mShouldReturnNull == true) {
		return null;
	}
	CIMInstanceProvider realProv = null;
	CIMQualifier qual = pCIMClass.getQualifier("provider");
	if (qual == null) {
	    Debug.trace3("No 'provider' qualifier found for class: "
			+ pCIMClass);
	    return null;
	}

	String provider = getProviderString(qual);

	ProviderAdapterIF prov = null;
	try {
	    prov = getProviderAdapter(pNamespace, provider, pCIMClass);
	} catch (ClassNotFoundException cnfe) {
	    Debug.trace3("getProviderAdapter threw ClassNotFoundException",
			cnfe);
	    throw new CIMProviderException(
			CIMProviderException.NO_INSTANCE_PROVIDER,
			pCIMClass.getName(),
			provider);
	}

	if (prov.isInstanceProvider()) {
	    try {
		realProv = (CIMInstanceProvider)prov.getProvider();
		if (realProv == null) {
		    Debug.trace3("getProvider returned NULL");
		    throw new CIMProviderException(
				CIMProviderException.NO_INSTANCE_PROVIDER,
				pCIMClass.getName(),
				provider);
		}
	    }
	    catch (ClassCastException cce) {
		Debug.trace3("Class Cast Exception", cce);
		throw new CIMProviderException(
				CIMProviderException.NOT_INSTANCE_PROVIDER,
				pCIMClass.getName(),
				provider);
	    }
	} else {
		Debug.trace3("isInstanceProvider returned false");
		throw new CIMProviderException(
			CIMProviderException.NOT_INSTANCE_PROVIDER,
			pCIMClass.getName(),
			provider);
	}

	return realProv;
    }

    /**
     * gets an associator provider for a class
     * @param pNamespace the namespace being used
     * @param pCIMClass The CIMClass for the provider
     * @return A CIMAssociatorProvider interface, or null if the class
     * has no instance provider
     */
    public CIMAssociatorProvider getAssociatorProvider(String pNamespace,
				CIMClass pCIMClass) throws CIMException {
	/* Should we just ignore this request and return null? */
	if (mShouldReturnNull == true) {
		return null;
	}
	CIMAssociatorProvider realProv = null;
	CIMQualifier qual = pCIMClass.getQualifier("provider");
	if (qual == null) {
	    Debug.trace3("No 'provider' qualifier found for class: "
			+ pCIMClass);
	    return null;
	}

	String provider = getProviderString(qual);
	ProviderAdapterIF prov = null;
	try {
	    prov = getProviderAdapter(pNamespace, provider, pCIMClass);
	} catch (ClassNotFoundException cnfe) {
	    Debug.trace3("getProviderAdapter threw ClassNotFoundException");
	    throw new CIMProviderException(
			CIMProviderException.NO_ASSOCIATOR_PROVIDER,
			pCIMClass.getName(),
			provider);
	}

	if (prov.isAssociatorProvider()) {
	    try {
		realProv = (CIMAssociatorProvider)prov.getProvider();
		if (realProv == null) {
		    Debug.trace3("getProvider returned NULL");
		    throw new CIMProviderException(
				CIMProviderException.NO_ASSOCIATOR_PROVIDER,
				pCIMClass.getName(),
				provider);
		}
	    }
	    catch (ClassCastException cce) {
		Debug.trace3("Class Cast Exception", cce);
		throw new CIMProviderException(
				CIMProviderException.NOT_ASSOCIATOR_PROVIDER,
				pCIMClass.getName(),
				provider);
	    }
	} else {
		Debug.trace3("isAssociatorProvider returned false");
		throw new CIMProviderException(
			CIMProviderException.NOT_ASSOCIATOR_PROVIDER,
			pCIMClass.getName(),
			provider);
	}

	return realProv;
    }

    /**
     * gets an Authorizable provider for a class
     * @param pNamespace the namespace being used
     * @param pCIMClass The CIMClass for the provider
     * @return An Authorizable interface, or null if the class
     * has no instance provider
     */
    public Authorizable getAuthorizableProvider(String pNamespace,
				CIMClass pCIMClass) throws CIMException {
	/* Should we just ignore this request and return null? */
	if (mShouldReturnNull == true) {
		return null;
	}
	Authorizable realProv = null;
	CIMQualifier qual = pCIMClass.getQualifier("provider");
	if (qual == null) {
	    Debug.trace3("No 'provider' qualifier found for class: "
			+ pCIMClass);
	    return null;
	}

	String provider = getProviderString(qual);

	ProviderAdapterIF prov = null;
	try {
	    prov = getProviderAdapter(pNamespace, provider, pCIMClass);
	} catch (ClassNotFoundException cnfe) {
	    Debug.trace3("getProviderAdapter threw ClassNotFoundException");
	    throw new CIMProviderException(
			CIMProviderException.NO_AUTHORIZABLE_PROVIDER,
			pCIMClass.getName(),
			provider);
	}

	if (prov.isAuthorizable()) {
	    try {
		realProv = (Authorizable)prov.getProvider();
		if (realProv == null) {
		    Debug.trace3("getProvider returned NULL");
		    throw new CIMProviderException(
				CIMProviderException.NO_AUTHORIZABLE_PROVIDER,
				pCIMClass.getName(),
				provider);
		}
	    }
	    catch (ClassCastException cce) {
		Debug.trace3("Class Cast Exception", cce);
		throw new CIMProviderException(
				CIMProviderException.NOT_AUTHORIZABLE_PROVIDER,
				pCIMClass.getName(),
				provider);
	    }
	} else {
		Debug.trace3("isAuthorizable returned false");
		throw new CIMProviderException(
			CIMProviderException.NOT_AUTHORIZABLE_PROVIDER,
			pCIMClass.getName(),
			provider);
	}

	return realProv;
    }

    /**
     * gets an Authorizable provider for a method (if available)
     * @param pNamespace the namespace being used
     * @param pMethod The CIMMethod for the provider
     * @return An Authorizable interface, or null if the class
     * has no instance provider
     */
    public Authorizable getAuthorizableProvider(String pNamespace,
				CIMMethod pMethod) throws CIMException {
	/* Should we just ignore this request and return null? */
	if (mShouldReturnNull == true) {
		return null;
	}
	Authorizable realProv = null;
	CIMQualifier qual = pMethod.getQualifier("provider");
	if (qual == null) {
	    Debug.trace3("No 'provider' qualifier found for Method: "
			+ pMethod);
	    return null;
	}

	String provider = getProviderString(qual);

	ProviderAdapterIF prov = null;
	try {
	    prov = getProviderAdapter(pNamespace, provider, null);
	} catch (ClassNotFoundException cnfe) {
	    Debug.trace3("getProviderAdapter threw ClassNotFoundException", cnfe);
	    throw new CIMProviderException(
			CIMProviderException.NO_AUTHORIZABLE_PROVIDER,
			pMethod.getName(),
			provider);
	}

	if (prov.isAuthorizable()) {
	    try {
		realProv = (Authorizable)prov.getProvider();
		if (realProv == null) {
		    Debug.trace3("getProvider returned NULL");
		    throw new CIMProviderException(
				CIMProviderException.NO_AUTHORIZABLE_PROVIDER,
				pMethod.getName(),
				provider);
		}
	    }
	    catch (ClassCastException cce) {
		Debug.trace3("Class Cast Exception", cce);
		throw new CIMProviderException(
				CIMProviderException.NOT_AUTHORIZABLE_PROVIDER,
				pMethod.getName(),
				provider);
	    }
	} else {
		Debug.trace3("isAuthorizable returned false");
		throw new CIMProviderException(
			CIMProviderException.NOT_AUTHORIZABLE_PROVIDER,
			pMethod.getName(),
			provider);
	}

	return realProv;
    }

    /**
     * Gets an Authorizable provider for a property (if available)
     * 
     * @param pNamespace the namespace being used
     * @param pProp The CIMProperty for the provider
     * @return An Authorizable interface, or null if the class has no instance
     *         provider
     */
    public Authorizable getAuthorizableProvider(String pNamespace,
				CIMProperty pProp) throws CIMException {
	/* Should we just ignore this request and return null? */
	if (mShouldReturnNull == true) {
		return null;
	}
	Authorizable realProv = null;
	// get the qualifier for this property
	CIMQualifier qual = pProp.getQualifier("provider");
	if (qual == null) {
	    Debug.trace3("No 'provider' qualifier found for Property: "
			+ pProp);
	    return null;
	}

	String provider = getProviderString(qual);

	ProviderAdapterIF prov = null;
	try {
	    prov = getProviderAdapter(pNamespace, provider, null);
	} catch (ClassNotFoundException cnfe) {
	    Debug.trace3("getProviderAdapter threw ClassNotFoundException", cnfe);
	    throw new CIMProviderException(
			CIMProviderException.NO_AUTHORIZABLE_PROVIDER,
			pProp.getName(),
			provider);
	}

	if (prov.isAuthorizable()) {
	    try {
		realProv = (Authorizable)prov.getProvider();
		if (realProv == null) {
		    Debug.trace3("getProvider returned NULL");
		    throw new CIMProviderException(
				CIMProviderException.NO_AUTHORIZABLE_PROVIDER,
				pProp.getName(),
				provider);
		}
	    }
	    catch (ClassCastException cce) {
		Debug.trace3("Class Cast Exception", cce);
		throw new CIMProviderException(
				CIMProviderException.NOT_AUTHORIZABLE_PROVIDER,
				pProp.getName(),
				provider);
	    }
	} else {
		Debug.trace3("isAuthorizable returned false");
		throw new CIMProviderException(
			CIMProviderException.NOT_AUTHORIZABLE_PROVIDER,
			pProp.getName(),
			provider);
	}

	return realProv;
    }

    /**
     * gets a MethodProvider provider for a class
     * @param pNamespace the namespace being used
     * @param pCIMClass The CIMClass for the provider
     * @return An InstanceProvider interface, or null if the class
     * has no instance provider
     */
    public CIMMethodProvider getMethodProvider(String pNamespace,
				CIMClass pCIMClass) throws CIMException {
	/* Should we just ignore this request and return null? */
	if (mShouldReturnNull == true) {
		return null;
	}
	CIMMethodProvider realProv = null;
	CIMQualifier qual = pCIMClass.getQualifier("provider");
	if (qual == null) {
	    Debug.trace3("No 'provider' qualifier found for class: "
			+ pCIMClass);
	    return null;
	}

	String provider = getProviderString(qual);

	ProviderAdapterIF prov = null;
	try {
	    prov = getProviderAdapter(pNamespace, provider, pCIMClass);
	} catch (ClassNotFoundException cnfe) {
	    Debug.trace3("getProviderAdapter threw ClassNotFoundException", cnfe);
	    throw new CIMProviderException(
			CIMProviderException.NO_METHOD_PROVIDER,
			pCIMClass.getName(),
			provider);
	}

	if (prov.isMethodProvider()) {
	    try {
		realProv = (CIMMethodProvider)prov.getProvider();
		if (realProv == null) {
		    Debug.trace3("getProvider returned NULL");
		    throw new CIMProviderException(
				CIMProviderException.NO_METHOD_PROVIDER,
				pCIMClass.getName(),
				provider);
		}
	    }
	    catch (ClassCastException cce) {
		Debug.trace3("Class Cast Exception", cce);
		throw new CIMProviderException(
				CIMProviderException.NOT_METHOD_PROVIDER,
				pCIMClass.getName(),
				provider);
	    }
	} else {
		Debug.trace3("isMethodProvider returned false");
		throw new CIMProviderException(
			CIMProviderException.NOT_METHOD_PROVIDER,
			pCIMClass.getName(),
			provider);
	}

	return realProv;
    }

    /**
     * gets a MethodProvider provider for a specific method
     * @param pNamespace the namespace being used
     * @param pMethod The method to get the provider for
     * @param pCIMClass the CIM Class the method belongs too
     * @return A MethodProvider interface, or null if the class
     * has no instance provider
     */
    public CIMMethodProvider getMethodProvider(String pNamespace,
		CIMMethod pMethod, CIMClass pCIMClass) throws CIMException {
	/* Should we just ignore this request and return null? */
	if (mShouldReturnNull == true) {
		return null;
	}
	CIMMethodProvider realProv = null;
	CIMQualifier qual = pMethod.getQualifier("provider");
	if (qual == null) {
	    Debug.trace3("No 'provider' qualifier found for Method: "
			+ pMethod);
	    return null;
	}

	String provider = getProviderString(qual);

	ProviderAdapterIF prov = null;
	try {
	    prov = getProviderAdapter(pNamespace, provider, pCIMClass);
	} catch (ClassNotFoundException cnfe) {
	    Debug.trace3("getProviderAdapter threw ClassNotFoundException", cnfe);
	    throw new CIMProviderException(
			CIMProviderException.NO_METHOD_PROVIDER,
			pMethod.getName(),
			provider);
	}

	if (prov.isMethodProvider()) {
	    try {
		realProv = (CIMMethodProvider)prov.getProvider();
		if (realProv == null) {
		    Debug.trace3("getProvider returned NULL");
		    throw new CIMProviderException(
				CIMProviderException.NO_METHOD_PROVIDER,
				pMethod.getName(),
				provider);
		}
	    }
	    catch (ClassCastException cce) {
		Debug.trace3("Class Cast Exception", cce);
		throw new CIMProviderException(
				CIMProviderException.NOT_METHOD_PROVIDER,
				pMethod.getName(),
				provider);
	    }
	} else {
		Debug.trace3("isMethodProvider returned false");
		throw new CIMProviderException(
			CIMProviderException.NOT_METHOD_PROVIDER,
			pMethod.getName(),
			provider);
	}

	return realProv;
    }

    /**
     * gets an EventProvider provider for a class
     * @param pNamespace the namespace being used
     * @param pCIMClass The CIMClass for the provider
     * @return An EventProvider interface, or null if the class
     * has no instance provider
     */
    public EventProvider getEventProvider(String pNamespace,
				CIMClass pCIMClass) throws CIMException {
	/* Should we just ignore this request and return null? */
	if (mShouldReturnNull == true) {
		return null;
	}
	EventProvider realProv = null;
	CIMQualifier qual = pCIMClass.getQualifier("provider");
	if (qual == null) {
	    Debug.trace3("No 'provider' qualifier found for class: "
			+ pCIMClass);
	    return null;
	}

	String provider = getProviderString(qual);

	ProviderAdapterIF prov = null;
	try {
	    prov = getProviderAdapter(pNamespace, provider, pCIMClass);
	} catch (ClassNotFoundException cnfe) {
	    Debug.trace3("getProviderAdapter threw ClassNotFoundException", cnfe);
	    throw new CIMProviderException(
			CIMProviderException.NO_EVENT_PROVIDER,
			pCIMClass.getName(),
			provider);
	}

	if (prov.isEventProvider()) {
	    try {
		realProv = (EventProvider)prov.getProvider();
		if (realProv == null) {
		    Debug.trace3("getProvider returned NULL");
		    throw new CIMProviderException(
				CIMProviderException.NO_EVENT_PROVIDER,
				pCIMClass.getName(),
				provider);
		}
	    }
	    catch (ClassCastException cce) {
		Debug.trace3("Class Cast Exception", cce);
		throw new CIMProviderException(
				CIMProviderException.NOT_EVENT_PROVIDER,
				pCIMClass.getName(),
				provider);
	    }
	} else {
		Debug.trace3("isEventProvider returned false");
		throw new CIMProviderException(
			CIMProviderException.NOT_EVENT_PROVIDER,
			pCIMClass.getName(),
			provider);
	}

	return realProv;
    }

    /**
     * gets an EventProvider provider for a class
     * @param pNamespace the namespace being used
     * @param pCIMClass The CIMClass for the provider
     * @return An EventProvider interface, or null if the class
     * has no instance provider
     */
    public CIMIndicationProvider getCIMIndicationProvider(String pNamespace,
				CIMClass pCIMClass) throws CIMException {
	/* Should we just ignore this request and return null? */
	if (mShouldReturnNull == true) {
		return null;
	}
	CIMIndicationProvider realProv = null;
	CIMQualifier qual = pCIMClass.getQualifier("provider");
	if (qual == null) {
	    Debug.trace3("No 'provider' qualifier found for class: "
			+ pCIMClass);
	    return null;
	}

	String provider = getProviderString(qual);

	ProviderAdapterIF prov = null;
	try {
	    prov = getProviderAdapter(pNamespace, provider, pCIMClass);
	} catch (ClassNotFoundException cnfe) {
	    Debug.trace3("getProviderAdapter threw ClassNotFoundException", cnfe);
	    throw new CIMProviderException(
			CIMProviderException.NO_INDICATION_PROVIDER,
			pCIMClass.getName(),
			provider);
	}

	if (prov.isCIMIndicationProvider()) {
	    try {
		realProv = (CIMIndicationProvider)prov.getProvider();
		if (realProv == null) {
		    Debug.trace3("getProvider returned NULL");
		    throw new CIMProviderException(
				CIMProviderException.NO_INDICATION_PROVIDER,
				pCIMClass.getName(),
				provider);
		}
	    }
	    catch (ClassCastException cce) {
		Debug.trace3("Class Cast Exception", cce);
		throw new CIMProviderException(
				CIMProviderException.NOT_INDICATION_PROVIDER,
				pCIMClass.getName(),
				provider);
	    }
	} else {
		Debug.trace3("isCIMIndicationProvider returned false");
		throw new CIMProviderException(
			CIMProviderException.NOT_INDICATION_PROVIDER,
			pCIMClass.getName(),
			provider);
	}

	return realProv;
    }

    /**
     * Gets a provider.
     * @param pNamespace the namespace being used
     * @param pQualString The Qualifier string for the provider
     * @return an instance of a class which implements CIMAdapterProviderIF
     * @throws CIMClassException if no provider can be found
     */
    private ProviderAdapterIF getProviderAdapter(String pNamespace,
	String pQualString, CIMClass pCimClass)
	throws CIMClassException, CIMException, ClassNotFoundException {
        // The provider name from the qualifier
        String providerName = getProviderName(pQualString);
	// The key from an adapter
	Debug.trace3("Need to get provider named "+providerName);
	Object providerKey = mProviders.get(providerName);
	// the adpater to use
	ProviderProtocolAdapterIF adapter = null;
	try {
	    // Determine which protocol is used for this provider
	    String providerProtocol = getProtocol(pQualString);
	    Debug.trace3("Need to get provider protocol adapter "+
	    providerProtocol);
	    // get the adapter for this protocol
	    adapter = (ProviderProtocolAdapterIF)mAdapters.get(
			providerProtocol);
	    // No adapter for the specified type
            if (adapter == null) {
		// throw an exception here?
		Debug.trace3("Did not find provider protocol adapter for: " +
		providerProtocol);
		throw new CIMProviderException(
			CIMProviderException.UNKNOWN_PROVIDER_ADAPTER,
			providerProtocol);
	    }
	} catch (Exception e) {
            Debug.trace2("Caught Exception at ProviderAdapterIF.getProviderAdapter", e);
	    throw new CIMClassException(e.toString());
	}
        // is this provider already loaded?
        if (providerKey == null) {
	    Debug.trace3("Loading provider from adapter");
	    // nope, ask adapter to load it
	    // Ask the adapter to load the provider
	    providerKey = adapter.loadProvider(pNamespace, providerName,
	                getProviderParams(pQualString),pCimClass);
            // put it in the list of loaded providers
            mProviders.put(providerName, providerKey);
        }
        // ask the adapter to give give us the CIMAdpaterProviderIF
	Debug.trace3("Getting provider adapter interface");
	ProviderAdapterIF ret = adapter.getProviderAdapter(providerKey);
	Debug.trace3("Got adapter interface " + ret);
	return ret;
    }

    // helper functions
    /**
     * This function looks at the provider qualifier and determines the
     * protocol used by this provider
     * @param providerQual The 'provider' qualifier
     */
    private String getProtocol(String providerQual) {
	int index = providerQual.indexOf(':');
	Debug.trace3("getting protocol");
	if (index < 0) {
	    Debug.trace3("No provider qualifier, defaulting to 'sunjava'");
	    // this is a old org.wbemservices.wbem Java provider
	    return "sunjava";
	} else {
	    // this is a qualifier of form
	    // providerType:provider:params.
	    String protocol = providerQual.substring(0, index);
	    Debug.trace3("Protocol: " + protocol);
	    return protocol;
	}
    }

    /**
     * This function looks at the provider qualifier and determines the
     * provider that will be used
     * @param providerQual The 'provider' qualifier
     */
    private String getProviderName(String providerQual) throws CIMException {
	int index = providerQual.indexOf(':');
	int length = providerQual.length();
	if (index < 0) {
	    // this is a java provider, return the class name
	    return providerQual;
	} else {
	    if (index == length - 1) {
		// Nothing after the colon
	        Debug.trace3("Invalid provider qualifier: " + providerQual);
		throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER,
		providerQual);
	    }
	    // this is a qualifier of form providerType:provider:params. 
	    index++; // Get rid of the colon
	    int index2 = providerQual.indexOf(':',index);
	    if (index2 < 0) {
		// No params, the rest of the string is the class name
		index2 = length;
	    }
	    String providerString = providerQual.substring(index, index2);
	    Debug.trace3("Provider: " + providerString);
	    return providerString;
	}
    }

    /**
     * This function gets the params specified in the qualifier
     * @param providerQual The 'provider' qualifier
     * @return An array of strings
     */
    private String[] getProviderParams(String providerQual) {
	int index = providerQual.indexOf(':');
	int length = providerQual.length();
        // if there is no ':' or index == length there are no params
	if (index < 0 || index == length - 1) {
            // return empty param
            return new String[0];
        }
        // this is a qualifier of form providerType:provider:params. 
	index++; // Get rid of the colon
        // find the next ':', so we have provider:params
	int index2 = providerQual.indexOf(':',index);
	if (index2 < 0) {
            // no more ':', no params - return an empty array
            return new String[0];
        }
        // we have params!
        index2++; // get rid of the colon
        index = 0; // reset to zero
        // get the part of the string that only has the params
        String params = providerQual.substring(index2);
        // create a string tokenizer based on commas
        StringTokenizer tok = new StringTokenizer(params, ",");
        String paramsArray[] = new String[tok.countTokens()];
        while (tok.hasMoreTokens()) {
            paramsArray[index] = tok.nextToken();
            index++;
        }
        return paramsArray;
     }
    /**
     * gets the 'provider' qualifier string
     * @param pQual the CIMQualifier
     * @return a string which represents the provider qualifier from the MOF
     */
    private String getProviderString(CIMQualifier pQual)
	throws CIMClassException, CIMException {
        if (pQual == null) {
            throw new CIMClassException(CIMClassException.CIM_ERR_NOT_FOUND,
                "Invalid qualifier, can not be null!");
        } else {
            // get the provider qualifer as a string
	    String qual = (String)pQual.getValue().getValue();
	    if (pQual == null) {
		throw new CIMClassException(CIMClassException.CIM_ERR_NOT_FOUND,
			"Invalid provider qualifier, no value found!");
	    }
            return qual;
        }
    }

    /**
     * This method is used internally by the CIMOM to register
     * Provider adapters during CIMOM initialization. 
     *
     * When the CIMOM starts up the first thing it will do is
     * go through an initilization process that includes registering
     * all of the configured provider protocol adapters.
     *
     * The registration process for a protocol adapter is to 
     * create an instance of Solaris_CIMOMProviderProtocolAdapter
     * The CIMOM is responsibile for enumerating the instances
     * of this class.  If the adapter is configured to be activated
     * on startup, the CIMOM will call the initialize() method.
     *
     * All Provider Protocol Adapaters are required to implement the 
     * org.wbemservices.wbem.cimom.protocol.adapter.ProviderProtocolAdapterIF
     * interface.
     */
    void startAdapters() throws CIMException {
        CIMInstance ci = null;
	try {
	    CIMObjectPath cop = new CIMObjectPath(PPACIMCLASS);
	    cop.setNameSpace(CIMOMImpl.INTEROPNS);
	    CIMInstance[] protocols = 
	    adapterProvider.enumerateInstances(cop, false,
	    true, true, null, null);
	    for (int i = 0; i < protocols.length; i++) {
		ci = protocols[i];
		handleAdapterInstance(ci);
                ci = null;
	    }
	} catch (Exception e) {
	    Debug.trace1("Provider registration failed: " + 
		((ci == null)?"no provider":ci.getObjectPath().toString()), e);
	}
    }

    // This method handles processing of the instance of the provider protocol
    // adapter. It finds the protocol type and registers that protocol type.
    private void handleAdapterInstance(CIMInstance ci) throws CIMException {
	Debug.trace3("Handling provider protocol " + ci);
	try {
                // get the classpath property
                CIMProperty pathProp = ci.getProperty(CLASSPATH);
                CIMValue pathVal = pathProp.getValue();
		// defautl to no additional classpaths
                String path[] = new String[0];
                // make sure value isn't null and that it's a string array
                if (pathVal != null && pathVal.isNull() == false &&
                pathVal.getType().getType() == CIMDataType.STRING_ARRAY) {
		    // get a vector of strings
		    java.util.Vector vec = (java.util.Vector)pathVal.getValue();
		    // convert the vector of strings to a string array
		    path = (String[])vec.toArray(new String[vec.size()]);
                }
		// create dynamic class loader for adpater, pass in classpath
                DynClassLoader dcl = new DynClassLoader(path,
			getClass().getClassLoader());
		// get the impl class property
		CIMProperty cp = ci.getProperty(IMPLCLASS);
		// get the value of the impl class
		CIMValue cv = cp.getValue();
		// get the string name of the class to load
		String className = (String)(cv.getValue());
		// load adapter class
                Class cl = dcl.loadClass(className);
                String automatic = "";
		try {
		    automatic = (String)
			(ci.getProperty("StartMode").getValue().getValue());
		} catch(Exception ex) {
		}
		String protocolType;
		try {
		    protocolType = (String)
			(ci.getProperty(PROTOCOLTYPE).getValue().getValue());
		} catch(NullPointerException ex) {
		    // We need a protocol type
		    throw new CIMPropertyException(
			  CIMException.CIM_ERR_INVALID_PARAMETER, PROTOCOLTYPE);
		}

		if (protocolType.trim().length() == 0) {
		    // We need a protocol type
		    throw new CIMPropertyException(
			CIMException.CIM_ERR_INVALID_PARAMETER, PROTOCOLTYPE);
		}

		if (protocolType.equals("internal")) {
		    // We do not accept internal provider adapter - this
		    // can never be changed.
		    throw new CIMPropertyException(
			CIMException.CIM_ERR_INVALID_PARAMETER, "internal");
		}

		ProviderProtocolAdapterIF adapter  = 
		    (ProviderProtocolAdapterIF)cl.newInstance();
		boolean isAuto = false;
		if (automatic.equalsIgnoreCase("Automatic")) {
		    isAuto = true;
		}
		RegisterProtocolProvider(protocolType, adapter, isAuto);
	} catch (CIMException e) {
	    throw e;
	} catch (Exception e) {
	    Debug.trace2("Adapter exception", e);
	    throw new CIMException(CIMException.CIM_ERR_FAILED, 
	    e.toString());
	}
    }

    // For now we will not implement Authorizable, since only root has access
    // to this class and we'll rely on the default CIMOM check.
    private class AdapterProvider implements CIMInstanceProvider,
    CIMMethodProvider {
	CIMInstanceProvider internalProvider;
	public AdapterProvider() {
	    internalProvider = mCimom.getInternalCIMInstanceProvider();
	}

	public CIMObjectPath[] enumerateInstanceNames(CIMObjectPath op, 
						      CIMClass cc)
	throws CIMException {
	    return internalProvider.enumerateInstanceNames(op, cc);
	}

	public CIMInstance[] enumerateInstances(CIMObjectPath op, 
						boolean localOnly,
						boolean includeQualifiers,
						boolean includeClassOrigin,
						String[] propList,
                				CIMClass cc)
	throws CIMException {
	    return internalProvider.enumerateInstances(op, localOnly,
	    includeQualifiers, includeClassOrigin, propList, cc);
	}

	public CIMInstance getInstance(CIMObjectPath op, 
				       boolean localOnly, 
				       boolean includeQualifiers, 
				       boolean includeClassOrigin, 
			    	       String[] propList,	
				       CIMClass cc)
	throws CIMException {
	    return internalProvider.getInstance(op, localOnly, 
	    includeQualifiers, includeClassOrigin, propList, cc);
	}

	public synchronized CIMObjectPath 
	createInstance(CIMObjectPath op, CIMInstance ci) 
	throws CIMException {
	    CIMObjectPath retVal = internalProvider.createInstance(op, ci);
	    try {
		handleAdapterInstance(ci);
	    } catch (CIMException e) {
		CIMObjectPath lop = new CIMObjectPath();
		lop.setNameSpace(op.getNameSpace());
		lop.setObjectName(op.getObjectName());
		lop.setKeys(ci.getKeys());
		internalProvider.deleteInstance(lop);
		throw e;
	    }
	    return retVal;
	}

	// We will not support updating the entries for now.
	public synchronized void setInstance(CIMObjectPath op, CIMInstance ci,
	boolean includeQualifier, String[] propertyList) 
	throws CIMException {
	    throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
	}

	public synchronized void 
	deleteInstance(CIMObjectPath op) throws CIMException {
	    // Stop and remove the adapter here.
	    CIMInstance ci = getInstance(op, false, false, false, null, null);
	    String protocolType;
	    try {
		protocolType = (String)
		(ci.getProperty(PROTOCOLTYPE).getValue().getValue());
	    } catch(NullPointerException ex) {
		// We need a protocol type
		throw new CIMPropertyException(
		CIMException.CIM_ERR_INVALID_PARAMETER,
		PROTOCOLTYPE);
	    }
	    UnRegisterProtocolProvider(protocolType);
	    internalProvider.deleteInstance(op);
	}

	public void initialize(javax.wbem.client.CIMOMHandle ch) {
	    // nothing to do yet
	}

	public void cleanup() {
	    // nothing to do yet.
	}

        public CIMInstance[] execQuery(CIMObjectPath op, String query, 
				       String ql, CIMClass cc) 
	throws CIMException {
	    return internalProvider.execQuery(op, query, ql, cc);
	}

	// We've to decide what exception we want to throw, and if any
	// auditing and logging needs to be done. This method is invoked
	// to handle starting and stopping of individual adapters.
	public CIMValue invokeMethod(CIMObjectPath op, String methodName,
	    CIMArgument[] inArgs, CIMArgument[] outArgs) throws CIMException {

	    CIMInstance ci = getInstance(op, false, false, false, null, null);
	    String protocolType;
	    try {
		protocolType = 
		(String)ci.getProperty(PROTOCOLTYPE).getValue().getValue();
	    } catch (NullPointerException e) {
		// Huh? there should be a value.
                Debug.trace1("Caught NullPointerException at AdapterProvider.invokeMethod",  e);
		return new CIMValue(new Integer(2));
	    }

	    ProviderProtocolAdapterIF adapter =  
	    (ProviderProtocolAdapterIF)
	    mAdapters.get(protocolType);

	    if (adapter == null) {
		// should probably log this
		return new CIMValue(new Integer(3));
	    }

	    if (methodName.equalsIgnoreCase(STARTSERVICE)) {
		adapter.start();
	    } else {
		adapter.stop();
	    }

	    return new CIMValue(new Integer(0));
	}
    }

    // This class implements a one to many association between the CIMOM and
    // its protocol adapters. 
    private static class AdapterAssocProvider extends OneToManyAssocProvider {
	private static final CIMObjectPath PPA_CLASS_OP =
	new CIMObjectPath(PPACIMCLASS, CIMOMImpl.INTEROPNS);
	
	protected String getOneRole(CIMObjectPath assocName) {
	    return "Antecedent";
	}

	protected String getManyRole(CIMObjectPath assocName) {
	    return "Dependent";
	}
    
	protected CIMObjectPath getManyClass(CIMObjectPath assocName) {
	    return PPA_CLASS_OP;
	}

	protected CIMObjectPath getOneClass(CIMObjectPath assocName) {
	    return CIMOMProvider.CLASSOP;
	}
    }
}
