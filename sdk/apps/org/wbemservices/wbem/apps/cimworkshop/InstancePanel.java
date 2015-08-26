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

import javax.swing.*;
import javax.swing.border.BevelBorder;

import java.awt.event.ActionEvent;
import java.util.Enumeration;

import javax.wbem.cim.CIMClass;
import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMObjectPath;
import javax.wbem.client.CIMClient;

import org.wbemservices.wbem.apps.common.ActionString;
import org.wbemservices.wbem.apps.common.CIMClientObject;
import org.wbemservices.wbem.apps.common.CIMErrorDialog;
import org.wbemservices.wbem.apps.common.I18N;

/**
 * 
 *
 * @version 	1.48, 08/16/01
 * @author 	Sun Microsystems
 */

public class InstancePanel extends JScrollPane {

    protected DefaultListModel listModel;
    protected JList instanceList;
    protected JTextArea emptyInstances;
    protected CIMClient cimClient;
    protected CIMClass cimClass;
    protected JLabel lInstances;


    public InstancePanel(CIMClass cClass) {
	this(cClass, false);
    }

    public InstancePanel(CIMClass cClass, boolean deepEnum) {
	super();
	cimClient = CIMClientObject.getClient();
	cimClass = cClass;

	String className = "";
	if (cimClass != null) {
	    className = cimClass.getName();
	}
	emptyInstances = new JTextArea(I18N.loadStringFormat(
				       "ERR_NO_INSTANCES", 
				       className));
	emptyInstances.setLineWrap(true);
	emptyInstances.setWrapStyleWord(true);
	emptyInstances.setBackground(this.getBackground());

	listModel = new DefaultListModel();
	instanceList = new JList(listModel);
	instanceList.getSelectionModel().setSelectionMode(
	    ListSelectionModel.SINGLE_SELECTION);
	instanceList.setBackground(this.getBackground());
	instanceList.setBorder(BorderFactory.createBevelBorder(
                               BevelBorder.LOWERED));
	ActionString asInstances = new ActionString("LBL_INSTANCES");
	lInstances = new JLabel(asInstances.getString() + ":");
	lInstances.setDisplayedMnemonic(asInstances.getMnemonic());
	setColumnHeaderView(lInstances);
	lInstances.setLabelFor(instanceList);
	setViewPort();
	setVisible(true);


    }

    public void actionPerformed(ActionEvent e) {
	String actionCmd = e.getActionCommand();
	if (actionCmd.equals("REFRESH")) {
	    refreshInstanceList();
	}
    }

    public JList getInstanceList() {
	return instanceList;
    }

    public void setViewPort() {
	boolean b = hasInstances();
	if (b) {
	    setViewportView(instanceList);
	} else {
	    setViewportView(emptyInstances);
	}
	lInstances.setEnabled(b);
    }

    public boolean hasInstances() {
	return (listModel.size() > 0);
    }

    public void refreshInstanceList() {
	listModel.removeAllElements();
	try {
	    CIMObjectPath className = new CIMObjectPath(cimClass.getName());
	    Enumeration e = cimClient.enumerateInstanceNames(className);
	    while (e.hasMoreElements()) {
		CIMObjectPath nextInstance = (CIMObjectPath)e.nextElement();
		listModel.addElement(nextInstance);
	    }
	} catch (CIMException exc) {
	    CIMErrorDialog.display(this, exc);
	}
	if (listModel.size() > 0) {
	    instanceList.setSelectedIndex(0);
	}

	setViewPort();
    }

    public void setListLabel(JLabel label) {
    }

}   


