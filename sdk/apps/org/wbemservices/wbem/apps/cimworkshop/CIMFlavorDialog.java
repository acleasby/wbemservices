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

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.wbem.cim.CIMFlavor;

/**
 * 
 *
 * @author 	Sun Microsystems
 */
public class CIMFlavorDialog extends AdminDialog implements 
    ListSelectionListener {

    protected JButton btnOK, btnCancel;    
    protected GenInfoPanel infoPanel;
    protected JList list;
    protected Vector flavors = null;
    protected boolean newElement;

    public CIMFlavorDialog(Frame parent, 
			  Vector prevFlavor,
			  String name,
			  boolean nElement) {

	super(parent, I18N.loadStringFormat("TTL_FLAVOR", name), false);
	infoPanel = this.getInfoPanel();


	JPanel mainPanel = getRightPanel();	
	newElement = nElement;

	mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

	JPanel centerPanel = new JPanel(new ColumnLayout(
					LAYOUT_ALIGNMENT.EXPAND));
	centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

	FlavorTypes flavorTypes = new FlavorTypes();
	list = new JList(flavorTypes.getTypes());

	list.getSelectionModel().setSelectionMode(
	    ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

	Enumeration e = prevFlavor.elements();
	while (e.hasMoreElements()) {
	    CIMFlavor cimFlavor = (CIMFlavor)e.nextElement();
	    int i = cimFlavor.getFlavor();
	    list.addSelectionInterval(i, i);
	}


	JScrollPane scrollList = new JScrollPane(list);
	ActionString asFlavors = new ActionString("MNU_FLAVOR");
// BUGFIX. Accessibility fixes
	JLabel lFlavors = new JLabel(asFlavors.getString() + ":");
	lFlavors.setDisplayedMnemonic(asFlavors.getMnemonic());
	lFlavors.setLabelFor(list);

	centerPanel.add(lFlavors);
	centerPanel.add(scrollList);


	btnCancel = this.getCancelBtn();
	btnCancel.addActionListener(new ButtonListener());

	btnOK = this.getOKBtn();
	if (nElement) {
    	    btnOK.addActionListener(new ButtonListener());
	    lFlavors.setDisplayedMnemonic(asFlavors.getMnemonic());
	    lFlavors.setLabelFor(list);
	} else {
	    getbuttonPanel().remove(btnOK);
	    btnCancel.setText(I18N.loadString("LBL_CLOSE",
			      "org.wbemservices.wbem.apps.common.common"));
	}

	mainPanel.add(centerPanel);
	list.setEnabled(nElement);

	// setup URLS for help
	this.setDefaultFocusListener(new ContextHelpListener(
		infoPanel, "cimworkshop", "flavors_000.htm"), true);	
	pack();
	setLocation(Util.getCenterPoint(parent, this));
	setVisible(true);
    }

    public Vector getSelectedFlavors() {
	return flavors;
    }


    public void valueChanged(ListSelectionEvent evt) {
	// only want to process this event when ValueIsAdjusting is false
	if (evt.getValueIsAdjusting()) {
	    return;
	}
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
	int[] selFlavors = list.getSelectedIndices();
	flavors = new Vector();
	for (int i = 0; i < selFlavors.length; i++) {
	    flavors.addElement(new CIMFlavor(selFlavors[i]));
	}	    
	dispose();
    }

    public void cancelClicked() {
	flavors = null;
	dispose();
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
