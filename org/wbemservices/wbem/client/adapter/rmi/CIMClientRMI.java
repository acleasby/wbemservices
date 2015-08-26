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
 *Contributor(s): WBEM Solutions, Inc., Brian Schlosser
 */

package org.wbemservices.wbem.client.adapter.rmi;

import javax.wbem.cim.CIMNameSpace;
import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMObjectPath;
import javax.wbem.cim.CIMClass;
import javax.wbem.cim.CIMInstance;
import javax.wbem.cim.CIMValue;
import javax.wbem.cim.CIMQualifierType;
import javax.wbem.cim.CIMArgument;
import javax.wbem.client.CIMClient;
import javax.wbem.client.CIMClientAPI;
import javax.wbem.client.CIMOperation;
import javax.wbem.client.CIMListener;
import javax.wbem.client.Debug;
import javax.wbem.client.CIMTransportException;
import javax.wbem.security.ClientSecurityContext;

import java.lang.reflect.Constructor;
import java.rmi.Naming;
import java.util.Vector;

/**
 * @since       WBEM 1.0
 */
public class CIMClientRMI implements CIMClientAPI {

    public  static final String RMIPORT        = "5987";
    private static final String RMIPORTPROP    = "javax.wbem.rmiport";
    private static final String RMIPROTVERSION = "1";

    private static final Object lockObject = new Object();
    private static final String protocol_name = CIMClient.CIM_RMI;

    private CIMClientAPI rmiAdapter;
    private CIMNameSpace nameSpace;

    private CIMClientAPI getComSunAdapter(String version, 
			CIMNameSpace name,
    			CIMListener clientListener, 
			Integer dbg) throws CIMException {
	try {
	    // We use reflection here, because the open tree cannot
	    // rely on the solaris tree. If the class is not found, that
	    // means we do not support backward compatiblity with comsun
	    // servers.
	    Debug.trace2("Loading comsun adapter for backward compatibility");
	    Class c = Class.forName("org.wbemservices.wbem.client.RMIComSunClient");
	    Class params[] = {
	    String.class, CIMNameSpace.class, CIMListener.class, Integer.class};
	    Constructor constr = c.getConstructor(params);
	    Object args[] = {version, name, clientListener, dbg};
	    CIMClientAPI api = (CIMClientAPI)constr.newInstance(args);
	    return api;
	} catch (Exception e) {
	    Debug.trace2("Got exception while initializing comsun adapter", e);
	    throw new CIMException(CIMException.VER_ERROR, 
	    RMIPROTVERSION, "No comsun adapter found");
	}
    }

    private CIMOM_1 retryConnection(String uri) 
    throws CIMTransportException, java.rmi.NotBoundException {
	// We should check
	// This almost definitely means that we have connected
	// to our cimom boot server.
	// We'll try to connect to it 3 times, to take care of
	// slow machines also.
	boolean lastExceptionNotBound = false;
	java.rmi.NotBoundException nbe = null;
	int i = 0;
	while (true) {
	    i++;
	    if (i > 5) {
		if (lastExceptionNotBound) {
		    // We found an RMI server, but we couldnt find this version
		    // of the CIMOM
		    throw nbe;
		} else {
		    throw new CIMTransportException(
		    CIMTransportException.TIMED_OUT, uri);
		}
	    }

	    synchronized (this) {
		try {
		    this.wait(4000);
		} catch (InterruptedException ie) {
		    // do nothing.
		}
	    }

	    try {
		CIMOM_1 comp = (CIMOM_1)Naming.lookup(uri);
		// There was no exception
		return comp;
	    } catch (java.rmi.NotBoundException e0) {
		// try again
		nbe = e0;
		lastExceptionNotBound = true;
	    } catch (Exception e1) {
		// try again
		lastExceptionNotBound = false;
	    }
	}
    }

    public CIMClientRMI(String version, 
			CIMNameSpace name,
    			CIMListener clientListener, 
			Integer dbg) throws CIMException {

	nameSpace = name;
	String rmiport = System.getProperty(RMIPORTPROP, RMIPORT);
	StringBuffer buffer = new StringBuffer(32);
	buffer.append("//");
	buffer.append(nameSpace.getHost());
	buffer.append(':');
	buffer.append(rmiport);
	buffer.append("/CIMOM_");
        // We used to use the version passed, in but the API version
        // does not match with the RMI protocol version anymore.
	buffer.append(RMIPROTVERSION);
        String uri = buffer.toString(); 

	try {
	    synchronized (lockObject) {

		CIMOM_1 comp;
		try {
		   comp = (CIMOM_1)Naming.lookup(uri);
		} catch (java.rmi.MarshalException me) {
		    comp = retryConnection(rmiport);
		} catch (java.rmi.ConnectIOException me) {
		    comp = retryConnection(rmiport);
		}

		rmiAdapter = new RMIJavaxClient(version, name, clientListener,
		dbg, comp);
	    }
        } catch (java.rmi.UnknownHostException ex) {
            throw new CIMTransportException(CIMTransportException.UNKNOWN_HOST, 
                    nameSpace.getHost());
        } catch (java.rmi.ConnectException ex) {
            throw new CIMTransportException(CIMTransportException.NO_CIMOM,
                    uri);
        } catch (java.rmi.RemoteException ex) {
            if (ex.detail instanceof ClassNotFoundException) {
                throw new NoClassDefFoundError(ex.detail.toString());
            }
            throw new CIMTransportException(CIMTransportException.NO_CIMOM, 
                    uri);
        } catch (java.net.MalformedURLException ex) {
             throw new CIMTransportException(CIMTransportException.NO_CIMOM, 
                   uri);
        } catch (java.rmi.NotBoundException ex) {
            // We need to see if this is an old CIMOM
            rmiAdapter = getComSunAdapter(version, name, clientListener, dbg);
        }
    }

    /**
     * Return the name of the CIM protocol being used.
     *
     * @return  The name of the CIM protocol.
     */
    public String getProtocol() {
	return (protocol_name);
    }

    /**
     * @exception CIMException Client authentication throws a CIM 
     *            security exception.
     */
    public synchronized void initSecurityContext(String version,
    ClientSecurityContext csc) throws CIMException {
	rmiAdapter.initSecurityContext(version, csc);
    }

    /** 
     * @exception CIMException The createNameSpace method throws a CIM exception.
     */ 
    public synchronized void createNameSpace(String version,
    CIMNameSpace currNs, CIMNameSpace newNs) throws CIMException {
	rmiAdapter.createNameSpace(version, currNs, newNs);
    }
    
    /** 
     * @exception CIMException The close method throws a CIM exception.
     */ 
    public synchronized void close(String version)
    throws CIMException {
	rmiAdapter.close(version);
    }

    /** 
     * @exception CIMException The deleteNameSpace method throws a CIM exception.
     */ 
    public synchronized void deleteNameSpace(String version,
    CIMNameSpace currNs, CIMNameSpace delNs) throws CIMException {
	rmiAdapter.deleteNameSpace(version, currNs, delNs);
    }
    /** 
     * @exception CIMException The deleteClass method throws a CIM exception.
     */ 
    public synchronized void deleteClass(String version, CIMNameSpace currNs,
    CIMObjectPath path) throws CIMException {
	rmiAdapter.deleteClass(version, currNs, path);
    }
    /** 
     * @exception CIMException The deleteInstance method throws a CIM exception.
     */ 
    public synchronized void deleteInstance(String version, CIMNameSpace currNs,
    CIMObjectPath path) throws CIMException {
	rmiAdapter.deleteInstance(version, currNs, path);
    }
    /** 
     * @exception CIMException The deleteQualifierType method throws a CIM exception.
     */ 
    public synchronized void deleteQualifierType(String version,
    CIMNameSpace currNs, CIMObjectPath path) throws CIMException {
	rmiAdapter.deleteQualifierType(version, currNs, path);
    }


    /**
     * @exception CIMException The enumerateClasses method throws a CIM
     *           exception.
     */
    public synchronized Vector enumerateClasses(String version,
						CIMNameSpace currNs,
						CIMObjectPath path,
						boolean deep,
						boolean localOnly,
						boolean includeQualifiers,
						boolean includeClassOrigin)
	throws CIMException {

	return rmiAdapter.enumerateClasses(version, currNs, path,
	deep, localOnly, includeQualifiers, includeClassOrigin);
    }

    /**
     * @exception CIMException  The enumerateClassNames method
     *            throws a CIM exception.
     */
    public synchronized Vector enumerateClassNames(String version,
						   CIMNameSpace currNs,
						   CIMObjectPath path,
						   boolean deep) 
	throws CIMException {

	return rmiAdapter.enumerateClassNames(version, currNs, path,
	deep);
    }

    /** 
     * @exception CIMException The enumNameSpace method throws a CIM exception.
     */ 
    public synchronized Vector enumNameSpace(String version,
    CIMNameSpace currNs, CIMObjectPath path, boolean deep)
    throws CIMException {
	return rmiAdapter.enumNameSpace(version, currNs, path, deep);
    }
    
    public Vector enumerateInstances(String version,
                                CIMNameSpace currNs,
                                CIMObjectPath path,
                                boolean deep,
                                boolean localOnly,
                                boolean includeQualifiers,
                                boolean includeClassOrigin,
                                String propertyList[]) throws CIMException {
	return rmiAdapter.enumerateInstances(version, currNs, path, deep,
	localOnly, includeQualifiers, includeClassOrigin, propertyList);
    }

    public Vector enumerateInstanceNames(String version,
                                CIMNameSpace currNs,
                                CIMObjectPath path) throws CIMException {
	return rmiAdapter.enumerateInstanceNames(version, currNs, path);
    }


    /** 
     * @exception CIMException The enumQualifierTypes method throws a CIM exception.
     */ 
    public synchronized Vector enumQualifierTypes(String version,
    CIMNameSpace currNs, CIMObjectPath path) throws CIMException {

	return rmiAdapter.enumQualifierTypes(version, currNs, path);
    }

    public CIMClass getClass(String version,
                           CIMNameSpace currNs,
                           CIMObjectPath path,
                           boolean localOnly,
			   boolean includeQualifiers,
			   boolean includeClassOrigin,
			   String propertyList[]) throws CIMException {

	return rmiAdapter.getClass(version, currNs, path, localOnly, 
	includeQualifiers, includeClassOrigin, propertyList);
    }


    public CIMInstance getInstance(String version,
                           	   CIMNameSpace currNs,
                           	   CIMObjectPath path,
                           	   boolean localOnly,
			   	   boolean includeQualifiers,
			   	   boolean includeClassOrigin,
			   	   String propertyList[]) 
	throws CIMException {

	return rmiAdapter.getInstance(version, currNs, path, localOnly,
	includeQualifiers, includeClassOrigin, propertyList);
    }

    /** 
     * @exception CIMException The invokeMethod method throws a CIM exception.
     */ 
    public synchronized CIMValue invokeMethod(String version,
    					      CIMNameSpace currNs, 
					      CIMObjectPath name, 
					      String methodName, 
					      CIMArgument[] inArgs,
    					      CIMArgument[] outArgs) 
	throws CIMException {

	return rmiAdapter.invokeMethod(version, currNs, name, methodName, 
	inArgs, outArgs);
    }

    /** 
     * @exception CIMException The CIMQualifierType method throws a CIM exception.
     */ 
    public synchronized CIMQualifierType getQualifierType(String version, 
    CIMNameSpace currNs, CIMObjectPath name) throws CIMException {
	return rmiAdapter.getQualifierType(version, currNs, name);
    }

    /** 
     * @exception CIMException The createQualifierType method 
     *                         throws a CIM exception.
     */ 
    public synchronized void createQualifierType(String version, 
    					         CIMNameSpace currNs, 
						 CIMObjectPath name, 
						 CIMQualifierType qt)
    	throws CIMException {

	rmiAdapter.createQualifierType(version, currNs, name, qt);
    }

    /** 
     * @exception CIMException The createClass method throws a CIM exception.
     */    
    public synchronized void createClass(String version, 
					 CIMNameSpace currNs,
    				         CIMObjectPath name, 
				         CIMClass cc) throws CIMException {

	rmiAdapter.createClass(version, currNs, name, cc);
    }

    /** 
     * @exception CIMException The createInstance method throws a 
     *                         CIM exception.
     */ 
    public synchronized CIMObjectPath createInstance(String version, 
    CIMNameSpace currNs, CIMObjectPath name, CIMInstance ci)
    throws CIMException {

	return rmiAdapter.createInstance(version, currNs, name, ci);
    }

    /**
     * @exception CIMException The setQualifierType method throws a CIM
     *                    exception.
     */
    public synchronized void setQualifierType(String version, 
    CIMNameSpace currNs, CIMObjectPath name, CIMQualifierType qt)
    throws CIMException {

	rmiAdapter.setQualifierType(version, currNs, name, qt);
    }

    /**
     * @exception CIMException The setClass method throws a CIM exception if
     *                    the class does not exist. <br>or <br>if an RMI
     *                    error is detected
     */
    public synchronized void setClass(String version, 
				      CIMNameSpace currNs,
    				      CIMObjectPath name, 
				      CIMClass cc) throws CIMException {

	rmiAdapter.setClass(version, currNs, name, cc);
    }

    /**
     * @exception CIMException The setInstance method throws a CIM exception.
     *                    <br>or <br>Throws a CIM Exception if an RMI error
     *                    is detected
     */
    public synchronized void setInstance(String version, 
					 CIMNameSpace currNs,
    					 CIMObjectPath name, 
					 CIMInstance ci,
					 boolean includeQualifier,
					 String[] propertyList) 
					 throws CIMException {

	rmiAdapter.setInstance(version, currNs, name, ci,
	includeQualifier, propertyList);
    }

    /**
     * @exception CIMException The getProperty method throws a CIM exception.
     *                    <br>or <br>Throws a CIM Exception if an RMI error
     *                    is detected
     */
    public synchronized CIMValue getProperty(String version,
    					     CIMNameSpace currNs, 
					     CIMObjectPath name, 
					     String propertyName)
    	throws CIMException {

	return rmiAdapter.getProperty(version, currNs, name, propertyName);
    }

    /**
     * @exception CIMException The setProperty method throws a CIM exception.
     *                    <br>or <br>Throws a CIM Exception if an RMI error
     *                    is detected
     */
    public synchronized void setProperty(String version, CIMNameSpace currNs,
    CIMObjectPath name, String propertyName, CIMValue cv)
    throws CIMException {

	rmiAdapter.setProperty(version, currNs, name, propertyName, cv);
    }
    
    public synchronized Vector execQuery(String version, CIMNameSpace currNs,
    CIMObjectPath relNS, String query, String ql) throws CIMException {

	return rmiAdapter.execQuery(version, currNs, relNS, query, ql);
    }

    public Vector associators(String version, CIMNameSpace currNs,
				CIMObjectPath objectName,
				String assocClass,
				String resultClass,
				String role,
				String resultRole,
				boolean includeQualifiers,
				boolean includeClassOrigin,
				String propertyList[]) 
	throws CIMException {

	return rmiAdapter.associators(version, currNs, objectName, assocClass,
	resultClass, role, resultRole, includeQualifiers, includeClassOrigin,
	propertyList);
    }

    public Vector associatorNames(String version, CIMNameSpace currNs,
				CIMObjectPath objectName,
				String assocClass,
				String resultClass,
				String role,
				String resultRole) throws CIMException {

	return rmiAdapter.associatorNames(version, currNs, objectName, 
	assocClass, resultClass, role, resultRole);
    }

    public Vector references(String version, CIMNameSpace currNs,
				CIMObjectPath objectName,
				String resultClass,
				String role,
				boolean includeQualifiers,
				boolean includeClassOrigin,
				String propertyList[]) throws CIMException {

	return rmiAdapter.references(version, currNs, objectName, resultClass,
	role, includeQualifiers, includeClassOrigin, propertyList);
    }

    public Vector referenceNames(String version, CIMNameSpace currNs,
				CIMObjectPath objectName,
				String resultClass,
				String role) 
				throws CIMException {

	return rmiAdapter.referenceNames(version, currNs, objectName, 
	resultClass, role);
    }


    public Vector performOperations(String version,
				CIMOperation[] batchedOperations)
				throws CIMException {

	return rmiAdapter.performOperations(version, batchedOperations);
    }

    public void setListener(String version)
    throws CIMException {

	rmiAdapter.setListener(version);
    }
    public void setListener(String version, int port) throws CIMException {
        // XXX N/A for RMI
        rmiAdapter.setListener(version);
    }

    public CIMInstance getIndicationHandler(CIMListener cl) throws 
    CIMException {

	return rmiAdapter.getIndicationHandler(cl);
    }

    public CIMInstance getIndicationListener(CIMListener cl)
        throws CIMException {

        return rmiAdapter.getIndicationListener(cl);
    }
}
