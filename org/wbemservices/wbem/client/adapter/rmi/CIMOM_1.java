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

package org.wbemservices.wbem.client.adapter.rmi; 

import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMNameSpace;
import javax.wbem.cim.CIMObjectPath;
import javax.wbem.cim.CIMQualifierType;
import javax.wbem.cim.CIMClass;
import javax.wbem.cim.CIMInstance;
import javax.wbem.cim.CIMValue;
import javax.wbem.client.CIMOperation;
import javax.wbem.cim.CIMArgument;
import javax.wbem.security.SecurityMessage;
import javax.wbem.security.SecurityToken;

import java.rmi.Remote;
import java.util.Vector;

/**
 * The CIMOM interface identifies remote CIM objects.
 * This interface defines methods for adding CIM object names
 * and object types to hashtables (associative arrays).
 *
 * @author      Sun Microsystems, Inc.
 * @since       WBEM 1.0
 *
 */
public interface CIMOM_1 extends Remote {

    // The hello method takes version info, a securityMessage containing
    // user principal info, locale info from the client (country, language,
    // and variant) and an array of otherInfo which may be additional 
    // parameters passed in the future.
    public SecurityMessage hello(String version, SecurityMessage cm,
    String country, String language, String variant, String[] otherInfo)
		throws java.rmi.RemoteException, Exception; 

    // Returns two objects, the SecurityMessage and a remote heartbeat object
    // The CIMOM RMI adapter uses this object to maintain the RMI lease.
    public Object[] credentials(String version, SecurityMessage cm)
		throws java.rmi.RemoteException, Exception; 

    /**
     * Closes the session.
     */
    public void close(String version, SecurityToken st)
		throws java.rmi.RemoteException, Exception; 

    /**
     * Creates a new namespace within the parent nameSpace.
     * object
     * @param parent The parent namespace.
     * @param nameSpace The namespace to be created.
     * @exception CIMException	the namespace already exists.
     *
     * @exception java.rmi.RemoteException RMI exception.
     *
     */
    public void createNameSpace(String version, CIMNameSpace parent, 
    				CIMNameSpace nameSpace,
				SecurityToken st)
		throws java.rmi.RemoteException, CIMException; 

    /**
     * Deletes a namespace within the parent nameSpace.
     * object
     * @param parent The parent namespace.
     * @param nameSpace The namespace to be created.
     * @exception CIMException	the namespace does not exists.
     *
     * @exception java.rmi.RemoteException RMI exception.
     *
     */
    public void deleteNameSpace(String version, CIMNameSpace parent, 
    				CIMNameSpace nameSpace,
				SecurityToken st)
		throws java.rmi.RemoteException, CIMException; 

    /**
     * takes a string for the CIM namespace and a CIM qualifier type
     * object
     *
     * @exception CIMException	
     *				qualifier type has an invalid
     *				name. The qualifier type
     *				name already exists in the
     *				namespace.
     *
     * @exception java.rmi.RemoteException 
     *						
     *
     */
    public void createQualifierType(String version, 
				    CIMNameSpace nameSpace, 
    				    CIMObjectPath name, 
				    CIMQualifierType qt, 
				    SecurityToken st)
		throws java.rmi.RemoteException, CIMException; 
	
    /**
     * takes a string for the CIM namespace and a CIM qualifier type
     * object
     *
     * @exception CIMException	
     * @exception java.rmi.RemoteException 
     *						
     *
     */
    public void setQualifierType(String version, 
				 CIMNameSpace nameSpace, 
    				 CIMObjectPath name, 
				 CIMQualifierType qt, 
				 SecurityToken st)
	throws java.rmi.RemoteException, CIMException; 
	
    /**
     * takes a string for the CIM namespace and a CIM class object 
     *
     * @exception CIMException	the class name already exists
     *				or the schema name is not
     *				prepended to the class name
     *
     * @exception java.rmi.RemoteException 
     *
     */
    public void createClass(String version, 
			    CIMNameSpace nameSpace, 
    			    CIMObjectPath name, 
			    CIMClass cc, 
			    SecurityToken st)
		throws java.rmi.RemoteException, CIMException; 
    
    /**
     * takes a string for the CIM namespace and a CIM class object 
     *
     * @exception CIMException	
     *
     * @exception java.rmi.RemoteException 
     *
     */
    public void setClass(String version, 
			 CIMNameSpace nameSpace, 
    			 CIMObjectPath name, 
			 CIMClass cc, 
			 SecurityToken st)
	throws java.rmi.RemoteException, CIMException; 
    
    /**
     * takes a string for the CIM namespace and a CIM instance object
     *
     * @exception CIMException	the CIM instance already exists
     *				in the namespace. The namespace
     *				and class name do not exist for
     *				this CIM instance.
     *
     * @exception java.rmi.RemoteException the CIM instance already exists
     *				in the namespace. The namespace
     *				and class name do not exist for
     *				this CIM instance.	
     */
    public CIMObjectPath createInstance(String version, 
					CIMNameSpace nameSpace, 
    					CIMObjectPath name, 
					CIMInstance ci, 
					SecurityToken st)
		throws java.rmi.RemoteException, CIMException; 
	
    public void setInstance(String version, 
			    CIMNameSpace nameSpace, 
    			    CIMObjectPath name, 
			    CIMInstance ci, 
			    boolean includeQualifiers,
			    String[] propertyList,
			    SecurityToken st)
	throws java.rmi.RemoteException, CIMException; 
	
    public void deleteClass(String version, CIMNameSpace nameSpace, 
    CIMObjectPath path, SecurityToken st)
		throws java.rmi.RemoteException, CIMException;

    public void deleteInstance(String version, CIMNameSpace nameSpace, 
    CIMObjectPath path, SecurityToken st)
		throws java.rmi.RemoteException, CIMException;

    public void deleteQualifierType(String version, CIMNameSpace nameSpace, 
    CIMObjectPath path, SecurityToken st)
		throws java.rmi.RemoteException, CIMException;

    public Vector enumNameSpace(String version, CIMNameSpace nameSpace, 
    CIMObjectPath path, boolean deep, SecurityToken st)
		throws java.rmi.RemoteException, CIMException;

    public Vector enumQualifierTypes(String version, 
				     CIMNameSpace nameSpace, 
    				     CIMObjectPath path, 
				     SecurityToken st) 
    	throws java.rmi.RemoteException, CIMException;
    
    /**
     * takes a string for the CIM namespace and a string for the
     * object model path (the path to a particular object within 
     * a CIM namespace), and returns the CIM qualifier type for the 
     * specified object
     *
     * @return CIMQualifierType	the CIM qualifier type for the
     *				specified object and namespace
     *
     * @exception CIMException	the namespace does not exist
     *
     * @exception java.rmi.RemoteException the namespace does not exist	
     *
     */
    public CIMQualifierType getQualifierType(String version, 
    CIMNameSpace nameSpace, CIMObjectPath name, SecurityToken st) 
    throws java.rmi.RemoteException, 
						CIMException;
   
    public Vector invokeMethod(String version, CIMNameSpace nameSpace, 
    CIMObjectPath name, String methodName, Vector params, SecurityToken st) 
    throws java.rmi.RemoteException, CIMException;

    // New invoke method which takes in an array of args. The returned
    // vector also has the output CIMArguments - the first element will be
    // the method's return value.
    public Vector invokeMethod(String version, CIMNameSpace nameSpace, 
    CIMObjectPath name, String methodName, CIMArgument[] args, 
    SecurityToken st) throws java.rmi.RemoteException, CIMException;

    public CIMValue getProperty(String version, CIMNameSpace nameSpace, 
    CIMObjectPath name, String propertyName, SecurityToken st) 
    throws java.rmi.RemoteException, CIMException;

    public void setProperty(String version, CIMNameSpace nameSpace, 
    CIMObjectPath name, String propertyName, CIMValue cv, SecurityToken st) 
    throws java.rmi.RemoteException, CIMException;

    public Vector execQuery(String version, CIMNameSpace nameSpace, 
    CIMObjectPath name, String query, String queryLanguage, SecurityToken st) 
    throws java.rmi.RemoteException, CIMException;

    public Vector enumerateClasses(String version,
				   CIMNameSpace currNs,
				   CIMObjectPath path,
				   boolean deep,
				   boolean localOnly,
				   boolean includeQualifiers,
				   boolean includeClassOrigin,
		     		   SecurityToken st)
	throws java.rmi.RemoteException, CIMException;

    public Vector enumerateClassNames(String version,
				   CIMNameSpace currNs,
				   CIMObjectPath path,
				   boolean deep,
		     		   SecurityToken st)
	throws java.rmi.RemoteException, CIMException;

    public Vector enumerateInstances(String version,
				     CIMNameSpace currNs,
				     CIMObjectPath path,
				     boolean deep,
				     boolean localOnly,
				     boolean includeQualifiers,
				     boolean includeClassOrigin,
				     String propertyList[],
				     SecurityToken st)
	throws java.rmi.RemoteException, CIMException;

    public Vector enumerateInstanceNames(String version,
					 CIMNameSpace currNs,
					 CIMObjectPath path,
					 SecurityToken st)
	throws java.rmi.RemoteException, CIMException;


    public CIMClass getClass(String version,
			     CIMNameSpace currNs,
			     CIMObjectPath path,
			     boolean localOnly,
			     boolean includeQualifiers,
			     boolean includeClassOrigin,
			     String propertyList[],
			     SecurityToken st) 
	throws java.rmi.RemoteException, CIMException;


    public CIMInstance getInstance(String version,
				   CIMNameSpace currNs,
				   CIMObjectPath path,
				   boolean localOnly,
				   boolean includeQualifiers,
				   boolean includeClassOrigin,
				   String propertyList[],
				   SecurityToken st)  				 
	throws java.rmi.RemoteException, CIMException;

    public Vector associators(String version, CIMNameSpace currNs,
				CIMObjectPath objectName,
				String assocClass,
				String resultClass,
				String role,
				String resultRole,
				boolean includeQualifiers,
				boolean includeClassOrigin,
				String propertyList[],
				SecurityToken st)
	throws java.rmi.RemoteException, CIMException;

    public Vector associatorNames(String version, CIMNameSpace currNs,
				CIMObjectPath objectName,
				String assocClass,
				String resultClass,
				String role,
				String resultRole,
				SecurityToken st)
	throws java.rmi.RemoteException, CIMException;

    public Vector references(String version, CIMNameSpace currNs,
				CIMObjectPath objectName,
				String resultClass,
				String role,
				boolean includeQualifiers,
				boolean includeClassOrigin,
				String propertyList[],
				SecurityToken st)
	throws java.rmi.RemoteException, CIMException;

    public Vector referenceNames(String version, CIMNameSpace currNs,
				CIMObjectPath objectName,
				String resultClass,
				String role,
				SecurityToken st)
	throws java.rmi.RemoteException, CIMException;

    public void assumeRole(String version,
			   String roleName,
			   String rolePasswd,
			   SecurityToken st)
	throws java.rmi.RemoteException, CIMException;
			   

    public Vector getVersion(String version,
			     SecurityToken st)
	throws java.rmi.RemoteException, CIMException;

    public Vector performOperations(String version,
			     	    CIMOperation[] batchedOps,
				    SecurityToken st)
	throws java.rmi.RemoteException, CIMException;

    public void setListener(String version,
			    RemoteCIMListener rl,
			    SecurityToken st)
	throws java.rmi.RemoteException, CIMException;
			 
}
