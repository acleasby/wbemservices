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
import java.util.Vector;

import javax.swing.*;
import javax.wbem.cim.*;
import javax.wbem.client.CIMClient;

import org.wbemservices.wbem.apps.common.ActionString;
import org.wbemservices.wbem.apps.common.CIMClientObject;
import org.wbemservices.wbem.apps.common.CIMErrorDialog;
import org.wbemservices.wbem.apps.common.Util;

/**
 * 
 *
 * @author 	Sun Microsystems
 */

public class CIMMethodsPanel extends JScrollPane implements 
    MouseListener, ActionListener {

    private DefaultListModel listModel;
    private JList methodsList;
    protected JPopupMenu popupMenu;
    protected JMenuItem mnuInvokeMethod;
    protected CIMClient cimClient;
    protected boolean newElement;
    protected boolean isInstance = false;
    protected CIMElement cimElement;

    public CIMMethodsPanel() {
	this(false);
    }
    public CIMMethodsPanel(boolean nElement) {
	super();
	newElement = nElement;

	listModel = new DefaultListModel() {
	    public Object getElementAt(int index) {
		CIMMethod method = (CIMMethod)super.getElementAt(index);
		return method.toString();
	    }
	};
	methodsList = new JList(listModel);

	methodsList.getSelectionModel().setSelectionMode(
	    ListSelectionModel.SINGLE_SELECTION);
	methodsList.setBackground(this.getBackground());
	methodsList.addMouseListener(this);

	setViewportView(methodsList);

	// create popup menu and menu items;
	popupMenu = new JPopupMenu();

	ActionString asQualifiers = new ActionString("LBL_QUALIFIERS");
	ActionString asInvoke = new ActionString("MNU_INVOKE_METHOD");
	JMenuItem menuItem = popupMenu.add(new JMenuItem(
					   asQualifiers.getString()));
	menuItem.setActionCommand("QUALIFIERS");
	menuItem.addActionListener(this);

	mnuInvokeMethod = new JMenuItem(asInvoke.getString());	
	mnuInvokeMethod.setActionCommand("INVOKE_METHOD");	
	mnuInvokeMethod.addActionListener(this);
	mnuInvokeMethod.setEnabled(CIMClientObject.userHasWritePermission());

    }



    public void actionPerformed(ActionEvent evt) {
	String actionCmd = evt.getActionCommand();
	if (actionCmd.equals("QUALIFIERS")) {
// BUGFIX. Accessibility fixes
	    showQualifiers();
	} else if (actionCmd.equals("INVOKE_METHOD")) {
	    invokeMethod();
	}
    }
    
// BUGFIX. Accessibility fixes
    public void showQualifiers() {
	int index = methodsList.getMinSelectionIndex();
	CIMMethod method = (CIMMethod)(listModel.elementAt(index));
	QualifierWindow qualifierWindow = new QualifierWindow(
	    Util.getFrame(this), cimClient, method, newElement);
    }
    public void populateList(CIMClient cc, CIMElement pElement) {
	cimClient = cc;
	Vector methods = new Vector();
	Enumeration enum;
	Util.setWaitCursor(this);

	cimElement = pElement;
	listModel.removeAllElements();
	if (pElement != null) {
	    if (pElement instanceof CIMInstance) {
		if (popupMenu.getComponentIndex(mnuInvokeMethod) == -1) {
		    popupMenu.add(mnuInvokeMethod);
		}
		isInstance = true;
		try {
		    CIMObjectPath op = new CIMObjectPath(
			((CIMInstance)pElement).getClassName());
		    CIMClass cl = cimClient.getClass(op, false, true, 
						     true, null);
		    methods = cl.getMethods();
		} catch (CIMException exc) {
		    CIMErrorDialog.display(this, exc);
		}
	    } else if (pElement instanceof CIMClass) {
		methods  = ((CIMClass)pElement).getMethods();
	    }
	    for (enum = methods.elements(); enum.hasMoreElements(); ) {
		CIMMethod method = (CIMMethod)enum.nextElement();
		listModel.addElement(method);
	    }
	}
	if (listModel.size() > 0) {
	    methodsList.setSelectedIndex(0);
	}

	Util.setDefaultCursor(this);
    }

    public void invokeMethod() {
	Util.setWaitCursor(this);
	CIMMethod method = (CIMMethod)listModel.elementAt(
			    methodsList.getSelectedIndex());
	CIMObjectPath op = ((CIMInstance)cimElement).getObjectPath();
	InvokeMethodDialog dlg = new InvokeMethodDialog(
	    Util.getFrame(this), method, op);		    
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
	} else if ((evt.getClickCount() > 1) && isInstance) {
	}
    }

    public void mouseEntered(MouseEvent evt) {
    }


    public void mouseExited(MouseEvent evt) {
    }

// BUGFIX. Accessibility fixes
    public CIMMethod getSelectedMethod() {
	int i = methodsList.getSelectedIndex();
	if (i >= 0) {
	    return (CIMMethod)listModel.elementAt(i);
	}
	return null;
    }

    public boolean isSelectionEmpty() {
	return methodsList.isSelectionEmpty(); 
    }
//
    protected void showPopupMenu(Point point) {
	int index = methodsList.locationToIndex(point);
	// if whitespace, don't show popup
	if (index == -1) {
	    return;
	}
	methodsList.setSelectedIndex(index);
	Point vpLocation = getViewport().getViewPosition();
	popupMenu.show(this, (point.x - vpLocation.x + 10), 
			     (point.y - vpLocation.y));
    }

}

