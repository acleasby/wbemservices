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

import org.wbemservices.wbem.cimom.adapter.client.ClientProtocolAdapterIF;
import org.wbemservices.wbem.cimom.util.DynClassLoader;

import javax.wbem.cim.*;
import javax.wbem.client.Debug;
import javax.wbem.client.ProviderCIMOMHandle;
import javax.wbem.provider.CIMInstanceProvider;
import javax.wbem.provider.CIMMethodProvider;
import javax.wbem.provider.CIMProvider;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * This class deals with the protocol adapters that the CIMOM deals with.
 * Currently the CIMOM can speak multiple client protocols like XML/HTTP,
 * RMI, SUNRMI (this was the old com.sun based RMI). We may want to deal
 * with event protocol adapters here or separately.
 */
public class ClientProtocolAdapterService implements
        InternalProviderAdapter.InternalServiceProvider {

    // The ProtocolAdapterService is an internal provider for the CIM class
    // WBEMServices_ObjectManagerClientProtocolAdapter

    // CIM class that represents the client protocol adapters
    private final static String CPA_CIM_CLASS =
            "WBEMServices_ObjectManagerClientProtocolAdapter";
    // Association class that links protocol adapters to the CIMOM.
    private final static String CPA_ASSOC_CLASS =
            "WBEMServices_ClientProtocolAdapterForManager";
    private final static String COMMMECH_ASSOC_CLASS =
            "CIM_CommMechanismForManager";
    // name of the start service method.
    private final static String STARTSERVICE = "StartService";
    // name of the stop service method.
    private final static String STOPSERVICE = "StopService";
    // name of the implementation class property
    private final static String IMPLCLASS = "ImplClass";
    // name of classpath property
    private final static String CLASSPATH = "classPath";
    // name of the protocol type property
    private final static String PROTOCOLTYPE = "Name";

    private ProviderCIMOMHandle pch;
    private AdapterProvider ap;
    private AdapterAssocProvider assocProvider;
    private CIMProvider commMechAssocProvider;
    private CIMOMServer cimom;
    private Map adapterMap = Collections.synchronizedMap(new HashMap());

    // InternalServiceProvider interface method. See InternalProviderAdapter.
    public String[] getProviderNames() {
        return new String[] { CPA_CIM_CLASS, CPA_ASSOC_CLASS,
                COMMMECH_ASSOC_CLASS };
    }

    // This method will be called by the CIMOM when it is first initializing
    // its services. It uses this method to get the appropriate providers
    // for filter, filterdelivery, rmidelivery etc.
    // InternalServiceProvider interface method. See InternalProviderAdapter.
    public CIMProvider getProvider(String className) throws CIMException {
        if (className.equals(CPA_CIM_CLASS)) {
            return ap;
        } else if (className.equals(COMMMECH_ASSOC_CLASS)) {
            return commMechAssocProvider;
        } else {
            return assocProvider;
        }
    }

    /**
     * This method is used internally by the CIMOM to register
     * Client adapters during CIMOM initialization.
     * <p/>
     * When the CIMOM starts up the first thing it will do is
     * go through an initilization process that includes registering
     * all of the configured client protocol adapters.
     * <p/>
     * The registration process for a protocol adapter is to
     * create an instance of WBEMServices_ObjectManagerClientProtocolAdapter
     * The method is responsibile for enumerating the instances
     * of this class.  If the adapter is configured to be activated
     * on startup, the method will call the initialize() method.
     * <p/>
     * All Client Protocol Adapaters are required to implement the
     * org.wbemservices.wbem.cimom.protocol.adapter.CIMOMClientProtocolAdapterIF
     * interface.
     * <p/>
     * NOTE: All internal CIMOM services may have a start method which the CIMOM
     * invokes and made part of a 'Service' interface, in which case this method
     * name will change.
     */
    void startAdapters() throws Exception {
        try {
            CIMObjectPath cop = new CIMObjectPath(CPA_CIM_CLASS);
            cop.setNameSpace(CIMOMImpl.INTEROPNS);
            Enumeration protocols = pch.enumerateInstances(cop, true,
                    false, true, true, null);
            while (protocols.hasMoreElements()) {
                CIMInstance ci = (CIMInstance) protocols.nextElement();
                try {
                    handleAdapterInstance(ci);
                } catch (Exception e) {
                    Debug.trace1("Client adapter registration failed", e);
                    // lets continue to the next one.
                }
            }

        } catch (Exception e) {
            System.err.println("!!!XXX: RegisterClientAdapter: Exception");
            Debug.trace1("Client adapter registration failed", e);
            e.printStackTrace();
        }
    }

    /**
     * Constructor for the service.
     *
     * @param pch A provider CIMOM handle that can be used by the internal
     *            providers.
     */
    ClientProtocolAdapterService(ProviderCIMOMHandle pch, CIMOMServer cimom) {
        // For now we use the same internal provider for both protocol
        // adapters.
        this.pch = pch;
        this.ap = new AdapterProvider();
        this.assocProvider = new AdapterAssocProvider();
        this.commMechAssocProvider = new CommMechAssocProvider();
        this.cimom = cimom;
        // cleanup the communication mechanisms. These instances must be
        // populated by the individual protocol adapters that we start up.
        CIMObjectPath classOp =
                new CIMObjectPath("CIM_ObjectManagerCommunicationMechanism",
                        CIMOMImpl.INTEROPNS);
        try {
            Enumeration e = pch.enumerateInstanceNames(classOp);
            while (e.hasMoreElements()) {
                CIMObjectPath op = (CIMObjectPath) e.nextElement();
                try {
                    pch.deleteInstance(op);
                } catch (CIMException dce) {
                    Debug.trace2("Exception deleting instance of CommMech",
                            dce);
                    // ignore the exception and continue.
                }
            }
        } catch (CIMException ce) {
            // Ignore the exception and continue
            Debug.trace2("Exception getting instances of CommMech",
                    ce);
        }
    }

    private void handleAdapterInstance(CIMInstance ci) throws CIMException {
        Debug.trace3("Handling client protocol " + ci);
        ClientProtocolAdapterIF adapter = null;
        try {
            // get the classpath property
            CIMProperty pathProp = ci.getProperty(CLASSPATH);
            CIMValue pathVal = pathProp.getValue();
            // default to no additional classpaths
            String path[] = new String[0];
            // make sure value isn't null and that it's a string array
            if (pathVal != null && pathVal.isNull() == false &&
                    pathVal.getType().getType() == CIMDataType.STRING_ARRAY) {
                // get a vector of strings
                java.util.Vector vec = (java.util.Vector) pathVal.getValue();
                // convert the vector of strings to a string array
                path = (String[]) vec.toArray(new String[vec.size()]);
            }
            // create dynamic class loader for adpater, pass in classpath
            DynClassLoader dcl = new DynClassLoader(path,
                    getClass().getClassLoader());

            // get the impl class property
            CIMProperty cp = ci.getProperty(IMPLCLASS);
            // get the value of the impl class
            CIMValue cv = cp.getValue();
            // get the string name of the class to load
            String className = (String) (cv.getValue());
            Class cl = dcl.loadClass(className);
            adapter = (ClientProtocolAdapterIF) cl.newInstance();
        } catch (Exception e) {

            Debug.trace2("Adapter instance failed", e);
            throw new CIMException(CIMException.CIM_ERR_FAILED, e.toString());
        }
        String protocolType;
        try {
            protocolType = (String)
                    (ci.getProperty(PROTOCOLTYPE).getValue().getValue());
        } catch (NullPointerException ex) {
            // We need a protocol type
            throw new CIMPropertyException(
                    CIMException.CIM_ERR_INVALID_PARAMETER,
                    PROTOCOLTYPE);
        }

        if (protocolType.trim().length() == 0) {
            // We need a protocol type
            throw new CIMPropertyException(
                    CIMException.CIM_ERR_INVALID_PARAMETER,
                    PROTOCOLTYPE);
        }

        String automatic = "";
        try {
            automatic = (String)
                    (ci.getProperty("StartMode").getValue().getValue());
        } catch (Exception ex) {
        }
        adapter.initialize(cimom, pch);
        if (automatic.equalsIgnoreCase("Automatic")) {
            adapter.startService(ci);
        }
        // We store a handle of the adapter in order to manipulate
        // it later.
        adapterMap.put(protocolType, adapter);
    }

    // *** Private stuff here *** //
    // For now we will not implement Authorizable, since only root has access
    // to this class and we'll rely on the default CIMOM check.
    private class AdapterProvider implements CIMInstanceProvider,
            CIMMethodProvider {
        CIMInstanceProvider internalProvider;

        public AdapterProvider() {
            internalProvider = pch.getInternalCIMInstanceProvider();
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
            // We'll need to update the dynamic class path.
            CIMObjectPath retVal = internalProvider.createInstance(op, ci);
            try {
                handleAdapterInstance(ci);
            } catch (CIMException e) {
                CIMObjectPath lop = new CIMObjectPath();
                lop.setNameSpace(op.getNameSpace());
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
            CIMProperty cp = ci.getProperty(PROTOCOLTYPE);
            CIMValue cv = cp.getValue();
            String className = (String) (cv.getValue());
            ClientProtocolAdapterIF adapter =
                    (ClientProtocolAdapterIF) adapterMap.get(className);
            internalProvider.setInstance(op, ci, includeQualifier,
                    propertyList);
            CIMInstance newci = getInstance(
                    op, false, false, false, null, null);
            if (adapter != null) {
                adapter.stopService();
                adapter.startService(newci);
            } else {
                try {
                    handleAdapterInstance(newci);
                } catch (CIMException e) {
                    CIMObjectPath lop = new CIMObjectPath();
                    lop.setNameSpace(op.getNameSpace());
                    lop.setKeys(ci.getKeys());
                    internalProvider.deleteInstance(lop);
                    throw e;
                }
            }
        }

        public synchronized void
        deleteInstance(CIMObjectPath op) throws CIMException {
            // Stop and remove the adapter here.
            CIMInstance ci = getInstance(op, false, false, false, null, null);
            CIMProperty cp = ci.getProperty(PROTOCOLTYPE);
            CIMValue cv = cp.getValue();
            String className = (String) (cv.getValue());
            ClientProtocolAdapterIF adapter =
                    (ClientProtocolAdapterIF) adapterMap.get(className);

            if (adapter != null) {
                adapter.stopService();
            }

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
            String name;
            try {
                name =
                        (String) ci.getProperty(PROTOCOLTYPE).getValue().getValue();
            } catch (NullPointerException e) {
                // Huh? there should be a value.
                Debug.trace1("Caught NullPointerException at ClientProtocolAdapterService.invokeMethod", e);
                return new CIMValue(new Integer(2));
            }

            ClientProtocolAdapterIF adapter =
                    (ClientProtocolAdapterIF) adapterMap.get(name);

            if (adapter == null) {
                // should probably log this
                return new CIMValue(new Integer(3));
            }

            if (methodName.equalsIgnoreCase(STARTSERVICE)) {
                adapter.startService(ci);
            } else {
                adapter.stopService();
            }

            return new CIMValue(new Integer(0));
        }
    }

    // This class implements a one to many association between the CIMOM and
    // its protocol adapters (both client and provider). 
    private static class AdapterAssocProvider extends OneToManyAssocProvider {
        private static final CIMObjectPath CPA_CLASS_OP =
                new CIMObjectPath(CPA_CIM_CLASS, CIMOMImpl.INTEROPNS);

        protected String getOneRole(CIMObjectPath assocName) {
            return "Antecedent";
        }

        protected String getManyRole(CIMObjectPath assocName) {
            return "Dependent";
        }

        protected CIMObjectPath getManyClass(CIMObjectPath assocName) {
            return CPA_CLASS_OP;
        }

        protected CIMObjectPath getOneClass(CIMObjectPath assocName) {
            return CIMOMProvider.CLASSOP;
        }
    }

    // This class implements a one to many association between the CIMOM and
    // its communciation mechanisms. 
    private static class CommMechAssocProvider extends OneToManyAssocProvider {
        private static final CIMObjectPath COMMMECH_CLASS_OP =
                new CIMObjectPath("CIM_ObjectManagerCommunicationMechanism",
                        CIMOMImpl.INTEROPNS);

        protected String getOneRole(CIMObjectPath assocName) {
            return "Antecedent";
        }

        protected String getManyRole(CIMObjectPath assocName) {
            return "Dependent";
        }

        protected CIMObjectPath getManyClass(CIMObjectPath assocName) {
            return COMMMECH_CLASS_OP;
        }

        protected CIMObjectPath getOneClass(CIMObjectPath assocName) {
            return CIMOMProvider.CLASSOP;
        }
    }

}
