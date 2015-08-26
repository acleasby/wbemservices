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

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.WindowEvent;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.wbemservices.wbem.apps.common.*;

import javax.wbem.cim.*;
import javax.wbem.client.CIMClient;

/**
 * 
 *
 * @author 	Sun Microsystems
 *
 */


public class AddClassDialog extends AdminDialog implements DocumentListener,
    ListSelectionListener {

    protected JButton btnOK, btnCancel;    
    protected GenInfoPanel infoPanel;
    protected JTextField classNameField;
    protected String className;
    
    private JButton btnAddProperty;
    private JButton btnDeleteProperty;
    private JButton btnPropertyQualifiers;
    private JButton btnClassQualifiers;
    private CIMTableTabbedPane tables;
    private CIMClass cimClass;
    private CIMClient cimClient;
    private int parentPropertyCount = 0;

    public AddClassDialog(Frame parent, String scName) {
	super(parent, I18N.loadString("TTL_NEW_CLASS"), false);

	infoPanel = this.getInfoPanel();

	btnOK = this.getOKBtn();
	btnOK.addActionListener(new ButtonListener());

	btnCancel = this.getCancelBtn();
	btnCancel.addActionListener(new ButtonListener());

	JPanel mainPanel = getRightPanel();	
	String tmpClassName = I18N.loadString("LBL_UNTITLED",
				    "org.wbemservices.wbem.apps.common.common");	

	cimClient = CIMClientObject.getClient();

	// create a temporary class so we can populate the table
	cimClass = new CIMClass(tmpClassName);
	if (!scName.equals("")) {
	    cimClass.setSuperClass(scName);
	    copySuperClassElements(scName);
	}

	classNameField = new JTextField(20);
	classNameField .addFocusListener(new TextFieldFocusListener());
	classNameField .getDocument().addDocumentListener(this);
	classNameField.setText(tmpClassName);
	mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
	mainPanel.setLayout(new BorderLayout());

	JPanel classNamePanel = new JPanel(new ColumnLayout(
					   LAYOUT_ALIGNMENT.EXPAND));
	classNamePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

	ActionString asClassName = new ActionString("LBL_CLASS_NAME");
	JLabel lClassName = new JLabel(asClassName.getString());
	lClassName.setDisplayedMnemonic(asClassName.getMnemonic());
	lClassName.setLabelFor(classNameField);
	classNamePanel.add(lClassName);
	classNamePanel.add(classNameField);
	mainPanel.add("North", classNamePanel);

	tables = new CIMTableTabbedPane(true);
	tables.removeMethodsTab();
	tables.addListSelectionListener(this);
	
	JPanel centerPanel = new JPanel(new BorderLayout());
	centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

	centerPanel.add("Center", tables);

	setUpControls();

	JPanel sideButtons = new JPanel();
	sideButtons.setBorder(BorderFactory.createEmptyBorder(25, 5, 5, 5));

	sideButtons.setLayout(new ColumnLayout());

	sideButtons.add(btnAddProperty);
	sideButtons.add(btnDeleteProperty);
	sideButtons.add(btnPropertyQualifiers);
	sideButtons.add(btnClassQualifiers);

	centerPanel.add("East", sideButtons);

	mainPanel.add("Center", centerPanel);	

	populateTables(cimClass);
	// setup URLS for help
	this.setDefaultFocusListener(new ContextHelpListener(
		infoPanel, "cimworkshop", "NewClass_000.htm"), true);

	btnOK.addFocusListener(new ContextHelpListener(
		infoPanel, "cimworkshop", "NewClass_000.htm"));

	btnCancel.addFocusListener(new ContextHelpListener(
		infoPanel, "cimworkshop", "NewClass_000.htm"));

	classNameField.addFocusListener(new ContextHelpListener(
		infoPanel, "cimworkshop", "NewClass_010.htm"));		
	btnAddProperty.addFocusListener(new ContextHelpListener(
		infoPanel, "cimworkshop", "NewClass_020.htm"));		
	btnDeleteProperty.addFocusListener(new ContextHelpListener(
		infoPanel, "cimworkshop", "NewClass_030.htm"));		
	btnPropertyQualifiers.addFocusListener(new ContextHelpListener(
		infoPanel, "cimworkshop", "NewClass_040.htm"));		
	btnClassQualifiers.addFocusListener(new ContextHelpListener(
		infoPanel, "cimworkshop", "NewClass_050.htm"));		
	

	setOKEnabled();
	setSize(DefaultProperties.addClassDlgSize);	
	Util.positionWindow(this, parent);
// BUGFIX. Accessibility fixes
	//classNameField.requestFocus();
	setVisible(true);

    }

    private void setUpControls() {
	ActionString asAddProperty = new ActionString("MNU_ADD_PROPERTY");
	ActionString asDeleteProperty = new ActionString("MNU_DELETE_PROPERTY");
	ActionString asPropertyQualifier = new ActionString(
	    "MNU_PROPERTY_QUALIFIERS");
	ActionString asClassQualifier = new ActionString(
	    "MNU_CLASS_QUALIFIERS");

	btnAddProperty = new JButton(asAddProperty.getString());
	btnAddProperty.setMnemonic(asAddProperty.getMnemonic());
	btnAddProperty.addActionListener(tables.getPropertiesTable());
	btnAddProperty.setActionCommand("ADD_PROPERTY");
	btnAddProperty.setToolTipText(asAddProperty.getString());
	btnAddProperty.setEnabled(true);

	btnDeleteProperty = new JButton(asDeleteProperty.getString());
	btnDeleteProperty.setMnemonic(asDeleteProperty.getMnemonic());
	btnDeleteProperty.addActionListener(tables.getPropertiesTable());
	btnDeleteProperty.setActionCommand("DELETE_PROPERTY");
	btnDeleteProperty.setToolTipText(asDeleteProperty.getString());
	btnDeleteProperty.setEnabled(false);

	  
	btnPropertyQualifiers = new JButton(asPropertyQualifier.getString());
	btnPropertyQualifiers.setMnemonic(
	    asPropertyQualifier.getMnemonic()); 
	btnPropertyQualifiers.addActionListener(tables.getPropertiesTable());
	btnPropertyQualifiers.setActionCommand("QUALIFIERS");
	btnPropertyQualifiers.setToolTipText(asPropertyQualifier.getString());
	btnPropertyQualifiers.setEnabled(false);

	btnClassQualifiers = new JButton(asClassQualifier.getString());
	btnClassQualifiers.setMnemonic(asClassQualifier.getMnemonic());
	btnClassQualifiers.addActionListener(new ButtonListener());
	btnClassQualifiers.setToolTipText(asClassQualifier.getString());
	btnClassQualifiers.setEnabled(true);
    }	

    private void copySuperClassElements(String scName) {
	CIMClass cimSuperClass;
	try {
	    cimSuperClass = cimClient.getClass(new CIMObjectPath(scName), 
					       false, true,
					       true, null);
	    Vector v = new Vector();
	    Vector propVector = cimSuperClass.getProperties();
	    if (propVector != null) {
		Enumeration e;
		for (e = propVector.elements(); e.hasMoreElements(); ) {
		    CIMProperty prop = (CIMProperty)
			((CIMProperty)e.nextElement()).clone();
		    if (prop.getOriginClass() == null) {
			prop.setOriginClass(scName);
		    }
		    v.addElement(prop);
		}
	    }
	    cimClass.setProperties(v);
	    v = new Vector();
	    Vector qualVector = cimSuperClass.getQualifiers();
	    if (qualVector != null) {
		Enumeration e;
		for (e = qualVector.elements(); e.hasMoreElements(); ) {
		    CIMQualifier qual;
		    qual = (CIMQualifier)
			   ((CIMQualifier)e.nextElement()).clone();
		    v.addElement(qual);
		}
	    }
	    cimClass.setQualifiers(v);
	    v = new Vector();
	    Vector methVector = cimSuperClass.getMethods();
	    if (methVector != null) {
		Enumeration e;
		for (e = methVector.elements(); e.hasMoreElements(); ) { 
		    CIMMethod meth = (CIMMethod)
				     ((CIMMethod)e.nextElement()).clone();
		    v.addElement(meth);
		}
	    }
	    cimClass.setMethods(v);
	} catch (CIMException exc) {
	    return;
	}

    }

    private void populateTables(CIMClass superClass) {
	tables.populateTables(cimClient, superClass);
	// get count of parent properties
	parentPropertyCount = tables.getProperties().size();
	Vector uneditableProperties = new Vector();
	for (int i = 0; i < parentPropertyCount; i++) {
	    uneditableProperties.addElement(new Integer(i));
	}
	tables.setUneditableProperties(uneditableProperties);
    }

    public void valueChanged(ListSelectionEvent evt) {
	// only want to process this event when ValueIsAdjusting is false
	if (evt.getValueIsAdjusting()) {
	    return;
	}

	btnPropertyQualifiers.setEnabled(true);

	int selectedRow = tables.getPropertiesTable().getSelectedRow();
	boolean bEnable = selectedRow > parentPropertyCount - 1;
	btnDeleteProperty.setEnabled(bEnable);
    }

    private boolean saveValues() {
	boolean ret = true;
	Util.setWaitCursor(this);
	// get class properties
	Vector props = tables.getProperties();

	Vector newProps = new Vector();
	// get properties that were added
	for (int i = parentPropertyCount; i < props.size(); i++) {
	    newProps.addElement(props.elementAt(i));
	}

	// set new properties to class and set the class
	if (props.size() > 0) {
	    cimClass.setProperties(newProps);
	}
	cimClass.setName(className);
	try {
	    cimClient.createClass(new CIMObjectPath(className), 
				  cimClass);
	} catch (CIMException exc) {
	    if (exc.getID().equals("CIM_ERR_INVALID_PARAMETER")) {
		JOptionPane.showMessageDialog(null, Util.wrapText(
		    I18N.loadString("MSG_INVALID_CLASS_NAME")), 
		    I18N.loadString("TTL_CIM_ERROR"), 
		    JOptionPane.ERROR_MESSAGE);
	    } else {
		CIMErrorDialog.display(this, exc);
	    }
	    ret = false;
	}
	Util.setDefaultCursor(this);
	return ret;
    }


    public String getClassName() {
	return className;
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
	btnOK.setEnabled(classNameField.getText().trim().length() != 0);
    }

    public void windowOpened(WindowEvent evt) {
// BUGFIX. Accessibility fixes
	classNameField.requestFocus();
    }

    public void focusGained(FocusEvent evt) {
	if (evt.getComponent() instanceof JTextField) {
	    JTextField tf = (JTextField) evt.getComponent();
	    if (tf.isEditable() && (tf.getText().length() > 0)) {
		tf.setCaretPosition(tf.getText().length());
		tf.selectAll();
	    }
	}
    }
    public void focusLost(FocusEvent evt) {
	if (evt.getComponent() instanceof JTextField) {
	    JTextField tf = (JTextField) evt.getComponent();
	    tf.select(0, 0);
	}
    }

    public boolean verifyClassName() {
	String cName = classNameField.getText().trim();

	String untitled  = I18N.loadString("LBL_UNTITLED",
	    "org.wbemservices.wbem.apps.common.common");
	
	if (cName.equals(untitled)) {
	    JOptionPane.showMessageDialog(null, I18N.loadStringFormat( 
		"ERR_CHANGE_CLASSNAME", untitled),
		I18N.loadString("TTL_CIM_ERROR"), 
		JOptionPane.ERROR_MESSAGE);
	    return false;
	}

	className = cName;
	return true;
    }

    class ButtonListener implements ActionListener {
	public void actionPerformed(ActionEvent e) {
	    if (e.getSource() == btnCancel) { // cancel button
		cancelClicked();
	    } else if (e.getSource() == btnOK) { // OK button
		okClicked();
	    } else if (e.getSource() == btnClassQualifiers) { // qualifiers btn
		classQualifiersClicked();
	    }
	}
    }

    public void okClicked() {
	if (!verifyClassName()) {
	    return;
	}
	if (saveValues()) {
	    dispose();
	}
    }
    
    public void cancelClicked() {
	className = null;
	dispose();
    }

    public void classQualifiersClicked() {	
	cimClass.setName(classNameField.getText().trim());
	QualifierWindow qualifierWindow = new QualifierWindow(
	    Util.getFrame(this), cimClient, cimClass, true);
    }
}
