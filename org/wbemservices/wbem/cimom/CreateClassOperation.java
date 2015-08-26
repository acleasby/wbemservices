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
import javax.wbem.cim.CIMClass;
import javax.wbem.client.CIMCreateClassOp;




public class CreateClassOperation extends CIMOMOperation {

    private CIMObjectPath op = null;
    private CIMClass cc = null;

    CreateClassOperation() {
    }

    public CreateClassOperation(CIMOMServer cimom, ServerSecurity ss,
			CIMCreateClassOp cimop, String version) {
	super(cimom, ss, cimop.getNameSpace(), version);
    	this.op = cimop.getModelPath();
	this.cc = cimop.getCIMClass();
    }

    public synchronized void run() {
	try {
	    LogFile.methodEntry("createClassOperation");
	    verifyCapabilities(WRITE);
	    cimom.createClass(version, ns, op, cc, ss);
	    LogFile.methodReturn("createClassOperation");
	} catch (Exception ex) {
	    result = ex;
	}
    }
}
