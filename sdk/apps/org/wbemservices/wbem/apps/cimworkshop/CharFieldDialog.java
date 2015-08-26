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
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.Frame;
import org.wbemservices.wbem.apps.common.ActionString;
import org.wbemservices.wbem.apps.common.ColumnLayout;
import org.wbemservices.wbem.apps.common.ContextHelpListener;
import org.wbemservices.wbem.apps.common.TextFieldFocusListener;
import org.wbemservices.wbem.apps.common.Util;

/**
 * 
 *
 * @version 	1.10, 08/16/01
 * @author 	Sun Microsystems
 */

class CharFieldDialog extends CIMEditDialog {

    public CharFieldDialog(Frame parent, String name, String type, 
			   String value) {
	this(parent, name, type, value, true);
    }

    public CharFieldDialog(Frame parent, String name, String type, 
			   String value, boolean enabled) {

	super(parent, name, type, enabled);	
	
	String tmpValue = value;

	if (tmpValue == null) {
	    tmpValue = "";
	}
	JPanel fieldPanel = new JPanel(new ColumnLayout());
	valueField = new JTextField(tmpValue, 20);
	String defaultHelp = "ShowValue_000.htm";	

	ActionString asValue = new ActionString("LBL_VALUE");
	JLabel valueLabel = new JLabel(asValue.getString() + ":");

	if (isEditable) {
	    valueLabel.setDisplayedMnemonic(asValue.getMnemonic());
	    valueLabel.setLabelFor(valueField);
	    valueField.setDocument(new CharDocument());	
	    valueField.getDocument().addDocumentListener(this);
	    valueField.addFocusListener(new TextFieldFocusListener());
	    valueField.addFocusListener(new ContextHelpListener(getInfoPanel(),
				        "cimworkshop", "CharString_010.htm"));
	    defaultHelp = "CharString_000.htm";
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

    public class CharDocument extends PlainDocument {
	public void insertString(int offset, String s, AttributeSet aSet) throws
	                         BadLocationException {
	    if (offset == 0) {
		super.insertString(offset, s, aSet);
	    }
	}
    }

}
