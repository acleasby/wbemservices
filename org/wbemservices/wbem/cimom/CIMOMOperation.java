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
import javax.wbem.cim.CIMException;




public abstract class CIMOMOperation implements Runnable {
    
    protected final static String READ = "read";
    protected final static String WRITE = "write";

    protected CIMNameSpace ns = null;
    protected CIMOMServer cimom = null;
    protected Object result = null;
    protected ServerSecurity ss = null;
    protected String version = "";

    CIMOMOperation() {
    }

    public CIMOMOperation(CIMOMServer cimom, CIMNameSpace ns) {
	this.cimom = cimom;
	this.ns = ns;
    }

    public CIMOMOperation(CIMOMServer cimom, ServerSecurity ss,
			CIMNameSpace ns, String version) {
	this(cimom, ns);
	this.ss = ss;
	this.version = version;
    }

    public Object getResult() {
	return result;
    }

    public void verifyCapabilities(String rw) throws CIMException{
	cimom.verifyCapabilities(ss, rw, ns.getNameSpace());
    }

    public abstract void run();
}
