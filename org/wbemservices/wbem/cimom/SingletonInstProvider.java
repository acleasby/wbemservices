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

import javax.wbem.cim.CIMClass;
import javax.wbem.cim.CIMObjectPath;
import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMInstance;
import javax.wbem.client.CIMOMHandle;
import javax.wbem.client.ProviderCIMOMHandle;
import javax.wbem.provider.CIMInstanceProvider;

/**
 * This abstract class provides a skeletal implementation of the 
 * CIMInstanceProvider interface to make it easer to implement an 
 * CIMInstanceProvider that deals with a single instance. Subclasses must 
 * implement the  <code>getSingletonInstance</code> and 
 * <code>getInstanceName</code> methods to be concrete.
 *
 * @author     	Sun Microsystems, Inc.
 * @version	1.0 11/06/01
 * @since	WBEM 2.5
 */
public abstract class SingletonInstProvider implements CIMInstanceProvider {

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
     * @return <code>CIMInstanceProvider</code> which is the internal provider 
     * associated with the <code>CIMOMHandle</code>
     */
    protected CIMInstanceProvider getCIMInstanceProvider() {
	return intInstProvider;
    }

    /**
     * Retrieves the name of the singleton instance.
     * This is an abstract method and subclasses must override this method
     * to return the singleton instance name. This method is used by the
     * <code>enumerateInstanceNames</code> and <code>getInstance</code> methods.
     * Technically, the getInstance call was sufficient, but this is provided 
     * for efficiency.
     *
     * @param op 	the class name for which the singleton instance is
     *                  required.
     * @param cc	the class reference
     *
     * @return CIMObjectPath 	the singleton instance name.
     * @see SingletonInstProvider#enumerateInstanceNames(CIMObjectPath, CIMClass)
     * @see SingletonInstProvider#getInstance(CIMObjectPath, boolean, boolean, boolean, String[], CIMClass)
     */
    protected abstract CIMObjectPath getInstanceName(CIMObjectPath op,
    CIMClass cc) throws CIMException;

    /**
     * This method returns the singleton instance of the specified class.
     * This is an abstract method and subclasses must override this method
     * to return the singleton instance. This method is also invoked by
     * <code>enumerateInstances, execQuery</code> and 
     * <code>getInstance </code> methods.
     * implementations.
     * @param op 		the object path of the class whose instance must
     *                          be retrieved.
     *                  	values for the instance.
     * @param localOnly 	if true, only the local properties of the 
     *				class are returned, otherwise all properties 
     *				are required
     * @param includeQualifiers if true, the qualifiers are returned as part of 
     *				of the returned instance, otherwise no 
     *				qualifiers will be returned
     * @param includeClassOrigin if true, the class origin of each property will
     *                           be returned
     * @param propertyList	if null, all properties are returned, otherwise
     * 				only the properties specified will be returned.
     *                          Any duplicate properties will be ignored.
     * @param cc		the class reference
     *
     * @return CIMInstance 	the retrieved instance.
     * @exception CIMException 	the method throws a CIMException
     *                          if the singleton instance cannot be retrieved.
     *
     * @see SingletonInstProvider#getInstance(CIMObjectPath, boolean, boolean, boolean, String[], CIMClass)
     * @see SingletonInstProvider#enumerateInstances(CIMObjectPath, boolean, boolean, boolean, String[], CIMClass)
     * @see SingletonInstProvider#execQuery(CIMObjectPath, String, String, CIMClass)
     */
    protected abstract CIMInstance getSingletonInstance(CIMObjectPath op, 
    				   boolean localOnly,
				   boolean includeQualifiers, 
				   boolean includeClassOrigin, 
				   String[] propertyList, 
				   CIMClass cc) throws CIMException;

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

    /**
     * Retrieves the instance specified in the argument CIMObjectPath. 
     * This method first invokes the subclass <code>getInstanceName</code>
     * method to retrieve the instance name and compare it to the input
     * object path <code>op</code>. If they match, then it invokes the
     * <code>getSingletonInstance</code> method.
     * @param op 		the name of the instance to be retrieved. 
     *           		This must include all of the keys and 
     *                  	values for the instance.
     * @param localOnly 	if true, only the local properties of the 
     *				class are returned, otherwise all properties 
     *				are required
     * @param includeQualifiers if true, the qualifiers are returned as part of 
     *				of the returned instance, otherwise no 
     *				qualifiers will be returned
     * @param includeClassOrigin if true, the class origin of each property will
     *                           be returned
     * @param propertyList	if null, all properties are returned, otherwise
     * 				only the properties specified will be returned.
     *                          Any duplicate properties will be ignored.
     * @param cc		the class reference
     *
     * @return CIMInstance 	the retrieved instance.
     * @exception CIMException 	the method throws a CIMException
     *                         	if the CIMObjectPath is incorrect or does not
     *                         	exist.
     *
     * @see SingletonInstProvider#getInstanceName(CIMObjectPath, CIMClass)
     * @see SingletonInstProvider#getSingletonInstance(CIMObjectPath, boolean, boolean, boolean, String[], CIMClass)
     */
    public CIMInstance getInstance(CIMObjectPath op, 
    				   boolean localOnly,
				   boolean includeQualifiers, 
				   boolean includeClassOrigin, 
				   String[] propertyList, 
				   CIMClass cc) throws CIMException {
	CIMObjectPath instanceName = getInstanceName(op, cc);
	if (!instanceName.equals(op)) {
	    throw new CIMException(CIMException.CIM_ERR_NOT_FOUND, op);
	} else {
	    return getSingletonInstance(op, localOnly, includeQualifiers,
	    includeClassOrigin, propertyList, cc);
	}
    }


    /**
     * Returns an array of CIMObjectPaths. Since this is a singleton it
     * calls the subclass getInstanceName method to get the instance name
     * and populate the array.
     *
     * @param op 	the class name to enumerate the instances
     * @param cc 	the class reference passed to the provider
     *
     * @return an array of CIMObjectPath containing names of the 
     *         enumerated instances.
     * @exception CIMException if the classname is null or 
     *                         does not exist.
     */
    public CIMObjectPath[] enumerateInstanceNames(CIMObjectPath op, 
					 	  CIMClass cc) 
    throws CIMException {
	CIMObjectPath[] opArray = {getInstanceName(op, cc)};
	return opArray;
    }

    /**
     * Enumerates all instances of the class which is specified by the 
     * CIMObjectPath argument. The entire instances and not just the 
     * names are returned. Since this is a singleton instance provider, it
     * invokes the <code>getSingletonInstance</code> method and returns it as 
     * part of the enumeration.
     *
     * @param op 		the object path specifies the class to be 
				enumerated
     * @param localOnly 	if true, only the local properties of the 
     *				class are returned, otherwise all properties 
     *				are required
     * @param includeQualifiers if true, the qualifiers are returned as part of 
     *				of the returned instance, otherwise no 
     *		 		qualifiers will be returned
     * @param includeClassOrigin if true, the class origin of each property will
     *                           be returned
     * @param propertyList	if null, all properties are returned, otherwise 
     * 				only the properties specified will be returned.
     *                          Any duplicate properties will be ignored.
     * @param cc		the class reference
     *
     * @return An array of CIMInstance
     *       
     * @exception CIMException if the CIMObjectPath is incorrect or does not
     *                         exist.
     * @see SingletonInstProvider#getSingletonInstance(CIMObjectPath, boolean, boolean, boolean, String[], CIMClass)
     */
    public CIMInstance[] enumerateInstances(CIMObjectPath op, 
					    boolean localOnly, 
					    boolean includeQualifiers, 
					    boolean includeClassOrigin, 
					    String[] propertyList, 
					    CIMClass cc) 
    throws CIMException {
	CIMInstance[] ciArray = {getSingletonInstance(op, localOnly, 
	includeQualifiers, includeClassOrigin, propertyList, cc)};
	return ciArray;
    }

    /**
     * Creates the instance specified by the CIMInstance argument in 
     * the namespace specified by the CIMObjectPath argument. Since it
     * is a singleton instance provider, createInstance throws a 
     * CIM_ERR_NOT_SUPPORTED.
     *
     * @param op the path of the instance to be set. 
     * @param ci the instance to be set.
     * @return CIMObjectPath of the created instance. This should be non-null
     *             only if one or more of the keys have changed.
     * @exception CIMException The method createInstance throws a CIMException.                       
     */
    public CIMObjectPath createInstance(CIMObjectPath op, CIMInstance ci) 
    throws CIMException {
	throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
    }


    /**
     * Updates the passed in <code>CIMInstance</code>. Throws 
     * CIM_ERR_NOT_SUPPORTED.
     *
     * @param op The path of the instance to be set. The important part
     *           in this parameter is the namespace component.
     * @param ci The instance to be set.
     * @exception CIMException  The setInstance method throws a CIMException.
     */
    public void setInstance(CIMObjectPath op, CIMInstance ci,
    boolean includeQualifier, String[] propertyList) 
    throws CIMException {
	throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
    }

    /**
     * Deletes the instance specified in the object path. Throws 
     * CIM_ERR_NOT_SUPPORTED since it is a singleton instance provider.
     *
     * @param op The object path of the instance to be deleted.
     * @exception CIMException The deleteInstance method throws a CIMException.
     */
    public void deleteInstance(CIMObjectPath op) 
    throws CIMException {
	throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
    }

   /**
    *
    * Filters instance based on the query. This method calls the 
    * <code>getSingletonInstance</code> method to retrieve the instance,
    * and signals the CIMOM to do the necessary filtering by returning null as 
    * the first array element.
    * @param  op     The object name of the CIM Class to enumerate.
    * @param  query  The CIM query expression.
    * @param  ql     The CIM query.
    * @param  cc     The CIM class reference.
    * @return an array of CIMInstance that met the specified criteria
    * @see SingletonInstProvider#getSingletonInstance(CIMObjectPath, boolean, boolean, boolean, String[], CIMClass)
    */
    public CIMInstance[] execQuery(CIMObjectPath op, 
				   String query, 
				   String ql, 
				   CIMClass cc)
    throws CIMException {
	CIMInstance ciArray[] = {null,
				getSingletonInstance(op, false, true, true, 
				null, cc)};
	return ciArray;
    }
}

