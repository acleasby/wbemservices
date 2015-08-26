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
package org.wbemservices.wbem.cimom.adapters.client.rmi;

import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMNameSpace;
import javax.wbem.cim.CIMObjectPath;
import javax.wbem.cim.CIMQualifierType;
import javax.wbem.cim.CIMClass;
import javax.wbem.cim.CIMInstance;
import javax.wbem.cim.CIMValue;
import javax.wbem.cim.CIMArgument;
import javax.wbem.client.CIMOperation;
import javax.wbem.client.CIMSecurityException;
import javax.wbem.security.SecurityMessage;
import javax.wbem.security.SecurityToken;

import java.util.Vector;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.server.Unreferenced;

import org.wbemservices.wbem.client.adapter.rmi.RemoteCIMListener;

public class CIMOM_1Impl extends UnicastRemoteObject 
	implements org.wbemservices.wbem.client.adapter.rmi.CIMOM_1 {

    CIMOMRMIImpl comp;
   
    public CIMOM_1Impl(CIMOMRMIImpl comp) throws Exception {
        super();
	this.comp = comp;
    }

    public SecurityMessage hello(String version, 
    				 SecurityMessage cm,
				 String country,
				 String language,
				 String variant,
				 String[] otherInfo) throws CIMException {
	try {
	    String clientHost = null;
	    try {
		clientHost = getClientHost();
	    } catch (Exception ex) {
	    }
	    return comp.hello(version, cm, clientHost);
	} catch (Error e) {
	    System.out.println(e.fillInStackTrace());
	    throw e;
	}
    }


    public Object[] credentials(String version, 
				       SecurityMessage cm) 
	throws CIMException {

	String clientHost = null;
	try {
	    clientHost = getClientHost();
	} catch (Exception ex) {
	}
	try {
	    Object[] resultVals = new Object[2];
	    SecurityMessage sm = comp.credentials(version, cm, clientHost);
	    resultVals[0] = sm;
	    final String sessionID = new String(sm.getSessionId());
	    // Return a RMILease object so that we know if the client has
	    // exited without a close.
	    resultVals[1] = new CIMRMILease_Impl(
		new Unreferenced() {
		    public void unreferenced() {
			// We're not doing anything forn now. We need to
			// clean up the sessionID session as well as any
			// outstanding event subscriptions for that session.
			// This way we dont have to rely on the ping method.
		    }
		}
	    );
	    return resultVals;
	} catch (Exception e) {
	    throw new CIMException(
		CIMSecurityException.INVALID_CREDENTIAL, e.toString());
	} catch (Error e) {
	    System.out.println(e.fillInStackTrace());
	    throw e;
	}
    }

    public void close(String version, SecurityToken st) 
    	throws CIMException {
	comp.close(version, st);
    }

    public void deleteNameSpace(String version, 
				CIMNameSpace parent, 
    			        CIMNameSpace nameSpace, 
				SecurityToken st) 
    	throws CIMException {
	comp.deleteNameSpace(version, parent, nameSpace, st);
    }


    public void createNameSpace(String version, 
				CIMNameSpace parent, 
				CIMNameSpace nameSpace,
				SecurityToken st) 
    	throws CIMException {
	comp.createNameSpace(version, parent, nameSpace, st);
    }

    public void createQualifierType(String version, 
				    CIMNameSpace nameSpace, 
			            CIMObjectPath objectName,
			            CIMQualifierType qt, 
				    SecurityToken st)
    	throws CIMException {
	comp.createQualifierType(version, nameSpace, objectName, qt, st);
    }

    /**
     * Adds a class to the CIMOM Repository
     */
    public void createClass(String version, 
			    CIMNameSpace nameSpace, 
			    CIMObjectPath objectName, 
			    CIMClass cc,
			    SecurityToken st)
    	throws CIMException {
	comp.createClass(version, nameSpace, objectName, cc, st);
    }

    public CIMObjectPath createInstance(String version, 
					CIMNameSpace nameSpace,
					CIMObjectPath objectName,
					CIMInstance ci,
					SecurityToken st)
    	throws CIMException {
	return comp.createInstance(version, nameSpace, objectName, ci, st);
    }

    public void setQualifierType(String version, 
				 CIMNameSpace nameSpace, 
			         CIMObjectPath objectName, 
			         CIMQualifierType qt, 
				 SecurityToken st)
    	throws CIMException {
	comp.setQualifierType(version, nameSpace, objectName, qt, st);
    }

    /**
     * Adds a class to the CIMOM Repository
     */
    public void setClass(String version, 
			 CIMNameSpace nameSpace, 
			 CIMObjectPath objectName, 
			 CIMClass cc,
			 SecurityToken st)
    	throws CIMException {
	comp.setClass(version, nameSpace, objectName, cc, st);
    }

    public void setInstance(String version, 
			    CIMNameSpace nameSpace,
			    CIMObjectPath objectName,
			    CIMInstance ci,
			    boolean includeQualifier,
			    String[] propertyList,
			    SecurityToken st)
    	throws CIMException {
	comp.setInstance(version, nameSpace, objectName, ci, includeQualifier,
	propertyList, st);
    }

    public void setProperty(String version, 
			    CIMNameSpace nameSpace,
			    CIMObjectPath objectName,
			    String propertyName,
			    CIMValue cv,
			    SecurityToken st)
    	throws CIMException {
	comp.setProperty(version, nameSpace, objectName, propertyName, cv, st);
    }

    public Vector execQuery(String version, 
			    CIMNameSpace nameSpace,
			    CIMObjectPath relNS,
			    String query,
			    String ql,
			    SecurityToken st)
    	throws CIMException {
	return comp.execQuery(version, nameSpace, relNS, query, ql, st);
    }


    public Vector invokeMethod(String version, 
			       CIMNameSpace nameSpace, 
			       CIMObjectPath objectName, 
			       String methodName,
			       Vector inParams,
			       SecurityToken st) 
    	throws CIMException {
	return comp.invokeMethod(version, nameSpace, objectName, 
				 methodName, inParams, st);
    }

    public Vector invokeMethod(String version, 
			       CIMNameSpace nameSpace, 
			       CIMObjectPath objectName, 
			       String methodName,
			       CIMArgument[] inArgs,
			       SecurityToken st) 
    	throws CIMException {
	return comp.invokeMethod(version, nameSpace, objectName, 
				 methodName, inArgs, st);
    }

    public CIMValue getProperty(String version, 
				CIMNameSpace nameSpace, 
				CIMObjectPath objectName,
				String propertyName, 
				SecurityToken st)
    	throws CIMException {
	return comp.getProperty(version, nameSpace, objectName, 
				propertyName, st);
    }
    
    public CIMQualifierType getQualifierType(String version, 
					     CIMNameSpace nameSpace,
					     CIMObjectPath objectName,
					     SecurityToken st)
    	throws CIMException {
	return comp.getQualifierType(version, nameSpace, objectName, st);
    }

    public void deleteClass(String version, 
			    CIMNameSpace ns,
			    CIMObjectPath path,
			    SecurityToken st)
    	throws CIMException {
	comp.deleteClass(version, ns, path, st);
    }

    public void deleteInstance(String version, 
			       CIMNameSpace ns, 
			       CIMObjectPath path, 
			       SecurityToken st) 
    throws CIMException {
	comp.deleteInstance(version, ns, path, st);
    }

    public void deleteQualifierType(String version, 
				    CIMNameSpace ns, 
    				    CIMObjectPath path, 
				    SecurityToken st) 
	throws CIMException {
	try {
	    comp.deleteQualifierType(version, ns, path, st);
	} catch (Error e) {
	    System.out.println(e.fillInStackTrace());
	    throw e;
	}
    }

    public Vector enumNameSpace(String version, CIMNameSpace ns, 
				CIMObjectPath path, boolean deep,
				SecurityToken st) 
    throws CIMException {
	return comp.enumNameSpace(version, ns, path, deep, st);
    }

    public Vector enumQualifierTypes(String version, CIMNameSpace ns, 
				     CIMObjectPath path,
				     SecurityToken st) 
    throws CIMException {
	return comp.enumQualifierTypes(version, ns, path, st);
    }

    public Vector enumerateClasses(String version,
                                   CIMNameSpace currNs,
                                   CIMObjectPath path,
                                   boolean deep,
                                   boolean localOnly,
                                   boolean includeQualifiers,
                                   boolean includeClassOrigin,
                                   SecurityToken st)
        throws CIMException {

	return comp.enumerateClasses(version, currNs, path, deep,
				     localOnly, includeQualifiers, 
				     includeClassOrigin, st);
    }

    public Vector enumerateClassNames(String version,
                                   CIMNameSpace currNs,
                                   CIMObjectPath path,
                                   boolean deep,
                                   SecurityToken st)
        throws CIMException {
	
	return comp.enumerateClassNames(version, currNs, path, deep, st);
    }

    public Vector enumerateInstances(String version,
                                     CIMNameSpace currNs,
                                     CIMObjectPath path,
                                     boolean deep,
                                     boolean localOnly,
                                     boolean includeQualifiers,
                                     boolean includeClassOrigin,
                                     String[]  propertyList,
                                     SecurityToken st)
        throws CIMException {
	
	return comp.enumerateInstances(version, currNs, path,
				       deep, localOnly, 
				       includeQualifiers, 
				       includeClassOrigin,
				       propertyList, st);
    }

    public Vector enumerateInstanceNames(String version,
                                         CIMNameSpace currNs,
                                         CIMObjectPath path,
                                         SecurityToken st)
        throws CIMException {

	return comp.enumerateInstanceNames(version, currNs, path, st);
    }


    public CIMClass getClass(String version,
                             CIMNameSpace currNs,
                             CIMObjectPath path,
                             boolean localOnly,
                             boolean includeQualifiers,
                             boolean includeClassOrigin,
                             String[] propertyList,
                             SecurityToken st)
        throws CIMException {

	return comp.getClass(version, currNs, path, localOnly, 
			     includeQualifiers, includeClassOrigin, 
			     propertyList, st);
    }


    public CIMInstance getInstance(String version,
                                   CIMNameSpace currNs,
                                   CIMObjectPath path,
                                   boolean localOnly,
                                   boolean includeQualifiers,
                                   boolean includeClassOrigin,
                                   String[] propertyList,
                                   SecurityToken st)

        throws CIMException {

	return comp.getInstance(version, currNs, path, localOnly,
			   includeQualifiers, includeClassOrigin,
			   propertyList, st);
    }

    public Vector associators(String version, CIMNameSpace currNs,
                                CIMObjectPath objectName,
                                String assocClass,
                                String resultClass,
                                String role,
                                String resultRole,
                                boolean includeQualifiers,
                                boolean includeClassOrigin,
                                String[] propertyList,
                                SecurityToken st)
        throws CIMException {

	return comp.associators(version, currNs, objectName, assocClass,
			   resultClass, role, resultRole, includeQualifiers,
			   includeClassOrigin, propertyList, st);
    }

    public Vector associatorNames(String version, CIMNameSpace currNs,
                                CIMObjectPath objectName,
                                String assocClass,
                                String resultClass,
                                String role,
                                String resultRole,
                                SecurityToken st)
        throws CIMException {
	
	return comp.associatorNames(version, currNs, objectName, assocClass,
				    resultClass, role, resultRole, st);
    }

    public Vector references(String version, CIMNameSpace currNs,
                                CIMObjectPath objectName,
                                String resultClass,
                                String role,
                                boolean includeQualifiers,
                                boolean includeClassOrigin,
                                String[] propertyList,
                                SecurityToken st)
        throws CIMException {

	return comp.references(version, currNs, objectName,
			       resultClass, role, includeQualifiers,
			       includeClassOrigin, propertyList, st);
    }

    public Vector referenceNames(String version, CIMNameSpace currNs,
                                CIMObjectPath objectName,
                                String resultClass,
                                String role,
                                SecurityToken st)
        throws CIMException {

	return comp.referenceNames(version, currNs, objectName,
				   resultClass, role, st);
    }

    public void assumeRole(String version,
                           String roleName,
                           String rolePasswd,
                           SecurityToken st)
        throws CIMException {
	comp.assumeRole(version, roleName, rolePasswd, st);
    }


    public Vector getVersion(String version,
                             SecurityToken st)
        throws CIMException {
	return comp.getVersion(version, st);
    }

    public Vector performOperations(String version,
                                    CIMOperation[] batchedOps,
                                    SecurityToken st)
        throws CIMException {
	return comp.performOperations(version, batchedOps, st);
    }

    public void setListener(String version,
                            RemoteCIMListener rl,
                            SecurityToken st)
        throws CIMException {
	comp.setListener(version, rl, st);
    }

}
