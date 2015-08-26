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

import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMInstance;
import javax.wbem.cim.CIMNameSpace;
import javax.wbem.cim.CIMObjectPath;
import javax.wbem.cim.CIMQualifierType;
import javax.wbem.cim.CIMClass;
import javax.wbem.cim.CIMValue;
import javax.wbem.cim.CIMArgument;
import java.util.Vector;

public interface CIMOMServer {

    public Vector enumerateClasses(String version, CIMNameSpace currNs,
				   CIMObjectPath path, Boolean deep,
				   Boolean localOnly,
				   Boolean includeQualifiers,
				   Boolean includeClassOrigin,
				   ServerSecurity ss) throws CIMException ;

    public Vector enumerateClassNames(String version,
				      CIMNameSpace currNs,
				      CIMObjectPath path,
				      Boolean deep,
				      ServerSecurity ss) throws CIMException;

    public Vector enumerateInstances(String version,
				     CIMNameSpace currNs,
				     CIMObjectPath path,
				     Boolean deep,
				     Boolean localOnly,
				     Boolean includeQualifiers,
				     Boolean includeClassOrigin,
				     String propertyList[],
				     ServerSecurity ss) throws CIMException;

    public Vector enumerateInstanceNames(String version,
					 CIMNameSpace currNs,
					 CIMObjectPath path,
					 ServerSecurity ss) throws CIMException;

    public CIMClass getClass(String version,
			     CIMNameSpace currNs,
			     CIMObjectPath path,
			     Boolean localOnly,
			     Boolean includeQualifiers,
			     Boolean includeClassOrigin,
			     String propertyList[],
			     ServerSecurity ss) throws CIMException;

    public CIMInstance getInstance(String version,
				   CIMNameSpace currNs,
				   CIMObjectPath path,
				   Boolean localOnly,
				   Boolean includeQualifiers,
				   Boolean includeClassOrigin,
				   String propertyList[],
				   ServerSecurity ss) throws CIMException;
   
    public void close(String version, ServerSecurity ss) throws CIMException;


    public void deleteNameSpace(String version, CIMNameSpace parent, 
    					     CIMNameSpace nameSpace, 
					     ServerSecurity ss) throws CIMException;
    public void createNameSpace(String version, 
				CIMNameSpace parent, 
    				CIMNameSpace nameSpace, 
				ServerSecurity ss) throws CIMException ;

    public void createQualifierType(String version, 
				    CIMNameSpace nameSpace, 
				    CIMObjectPath objectName, 
				    CIMQualifierType qt,
				    ServerSecurity ss) throws CIMException;

    public void setQualifierType(String version, 
				 CIMNameSpace nameSpace, 
				 CIMObjectPath objectName, 
				 CIMQualifierType qt,
				 ServerSecurity ss) throws CIMException;


    public void createClass(String version, 
			    CIMNameSpace nameSpace, 
			    CIMObjectPath objectName, 
			    CIMClass cc,
			    ServerSecurity ss) throws CIMException;


    public void setClass(String version, 
			 CIMNameSpace nameSpace, 
			 CIMObjectPath objectName, 
			 CIMClass cc,
			 ServerSecurity ss) throws  CIMException;

    public CIMObjectPath createInstance(String version, 
					CIMNameSpace nameSpace,
					CIMObjectPath objectName,
					CIMInstance ci,
					ServerSecurity ss) throws CIMException;
    public void setInstance(String version,
			    CIMNameSpace nameSpace,
			    CIMObjectPath objectName,
			    CIMInstance ci,
			    ServerSecurity ss) throws CIMException;

    public void setInstance(String version,
			    CIMNameSpace nameSpace,
			    CIMObjectPath objectName,
			    CIMInstance ci,
			    boolean includeQualifier,
			    String[] propertyList,
			    ServerSecurity ss) throws CIMException;

    public void setProperty(String version, 
 			    CIMNameSpace nameSpace,
			    CIMObjectPath objectName,
			    String propertyName,
			    CIMValue newValue,
			    ServerSecurity ss) throws CIMException;

    public Vector execQuery(String version, 
			    CIMNameSpace nameSpace,
			    CIMObjectPath relNS,
			    String query,
			    String ql,
			    ServerSecurity ss) throws CIMException;

    public Vector associators(String version, 
			      CIMNameSpace nameSpace,
			      CIMObjectPath objectName,
			      String assocClass,
			      String resultClass,
			      String role,
			      String resultRole,
			      Boolean includeQualifiers,
			      Boolean includeClassOrigin,
			      String propertyList[],
			      ServerSecurity ss) throws CIMException;


    public Vector associatorNames(String version, 
				CIMNameSpace nameSpace,
				CIMObjectPath objectName,
				String assocClass,
				String resultClass,
				String role,
				String resultRole,
				ServerSecurity ss) throws CIMException;

    public Vector references(String version, 
				CIMNameSpace nameSpace,
				CIMObjectPath objectName,
				String resultClass,
				String role,
				Boolean includeQualifiers,
				Boolean includeClassOrigin,
				String propertyList[],
				ServerSecurity ss) throws CIMException;

    public Vector referenceNames(String version, 
				CIMNameSpace nameSpace,
				CIMObjectPath objectName,
				String resultClass,
				String role,
				ServerSecurity ss) throws CIMException;
    public Vector invokeMethod(String version, 
			       CIMNameSpace nameSpace, 
    			       CIMObjectPath objectName, 
			       String methodName, 
			       Vector inParams,
			       ServerSecurity ss) throws CIMException;

    public Vector invokeMethod(String version, 
			       CIMNameSpace nameSpace, 
    			       CIMObjectPath objectName, 
			       String methodName, 
			       CIMArgument[] inArgs,
			       ServerSecurity ss) throws CIMException;

    public CIMInstance getInstance(String version, 
				   CIMNameSpace nameSpace, 
    				   CIMObjectPath objectName, 
				   boolean localOnly, 
				   ServerSecurity ss) throws CIMException;

    public CIMValue getProperty(String version, 
				CIMNameSpace nameSpace, 
				CIMObjectPath objectName, 
				String propertyName,
				ServerSecurity ss) throws CIMException;

    public CIMQualifierType getQualifierType(String version, 
					     CIMNameSpace nameSpace, 
					     CIMObjectPath objectName,
					     ServerSecurity ss) throws CIMException;

    public void deleteClass(String version, CIMNameSpace ns, 
			    CIMObjectPath path,
			    ServerSecurity ss) throws CIMException;

    public void deleteInstance(String version, CIMNameSpace ns, 
		    CIMObjectPath path, ServerSecurity ss) throws CIMException;

    public void deleteQualifierType(String version, CIMNameSpace ns, 
		     CIMObjectPath path, ServerSecurity ss) throws CIMException;

    public Vector enumNameSpace(String version, CIMNameSpace ns, 
				CIMObjectPath path, boolean deep,
				ServerSecurity ss) throws CIMException;

    public Vector enumInstances(String version, CIMNameSpace ns, 
				CIMObjectPath path, 
				boolean deep,
				ServerSecurity ss) throws CIMException;

    public Vector enumInstances(String version, CIMNameSpace ns, 
				CIMObjectPath path, 
				boolean deep,
				boolean localOnly,
				ServerSecurity ss) throws CIMException;

    public Vector enumQualifierTypes(String version, CIMNameSpace ns, 
				     CIMObjectPath path, 
				     ServerSecurity ss) throws CIMException;

    public void verifyCapabilities(ServerSecurity ss, String operation,
	String namespace) throws CIMException;
}
