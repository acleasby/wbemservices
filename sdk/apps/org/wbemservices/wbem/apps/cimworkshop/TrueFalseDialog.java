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
 
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import java.awt.Frame;
import java.awt.Insets;
import org.wbemservices.wbem.apps.common.ActionString;
import org.wbemservices.wbem.apps.common.ColumnLayout;
import org.wbemservices.wbem.apps.common.Util;

/**
 * 
 *
 * @author 	Sun Microsystems
 */

class TrueFalseDialog extends CIMEditDialog {

    JRadioButton trueButton;
    JRadioButton falseButton;
    JRadioButton noValueButton;

    public TrueFalseDialog(Frame parent, Boolean state) {
	this(parent, "", "", state, true);
    }

    public TrueFalseDialog(Frame parent, Boolean state, boolean enabled) {
	this(parent, "", "", state, enabled);
    }

    public TrueFalseDialog(Frame parent, String name, String type,
			   Boolean state, boolean enabled) {

	super(parent, name, type, enabled);	

	ButtonGroup group = new ButtonGroup();
	ActionString asTrue = new ActionString("LBL_TRUE", 
	    "org.wbemservices.wbem.apps.common.common");
	ActionString asFalse = new ActionString("LBL_FALSE", 
	    "org.wbemservices.wbem.apps.common.common");
	ActionString asNoValue = new ActionString("LBL_NO_VALUE", 
	    "org.wbemservices.wbem.apps.common.common");

	trueButton = new JRadioButton(asTrue.getString());
	trueButton.setMnemonic(asTrue.getMnemonic());
	trueButton.setActionCommand("TRUE");
	trueButton.addActionListener(this);

	falseButton = new JRadioButton(asFalse.getString());
	falseButton.setMnemonic(asFalse.getMnemonic());
	falseButton.setActionCommand("FALSE");
	falseButton.addActionListener(this);

	noValueButton = new JRadioButton(asNoValue.getString());
	noValueButton.setMnemonic(asNoValue.getMnemonic());
	noValueButton.setActionCommand("NO_VALUE");
	noValueButton.addActionListener(this);
	group.add(trueButton);
	group.add(falseButton);
	group.add(noValueButton);
	

	JPanel radioButtonPanel = new JPanel(new ColumnLayout()) {
	    public Insets getInsets() {
		return new Insets(5, 5, 5, 5);
	    }
	};
	radioButtonPanel.add(trueButton);
	radioButtonPanel.add(falseButton);
	radioButtonPanel.add(noValueButton);


	if (state == null) {
	    noValueButton.setSelected(true);
	} else if (state.booleanValue()) {
	    trueButton.setSelected(true);
	} else {
	    falseButton.setSelected(true);
	}
	String defaultHelp = "ShowValue_000.htm";	
	if (enabled) {
	    defaultHelp = "Boolean_000.htm";
	} else {
	    trueButton.setEnabled(false);
	    falseButton.setEnabled(false);
// BUGFIX. Accessibility fixes
	    noValueButton.setEnabled(false);
	}
	setDefaultHelp(defaultHelp);
	JPanel mainPanel = this.getMainPanel();
	mainPanel.add(radioButtonPanel);

	pack();
	setLocation(Util.getCenterPoint(parent, this));
	setVisible(true);    
    }

    public void okClicked() {

	if (nameField != null) {
	    nameString = nameField.getText().trim();
	}

	if (noValueButton.isSelected()) {
	    returnString = "";
	} else {
	    ActionString aString = null;
	    if (trueButton.isSelected()) {
		aString  = new ActionString("LBL_TRUE",
		    "org.wbemservices.wbem.apps.common.common");
	    } else {
		aString  = new ActionString("LBL_FALSE",
	            "org.wbemservices.wbem.apps.common.common");
	    }
	    returnString = aString.getString();
	}

	if (setReturnObject()) {
	    dispose();
	}
    }

}
	


