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

import javax.wbem.cim.CIMScope;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.Enumeration;
import java.util.Vector;

/**
 * 
 *
 * @author 	Sun Microsystems
 */

public class CIMScopeDialog extends AdminDialog implements 
    ListSelectionListener {

    protected JButton btnOK, btnCancel;    
    protected GenInfoPanel infoPanel;
    protected JList list;
    protected Vector scope = null;
    protected boolean newElement;

    public CIMScopeDialog(Frame parent, 
			  Vector prevScope,
			  String name,
			  boolean nElement) {

	super(parent, I18N.loadStringFormat("TTL_SCOPE", name), false);
	infoPanel = this.getInfoPanel();


	JPanel mainPanel = getRightPanel();	
		
	newElement = nElement;

	mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

	JPanel centerPanel = new JPanel(new ColumnLayout(
					LAYOUT_ALIGNMENT.EXPAND));
	centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

	ScopeTypes scopeTypes = new ScopeTypes();
	list = new JList(scopeTypes.getTypes());

	list.getSelectionModel().setSelectionMode(
	    ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	Enumeration e = prevScope.elements();
	while (e.hasMoreElements()) {
	    CIMScope cimScope = (CIMScope)e.nextElement();
	    int i = cimScope.getScope();
	    list.addSelectionInterval(i, i);
	}


	JScrollPane scrollList = new JScrollPane(list);
	ActionString asScope = new ActionString("MNU_SCOPE");
// BUGFIX. Accessibility fixes
	JLabel lScope = new JLabel(asScope.getString() + ":");
	lScope.setDisplayedMnemonic(asScope.getMnemonic());
	lScope.setLabelFor(list);
//
	centerPanel.add(lScope);
	centerPanel.add(scrollList);

	btnCancel = this.getCancelBtn();
	btnCancel.addActionListener(new ButtonListener());

	btnOK = this.getOKBtn();
	if (nElement) {
    	    btnOK.addActionListener(new ButtonListener());
	    lScope.setDisplayedMnemonic(asScope.getMnemonic());
	    lScope.setLabelFor(list);
	} else {
	    getbuttonPanel().remove(btnOK);
	    btnCancel.setText(I18N.loadString("LBL_CLOSE",
			      "org.wbemservices.wbem.apps.common.common"));
	}

	mainPanel.add(centerPanel);

	list.setEnabled(nElement);
	// setup URLS for help
	this.setDefaultFocusListener(new ContextHelpListener(
		infoPanel, "cimworkshop", "scope_000.htm"), true);
	
	pack();
	setLocation(Util.getCenterPoint(parent, this));
	setVisible(true);
    }

    public Vector getSelectedScope() {
	return scope;
    }


    public void valueChanged(ListSelectionEvent evt) {
	// only want to process this event when ValueIsAdjusting is false
	if (evt.getValueIsAdjusting()) {
	    return;
	}
    }

    public void cancelClicked() {
	scope = null;
	dispose();
    }

    public void okClicked() {
	if (newElement) {
	    int[] selScope = list.getSelectedIndices();
	    scope = new Vector();
	    for (int i = 0; i < selScope.length; i++) {
	        scope.addElement(new CIMScope(selScope[i]));
	    }
	}
	dispose();
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

// BUGFIX. Accessibility fixes
    // when window opens, request focus on list if new element, otherwise
    // request focus on cancel button
    public void windowOpened(WindowEvent evt) {
	super.windowOpened(evt);
	if (newElement) {
	    list.requestFocus();
	} else {
	    btnCancel.requestFocus();
	}
	    
    }

}
