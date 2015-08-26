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
 *are Copyright © 2002 Sun Microsystems, Inc.
 *
 *All Rights Reserved.
 *
 *Contributor(s): _______________________________________
 */

package org.wbemservices.wbem.cimom;

import java.util.Enumeration;
import java.util.Vector;
import java.util.List;
import java.util.ArrayList;

import javax.wbem.client.CIMOMHandle;
import javax.wbem.client.Debug;
import javax.wbem.client.ProviderCIMOMHandle;
import javax.wbem.cim.CIMClass;
import javax.wbem.cim.CIMObjectPath;
import javax.wbem.cim.CIMProperty;
import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMInstance;
import javax.wbem.cim.CIMValue;

import javax.wbem.provider.CIMInstanceProvider;

/**
 *
 * This class is the provider for CIM_Namespace class. It uses the 
 * enumerateNameSpace method to get the available instances of namespace. 
 * Properties lise ClassInfo, DescriptionOfClassInfo are extracted from the
 * repository, by nulling out the propagated keys.
 *
 * @author     	Sun Microsystems, Inc.
 * @version	1.0 11/20/01
 * @since	WBEM 2.5
 */
class NamespaceProvider implements CIMInstanceProvider {
    // private fields
    private ProviderCIMOMHandle pch = null;
    private CIMInstanceProvider intInstProvider = null;
    // This is package protected, so that other adapters that associate
    // the namespace to its configurations can get its class name.
    static final CIMObjectPath CLASSOP = 
    new CIMObjectPath("CIM_Namespace", CIMOMImpl.INTEROPNS);
    private static final CIMValue CCNValue = new CIMValue("CIM_Namespace");
    // property names
    private static final String CCN = "CreationClassName";
    private static final String NAME = "Name";
    private static final String SYSNAME = "SystemName";
    private static final String SYSCCN = "SystemCreationClassName";
    private static final String OMCCN = "ObjectManagerCreationClassName";
    private static final String OMNAME = "ObjectManagerName";
    private static final String CLASSINFO = "ClassInfo";
    private static final String CLASSINFODESC = "DescriptionOfClassInfo";
    private static final String CAPTION = "Caption";
    private static final String DESCRIPTION = "Description";

    /**
     * This method generates a CIM_Namespace instance by combining the
     * propagated keys from the CIMOM instance with the namespace information.
     * @param cimomInstance The instance of the containing CIMOM.
     * @param namespace     The namespace name returned as a result of
     *                      an enumNameSpace call.
     * @param cc            The CIM_Namespace class.
     * @param classOp       The object path to cc. The namespace information
     *                      in op is used to populate the generated object
     * @return              CIMInstance representing the CIM_Namespace.
     */
    private CIMInstance generateInstance(CIMInstance cimomInstance,
    CIMObjectPath namespace, CIMClass cc, CIMObjectPath classOp) {
        CIMInstance ci = cc.newInstance();
        // Set the local keys
        try {
            ci.setProperty(CCN, CCNValue);
            ci.setProperty(NAME, new CIMValue(namespace.getNameSpace()));
        } catch (CIMException ex) {
            //Don't ever expect to get here
            Debug.trace1("Exception trying to setProperties", ex);
        }
        
        // Check if we have an instance in the repository with the local keys
        // set. This instance could have been created during mofregistry,
        // product build or as a result of a createInstance. The propagated
        // keys are purposefully not populated in this repository instance,
        // because they may not be available during mofreg/build time.
        CIMObjectPath op = ci.getObjectPath();
        op.setNameSpace(classOp.getNameSpace());
        CIMInstance repositoryInstance = null;
        try {
            repositoryInstance =
                intInstProvider.getInstance(op, false, false, true, null, cc);
        } catch (CIMException ce) {
            // ignore the exception.
        }
        try {
            if (repositoryInstance != null) {
               // extract the stored properties.
                ci.setProperty(CLASSINFO, 
                repositoryInstance.getProperty(CLASSINFO).getValue());
                ci.setProperty(CLASSINFODESC, 
                repositoryInstance.getProperty(CLASSINFODESC).getValue());
                ci.setProperty(CAPTION, 
                repositoryInstance.getProperty(CAPTION).getValue());
                ci.setProperty(DESCRIPTION, 
                repositoryInstance.getProperty(DESCRIPTION).getValue());
            }

            // Set the propagated keys
            ci.setProperty(SYSCCN, cimomInstance.getProperty(SYSCCN).getValue());
            ci.setProperty(SYSNAME, cimomInstance.getProperty(SYSNAME).getValue());
            ci.setProperty(OMCCN, cimomInstance.getProperty(CCN).getValue());
            ci.setProperty(OMNAME, cimomInstance.getProperty(NAME).getValue());
        } catch (CIMException ex) {
            //Don't ever expect to get here
            Debug.trace1("Exception trying to setProperties", ex);
        }
        return ci;
    }

    /**
     * This method generates a CIM_Namespace object path by combining the
     * propagated keys from the CIMOM instance with the namespace information.
     * @param cimomInstance The instance of the containing CIMOM.
     * @param namespace     The namespace name returned as a result of
     *                      an enumNameSpace call.
     * @param cc            The CIM_Namespace class.
     * @param op            The object path to cc. The namespace information
     *                      in op is used to populate the generated object
     *                      path.
     * @return              CIMObjectPath representing the CIM_Namespace name.
     */
    private CIMObjectPath generateInstanceName(CIMInstance cimomInstance,
    CIMObjectPath namespace, CIMClass cc, CIMObjectPath op) {
        CIMInstance ci = cc.newInstance();
        try {
            ci.setProperty(SYSCCN, 
                cimomInstance.getProperty(SYSCCN).getValue());
            ci.setProperty(SYSNAME, 
                cimomInstance.getProperty(SYSNAME).getValue());
            ci.setProperty(OMCCN, 
                cimomInstance.getProperty(CCN).getValue());
            ci.setProperty(OMNAME, 
                cimomInstance.getProperty(NAME).getValue());
            ci.setProperty(CCN, CCNValue);
            ci.setProperty(NAME, new CIMValue(namespace.getNameSpace()));
        }
        catch(CIMException ex) {
            //Don't ever expect to get here
            Debug.trace1("Exception trying to setProperties", ex);
        }
        CIMObjectPath retOp = ci.getObjectPath();
        retOp.setNameSpace(op.getNameSpace());
        return retOp;
    }

    /**
     * This method populates an instance but ignores the propagated keys.
     * @param op  Namespace for the instance
     * @param ci  Source CIMInstance
     * @return    CIMInstance that has been populated with the desired info.
     */
    private CIMInstance populateInstance(CIMObjectPath op, CIMInstance ci,
    boolean includeQualifier, String[] propertyList)
    throws CIMException {
	op.setObjectName(ci.getClassName());
	CIMClass cc = pch.getClass(op, false, true, true, null);
	CIMInstance nci = cc.newInstance();
	// We purposely ignore the non local keys, to match the state that
	// would exist during mofreg/build time.
	nci.setProperty(CLASSINFO, 
	    ci.getProperty(CLASSINFO).getValue());
	nci.setProperty(CLASSINFODESC, 
	    ci.getProperty(CLASSINFODESC).getValue());
	nci.setProperty(CAPTION, ci.getProperty(CAPTION).getValue());
	nci.setProperty(DESCRIPTION, 
	    ci.getProperty(DESCRIPTION).getValue());
	// set the local keys
	String name = (String)ci.getProperty(NAME).getValue().getValue();
	// Make this a canonical name
	CIMObjectPath canonOp = new CIMObjectPath("", "/" + name);
	CIMValue nsValue = new CIMValue(canonOp.getNameSpace());
	nci.setProperty(NAME, nsValue);
	nci.setProperty(CCN, CCNValue);
	return nci;
    }


    public void initialize(CIMOMHandle ch) {
	pch = (ProviderCIMOMHandle)ch;
	intInstProvider = pch.getInternalCIMInstanceProvider();
    }

    public void cleanup() {
	// Nothing to do.
    }

    public CIMObjectPath[] enumerateInstanceNames(CIMObjectPath op, 
					 	  CIMClass cc) 
    throws CIMException {
	
	// We assume theres just one instance of CIMOM
	CIMInstance cimomInstance = 
	(CIMInstance)pch.enumerateInstances(CIMOMProvider.CLASSOP,
		true, false, true, true,
		null).nextElement();

	Enumeration e = pch.enumNameSpace(new CIMObjectPath(), true);
	List nameList = new ArrayList();
	// Enumerate the namespaces and generate appropriate instance names
	while (e.hasMoreElements()) {
	    CIMObjectPath nsOp = (CIMObjectPath)e.nextElement();
	    CIMObjectPath nsName = 
	    generateInstanceName(cimomInstance, nsOp, cc, op);
	    nameList.add(nsName);
	}

	CIMObjectPath[] nameArray = new CIMObjectPath[nameList.size()];
	return (CIMObjectPath[])nameList.toArray(nameArray);

    }

    public CIMInstance[] enumerateInstances(CIMObjectPath op, 
					    boolean localOnly, 
					    boolean includeQualifiers, 
					    boolean includeClassOrigin, 
					    String[] propertyList, 
					    CIMClass cc) 
    throws CIMException {
	// We assume theres just one instance of CIMOM
	CIMInstance cimomInstance = 
	(CIMInstance)pch.enumerateInstances(CIMOMProvider.CLASSOP,
		true, false, true, true,
		null).nextElement();

	Enumeration e = pch.enumNameSpace(new CIMObjectPath(), true);
	List instList = new ArrayList();

	// Enumerate the namespaces and generate appropriate instances
	while (e.hasMoreElements()) {
	    CIMObjectPath nsOp = (CIMObjectPath)e.nextElement();
	    CIMInstance ci = generateInstance(cimomInstance, nsOp, cc, op);
	    if (localOnly) {
		ci = ci.localElements();
	    }
	    ci = ci.filterProperties(propertyList, includeQualifiers,
	    includeClassOrigin);
	    instList.add(ci);
	}

	CIMInstance[] instArray = new CIMInstance[instList.size()];
	return (CIMInstance[])instList.toArray(instArray);
    }

    public CIMInstance getInstance(CIMObjectPath op, 
    				   boolean localOnly,
				   boolean includeQualifiers, 
				   boolean includeClassOrigin, 
				   String[] propertyList, 
				   CIMClass cc)
    throws CIMException {
	// Get the name prop
	Enumeration eKeys = op.getKeys().elements();
	String name = null;
	while (eKeys.hasMoreElements()) {
	    CIMProperty cp = (CIMProperty)eKeys.nextElement();
	    if (cp.getName().equalsIgnoreCase(NAME)) {
		try {
		    name = (String)cp.getValue().getValue();
		} catch (NullPointerException ex) {
		    throw new CIMException(CIMException.CIM_ERR_NOT_FOUND, op);
		}
	    }
	}
	// No name found
	if (name == null) {
	    throw new CIMException(CIMException.CIM_ERR_NOT_FOUND, op);
	}
	// First check if there is a namespace that matches the Name property
	Enumeration e = pch.enumNameSpace(new CIMObjectPath(), true);
	while (e.hasMoreElements()) {
	    CIMObjectPath nsOp = (CIMObjectPath)e.nextElement();
	    if (nsOp.getNameSpace().equals(name)) {
		// The name matches, now see if the rest of the keys match
		// Get the CIMOM instance first, to obtain the propagated keys
		CIMInstance cimomInstance = 
		(CIMInstance)pch.enumerateInstances(CIMOMProvider.CLASSOP,
			true, false, true, true,
			null).nextElement();
		// Generate the namespace instance
		CIMInstance ci = generateInstance(cimomInstance, nsOp, cc, op);
		CIMObjectPath testnsOp = ci.getObjectPath();
		testnsOp.setNameSpace(op.getNameSpace());
		if (testnsOp.equals(op)) {
		    if (localOnly) {
			ci = ci.localElements();
		    }
		    ci = ci.filterProperties(propertyList, includeQualifiers,
		    includeClassOrigin);
		    return ci;
		} else {
		    throw new CIMException(CIMException.CIM_ERR_NOT_FOUND, op);
		}
	    }
	}
	throw new CIMException(CIMException.CIM_ERR_NOT_FOUND, op);
    }

    public CIMObjectPath createInstance(CIMObjectPath op, CIMInstance ci) 
    throws CIMException {
	CIMInstance nci = populateInstance(op, ci, true, null);
	intInstProvider.createInstance(op, nci);
	return null;
    }

    public void setInstance(CIMObjectPath op, CIMInstance ci,
    boolean includeQualifier, String[] propertyList) 
    throws CIMException {
	CIMInstance nci = populateInstance(op, ci, includeQualifier,
	propertyList);
	intInstProvider.setInstance(op, nci, includeQualifier, propertyList);
    }

    public void deleteInstance(CIMObjectPath op) 
    throws CIMException {
	// Only use the local keys to remove the repository instance.
	CIMClass cc = pch.getClass(op, false, true, true, null);
	Enumeration e = op.getKeys().elements();
	CIMObjectPath delOp = new CIMObjectPath("", op.getNameSpace());
	delOp.setObjectName(op.getObjectName());
	Vector v = new Vector();
	while (e.hasMoreElements()) {
	    CIMProperty cp = (CIMProperty)e.nextElement();
	    if (cp.getName().equalsIgnoreCase(NAME)) {
		v.addElement(cp);
	    } else if (cp.getName().equalsIgnoreCase(CCN)) {
		v.addElement(cp);
	    } else {
		CIMProperty cpt = new CIMProperty(cp.getName());
		cpt.setValue(cc.getProperty(cp.getName()).getValue());
		v.addElement(cpt);
	    }
	}
	delOp.setKeys(v);
	try {
	    intInstProvider.deleteInstance(delOp);
	} catch (CIMException ce) {
	    if (ce.getID().equalsIgnoreCase(CIMException.CIM_ERR_NOT_FOUND)) {
		throw new CIMException(CIMException.CIM_ERR_NOT_FOUND, op);
	    } else {
		throw ce;
	    }
	}
    }

    public CIMInstance[] execQuery(CIMObjectPath op, 
				   String query, 
				   String ql, 
				   CIMClass cc)
    throws CIMException {
	// Let the CIMOM handle this - we will populate null as the first
	// element of the return array.
	CIMInstance[] instArray = 
	enumerateInstances(op, false, true, true, null, cc);
	CIMInstance[] instArrayRet = new CIMInstance[instArray.length + 1];
	System.arraycopy(instArray, 0, instArrayRet, 1, instArray.length);
	instArrayRet[0] = null;
	return instArrayRet;
    }
}
