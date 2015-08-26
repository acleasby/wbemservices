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
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.wbem.cim.CIMClass;
import javax.wbem.cim.CIMInstance;
import javax.wbem.cim.CIMProperty;
import javax.wbem.cim.CIMValue;
import javax.wbem.cim.CIMException;
import javax.wbem.client.CIMClient;

import org.wbemservices.wbem.apps.common.ActionString;
import org.wbemservices.wbem.apps.common.AdminDialog;
import org.wbemservices.wbem.apps.common.ContextHelpListener;
import org.wbemservices.wbem.apps.common.GenInfoPanel;
import org.wbemservices.wbem.apps.common.I18N;
import org.wbemservices.wbem.apps.common.Util;

/**
 * 
 *
 * @author 	Sun Microsystems
 */

public class CreateInstanceDialog extends AdminDialog implements 
    ActionListener, TableModelListener {

    protected JButton btnOK, btnCancel;
    protected GenInfoPanel infoPanel;
    protected CIMClient cimClient;
    protected CIMInstance newInstance;
    protected String selectedNameSpace;
    protected CIMKeyPropertiesPanel table;
    protected JTextArea msg;
    protected Vector properties;

    public CreateInstanceDialog(Frame parent, CIMClient cc, CIMClass cClass) {
	super(parent, "", false);

	setTitle(I18N.loadStringFormat("TTL_ADD_INSTANCE", cClass.getName()));
	cimClient = cc;

	infoPanel = this.getInfoPanel();

	JPanel pane = getRightPanel();
// BUGFIX. Accessibility fixes
	pane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	pane.setLayout(new BorderLayout());

	ActionString asProps = new ActionString("LBL_PROPERTIES");
	JLabel lProps = new JLabel(asProps.getString() + ":");
	lProps.setDisplayedMnemonic(asProps.getMnemonic());

	newInstance = cClass.newInstance();
	table = new CIMKeyPropertiesPanel(cimClient, newInstance);
	table.setPreferredSize(new Dimension(300, 200));
	
	lProps.setLabelFor(table.getTable());
	lProps.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
//
	table.addTableListener(this);

	btnOK = this.getOKBtn();
	btnOK.addActionListener(new OKCancelButtonListener());

	btnCancel = this.getCancelBtn();
	btnCancel.addActionListener(new OKCancelButtonListener());

// BUGFIX. Accessibility fixes
	pane.add("North", lProps);
	pane.add("Center", table);
//
	// setup URLS for help
	this.setDefaultFocusListener(new ContextHelpListener(
		infoPanel, "cimworkshop", "AddInstance_000.htm"), true);

	btnOK.addFocusListener(new ContextHelpListener(
	    infoPanel, "cimworkshop", "AddInstance_000.htm"));		
	btnCancel.addFocusListener(new ContextHelpListener(
	    infoPanel, "cimworkshop", "AddInstance_000.htm"));		
	
	pack();
	enableOKBtn();
	setLocation(Util.getCenterPoint(parent, this));
	setVisible(true);
    }

    protected void enableOKBtn() {
	Vector newProperties = table.getProperties();
	Enumeration e = newProperties.elements();
	while (e.hasMoreElements()) {
	    CIMProperty cp = (CIMProperty)e.nextElement();
	    if (cp.isKey()) {
		CIMValue cv = cp.getValue();
		if (cv == null || cv.getValue() == null) {
		    btnOK.setEnabled(false);
		    return;
		}
	    }
	}
	btnOK.setEnabled(true);
    }

    public void tableChanged(TableModelEvent evt) {
	enableOKBtn();
    }

    public CIMInstance getInstance() {
	return newInstance;
    }

    public void okClicked() {	
	Vector newProperties = table.getProperties();
    try {
        newInstance.updatePropertyValues(newProperties);
    } catch(CIMException ex) {
        ex.printStackTrace();
    }
	dispose();
    }
    
    public void cancelClicked() {
	newInstance = null;
	dispose();
    }


    class OKCancelButtonListener implements ActionListener {
	public void actionPerformed(ActionEvent e) {
	    if (e.getSource() == btnCancel) { // cancel button
		cancelClicked();
	    } else if (e.getSource() == btnOK) { // OK button
		okClicked();
	    }
	}
    }

// BUGFIX. Accessibility fixes
    // when window opens, request focus on table
    public void windowOpened(WindowEvent evt) {
	super.windowOpened(evt);
	JTable propTable =  table.getTable();
	propTable.requestFocus();
       	if (propTable.getRowCount() > 0) {
	  propTable.setRowSelectionInterval(0, 0);
	}
	    
    }

}
