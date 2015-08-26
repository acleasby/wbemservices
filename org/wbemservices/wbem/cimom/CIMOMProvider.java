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

import java.net.InetAddress;

import javax.wbem.client.CIMOMHandle;
import javax.wbem.client.Debug;
import javax.wbem.cim.CIMArgument;
import javax.wbem.cim.CIMClass;
import javax.wbem.cim.CIMObjectPath;
import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMInstance;
import javax.wbem.cim.CIMValue;

import javax.wbem.provider.CIMMethodProvider;

/**
 *
 * This class is the provider for WBEMServices_ObjectManager class. It is
 * a subclass of CIM_ObjectManager defined in the Interop mof.
 * Essentially this class represents the CIMOM as a CIM_Service
 * In additional to the standard service methods like start and stop, the
 * WBEMServices_ObjectManger class takes a mofreg method to do mof registry
 * operations.
 * This provider is an internal CIMOM provider and returns a single instance
 * of the CIM_ObjectManager class representing this CIMOM.
 * @author     	Sun Microsystems, Inc.
 * @version	1.0 11/01/01
 * @since	WBEM 2.5
 */
class CIMOMProvider extends SingletonInstProvider implements CIMMethodProvider {
    // This is package protected, so that other adapters that associate
    // the CIMOM to its configurations can get its class name.
    static final CIMObjectPath CLASSOP = 
    new CIMObjectPath("WBEMServices_ObjectManager", CIMOMImpl.INTEROPNS);

    private Mofregistry mofreg;
    CIMObjectPath instanceName;
    CIMInstance fullInstance;

    private void initData() throws CIMException {
	CIMOMHandle ch = getCIMOMHandle();

	//Generate the instance
	CIMClass cc = ch.getClass(CLASSOP, false, true, true, null);
	fullInstance = cc.newInstance();
	fullInstance.setProperty("SystemCreationClassName", 
	new CIMValue(""));
	String hostName;
	try {
	    hostName = InetAddress.getLocalHost().getHostName();
	} catch (Exception e) {
	    hostName = "";
	}
	fullInstance.setProperty("SystemName", new CIMValue(hostName));
	fullInstance.setProperty("CreationClassName", 
	new CIMValue(cc.getName()));
	fullInstance.setProperty("Name", new CIMValue(cc.getName()));
	fullInstance.setProperty("StartMode", 
	new CIMValue("Automatic"));
	fullInstance.setProperty("Started", CIMValue.TRUE);
	fullInstance.setProperty("Version", 
	new CIMValue(Version.major + "." + Version.minor + "." + 
	Version.revision));

	instanceName = fullInstance.getObjectPath();
	instanceName.setNameSpace(CLASSOP.getNameSpace());

    }

    // Retrieves the instance specified in op
    protected CIMInstance getSingletonInstance(CIMObjectPath op, 
    				   boolean localOnly,
				   boolean includeQualifiers, 
				   boolean includeClassOrigin, 
				   String[] propertyList, 
				   CIMClass cc) throws CIMException {
	CIMInstance returnInstance = fullInstance;
	if (localOnly) {
	    returnInstance = returnInstance.localElements();
	}
	if ((propertyList != null) || (!includeQualifiers) || 
	(!includeClassOrigin)) {
	    // filtering is required
	    returnInstance = returnInstance.filterProperties(propertyList,
	    includeQualifiers, includeClassOrigin);
	}
	return returnInstance;
    }
		

    // Retrieves the name of the singleton instance. Overriding the abstract
    // superclass method.
    protected CIMObjectPath getInstanceName(CIMObjectPath op,
    CIMClass cc) {
	return instanceName;
    }

    // Overriding the superclass method to initialize the instance and
    // instance name.
    public synchronized void initialize(CIMOMHandle ch) throws CIMException {
	super.initialize(ch);
	initData();
    }

    // Invokes the method on op - though since this is a singleton instance,
    // we just ignore op. Its basically like a static method at this point
    // Throws CIM_ERR_NOT_SUPPORTED for unsupported methods.
    public CIMValue invokeMethod(CIMObjectPath op, String methodName, 
	CIMArgument[] inArgs, CIMArgument[] outArgs) throws CIMException {
	if (methodName.equalsIgnoreCase("registerMOF")) {
	    Debug.trace2("registerMOF method invoked");
	    try {
		mofreg.mofReg();
	    } catch (CIMException ce) {
		Debug.trace1("registerMOF exception", ce);
		throw ce;
	    } catch (Exception e) {
		Debug.trace1("registerMOF exception", e);
		throw new CIMException(CIMException.CIM_ERR_FAILED,
		e.toString());
	    }
	    Debug.trace2("registerMOF method completed");
	    return new CIMValue(new Byte((byte)0));
	}
	// Need start and stop service too.
	throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
    }

    /**
     * Constructor for this class - it takes the mof registry instance in
     * order to perform the mofReg method.
     */
    CIMOMProvider(Mofregistry mofreg) {
	this.mofreg = mofreg;
    }
}
