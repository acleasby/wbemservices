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
 *are Copyright © 2003 Sun Microsystems, Inc.
 *
 *All Rights Reserved.
 *
 *Contributor(s): _______________________________________
*/

package org.wbemservices.wbem.cimom;

import javax.wbem.cim.CIMObjectPath;
import javax.wbem.client.CIMInvokeArgsMethodOp;
import javax.wbem.cim.CIMArgument;

class InvokeArgsMethodOperation extends CIMOMOperation {

    private CIMObjectPath op = null;
    private String methodName = null;
    private CIMArgument[] inArgs = null;
    private CIMArgument[] outArgs = null;

    InvokeArgsMethodOperation() {
    }

    InvokeArgsMethodOperation(CIMOMServer cimom, ServerSecurity ss,
    				CIMInvokeArgsMethodOp cimop,
				String version) {

	super(cimom, ss, cimop.getNameSpace(), version);
    	this.op = cimop.getModelPath();
	this.methodName = cimop.getMethodName();
	this.inArgs = cimop.getInArgs();
	this.outArgs = cimop.getOutArgs();
    }

    public synchronized void run() {
	try {
	    LogFile.methodEntry("invokeMethodOperation");
   	//  result = cimom.intinvokeMethod(ns, op, methodName, inParams, false);
	    
	    result = cimom.invokeMethod(version, ns, op, methodName,
	    			inArgs, ss);

	    LogFile.methodReturn("invokeMethodOperation");
	} catch (Exception ex) {
	    result = ex;
	}
    }
}
