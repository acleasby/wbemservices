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
import javax.wbem.cim.CIMInstance;
import javax.wbem.client.CIMCreateInstanceOp;




class CreateInstanceOperation extends CIMOMOperation {

    private CIMObjectPath op = null;
    private CIMInstance ci = null;

    CreateInstanceOperation() {
    }

    CreateInstanceOperation(CIMOMServer cimom, ServerSecurity ss,
    				CIMCreateInstanceOp cimop,
				String version) {

	super(cimom, ss, cimop.getNameSpace(), version);
    	this.op = cimop.getModelPath();
	this.ci = cimop.getCIMInstance();
    }

    public synchronized void run() {
	try {
	    LogFile.methodEntry("createInstanceOperation");
	    result = cimom.createInstance(version, ns, op, ci, ss);
	    LogFile.methodReturn("createInstanceOperation");
	} catch (Exception ex) {
	    result = ex;
	}
    }
}
