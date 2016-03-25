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
 *are Copyright ï¿½ 2001 Sun Microsystems, Inc.
 *
 *All Rights Reserved.
 *
 *Contributor(s): WBEM Solutions, Inc.
 */

package org.wbemservices.wbem.cimom;

import javax.wbem.cim.*;
import javax.wbem.client.*;
import javax.wbem.provider.CIMAssociatorProvider;
import javax.wbem.provider.CIMInstanceProvider;
import java.util.Enumeration;
import java.util.Vector;

/**
 * A handle to internal CIMOM (This handle is used by providers
 * to access the CIMOM).
 *
 * @author Sun Microsystems, Inc.
 * @since WBEM 1.0
 */
public class ProviderClient implements ProviderCIMOMHandle {

    protected CIMNameSpace nameSpace;
    protected CIMOMImpl cimom;
    private InternalProvider internalProvider;
    private CIMAssociatorProvider internalAssocProvider;

    private class InternalProvider implements CIMAssociatorProvider,
            CIMInstanceProvider {

        public void initialize(CIMOMHandle ch)
                throws CIMException {
        }

        public void cleanup() throws CIMException {
        }

        public CIMObjectPath[] enumerateInstanceNames(CIMObjectPath op,
                CIMClass cc)
                throws CIMException {
            return cimom.enumerateInstanceNames(op, cc);
        }

        public CIMInstance[] enumerateInstances(CIMObjectPath op,
                boolean localOnly,
                boolean includeQualifiers,
                boolean includeClassOrigin,
                String[] propList,
                CIMClass cc)
                throws CIMException {
            return cimom.enumerateInstances(op, localOnly,
                    includeQualifiers,
                    includeClassOrigin,
                    propList, cc);
        }

        public CIMInstance getInstance(CIMObjectPath op,
                boolean localOnly,
                boolean includeQualifiers,
                boolean includeClassOrigin,
                String[] propertyList,
                CIMClass cc)
                throws CIMException {
            return cimom.getInstance(op, localOnly, includeQualifiers,
                    includeClassOrigin, propertyList,
                    cc);
        }

        public CIMObjectPath createInstance(CIMObjectPath op, CIMInstance ci)
                throws CIMException {
            return cimom.createInstance(op, ci);
        }

        public void setInstance(CIMObjectPath op, CIMInstance ci)
                throws CIMException {
            cimom.setInstance(op, ci, true, null);
        }

        public void setInstance(CIMObjectPath op, CIMInstance ci,
                boolean includeQualifiers, String[] propertyList)
                throws CIMException {
            cimom.setInstance(op, ci, includeQualifiers, propertyList);
        }

        public void deleteInstance(CIMObjectPath op) throws CIMException {
            cimom.deleteInstance(op);
        }

        public CIMInstance[] execQuery(CIMObjectPath op,
                String query,
                String ql,
                CIMClass cc)
                throws CIMException {
            return cimom.execQuery(op, query, ql, cc);
        }

        public CIMInstance[] associators(CIMObjectPath assocName,
                CIMObjectPath objectName,
                String resultClass,
                String role,
                String resultRole,
                boolean includeQualifiers,
                boolean includeClassOrigin,
                String propertyList[]) throws CIMException {
            return cimom.associators(assocName, objectName, resultClass,
                    role, resultRole, includeQualifiers, includeClassOrigin,
                    propertyList);
        }

        public CIMObjectPath[] associatorNames(CIMObjectPath assocName,
                CIMObjectPath objectName,
                String resultClass,
                String role,
                String resultRole)
                throws CIMException {
            return cimom.associatorNames(assocName, objectName,
                    resultClass, role, resultRole);
        }

        public CIMInstance[] references(CIMObjectPath assocName,
                CIMObjectPath objectName,
                String role,
                boolean includeQualifiers,
                boolean includeClassOrigin,
                String propertyList[])
                throws CIMException {
            return cimom.references(assocName, objectName, role,
                    includeQualifiers, includeClassOrigin, propertyList);
        }

        public CIMObjectPath[] referenceNames(CIMObjectPath assocName,
                CIMObjectPath objectName,
                String role)
                throws CIMException {
            return cimom.referenceNames(assocName, objectName, role);
        }
    } //internalprovider

    private class InternalAssocProvider implements CIMAssociatorProvider {

        public void initialize(CIMOMHandle ch)
                throws CIMException {
        }

        public void cleanup() throws CIMException {
        }

        public CIMInstance[] associators(CIMObjectPath assocName,
                CIMObjectPath objectName,
                String resultClass,
                String role,
                String resultRole,
                boolean includeQualifiers,
                boolean includeClassOrigin,
                String propertyList[]) throws CIMException {

            return cimom.associators(assocName, objectName, resultClass,
                    role, resultRole, includeQualifiers, includeClassOrigin,
                    propertyList);
        }

        public CIMObjectPath[] associatorNames(CIMObjectPath assocName,
                CIMObjectPath objectName,
                String resultClass,
                String role,
                String resultRole)
                throws CIMException {
            return cimom.associatorNames(assocName, objectName,
                    resultClass, role, resultRole);
        }

        public CIMInstance[] references(CIMObjectPath assocName,
                CIMObjectPath objectName,
                String role,
                boolean includeQualifiers,
                boolean includeClassOrigin,
                String propertyList[])
                throws CIMException {

            return cimom.references(assocName, objectName, role,
                    includeQualifiers, includeClassOrigin, propertyList);
        }

        public CIMObjectPath[] referenceNames(CIMObjectPath assocName,
                CIMObjectPath objectName,
                String role)
                throws CIMException {
            return cimom.referenceNames(assocName, objectName, role);
        }
    } //internalprovider

    public ProviderClient() throws CIMException {
        try {
            this.cimom = new CIMOMImpl();
        } catch (Exception e) {
            //e.printStackTrace();
            throw new CIMException(CIMException.CIM_ERR_FAILED, e);
        }
        nameSpace = new CIMNameSpace("", "");
        internalProvider = new InternalProvider();
        internalAssocProvider = new InternalAssocProvider();
    }

    ProviderClient(CIMOMImpl cimom) throws CIMException {
        this(cimom, new CIMNameSpace("", ""));
    }

    ProviderClient(CIMOMImpl cimom, CIMNameSpace name)
            throws CIMException {
        this.cimom = cimom;
        nameSpace = new CIMNameSpace("", "");
        internalProvider = new InternalProvider();
        internalAssocProvider = new InternalAssocProvider();
    }

    public void createNameSpace(CIMNameSpace ns)
            throws CIMException {
        CIMNSStaticMethods.createNameSpace(nameSpace, ns);
    }

    public void close() throws CIMException {
        // Do nothing
    }

    public void deleteNameSpace(CIMNameSpace ns) throws CIMException {
        CIMNSStaticMethods.deleteNameSpace(nameSpace, ns);
    }

    public void deleteClass(CIMObjectPath path)
            throws CIMException {
        cimom.intdeleteClass(nameSpace, path);
    }

    public void deleteInstance(CIMObjectPath path)
            throws CIMException {
        cimom.intdeleteInstance(nameSpace, path, true);
    }

    public void deleteQualifierType(CIMObjectPath path)
            throws CIMException {

        // do nothing
    }

    public Enumeration enumNameSpace(CIMObjectPath path, boolean deep)
            throws CIMException {
        Vector v = CIMNSStaticMethods.enumNameSpace(nameSpace, path, deep);
        return v.elements();
    }

    public Enumeration enumClass(CIMObjectPath path, boolean deep)
            throws CIMException {
        Vector v = cimom.intenumClass(nameSpace, path, deep);
        return v.elements();
    }

    public Enumeration enumClass(CIMObjectPath path, boolean deep,
            boolean localOnly)
            throws CIMException {
        Vector v = cimom.intenumClass(nameSpace, path, deep, localOnly);
        return v.elements();
    }

    public Enumeration enumInstances(CIMObjectPath path, boolean deep)
            throws CIMException {

        Vector v = cimom.intenumInstances(nameSpace, path, deep, true);
        return v.elements();
    }

    public Enumeration enumInstances(CIMObjectPath path,
            boolean deep, boolean localOnly)
            throws CIMException {

        Vector v = cimom.intenumInstances(nameSpace, path, deep, localOnly, true, null);
        return v.elements();
    }

    public Enumeration enumQualifierTypes(CIMObjectPath path)
            throws CIMException {
        Vector v = CIMQtypeStaticMethods.enumQualifierTypes(nameSpace, path);
        return v.elements();
    }

    public CIMClass getClass(CIMObjectPath name, boolean localOnly)
            throws CIMException {
        CIMClass cc = cimom.intgetClass(nameSpace, name, localOnly);
        if (cc == null) {
            throw new CIMClassException(CIMException.CIM_ERR_NOT_FOUND,
                    name.getObjectName());
        }
        return cc;

    }

    public CIMInstance getInstance(CIMObjectPath name, boolean localOnly)
            throws CIMException {
        CIMInstance ci = cimom.intgetInstance(nameSpace, name, localOnly,
                true, true, null, true);
        if (ci == null) {
            throw new CIMInstanceException(CIMException.CIM_ERR_NOT_FOUND,
                    name.toString());
        }
        return ci;
    }

    public CIMValue getProperty(CIMObjectPath name, String propertyName)
            throws CIMException {
        return cimom.intgetProperty(nameSpace, name, propertyName, true);
    }

    public CIMValue invokeMethod(CIMObjectPath name, String methodName,
            Vector inParams,
            Vector outParams)
            throws CIMException {
        Vector v = cimom.intinvokeMethod(nameSpace, name, methodName,
                inParams, null, true);
        Enumeration e = v.elements();
        CIMValue retVal = (CIMValue) e.nextElement();
        while (e.hasMoreElements()) {
            outParams.addElement(e.nextElement());
        }
        return retVal;
    }

    public CIMValue invokeMethod(CIMObjectPath name, String methodName,
            CIMArgument[] inArgs,
            CIMArgument[] outArgs)
            throws CIMException {
        Vector v = cimom.intinvokeMethod(nameSpace, name, methodName,
                null, inArgs, true);
        Enumeration e = v.elements();
        CIMValue retVal = (CIMValue) e.nextElement();
        int count = 0;
        while (e.hasMoreElements()) {
            outArgs[count] = (CIMArgument) e.nextElement();
        }
        return retVal;
    }

    public CIMQualifierType getQualifierType(CIMObjectPath name)
            throws CIMException {
        CIMQualifierType qt =
                CIMQtypeStaticMethods.getQualifierType(nameSpace, name);

        if (qt == null) {
            throw new CIMQualifierTypeException(
                    CIMException.CIM_ERR_NOT_FOUND, name.getObjectName());
        }

        return qt;
    }

    public void setQualifierType(CIMObjectPath name, CIMQualifierType qt)
            throws CIMException {
        CIMQtypeStaticMethods.setCIMElement(nameSpace, name, qt);
    }

    public void createQualifierType(CIMObjectPath name, CIMQualifierType qt)
            throws CIMException {
        CIMQtypeStaticMethods.addCIMElement(nameSpace, name, qt);
    }

    public void setClass(CIMObjectPath name, CIMClass cc)
            throws CIMException {
        cimom.intsetCIMElement(nameSpace, name, cc);
    }

    public void createClass(CIMObjectPath name, CIMClass cc)
            throws CIMException {
        cimom.intaddCIMElement(nameSpace, name, cc);
    }

    public void setInstance(CIMObjectPath name, CIMInstance ci)
            throws CIMException {
        cimom.intsetCIMElement(nameSpace, name, ci, true, true, null, true);
    }

    public void setInstance(CIMObjectPath name, CIMInstance ci,
            boolean includeQualifier, String[] propertyList)
            throws CIMException {
        cimom.intsetCIMElement(nameSpace, name, ci, true, includeQualifier,
                propertyList, true);
    }

    public CIMObjectPath createInstance(CIMObjectPath name, CIMInstance ci)
            throws CIMException {
        return cimom.intaddCIMElement(nameSpace, name, ci, true);
    }

    public void setProperty(CIMObjectPath name, String propertyName,
            CIMValue newValue) throws CIMException {
        cimom.intsetProperty(nameSpace, name, propertyName, newValue, true);
    }

    public void setProperty(CIMObjectPath name, String propertyName)
            throws CIMException {
        setProperty(name, propertyName, null);
    }

    public Enumeration execQuery(CIMObjectPath op, String query, String ql)
            throws CIMException {
        return cimom.intexecQuery(nameSpace, op, query, ql, true).
                elements();
    }

    public Enumeration enumerateInstances(CIMObjectPath path,
            boolean deep,
            boolean localOnly,
            boolean includeQualifiers,
            boolean includeClassOrigin,
            String propertyList[])
            throws CIMException {

        return cimom.intenumInstances(nameSpace, path, true,
                localOnly, false, null).elements();

    }

    public Enumeration enumerateInstanceNames(CIMObjectPath path)
            throws CIMException {

        return cimom.intenumInstances(nameSpace, path,
                true, false).elements();
    }

    public Enumeration enumerateClassNames(CIMObjectPath path,
            boolean deep)
            throws CIMException {

        return cimom.intenumClass(nameSpace, path, deep).elements();
    }

    public Enumeration enumerateClasses(CIMObjectPath path,
            boolean deep,
            boolean localOnly,
            boolean includeQualifiers,
            boolean includeClassOrigin)
            throws CIMException {

        return cimom.intenumClass(nameSpace, path, deep,
                localOnly).elements();
    }

    public CIMClass getClass(CIMObjectPath path,
            boolean localOnly,
            boolean includeQualifiers,
            boolean includeClassOrigin,
            String propertyList[])
            throws CIMException {

        CIMClass cc = cimom.intgetClass(nameSpace, path, localOnly);
        if (cc == null) {
            throw new CIMClassException(CIMException.CIM_ERR_NOT_FOUND,
                    path.getObjectName());
        }

        return cc.filterProperties(null, includeQualifiers,
                includeClassOrigin);
    }

    public CIMInstance getInstance(CIMObjectPath path,
            boolean localOnly,
            boolean includeQualifiers,
            boolean includeClassOrigin,
            String propertyList[])
            throws CIMException {

        CIMInstance ci = cimom.intgetInstance(nameSpace, path, localOnly,
                includeQualifiers, includeClassOrigin, propertyList, false);
        if (ci == null) {
            throw new CIMInstanceException(CIMException.CIM_ERR_NOT_FOUND,
                    path.toString());
        }
        return ci.filterProperties(propertyList, includeQualifiers,
                includeClassOrigin);
    }

    public Enumeration associators(CIMObjectPath objectName,
            String assocClass,
            String resultClass,
            String role,
            String resultRole,
            boolean includeQualifiers,
            boolean includeClassOrigin,
            String propertyList[])
            throws CIMException {
        return cimom.intassociators(nameSpace, objectName,
                assocClass, resultClass, role, resultRole, includeQualifiers,
                includeClassOrigin, propertyList, true).elements();

    }

    public Enumeration associators(CIMObjectPath objectName)
            throws CIMException {
        return associators(objectName, null, null, null, null,
                true, true, null);
    }

    public Enumeration associatorNames(CIMObjectPath objectName,
            String assocClass,
            String resultClass,
            String role,
            String resultRole)
            throws CIMException {
        return cimom.intassociatorNames(nameSpace, objectName,
                assocClass, resultClass, role, resultRole, true).elements();
    }

    public Enumeration associatorNames(CIMObjectPath objectName)
            throws CIMException {
        return associatorNames(objectName, null, null, null, null);
    }

    public Enumeration references(CIMObjectPath objectName,
            String resultClass,
            String role,
            boolean includeQualifiers,
            boolean includeClassOrigin,
            String propertyList[])
            throws CIMException {
        return cimom.intreferences(nameSpace, objectName,
                resultClass, role, includeQualifiers, includeClassOrigin,
                propertyList, true).elements();
    }

    public Enumeration references(CIMObjectPath objectName)
            throws CIMException {
        return references(objectName, null, null, true, true, null);
    }

    public Enumeration referenceNames(CIMObjectPath objectName,
            String resultClass,
            String role)
            throws CIMException {
        return cimom.intreferenceNames(nameSpace, objectName,
                resultClass, role, true).elements();
    }

    public Enumeration referenceNames(CIMObjectPath objectName)
            throws CIMException {
        return referenceNames(objectName, null, null);
    }

    public void addCIMListener(javax.wbem.client.CIMListener l) throws CIMException {
        return;
    }

    public void removeCIMListener(javax.wbem.client.CIMListener l) throws CIMException {
        return;
    }

    public CIMInstanceProvider getInternalCIMInstanceProvider() {
        return internalProvider;
    }

    public CIMAssociatorProvider getInternalCIMAssociatorProvider() {
        return internalAssocProvider;
    }

    public String getCurrentUser() {
        CommonServerSecurityContext ss = ServerSecurity.getRequestSession();
        if (ss == null) {
            return null;
        }
        return (ss.getUserName());
    }

    public String getCurrentRole() {
        CommonServerSecurityContext ss = ServerSecurity.getRequestSession();
        if (ss == null) {
            return null;
        }
        return (ss.getRoleName());
    }

    public String getCurrentClientHost() {
        CommonServerSecurityContext ss = ServerSecurity.getRequestSession();
        if (ss == null) {
            return null;
        }
        return (ss.getClientHostName());
    }

    public int getCurrentAuditId() {
        CommonServerSecurityContext ss = ServerSecurity.getRequestSession();
        if (ss == null) {
            return (0);
        }
        return (ss.getAuditId());
    }

    public String decryptData(String inData) {

        String data = null;

        // If the input value is null or not an encrypted string,
        // just return the input value.  No decryption necessary.
        if ((inData == null) || (inData.charAt(0) != '<')) {
            return inData;
        }

        // Get the common security context object and try to caste it
        // as a WBEM ServerSecurity instance.  If caste fails, return null.
        CommonServerSecurityContext ss = ServerSecurity.getRequestSession();
        ServerSecurity ss2 = null;
        try {
            ss2 = (ServerSecurity) ss;
        } catch (Exception ex) {
            return null;
        }

        // Strip off brackets and decrypt the hexadecimal string
        int idx = inData.length() - 1;
        if (inData.charAt(idx) != '>') {
            data = inData.substring(1);
        } else {
            data = inData.substring(1, idx);
        }
        return (ss2.trans51Unformat(data));

    }

    // Need implementation for interface.  However, providers
    // cannot encrypt data, only decrypt.  Throw an exception!
    public String encryptData(String inData) throws CIMException {
        String data = null;
        if (data == null) {
            throw new CIMException(
                    CIMException.CIM_ERR_NOT_SUPPORTED);
        }
        return (null);
    }

    public void deliverEvent(String namespace, CIMInstance indication) {
        EventService.eventService.deliverEvent(namespace, indication);
    }

    public void deliverEvent(CIMInstance indication,
            CIMObjectPath[] matchedFilterOps) {
        EventService.eventService.deliverEvent(indication, matchedFilterOps);
    }

    /*
     * Executes the list of batch operations listed in the
     * BatchHandle object.
     * The method returns a BatchResult object that contains
     * the return values of each of the operations that were
     * executed. The operations are executed in the order in which they were
     * invoked 
     *
     * @returns	BatchResult This object contains the results of the
     * corresponding batch-mode operations invoked earlier. 
     *
     * @param  bc The handle to a batch object which contains the list
     * of operations to be exectuted.
     *
     * @exception	If the list of operations to be executed is empty.
     */
    public BatchResult performBatchOperations(BatchHandle bc)
            throws CIMException {
        throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
    }

    public CIMInstance getIndicationHandler(CIMListener cl) throws
            CIMException {
        throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
    }
}
