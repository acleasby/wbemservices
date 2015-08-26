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
package org.wbemservices.wbem.cimom.adapters.provider.java;

import org.wbemservices.wbem.cimom.adapter.provider.*;

import org.wbemservices.wbem.cimom.util.DynClassLoader;

import javax.wbem.cim.*;
import javax.wbem.client.CIMOMHandle;
import javax.wbem.client.Debug;
import javax.wbem.client.ProviderCIMOMHandle;
import javax.wbem.provider.*;

/**
 * This class does the work of loading and tracking providers written in
 * Java.
 */
public class JavaProviderAdapter implements ProviderProtocolAdapterIF,
						CIMInstanceProvider {
    private static final String DESCRIPTION = "A Java class provider adapter";
    private static final String VENDOR = "Sun Microsystems Inc.";
    private static final int VERSION = 1;
    private final static String PROVIDERPATHCLASS = "solaris_providerpath";
    private final static String SYSTEMNS = "/root/system";
    private final static String PATHURLPROPERTY = "pathurl";
    private boolean valid = false;

    private ProviderCIMOMHandle mCimom;
    private DynClassLoader dcl = null;
    private boolean passAllCheck = false;
    private CIMInstanceProvider internalProvider;

    public JavaProviderAdapter() {
        ClassLoader cl = this.getClass().getClassLoader();
        if (cl instanceof DynClassLoader) {
            dcl = (DynClassLoader)cl;
        } else {
            dcl = new DynClassLoader(getClass().getClassLoader());
        }
    }
    
    /** adds a path the classloader
    * @param path the path to add to the loader
    * @hrows CIMException if it is unable to add the path
    */
    void updatePath(String path) throws CIMException {
        try {
            dcl.addToClassPath(path);
        } catch (Exception e) {
            throw new CIMException(CIMException.CIM_ERR_FAILED, e);
        }
    }

    public void initialize(ProviderCIMOMHandle pCimom) {
        mCimom = pCimom;
        String passAll = System.getProperty("passAllProviderCheck", "false");
        if(passAll != null) {
            this.passAllCheck = passAll.compareToIgnoreCase("true") == 0;
        }
	// Populate the provider paths
	CIMObjectPath provPath = new CIMObjectPath(PROVIDERPATHCLASS);
	provPath.setNameSpace(SYSTEMNS);
	try {
	    internalProvider = mCimom.getInternalCIMInstanceProvider();
	    CIMObjectPath[] ops =
                internalProvider.enumerateInstanceNames(provPath, null);
	    if (ops == null) {
		Debug.trace2("Got a null return for enumerate");
		return;
	    }
	    // There will only be one property in each key, that is the path
	    // that we want.
	    for (int i = 0; i < ops.length; i++) {
		CIMObjectPath pathOp = ops[i];
		CIMProperty pathProp = 
		(CIMProperty)pathOp.getKeys().elementAt(0);
		try {
		    String url = (String)(pathProp.getValue().getValue());
		    updatePath(url);
		} catch (Exception e1) {
		   Debug.trace1("Failure updating the classpath", e1);
		}
	    }
	} catch (Exception e) {
	    Debug.trace1("Problem enumerating path info", e);
	}
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
    * current VM is JDK 1.2, so teh adpater should return false.
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
    * Tells the adapter to load a specific provider.
    * @param pNamespace The namespace being used
    * @param pName The name the provider to load
    * @param pParams an array of parameters
    * @param pCimClass the class this provider is being loaded for
    * @return A unique object which represents the provider to this
    * adapter.
    * @throws ClassNotFoundException if a provider by the specified name
    * @throws CIMClassException if the specified class is not a provider
    * can not be loaded.
    * @see #unloadProvider(Object)
    */
    public Object loadProvider(String pNamespace, String pName,
	String[] pParams, CIMClass pCimClass) throws ClassNotFoundException, CIMClassException {
	try {
	    Debug.trace3("Need to load provider "+pName);
	    Object obj = null;
	    Class c = dcl.loadClass(pName);
	    Debug.trace3("Loaded java class "+c.getName());
	    obj = c.newInstance();
	    // Check if its a provider adapterIF
	    if (obj instanceof ProviderAdapterIF) {
		ProviderAdapterIF provider = (ProviderAdapterIF)obj;
		// Initialize the provider
		provider.getProvider().initialize(mCimom);
		return provider;
	    } else if (obj instanceof CIMProvider) {
		Debug.trace3(c.getName() + " is a new provider type");
		// It is a javax.wbem.cim.provider.CIMProvider
		CIMProvider provider = (CIMProvider)obj;
		// Wrap the provider
		ProviderAdapterIF providerIF = 
		new WrappedProviderAdapter(provider);
		// Initialize the provider
		providerIF.getProvider().initialize(mCimom);
		return providerIF;
	    }
	}
	catch (InstantiationException ie) {
	    throw new ClassNotFoundException(ie.toString(), ie);
	}
	catch (IllegalAccessException iae) {
	    throw new CIMClassException(iae.toString(), iae);
	}
	catch (CIMException ce) {
	    throw new ClassNotFoundException(ce.toString(), ce);
	}
	// if we get here the class we loaded is not a provider
	throw new CIMClassException(CIMException.CIM_ERR_INVALID_CLASS);
    }

    /**
    * Tells the adapter to unload the specified provider. The provider
    * <B>MUST</B> be unloaded when this function is called. If the adapter
    * can't unload the provider, the CIMOM will be in an undefined state.
    * @param pUnique The object returned from <i>LoadProvider</i> which
    * should uniquely identify the provider to the adapter
    * @throws CIMClassException if the provider can not be unloaded
    */
    public void unloadProvider(Object pUnique)
    throws CIMClassException {
        // Given the way this provider works, if pUnique is not null then we
        // have a valid provider, as such none of the calls below should fail
        // except 'cleanup'
        if (pUnique == null) {
            throw new CIMClassException(CIMException.CIM_ERR_FAILED,
                "Provider identifier is null");
        }
        CIMProvider prov = getProviderAdapter(pUnique).getProvider();
        try {
            prov.cleanup();
        }
        catch(CIMException ce) {
            throw new CIMClassException(ce.getID(), ce);
        }
    }

    /**
    * Asks teh adapter to return the 'provider' associated with the
    * passed in object
    * @param pProvider The object which the adapter returned via a call
    * to LoadProvider which uniquely identifies a provider
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
        private boolean isCIMInst = false;
        private boolean isCIMMethod = false;
        private boolean isCIMAssoc = false;
        private boolean isCIMIndication = false;
        private boolean isAuth = false;
        private boolean isEvent = false;
        
        /**
         * Constructs a new wrapper
         * @param pWrapped the underlying provider
         */
        WrappedProviderAdapter(CIMProvider pWrapped) {
	    mWrapped = pWrapped;
            /* first check if the provider implements the new interfaces */
            isCIMInst = (pWrapped instanceof CIMInstanceProvider);
            isCIMMethod = (pWrapped instanceof CIMMethodProvider);
            isCIMAssoc = (pWrapped instanceof CIMAssociatorProvider);
            isCIMIndication = (pWrapped instanceof CIMIndicationProvider);
            isAuth = (pWrapped instanceof Authorizable);
            isEvent = (pWrapped instanceof EventProvider);
            /* now check for old interfaces, first make sure the provider
             * doesn't implement the new interface. If it doesn't see if
             * it implements the old interface
             */
        }
        /**
         * Indicates that the provider implements the CIMInstanceProvider
	 * interface
         * @return true if it does implement the interface or false otherwise
         */
        public boolean isInstanceProvider() {
            return this.isCIMInst;
        }

        /**
         * Indicates that the provider implements the CIMMethodProvider interface
         * @return true if it does implement the interface or false otherwise
         */
        public boolean isMethodProvider() {
            return this.isCIMMethod;
        }

        /**
         * Indicates that the provider implements the CIMIndicationProvider interface
         * @return true if it does implement the interface or false otherwise
         */
        public boolean isCIMIndicationProvider() {
             return this.isCIMIndication;
        }

        /**
         * Indicates that the provider implements the EventProvider interface
         * @return true if it does implement the interface or false otherwise
         */
        public boolean isEventProvider() {
            return this.isEvent;
        }

        /**
         * Indicates that the provider implements the Authorizable interface
         * @return true if it does implement the interface or false otherwise
         */
        public boolean isAuthorizable() {
            return this.isAuth;
        }

        /**
         * Indicates that the provider implements the AssociatorProvider
	 * interface
         * @return true if it does implement the interface or false otherwise
         */
         public boolean isAssociatorProvider() {
             return this.isCIMAssoc;
         }
                
        /**
         * This function returns the wrapped provider
         */
        public CIMProvider getProvider() {
            return mWrapped;
        }
    }

    // Begin instance provider methods here. This adapter is a provider for
    // Solaris_ProviderPath. When instance of this class are created, this
    // results in the CLASSPATH to get updated.
    public CIMObjectPath[] enumerateInstanceNames(CIMObjectPath op, 
					 	  CIMClass cc)
    throws CIMException {
	// delegate to the internal provider.
	return internalProvider.enumerateInstanceNames(op, cc);
    }

    public CIMInstance[] enumerateInstances(CIMObjectPath op, 
					    boolean localOnly, 
					    boolean includeQualifiers, 
					    boolean includeClassOrigin, 
					    String[] propertyList, 
					    CIMClass cc) 
    throws CIMException {
	// delegate to the internal provider.
	return internalProvider.enumerateInstances(op, localOnly, 
							includeQualifiers,
							includeClassOrigin, 
							propertyList, cc);
    }

    public CIMInstance getInstance(CIMObjectPath op, 
    				   boolean localOnly,
				   boolean includeQualifiers, 
				   boolean includeClassOrigin, 
				   String[] propertyList, 
				   CIMClass cc)
    throws CIMException {
	// delegate to the internal provider.
	return internalProvider.getInstance(op, localOnly, 
					includeQualifiers, includeClassOrigin,
					propertyList, cc);
    }

    public CIMObjectPath createInstance(CIMObjectPath op, CIMInstance ci) 
    throws CIMException {
	// We'll first update the CLASSPATH and then delegate to the internal
	// provider.
        try {
	    String url =
	    (String)ci.getProperty(PATHURLPROPERTY).getValue().getValue();
	    updatePath(url);
	    // now delegate
	    return internalProvider.createInstance(op, ci);
        } catch (CIMException e) {
	    throw e;
	} catch (Exception e) {
                throw new CIMException(CIMException.CIM_ERR_FAILED, e);
        }
    }


    public void setInstance(CIMObjectPath op, CIMInstance ci,
        boolean includeQualifiers, String[] propertyList) throws CIMException {
	// We do not allow sets for now.
	throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
    }

    public void deleteInstance(CIMObjectPath op) 
    throws CIMException {
	// delegate. This is a possible issue here - Do we now want to remove
	// the given url from the CLASSPATH? We're not doing it for now. The
	// url disappears when the adapter is reinitialized - i.e. during
	// CIMOM reboot.
	internalProvider.deleteInstance(op);
    }

    public CIMInstance[] execQuery(CIMObjectPath op, 
				   String query, 
				   String ql, 
				   CIMClass cc)
    throws CIMException {
	// delegate.
	return internalProvider.execQuery(op, query, ql, cc);
    }

    public void initialize(CIMOMHandle pCimom) {
	// this is just there to satisfy the CIMInstanceProvider interface.
    }

    public void cleanup() {
	// Should be careful here - we dont do anything. The 
	// JavaProviderAdapter cleans itself up only if the factory tells it
	// to.
    }
}
