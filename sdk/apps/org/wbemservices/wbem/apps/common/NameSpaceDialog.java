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
 *Contributor(s): WBEM Solutions, Inc.
*/

package org.wbemservices.wbem.apps.common;

import java.awt.Frame;
import java.awt.ItemSelectable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.EventListenerList;
import javax.wbem.cim.CIMClass;
import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMInstance;
import javax.wbem.cim.CIMObjectPath;
import javax.wbem.cim.CIMProperty;
import javax.wbem.cim.CIMValue;
import javax.wbem.client.CIMClient;

/**
 * 
 *
 * @version 	%I%, %G%
 * @author 	Sun Microsystems
 */

public class NameSpaceDialog extends AdminDialog implements 
    ItemSelectable {

    private JButton btnOK, btnCancel, btnAddNS, btnDeleteNS;
    //    private NameSpaceTreePane tree;
    private String selectedNameSpace;
    private EventListenerList listenerList;
    protected GenInfoPanel infoPanel;
    protected JList list;
    protected DefaultListModel listModel;
    protected static  CIMClient cimClient;
    protected AdminDialog thisDialog;
    protected CIMObjectPath currentNameSpace;
    protected JScrollPane scrollList;    
    protected int cellWidth = 5;

    private final static int NOT_FOUND = -1;
    private final static int CIM_NAMESPACE = 0 ;
    private final static int __NAMESPACE = 1;

    private static int foundClassName = NOT_FOUND;

 
    public NameSpaceDialog(Frame parent) {
	super(parent, I18N.loadString("TTL_CHANGE_NAMESPACE"), false);

	infoPanel = this.getInfoPanel();
	thisDialog = this;
	
	currentNameSpace = new CIMObjectPath("", CIMClientObject.getNameSpace());
	listenerList = new EventListenerList();

	btnOK = this.getOKBtn();
	btnOK.addActionListener(new OKCancelButtonListener());

	btnCancel = this.getCancelBtn();
	btnCancel.addActionListener(new OKCancelButtonListener());
	JPanel mainPanel = getRightPanel();	
	mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
	
	JPanel centerPanel = new JPanel(new ColumnLayout(
					LAYOUT_ALIGNMENT.EXPAND));
	centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

	listModel = new DefaultListModel();
	list = new JList(listModel);
	list.getSelectionModel().setSelectionMode(
	    ListSelectionModel.SINGLE_SELECTION);

	ActionString asNameSpace = new ActionString("LBL_NAMESPACE");
	JLabel lNameSpace = new JLabel(asNameSpace.getString());
	lNameSpace.setDisplayedMnemonic(asNameSpace.getMnemonic());
	lNameSpace.setLabelFor(list);
	scrollList = new JScrollPane(list);
	scrollList.setPreferredSize(scrollList.getPreferredSize());
	centerPanel.add(lNameSpace);
	centerPanel.add(scrollList);
	
	ActionString asAdd = new ActionString("LBL_ADD");
	ActionString asDelete = new ActionString("LBL_DELETE");

	ButtonPanel bPanel = new ButtonPanel();
	btnAddNS = new JButton(asAdd.getString());
	btnAddNS.setMnemonic(asAdd.getMnemonic());
	btnAddNS.addActionListener(new NSButtonListener());
	btnDeleteNS = new JButton(asDelete.getString());
	btnDeleteNS.setMnemonic(asDelete.getMnemonic());
	btnDeleteNS.addActionListener(new NSButtonListener());

	bPanel.add(btnAddNS);
	bPanel.add(btnDeleteNS);
	centerPanel.add(bPanel);
	
	mainPanel.add(centerPanel);

	this.setDefaultFocusListener(new ContextHelpListener(
		infoPanel, "cimadmin", "namsp_000.htm"), true);
	
	pack();
	Util.positionWindow(this, parent);
    }

    class OKCancelButtonListener implements ActionListener {
	public void actionPerformed(ActionEvent e) {
	    if (e.getSource() == btnCancel) { 
		cancelClicked();
	    } else if (e.getSource() == btnOK) { 
		okClicked();
	    }
	}
    }

    public void okClicked() {
	int i = list.getSelectedIndex();
	CIMObjectPath opNameSpace;
	if (i < 0) {
	    opNameSpace = currentNameSpace;
	} else {
	    opNameSpace = (CIMObjectPath)listModel.elementAt(
		list.getSelectedIndex());
	}
	selectedNameSpace = opNameSpace.getNameSpace();
	fireItemEvent(new ItemEvent(this, 0, "select name space", 
				    ItemEvent.ITEM_STATE_CHANGED));
    }
    
    public void cancelClicked() {
	selectedNameSpace = null;
	setVisible(false);
	fireItemEvent(new ItemEvent(this, 0, "dialog cancel", 
	    	  ItemEvent.ITEM_STATE_CHANGED));
    }

    public String getSelectedNameSpace() {
	return selectedNameSpace;
    }

    public Object[] getSelectedObjects() {
	String[] val;
	val = new String[1];
	return val;
    }

    public void addItemListener(ItemListener listener) {
	listenerList.add(ItemListener.class, listener);
    }

    public void removeItemListener(ItemListener listener) {
	listenerList.remove(ItemListener.class, listener);
    }

    protected void fireItemEvent(ItemEvent evt) {
	Object[] listeners = listenerList.getListenerList();

	for (int i = listeners.length-2; i >= 0; i -= 2) {
	    if (listeners[i] == ItemListener.class) {
		((ItemListener)listeners[i+1]).itemStateChanged(evt);
	    }
	}
    }

    public void windowOpened(WindowEvent evt) {
	super.windowOpened(evt);
	refreshList();
	list.setSelectedValue(currentNameSpace, true);
	// BUGFIX. Accessibility fixes
	list.requestFocus();
    }

    private void refreshList() {
	Util.setWaitCursor(this);
	listModel.removeAllElements();
	Enumeration e = getAllNameSpaces();
	while (e.hasMoreElements()) {
	    CIMObjectPath nextNS = (CIMObjectPath)e.nextElement();
	    cellWidth = Math.max(cellWidth, nextNS.toString().length());
	    listModel.addElement(nextNS);
	}
	btnDeleteNS.setEnabled(listModel.size() > 0);
	setCellWidth();
	Util.setDefaultCursor(this);
    }

    private void setCellWidth() {
	String proto = "";
	for (int i = 0; i < cellWidth; i++)
	    proto += "A";
	list.setPrototypeCellValue(proto);
    }
	
    private void setCellWidth(String s) {
	cellWidth = Math.max(cellWidth, s.length());
	setCellWidth();
    }

    public static Enumeration getAllNameSpaces() {
	Enumeration enum = null;

	CIMObjectPath op = new CIMObjectPath();
	if ((foundClassName == NOT_FOUND) || (foundClassName == CIM_NAMESPACE)) {
	    // get a CIMClient object that's connected to the interop namespace
	    CIMClient interopClient = CIMClientObject.createInteropClient();
	    op.setObjectName("CIM_Namespace");
	    try {
		enum = interopClient.enumerateInstanceNames(op);
		if (enum.hasMoreElements()) {
		    foundClassName = CIM_NAMESPACE;
		} else {
		    foundClassName = NOT_FOUND;
		}
	    } catch (CIMException exc) {
		    foundClassName = NOT_FOUND;		
	    }
	    if (interopClient != null) {
		try {
		    interopClient.close();
		} catch (CIMException exc) {
		    // ignore
		}
	    }
	}

	if ((foundClassName == NOT_FOUND) || (foundClassName == __NAMESPACE)) {
	    op.setObjectName("__Namespace");
	    CIMClient rootClient = CIMClientObject.changeNameSpace("root");
	    try {
		enum = rootClient.enumerateInstanceNames(op);
		if (enum.hasMoreElements()) {
		    foundClassName = __NAMESPACE;
		} else {
		    foundClassName = NOT_FOUND;
		}
	    } catch (CIMException exc) {
		    foundClassName = NOT_FOUND;		
	    }
	    if (rootClient != null) {
		try {
		    rootClient.close();
		} catch (CIMException exc) {
		    // ignore
		}
	    }
	}

	Vector v = new Vector();
	// if we have Namespace object paths, get the namespace names.
	if ((enum != null) && (foundClassName != NOT_FOUND)) {
	    while (enum.hasMoreElements()) {
		CIMObjectPath opNS = (CIMObjectPath)enum.nextElement();

		if (foundClassName == CIM_NAMESPACE) {
		    Enumeration keyEnum = opNS.getKeys().elements();
		    while (keyEnum.hasMoreElements()) {
			CIMProperty keyProp = 
			    (CIMProperty)keyEnum.nextElement();
			if ((keyProp.getName().equalsIgnoreCase("Name")) &&
			    (keyProp.getValue() != null)) {
			    CIMObjectPath newOP = new CIMObjectPath();
			    newOP.setNameSpace((String)
					       keyProp.getValue().getValue());
			    v.add(newOP);
			    break;
			}
		    }
		} else {
		    CIMObjectPath newOP = new CIMObjectPath();
		    newOP.setNameSpace("root\\" + opNS.getNameSpace());
		    v.add(newOP);
		}
	    }
        }
	return v.elements();
    }

    private boolean createNameSpace(String nameSpace) {
	boolean ret = false;
	CIMClient newCIMClient = null;
	try {
	    switch (foundClassName) {
	    case __NAMESPACE:
		newCIMClient = CIMClientObject.changeNameSpace("root");
		Vector v = new Vector();
		CIMProperty prop = new CIMProperty("NameSpace");
		prop.setValue(new CIMValue(nameSpace));
		v.addElement(prop);
		CIMInstance ci = new CIMInstance();
		ci.setClassName("__Namespace");
		ci.setProperties(v);
		newCIMClient.createInstance(new CIMObjectPath(), ci);
		ret = true;
		break;
	    case CIM_NAMESPACE:
	    default:
		newCIMClient = CIMClientObject.createInteropClient();
		CIMClass cClass = newCIMClient.getClass(
		    new CIMObjectPath("CIM_Namespace"));
		CIMInstance cInst = cClass.newInstance();
		cInst.setProperty("Name", new CIMValue(nameSpace)); 
		newCIMClient.createInstance(new CIMObjectPath(), cInst);
		ret = true;
	    }
	} catch (CIMException exc) {
	    if (exc.getID().equals("XMLERROR")) {
		ret = true;
	    } else {
		// if we have an error, display error dialog
		CIMErrorDialog.display(null, exc);
	    }
	}
	if (newCIMClient != null) {
	    try {
		newCIMClient.close();
	    } catch (CIMException exc) {
		// ignore
	    }
	}
	return ret;
    }

    private boolean deleteNameSpace(String namespace) {
	CIMClient newCIMClient = null;
	boolean ret = false;
	Enumeration enum;
	try {
	    switch (foundClassName) {
	    case __NAMESPACE:
		newCIMClient = CIMClientObject.changeNameSpace("root");
		Vector v = new Vector();
		CIMProperty prop = new CIMProperty("NameSpace");
		prop.setValue(new CIMValue(namespace));
		v.addElement(prop);
		CIMObjectPath op = new CIMObjectPath("__Namespace", v);
		newCIMClient.deleteInstance(op);
		ret = true;
		break;
	    case CIM_NAMESPACE:
	    default:
		newCIMClient = CIMClientObject.createInteropClient();
		enum = newCIMClient.enumerateInstanceNames(
	            new CIMObjectPath("CIM_NameSpace"));
		while (enum.hasMoreElements()) {
		    CIMObjectPath opInst =  (CIMObjectPath)enum.nextElement();
		    Enumeration keyEnum = opInst.getKeys().elements();
		    while (keyEnum.hasMoreElements()) {
			CIMProperty keyProp = 
			    (CIMProperty)keyEnum.nextElement();
			if ((keyProp.getName().equalsIgnoreCase("Name")) &&
			    (keyProp.getValue() != null) &&
			    (namespace.equalsIgnoreCase((String)
			        keyProp.getValue().getValue()))) {
			    newCIMClient.deleteInstance(opInst);
			    ret = true;;
			}
		    }
		}
	    }
	} catch (CIMException exc) {
	    // if we have an error, display error dialog
	    CIMErrorDialog.display(null, exc);
	}

	if (newCIMClient != null) {
	    try {
		newCIMClient.close();
	    } catch (CIMException exc) {
		// ignore
	    }
	}
	return ret;
    }
    
    class NSButtonListener implements ActionListener {
	public void actionPerformed(ActionEvent e) {
	    if (e.getSource() == btnAddNS) { 
		String newNS = JOptionPane.showInputDialog(thisDialog, 
		    I18N.loadString("ASK_CREATE_NAMESPACE"),
		    I18N.loadString("TTL_NEW_NAMESPACE"),
		    JOptionPane.PLAIN_MESSAGE);
		if ((newNS != null) && !newNS.equals("")) {
		    // clean namespace string will return expected slashes
		    newNS = Util.cleanNameSpaceString(newNS);
		    // try to create new namespace
		    if (createNameSpace(newNS)) {
			listModel.addElement(new CIMObjectPath("", newNS));
			setCellWidth(newNS);
		    } 
		}
	    } else if (e.getSource() == btnDeleteNS) { 
		int selectedIndex = list.getSelectedIndex();
		if (selectedIndex < 0) {
		    JOptionPane.showMessageDialog(thisDialog, I18N.loadString(
					  "ERR_NO_NAMESPACE_SELECTED"), 
					  I18N.loadString("TTL_CIM_ERROR"), 
					  JOptionPane.ERROR_MESSAGE);
		} else {
		    CIMObjectPath op = (CIMObjectPath)listModel.elementAt(
		        selectedIndex);
		    if (op.getNameSpace().equalsIgnoreCase(
			currentNameSpace.getNameSpace())) {
			JOptionPane.showMessageDialog(thisDialog, 
		        I18N.loadString("ERR_CURRENT_NAMESPACE"), 
			I18N.loadString("TTL_CIM_ERROR"), 
			JOptionPane.INFORMATION_MESSAGE);		    
		    } else {
			String selectedNS = op.getNameSpace();
			int option = JOptionPane.showConfirmDialog(thisDialog,  
		            I18N.loadStringFormat("ASK_DELETE_NAMESPACE", 
			    selectedNS), I18N.loadString("TTL_DLG_CIM_WORKSHOP"),
			    JOptionPane.OK_CANCEL_OPTION, 
			    JOptionPane.QUESTION_MESSAGE);
		        if (option == JOptionPane.YES_OPTION) {
			    if (deleteNameSpace(selectedNS)) {
				listModel.removeElement(op);
				if (selectedIndex > 0) {
				    selectedIndex -= 1;
				} else if (selectedIndex >= listModel.size()) {
				    selectedIndex = listModel.size() - 1;
				}
				list.setSelectedIndex(selectedIndex);
			    }
			}
		    }
		}
	    }
	}
    }
}
