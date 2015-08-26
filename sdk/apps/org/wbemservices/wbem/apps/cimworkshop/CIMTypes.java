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

import java.util.Vector;

import javax.wbem.cim.CIMDataType;

import org.wbemservices.wbem.apps.common.Util;

/**
 * 
 *
 * @version 	1.16, 08/16/01
 * @author 	Sun Microsystems
 */

public class CIMTypes {

    private Vector types;

    public CIMTypes() {
	types = new Vector();
	CIMDataType cdt = null;
	for (int i = CIMDataType.UINT8; i <= CIMDataType.CHAR16_ARRAY; i++) {
	    try {
		cdt = new CIMDataType(i);
		types.addElement(Util.getDataTypeString(cdt));
	    } catch (IllegalArgumentException exc) {
	    }
	}

	// add reference data type
	cdt = new CIMDataType("");
	types.addElement(cdt.toString().trim());
    }

    public Vector getTypes() {
	return types;
    }

    public int getCIMType(String type) {
	if (type == null) {
	    return -1;
	} else if (type.indexOf('_') > 0) {
	    return CIMDataType.REFERENCE;
	}
	return types.indexOf(type);
    }

    public String getCIMType(int type) {
	if (type >= types.size()) {
	    return null;
	}
	return (String)types.elementAt(type);
    }
}


