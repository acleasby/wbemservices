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

import javax.wbem.cim.CIMNameSpace;
import javax.wbem.client.CIMDeleteNameSpaceOp;




class DeleteNameSpaceOperation extends CIMOMOperation {

    private CIMNameSpace relNs = null;

    DeleteNameSpaceOperation() {
    }

    DeleteNameSpaceOperation(CIMOMServer cimom, ServerSecurity ss,
			CIMDeleteNameSpaceOp cimop, String version) {

	super(cimom, ss, cimop.getNameSpace(), version);
	this.relNs = cimop.getRelativeNameSpace();
    }

    public synchronized void run() {
	try {
	    LogFile.methodEntry("deleteNameSpaceOperation");
	    verifyCapabilities(WRITE);
	    // CIMNSStaticMethods.deleteNameSpace(ns, relNs);
	    cimom.deleteNameSpace(version, ns, relNs, ss);
	    LogFile.methodReturn("deleteNameSpaceOperation");
	} catch (Exception ex) {
	    result = ex;
	}
    }
}
