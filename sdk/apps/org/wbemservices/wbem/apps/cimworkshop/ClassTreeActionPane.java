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

import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.tree.*;
import javax.wbem.cim.*;

import org.wbemservices.wbem.apps.common.*;

/**
 * 
 *
 * @author 	Sun Microsystems
 */

public class ClassTreeActionPane extends ClassTreePane {
  
    private CIMTableTabbedPane tablePane;
    private ObjectTreeFrame parentFrame;
    private CWSLoginDialog loginDialog;
    private Vector instanceFrameList;
    private JMenuItem instanceMenuItem;
    private JMenuItem addClassMenuItem;
    private JMenuItem rootAddClassMenuItem;
    private JMenuItem addQualifierType;

    public ClassTreeActionPane(ObjectTreeFrame parent, CIMTableTabbedPane tp) {
	super();
	instanceFrameList = new Vector();
	parentFrame = parent;
	tablePane = tp;
	JMenuItem popupMenuItem;
	ActionString asAdd = new ActionString("MNU_ADD_CLASS");
	ActionString asInstances = new ActionString("MNU_INSTANCES");
	ActionString asQualifiers = new ActionString("MNU_QUALIFIERS");
	ActionString asRefresh = new ActionString("MNU_REFRESH_CLASS");
	ActionString asQualifierType = new ActionString(
	    "MNU_ADD_QUALIFIER_TYPE");
	addClassMenuItem = popupMenu.add(new JMenuItem(asAdd.getString()));
	addClassMenuItem.setActionCommand("ADD_CLASS");
	addClassMenuItem.addActionListener(this);
	instanceMenuItem = popupMenu.add(new JMenuItem(
					 asInstances.getString()));
	instanceMenuItem.setActionCommand("INSTANCES");
	instanceMenuItem.addActionListener(this);

	popupMenuItem = popupMenu.add(new JMenuItem(asQualifiers.getString()));
	popupMenuItem.setActionCommand("QUALIFIERS");
	popupMenuItem.addActionListener(this);
	popupMenu.addSeparator();
	popupMenuItem = popupMenu.add(new JMenuItem(asRefresh.getString()));
	popupMenuItem.setActionCommand("REFRESH");
	popupMenuItem.addActionListener(this);

	rootAddClassMenuItem = rootPopupMenu.add(new JMenuItem(
						 asAdd.getString()));
	rootAddClassMenuItem.setActionCommand("ADD_CLASS");
	rootAddClassMenuItem.addActionListener(this);

	addQualifierType = rootPopupMenu.add(new JMenuItem(
					     asQualifierType.getString()));
	addQualifierType.setActionCommand("ADD_QUALIFIER_TYPE");
	addQualifierType.addActionListener(this);

	rootPopupMenu.addSeparator();

	popupMenuItem = rootPopupMenu.add(new JMenuItem(asRefresh.getString()));
	popupMenuItem.setActionCommand("REFRESH"); 
	popupMenuItem.addActionListener(this);
	
    }
   
    /**
     * enables or disabled instance menu item
     */
    public void enableInstanceMenuItem(boolean b) {
	instanceMenuItem.setEnabled(b);
    }

    public void actionPerformed(ActionEvent e) {
	Util.setWaitCursor(this);
	String action = e.getActionCommand();

	if (action.equals("hostDialog")) {
	    getHostInfo();
	} else if (getSelectedNodeString() == null) {
	    JOptionPane.showMessageDialog(this, I18N.loadString(
					  "ERR_NO_CLASS_SELECTED"), 
					  I18N.loadString("TTL_CIM_ERROR"), 
					  JOptionPane.ERROR_MESSAGE);
	} else if (action.equals("INSTANCES")) {
	    String classNameString = getSelectedNodeString();		
	    CIMObjectPath op = new CIMObjectPath(classNameString);
	    try {
    		CIMClass cimClass = cimClient.getClass(op, false, true, 
						       true, null);
		if (!cimClass.isKeyed()) {
		    JOptionPane.showMessageDialog(this, I18N.loadStringFormat(
			"MSG_NOT_KEYED_CLASS", classNameString), 
			I18N.loadString("TTL_DLG_CIM_WORKSHOP"), 
			JOptionPane.INFORMATION_MESSAGE);
		} else {
		    // always do deep enumeration
		    InstanceFrame instanceFrame = new InstanceFrame(
			parentFrame, cimClient, cimClass, true);
		    instanceFrameList.addElement(instanceFrame);
		}
	    } catch (CIMException exc) {
		CIMErrorDialog.display(this, exc);
	    }
	} else if (action.equals("QUALIFIERS")) {
	    String classNameString = getSelectedNodeString();
	    CIMObjectPath op = new CIMObjectPath(classNameString);
	    try {
    		CIMClass cimClass = cimClient.getClass(op, false, true, 
						       true, null);
			
		if (cimClass.getQualifiers().size() > 0) {
		    QualifierWindow qualifierWindow = new QualifierWindow(
			parentFrame, cimClient, cimClass, false);
		} else {
		    JOptionPane.showMessageDialog(this, I18N.loadStringFormat(
			"ERR_NO_QUALIFIERS", classNameString), 
			I18N.loadString("TTL_CIM_ERROR"), 
			JOptionPane.ERROR_MESSAGE);
		}
	    } catch (CIMException exc) {
		CIMErrorDialog.display(this, exc);
	    }
	} else if (action.equals("ASSOC_TRAVERSAL")) {
	    String classNameString = getSelectedNodeString();
	    CIMObjectPath op = new CIMObjectPath(classNameString);
	    ReferenceTraversalDialog refDialog = new 
		ReferenceTraversalDialog(parentFrame, op, cimClient);
	    
	} else if (action.equals("ADD_CLASS")) {
	    Util.setWaitCursor(this);
	    String currentClass = getSelectedNodeString();
	    if (isRootSelected()) {
		currentClass = "";
	    }
	    AddClassDialog dlg = new AddClassDialog(
		parentFrame, currentClass);
	    String className = dlg.getClassName();
	    if (className != null) {
		ClassTreeNode newNode = 
		    new ClassTreeNode(new CIMObjectPath(className));
		addNodeToSelected(newNode);
		TreePath path = new TreePath(newNode.getPath());
		tree.expandPath(path);
		tree.setSelectionPath(path);
	    }
	} else if (action.equals("DELETE_CLASS")) {
	    String classNameString = getSelectedNodeString();
	    int option = JOptionPane.showConfirmDialog(this,  
		    I18N.loadStringFormat("ASK_DELETE_CLASS", 
		    classNameString), I18N.loadString("TTL_DLG_CIM_WORKSHOP"),
		    JOptionPane.OK_CANCEL_OPTION, 
		    JOptionPane.QUESTION_MESSAGE);
	    if (option == JOptionPane.YES_OPTION) {
		try {
		    cimClient.deleteClass(new CIMObjectPath(classNameString));
		    deleteSelectedNode();
		} catch (CIMException exc) {
		    CIMErrorDialog.display(this, exc);
		}
	    }
	} else if (action.equals("ADD_QUALIFIER_TYPE")) {
	    AddQualifierTypeDialog aqtDialog = new AddQualifierTypeDialog(
	        parentFrame);
	} else if (action.equals("EXEC_QUERY")) {
	    QueryInputDialog queryInputDialog = new QueryInputDialog(
                parentFrame);
	    String queryString = queryInputDialog.getQueryString();
	    if (queryString != null) {
		QueryFrame queryFrame = new QueryFrame(parentFrame, cimClient,
						       queryString);
		instanceFrameList.add(queryFrame);
	    } 
	} else {
	    super.actionPerformed(e);
	} 
	Util.setDefaultCursor(this);
    }
   

    /**
     * Shows login dialog and gets info from it.
     */
    public boolean getHostInfo() {
	loginDialog = new CWSLoginDialog(parentFrame);
	boolean loggedOn = loginDialog.getLoggedIn();
	if (loggedOn) {
	    // enable/disable menu items depending on user's permissions
	    boolean userCanWrite = CIMClientObject.userHasWritePermission();
	    addClassMenuItem.setEnabled(userCanWrite);
	    rootAddClassMenuItem.setEnabled(userCanWrite);
	    addQualifierType.setEnabled(userCanWrite);
	    parentFrame.initializeNewHost();
	}
	return loggedOn;
    }

    /**
     * gets called when new CIMClient object is initialized.  
     */
    public void initializeClassTree() {
	WaitDialog waitDialog = new WaitDialog(parentFrame,
	    I18N.loadString("LBL_WAIT", "org.wbemservices.wbem.apps.common.common"),
	    I18N.loadString("MSG_ENUM_CLASSES"));
	waitDialog.setLocation(Util.getCenterPoint(parentFrame,
 			       waitDialog));
	waitDialog.start();
	refreshTree();
	waitDialog.stop();
	
	closeOpenInstanceFrames();
    }


    /**
     * closes all open instances frames.  This gets called when user changes
     * host and/or namespace
     */
    public void closeOpenInstanceFrames() {
	Enumeration e;
	for (e = instanceFrameList.elements(); e.hasMoreElements(); ) {
	    JFrame frame = (JFrame)e.nextElement();
	    frame.dispose();
	}
	instanceFrameList.removeAllElements();
    }


    protected void populateTables(CIMClass cimClass) {
	tablePane.populateTables(cimClient, cimClass);
    }


}
