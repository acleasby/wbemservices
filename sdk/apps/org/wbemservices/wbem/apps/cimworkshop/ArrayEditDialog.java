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


import org.wbemservices.wbem.apps.common.ActionString;
import org.wbemservices.wbem.apps.common.CancelObject;
import org.wbemservices.wbem.apps.common.ColumnLayout;
import org.wbemservices.wbem.apps.common.Util;

import javax.wbem.cim.CIMDataType;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

/**
 * 
 *
 * @version 	%I%, %G%
 * @author 	Sun Microsystems
 */
class ArrayEditDialog extends CIMEditDialog implements MouseListener,
    ListSelectionListener, ActionListener {

    protected JList list;
    protected JButton btnAdd;
    protected JButton btnDelete;
    protected JButton btnModify;
    protected JPopupMenu popup;
    protected int currentRow;
    protected Frame parentFrame;
    protected Vector cimValues;
    protected CIMTypes cimTypes;

    public ArrayEditDialog(Frame frame, String name, String type, 
			   Vector values) {
	this(frame, name, type, values, true);
    }

    public ArrayEditDialog(Frame frame, String name, String type, 
			   Vector values, boolean editable) {


	super(frame, name, type, editable);
	
	cimTypes = new CIMTypes();
	int currentType = cimTypes.getCIMType(dataType);
	currentType -= 14;
	dataType = cimTypes.getCIMType(currentType);

	cimValues = values;
	if (cimValues == null) {
	    cimValues = new Vector();
	}
	parentFrame = frame;
	list = new JList(cimValues);
	list.setVisibleRowCount(7);

	JScrollPane scrollPane = new JScrollPane(list) {
	    public Dimension getPreferredSize() {
		return new Dimension(100, 125);
	    }
	};

	JPanel sideButtons = new JPanel(new ColumnLayout());	
	String defaultHelp = "ShowValue_000.htm";
	if (isEditable) {
	    list.addMouseListener(this);
	    list.addListSelectionListener(this);
	    
	    ActionString asAdd = new ActionString("MNU_ADD",
	        "org.wbemservices.wbem.apps.common.common");
	    ActionString asDelete = new ActionString("MNU_DELETE",
	        "org.wbemservices.wbem.apps.common.common");
	    ActionString asModify = new ActionString("MNU_MODIFY",
	        "org.wbemservices.wbem.apps.common.common");

	    btnAdd = new JButton(asAdd.getString());
	    btnAdd.setMnemonic(asAdd.getMnemonic());
	    btnAdd.addActionListener(this);
	    btnAdd.setActionCommand("add");

	    btnDelete = new JButton(asDelete.getString());
	    btnDelete.setMnemonic(asDelete.getMnemonic());
	    btnDelete.addActionListener(this);
	    btnDelete.setActionCommand("delete");

	    btnModify = new JButton(asModify.getString());
	    btnModify.setMnemonic(asModify.getMnemonic());
	    btnModify.addActionListener(this);
	    btnModify.setActionCommand("modify");


	    sideButtons.setBorder(BorderFactory.createEmptyBorder(50, 5, 5, 5));

	    sideButtons.add(btnAdd);
	    sideButtons.add(btnDelete);
	    sideButtons.add(btnModify);
	    btnDelete.setEnabled(false);
	    btnModify.setEnabled(false);
	    defaultHelp = "Array_000.htm";

	    JMenuItem menuItem;

	    popup = new JPopupMenu();

	    menuItem = popup.add(new JMenuItem(asAdd.getString()));
	    menuItem.setMnemonic(asAdd.getMnemonic());
	    menuItem.setActionCommand("add");
	    menuItem.addActionListener(this);

	    menuItem = popup.add(new JMenuItem(asDelete.getString()));
	    menuItem.setMnemonic(asDelete.getMnemonic());
	    menuItem.setActionCommand("delete");
	    menuItem.addActionListener(this);
	    menuItem = popup.add(new JMenuItem(asModify.getString()));
	    menuItem.setMnemonic(asModify.getMnemonic());
	    menuItem.setActionCommand("modify");
	    menuItem.addActionListener(this);

	} else {
	    list.setEnabled(false);
	}
	
	JPanel listPanel = new JPanel(new ColumnLayout());
	
	ActionString asValue = new ActionString("LBL_VALUE");
	JLabel valueLabel = new JLabel(asValue.getString() + ":");
	valueLabel.setDisplayedMnemonic(asValue.getMnemonic());
	valueLabel.setLabelFor(list);

	listPanel.add(valueLabel);
	listPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
	listPanel.add(scrollPane);

	JPanel basePanel = new JPanel(new BorderLayout());

	basePanel.add("West", listPanel);
	basePanel.add("East", sideButtons);
	JPanel mainPanel = this.getMainPanel();
	mainPanel.add(basePanel);
	
	setDefaultHelp(defaultHelp);

	if ((cimValues.size() > 0) && editable) {
	    list.setSelectedIndex(0);
	}
	    
	pack();
	setLocation(Util.getCenterPoint(frame, this));
	setVisible(true);
    }

    public void actionPerformed(ActionEvent evt) {
	String actionCmd = evt.getActionCommand();
	if (!isEditable) {
	    return;
	} else if (actionCmd.equals("add")) {
	    showDialog(null);
	    list.setSelectedIndex(cimValues.size() - 1);
	} else if (actionCmd.equals("modify")) {
	    Object o = cimValues.elementAt(list.getSelectedIndex());
	    showDialog(o);
	} else if (actionCmd.equals("delete")) {
	    cimValues.removeElementAt(list.getSelectedIndex());
	    list.setListData(cimValues);
	    if (cimValues.size() > 0) {
		list.setSelectedIndex(0);
	    } else {
		btnDelete.setEnabled(false);
		btnModify.setEnabled(false);
	    }
	}
    }

    protected void showPopupMenu(Point point) {
	if (!isEditable) {
	    return;
	}
	currentRow = list.locationToIndex(point);
	list.getSelectionModel().setSelectionInterval(currentRow, currentRow);
	popup.show(this, point.x, point.y);
    }


    public void mousePressed(MouseEvent evt) {
	if (evt.isPopupTrigger()) {
	    showPopupMenu(evt.getPoint());
	}
    }

    public void mouseReleased(MouseEvent evt) {
	if (evt.isPopupTrigger()) {
	    showPopupMenu(evt.getPoint());
	}
    }

    public void mouseClicked(MouseEvent evt) {
	if (evt.isPopupTrigger()) {
	    showPopupMenu(evt.getPoint());
	}
    }

    public void mouseEntered(MouseEvent evt) {
    }


    public void mouseExited(MouseEvent evt) {
    }

    public void showDialog(Object value) {
	CIMEditDialog editDialog;
	String valueString = "";
	if (value != null) {
	    valueString = value.toString();
	}

	int currentType = cimTypes.getCIMType(dataType);

	switch (currentType) {
	    case CIMDataType.BOOLEAN:
		editDialog = new TrueFalseDialog(parentFrame, nameString,
		    dataType, (Boolean)value, isEditable);
		break;
	    case CIMDataType.STRING:
		editDialog = new TextFieldDialog(parentFrame, nameString, 
		    dataType, valueString, isEditable);
		break;
	    case CIMDataType.UINT8:
	    case CIMDataType.UINT16:
	    case CIMDataType.UINT32:
	    case CIMDataType.UINT64:
	    case CIMDataType.SINT8:
	    case CIMDataType.SINT16:
	    case CIMDataType.SINT32:
	    case CIMDataType.SINT64:
		editDialog = new IntegerFieldDialog(parentFrame, nameString, 
		    dataType, valueString, isEditable);
		break;
	    case CIMDataType.REAL32:
	    case CIMDataType.REAL64:
		editDialog = new RealNumberFieldDialog(parentFrame, nameString, 
		    dataType, valueString, isEditable);
		break;
	    case CIMDataType.DATETIME:
		editDialog = new DateTimeDialog(parentFrame, nameString, 
		    dataType, valueString, isEditable);
		break;
	    case CIMDataType.CHAR16:
		editDialog = new CharFieldDialog(parentFrame, nameString, 
		    dataType, valueString, isEditable);
		break;
	    default:
		editDialog = new TextFieldDialog(parentFrame, nameString, 
		    dataType, valueString, isEditable);
		break;
	}
    
	Object currentValue = editDialog.getValueObject();
	if ((currentValue instanceof CancelObject) || (currentValue == null)) {
	    return;
	} else {
	    if (value == null) {
		cimValues.addElement(currentValue);
	    } else {
		cimValues.setElementAt(currentValue, list.getSelectedIndex());
	    }
	    list.setListData(cimValues);    
	}
    }

    public void okClicked() { 
	if (cimValues.size() == 0) {
	    returnObject =  null;
	} else {
	    returnObject = cimValues;
	}
	dispose();
    }

    public void valueChanged(ListSelectionEvent evt) {
	// only want to process this event when ValueIsAdjusting is false
	if (evt.getValueIsAdjusting()) {
	    return;
	}
	boolean b = (list.getSelectedIndex() >= 0);
	btnDelete.setEnabled(b);
	btnModify.setEnabled(b);
    }

    public Vector getValue() {
	return cimValues;
    }
}
