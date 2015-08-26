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

import org.wbemservices.wbem.apps.common.*;


import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import javax.wbem.cim.*;
import javax.wbem.client.CIMClient;


/**
 * 
 *
 * @version 	1.4, 03/28/01
 * @author 	Sun Microsystems Inc.
 */

/**
 * This class displays a dialog that allows a user to add  a new qualifier 
 * type to a namespace
 *
 */
public class AddQualifierTypeDialog extends AddElementDialog {

    protected CIMQualifierType qualifierType;
    protected JTextField defaultValueField;
    protected CIMTypes cimTypes;
    Vector flavor;
    Vector scope;
    Object defaultValue = null;
    Frame thisFrame;
    
    /**
     * AddQulifierType Constructor
     *
     * @param frame    The frame that this dialog is displayed from
     */
    public AddQualifierTypeDialog(Frame frame) {
	super(frame);
	thisFrame = frame;
	// create new, blank qualifier type
	qualifierType = new CIMQualifierType();
	// create empty vectors to contain scope and flavor
	flavor = new Vector();
	scope = new Vector();

	// get list of CimDataTypes
	cimTypes = new CIMTypes();
	Vector v = cimTypes.getTypes();
	// Qualifier Types cannot be of type ref so remove from list
	v.removeElementAt(CIMDataType.REFERENCE);
	// set cimtypes in list
	list.setListData(v);
	list.setSelectedIndex(0);
	list.setVisibleRowCount(6);
	list.getSelectionModel().addListSelectionListener(
	    new QTListSelectionListener());

	// add textfield for default value
	defaultValueField = new JTextField(20);
	defaultValueField.setEditable(false);
	JPanel defaultValuePanel = new JPanel(new ColumnLayout(
					      LAYOUT_ALIGNMENT.EXPAND, 0, 5));
	defaultValuePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 
								    10, 0));
	
	ActionString asDefaultValue = new ActionString("LBL_DEFAULT_VALUE");
	JLabel lDefaultValue = new JLabel(asDefaultValue.getString() + ":");

	// create buttons for setting scope, flavor and default value
	ActionString asSet = new ActionString("LBL_SET");
	ActionString asScope = new ActionString("MNU_SCOPE");
	ActionString asFlavor = new ActionString("MNU_FLAVOR");
	
	QualifierTypeButtonListener qtbListener = new 
	    QualifierTypeButtonListener();

	JButton btnSet = new JButton(asSet.getString());
	btnSet.setMnemonic(asSet.getMnemonic());
	btnSet.setActionCommand("SET");
	btnSet.addActionListener(qtbListener);

	JButton btnScope = new JButton(asScope.getString());
	btnScope.setMnemonic(asScope.getMnemonic());
	btnScope.setActionCommand("SCOPE");
	btnScope.addActionListener(qtbListener);

	JButton btnFlavor = new JButton(asFlavor.getString());
	btnFlavor.setMnemonic(asFlavor.getMnemonic());
	btnFlavor.setActionCommand("FLAVOR");
	btnFlavor.addActionListener(qtbListener);
	
	JPanel buttonPanel = new JPanel();
	buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 15, 0));
	buttonPanel.add(btnScope);
	buttonPanel.add(btnFlavor);
	
	JPanel textPanel = new JPanel(new BorderLayout());
	
	JPanel editPanel = new JPanel();
	editPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
	editPanel.add(defaultValueField);

	textPanel.add(editPanel, "West");
	textPanel.add(btnSet, "East");

	
	defaultValuePanel.add(lDefaultValue);
	defaultValuePanel.add(textPanel);

	JPanel mainPanel = getMainPanel();
	mainPanel.add(defaultValuePanel);
	mainPanel.add(buttonPanel);

	setTitle(I18N.loadString("TTL_ADD_QUALIFIER_TYPE"));
	// setup URLS for help
	this.setDefaultFocusListener(new ContextHelpListener(
		infoPanel, "cimworkshop", "AddQualifierType_000.htm"), true);

	nameField.addFocusListener(new ContextHelpListener(
		infoPanel, "cimworkshop", "AddQualifierType_010.htm"));
	list.addFocusListener(new ContextHelpListener(
		infoPanel, "cimworkshop", "AddQualifierType_020.htm"));
	btnSet.addFocusListener(new ContextHelpListener(
		infoPanel, "cimworkshop", "AddQualifierType_030.htm"));

	setVisible(true);
    }

    public void okClicked() {
	// get name of qualifier.  If empty display error
	nameString = nameField.getText().trim();
	if (nameString.length() == 0) {
	    JOptionPane.showMessageDialog(null, I18N.loadString( 
		"ERR_QUALIFIER_TYPE_NAME"),
		I18N.loadString("TTL_CIM_ERROR"), 
		JOptionPane.ERROR_MESSAGE);
	    return;
	}
	// get selected type
	typeString = (String)list.getSelectedValue();
	// create CIMType from selected type
	CIMDataType cimDataType = new CIMDataType(cimTypes.getCIMType(
						  typeString));
	// set values for qualifier type
	qualifierType.setType(cimDataType);
	qualifierType.setName(nameString);
	// loop thru selected scope and add to qualifier type
	Enumeration e = scope.elements();
	while (e.hasMoreElements()) {
	    CIMScope scopeVal = (CIMScope)e.nextElement();
	    qualifierType.addScope(scopeVal);
	}	    
	// loop thru selected flavors and add to qualifier type
	e = flavor.elements();
	while (e.hasMoreElements()) {
	    CIMFlavor flavorVal = (CIMFlavor)e.nextElement();
	    qualifierType.addFlavor(flavorVal);
	}
	// if default value, set it
	if (defaultValue != null) {
	    qualifierType.setDefaultValue(new CIMValue(defaultValue));
	}
	try {
	    // get current CIMClient
	    CIMClient cc = CIMClientObject.getClient();
	    // set qualifier type and exit
	    cc.createQualifierType(new CIMObjectPath(), qualifierType);
	    dispose();
	} catch (CIMException exc) {
	    CIMErrorDialog.display(this, exc);
	}
	    
    }

    class QualifierTypeButtonListener implements ActionListener {
	public void actionPerformed(ActionEvent evt) {
	    String actionCmd = evt.getActionCommand();
	    String name = nameField.getText().trim();
	    if (name.length() == 0) {
		name = I18N.loadString("LBL_UNKNOWN", 
				       "org.wbemservices.wbem.apps.common.common");
	    }
	    if (actionCmd.equals("SET")) {
		// if set button click, show CIMValue dialog which
		// lets a user set a CIMValue depending on the CIMDataType
		Object tmpValue = defaultValue;
		typeString = (String)list.getSelectedValue();
		CIMDataType cimDataType = new CIMDataType(cimTypes.getCIMType(
							  typeString));
		defaultValue = CIMValueDialog.showDialog(thisFrame, 
							 tmpValue, name,
							 cimDataType, true);
		// if user didn't hit cancel button on CIMValueDialog, set value
		if (!(defaultValue instanceof CancelObject)) { 
		    if (defaultValue == null) {
			defaultValueField.setText("");
		    } else {
			defaultValueField.setText(defaultValue.toString());
		    }
		}
	    } else if (actionCmd.equals("SCOPE")) {
		// if Scope button clicked, show dialog that allows
		// the user to set scope
		CIMScopeDialog dlg = new CIMScopeDialog(thisFrame,
							scope, name, true);
		Vector retVal = dlg.getSelectedScope();
		if (retVal != null) {
		    scope = retVal;
		}

	    } else if (actionCmd.equals("FLAVOR")) {
		// if Flavor button clicked, show dialog that allows
		// the user to set flavor
		CIMFlavorDialog dlg = new CIMFlavorDialog(thisFrame,
							  flavor, name, true);
		Vector retVal = dlg.getSelectedFlavors();
		if (retVal != null) {
		    flavor = retVal;
		}
	    }
	}
	
    }

    class QTListSelectionListener implements ListSelectionListener {
	// if user changes the type, we clear out any default value set.
	// this is done so we don't have an invalid value for the type.
	public void valueChanged(ListSelectionEvent evt) {
	    // only want to process this event when ValueIsAdjusting is false
	    if (!evt.getValueIsAdjusting()) {
		if (defaultValue != null) {
		    defaultValue = null;
		    defaultValueField.setText("");
		}
	    }
	}
    }
    
}
