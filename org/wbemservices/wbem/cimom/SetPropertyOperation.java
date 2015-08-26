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
 *are Copyright � 2001 Sun Microsystems, Inc.
 *
 *All Rights Reserved.
 *
 *Contributor(s): _______________________________________
*/

package org.wbemservices.wbem.cimom;

import javax.wbem.cim.CIMObjectPath;
import javax.wbem.cim.CIMValue;
import javax.wbem.client.CIMSetPropertyOp;




class SetPropertyOperation extends CIMOMOperation {

    private CIMObjectPath op = null;
    private String propname = null;
    private CIMValue cv = null;

    SetPropertyOperation() {
    }

    SetPropertyOperation(CIMOMServer cimom, ServerSecurity ss,
			CIMSetPropertyOp cimop, String version) {

	super(cimom, ss, cimop.getNameSpace(), version);
    	this.op = cimop.getModelPath();
	this.propname = cimop.getPropertyName();
	this.cv = cimop.getCIMValue();
    }

    public synchronized void run() {
	try {
	    LogFile.methodEntry("setPropertyOperation");
	    verifyCapabilities(WRITE);
	    // cimom.intsetProperty(ns, op, propname, cv, false);

	    cimom.setProperty(version, ns, op, propname, cv, ss);

	    LogFile.methodReturn("setPropertyOperation");
	} catch (Exception ex) {
	    result = ex;
	}
    }
}