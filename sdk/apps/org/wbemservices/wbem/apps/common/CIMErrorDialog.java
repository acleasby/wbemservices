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

package org.wbemservices.wbem.apps.common;

import javax.swing.JOptionPane;

import javax.wbem.cim.CIMException;

import java.awt.Component;
import java.util.Vector;

/**
 * 
 * @version 	1.12, 08/16/01
 * @author 	Sun Microsystems
 */

public class CIMErrorDialog  {

    public static void display(Component comp, CIMException exc) {
	String msgFormat;
	String msgID = exc.getID();
	if (I18N.isStringAvailable(msgID, 
				   "org.wbemservices.wbem.apps.common.CIMError")) {
	    if (exc.getParams() != null) {
		msgFormat = I18N.loadStringFormat(msgID, exc.getParams(),
					"org.wbemservices.wbem.apps.common.CIMError");
	    } else {
		msgFormat = I18N.loadString(msgID,
					"org.wbemservices.wbem.apps.common.CIMError");
	    }
	} else {
	    Vector v = new Vector();
	    v.add(msgID);
	    msgFormat = I18N.loadStringFormat("UNKNOWN_CIM_ERROR", v,
					"org.wbemservices.wbem.apps.common.CIMError");
	}
	JOptionPane.showMessageDialog(comp, Util.wrapText(msgFormat, 40), 
	    I18N.loadString("TTL_ERROR", "org.wbemservices.wbem.apps.common.common"), 
	    JOptionPane.ERROR_MESSAGE);

    }
}


