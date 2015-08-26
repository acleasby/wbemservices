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

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelListener;
import javax.wbem.cim.*;
import javax.wbem.client.CIMClient;

import org.wbemservices.wbem.apps.common.ActionString;
import org.wbemservices.wbem.apps.common.I18N;
import org.wbemservices.wbem.apps.common.Util;


/**
 * 
 *
 * @author 	Sun Microsystems
 */

public class CIMPropertiesPanel extends CIMElementsTable {

    JMenuItem mnuDeleteProp;

    public CIMPropertiesPanel() {
	this(false);
    }

    public CIMPropertiesPanel(boolean nElement) {
	super(PROPERTY_TABLE, nElement);
	JMenuItem menuItem;
	if (nElement) {
	    ActionString asAdd = new ActionString("MNU_ADD_PROPERTY");
	    ActionString asDelete = new ActionString("MNU_DELETE_PROPERTY");
	    menuItem = popupMenu.add(new JMenuItem(asAdd.getString()));
	    menuItem.setActionCommand("ADD_PROPERTY");
	    menuItem.addActionListener(this);
	    mnuDeleteProp = popupMenu.add(new JMenuItem(asDelete.getString()));
	    mnuDeleteProp.setActionCommand("DELETE_PROPERTY");
	    mnuDeleteProp.addActionListener(this);
	} else {
	    ActionString asQualifiers = new ActionString("LBL_QUALIFIERS");
	    ActionString asShow = new ActionString("MNU_SHOW_VALUE");
	    menuItem = popupMenu.add(new JMenuItem(asQualifiers.getString()));
	    menuItem.setActionCommand("QUALIFIERS");
	    menuItem.addActionListener(this);
	    menuItem = popupMenu.add(new JMenuItem(asShow.getString()));
	    menuItem.setActionCommand("SHOW_VALUE");
	    menuItem.addActionListener(this);
	    table.addMouseListener(this);
	}

    }


    public void populateTable(CIMClient cc, CIMElement pElement) {
	cimClient = cc;
	parentElement = pElement;
	Util.setWaitCursor(this);


	if (pElement == null) {
	    cimElements = new Vector();
	} else {
	    if (parentElement instanceof CIMInstance) {
		setAccessState(ONLY_VALUE_EDITABLE);
		cimElements = ((CIMInstance)parentElement).getProperties();
	    } else if (parentElement instanceof CIMClass) {
		cimElements = ((CIMClass)parentElement).getProperties();
		isClass = true;
	    }
	}

	if (elementsDataModel != null && cimElements != null) {
	    elementsDataModel.setNumRows(cimElements.size());
	    table.repaint();
// BUGFIX. Accessibility fixes
	    if (cimElements.size() > 0) {
		table.setRowSelectionInterval(0, 0);
	    }
	} else {
	    elementsDataModel.setNumRows(0);
	    table.repaint();
	}

	Util.setDefaultCursor(this);
    }

    public void actionPerformed(ActionEvent evt) {
	Util.setWaitCursor(this);
	super.actionPerformed(evt);
	String actionCmd = evt.getActionCommand();
	if (actionCmd.equals("QUALIFIERS")) {
// BUGFIX. Accessibility fixes
	    showQualifiers();
	} else if (actionCmd.equals("ADD_PROPERTY")) {
	    boolean done;
	    String name;
	    AddPropertyDialog dlg;
	    do {
		done = true;
		dlg = new AddPropertyDialog(Util.getFrame(this));
		name = dlg.getName();
		if (isNameDefined(name)) {
		    JOptionPane.showMessageDialog(this, 
			I18N.loadStringFormat("ERR_DUP_PROPERTY", 
			name), I18N.loadString("TTL_CIM_ERROR"), 
			JOptionPane.ERROR_MESSAGE);
		    done = false;
		}
	    } while (!done);

	    if (name != null) {
		String value = dlg.getSelectedType();
		CIMProperty prop = new CIMProperty(name);
		CIMDataType cdt;
		if ((value).indexOf('_') > 0) {
		    cdt = new CIMDataType(value);
		} else {
		    cdt = new CIMDataType(cimTypes.getCIMType(value));
		}
		prop.setType(cdt);

		cimElements.addElement(prop);
		elementsDataModel.setNumRows(cimElements.size());
	    }
	} else if (actionCmd.equals("DELETE_PROPERTY")) {
	    cimElements.removeElementAt(getSelectedRow());
	    elementsDataModel.setNumRows(cimElements.size());
	}
	Util.setDefaultCursor(this);
    }

// BUGFIX. Accessibility fixes
    public void showQualifiers() {
	CIMProperty property = (CIMProperty)cimElements.elementAt(
			       getSelectedRow());
	// if property is set to be uneditable, qualifier is uneditable
	boolean b = newElement;
	if (uneditableRows.contains(new Integer(getSelectedRow()))) {
	    b = false;
	}
	QualifierWindow qualifierWindow = new QualifierWindow(
	    Util.getFrame(this), cimClient, property, b);
    }


    public void valueChanged(ListSelectionEvent evt) {
	// only want to process this event when ValueIsAdjusting is false
	if (evt.getValueIsAdjusting()) {
	    return;
	}

	// disable row if uneditable (new class but inherited prop
	if (mnuDeleteProp != null) {
	    boolean b = true;
	    if (uneditableRows.contains(new Integer(getSelectedRow()))) {
		b = false;
	    }
	    mnuDeleteProp.setEnabled(b);
	}
    }

    protected void createPopupMenu(Point point) {
	int currentRow = table.rowAtPoint(point);
	CIMProperty prop = (CIMProperty)cimElements.elementAt(currentRow);
	table.setRowSelectionInterval(currentRow, currentRow);
	Point vpLocation = getViewport().getViewPosition();
	popupMenu.show(this, (point.x - vpLocation.x + 10), 
			     (point.y - vpLocation.y));
    }


    public void addTableModelListener(TableModelListener tml) {
	elementsDataModel.addTableModelListener(tml);
    }

    public Vector getProperties() {
	return cimElements;
    }

}
