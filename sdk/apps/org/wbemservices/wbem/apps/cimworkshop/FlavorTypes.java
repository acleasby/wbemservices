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

package org.wbemservices.wbem.apps.cimworkshop;

import java.util.Vector;

/**
 * 
 *
 * @version 	1.5, 08/16/01
 * @author 	Sun Microsystems
 */

public class FlavorTypes {

    private Vector types;

    public FlavorTypes() {
	types = new Vector();
	types.addElement("ENABLEOVERRIDE");
	types.addElement("DISABLEOVERRIDE");
	types.addElement("RESTRICTED");
	types.addElement("TOSUBCLASS");
	types.addElement("TRANSLATABLE");
    }

    public Vector getTypes() {
	return types;
    }

    public int getFlavorType(String type) {
	if (type == null) {
	    return -1;
	}
	return types.indexOf(type);
    }

    public String getFlavorType(int type) {
	if (type >= types.size()) {
	    return null;
	}
	return (String)types.elementAt(type);
    }
}


