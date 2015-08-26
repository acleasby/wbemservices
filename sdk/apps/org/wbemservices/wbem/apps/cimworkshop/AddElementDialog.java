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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.WindowEvent;
import java.awt.Frame;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.wbemservices.wbem.apps.common.*;

/**
 * 
 *
 * @version 	1.15, 08/16/01
 * @author 	Sun Microsystems
 */

public class AddElementDialog extends AdminDialog implements DocumentListener {

    protected JButton btnOK, btnCancel;    
    protected GenInfoPanel infoPanel;
    protected String nameString;
    protected String typeString;
    protected JTextField nameField;
    protected JList list;
    private JPanel mainPanel;
    
    public AddElementDialog(Frame parent) {
	super(parent, I18N.loadString("TTL_DLG_CIM_WORKSHOP"), false);
	infoPanel = this.getInfoPanel();

	btnOK = this.getOKBtn();
	btnOK.addActionListener(new ButtonListener());

	btnCancel = this.getCancelBtn();
	btnCancel.addActionListener(new ButtonListener());
	mainPanel = getRightPanel();	
	
	mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 15, 15, 15));
	mainPanel.setLayout(new ColumnLayout(LAYOUT_ALIGNMENT.EXPAND));


	nameField = new JTextField(20);
	nameField.getDocument().addDocumentListener(this);
	nameField.addFocusListener(new TextFieldFocusListener());
	JPanel textFieldPanel = new JPanel(new ColumnLayout(
					   LAYOUT_ALIGNMENT.EXPAND, 0, 5));
	textFieldPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
	
	ActionString asName = new ActionString("LBL_NAME",
			       "org.wbemservices.wbem.apps.common.common");
	JLabel lName = new JLabel(asName.getString() + ":");
	lName.setDisplayedMnemonic(asName.getMnemonic());
	lName.setLabelFor(nameField);
	
	textFieldPanel.add(lName);
	textFieldPanel.add(nameField);

	CIMTypes cimTypes = new CIMTypes();
	list = new JList(cimTypes.getTypes());
	JScrollPane scrollPane = new JScrollPane(list);
	list.setVisibleRowCount(10);
	list.getSelectionModel().setSelectionMode(
	    ListSelectionModel.SINGLE_SELECTION);
	list.setSelectedIndex(0);


	ActionString asType = new ActionString("LBL_TYPE");
	JLabel lType = new JLabel(asType.getString() + ":");
	lType.setDisplayedMnemonic(asType.getMnemonic());
	lType.setLabelFor(list);
	mainPanel.add(textFieldPanel);
	mainPanel.add(lType);
	mainPanel.add(scrollPane);


	setOKEnabled();

    }


    /**
     * called if an insert is done to a document listener
     */
    public void insertUpdate(DocumentEvent e) {
	setOKEnabled();
    }

    /**
     * called if a remove is done to a document listener
     */
    public void removeUpdate(DocumentEvent e) {
	setOKEnabled();
    }

    /**
     * called if an atribute is changed on one of the document listeners
     */
    public void changedUpdate(DocumentEvent e) {
	setOKEnabled();
    }

    public void setOKEnabled() {	
	btnOK.setEnabled(nameField.getText().trim().length() != 0);
    }

    public void windowActivated(WindowEvent evt) {}
    public void windowClosed(WindowEvent evt) {}
    public void windowClosing(WindowEvent evt) {}
    public void windowDeactivated(WindowEvent evt) {}
    public void windowDeiconified(WindowEvent evt) {}
    public void windowIconified(WindowEvent evt) {}
    public void windowOpened(WindowEvent evt) {
	nameField.requestFocus();	
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

    public String getName() {
	return nameString;
    }

    public String getSelectedType() {
	return typeString;
    }

    class ButtonListener implements ActionListener {
	public void actionPerformed(ActionEvent e) {
	    if (e.getSource() == btnCancel) { // cancel button
		cancelClicked();
	    } else if (e.getSource() == btnOK) { // OK button
		okClicked();
	    }
	}
    }

    public void okClicked() {
	nameString = nameField.getText().trim();
	typeString = (String)list.getSelectedValue();
	dispose();
    }

    public void cancelClicked() {
	nameString = null;
	typeString = null;
	dispose();
    }    

    public JPanel getMainPanel() {
	return mainPanel;
    }
}
