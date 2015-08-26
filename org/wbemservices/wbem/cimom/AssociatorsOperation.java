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

import javax.wbem.client.CIMAssociatorsOp;
import javax.wbem.cim.CIMObjectPath;




public class AssociatorsOperation extends CIMOMOperation {

    private boolean includeQualifiers = false;
    private boolean includeClassOrigin = false;
    private String[] propertyList = null;

    protected CIMObjectPath op = null;
    protected String assocClass = null;
    protected String resultClass = null;
    protected String role = null;
    protected String resultRole = null;

    AssociatorsOperation() {
    }

    public AssociatorsOperation(CIMOMServer cimom, ServerSecurity ss,
			CIMAssociatorsOp cimop,
			String version) {

	super(cimom, ss, cimop.getNameSpace(), version);

	this.op = cimop.getModelPath();
	this.assocClass = cimop.getAssociationClass();
	this.resultClass = cimop.getResultClass();
	this.role = cimop.getRole();
	this.resultRole = cimop.getResultRole();
	this.includeQualifiers = cimop.isQualifiersIncluded();
	this.includeClassOrigin = cimop.isClassOriginIncluded();
	this.propertyList = cimop.getPropertyList();
    }

    public synchronized void run() {
	try {
	    LogFile.methodEntry("associatorsOperation");
	    verifyCapabilities(READ);

	    
	//  result = cimom.intassociators(ns, op, assocClass, resultClass,
	//		role, resultRole, includeQualifiers,
	//		includeClassOrigin, propertyList, false);

	    result = cimom.associators(version, ns, op, assocClass,
	    			resultClass, role, resultRole,
				(includeQualifiers ? Boolean.TRUE : Boolean.FALSE), 
				(includeClassOrigin ? Boolean.TRUE : Boolean.FALSE),
				propertyList, ss);
	    LogFile.methodReturn("associatorsOperation");
	} catch (Exception ex) {
	    result = ex;
	}
    }
}
