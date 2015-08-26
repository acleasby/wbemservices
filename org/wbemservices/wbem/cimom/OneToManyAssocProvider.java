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

import java.util.Vector;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import javax.wbem.cim.CIMClass;
import javax.wbem.cim.CIMObjectPath;
import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMInstance;
import javax.wbem.cim.CIMProperty;
import javax.wbem.cim.CIMValue;
import javax.wbem.client.CIMOMHandle;
import javax.wbem.client.ProviderCIMOMHandle;
import javax.wbem.provider.CIMAssociatorProvider;
import javax.wbem.provider.CIMInstanceProvider;

/**
 * This abstract class provides a skeletal implementation of the 
 * InstanceProvider interface to make it easer to implement an InstanceProvider
 * that deals with a single instance. Subclasses must implement the 
 * <code>getSingletonInstance</code> and <code>getInstanceName</code> methods 
 * to be concrete.
 *
 * @author     	Sun Microsystems, Inc.
 * @version	1.0 11/06/01
 * @since	WBEM 2.5
 */
public abstract class OneToManyAssocProvider implements CIMInstanceProvider,
CIMAssociatorProvider {

    // private fields
    private ProviderCIMOMHandle pch = null;
    private CIMInstanceProvider intInstProvider = null;

    /**
     * Retrieve the stored <code>CIMOMHandle</code>.
     * @return <code>ProviderCIMOMHandle</code> which has been stored during the
     * initialize routine.
     */
    protected ProviderCIMOMHandle getCIMOMHandle() {
	return pch;
    }

    /**
     * Retrieve the internal instance provider associated with the 
     * <code>CIMOMHandle</code>. This instance provider can be used
     * by the provider to store and retrieve instances from the repository.
     * @return <code>InstanceProvider</code> which is the internal provider 
     * associated with the <code>CIMOMHandle</code>
     */
    protected CIMInstanceProvider getCIMInstanceProvider() {
	return intInstProvider;
    }

    /**
     * This method initializes the provider. It stores the CIMOMHandle and
     * Internal provider in private fields and can be retrieved using the 
     * getCIMOMHandle, getInstanceProvider methods. 
     * Subclasses should override this method to do any custom handling and 
     * call super.initialize if they want the superclass to handle 
     * <code>getCIMOMHandle</code> and <code>getInstanceProvider</code> methods
     *
     * @param ch the </code>ProviderCIMOMHandle</code> that must be stored.
     * @throws ClassCastException if the <code>CIMOMHandle</code> is not a 
     * ProviderCIMOMHandle. No <code>CIMException</code> is thrown, subclasses 
     * may throw a CIMException.
     */
    public void initialize(CIMOMHandle ch) throws CIMException {
	pch = (ProviderCIMOMHandle)ch;
	intInstProvider = pch.getInternalCIMInstanceProvider();
    }

    /**
     * Does nothing. The subclass must override this method if it needs
     * any special cleanup actions.
     */
    public void cleanup() throws CIMException {
    }

    protected abstract String getOneRole(CIMObjectPath assocName);

    protected abstract String getManyRole(CIMObjectPath assocName);
    
    protected abstract CIMObjectPath getManyClass(CIMObjectPath assocName);

    protected abstract CIMObjectPath getOneClass(CIMObjectPath assocName);

    private CIMInstance generateInstance(CIMObjectPath ref1, 
        CIMObjectPath ref2, CIMClass cc, CIMObjectPath assocName) {
        CIMInstance ci = cc.newInstance();
        try {
            ci.setProperty(getOneRole(assocName), new CIMValue(ref1));
        } catch (CIMException ex) {
	       // Ignore property not found exceptions, these may have
	       // been removed for filtering
           if(ex.getID() != CIMException.CIM_ERR_NO_SUCH_PROPERTY) {
               //?? Finish me
           }
        }
        try {
            ci.setProperty(getManyRole(assocName), new CIMValue(ref2));
        } catch (CIMException ex) {
            // Ignore property not found exceptions, these may have
            // been removed for filtering
            if(ex.getID() != CIMException.CIM_ERR_NO_SUCH_PROPERTY) {
                //?? Finish me
            }
        }
        return ci;
    }

    // This method enumerates the single CIMOM instance, and associates
    // it with each enumerated client protocol adapter.
    public CIMObjectPath[] enumerateInstanceNames(CIMObjectPath op, 
						  CIMClass cc)
    throws CIMException {
	// Get the name of the CIMOM. We assume there is only one instance
	// since this is a one to many association.
	CIMObjectPath cimomName = 
	(CIMObjectPath)pch.
			enumerateInstanceNames(getOneClass(op)).nextElement();

	CIMObjectPath adapterClassName = getManyClass(op);
	// Get the names of all the client protocol adapters
	Enumeration e = pch.enumerateInstanceNames(adapterClassName);
	List namesList = new ArrayList();
	while (e.hasMoreElements()) {
	    CIMObjectPath dependent = (CIMObjectPath)e.nextElement();
	    CIMObjectPath iName = 
	    // Associate the adapter to the instance.
	    generateInstance(cimomName, dependent, cc, op).getObjectPath();
	    iName.setNameSpace(op.getNameSpace());
	    namesList.add(iName);
	}
	CIMObjectPath[] namesArray = new CIMObjectPath[namesList.size()];
	return (CIMObjectPath[])namesList.toArray(namesArray);
    }

    // This method enumerates the single CIMOM instance, and associates
    // it with each enumerated client protocol adapter.
    public CIMInstance[] enumerateInstances(CIMObjectPath op, 
					    boolean localOnly,
					    boolean includeQualifiers,
					    boolean includeClassOrigin,
					    String[] propList,
					    CIMClass cc)
    throws CIMException {
	// Get the name of the CIMOM. We assume there is only one instance
	// since this is a one to many association.
	CIMObjectPath cimomName = 
	(CIMObjectPath)pch.
			enumerateInstanceNames(getOneClass(op)).nextElement();

	CIMObjectPath adapterClassName = getManyClass(op);
	Enumeration e = pch.enumerateInstanceNames(adapterClassName);
	List instanceList = new ArrayList();

	if (localOnly) {
	    cc = cc.localElements();
	}
	cc = cc.filterProperties(propList, includeQualifiers, 
	includeClassOrigin);

	while (e.hasMoreElements()) {
	    CIMObjectPath dependent = (CIMObjectPath)e.nextElement();
	    CIMInstance ci = 
	    generateInstance(cimomName, dependent, cc, op);
	    instanceList.add(ci);
	}
	CIMInstance[] instanceArray = new CIMInstance[instanceList.size()];
	return (CIMInstance[])instanceList.toArray(instanceArray);
    }

    public CIMInstance getInstance(CIMObjectPath op, 
				   boolean localOnly, 
				   boolean includeQualifiers, 
				   boolean includeClassOrigin, 
				   String[] propList,	
				   CIMClass cc)
    throws CIMException {
	Enumeration e = op.getKeys().elements();
	CIMObjectPath antecedent = null;
	CIMObjectPath dependent = null;
	while (e.hasMoreElements()) {
	    CIMProperty cp = (CIMProperty)e.nextElement();
	    if (cp.getName().equalsIgnoreCase(getOneRole(op))) {
		// We'll just let the Null pointer exception be rethrown
		antecedent = (CIMObjectPath)cp.getValue().getValue();
	    }
	    if (cp.getName().equalsIgnoreCase(getManyRole(op))) {
		dependent = (CIMObjectPath)cp.getValue().getValue();
	    }
	}
	// Check if the antecedent and dependent exist. The methods
	// will throw a CIM_ERR_NOT_FOUND if they dont exist.
	try {
	    pch.getInstance(antecedent, true, false, false, null);
	    pch.getInstance(dependent, true, false, false, null);
	} catch (CIMException ex) {
	    throw new CIMException(CIMException.CIM_ERR_NOT_FOUND, op);
	}

	CIMInstance ci = 
	generateInstance(antecedent, dependent, cc, op);
	if (localOnly) {
	    ci = ci.localElements();
	}
	ci = ci.filterProperties(propList, includeQualifiers,
	includeClassOrigin);
	return ci;
    }

    public CIMObjectPath createInstance(CIMObjectPath op, CIMInstance ci) 
    throws CIMException {
	throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
    }

    // We will not support updating the entries for now.
    public synchronized void setInstance(CIMObjectPath op, CIMInstance ci,
    boolean includeQualifier, String[] propertyList) 
    throws CIMException {
	throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
    }

    public synchronized void deleteInstance(CIMObjectPath op) 
    throws CIMException {
	throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
    }

    public CIMInstance[] execQuery(CIMObjectPath op, String query, 
				   String ql, CIMClass cc) 
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

    // This method makes sure that the passed in roles match the
    // appropriate role names.
    private boolean checkRoles(String oneRole, String manyRole, 
    CIMObjectPath assocName) {
	if ((oneRole != null) && 
	(!oneRole.equalsIgnoreCase(getOneRole(assocName)))) {
	    return false;
	}
	if ((manyRole != null) && 
	(!manyRole.equalsIgnoreCase(getManyRole(assocName)))) {
	    return false;
	}
	return true;
    }

    private Vector associatorTraversal(CIMObjectPath assocName,
			    CIMObjectPath objectName,
			    String resultClass,
			    String role,
			    String resultRole,
			    boolean includeQualifiers,
			    boolean includeClassOrigin,
			    String[] propertyList,
			    boolean names) throws CIMException {
	// The checking for resultClass can be made more efficient, by first
	// finding the intersection between the enumerated class/subclasses
	// and resultClass/subclasses.
	// to find an intersection between the 
	Map resultClasses = null;
	if (resultClass != null) {
	    // Find the resultClasses
	    resultClasses = new HashMap();
	    resultClasses.put(resultClass.toLowerCase(), "");
	    CIMObjectPath resultClassOp = new CIMObjectPath(resultClass,
						assocName.getNameSpace());
	    Enumeration e = pch.enumerateClassNames(resultClassOp, true);
	    while (e.hasMoreElements()) {
		String className = 
		((CIMObjectPath)e.nextElement()).getObjectName();
		resultClasses.put(className.toLowerCase(), "");
	    }
	}
	// Check if the instance is present.
	pch.getInstance(objectName, true, false, false, null);
	if (objectName.getObjectName().equalsIgnoreCase(
	getOneClass(assocName).getObjectName())) {
	    // We are dealing with the CIM object manager
	    if (!checkRoles(role, resultRole, assocName)) {
		return new Vector();
	    }

	    // role and resultRole check out fine. The associated instances
	    // are all the instances of getManyRole() ref class.
	    CIMObjectPath adapterClassName = getManyClass(assocName);

	    Enumeration e;
	    if (names) {
		e = pch.enumerateInstanceNames(adapterClassName);
	    } else {
		e = pch.enumerateInstances(adapterClassName, true, false,
		includeQualifiers, includeClassOrigin, propertyList);
	    }

	    Vector v = new Vector();
	    while (e.hasMoreElements()) {
		Object o = e.nextElement();
		if (resultClass != null) {
		    // Check if the class is a subclass of resultClass.
		    String className;
		    if (names) {
			className = ((CIMObjectPath)o).getObjectName();
		    } else {
			className = ((CIMInstance)o).getClassName();
		    }
		    if (resultClasses.get(className.toLowerCase()) != null) {
			// Ok the current object is a resultClass object
			v.addElement(o);
		    }
		} else {
		    v.addElement(o);
		}
	    }
	    return v;
	} else {
	    // We are dealing with an adapter.
	    checkRoles(resultRole, role, assocName);
	    // role and resultRole check out fine. The associated instances
	    // are all the instances of getOneRole() ref class.
	    Object result;

	    if (names) {
		result = 
		pch.enumerateInstanceNames(getOneClass(assocName)).
		nextElement();
	    } else {
		result =
		(CIMInstance)pch.enumerateInstances(getOneClass(assocName),
		true, false, includeQualifiers, includeClassOrigin,
		propertyList).nextElement();
	    }

	    Vector v = new Vector();
	    if (resultClass != null) {
		// Check if the class is a subclass of resultClass.
		String className;
		if (names) {
		    className = ((CIMObjectPath)result).getObjectName();
		} else {
		    className = ((CIMInstance)result).getClassName();
		}
		if (resultClasses.get(className.toLowerCase()) != null) {
		    // Ok the current object is a resultClass object
		    v.addElement(result);
		}
	    } else {
		v.addElement(result);
	    }
	    return v;
	}
    }

    public CIMInstance[] associators(CIMObjectPath assocName,
			    CIMObjectPath objectName,
			    String resultClass,
			    String role,
			    String resultRole,
			    boolean includeQualifiers,
			    boolean includeClassOrigin,
			    String[] propertyList) throws CIMException {
	Vector v = associatorTraversal(assocName, objectName, resultClass,
	role, resultRole, includeQualifiers, includeClassOrigin,
	propertyList, false);
	CIMInstance[] instArray = new CIMInstance[v.size()];
	return (CIMInstance[])v.toArray(instArray);
    }

    public CIMObjectPath[] associatorNames(CIMObjectPath assocName,
				       CIMObjectPath objectName,
				       String resultClass,
				       String role,
				       String resultRole) 
    throws CIMException {
	Vector v = associatorTraversal(assocName, objectName, resultClass,
	role, resultRole, false, false, null, true);
	CIMObjectPath[] opArray = new CIMObjectPath[v.size()];
	return (CIMObjectPath[])v.toArray(opArray);
    }

    private Vector referenceTraversal(CIMObjectPath assocName,
			    CIMObjectPath objectName,
			    String role,
			    boolean includeQualifiers,
			    boolean includeClassOrigin,
			    String[] propertyList,
			    boolean names) throws CIMException {
	// Check if the instance is present.
	pch.getInstance(objectName, true, false, false, null);
	// Get the association class
	CIMClass cc = pch.getClass(assocName, false, true, true, null);

	if (objectName.getObjectName().equalsIgnoreCase(
	getOneClass(assocName).getObjectName())) {
	    // We are dealing with the CIM object manager
	    if ((role != null) && 
	    !role.equalsIgnoreCase(getOneRole(assocName))) {
		// The CIMOM only plays the getOneRole() role 
		return new Vector();
	    }

	    // role checks out fine. Since this is a one to many association
	    // the result is the same as the enumeration.

	    Vector v = new Vector();
	    if (names) {
		CIMObjectPath[] opArray = 
		enumerateInstanceNames(assocName, cc);
		for (int i = 0; i < opArray.length; i++) {
		    v.addElement(opArray[i]);
		}
	    } else {
		CIMInstance[] ciArray = 
		enumerateInstances(assocName, false, includeQualifiers,
		includeClassOrigin, propertyList, cc);
		for (int i = 0; i < ciArray.length; i++) {
		    v.addElement(ciArray[i]);
		}
	    }
	    return v;
	} else {
	    // We are dealing with an adapter.
	    if ((role != null) && 
	    !role.equalsIgnoreCase(getManyRole(assocName))) {
		// The CIMOM only plays the getOneRole() role 
		return new Vector();
	    }

	    // role checks out fine. Since this is a one to many association
	    // the result is the association to the object manager.
	    CIMObjectPath cimomName = (CIMObjectPath)
		pch.enumerateInstanceNames(getOneClass(assocName)).
		nextElement();

	    Object result;
	    CIMInstance resultInstance =
		generateInstance(cimomName, objectName, cc, assocName);

	    if (names) {
		CIMObjectPath resultOp = resultInstance.getObjectPath();
		resultOp.setNameSpace(objectName.getNameSpace());
		result = resultOp;
	    } else {
		result =
		generateInstance(cimomName, objectName, cc, assocName);
	    }
	    Vector v = new Vector();
	    v.addElement(result);
	    return v;
	}
    }

    public CIMInstance[] references(CIMObjectPath assocName,
				CIMObjectPath objectName,
				String role,
				boolean includeQualifiers,
				boolean includeClassOrigin,
				String[] propertyList) throws CIMException {
	Vector v = referenceTraversal(assocName, objectName, role,
	includeQualifiers, includeClassOrigin, propertyList, false);
	CIMInstance[] instArray = new CIMInstance[v.size()];
	return (CIMInstance[])v.toArray(instArray);
    }

    public CIMObjectPath[] referenceNames(CIMObjectPath assocName,
					  CIMObjectPath objectName,
					  String role) throws CIMException {
	Vector v = referenceTraversal(assocName, objectName, role,
	false, false, null, true);
	CIMObjectPath[] opArray = new CIMObjectPath[v.size()];
	return (CIMObjectPath[])v.toArray(opArray);
    }
}

