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

import javax.wbem.cim.*;
import javax.wbem.client.CIMClient;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


/**
 * 
 *
 * @version 	1.9, 08/16/01
 * @author 	Sun Microsystems
 */

class ReferenceEditDialog extends CIMEditDialog implements 
    ListSelectionListener, WindowListener {

    protected InstancePanel instancePanel = null;
    protected CIMKeyPropertiesPanel keyTable = null;

    JList instanceList = null;
    //    CIMClass cimClass;
    CIMInstance cimInstance;
    
    CIMObjectPath objectPath;

    protected Frame parentFrame;
    protected Vector cimValues;
    protected CIMTypes cimTypes;
    protected CIMClient cimClient;

    public ReferenceEditDialog(Frame frame, String name, CIMDataType cdt,
			       CIMObjectPath op) {
	this(frame, name, cdt, op, true);
    }

    public ReferenceEditDialog(Frame frame, String name, CIMDataType cdt,
			       CIMObjectPath op, boolean editable) {

	super(frame, name, cdt.toString(), editable);
	
	parentFrame = frame;
	cimClient = CIMClientObject.getClient();
	
	CIMClass cimClass = null;
	String className = cdt.getRefClassName();

	try {
	    cimClass = cimClient.getClass(new CIMObjectPath(className), false,
					  true, true, null);
	} catch (CIMException e) {
	    CIMErrorDialog.display(this, e);
	    dispose();
	}
	    
	objectPath = new CIMObjectPath(className);
	objectPath.setNameSpace(CIMClientObject.getNameSpace());


	cimInstance = cimClass.newInstance();
	keyTable = new CIMKeyPropertiesPanel(cimClient, cimInstance, editable);
	keyTable.setPreferredSize(new Dimension(500, 100));

	JPanel topPanel = new JPanel();
	topPanel.setLayout(new ColumnLayout(LAYOUT_ALIGNMENT.EXPAND));
	topPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));

	String sKeys = I18N.loadStringFormat("LBL_INSTANCE_KEYS", className);
	ActionString asKeys = new ActionString(sKeys, true);
	JLabel lKeys = new JLabel(asKeys.getString());
	lKeys.setDisplayedMnemonic(asKeys.getMnemonic());
	lKeys.setLabelFor(keyTable);
	topPanel.add(lKeys);
	topPanel.add(keyTable);

	JPanel mainPanel = this.getMainPanel();
	mainPanel.add(topPanel);
	String defaultHelp = "ShowValue_000.htm"; 
	if (isEditable) {
	    instancePanel = new InstancePanel(cimClass);
	    instancePanel.setPreferredSize(new Dimension(500, 100));
	    instancePanel.refreshInstanceList();

	    instanceList = instancePanel.getInstanceList();
	    instanceList.addListSelectionListener(this);

	    JPanel bottomPanel = new JPanel();
	    bottomPanel.setLayout(new ColumnLayout(LAYOUT_ALIGNMENT.EXPAND));
	    bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

	    String sInstances = I18N.loadStringFormat("LBL_INSTANCES_OF", 
						      className); 
	    ActionString asInstances = new ActionString(sInstances, true);
	    JLabel lInstances = new JLabel(asInstances.getString());
	    lInstances.setDisplayedMnemonic(asInstances.getMnemonic());
	    lInstances.setLabelFor(instancePanel);
	    bottomPanel.add(lInstances);
	    bottomPanel.add(instancePanel);
	    mainPanel.add(bottomPanel);
	    defaultHelp = "ReferenceEdit_000.htm";
	} else {
	    keyTable.setEnabled(false);
	}

	setDefaultHelp(defaultHelp);

	addWindowListener(this);
	pack();
	setLocation(Util.getCenterPoint(frame, this));
	setVisible(true);
    }

    public void valueChanged(ListSelectionEvent evt) {
	// only want to process this event when ValueIsAdjusting is false	
	if (evt.getValueIsAdjusting()) {
	    return;
	}

	int i;
	i = instanceList.getSelectedIndex();
	Util.setWaitCursor(this);
	DefaultListModel listModel = (DefaultListModel)instanceList.getModel();
	if (!listModel.isEmpty() && (i >= 0) && 
		    (i < listModel.size())) {
	    try {
		CIMInstance ci = cimClient.getInstance(
		    (CIMObjectPath) listModel.elementAt(i), false,
		    true, true, null);
		keyTable.setInstance(ci);
		objectPath.setObjectName(ci.getClassName());
	    } catch (CIMException exc) {
		CIMErrorDialog.display(this, exc);
	    }
	}
 	Util.setDefaultCursor(this);
    }


    public void okClicked() {
	Vector newProperties = keyTable.getProperties();
	Enumeration e = newProperties.elements();
	while (e.hasMoreElements()) {
	    CIMProperty cp = (CIMProperty)e.nextElement();
	    CIMValue cv = cp.getValue();
	    if (cp.isKey() ) {
                if (cv == null || cv.getValue() == null) {
		    JOptionPane.showMessageDialog(this, 
		        I18N.loadStringFormat("ERR_NO_PROPERTY_VALUE", 
		        cp.getName()), I18N.loadString("TTL_CIM_ERROR"),
		        JOptionPane.ERROR_MESSAGE);
		    return;
                } else {
                    objectPath.addKey(cp.getName(), cp.getValue());
                }
	    }
	}
 
	returnObject = objectPath;
	returnString = objectPath.toString();
	dispose();
    }

    public void windowActivated(WindowEvent evt) {}
    public void windowClosed(WindowEvent evt) {}
    public void windowClosing(WindowEvent evt) {}
    public void windowDeactivated(WindowEvent evt) {}
    public void windowDeiconified(WindowEvent evt) {}
    public void windowIconified(WindowEvent evt) {}

    
    public void windowOpened(WindowEvent evt) {	
	// when window opens, select first index if list not empty.  This
	// will kick off listSelectionEvent and fill table
	
	if (!((DefaultListModel)instanceList.getModel()).isEmpty() && 
	    isEditable) {
	    instanceList.clearSelection();
	    instanceList.setSelectedIndex(0);
	}

	if (isEditable) {
	    getOKBtn().requestFocus();
	} else {
	    getCancelBtn().requestFocus();
	}	    
    }

}

