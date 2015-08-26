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

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.BorderFactory;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import java.awt.Frame;
import org.wbemservices.wbem.apps.common.ActionString;
import org.wbemservices.wbem.apps.common.ColumnLayout;
import org.wbemservices.wbem.apps.common.I18N;
import org.wbemservices.wbem.apps.common.TextFieldFocusListener;
import org.wbemservices.wbem.apps.common.ContextHelpListener;
import org.wbemservices.wbem.apps.common.Util;


/**
 * 
 *
 * @version 	1.20, 08/16/01
 * @author 	Sun Microsystems
 */

class TextFieldDialog extends CIMEditDialog {

    public TextFieldDialog(Frame parent, String name, String type, 
			   String value) {
	this(parent, name, type, value, true);
    }

    public TextFieldDialog(Frame parent, String name, String type, 
			   String value, boolean editable) {

	super(parent, name, type, editable);	

	String tmpValue = value;
	JPanel fieldPanel = new JPanel(new ColumnLayout());

	ActionString asValue = new ActionString("LBL_VALUE");
	JLabel lValue = new JLabel(asValue.getString() + ":");
	lValue.setDisplayedMnemonic(asValue.getMnemonic());
	fieldPanel.add(lValue);
	fieldPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
	String defaultHelp = "ShowValue_000.htm";
	if (editable) {
	    if (tmpValue == null) {
		tmpValue = "";
	    }
	    valueField = new JTextField(tmpValue, 20);
	    valueField.getDocument().addDocumentListener(this);
	    valueField.addFocusListener(new TextFieldFocusListener());
	    valueField.addFocusListener(new ContextHelpListener(getInfoPanel(),
					"cimworkshop", "CharString_000.htm"));
	    fieldPanel.add(valueField);
	    lValue.setLabelFor(valueField);
	    defaultHelp = "CharString_000.htm";
	} else {
	    if (tmpValue == null) {
		tmpValue = I18N.loadString("LBL_EMPTY");
	    }
	    JTextArea tArea = new JTextArea(tmpValue, 5, 20);
	    tArea.setLineWrap(true);
	    tArea.setWrapStyleWord(true);
	    tArea.setEnabled(false);
	    fieldPanel.add(new JScrollPane(tArea));
	    lValue.setLabelFor(tArea);
	}

	setDefaultHelp(defaultHelp);

	JPanel mainPanel = this.getMainPanel();
	mainPanel.add(fieldPanel);

	setOKEnabled();
	pack();
	setLocation(Util.getCenterPoint(parent, this));
	setVisible(true);
    }

}
