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

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.awt.Frame;

import org.wbemservices.wbem.apps.common.ActionString;
import org.wbemservices.wbem.apps.common.ColumnLayout;
import org.wbemservices.wbem.apps.common.ContextHelpListener;
import org.wbemservices.wbem.apps.common.TextFieldFocusListener;
import org.wbemservices.wbem.apps.common.Util;

import javax.wbem.cim.CIMDataType;

/**
 * 
 *
 * @version 	1.19, 08/16/01
 * @author 	Sun Microsystems
 */

class RealNumberFieldDialog extends CIMEditDialog {

    public RealNumberFieldDialog(Frame parent, String name, String type, 
				 String value, boolean signed) {
	this(parent, name, type, value, signed, true);
    }

    public RealNumberFieldDialog(Frame parent, String name, String type, 
				 String value, boolean signed, 
				 boolean enabled) {
	super(parent, name, type, enabled);	
	
	String tmpValue = value;
	if (tmpValue == null) {
	    tmpValue = "";
	}
	JPanel fieldPanel = new JPanel(new ColumnLayout());
	valueField = new RealNumberField(tmpValue, 20);
	String defaultHelp = "ShowValue_000.htm";
	ActionString asValue = new ActionString("LBL_VALUE");
	JLabel valueLabel = new JLabel(asValue.getString() + ":");

	if (isEditable) {
	    valueLabel.setDisplayedMnemonic(asValue.getMnemonic());
	    valueLabel.setLabelFor(valueField);
	    valueField.getDocument().addDocumentListener(this);
	    valueField.addFocusListener(new TextFieldFocusListener());
	    String tfHelp = "Real_010.htm";
	    if (cimType == CIMDataType.REAL64) {
		tfHelp = "Real_020.htm";
	    }
	    valueField.addFocusListener(new ContextHelpListener(getInfoPanel(), 
					"cimworkshop", tfHelp));
	    defaultHelp = "Real_000.htm";
	} else {
	    valueField.setEnabled(false);
	}
	setDefaultHelp(defaultHelp);
	fieldPanel.add(valueLabel);
	fieldPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
	fieldPanel.add(valueField);
	JPanel mainPanel = this.getMainPanel();
	mainPanel.add(fieldPanel);

	setOKEnabled();
	pack();
	setLocation(Util.getCenterPoint(parent, this));
	setVisible(true);
    }

}
