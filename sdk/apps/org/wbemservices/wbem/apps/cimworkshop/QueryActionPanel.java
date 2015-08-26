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
import java.awt.event.ActionListener;
import java.util.Enumeration;

import javax.swing.event.*;
import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMInstance;
import javax.wbem.cim.CIMObjectPath;
import javax.wbem.client.CIMClient;

import org.wbemservices.wbem.apps.common.CIMErrorDialog;
import org.wbemservices.wbem.apps.common.I18N;
import org.wbemservices.wbem.apps.common.Util;

/**
 * 
 *
 * @version 	1.3, 08/16/01
 * @author 	Sun Microsystems
 */

public class QueryActionPanel extends InstancePanel implements 
    ListSelectionListener, ActionListener {

    private CIMTableTabbedPane tablePane;

    private QueryFrame queryFrame;
    private String queryString = null;

    public QueryActionPanel(CIMTableTabbedPane tp, QueryFrame qFrame, 
			    String query) {
	super(null, true);
	emptyInstances.setText(I18N.loadString("ERR_QUERY_RESULT"));
	queryString = query;
	tablePane = tp;
	queryFrame = qFrame;

	instanceList.getSelectionModel().addListSelectionListener(this);

	setVisible(true);

    }

    public void actionPerformed(ActionEvent e) {
    }

    public void valueChanged(ListSelectionEvent evt) {
	// only want to process this event when ValueIsAdjusting is false
	if (evt.getValueIsAdjusting()) {
	    return;
	}

	int i;
	i = instanceList.getSelectedIndex();
	Util.setWaitCursor(this);
	if (!listModel.isEmpty() && (i >= 0) && 
		    (i < listModel.size())) {
	    try {
		CIMObjectPath op = (CIMObjectPath) listModel.elementAt(i);
		CIMInstance ci = cimClient.getInstance(op, false, true, 
						       true, null);
		tablePane.populateTables(cimClient, ci);
	    } catch (CIMException exc) {
		tablePane.populateTables(null, null);
		CIMErrorDialog.display(this, exc);
	    }
	} else {
	    tablePane.populateTables(null, null);
	}
 	Util.setDefaultCursor(this);
    }

    public void refreshInstanceList() {
	listModel.removeAllElements();
	try {
	    Enumeration e = cimClient.execQuery(new CIMObjectPath(), 
						queryString, CIMClient.WQL);
	    while (e.hasMoreElements()) {
		CIMInstance nextInstance = (CIMInstance)e.nextElement();
		CIMObjectPath op = nextInstance.getObjectPath();
		listModel.addElement(op);
	    }
	} catch (CIMException exc) {
	    CIMErrorDialog.display(this, exc);
	}
	if (listModel.size() > 0) {
	    instanceList.setSelectedIndex(0);
	}


	setViewPort();
    }

}   
