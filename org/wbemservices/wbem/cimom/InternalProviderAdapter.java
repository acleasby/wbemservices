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

import org.wbemservices.wbem.cimom.adapter.provider.*;

import javax.wbem.cim.*;
import javax.wbem.client.ProviderCIMOMHandle;
import javax.wbem.provider.*;

import java.util.*;

/**
 * This class does the work of loading and tracking providers 
 * that are internal to the CIMOM
 */
public class InternalProviderAdapter implements ProviderProtocolAdapterIF {
    private static final String DESCRIPTION = "Internal class provider adapter";
    private static final String VENDOR = "Sun Microsystems Inc.";
    private static final int VERSION = 1;
    private boolean valid = false;

    private ProviderCIMOMHandle mCimom;
    private static Map internalProviders = Collections.synchronizedMap(new HashMap());

    // This interface is implemented by all the internal 'services' which
    // have providers embedded in them. The providers are for those parts
    // of the service which have been modeled and can be managed through
    // standard CIM mechanisms. The InternalProviderAdapter accesses all
    // the embedded providers of the service through this interface.
    public interface InternalServiceProvider {
	// Returns the names of the internal providers.
	String[] getProviderNames();
	// For the given providerName, returns the actual provider.
	CIMProvider getProvider(String providerName) throws CIMException;
    }

    // XXX for now, the CIMOM needs to set this up, becuase the CIMOM itself
    // can be a provider. We'll remove this once we move all providers out
    // of the CIMOM.
    CIMOMImpl cimom;
    InternalServiceProvider[] serviceProviderArray;

    // Make a package protected consturctor. Only the CIMOM package
    // initializes this adapter.
    InternalProviderAdapter() {
    }

    // Must throw CIMException here ??
    public void initialize(ProviderCIMOMHandle pCimom) {
        mCimom = pCimom;
	// Ask the services what providers are present.
	for (int count = 0; count < serviceProviderArray.length; count++) {
	    InternalServiceProvider sp = serviceProviderArray[count];
	    String[] providerNames = sp.getProviderNames();
	    // Update the internal provider list with the returned
	    // providers.
	    for (int j = providerNames.length - 1; j >= 0; j--) {
		String pn = providerNames[j];
		try {
		    // This try catch must go, we should throw an exception
		    CIMProvider cp = sp.getProvider(pn);
		    cp.initialize(pCimom);
		    internalProviders.put(pn, cp);
		} catch (Exception e) {
		    javax.wbem.client.Debug.trace1("Unable to add provider: " + pn, e);
		}
	    }
	}
	internalProviders.put("cimom", cimom);
    }

    /**
    * Called when the CIMOM is ready to use the adapter.
    * <I>Can be called multiple times</I>
    */
    public void start() {
	valid = true;
    }

    /**
    * Called by the CIMOM after calling <I>start</I> to ensure
    * that the adpater is in a usable state. <B>It will only be called
    * once</B>.<br>
    * For example the adapter may require features from JDK 1.3, but the
    * current VM is JDK 1.2, so the adpater should return false.
    * @return true if the adapter can be used, false otherwise
    */
    public boolean isValid() {
        return valid;
    }

    /**
    * called by the CIMOM to tell the adapter to stop running
    * <I>Can be called multiple times</I>
    */
    public void stop() {
	valid = false;
    }

    /**
    * Returns the version of the adapter
    * @return an integer which identifies the version of the adapter
    */
    public int getVersion() {
        return VERSION;
    }

    /**
    * The name of the vendor of this adapter
    */
    public String getVendor() {
        return VENDOR;
    }

    /**
    * The description of this adpater
    */
    public String getDescription() {
        return DESCRIPTION;
    }
    
    /**
    * Adds a provider to the internal provider list
    */
    static public void putProvider(String name, CIMProvider cp) {
        internalProviders.put(name, cp);
    }

    /**
     * Tells the adapter to load a specific provider.
     * 
     * @param pNamespace The namespace being used
     * @param pName The name the provider to load
     * @param pParams an array of parameters
     * @param pCimClass the CIM Class the provider is being loaded for
     * @return A unique object which represents the provider to this adapter.
     * @throws ClassNotFoundException if a provider by the specified name
     * @throws CIMClassException if the specified class is not a provider can
     *                 not be loaded.
     * @see #unloadProvider(Object)
     */
    public Object loadProvider(String pNamespace, String pName,
	String[] pParams, CIMClass pCimClass) throws ClassNotFoundException, CIMClassException {
        CIMProvider provider = null;
	provider = 
	(CIMProvider)internalProviders.get(pName);
	if (provider == null) {
	    // The CIMOM is the default provider
	    provider = cimom;
	}
        if (provider instanceof ProviderAdapterIF) {
            return provider;
        } else {
            return new WrappedProviderAdapter(provider);
        }
    }

    /**
     * Tells the adapter to unload the specified provider. The provider <B>
     * MUST</B> be unloaded when this function is called. If the adapter can't
     * unload the provider, the CIMOM will be in an undefined state.
     * 
     * @param pUnique The object returned from <i>LoadProvider</i> which
     *                should uniquely identify the provider to the adapter
     * @throws CIMClassException if the provider can not be unloaded
     */
    public void unloadProvider(Object pUnique)
    throws CIMClassException {
        // XXX how to unload a class??!! XXX
    }

    /**
     * XXX Why do we need this method?? Asks the adapter to return the
     * 'provider' associated with the passed in object
     * 
     * @param pProvider The object which the adapter returned via a call to
     *                LoadProvider which uniquely identifies a provider
     * @return A class which implements the CIMAdapterProviderIF interface.
     * @throws CIMClassException if the provider can not be located.
     */
    public ProviderAdapterIF getProviderAdapter(Object pProvider)
    throws CIMClassException {
        if (pProvider instanceof ProviderAdapterIF) {
            return (ProviderAdapterIF)pProvider;
        }
        throw new CIMClassException(CIMException.CIM_ERR_INVALID_CLASS,
            "Unrecognized object!");
    }

    /**
    * Called by the CIMOM to tell the adapter that it should cease
    * and desists all operations. The adapter will be released from
    * memory after this call
    */
    public void terminate() {
    }

    /**
     * a wrapper class which is used to wrap Java providers in a
     */
    public class WrappedProviderAdapter implements ProviderAdapterIF {
        private CIMProvider mWrapped = null;
        /**
         * Constructs a new wrapper
         * @param pWrapped the underlying provider
         */
        WrappedProviderAdapter(CIMProvider pWrapped) {
            mWrapped = pWrapped;
        }
        /**
         * Indicates that the provider implements the InstanceProvider
	 * interface
         * @return true if it does implement the interface or false otherwise
         */
        public boolean isInstanceProvider() {
            return (mWrapped instanceof CIMInstanceProvider);
        }

        /**
         * Indicates that the provider implements the MethodProvider interface
         * @return true if it does implement the interface or false otherwise
         */
        public boolean isMethodProvider() {
            return (mWrapped instanceof CIMMethodProvider);
        }

        /**
         * Indicates that the provider implements the CIMIndicationProvider interface
         * @return true if it does implement the interface or false otherwise
         */
        public boolean isCIMIndicationProvider() {
             return (mWrapped instanceof CIMIndicationProvider);
        }

        /**
         * Indicates that the provider implements the EventProvider interface
         * @return true if it does implement the interface or false otherwise
         */
        public boolean isEventProvider() {
            return (mWrapped instanceof EventProvider);
        }

        /**
         * Indicates that the provider implements the Authorizable interface
         * @return true if it does implement the interface or false otherwise
         */
        public boolean isAuthorizable() {
            return (mWrapped instanceof Authorizable);
        }

        /**
         * Indicates that the provider implements the AssociatorProvider
	 * interface
         * @return true if it does implement the interface or false otherwise
         */
         public boolean isAssociatorProvider() {
             return (mWrapped instanceof CIMAssociatorProvider);
         }
                
        /**
         * This function returns the wrapped provider
         */
        public CIMProvider getProvider() {
            return mWrapped;
        }
    }
}
