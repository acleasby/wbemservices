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
  
package org.wbemservices.wbem.compiler.mofc;

import javax.wbem.client.CIMOMHandle;
import javax.wbem.client.BatchHandle;
import javax.wbem.client.BatchResult;
import javax.wbem.client.CIMListener;
import javax.wbem.cim.CIMNameSpace;
import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMObjectPath;
import javax.wbem.cim.CIMQualifierType;
import javax.wbem.cim.CIMClass;
import javax.wbem.cim.CIMInstance;
import javax.wbem.cim.CIMArgument;
import javax.wbem.cim.CIMValue;

import java.security.Principal;
import java.util.Enumeration;
import java.util.Vector;



class LocalCIMClient implements CIMOMHandle  {

    private static Object syncObj = new Object();
    private static CIMOMHandle api = null;
    private CIMNameSpace nameSpace = null;
    // We could make this configurable later
    private static String LOCALCIMOMHANDLE = 
    "org.wbemservices.wbem.cimom.ProviderClient";

    // If the parser is being invoked by another class, it will pass in
    // the CIMOMHandle api to use. From command line, api will be null.
    public LocalCIMClient(CIMNameSpace name, Principal principal,
        Object credential, CIMOMHandle api) throws CIMException {

        if (name == null) {
            name = new CIMNameSpace();
        }

        name.setNameSpace('/'+name.getNameSpace());
        nameSpace = new CIMNameSpace(name.getHost(), name.getNameSpace()); 

        synchronized (syncObj) {
            if (api == null) {
                if (LocalCIMClient.api == null) {
                    try {
                        // This handle could be configurable later.
                        Class c = Class.forName(LOCALCIMOMHANDLE);
                        LocalCIMClient.api = (CIMOMHandle)c.newInstance();
                    } catch (Exception e) {
                        throw new CIMException(CIMException.CIM_ERR_FAILED, e);
                    }
                }
            } else {
                LocalCIMClient.api = api;
            }
        }
    }

    /* see CIMOMHandle interface for the java doc */
    public synchronized void createNameSpace(CIMNameSpace ns)
        throws CIMException {
	    api.createNameSpace(ns);
    }

    /* see CIMOMHandle interface for the java doc */
    public synchronized void close() throws CIMException {
	api.close();
    }

    /* see CIMOMHandle interface for the java doc */
    public synchronized void createQualifierType(CIMObjectPath name,
						 CIMQualifierType qt)
    throws CIMException {
	CIMObjectPath absname = fixAbsObjectPath(name);
	api.createQualifierType(absname, qt);
    }

    /* see CIMOMHandle interface for the java doc */
    public synchronized void setQualifierType(CIMObjectPath name,
					      CIMQualifierType qt)
    throws CIMException {
	CIMObjectPath absname = fixAbsObjectPath(name);
	api.setQualifierType(absname, qt);
    }


    /* see CIMOMHandle interface for the java doc */
    public synchronized void createClass(CIMObjectPath name, CIMClass cc) 
        throws CIMException {
	    CIMObjectPath absname = fixAbsObjectPath(name);
	    api.createClass(absname, cc);

    }

   
    /* see CIMOMHandle interface for the java doc */
    public synchronized void setClass(CIMObjectPath name, CIMClass cc)
	throws CIMException {
	    CIMObjectPath absname = fixAbsObjectPath(name);
	    api.setClass(absname, cc);

    }

    /* see CIMOMHandle interface for the java doc */
    public synchronized CIMObjectPath createInstance(CIMObjectPath name, 
						     CIMInstance ci)
	throws CIMException {
	    CIMObjectPath absname = fixAbsObjectPath(name);
	    return api.createInstance(absname, ci);
    }

    /* see CIMOMHandle interface for the java doc */
    public synchronized void setInstance(CIMObjectPath name, CIMInstance ci)
	throws CIMException {
	    CIMObjectPath absname = fixAbsObjectPath(name);
	    api.setInstance(absname, ci);
    }

    /* see CIMOMHandle interface for the java doc */
    public synchronized void setInstance(CIMObjectPath name, CIMInstance ci,
    boolean includeQualifier, String[] propertyList)
	throws CIMException {
	    CIMObjectPath absname = fixAbsObjectPath(name);
	    api.setInstance(absname, ci, includeQualifier, propertyList);
    }

    /* see CIMOMHandle interface for the java doc */
    public Enumeration enumNameSpace(CIMObjectPath path, 
				     boolean deep) throws CIMException {
	    throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
    }


    /* see CIMOMHandle interface for the java doc */
    public Enumeration enumQualifierTypes(CIMObjectPath path)
	throws CIMException {
	    throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
    }
    
    /* see CIMOMHandle interface for the java doc */
    public CIMValue getProperty(CIMObjectPath name, String propertyName)
	throws CIMException {
	    throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
    }
     
    /* see CIMOMHandle interface for the java doc */
    public void setProperty(CIMObjectPath name, String propertyName,
			    CIMValue cv) throws CIMException {
	    throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
    }

    /* see CIMOMHandle interface for the java doc */
    public void setProperty(CIMObjectPath name, String propertyName)
			    throws CIMException {
	    throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
    }

    /* see CIMOMHandle interface for the java doc */
    public Enumeration execQuery(CIMObjectPath name, String query,
				 String queryLanguage) throws CIMException {
	    throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
    }

    public CIMInstance getInstance(CIMObjectPath name,
                                boolean localOnly,
                                boolean includeQualifiers,
                                boolean includeClassOrigin,
                                String[] propertyList)
    throws CIMException {
	throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
    }

    public CIMClass getClass(CIMObjectPath name,
                                boolean localOnly,
                                boolean includeQualifiers,
                                boolean includeClassOrigin,
                                String[] propertyList)
    throws CIMException {
	CIMObjectPath absname = fixAbsObjectPath(name);
	return api.getClass(absname, localOnly, includeQualifiers, 
			    includeClassOrigin, propertyList);
    }

    public Enumeration enumerateClassNames(CIMObjectPath path,
                                      boolean deep)
        throws CIMException {
	throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
    }

    public Enumeration enumerateClasses(CIMObjectPath path,
                                   boolean deep,
                                   boolean localOnly,
                                   boolean includeQualifiers,
                                   boolean includeClassOrigin)
        throws CIMException {
	throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
    }

    public Enumeration enumerateInstances(CIMObjectPath path,
                                boolean deep,
                                boolean localOnly,
                                boolean includeQualifiers,
                                boolean includeClassOrigin,
                                String[] propertyList)
        throws CIMException {
	throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
    }

    public Enumeration enumerateInstanceNames(CIMObjectPath path)
        throws CIMException {
	throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
    }


    /* see CIMOMHandle interface for the java doc */
    public Enumeration associators(CIMObjectPath objectName,
				String assocClass,
				String resultClass,
				String role,
				String resultRole,
				boolean includeQualifiers,
				boolean includeClassOrigin,
				String[] propertyList) throws CIMException {
	    throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
    }

    public Enumeration associators(CIMObjectPath objectName)
				throws CIMException {
	    throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
    }

    /* see CIMOMHandle interface for the java doc */
    public Enumeration associatorNames(CIMObjectPath objectName,
				String assocClass,
				String resultClass,
				String role,
				       String resultRole) throws CIMException {
	    throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
    }

    public Enumeration associatorNames(CIMObjectPath objectName)
				throws CIMException {
	    throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
    }

    /* see CIMOMHandle interface for the java doc */
    public Enumeration references(CIMObjectPath objectName,
				String resultClass,
				String role,
				boolean includeQualifiers,
				boolean includeClassOrigin,
				  String[] propertyList) throws CIMException {
	    throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
    }

    public Enumeration references(CIMObjectPath objectName)
				throws CIMException {
	    throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
    }

    /* see CIMOMHandle interface for the java doc */
    public Enumeration referenceNames(CIMObjectPath objectName,
				String resultClass,
				      String role) throws CIMException {
	    throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
    }

    public Enumeration referenceNames(CIMObjectPath objectName)
				throws CIMException {
	    throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
    }

    /* see CIMOMHandle interface for the java doc */
    public String encryptData(String clearData) throws CIMException {
	    throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
    }

    /* see CIMOMHandle interface for the java doc */
    public BatchResult performBatchOperations(BatchHandle bc) 
    		throws CIMException {

	throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
    }

    /* see CIMOMHandle interface for the java doc */
    public CIMValue invokeMethod(CIMObjectPath name,
				 String methodName,
				 Vector inParams,
				 Vector outParams)
	throws CIMException {
	    throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
    }

    /* see CIMOMHandle interface for the java doc */
    public CIMValue invokeMethod(CIMObjectPath name,
				 String methodName,
				 CIMArgument[] inArgs,
				 CIMArgument[] outArgs)
	throws CIMException {
	    throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
    }

    /* see CIMOMHandle interface for the java doc */
    public void deleteClass(CIMObjectPath path)	 
	throws CIMException {
	    CIMObjectPath absname = fixAbsObjectPath(path);
	    api.deleteClass(absname);
    }

    /* see CIMOMHandle interface for the java doc */
    public void deleteInstance(CIMObjectPath path)
	throws CIMException {
	    CIMObjectPath absname = fixAbsObjectPath(path);
	    api.deleteInstance(absname);
    }


    /* see CIMOMHandle interface for the java doc */
    public void deleteQualifierType(CIMObjectPath path)
	throws CIMException {
	    throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
    }

    /* see CIMOMHandle interface for the java doc */
    public CIMQualifierType getQualifierType(CIMObjectPath name) 
	throws CIMException {
	    throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
    }

    /* see CIMOMHandle interface for the java doc */
    public void deleteNameSpace(CIMNameSpace ns) throws CIMException {
	    throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
    }


    /* see CIMOMHandle interface for the java doc */
    public void addCIMListener(CIMListener l) throws CIMException {}

    /* see CIMOMHandle interface for the java doc */
    public void removeCIMListener(CIMListener l) throws CIMException {}

    public CIMInstance getIndicationHandler(CIMListener l) 
    throws CIMException {
	throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
    }


    private CIMObjectPath fixAbsObjectPath(CIMObjectPath op) {
	String s = nameSpace.getNameSpace() + '/' + op.getNameSpace();
	CIMObjectPath rtop =
	    new CIMObjectPath(op.getObjectName(), 
			      (Vector)op.getKeys().clone()); 
	rtop.setHost(op.getHost());
	rtop.setNameSpace(s);
	return rtop;
    }

}
