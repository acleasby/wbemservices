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

import javax.wbem.cim.CIMObjectPath;
import javax.wbem.client.CIMEnumClassOp;




public class EnumClassOperation extends CIMOMOperation {

    private CIMObjectPath op = null;
    private boolean deep;
    private boolean localOnly;
    private boolean includeQualifiers;
    private boolean includeClassOrigin;

    EnumClassOperation() {
    }

    public EnumClassOperation(CIMOMServer cimom, ServerSecurity ss,
			CIMEnumClassOp cimop, String version) {

	super(cimom, ss, cimop.getNameSpace(), version);
    	this.op = cimop.getModelPath();
	this.deep = cimop.isDeep();
	this.localOnly = cimop.isLocalOnly();
	this.includeQualifiers = cimop.isQualifiersIncluded();
	this.includeClassOrigin = cimop.isClassOriginIncluded();
    }

    public synchronized void run() {
	try {
	    LogFile.methodEntry("enumClassOperation");
	    verifyCapabilities(READ);
	    // result = (Object)cimom.intenumClass(ns, op, deep, localOnly);

	    result = cimom.enumerateClasses(version, ns, op, 
	    			(deep ? Boolean.TRUE : Boolean.FALSE),
	  			(localOnly ? Boolean.TRUE : Boolean.FALSE), 
				(includeQualifiers ? Boolean.TRUE : Boolean.FALSE), 
				(includeClassOrigin ? Boolean.TRUE : Boolean.FALSE), ss);
	   
	    LogFile.methodReturn("enumClassOperation");
	} catch (Exception ex) {
	    result = ex;
	}
    }
}
