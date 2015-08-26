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
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Enumeration;

import javax.swing.JOptionPane;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.*;
import javax.wbem.cim.CIMClass;
import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMInstance;
import javax.wbem.cim.CIMObjectPath;

import org.wbemservices.wbem.apps.common.ActionString;
import org.wbemservices.wbem.apps.common.CIMClientObject;
import org.wbemservices.wbem.apps.common.CIMErrorDialog;
import org.wbemservices.wbem.apps.common.I18N;
import org.wbemservices.wbem.apps.common.Util;

/**
 * 
 *
 * @author 	Sun Microsystems
 */

public class InstanceActionPanel extends InstancePanel implements MouseListener,
    ListSelectionListener, ActionListener, ListDataListener, 
    TableModelListener {

    private CIMTableTabbedPane tablePane;
    private JPopupMenu popupMenu;
    private JMenuItem mnuDeleteInstance;
    private InstanceFrame instanceFrame;
    private boolean propertyChanged;
    private boolean writePermission = false;
    private int prevInstanceIndex = -1;

    public InstanceActionPanel(CIMTableTabbedPane tp, InstanceFrame iFrame, 
			 CIMClass cClass) {
	this(tp, iFrame, cClass, false);
    }

    public InstanceActionPanel(CIMTableTabbedPane tp, InstanceFrame iFrame, 
			 CIMClass cClass, boolean deepEnum) {
	super(cClass, deepEnum);
	tablePane = tp;
	instanceFrame = iFrame;
	tablePane.addPropertyTableModelListener(this);
	writePermission = CIMClientObject.userHasWritePermission();
	// create popup menu and menu items;
	popupMenu = new JPopupMenu();
	JMenuItem popupMenuItem;

	ActionString asRefresh = new ActionString("MNU_REFRESH",
	    "org.wbemservices.wbem.apps.common.common");
	ActionString asAdd = new ActionString("MNU_ADD_INSTANCE");
	ActionString asDelete = new ActionString("MNU_DELETE_INSTANCE");
	popupMenuItem = popupMenu.add(new JMenuItem(asRefresh.getString()));
	popupMenuItem.setActionCommand("REFRESH");
	popupMenuItem.addActionListener(this);
	popupMenuItem = popupMenu.add(new JMenuItem(asAdd.getString()));
	popupMenuItem.setActionCommand("ADD_INSTANCE");
	popupMenuItem.addActionListener(this);
	popupMenuItem.setEnabled(writePermission);
	mnuDeleteInstance = popupMenu.add(new JMenuItem(asDelete.getString()));
	mnuDeleteInstance.addActionListener(this);
	mnuDeleteInstance.setActionCommand("DELETE_INSTANCE");
	mnuDeleteInstance.setEnabled(false);

	listModel.addListDataListener(this);
	instanceList.addMouseListener(this);
	instanceList.getSelectionModel().addListSelectionListener(this);

	setVisible(true);

    }

    public void actionPerformed(ActionEvent e) {
	String actionCmd = e.getActionCommand();
	if (actionCmd.equals("ADD_INSTANCE")) {
	    CreateInstanceDialog dlg = new CreateInstanceDialog(
		Util.getFrame(this), cimClient, cimClass);
	    CIMInstance newInstance = dlg.getInstance();
	    if (newInstance != null) {
		try {
		    CIMObjectPath newObjectPath = newInstance.getObjectPath();
		    cimClient.createInstance(newObjectPath, newInstance);
		    refreshInstanceList();
		    instanceList.setSelectedIndex(listModel.size() - 1);
		} catch (CIMException exc) {
		    CIMErrorDialog.display(this, exc);
		}
	    }
	} else if (actionCmd.equals("DELETE_INSTANCE")) {
	    if (propertyChanged) {
		saveCurrentInstance(true);
	    }
	    int i = instanceList.getSelectedIndex();
	    if (i >= 0) {
		int option = JOptionPane.showConfirmDialog(this,  
			I18N.loadStringFormat("ASK_DELETE_INSTANCE", 
			listModel.getElementAt(i)), 
			I18N.loadString("TTL_DLG_CIM_WORKSHOP"),
			JOptionPane.OK_CANCEL_OPTION, 
			JOptionPane.QUESTION_MESSAGE);

		if (option == JOptionPane.YES_OPTION) {
		    try {
			cimClient.deleteInstance((CIMObjectPath)
			    listModel.elementAt(i));
			listModel.removeElementAt(i);
		    } catch (CIMException exc) {
			CIMErrorDialog.display(this, exc);
		    }
		}
	    }	    
	} else if (actionCmd.equals("ASSOC_TRAVERSAL")) {
	    if (propertyChanged) {
		saveCurrentInstance(true);
	    }
	    int i = instanceList.getSelectedIndex();
	    if (i >= 0) {
		CIMObjectPath op = (CIMObjectPath) listModel.elementAt(i);
		ReferenceTraversalDialog refDialog = new 
		    ReferenceTraversalDialog(instanceFrame, op, cimClient);
	    }
	    
	} else if (actionCmd.equals("SAVE_INSTANCE")) {
	    saveCurrentInstance(false);
	} else {
	    super.actionPerformed(e);
	}
    }


    /**
     * saveCurrentInstance 
     * Saves the currently selected instance.
     * @param askToSave true if you want to show a confirmation dialog before 
     *                  saving, otherwise false.
     *
     */
    public void saveCurrentInstance(boolean askToSave) {
	saveInstance(instanceList.getSelectedIndex(), askToSave);
    }

    
    /**
     * saveInstance 
     * Saves the instance at a particular index
     * @param index     index of the instance to save
     * @param askToSave true if you want to show a confirmation dialog before 
     *                  saving, otherwise false.
     *
     */
    public void saveInstance(int index, boolean askToSave) {
	int option = JOptionPane.YES_OPTION;
	if (index >= 0) {
	    CIMObjectPath instanceOP = (CIMObjectPath)listModel.elementAt(index);
	    if (askToSave) {
		option = JOptionPane.showConfirmDialog(this,  
		    I18N.loadStringFormat("ASK_SAVE_INSTANCE_PROPERTIES", 
		    instanceOP.toString()), 
		    I18N.loadString("TTL_DLG_CIM_WORKSHOP"),
		    JOptionPane.OK_CANCEL_OPTION, 
		    JOptionPane.QUESTION_MESSAGE);
	    }
	    if (option == JOptionPane.YES_OPTION) {
		try {
		    CIMInstance ci = cimClient.getInstance(instanceOP, false, 
							   true, true, null);
		    ci.updatePropertyValues(tablePane.getProperties());
		    cimClient.setInstance(instanceOP, ci);
		} catch (CIMException exc) {
		    CIMErrorDialog.display(this, exc);
		}
	    }
	}
	instanceFrame.enableSaveInstanceMenu(false);
	propertyChanged = false;
    }

    public void valueChanged(ListSelectionEvent evt) {
	// only want to process this event when ValueIsAdjusting is false
	if (evt.getValueIsAdjusting()) {
	    return;
	}
	if (propertyChanged) {
	    saveInstance(prevInstanceIndex, true);
	}
	int i = instanceList.getSelectedIndex();
	// save currently selected index as prev index
	prevInstanceIndex = i;
	Util.setWaitCursor(this);
	if (!listModel.isEmpty() && (i >= 0) && 
		    (i < listModel.size())) {
	    try {
		CIMObjectPath op = (CIMObjectPath) listModel.elementAt(i);
		CIMInstance ci = cimClient.getInstance(op, false, true, 
						       true, null);
		tablePane.populateTables(cimClient, ci);
		boolean bAssocMenu = false;
		try {
		    Enumeration e = cimClient.referenceNames(op, "", "");
		    bAssocMenu = (e != null);
		} catch (CIMException e) {
		    // if error is NOT_ASSOCIATOR_PROVIDER, we assume
		    // it does have references and enable the "Traverse
		    // Associations" menu
		    if (e.getID().equals("NOT_ASSOCIATOR_PROVIDER")) {
			bAssocMenu = true;
		    }
		}
		instanceFrame.enableAssocMenu(bAssocMenu);
	    } catch (CIMException exc) {
		tablePane.populateTables(null, null);
		CIMErrorDialog.display(this, exc);
	    }
	} else {
	    tablePane.populateTables(null, null);
	}
 	Util.setDefaultCursor(this);
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

    public void refreshInstanceList() {
	if (propertyChanged) {
	    saveCurrentInstance(true);
	}
	super.refreshInstanceList();

// BUGFIX. Accessibility fixes
	boolean b = (listModel.size() > 0);
	instanceFrame.enableAssocMenu(b);
	instanceFrame.enablePropMenu(b);
	
	// delete instance menus enabled only if there are instances and user 
	// has write permissions 
	b = b && writePermission;
	instanceFrame.enableDeleteInstanceMenu(b);
	mnuDeleteInstance.setEnabled(b);
//
    }

    public void mouseExited(MouseEvent evt) {
    }

    protected void showPopupMenu(Point point) {
	int index = instanceList.locationToIndex(point);
	// if whitespace, don't show popup
	if (index == -1) {
	    return;
	}
	instanceList.setSelectedIndex(index);
	Point vpLocation = getViewport().getViewPosition();
	popupMenu.show(this, (point.x - vpLocation.x + 10), 
			     (point.y - vpLocation.y));
    }

    public void intervalAdded(ListDataEvent e) {
	mnuDeleteInstance.setEnabled(writePermission);
	instanceFrame.enableDeleteInstanceMenu(writePermission);
// BUGFIX. Accessibility fixes
	instanceFrame.enablePropMenu(true);
    }

    public void intervalRemoved(ListDataEvent e) {
	setViewPort();
	if (listModel.size() <= 0) {
	    mnuDeleteInstance.setEnabled(false);
	    instanceFrame.enableDeleteInstanceMenu(false);
	    instanceFrame.enableAssocMenu(false);
// BUGFIX. Accessibility fixes
	    instanceFrame.enablePropMenu(false);
	    tablePane.populateTables(null, null);
	    return;
	}
	int index = e.getIndex0();
	if (index > 0) {
	    --index;
	}
	instanceList.setSelectedIndex(index);
    }

    public void contentsChanged(ListDataEvent e) {
    }

    public void tableChanged(TableModelEvent evt) {
	int evtType = evt.getType();
	if (evtType == TableModelEvent.UPDATE) {
	    instanceFrame.enableSaveInstanceMenu(writePermission);
	    propertyChanged = true;
	}
    }

}   
