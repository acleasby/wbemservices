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
 *Contributor(s): WBEM Solutions, Inc.
*/
package org.wbemservices.wbem.cimom.adapters.client.http;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.PasswordAuthentication;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;

import javax.wbem.cim.CIMArgument;
import javax.wbem.cim.CIMClass;
import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMInstance;
import javax.wbem.cim.CIMNameSpace;
import javax.wbem.cim.CIMObjectPath;
import javax.wbem.cim.CIMQualifierType;
import javax.wbem.cim.CIMValue;
import javax.wbem.client.CIMSecurityException;
import javax.wbem.security.SecurityUtil;

import org.wbemservices.wbem.cimom.CIMOMServer;
import org.wbemservices.wbem.cimom.ServerSecurity;

class CIMOMXMLImpl {

    private final String LOOPBACK_ADDRESS = "127.0.0.1";
    private static String localIP = null;
    private static boolean verbose = false;

    // Map of client contexts and the session ids.
    private HashMap sessionMap = new HashMap();

    CIMOMServer comp;

    CIMOMXMLImpl(CIMOMServer comp) throws Exception {
		this.comp = comp;
    }
    
	CIMOMServer getCIMOMImp() {
		return comp;
	}
	
    void hello(String version) 	throws CIMException  {
    }

    Enumeration enumerateClasses(String version,
				   CIMNameSpace nameSpace,
				   CIMObjectPath className,
				   boolean deep,
				   boolean localOnly,
				   boolean includeQualifiers,
				   boolean includeClassOrigin,
				   PasswordAuthentication auth)  
    throws CIMException {
	try {
	    ServerSecurity ss = getServerSecurityDo(auth);
	    Vector rt = comp.enumerateClasses(version, nameSpace, className,
					      new Boolean(deep),
					      new Boolean(localOnly),
					      new Boolean(includeQualifiers), 
					      new Boolean(includeClassOrigin),
					      ss);
	    return rt.elements();
	} catch (CIMException e) {
	    if (verbose) {
		e.printStackTrace();
	    }
	    throw e;
	}
    }

   CIMInstance getInstance(String version, CIMNameSpace currNs,
		CIMObjectPath path, boolean localOnly, boolean includeQualifiers,
		boolean includeClassOrigin, String propertyList[], 
		PasswordAuthentication auth) throws CIMException {
			
		ServerSecurity ss = getServerSecurityDo(auth);
		return comp.getInstance(version, currNs, path,
			new Boolean(localOnly), new Boolean(includeQualifiers), 
			new Boolean(includeClassOrigin), propertyList, ss);
       
   } 

	Enumeration enumerateInstances(String version, CIMNameSpace currNs,
		CIMObjectPath path, boolean deep, boolean localOnly,
		boolean includeQualifiers, boolean includeClassOrigin,
		String propertyList[], PasswordAuthentication auth)
		throws CIMException {
		ServerSecurity ss = getServerSecurityDo(auth);
		Vector rt =
			comp.enumerateInstances(
				version, currNs, path, new Boolean(deep), 
				new Boolean(localOnly),new Boolean(includeQualifiers),
				new Boolean(includeClassOrigin), propertyList, ss);
		return rt.elements();
	}

    CIMValue getProperty(String version, CIMNameSpace nameSpace, 
    	CIMObjectPath objectName, String propertyName, PasswordAuthentication auth)
    	throws CIMException {
    		
		ServerSecurity ss = getServerSecurityDo(auth);
		return comp.getProperty(version, nameSpace, objectName, propertyName, ss);
    }

    void setProperty(String version, CIMNameSpace nameSpace,
					   CIMObjectPath objectName,
					   String propertyName,
					   CIMValue cv,
					   PasswordAuthentication auth)
    throws CIMException {
	ServerSecurity ss = getServerSecurityDo(auth);
	comp.setProperty(version, nameSpace, objectName, 
			 propertyName, cv, ss);
    }

 

    void addCIMElement(String version, CIMNameSpace nameSpace, 
					   CIMObjectPath objectName, 
					   CIMClass cc,
					   PasswordAuthentication auth)
    throws CIMException {
	ServerSecurity ss = getServerSecurityDo(auth);
	comp.createClass(version, nameSpace, objectName, cc, ss);
    }


    void setCIMElement(String version, CIMNameSpace nameSpace, 
					   CIMObjectPath objectName, 
					   CIMClass cc,
					   PasswordAuthentication auth)
    throws CIMException {
	ServerSecurity ss = getServerSecurityDo(auth);
	comp.setClass(version, nameSpace, objectName, cc, ss);
    }


    Enumeration associators(String version, 
				CIMNameSpace nameSpace,
				CIMObjectPath objectName,
				String assocClass,
				String resultClass,
				String role,
				String resultRole,
				boolean includeQualifiers,
				boolean includeClassOrigin,
				String propertyList[],
				PasswordAuthentication auth)
     throws CIMException {
	 ServerSecurity ss = getServerSecurityDo(auth);
	 Vector rt =comp.associators(version, nameSpace, objectName, assocClass, 
			   resultClass, role, resultRole,
			   new Boolean (includeQualifiers),
			   new Boolean (includeClassOrigin),
			   propertyList, ss);
	 return rt.elements();

    }

    Enumeration associatorNames(String version, 
				CIMNameSpace nameSpace,
				CIMObjectPath objectName,
				String assocClass,
				String resultClass,
				String role,
				String resultRole,
				PasswordAuthentication auth)
    throws CIMException {
	 ServerSecurity ss = getServerSecurityDo(auth);
	 Vector rt =comp.associatorNames(version, nameSpace, objectName, assocClass, 
			   resultClass, role, resultRole, ss);
	 return rt.elements();
    }

    Enumeration references(String version, 
				CIMNameSpace nameSpace,
				CIMObjectPath objectName,
				String resultClass,
				String role,
				boolean includeQualifiers,
				boolean includeClassOrigin,
				String propertyList[],
				PasswordAuthentication auth) 
    throws CIMException {
	 ServerSecurity ss = getServerSecurityDo(auth);
	 Vector rt =comp.references(version, nameSpace, objectName, resultClass, 
			   role, new Boolean(includeQualifiers), 
			   new Boolean(includeClassOrigin), propertyList, ss);
	 return rt.elements();
    }


    Enumeration referenceNames(String version, 
				CIMNameSpace nameSpace,
				CIMObjectPath objectName,
				String resultClass,
				String role,
				PasswordAuthentication auth) 
    throws CIMException {
	 ServerSecurity ss = getServerSecurityDo(auth);
	 Vector rt = comp.referenceNames(version, nameSpace, objectName, resultClass, 
			   role, ss);
	 return rt.elements();
    }


    CIMObjectPath addCIMElement(String version, CIMNameSpace nameSpace,
					   CIMObjectPath objectName,
					   CIMInstance ci,
					   PasswordAuthentication auth)
    throws CIMException {
	ServerSecurity ss = getServerSecurityDo(auth);
	return comp.createInstance(version, nameSpace, objectName,
				      ci, ss);
    }


    void setCIMElement(String version, CIMNameSpace nameSpace,
					   CIMObjectPath objectName,
					   CIMInstance ci,
					   PasswordAuthentication auth)
    throws CIMException {
	ServerSecurity ss = getServerSecurityDo(auth);
	comp.setInstance(version, nameSpace, objectName, ci, ss);
    }

    void setCIMElement(String version, CIMNameSpace nameSpace,
					   CIMObjectPath objectName,
					   CIMInstance ci,
					   boolean includeQualifiers,
					   String propertyList[],
					   PasswordAuthentication auth)
    throws CIMException {
	ServerSecurity ss = getServerSecurityDo(auth);
	comp.setInstance(version, nameSpace, objectName, ci, 
			includeQualifiers, propertyList, ss);
    }

    void deleteInstance(String version, CIMNameSpace ns, 
		CIMObjectPath path, PasswordAuthentication auth)
    throws CIMException {
	ServerSecurity ss = getServerSecurityDo(auth);
	comp.deleteInstance(version, ns, path, ss);
    }

    Enumeration enumNameSpace(String version, CIMNameSpace ns, 
				CIMObjectPath path, boolean deep, PasswordAuthentication auth) 
    throws CIMException {
	ServerSecurity ss = getServerSecurityDo(auth);
	Vector rt =  comp.enumNameSpace(version, ns, path, deep, ss);
	return rt.elements();

    }

  
    void deleteQualifierType(String version, CIMNameSpace ns, 
			     CIMObjectPath path, 
			     PasswordAuthentication auth)
    throws CIMException {
	ServerSecurity ss = getServerSecurityDo(auth);
	comp.deleteQualifierType(version, ns, path, ss);
    }

    CIMQualifierType getQualifierType(String version, 
				      CIMNameSpace nameSpace, 
				      CIMObjectPath objectName,
				      PasswordAuthentication auth)
    throws CIMException {
	ServerSecurity ss = getServerSecurityDo(auth);
	return comp.getQualifierType(version, nameSpace, objectName,
					 ss);
    }


    Enumeration enumQualifierTypes(String version, 
				   CIMNameSpace ns, 
				   CIMObjectPath path, 
				   PasswordAuthentication auth)
    throws CIMException {
	ServerSecurity ss = getServerSecurityDo(auth);
	Vector rt =  comp.enumQualifierTypes(version, ns, path, ss);
	return rt.elements();
    }

    void setCIMElement(String version, CIMNameSpace nameSpace, 
	    	       CIMObjectPath objectName, CIMQualifierType qt, 
		       PasswordAuthentication auth)
    throws CIMException {
	ServerSecurity ss = getServerSecurityDo(auth);
	comp.setQualifierType(version, nameSpace, objectName, qt, ss);
    }


    Enumeration execQuery(String version, CIMNameSpace nameSpace,
		CIMObjectPath relNS, String query, String ql, 
		PasswordAuthentication auth) throws CIMException {
			
		ServerSecurity ss = getServerSecurityDo(auth);
		Vector rt = comp.execQuery(version, nameSpace, relNS, query,  ql, ss);
		return rt.elements(); 				 
    }

	Vector invokeMethod(String version, CIMNameSpace nameSpace,
		CIMObjectPath objectName, String methodName, CIMArgument[] inParams,
		PasswordAuthentication auth) throws CIMException {
			
		ServerSecurity ss = getServerSecurityDo(auth);
		return comp.invokeMethod(version, nameSpace, objectName, methodName, 
			inParams, ss);
	}

    void deleteClass(String version, CIMNameSpace ns, CIMObjectPath path,
    	PasswordAuthentication auth) throws CIMException {

		ServerSecurity ss = getServerSecurityDo(auth);
		comp.deleteClass(version, ns, path, ss);
    }


    void deleteNameSpace(String version, CIMNameSpace parent, 
		CIMNameSpace nameSpace, PasswordAuthentication auth) 
		throws CIMException {
			
		try {
		    ServerSecurity ss = getServerSecurityDo(auth);
		    comp.deleteNameSpace(version, parent, nameSpace, ss);
		} catch (CIMException e) {
		    if (verbose) {
			e.printStackTrace();
		    }
		    throw e;
		} catch (Error e) {
		    throw e;
		}
    }


    void createNameSpace(String version, CIMNameSpace parent, 
		CIMNameSpace nameSpace, PasswordAuthentication auth) 
    	throws CIMException {
    		
		try {
		    ServerSecurity ss = getServerSecurityDo(auth);
		    comp.createNameSpace(version, parent, nameSpace, ss);
		} catch (CIMException e) {
		    throw e;
		} catch (Error e) {
		    throw e;
		}
    }

    void addCIMElement(String version, CIMNameSpace nameSpace, 
		CIMObjectPath objectName, CIMQualifierType qt, 
		PasswordAuthentication auth) throws CIMException {

	    ServerSecurity ss = getServerSecurityDo(auth);
	    comp.createQualifierType(version, nameSpace, objectName, qt, ss);
    }



	Enumeration enumerateClassNames(String version, CIMNameSpace ns,
		CIMObjectPath path, boolean deep, PasswordAuthentication auth) 
		throws CIMException {
			
		ServerSecurity ss = getServerSecurityDo(auth);
		Vector rt =  comp.enumerateClassNames(version, ns, path, 
			new Boolean(deep), ss);
			return rt.elements();		
	}

	Enumeration enumerateInstanceNames(String version, CIMNameSpace ns, 
		CIMObjectPath path,	PasswordAuthentication auth) throws CIMException {
		try {
			ServerSecurity ss = getServerSecurityDo(auth);
			Vector rt = comp.enumerateInstanceNames(version, ns, path, ss);
			return rt.elements();
		} catch (CIMException e) {
			throw e;
		}
	}


    private ServerSecurity getServerSecurityDo(PasswordAuthentication auth) 
		throws CIMSecurityException {

		synchronized (sessionMap) {
		    ServerSecurity ss =  (ServerSecurity)sessionMap.get(auth);
		    if (ss == null) {
			// mimic CIMOMRMIImpl
			byte[] sessionKey = new byte[5];
			SecurityUtil.secrand.nextBytes(sessionKey);
			sessionKey = convertTo16(sessionKey);
			byte[] auditKey = new byte[4];
			System.arraycopy(sessionKey, 0, auditKey, 0, 4);
	
			//ServerSecurity("root", null, "localhost", new byte[4]);
			ss = new ServerSecurity(auth.getUserName(), null, 
						getLocalIPAddress(), auditKey);
		    }
		    return ss;
		}
    }

    
	private byte[] convertTo16(byte[] sessionKey) {
		byte[] output = new byte[16];
		System.arraycopy(sessionKey, 0, output, 0, 5);
		System.arraycopy(sessionKey, 0, output, 5, 5);
		System.arraycopy(sessionKey, 0, output, 10, 5);
		output[15] = sessionKey[0];
		return output;
	}

    CIMClass getClass(String version, CIMNameSpace nameSpace, 
    	CIMObjectPath objectName, boolean localOnly, boolean includeQualifiers,
		boolean includeClassOrigin, String[] propertyList, 
		PasswordAuthentication auth) throws CIMException {
		try {
		    ServerSecurity ss = getServerSecurityDo(auth);
		    return comp.getClass(version, nameSpace, objectName, 
				         new Boolean(localOnly), 
				         new Boolean(includeQualifiers), 
				         new Boolean(includeClassOrigin),
				         propertyList,
					 ss);
		} catch (CIMException e) {
		    if (verbose) {
				e.printStackTrace();
		    }
		    throw e;
		} catch (Error e) {
		    System.out.println(e.fillInStackTrace());
		    throw e;
		}
    }

    // Get the IP address of the local machine. 
    private String getLocalIPAddress() {
	// if we've already found local IP, return it
	if (localIP != null) {
	    return localIP;
	}
        try {
	    // get all network interfaces
	    Enumeration e = NetworkInterface.getNetworkInterfaces();
	    while (e.hasMoreElements()) {
		NetworkInterface ni = (NetworkInterface)e.nextElement();
		Enumeration e1 = ni.getInetAddresses();
		// get all InetAddresses for each NetworkInterface
		while (e1.hasMoreElements()) {
		    InetAddress inetAddress = (InetAddress)e1.nextElement();
		    String address = inetAddress.getHostAddress();
		    // if the address is not null, empty or the loopback 
		    // address, set static localIP variable and return it
		    if ((address != null) &&
			(address.trim().length() != 0) && 
			(!address.equalsIgnoreCase(LOOPBACK_ADDRESS)) ) {
			localIP = address;
			return address;
		    }	
		}
	    } 
	    
        } catch (Exception e) {
            // ignore, use loopback
        }
	// couldn't find valid local ip address.  Set static local ip address
	// to loopback address and return it.
	localIP = LOOPBACK_ADDRESS;
        return localIP;
    }

}
