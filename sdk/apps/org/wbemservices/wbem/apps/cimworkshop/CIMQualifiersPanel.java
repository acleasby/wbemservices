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
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.event.TableModelEvent;

import org.wbemservices.wbem.apps.common.ActionString;
import org.wbemservices.wbem.apps.common.CIMErrorDialog;
import org.wbemservices.wbem.apps.common.I18N;
import org.wbemservices.wbem.apps.common.Util;

import javax.wbem.cim.*;
import javax.wbem.client.CIMClient;

/**
 * 
 *
 * @author 	Sun Microsystems
 */

public class CIMQualifiersPanel extends CIMElementsTable {


    public CIMQualifiersPanel(CIMClient cc, CIMElement pElement, 
			      boolean nElement) {
	super(QUALIFIER_TABLE, nElement);
	newElement = nElement;
	populateTable(cc, pElement);

// BUGFIX. Accessibility fixes
	ActionString asQualifiers = new ActionString("LBL_QUALIFIERS");
	JLabel lQualifiers = new JLabel(asQualifiers.getString() + ":");
	lQualifiers.setDisplayedMnemonic(asQualifiers.getMnemonic());
	setColumnHeaderView(lQualifiers);
	lQualifiers.setLabelFor(table);
//
	table.addMouseListener(this);
    	JMenuItem menuItem;
	ActionString asScope = new ActionString("MNU_SCOPE");
	ActionString asFlavors = new ActionString("MNU_FLAVOR");
	menuItem = popupMenu.add(new JMenuItem(asScope.getString()));
	menuItem.setActionCommand("SCOPE");
	menuItem.addActionListener(this);
	menuItem = popupMenu.add(new JMenuItem(asFlavors.getString()));
	menuItem.setActionCommand("FLAVOR");
	menuItem.addActionListener(this);

	if (nElement) {
	    ActionString asAdd = new ActionString("MNU_ADD_QUALIFIER");
	    ActionString asDelete = new ActionString("MNU_DELETE_QUALIFIER");
	    menuItem = popupMenu.add(new JMenuItem(asAdd.getString()));
	    menuItem.setActionCommand("ADD_QUALIFIER");
	    menuItem.addActionListener(this);
	    menuItem = popupMenu.add(new JMenuItem(asDelete.getString()));
	    menuItem.setActionCommand("DELETE_QUALIFIER");
	    menuItem.addActionListener(this);
	} else {
	    ActionString asShow = new ActionString("MNU_SHOW_VALUE");
	    menuItem = popupMenu.add(new JMenuItem(asShow.getString()));
	    menuItem.setActionCommand("SHOW_VALUE");
	    menuItem.addActionListener(this);
	}
    }

    public void actionPerformed(ActionEvent evt) {
	super.actionPerformed(evt);
	String actionCmd = evt.getActionCommand();
	if (actionCmd.equals("ADD_QUALIFIER")) {
	    try {
		Enumeration e = cimClient.enumQualifierTypes(
		    new CIMObjectPath(""));
		Vector qTypes = new Vector();
		while (e.hasMoreElements()) {
		    CIMQualifierType cq = (CIMQualifierType)e.nextElement();
		    qTypes.addElement(cq.getName());
		}
		Util.sortVector(qTypes);
		AddQualifierDialog dlg;
		String name;
		boolean done;
		do {
		    done = true;
		    dlg = new AddQualifierDialog(Util.getFrame(this), qTypes);
		    name = dlg.getName();
		    if (isNameDefined(name)) {
			JOptionPane.showMessageDialog(this, 
			    I18N.loadStringFormat("ERR_DUP_QUALIFIER", 
			    name), I18N.loadString("TTL_CIM_ERROR"), 
			    JOptionPane.ERROR_MESSAGE);
			done = false;
		    }
		} while (!done);
		if (name != null) {
		    // user did no click cancel in dialog
		    CIMQualifierType cqt = cimClient.getQualifierType(
			new CIMObjectPath(name));
		    CIMQualifier qualifier = new CIMQualifier(name, cqt);
		    cimElements.addElement(qualifier);
		    elementsDataModel.setNumRows(cimElements.size());
		}
	    } catch (CIMException e) {
		CIMErrorDialog.display(this, e);
	    }
	} else if (actionCmd.equals("DELETE_QUALIFIER")) {
	    cimElements.removeElementAt(getSelectedRow());
	    elementsDataModel.setNumRows(cimElements.size());
	} else if (actionCmd.equals("SCOPE")) {
	    CIMQualifier cq = (CIMQualifier)cimElements.elementAt(
			      getSelectedRow());
	    CIMObjectPath op = new CIMObjectPath(cq.getName());
	    try {
		CIMQualifierType cqt = cimClient.getQualifierType(op);
		Vector scope = cqt.getScope();
		CIMScopeDialog dlg = new CIMScopeDialog(Util.getFrame(this),
							scope, cq.getName(), 
							false);
	    } catch (CIMException exc) {
		CIMErrorDialog.display(this, exc);
	    }
	} else if (actionCmd.equals("FLAVOR")) {
	    CIMQualifier cq = (CIMQualifier)cimElements.elementAt(
			      getSelectedRow());
	    CIMObjectPath op = new CIMObjectPath(cq.getName());
	    try {
		CIMQualifierType cqt = cimClient.getQualifierType(op);
		Vector flavor = cqt.getFlavor();
		CIMFlavorDialog dlg = new CIMFlavorDialog(Util.getFrame(this),
							  flavor, 
							  cq.getName(), 
							  false);
	    } catch (CIMException exc) {
		CIMErrorDialog.display(this, exc);
	    }
	    
	}
    }
	    
    public void populateTable(CIMClient cc, CIMElement pElement) {
	Util.setWaitCursor(this);

	cimClient = cc;
	parentElement = pElement;

	if (pElement == null) {
	    cimElements = null;
	} else {
	    if (pElement instanceof CIMInstance) {
		cimElements = ((CIMInstance)parentElement).getQualifiers();
	    } else if (pElement instanceof CIMClass) {
		cimElements = ((CIMClass)parentElement).getQualifiers();
	    } else if (pElement instanceof CIMProperty) {
		cimElements = ((CIMProperty)parentElement).getQualifiers();
	    } else if (pElement instanceof CIMMethod) {
		cimElements = ((CIMMethod)parentElement).getQualifiers();
	    }
	}

	if (elementsDataModel != null && cimElements != null) {
	    elementsDataModel.setNumRows(cimElements.size());
	    table.repaint();
	} else {
	    elementsDataModel.setNumRows(0);
	    table.repaint();
	}

	Util.setDefaultCursor(this);
    }


    public Vector getQualifiers() {
	return cimElements;
    }

    public void tableChanged(TableModelEvent e) {
    }


}
