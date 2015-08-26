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
 *are Copyright © 2003 Sun Microsystems, Inc.
 *
 *All Rights Reserved.
 *
 *Contributor(s): WBEM Solutions, Inc.
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
import javax.wbem.security.ClientSecurityContext;
import javax.wbem.security.SecurityMessage;
import javax.wbem.security.SecurityToken;

import java.rmi.NoSuchObjectException;
import java.rmi.ServerException;
import java.rmi.server.UnicastRemoteObject;

import java.util.Locale;
import java.util.Vector;

import org.wbemservices.wbem.client.SunDigestClientSecurity;

/**
 * @author      Sun Microsystems, Inc.
 * @version     1.2 01/30/02 
 * @since       WBEM 2.5
 */
public class RMIJavaxClient implements CIMClientAPI {

    // The protocol name.  Must be consistent with CIM standard name.
    private static final String protocol_name = CIMClient.CIM_RMI;
    // Name of the CIM Indication handler for this transport
    private static final String HANDLERCLASS = "solaris_javaxrmidelivery";

    private CIMOM_1 comp;
    private CIMNameSpace nameSpace;
    private SunDigestClientSecurity cs;
    private CIMListener clientListener;
    private RemoteCIMListener remoteListener;
    private int debug;
    public final static String RMIPORT = "5987";
    private final String RMIPORTPROP = "javax.wbem.rmiport";
    private final String RMIPROTVERSION = "1";

    private CheckSumGen csg = new CheckSumGen();

    // For now we have only two versions we need to keep track of, so it
    // is a boolean
    private boolean latestVersion = false;

    // This object is passed back by the CIMOM RMI adapter to make sure that 
    // the client is still available. The CIMOM RMI adapter uses the RMI runtime
    // lease mechanism to receive an Unreferenced call.
    private Object heartBeatObj = null;

    public RMIJavaxClient(String version, 
			CIMNameSpace name,
    			CIMListener clientListener, 
			Integer dbg,
			CIMOM_1 comp) throws CIMException {
	
	this.clientListener = clientListener;
	nameSpace = name;
	this.debug = dbg.intValue();
	cs = null;
	this.comp = comp;
    }

    /**
     * Return the name of the CIM protocol being used.
     *
     * @return	The name of the CIM protocol.
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

	// Must cast security context to our Sun digest mechanism in order
	// to get authentication method calls over RMI.
	try {
	    this.cs = (SunDigestClientSecurity) csc;
	} catch (Exception ex) {
	    throw new CIMException(CIMException.CIM_ERR_FAILED,
			"Invalid security context");
	}

	// Authenticate with remote CIMOM to set up secure session.
	// Note: This all gets replaced with a loop calling a single
	// remote method until the client security session is established.
	// Unfortunately, we must wait for the next protocol version!
	SecurityMessage cm = cs.generateHello();
	Locale l = Locale.getDefault();
	SecurityMessage sm = this.hello(version, cm, l.getCountry(),
	l.getLanguage(), l.getVariant(), null);
	cm = cs.generateResponse(sm);
	sm = this.credentials(version, cm);
	cs.checkResult(sm);

	// Some implementations may support assuming a role on the server.
	String roleName = cs.getRoleName();
	if ((roleName != null) && (roleName.trim().length() > 0)) {
	    String rolePswd = cs.getRolePassword();
	    String encrPswd = cs.trans51Format(rolePswd);
	    this.assumeRole(version, roleName, encrPswd);
	}

	latestVersion = checkVersion(version);
    }

    boolean checkVersion(String version) throws CIMException {
	String[] oarray = {version, "getVersion"};

	synchronized (cs) {
	    SecurityToken st = cs.getSecurityToken(oarray);
	    // We're not incrementing the session key on purpose. This is
	    // because old CIMOMs who do not support getVersion will throw
	    // a NOT_SUPPORTED exception, but unfortunately do not increment
	    // their session key. So new CIMOMs do not increment their session
	    // key for this method.

	    try {
		Vector outp = comp.getVersion(version, st);
	    } catch (CIMException e) {
		return false;
	    } catch (Exception e) {
		throw new CIMException("RMIERROR", "getVersion", e);
	    }
	}
	// We dont bother checking the version number here, since we are
	// just dealing with two for now. (non-versioned and versioned)
	return true;
    }


    /** 
     * @exception CIMException The createNameSpace method throws a CIM exception.
     */ 
    public synchronized void createNameSpace(String version,
    CIMNameSpace currNs, CIMNameSpace newNs) throws CIMException {
	
	String[] oarray = {version, "createNameSpace", csg.toString(currNs),
	csg.toString(newNs)};

	synchronized (cs) {
	    SecurityToken st = cs.getSecurityToken(oarray);
	    cs.incSessionKey();
	    try {
		comp.createNameSpace(version, currNs, newNs, st);
	    } catch (CIMException e) {
		throw e;
	    } catch (Exception e) {
		throw new CIMException("RMIERROR", "createNameSpace", e);
	    }
	}

    }
    
/** 
* @exception CIMException The close method throws a CIM exception.
*/ 
    public synchronized void close(String version)
    throws CIMException {

	String[] oarray = {version, "close"};

	synchronized (cs) {
	    SecurityToken st = cs.getSecurityToken(oarray);
	    cs.incSessionKey();
	    try {
		try {
		    UnicastRemoteObject.unexportObject(remoteListener, false);
		} catch (NoSuchObjectException e) {
		    // This means that the unreferenced call in remoteListener
		    // has already unexported, so we ignore.
		}
		comp.close(version, st);
		// App wont exit if this remains exported.
	    } catch (CIMException e) {
		throw e;
	    } catch (Exception e) {
		throw new CIMException("RMIERROR", "close", e);
	    }
	}

    }

/** 
* @exception CIMException The deleteNameSpace method throws a CIM exception.
*/ 
    public synchronized void deleteNameSpace(String version,
    CIMNameSpace currNs, CIMNameSpace delNs) throws CIMException {

	String[] oarray = {version, "deleteNameSpace", csg.toString(currNs),
			csg.toString(delNs)};

	synchronized (cs) {
	    SecurityToken st = cs.getSecurityToken(oarray);
	    cs.incSessionKey();
	    try {
		comp.deleteNameSpace(version, currNs, delNs, st);
	    } catch (CIMException e) {
		throw e;
	    } catch (Exception e) {
		throw new CIMException("RMIERROR", "deleteNameSpace", e);
	    }
	}

    }
/** 
* @exception CIMException The deleteClass method throws a CIM exception.
*/ 
    public synchronized void deleteClass(String version, CIMNameSpace currNs,
    CIMObjectPath path) throws CIMException {

	String[] oarray = {version, "deleteClass", csg.toString(currNs),
	csg.toString(path)};

	synchronized (cs) {
	    SecurityToken st = cs.getSecurityToken(oarray);
	    cs.incSessionKey();
	    try {
		comp.deleteClass(version, currNs, path, st);
	    } catch (CIMException e) {
		throw e;
	    } catch (Exception e) {
		throw new CIMException("RMIERROR", "deleteClass", e);
	    }
	}

    }
/** 
* @exception CIMException The deleteInstance method throws a CIM exception.
*/ 
    public synchronized void deleteInstance(String version, CIMNameSpace currNs,
    CIMObjectPath path) throws CIMException {

	String[] oarray = {version, "deleteInstance", csg.toString(currNs),
	csg.toString(path)};

	synchronized (cs) {
	    SecurityToken st = cs.getSecurityToken(oarray);
	    cs.incSessionKey();
	    try {
		comp.deleteInstance(version, currNs, path, st);
	    } catch (CIMException e) {
		throw e;
	    } catch (Exception e) {
		throw new CIMException("RMIERROR", "deleteInstance", e);
	    }
	}

    }
/** 
* @exception CIMException The deleteQualifierType method throws a CIM exception.
*/ 
    public synchronized void deleteQualifierType(String version,
    CIMNameSpace currNs, CIMObjectPath path) throws CIMException {

	String[] oarray = {version, "deleteQualifierType",
	csg.toString(currNs), csg.toString(path)};

	synchronized (cs) {
	    SecurityToken st = cs.getSecurityToken(oarray);
	    cs.incSessionKey();
	    try {
		comp.deleteQualifierType(version, currNs, path, st);
	    } catch (CIMException e) {
		throw e;
	    } catch (Exception e) {
		throw new CIMException("RMIERROR", "deleteQualifierType", e);
	    }
	}

    }


    /**
     * @exception CIMException The enumerateClasses method throws a CIM
     *			 exception.
     */
    public synchronized Vector enumerateClasses(String version,
						CIMNameSpace currNs,
						CIMObjectPath path,
						boolean deep,
						boolean localOnly,
						boolean includeQualifiers,
						boolean includeClassOrigin)
	throws CIMException {

	Boolean bln = (deep ? Boolean.TRUE : Boolean.FALSE);
	String[] oarray = {version, "enumerateClasses", csg.toString(currNs),
			   csg.toString(path), bln.toString()};

	synchronized (cs) {
	    SecurityToken st = cs.getSecurityToken(oarray);
	    cs.incSessionKey();


	    try {
		return comp.enumerateClasses(version, currNs, path,
					     deep, localOnly, 
					     includeQualifiers, 
					     includeClassOrigin,
					     st);
	    } catch (CIMException e) {
		throw e;
	    } catch (Exception e) {
		throw new CIMException("RMIERROR", "enumerateClasses", e);
	    }
	}

    }

    /**
     * @exception CIMException  The enumerateClassNames method
     *			  throws a CIM exception.
     */
    public synchronized Vector enumerateClassNames(String version,
						   CIMNameSpace currNs,
						   CIMObjectPath path,
						   boolean deep) 
	throws CIMException {

        Boolean bln = (deep ? Boolean.TRUE : Boolean.FALSE);
        String[] oarray = {version, "enumerateClassNames", 
			   csg.toString(currNs), csg.toString(path), 
			   bln.toString()};

        synchronized (cs) {
            SecurityToken st = cs.getSecurityToken(oarray);
            cs.incSessionKey();
            try {
                return comp.enumerateClassNames(version, currNs, path, deep,
				      st);
            } catch (CIMException e) {
                throw e;
            } catch (Exception e) {
                throw new CIMException("RMIERROR", "enumClass", e);
            }
        }

    }

    /** 
     * @exception CIMException The enumNameSpace method throws a CIM exception.
     */ 
    public synchronized Vector enumNameSpace(String version,
    CIMNameSpace currNs, CIMObjectPath path, boolean deep)
    throws CIMException {

	Boolean bln = (deep ? Boolean.TRUE : Boolean.FALSE);
	String[] oarray = {version, "enumNameSpace", csg.toString(currNs),
	csg.toString(path), bln.toString()};

	synchronized (cs) {
	    SecurityToken st = cs.getSecurityToken(oarray);
	    cs.incSessionKey();
	    try {
		return comp.enumNameSpace(version, currNs, path, deep, st);
	    } catch (CIMException e) {
		throw e;
	    } catch (Exception e) {
		throw new CIMException("RMIERROR", "enumNameSpace", e);
	    }
	}

    }
    
    public Vector enumerateInstances(String version,
                                CIMNameSpace currNs,
                                CIMObjectPath path,
                                boolean deep,
                                boolean localOnly,
                                boolean includeQualifiers,
                                boolean includeClassOrigin,
                                String propertyList[]) throws CIMException {

	String fName = "enumerateInstances";

	Boolean bln = (deep ? Boolean.TRUE : Boolean.FALSE);
	String[] oarray = {version, fName, csg.toString(currNs),
	csg.toString(path), bln.toString()};

	synchronized (cs) {
	    SecurityToken st = cs.getSecurityToken(oarray);
	    cs.incSessionKey();
	
	    try {
		return comp.enumerateInstances(version, currNs, path,
					       deep, 
					       localOnly,
					       includeQualifiers, 
					       includeClassOrigin,
					       propertyList,
					       st);
	    } catch (CIMException e) {
		throw e;
	    } catch (Exception e) {
		throw new CIMException("RMIERROR", 
				       fName, e);
	    }
	}
    }

    public Vector enumerateInstanceNames(String version,
                                CIMNameSpace currNs,
                                CIMObjectPath path) throws CIMException {
	if (!latestVersion) {
	    throw new CIMException(CIMException.VER_ERROR, version);
	}

	String[] oarray = {version, "enumerateInstanceNames", 
			   csg.toString(currNs),
			   csg.toString(path)};

	synchronized (cs) {
	    SecurityToken st = cs.getSecurityToken(oarray);
	    cs.incSessionKey();

            try {
                return comp.enumerateInstanceNames(version,
					      currNs,
					      path,
					      st);
	    } catch (CIMException e) {
		throw e;
	    } catch (Exception e) {
		throw new CIMException("RMIERROR", 
				       "enumerateInstanceNames", e);
	    }
	}

    }


    /** 
     * @exception CIMException The enumQualifierTypes method throws a CIM exception.
     */ 
    public synchronized Vector enumQualifierTypes(String version,
    CIMNameSpace currNs, CIMObjectPath path) throws CIMException {

	String[] oarray = {version, "enumQualifierTypes",
	csg.toString(currNs), csg.toString(path)};

	synchronized (cs) {
	    SecurityToken st = cs.getSecurityToken(oarray);
	    cs.incSessionKey();
	    try {
		return comp.enumQualifierTypes(version, currNs, path, st);
	    } catch (CIMException e) {
		throw e;
	    } catch (Exception e) {
		throw new CIMException("RMIERROR", "enumQualifierTypes", e);
	    }
	}

    }

    public CIMClass getClass(String version,
                           CIMNameSpace currNs,
                           CIMObjectPath path,
                           boolean localOnly,
			   boolean includeQualifiers,
			   boolean includeClassOrigin,
			   String propertyList[]) throws CIMException {

	String fName = "getClass";

	Boolean blo = (localOnly ? Boolean.TRUE : Boolean.FALSE);
	String[] oarray = {version, fName,
			   csg.toString(currNs),
			   csg.toString(path), blo.toString()};
	synchronized (cs) {
	    SecurityToken st = cs.getSecurityToken(oarray);

	    cs.incSessionKey();

            try {
                return comp.getClass(version, currNs, path,
				     localOnly,
				     includeQualifiers,
				     includeClassOrigin,
				     propertyList,
				     st);
	    } catch (CIMException e) {
		throw e;
	    } catch (Exception e) {
		throw new CIMException("RMIERROR", fName, 
					e);
	    }
	}

    }


    public CIMInstance getInstance(String version,
                           	   CIMNameSpace currNs,
                           	   CIMObjectPath path,
                           	   boolean localOnly,
			   	   boolean includeQualifiers,
			   	   boolean includeClassOrigin,
			   	   String propertyList[]) 
	throws CIMException {

	String fName = "getInstance";

	Boolean blo = (localOnly ? Boolean.TRUE : Boolean.FALSE);
	String[] oarray = {version, fName,
			   csg.toString(currNs),
			   csg.toString(path), blo.toString()};

	synchronized (cs) {
	    SecurityToken st = cs.getSecurityToken(oarray);
	    cs.incSessionKey();

            try {
                return comp.getInstance(version, currNs, path,
					localOnly,
					includeQualifiers,
					includeClassOrigin,
					propertyList,
					st);
	    } catch (CIMException e) {
		throw e;
	    } catch (Exception e) {
		throw new CIMException("RMIERROR", fName, 
					e);
	    }
	}

    }

    /** 
     * @exception CIMException The invokeMethod method throws a CIM exception.
     */ 
    public synchronized CIMValue invokeMethod(String version,
    					      CIMNameSpace currNs, 
					      CIMObjectPath name, 
					      String methodName, 
					      Vector inParams,
    					      Vector outParams) 
	throws CIMException {

	String[] oarray = {version, "invokeMethod", csg.toString(currNs),
	csg.toString(name), methodName, csg.toString(inParams)};

	synchronized (cs) {
	    SecurityToken st = cs.getSecurityToken(oarray);
	    cs.incSessionKey();
	    try {
		Vector v = comp.invokeMethod(version, currNs, name, methodName,
					inParams, st);
		for (int i = 1; i < v.size(); i++) {
		    outParams.addElement(v.elementAt(i));
		}
		return (CIMValue)v.elementAt(0);
	    } catch (CIMException e) {
		throw e;
	    } catch (Exception e) {
		throw new CIMException("RMIERROR", "invokeMethod", e);
	    }
	}

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

	String[] oarray = {version, "invokeMethod", csg.toString(currNs),
	csg.toString(name), methodName};

	Vector v;

	synchronized (cs) {
	    SecurityToken st = cs.getSecurityToken(oarray);
	    cs.incSessionKey();
	    try {
		v = comp.invokeMethod(version, currNs, name, methodName,
					inArgs, st);
	    } catch (CIMException e) {
		throw e;
	    } catch (Exception e) {
		throw new CIMException("RMIERROR", "invokeMethod", e);
	    }

	    for (int i = 1; i < v.size(); i++) {
		outArgs[i - 1] = (CIMArgument)v.elementAt(i);
	    }
	    return (CIMValue)v.elementAt(0);
	}

    }

    /** 
     * @exception CIMException The CIMQualifierType method throws a CIM exception.
     */ 
    public synchronized CIMQualifierType getQualifierType(String version, 
    CIMNameSpace currNs, CIMObjectPath name) throws CIMException {

	String[] oarray = {version, "getQualifierType", csg.toString(currNs),
	csg.toString(name)};

	synchronized (cs) {
	    SecurityToken st = cs.getSecurityToken(oarray);
	    cs.incSessionKey();
	    try {
		return comp.getQualifierType(version, currNs, name, st);
	    } catch (CIMException e) {
		throw e;
	    } catch (Exception e) {
		throw new CIMException("RMIERROR", "getQualifierType", e);
	    }
	}

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

	String[] oarray = {version, "createQualifierType", csg.toString(currNs),
			   csg.toString(name), csg.toString(qt)};

	synchronized (cs) {
	    SecurityToken st = cs.getSecurityToken(oarray);
	    cs.incSessionKey();
	    try {
		comp.createQualifierType(version, currNs, name, qt, st);
	    } catch (CIMException e) {
		throw e;
	    } catch (Exception e) {
		throw new CIMException("RMIERROR", "createQualifierType", e);
	    }
	}

    }

    /** 
     * @exception CIMException The createClass method throws a CIM exception.
     */    
    public synchronized void createClass(String version, 
					 CIMNameSpace currNs,
    				         CIMObjectPath name, 
				         CIMClass cc) throws CIMException {

	String[] oarray = {version, "createClass", csg.toString(currNs),
			   csg.toString(name), csg.toString(cc)};

	synchronized (cs) {
	    SecurityToken st = cs.getSecurityToken(oarray);
	    cs.incSessionKey();
	    try {
		comp.createClass(version, currNs, name, cc, st);
	    } catch (CIMException e) {
		throw e;
	    } catch (Exception e) {
		throw new CIMException("RMIERROR", "createClass", e);
	    }
	}

    }

    /** 
     * @exception CIMException The createInstance method throws a 
     *                         CIM exception.
     */ 
    public synchronized CIMObjectPath createInstance(String version, 
    CIMNameSpace currNs, CIMObjectPath name, CIMInstance ci)
    throws CIMException {

	String[] oarray = {version, "createInstance", csg.toString(currNs),
			   csg.toString(name), csg.toString(ci)};

	synchronized (cs) {
	    SecurityToken st = cs.getSecurityToken(oarray);
	    cs.incSessionKey();
	    try {
		return comp.createInstance(version, currNs, name, ci, st);
	    } catch (CIMException e) {
		throw e;
	    } catch (Exception e) {
		throw new CIMException("RMIERROR", "createInstance", e);
	    }
	}

    }

    /** 
     * @exception CIMException The setQualifierType method throws 
     * 			       a CIM exception.
     */     
    public synchronized void setQualifierType(String version, 
    CIMNameSpace currNs, CIMObjectPath name, CIMQualifierType qt)
    throws CIMException {

	String[] oarray = {version, "setQualifierType", csg.toString(currNs),
			   csg.toString(name), csg.toString(qt)};

	synchronized (cs) {
	    SecurityToken st = cs.getSecurityToken(oarray);
	    cs.incSessionKey();
	    try {
		comp.setQualifierType(version, currNs, name, qt, st);
	    } catch (CIMException e) {
		throw e;
	    } catch (Exception e) {
		throw new CIMException("RMIERROR", "setQualifierType", e);
	    }
	}
    }

    /**
     * @exception CIMException The setClass method throws a CIM exception if
     *                    the class does not exist. <br>or <br>Throws a CIM
     *                    Exception if an RMI error is detected
     */
    public synchronized void setClass(String version, 
				      CIMNameSpace currNs,
    				      CIMObjectPath name, 
				      CIMClass cc) throws CIMException {

	String[] oarray = {version, "setClass", csg.toString(currNs),
	csg.toString(name), csg.toString(cc)};

	synchronized (cs) {
	    SecurityToken st = cs.getSecurityToken(oarray);
	    cs.incSessionKey();
	    try {
		comp.setClass(version, currNs, name, cc, st);
	    } catch (CIMException e) {
		throw e;
	    } catch (Exception e) {
		throw new CIMException("RMIERROR", "setClass", e);
	    }
	}

    }

    /**
     * @exception CIMException The setInstance method throws a CIM exception.
     *                    <br>or <br>if an RMI error is detected
     */
    public synchronized void setInstance(String version, 
					 CIMNameSpace currNs,
    					 CIMObjectPath name, 
					 CIMInstance ci,
					 boolean includeQualifier,
					 String[] propertyList) 
					 throws CIMException {

	String[] oarray = {version, "setInstance", csg.toString(currNs),
			   csg.toString(name), csg.toString(ci)};

	synchronized (cs) {
	    SecurityToken st = cs.getSecurityToken(oarray);
	    cs.incSessionKey();
	    try {
		comp.setInstance(version, currNs, name, ci, 
		includeQualifier, propertyList, st);
	    } catch (CIMException e) {
		throw e;
	    } catch (Exception e) {
		throw new CIMException("RMIERROR", "setInstance", e);
	    }
	}

    }

    /**
     * @exception CIMException The getProperty method throws a CIM exception.
     *                    <br>or <br>if an RMI error is detected
     */
    public synchronized CIMValue getProperty(String version,
    					     CIMNameSpace currNs, 
					     CIMObjectPath name, 
					     String propertyName)
    	throws CIMException {

	String[] oarray = {version, "getProperty", csg.toString(currNs),
	csg.toString(name), propertyName};

	synchronized (cs) {
	    SecurityToken st = cs.getSecurityToken(oarray);
	    cs.incSessionKey();
	    try {
		return comp.getProperty(version, currNs, name, propertyName,
					st);
	    } catch (CIMException e) {
		throw e;
	    } catch (Exception e) {
		throw new CIMException("RMIERROR", "getProperty", e);
	    }
	}

    }

    /**
     * @exception CIMException The setProperty method throws a CIM exception.
     *                    <br>or <br>if an RMI error is detected
     */
    public synchronized void setProperty(String version, CIMNameSpace currNs,
    CIMObjectPath name, String propertyName, CIMValue cv)
    throws CIMException {

	String[] oarray = {version, "setProperty", csg.toString(currNs),
	csg.toString(name), propertyName, csg.toString(cv)};

	synchronized (cs) {
	    SecurityToken st = cs.getSecurityToken(oarray);
	    cs.incSessionKey();
	    try {
		comp.setProperty(version, currNs, name, propertyName, cv, st);
	    } catch (CIMException e) {
		throw e;
	    } catch (Exception e) {
		throw new CIMException("RMIERROR", "setProperty", e);
	    }
	}

    }
    
    public synchronized Vector execQuery(String version, CIMNameSpace currNs,
    CIMObjectPath relNS, String query, String ql) throws CIMException {

	String[] oarray = {version, "execQuery", csg.toString(currNs),
	csg.toString(relNS), query, ql+""};

	synchronized (cs) {
	    SecurityToken st = cs.getSecurityToken(oarray);
	    cs.incSessionKey();
	    try {
		return comp.execQuery(version, currNs, relNS, query, ql, st);
	    } catch (CIMException e) {
		throw e;
	    } catch (Exception e) {
		throw new CIMException("RMIERROR", "execQuery", e);
	    }
	}

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

	String fName = "associators";

	String[] oarray = {version, fName, csg.toString(currNs),
	csg.toString(objectName), assocClass, resultClass, role, resultRole};

	synchronized (cs) {
	    SecurityToken st = cs.getSecurityToken(oarray);
	    cs.incSessionKey();

	    try {
		return comp.associators(version, currNs, objectName, 
					assocClass, resultClass, role,
					resultRole, includeQualifiers,
					includeClassOrigin,
					propertyList, st);

	    } catch (CIMException e) {
		throw e;
	    } catch (Exception e) {
		throw new CIMException("RMIERROR", fName, e);
	    }
	}

    }

    public Vector associatorNames(String version, CIMNameSpace currNs,
				CIMObjectPath objectName,
				String assocClass,
				String resultClass,
				String role,
				String resultRole) throws CIMException {

	String[] oarray = {version, "associatorNames", csg.toString(currNs),
	csg.toString(objectName), assocClass, resultClass, role, resultRole};

	synchronized (cs) {
	    SecurityToken st = cs.getSecurityToken(oarray);
	    cs.incSessionKey();

	    try {
		return comp.associatorNames(version, currNs,
					   objectName,
					   assocClass,
					   resultClass,
					   role,
					   resultRole,
					   st);
	    } catch (CIMException e) {
		throw e;
	    } catch (Exception e) {
		throw new CIMException("RMIERROR", "associatorNames", e);
	    }
	}

    }

    public Vector references(String version, CIMNameSpace currNs,
				CIMObjectPath objectName,
				String resultClass,
				String role,
				boolean includeQualifiers,
				boolean includeClassOrigin,
				String propertyList[]) throws CIMException {

	String[] oarray = {version, "references", csg.toString(currNs),
	csg.toString(objectName), resultClass, role};

	synchronized (cs) {
	    SecurityToken st = cs.getSecurityToken(oarray);
	    cs.incSessionKey();

	    try {
		return comp.references(version, currNs, objectName,
					resultClass, role, 
					includeQualifiers,
					includeClassOrigin,
					propertyList,
					st);
	    } catch (CIMException e) {
		throw e;
	    } catch (Exception e) {
		throw new CIMException("RMIERROR", "references", e);
	    }
	}

    }

    public Vector referenceNames(String version, CIMNameSpace currNs,
				CIMObjectPath objectName,
				String resultClass,
				String role) 
				throws CIMException {

	String[] oarray = {version, "referenceNames", csg.toString(currNs),
	csg.toString(objectName), resultClass, role};

	synchronized (cs) {
	    SecurityToken st = cs.getSecurityToken(oarray);
	    cs.incSessionKey();

	    try {
		return comp.referenceNames(version, currNs, objectName,
					   resultClass, role, st);
	    } catch (CIMException e) {
		throw e;
	    } catch (Exception e) {
		throw new CIMException("RMIERROR", "referenceNames", e);
	    }
	}

    }


    public Vector performOperations(String version,
				CIMOperation[] batchedOperations)
				throws CIMException {

	StringBuffer buff = new StringBuffer();
	for (int i = 0; i < batchedOperations.length; i++) {
	    buff.append(batchedOperations[i].getClass().getName());
	}
	String[] oarray = {version, buff.toString()};
	
	synchronized (cs) {
	    SecurityToken st = cs.getSecurityToken(oarray);
	    cs.incSessionKey();

	    try {
		return comp.performOperations(version, 
					      batchedOperations, st);
	    } catch (CIMException e) {
		throw e;
	    } catch (Exception e) {
		throw new CIMException("RMIERROR", "performOperations", e);
	    }
	}
    }

    /**
     * @exception CIMException The assumeRole method throws a CIM exception.
     */
    private void assumeRole(String version, String roleName, String encrPswd)
	throws CIMException {

	String[] oarray = {version, "assumeRole", roleName, encrPswd};

	synchronized (cs) {
	    SecurityToken st = cs.getSecurityToken(oarray);
	    cs.incSessionKey();

	    try {
	  	comp.assumeRole(version, roleName, encrPswd, st);
	    } catch (CIMException e) {
		throw e;
	    } catch (Exception e) {
		throw new CIMException("RMIERROR", "assumeRole", e);
	    }
	}

    }

    /**
     * @exception CIMException The hello method throws a CIM exception. <br>or
     *                    <br>if an RMI error is detected
     */
    private SecurityMessage hello(String version, SecurityMessage cm,
    String country, String language, String variant, String[] otherInfo)
    throws CIMException {
	try {
	    return comp.hello(version, cm, country, language, variant, 
	    otherInfo);
	} catch (CIMException e) {
	    throw e;
	} catch (Exception e) {
	    // e.printStackTrace();
	    throw new CIMException("RMIERROR", "hello", e);
	}
    }

    /**
     * @exception CIMException The credentials method throws a CIM exception.
     *                    <br>or <br>if an RMI error is detected
     */
    private SecurityMessage credentials(String version, SecurityMessage cm)
    throws CIMException {
	try {
	    Object[] returnVals = comp.credentials(version, cm);
	    heartBeatObj = returnVals[1];
	    return (SecurityMessage)returnVals[0];
	} catch (CIMException e) {
	    throw e;
	} catch (Exception e) {
	    throw new CIMException("RMIERROR", "credentials", e);
	}
    }

    public void setListener(String version)
    throws CIMException {
	setListener(version, 0);
    }

    public void setListener(String version, int port)
    throws CIMException {
	//XXX port is currently N/A for RMI

	String[] oarray = {version, "setListener"};
	SecurityToken st = cs.getSecurityToken(oarray);

	try {
	    remoteListener = new RemoteListenerImpl(clientListener);
	    comp.setListener(version, remoteListener, st);
	} catch (CIMException e) {
	    try {
		// App wont exit if this remains exported.
		UnicastRemoteObject.unexportObject(remoteListener, false);
	    } catch (Exception e1) {
		// Ignore for now
	    }
	    if (!e.getID().equals(CIMException.CIM_ERR_NOT_SUPPORTED)) {
		throw e;
	    }
	} catch (Exception e) {
	    try {
		// App wont exit if this remains exported.
		UnicastRemoteObject.unexportObject(remoteListener, false);
	    } catch (Exception e1) {
		// Ignore for now
	    }

	    if (e instanceof ServerException) {
		// This most likely means it got an unmarshal on the
		// remoteListener class. We may need to go to CIMOM3 to
		// avoid this kind of check.
		throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
	    }
	    throw new CIMException("RMIERROR", "setListener", e);
	}
    }

    public CIMInstance getIndicationHandler(CIMListener cl) throws 
    CIMException {
	if (cl != null) {
	    // We dont support this right now
	    throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
	}
	CIMInstance ci = new CIMInstance();
	ci.setClassName(HANDLERCLASS);
	// No properties need to be set. These will be handled by the CIMOM.
	return ci;
    }

    public CIMInstance getIndicationListener(CIMListener cl)
        throws CIMException {
        if (cl != null) {
            // We dont support this right now
            throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
        }
        CIMInstance ci = new CIMInstance();
        //ci.setClassName(LISTENERCLASS);
        ci.setClassName(HANDLERCLASS);
        // No properties need to be set. These will be handled by the CIMOM.
        return ci;
    }

}

