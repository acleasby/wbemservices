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

import javax.swing.JButton;
import javax.swing.JPanel;

import javax.swing.JLabel;
import javax.swing.JTable;

import javax.swing.BorderFactory;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.event.WindowEvent;

import javax.wbem.cim.CIMElement;
import javax.wbem.client.CIMClient;

import org.wbemservices.wbem.apps.common.*;

/**
 * 
 *
 * @author 	Sun Microsystems
 */


public class QualifierWindow extends AdminDialog implements 
    ListSelectionListener, TableModelListener {
    

    protected JButton btnOK, btnCancel;    
    protected GenInfoPanel infoPanel;
    protected CIMQualifiersPanel cimQualifiersPanel;
    protected CIMClient cimClient;
    protected CIMElement parentElement;
    private JButton btnScope;
    private JButton btnFlavor;
    private JButton btnDeleteQualifier;

    public QualifierWindow(Frame parent, CIMClient cc, CIMElement pElement, 
			   boolean nElement) {
	super(parent, I18N.loadStringFormat("TTL_QUALIFIERS", 
	      pElement.getName()), false);
	infoPanel = this.getInfoPanel();

	btnCancel = this.getCancelBtn();
	btnCancel.addActionListener(new ButtonListener());

	btnOK = this.getOKBtn();
	if (nElement) {
    	    btnOK.addActionListener(new ButtonListener());
	} else {
	    getbuttonPanel().remove(btnOK);
	    btnCancel.setText(I18N.loadString("LBL_CLOSE",
			      "org.wbemservices.wbem.apps.common.common"));
	}

	JPanel mainPanel = getRightPanel();	
	
	mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

	JPanel centerPanel = new JPanel(new BorderLayout());
	centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

// BUGFIX. Accessibility fixes
	ActionString asQualifiers = new ActionString("LBL_QUALIFIERS");
	JLabel lQualifiers = new JLabel(asQualifiers.getString() + ":");
	lQualifiers.setDisplayedMnemonic(asQualifiers.getMnemonic());


	cimQualifiersPanel = new CIMQualifiersPanel(cc, pElement, nElement);
	lQualifiers.setLabelFor(cimQualifiersPanel.getTable());
	lQualifiers.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

	centerPanel.add("North", lQualifiers);
	centerPanel.add("Center", cimQualifiersPanel);
//

	ActionString asScope = new ActionString("MNU_SCOPE");
	ActionString asFlavors = new ActionString("MNU_FLAVOR");
	ActionString asAdd = new ActionString("MNU_ADD_QUALIFIER");
	ActionString asDelete = new ActionString("MNU_DELETE_QUALIFIER");

	btnScope = new JButton(asScope.getString());
	btnScope.setMnemonic(asScope.getMnemonic());
	btnScope.addActionListener(cimQualifiersPanel);
	btnScope.setActionCommand("SCOPE");
	btnScope.setEnabled(false);

	btnFlavor = new JButton(asFlavors.getString());
	btnFlavor.setMnemonic(asFlavors.getMnemonic());
	btnFlavor.addActionListener(cimQualifiersPanel);
	btnFlavor.setActionCommand("FLAVOR");
	btnFlavor.setEnabled(false);

	JButton btnAddQualifier = new JButton(asAdd.getString());
	btnAddQualifier.setMnemonic(asAdd.getMnemonic());
	btnAddQualifier.addActionListener(cimQualifiersPanel);
	btnAddQualifier.setActionCommand("ADD_QUALIFIER");
	btnAddQualifier.setEnabled(true);


	btnDeleteQualifier = new JButton(asDelete.getString());
	btnDeleteQualifier.setMnemonic(asDelete.getMnemonic());
	btnDeleteQualifier.addActionListener(cimQualifiersPanel);
	btnDeleteQualifier.setActionCommand("DELETE_QUALIFIER");
	btnDeleteQualifier.setEnabled(false);

	JPanel sideButtons = new JPanel();
	sideButtons.setBorder(BorderFactory.createEmptyBorder(25, 10, 5, 5));
	sideButtons.setLayout(new ColumnLayout());
    
	sideButtons.add(btnScope);
	sideButtons.add(btnFlavor);
	if (nElement) {
	    sideButtons.add(btnAddQualifier);
	    sideButtons.add(btnDeleteQualifier);
	}

	centerPanel.add("East", sideButtons);

	mainPanel.add("Center", centerPanel);	
	// setup URLS for help
	if (nElement) {
	    this.setDefaultFocusListener(new ContextHelpListener(
	        infoPanel, "cimworkshop", "qualifiers_000.htm"), true);
	} else {
	    this.setDefaultFocusListener(new ContextHelpListener(
	        infoPanel, "cimworkshop", "qualifiers_001.htm"), true);
	}

	btnScope.addFocusListener(new ContextHelpListener(
		infoPanel, "cimworkshop", "qualifiers_010.htm"));		
	btnFlavor.addFocusListener(new ContextHelpListener(
		infoPanel, "cimworkshop", "qualifiers_020.htm"));		
	btnAddQualifier.addFocusListener(new ContextHelpListener(
		infoPanel, "cimworkshop", "qualifiers_030.htm"));		
	btnDeleteQualifier.addFocusListener(new ContextHelpListener(
		infoPanel, "cimworkshop", "qualifiers_040.htm"));		
	

	cimQualifiersPanel.addListSelectionListener(this);
	cimQualifiersPanel.addTableListener(this);
	setSize(DefaultProperties.qualifierDlgSize);	
	setLocation(Util.getCenterPoint(parent, this));
	setVisible(true);
    }

    public void valueChanged(ListSelectionEvent evt) {
	// only want to process this event when ValueIsAdjusting is false
	if (evt.getValueIsAdjusting()) {
	    return;
	}

	int selectedRow = cimQualifiersPanel.getSelectedRow();
	boolean bEnable = selectedRow >= 0;
	btnDeleteQualifier.setEnabled(bEnable);
	btnScope.setEnabled(bEnable);
	btnFlavor.setEnabled(bEnable);
    }

    public void tableChanged(TableModelEvent e) {
	if (cimQualifiersPanel.getQualifiers().size() == 0) {
	    btnDeleteQualifier.setEnabled(false);
	    btnScope.setEnabled(false);
	    btnFlavor.setEnabled(false);
	}
    }

    public void populateTable(CIMElement pElement) {
	cimQualifiersPanel.populateTable(cimClient, pElement);
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
	dispose();
    }

    public void cancelClicked() {
	dispose();
    }    

// BUGFIX. Accessibility fixes
    // when window opens, request focus on table
    public void windowOpened(WindowEvent evt) {
	super.windowOpened(evt);
	JTable table = cimQualifiersPanel.getTable();
	table.requestFocus();
       	if (table.getRowCount() > 0) {
	  table.setRowSelectionInterval(0, 0);
	}
	    
    }

}
