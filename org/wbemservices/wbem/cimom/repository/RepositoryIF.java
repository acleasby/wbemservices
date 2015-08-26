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

package org.wbemservices.wbem.cimom.repository;


import java.util.Vector;

import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMInstance;
import javax.wbem.cim.CIMQualifierType;
import javax.wbem.cim.CIMClass;
import javax.wbem.cim.CIMObjectPath;



/*
 * CIM Repository Interface
 */

public interface RepositoryIF {

    /**
     *
     * @parm namespace    name of  namespace that needs to be created.
     */
    public void createNameSpace(String namespace)
    throws CIMException; 

   /*
    * Add this qualifier to the specified namespace
    *
    * @param namespace    name of namespace that contains this qualifier
    * @param qt    the qualifier to be added
    */
    public void addCIMElement(String namespace, 
			      CIMQualifierType qt) 
			     throws CIMException; 

   /*
    * Add this class to the specified namespace
    *
    * @param namespace    name of namespace that contains this class
    * @param cc    the class to be added
    */
    public void addCIMElement(String namespace, 
			      CIMClass cc)
			     throws CIMException;
    /**
     * @param namespace    name of namespace that contains this instance
     * @param ci    the instance to be added
     */
    public void addCIMElement(String namespace, 
			      CIMInstance ci)
			     throws CIMException;

    /*
     * Replace the existing qualifier with the specified one
     *
     * @param namespace    name of namespace that contains this qualifier
     * @param qt    the new qualifier to be added
     */
    public void setQualifierType(String namespace, 
		    		 CIMQualifierType qt)
    				throws CIMException;

    /*
     * Replace the existing class with the specified one
     *
     * @param namespace    name of namespace that contains this class
     * @param cc    the new class to be added
     */
    public void setClass(String namespace, 
			 CIMClass cc)
    			throws CIMException;

    /*
     * Replace the existing instance with the specified one
     *
     * @param namespace    name of namespace that contains this instance
     * @param qt    the new instance to be added
     */
    public void setInstance(String namespace, 
			    CIMInstance ci)
    			   throws CIMException;

    /*
     * Retrieve the qualifier that specified by the CIMObjectPath
     *
     * @param op    the CIMObjectPath of this qualifier
     * @return CIMQualifierType    the retrived CIMQualifierType object
     */
    public CIMQualifierType getQualifierType(CIMObjectPath op) 
    					   throws CIMException;

    /*
     * Retrieve the qualifier that specified by the qualifier name
     *
     * @param namespace    name of namespace that contains this qualifier
     * @param qtName    the name of this qualifier
     * @return CIMQualifierType    the retrived CIMQualifierType object
     */
    public CIMQualifierType getQualifierType(String namespace, 
					     String qtName) 
    					    throws CIMException;

    /*
     * Retrieve the class that specified by the CIMObjectPath
     *
     * @param op    the CIMObjectPath of this class
     * @return CIMClass    the retrived CIMClass object
     */
    public CIMClass getClass(CIMObjectPath op) 
			    throws CIMException;

    /*
     * Retrieve the class that specified by the class name
     *
     * @param namespace    name of namespace that contains this class
     * @param className    the name of this class
     * @return CIMClass    the retrived CIMClass object
     */
    public CIMClass getClass(String namespace,
			     String className) 
    			    throws CIMException;

    /*
     * Retrieve the instance that specified by the CIMObjectPath
     *
     * @param op    the CIMObjectPath of this instance
     * @return CIMInstance    the retrived CIMInstance object
     */
    public CIMInstance getInstance(CIMObjectPath op) 
    				 throws CIMException;
 
    /*
     * Retrieve the instance that specified by the instance name
     *
     * @param namespace    name of namespace that contains this instance
     * @param iname    the name of this instance
     * @return CIMInstance    the retrived CIMInstance object
     */
    public CIMInstance getInstance(String namespace, 
		    		   String iname) 
    				  throws CIMException;

    /*
     * Delete the specified namespace
     *
     * @param namespace    name of namespace that needs to be deleted
     */
    public void deleteNameSpace(String namespace)
    				throws CIMException;


    /*
     * Delete the specified qualifier
     *
     * @param op    the CIMObjectPath of this qualifier
     */
    public void deleteQualifier(CIMObjectPath op)
    			       throws CIMException;
 
    /*
     * Delete the specified qualifier
     *
     * @param namespace    name of namespace that contains this qualifier
     * @param qtName    the name of this qualifier
     */
    public void deleteQualifier(String namespace, 
		    		String qtName)
    			       throws CIMException;

    /*
     * Delete the specified class
     *
     * @param op    the CIMObjectPath of this class
     */
    public void deleteClass(CIMObjectPath op)
    throws CIMException;

    /*
     * Delete the specified class
     *
     * @param namespace    name of namespace that contains this class
     * @param className    the name of this class
     */
    public void deleteClass(String namespace, String className)
    throws CIMException;

    /*
     * Delete the specified instance
     *
     * @param op    the CIMObjectPath of this instance
     */
    public void deleteInstance(CIMObjectPath op)
    throws CIMException;

    /*
     * Delete the specified instance
     *
     * @param namespace    name of namespace that contains this instance
     * @param iname the name of this class
     */
    public void deleteInstance(String namespace, String iname)
    throws CIMException;

    /*
     * Enumerate the specified namespace
     *
     * @param namespace    name of namesapce that needs to be enumerated
     * @param deep    true for deep enumeration; false for shallow enumeration
     * @return Vector    list of name of namespaces
     */
    public Vector enumerateNameSpace(String namespace, boolean deep)
    throws CIMException;


    /*
     * Enumerate qualifiers of the specified namespace
     *
     * @param op    the CIMObjectPath of namesapce 
     * @return Vector    list of CIMObjectPath of qualifiers
     */
    public Vector enumerateQualifierTypes(CIMObjectPath op) 
    throws CIMException;

    /*
     * Enumerate qualifiers of the specified namespace
     *
     * @param namespace    name of namesapce that needs to be enumerated
     * @return Vector    list of CIMObjectPath of qualifiers
     */
    public Vector enumerateQualifierTypes(String namespace)
    throws CIMException;

    /*
     * Enumerate classes of the specified class
     *
     * @param op    the CIMObjectPath of this class
     * @param deep    true for deep enumeration; false for shallow enumeration
     * @return Vector    list of CIMObjectPath of classes
     */
    public Vector enumerateClasses(CIMObjectPath op, boolean deep) 
    throws CIMException;

    /*
     * Enumerate classes of the specified class
     *
     * @param namespace    name of namespace that contains this class
     * @param className    name of this class
     * @param deep    true for deep enumeration; false for shallow enumeration
     * @return Vector    list of CIMObjectPath of classes
     */
    public Vector enumerateClasses(String namespace, String className, boolean deep) 
    throws CIMException;

    /*
     * Enumerate classes of the specified class
     *
     * @param op    the CIMObjectPath of this class
     * @param deep    true for deep enumeration; false for shallow enumeration
     * @param localonly    true for class contains only local elements
     * @return Vector   list of CIMClass of classes
     */
    public Vector enumerateClasses(CIMObjectPath op, boolean deep, 
				   boolean localonly) 
    				  throws CIMException;

    /*
     * Enumerate classes of the specified class
     *
     * @param namespace    name of namespace that contains this class
     * @param className    name of this class
     * @param deep    true for deep enumeration; false for shallow enumeration
     * @param localonly    true for class conatins only local elements
     * @return Vector   list of CIMClass of classes
     */
    public Vector enumerateClasses(String namespace, 
		    		   String className, 
		    		   boolean deep,
		    		   boolean localonly) 
    				  throws CIMException;

    /*
     * Enumerate instances of the specified class
     *
     * @param op    the CIMObjectPath of this class
     * @param deep    true for deep enumeration; false for shallow enumeration
     * @return Vector   list of CIMObjectPath of instances
     */
    public Vector enumerateInstances(CIMObjectPath op, 
				     boolean deep) 
    				    throws CIMException;

    /*
     * Enumerate instances of the specified class
     *
     * @param namespace    name of namespace that contains this class
     * @param className    name of this classs
     * @param deep    true for deep enumeration; false for shallow enumeration
     * @return Vector   list of CIMObjectPath of instances
     */
    public Vector enumerateInstances(String namespace, 
				     String className, 
				     boolean deep) 
    				    throws CIMException;

    /*
     * Enumerate instances of the specified class
     *
     * @param op    the CIMObjectPath of this class
     * @param deep    true for deep enumeration; false for shallow enumeration
     * @param localonly    true for class contains only local elements
     * @return Vector   list of CIMInstance of instances
     */
    public Vector enumerateInstances(CIMObjectPath op, 
				     boolean deep, 
				     boolean localonly) 
    				    throws CIMException;

    /*
     * Enumerate instances of the specified class
     *
     * @param namespace    name of namespace that contains this class
     * @param className    name of this classs
     * @param deep    true for deep enumeration; false for shallow enumeration
     * @param localonly    true for class contains only local elements
     * @return Vector   list of CIMInsatnce of instances
     */
    public Vector enumerateInstances(String namespace, 
		    		     String className, 
		    		     boolean deep,
		    		     boolean localonly) 
    				    throws CIMException;

    /*
     * Execute a query statemnet
     *
     * @param namespace    name of namespace that the query operates against
     * @param stmt    the query statement
     * @param int    1:WQL
     * @param cc	CIMClass to exec the query on
     * @return Vector    List of selected CIMElement
     */
    public Vector execQuery(CIMObjectPath op,
			    String stmt,
			    String queryType,
			    CIMClass cc)
			   throws CIMException;

}
