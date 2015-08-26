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

import org.wbemservices.wbem.apps.common.ActionString;
import org.wbemservices.wbem.apps.common.ColumnLayout;
import org.wbemservices.wbem.apps.common.ContextHelpListener;
import org.wbemservices.wbem.apps.common.SizedIntegerDocument;
import org.wbemservices.wbem.apps.common.TextFieldFocusListener;
import org.wbemservices.wbem.apps.common.Util;

import javax.wbem.cim.CIMDataType;

import java.awt.Frame;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * 
 *
 * @version 	1.21, 08/16/01
 * @author 	Sun Microsystems
 */

class IntegerFieldDialog extends CIMEditDialog {


    public IntegerFieldDialog(Frame parent, String name, String type, 
			      String value) {
	this(parent, name, type, value, true);
    }

    public IntegerFieldDialog(Frame parent, String name, String type, 
			      String value, boolean enabled) {

	super(parent, name, type, enabled);	

	String tmpValue = value;
	if (tmpValue == null) {
	    tmpValue = "";
	}

	JPanel fieldPanel = new JPanel(new ColumnLayout());
	valueField = new JTextField(20);
	
	String defaultHelp = "ShowValue_000.htm";

	ActionString asValue = new ActionString("LBL_VALUE");
	JLabel valueLabel = new JLabel(asValue.getString() + ":");


	if (enabled) {
	    long min = 0;
	    long max = 255;

	    String tfHelp;
	    switch (cimType) {
	    case CIMDataType.SINT8:
		tfHelp = "Int_010.htm";
		min = -128;
		max = 127;
		    break;
	    case CIMDataType.SINT16:
		tfHelp = "Int_020.htm";
		min = -32768;
		max = 32767;
		break;
	    case CIMDataType.SINT32:
		tfHelp = "Int_030.htm";
		min = Integer.MIN_VALUE;
		max = Integer.MAX_VALUE;
		break;
	    case CIMDataType.SINT64:
		tfHelp = "Int_040.htm";
		min = Long.MIN_VALUE;
		max = Long.MAX_VALUE;
		break;
	    case CIMDataType.UINT8:
		tfHelp = "Int_050.htm";
		break;
	    case CIMDataType.UINT16:
		tfHelp = "Int_060.htm";
		max = 65535;
		break;
	    case CIMDataType.UINT32:
		tfHelp = "Int_070.htm";
		max = 4294967295L;
		break;
	    case CIMDataType.UINT64:
		tfHelp = "Int_080.htm";
		valueField = new IntegerField(tmpValue, false);
		break;
	    default:
		tfHelp = "Int_000.htm";
	    }

	    valueLabel.setDisplayedMnemonic(asValue.getMnemonic());
	    valueLabel.setLabelFor(valueField);
	    valueField.getDocument().addDocumentListener(this);
	    valueField.addFocusListener(new TextFieldFocusListener());
	    valueField.addFocusListener(new ContextHelpListener(getInfoPanel(),
					"cimworkshop", tfHelp));
	    defaultHelp = "Int_000.htm";
	    if (cimType != CIMDataType.UINT64) {
		SizedIntegerDocument valueDoc = new SizedIntegerDocument(
		    valueField, min, max);
		valueField.setDocument(valueDoc);
	    }
	} else {
	    valueField.setEnabled(false);
	}
	valueField.setText(tmpValue);
	setDefaultHelp(defaultHelp);
	fieldPanel.add(valueLabel);
	fieldPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
	fieldPanel.add(valueField);
	JPanel mainPanel = this.getMainPanel();
	mainPanel.add(fieldPanel);

	pack();
	setLocation(Util.getCenterPoint(parent, this));
	setVisible(true);
    }


}
