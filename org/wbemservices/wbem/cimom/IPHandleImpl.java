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
 
import javax.wbem.cim.CIMInstance;
import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMValue;

import java.rmi.RemoteException;
import java.lang.reflect.Method;



/**
 * Alias Element class
 * 
 * This class gets and sets values for CIM properties. 
 * 
 *
 * @author    
 * @version     1.5, 09/11/98
 * @Since       wbem 1.0
 */
public class IPHandleImpl {

    Object iProvider;
    CIMInstance ci;
    Class providerClass;
    Class noparams[] = new Class[0];
    Object noargs[] = new Object[0];
    Class CIMValueClass[] = new Class[1];
    Object[] args = new Object[1];
    
    /**
     * Constructor initializes an IPHandleImpl object with the 
     * CIMValue class object
     *
     * @exception RemoteException initializer error
     *
     */
    public IPHandleImpl() throws java.rmi.RemoteException {
	try {
	    CIMValueClass[0] = Class.forName("javax.wbem.cim.CIMValue");
	} catch(Exception e) {
	    throw new RemoteException("Initializer error");
	}
    }
    
    /**
     * sets this CIM instance to the specified CIMInstance object
     *
     * @param ci the CIMInstance used to set this CIM instance
     *
     */
    void setInstanceElement(CIMInstance ci) {
	this.ci = ci;
    }
    
    /**
     * sets this instance provider object to the specified object
     *
     * @param o the object used to set this instance provider object
     *
     */
    void setIProvider(Object o) {
	this.iProvider = o;
	providerClass = o.getClass();
    }
    
    /**
     * 
     * gets the CIMValue for the specified property from the
     * instance provider for a CIM instance.
     *
     * This method concatenates the string "get"
     * and the specified property name, and then uses that 
     * concatenated string to get the method name for the 
     * provider. The method then invokes the appropriate 
     * "getProperty" method on the instance provider and returns
     * the CIMValue.
     * 
     *
     * @param propertyName 		the string name of a property
     * @return CIMValue 		the CIMValue for the specified property
     * @exception RemoteException 	XXX
     * @exception CIMException		XXX
     */
    public CIMValue getPropertyValue(String propertyName) 
    throws java.rmi.RemoteException , CIMException {
	String methodname = "get" + propertyName;
	CIMValue cv;
	try {
	    Method method = providerClass.getMethod(methodname, noparams);	
	    cv = (CIMValue)method.invoke(iProvider, noargs);
	} catch(Exception e) {
	    throw new CIMException(e.toString());
	}
	return cv;
    }

    /**
     * Sets the property value for this CIM instance to the specified CIMValue
     * and property. This method concatenates the string "set" and the
     * specified property name, and then uses that concatenated string to
     * determine the "setProperty" method used by this instance provider. The
     * instance provider's "setProperty" method is invoked to set the property
     * value.
     *
     * @param propertyName 		the string name of a property
     * @param value the CIMValue for the specified property
     * @exception RemoteException 	XXX
     * @exception CIMException		XXX
     */
    public void setPropertyValue(String propertyName, CIMValue value) 
    throws java.rmi.RemoteException, CIMException {
	String methodname = "set" + propertyName;
	try {
	    Method method = providerClass.getMethod(methodname, CIMValueClass);	
	    args[0] = value;
	    method.invoke(iProvider, args);
	} catch(Exception e) {
	    throw new CIMException(e.toString());
	}
    }
}
