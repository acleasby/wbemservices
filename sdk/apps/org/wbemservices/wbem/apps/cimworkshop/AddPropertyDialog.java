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

import org.wbemservices.wbem.apps.common.ContextHelpListener;
import org.wbemservices.wbem.apps.common.I18N;
import org.wbemservices.wbem.apps.common.Util;

import javax.wbem.cim.CIMDataType;

import java.awt.Frame;

/**
 * 
 *
 * @version 	1.13, 08/16/01
 * @author 	Sun Microsystems
 */

public class AddPropertyDialog extends AddElementDialog {
    
    public AddPropertyDialog(Frame frame) {
	super(frame);
	setTitle(I18N.loadString("TTL_ADD_PROPERTY"));
	// setup URLS for help
	this.setDefaultFocusListener(new ContextHelpListener(
		infoPanel, "cimworkshop", "AddProperties_000.htm"), true);

	nameField.addFocusListener(new ContextHelpListener(
		infoPanel, "cimworkshop", "AddProperties_010.htm"));		
	list.addFocusListener(new ContextHelpListener(
		infoPanel, "cimworkshop", "AddProperties_020.htm"));		
	setVisible(true);
    }

    public void okClicked() {
	nameString = nameField.getText().trim();
	if (list.getSelectedIndex() == CIMDataType.REFERENCE) {
	    ClassSelectionDialog csd = new ClassSelectionDialog(
		Util.getFrame(this));
	    String className = csd.getClassName(); 
	    if (className.length() < 1) {
		return;
	    } else {
		// if reference, return classname as type
		typeString = className;
		dispose();
	    }
	} else {
	    typeString = (String)list.getSelectedValue();
	    dispose();
	}
    }

}
