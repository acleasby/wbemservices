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

package org.wbemservices.wbem.apps.cimworkshop;

import java.net.URLClassLoader;

import org.wbemservices.wbem.apps.common.CIMClientObject;
import org.wbemservices.wbem.apps.common.DefaultProperties;
import org.wbemservices.wbem.apps.common.I18N;
import org.wbemservices.wbem.apps.common.Util;

import javax.wbem.client.CIMClient;

/**
 * 
 *
 * @version 	%I%, %G%
 * @author 	Sun Microsystems
 */

public class CIMWorkshop {

    public CIMWorkshop(String protocol) {
	String installDir = System.getProperty("install.dir");
	DefaultProperties.setInstallLoc(installDir, "cimworkshop");
	I18N.setResourceName("org.wbemservices.wbem.apps.common.CWS");
	URLClassLoader urlCL = (URLClassLoader)this.getClass().getClassLoader();
	Util.setClassLoader(urlCL);

	ObjectTreeFrame otf = new ObjectTreeFrame();

	if (!otf.login()) {
	    System.exit(0);
	}
    }

    public static void main(String[] argv) {
	String protocol = "CIM-XML";
	if (argv.length > 0) {
	    protocol = argv[0];
	} 
	CIMWorkshop cimWorkshop = new CIMWorkshop(protocol);
    }

}
