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


import java.lang.reflect.Constructor;
import java.util.HashMap;


import javax.wbem.cim.CIMException;
import javax.wbem.client.CIMOperation;

public class BatchOperationFactory {

    // private final static String PKG = "org.wbemservices.wbem.cimom.";

    private final static String PKG = "javax.wbem.client.";

    private final static int PKG_NAME_LEN = PKG.length();

    private static HashMap operationHash = new HashMap();

    static {
    	operationHash.put("CIMAssociatorNamesOp",
				"AssociatorNames");
    	operationHash.put("CIMAssociatorsOp",
				"Associators");
    	operationHash.put("CIMCreateClassOp",
				"CreateClass");
    	operationHash.put("CIMCreateInstanceOp",
				"CreateInstance");
    	operationHash.put("CIMCreateNameSpaceOp",
				"CreateNameSpace");
    	operationHash.put("CIMCreateQualifierTypeOp",
				"CreateQualifierType");
    	operationHash.put("CIMDeleteClassOp",
				"DeleteClass");
    	operationHash.put("CIMDeleteInstanceOp",
				"DeleteInstance");
    	operationHash.put("CIMDeleteNameSpaceOp",
				"DeleteNameSpace");
    	operationHash.put("CIMDeleteQualifierTypeOp",
				"DeleteQualifierType");
    	operationHash.put("CIMEnumClassNamesOp",
				"EnumClassNames");
    	operationHash.put("CIMEnumClassOp",
				"EnumClass");
    	operationHash.put("CIMEnumInstanceNamesOp",
				"EnumInstanceNames");
    	operationHash.put("CIMEnumInstancesOp",
				"EnumInstances");
    	operationHash.put("CIMEnumNameSpaceOp",
				"EnumNameSpace");
    	operationHash.put("CIMEnumQualifierTypesOp",
				"EnumQualifierTypes");
    	operationHash.put("CIMExecQueryOp",
				"ExecQuery");
    	operationHash.put("CIMGetClassOp",
				"GetClass");
    	operationHash.put("CIMGetInstanceOp",
				"GetInstance");
    	operationHash.put("CIMGetPropertyOp",
				"GetProperty");
    	operationHash.put("CIMGetQualifierTypeOp",
				"GetQualifierType");
    	operationHash.put("CIMInvokeMethodOp",
				"InvokeMethod");
    	operationHash.put("CIMInvokeArgsMethodOp",
				"InvokeArgsMethod");
    	operationHash.put("CIMReferenceNamesOp",
				"ReferenceNames");
    	operationHash.put("CIMReferencesOp",
				"References");
    	operationHash.put("CIMSetClassOp",
				"SetClass");
    	operationHash.put("CIMSetInstanceOp",
				"SetInstance");
    	operationHash.put("CIMSetPropertyOp",
				"SetProperty");
    	operationHash.put("CIMSetQualifierTypeOp",
				"SetQualifierType");
    }

    public static CIMOMOperation getCIMOMOperation(CIMOMServer cimom, 
    						    ServerSecurity ss,
    						    CIMOperation cimOp,
						    String version) 
    		throws CIMException {
	
	Class cimopClass = cimOp.getClass();

	String cimOpName = cimopClass.getName();

	String abbrev_name = cimOpName.substring(PKG_NAME_LEN);

	String cimomOpName = (String)operationHash.get(abbrev_name);

	// Figure out the right exception class...
	if (cimomOpName == null) {
	    throw new CIMException(CIMException.CIM_ERR_FAILED,
	    			"No such operation!!");
	}

	Class cl = null;
	try {
	    cl = Class.forName("org.wbemservices.wbem.cimom." + 
	    			cimomOpName + 
				"Operation");
	} catch (Exception ex) {
	    throw new CIMException(CIMException.CIM_ERR_FAILED,
	    			"No such operation");
	}

	CIMOMOperation cimomOp = null;
	try {
	    Class [] paramTypes = { org.wbemservices.wbem.cimom.CIMOMServer.class,
	    			    org.wbemservices.wbem.cimom.ServerSecurity.class,
				    cimopClass,
				    java.lang.String.class};
	    
	    Object [] params = { cimom, ss, cimOp, version };

	    Constructor cons = cl.getDeclaredConstructor(paramTypes);
	    cimomOp = (CIMOMOperation)cons.newInstance(params);
	} catch (Exception ex) {
	    throw new CIMException(CIMException.CIM_ERR_FAILED, ex);
	}

	return cimomOp;
    }
}

