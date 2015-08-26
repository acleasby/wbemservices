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
package org.wbemservices.wbem.cimom.adapter.provider;


import javax.wbem.client.ProviderCIMOMHandle;

import javax.wbem.cim.CIMClassException;
import javax.wbem.cim.CIMClass;

/**
 * This interface can be implemented to allow the CIMOM to interface with new
 * provider implementations, for example there maybe a JNI, RMI, IIOP, and RPC
 * based providers.  In order for the CIMOM to access these providers it needs
 * an "Adapter" to provider the actual code for communication.  THis interface
 * allows the CIMOM a standard way of having these "Adapters" plug into it.
 *
 * @author  Jim M.
 * @version @(#)ProviderProtocolAdapterIF.java	1.2
 */
public interface ProviderProtocolAdapterIF {

        /**
         * Tells the adapter to load a specific provider.
	 * @param pNamespace the name of the namespace
         * @param pName The name of the provider to load
         * @param pParams an array of parameters
	 * @param pCimClass the CIM Class this provider is being loaded for
         * @return A unique object which represents the provider to the
         * adapter. This object is considered a <I>key</I> to the adapter for
         * the specified provider.  The Adpater should be able to use this
         * Object to identify the provider in question.
         * @throws ClassNotFoundException if a provider by the specified name
         * can not be loaded.
         * @see #unloadProvider(Object)
         */
        public Object loadProvider(String pNamespace, String pName,
	    String[] pParams, CIMClass pCimClass)
	    throws CIMClassException, ClassNotFoundException;

        /**
         * Tells the adapter to unload the specified provider. The provider
         * <B>MUST</B> be unloaded when this function is called. If the adapter
         * can't unload the provider, the CIMOM will be in an undefined state.
         * @param pUnique The object returned from <i>LoadProvider</i> which
         * should uniquely identify the provider to the adapter
         * @throws CIMClassException if the provider can not be unloaded
         */
        public void unloadProvider(Object pUnique)
            throws CIMClassException;

        /**
         * Asks teh adapter to return the 'provider' associated with the
         * passed in object
         * @param pProvider The object which the adapter returned via a call
         * to LoadProvider which uniquely identifies a provider
         * @return A class which implements the CIMAdapterProviderIF interface.
         * @throws CIMClassException if the provider can not be located.
         */
        public ProviderAdapterIF getProviderAdapter(Object pProvider)
            throws CIMClassException;

        /**
         * Returns the version of the adapter
         * @return an integer which identifies the version of the adapter
         */
        public int getVersion();

        /**
         * The name of the vendor of this adapter
         */
        public String getVendor();

        /**
         * The description of this adpater
         */
        public String getDescription();

        /**
         * Called once to allow for any one time intializations to occur.
         * <I>Will only be called once</I>
         * @param pHandle A CIMOMderHandle instance
         */
        public void initialize(ProviderCIMOMHandle pHandle);


        /**
         * Called when the CIMOM is ready to use the adapter.
         * <I>Can be called multiple times</I>
         */
        public void start();

        /**
         * Called by the CIMOM after calling <I>start</I> to ensure
         * that the adpater is in a usable state. <B>It will only be called
         * once</B>.<br>
         * For example the adapter may require features from JDK 1.3, but the
         * current VM is JDK 1.2, so teh adpater should return false.
         * @return true if the adapter can be used, false otherwise
         */
        public boolean isValid();

        /**
         * called by the CIMOM to tell the adapter to stop running
         * <I>Can be called multiple times</I>
         */
        public void stop();

        /**
         * Called by the CIMOM to tell the adapter that it should cease
         * and desists all operations. The adapter will be released from
         * memory after this call
         */
        public void terminate();
}

