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

import javax.swing.*;
import javax.swing.event.*;

import java.awt.*;
import java.awt.event.*;

import org.wbemservices.wbem.apps.common.*;

import javax.wbem.cim.*;

/**
 * 
 *
 * @version 	%I%, %G%
 * @author 	Sun Microsystems
 */

public class CIMEditDialog extends AdminDialog implements DocumentListener {

    private JButton okBtn, cancelBtn;
    private GenInfoPanel infoPanel;
    protected JTextField nameField;
    protected JTextField valueField;
    protected String returnString = null;
    protected String nameString = null;
    protected Object returnObject = null;
    protected String dataType;
    protected int cimType;
    protected CIMTypes cimTypes;
    protected boolean isEditable = true;
    private JPanel mainPanel;    

    public CIMEditDialog(Frame parent) {
	this(parent, "", "", true);
    }

    public CIMEditDialog(Frame parent, String name, String type) {
	this(parent, name, type, true);
    }

    public CIMEditDialog(Frame parent, String name, String type, 
			 boolean editable) {
	super(parent, I18N.loadString("TTL_DLG_CIM_WORKSHOP"), false);

	infoPanel = super.getInfoPanel();

	isEditable = editable;
	

	cancelBtn = this.getCancelBtn();
	cancelBtn.addActionListener(new OKCancelButtonListener());
	okBtn = this.getOKBtn();

	if (isEditable) {
	    okBtn.addActionListener(new OKCancelButtonListener());
	    setTitle(I18N.loadStringFormat("TTL_SET_VALUE", name));
	} else {
	    JPanel bPanel = getbuttonPanel();
	    bPanel.remove(okBtn);
	    cancelBtn.setText(I18N.loadString("LBL_CLOSE",
			      "org.wbemservices.wbem.apps.common.common"));
	    setTitle(I18N.loadStringFormat("TTL_SHOW_VALUE", name));
	}
	mainPanel = getRightPanel();	
	mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 15, 15, 15));
	mainPanel.setLayout(new ColumnLayout(LAYOUT_ALIGNMENT.EXPAND));
	dataType = type;
	cimTypes = new CIMTypes();

	cimType = cimTypes.getCIMType(type);


	ActionString asType = new ActionString("LBL_TYPE");
	JLabel typeLabel = new JLabel(asType.getString() + ":  " + type);

	if (name == null) {
	    nameField = new JTextField();
	    if (isEditable) {
		nameField.getDocument().addDocumentListener(this);
		nameField.addFocusListener(new TextFieldFocusListener());
	    } else {
		nameField.setEnabled(false);
	    }
	    mainPanel.add(nameField);
	} else {

	    ActionString asName = new ActionString("LBL_NAME", 
	        "org.wbemservices.wbem.apps.common.common");
    	    JLabel nameLabel = new JLabel(asName.getString() + ":  " + name);
	    mainPanel.add(nameLabel);
	    nameString = name;
	}
	mainPanel.add(typeLabel);
	setOKEnabled();
    }

    public JPanel getMainPanel() {
    	return mainPanel;
    }

    protected void setDefaultHelp(String helpFile) {
	GenInfoPanel infoPanel = this.getInfoPanel();
	this.setDefaultFocusListener(new ContextHelpListener(
		infoPanel, "cimworkshop", helpFile), true);
	if (okBtn != null) {
	    okBtn.addFocusListener(new ContextHelpListener(infoPanel, 
				      "cimworkshop", helpFile));
	}
	if (cancelBtn != null) {
	    cancelBtn.addFocusListener(new ContextHelpListener(infoPanel, 
				      "cimworkshop", helpFile));
	}
    }    

    public GenInfoPanel getInfoPanel() {
	return infoPanel;
    }    

    public void okClicked() {	
	if (valueField != null) {
	    returnString = valueField.getText().trim();
	}
	if (nameField != null) {
	    nameString = nameField.getText().trim();
	}
	if (setReturnObject()) {
	    dispose();
	}
    }
    
    public void cancelClicked() {
	returnString = null;
	returnObject = new CancelObject();
	nameString = null;
    	dispose();
    }
    
    class OKCancelButtonListener implements ActionListener {
	public void actionPerformed(ActionEvent e) {
	    if (e.getSource() == cancelBtn) { // cancel button
		cancelClicked();
	    } else if (e.getSource() == okBtn) { // OK button
		okClicked();
	    }
	}
    }
    
    public Object getValueObject() {
	return returnObject;
    }

    public String getValueString() {
	return returnString;
    }

    public String getNameString() {
	return nameString;
    }

    public void windowOpened(WindowEvent evt) {
	if (!isEditable) {
	    cancelBtn.requestFocus();
	} else if (nameField != null) {
	    nameField.requestFocus();
	} else if (valueField != null) {
	    valueField.requestFocus();
	} else {
	    okBtn.requestFocus();
	}
    }

    public void changedUpdate(DocumentEvent evt) {
	setOKEnabled();
	}
    public void insertUpdate(DocumentEvent evt) {
	setOKEnabled();
	}

    public void removeUpdate(DocumentEvent evt) {
	setOKEnabled();
	}

    public void setOKEnabled() {
	boolean b = true;
	if (!isEditable) {
	    return;
	}
	if (nameField != null) {
	    b = (nameField.getText().trim().length() != 0);
	}
	okBtn.setEnabled(b);	
    }

    public void setOKEnabled(boolean b) {
	okBtn.setEnabled(b);
    }

    public boolean setReturnObject() {
	if (returnString == null) {
	    returnObject = null;
	    return false;
	} else if (returnString.length() == 0) {
	    returnObject = null;
	    return true;
	}
	try {
	    switch (cimType) {
	    case CIMDataType.SINT8:
		returnObject = new Byte(returnString);
		break;
	    case CIMDataType.SINT16:
		returnObject = new Short(returnString);
		break;
	    case CIMDataType.SINT32:
		returnObject = new Integer(returnString);
		break;
	    case CIMDataType.SINT64:
		returnObject = new Long(returnString);
		break;
	    case CIMDataType.UINT8:
		returnObject = new UnsignedInt8(returnString);
		break;
	    case CIMDataType.UINT16:
		returnObject = new UnsignedInt16(returnString);
		break;
	    case CIMDataType.UINT32:
		returnObject = new UnsignedInt32(returnString);
		break;
	    case CIMDataType.UINT64:
		returnObject = new UnsignedInt64(returnString);
		break;
	    case CIMDataType.STRING:
		returnObject = returnString;
		break;
	    case CIMDataType.BOOLEAN:
		ActionString asTrue = new ActionString("LBL_TRUE",
	            "org.wbemservices.wbem.apps.common.common");
		boolean b = returnString.equals(asTrue.getString());
		returnObject = new Boolean(b);
		break;
	    case CIMDataType.REAL32:
		returnObject = new Float(returnString);
		break;
	    case CIMDataType.REAL64:
		returnObject = new Double(returnString);
		break;
	    case CIMDataType.DATETIME:
		returnObject = new CIMDateTime(returnString);
		break;
	    case CIMDataType.CHAR16:
		returnObject = new Character(returnString.charAt(0));
		break;
	    default:
		returnObject = returnString;
	    }
	} catch (NumberFormatException exc) {
	    JOptionPane.showMessageDialog(this, I18N.loadStringFormat(
		"ERR_INVALID_VALUE_TYPE", returnString, dataType), 
		I18N.loadString("TTL_CIM_ERROR"), 
		JOptionPane.ERROR_MESSAGE);
	    return false;
	} catch (IllegalArgumentException exc) {
	    JOptionPane.showMessageDialog(this, I18N.loadString(
		"ERR_INVALID_DATETIME_STRING"),
		I18N.loadString("TTL_CIM_ERROR"),
		JOptionPane.ERROR_MESSAGE);
	    return false;
	}

	return true;
    }

}

