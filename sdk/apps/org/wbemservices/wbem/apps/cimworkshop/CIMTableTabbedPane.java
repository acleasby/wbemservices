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

import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;

import javax.wbem.cim.CIMClass;
import javax.wbem.cim.CIMElement;
import javax.wbem.cim.CIMObjectPath;
import javax.wbem.client.CIMClient;

import org.wbemservices.wbem.apps.common.ActionString;
import org.wbemservices.wbem.apps.common.I18N;

/**
 * 
 *
 * @author 	Sun Microsystems
 */

public class CIMTableTabbedPane extends JTabbedPane {


    protected CIMPropertiesPanel propertiesPanel;
    protected CIMMethodsPanel methodsPanel;
    protected CIMEventsPanel eventsPanel;

    protected Vector cimProperties =  null;
    protected Vector cimMethods = null;

    protected ActionString asProps;
    protected ActionString asMethods;
    protected ActionString asEvents;

    public CIMTableTabbedPane() {
	this(false);
    }

    public CIMTableTabbedPane(boolean nElement) {
	super();

	asProps = new ActionString("LBL_PROPERTIES");
	asMethods = new ActionString("LBL_METHODS");
	asEvents = new ActionString("LBL_EVENTS");

	addPropertiesTab(nElement);
	addMethodsTab(nElement);

	setSelectedIndex(0);

    }

    /**
     * removes the tab with the properties on it
     */
    public void removePropertiesTab() {
	removeTab(asProps.getString());
    }

    /**
     * removes the tab with the methods on it
     */
    public void removeMethodsTab() {
	removeTab(asMethods.getString());
    }

    /**
     * removes the tab with the event selections on it
     */
    public void removeEventsTab() {
	removeTab(asEvents.getString());
    }

    /**
     * removes the tab with the specified title
     */
    protected void removeTab(String title) {
	int index = indexOfTab(title);
	if (index >= 0) {
	    removeTabAt(index);
	}
    }

    public void addPropertiesTab(boolean nElement) {
	if (propertiesPanel == null) {
	    propertiesPanel = new CIMPropertiesPanel(nElement);
	}
	addTab(asProps.getString(), propertiesPanel);
    }
    
    public void addMethodsTab(boolean nElement) {
	if (methodsPanel == null) {
	    methodsPanel = new CIMMethodsPanel(nElement);
	}
	addTab(asMethods.getString(), methodsPanel);
    }
    
    public void addEventsTab() {
	if (eventsPanel == null) {
	    eventsPanel = new CIMEventsPanel();
	}
	addTab(asEvents.getString(), eventsPanel);
    }

    protected void addTab(String title, JPanel panel) {
	// only add tab if it doesn't already exist
	if (indexOfTab(title) < 0) {
	    addTab(title, panel);
	}
    }    

    public void initializeEvents(CIMObjectPath iHandler, CIMClient cClient) {
	if (eventsPanel != null) {
	    eventsPanel.initializeEvents(iHandler, cClient);
	}
    }

    public void deleteEventSubscriptions() {
	if (eventsPanel != null) {
	    eventsPanel.deleteEventSubscriptions();
	}
    }

    public void populateTables(CIMClient cc,
			       CIMElement pElement) {

	propertiesPanel.populateTable(cc, pElement);
	methodsPanel.populateList(cc, pElement);
	if (eventsPanel != null) {
	    eventsPanel.setEventsSelection((CIMClass)pElement);
	}
    }

    public void addPropertyTableModelListener(TableModelListener tml) {
	propertiesPanel.addTableModelListener(tml);
    }

    public void addListSelectionListener(ListSelectionListener lsl) {
	propertiesPanel.addListSelectionListener(lsl);
    }

    public CIMPropertiesPanel getPropertiesTable() {
	return propertiesPanel;
    }

    public CIMMethodsPanel getMethodsTable() {
	return methodsPanel;
    }

    public CIMEventsPanel getEventPanel() {
	return eventsPanel;
    }

    public Vector getProperties() {
	return propertiesPanel.getProperties();
    }

    public void setUneditableProperties(Vector v) {
	propertiesPanel.setUneditableRows(v);
    }

// BUGFIX. Accessibility fixes. Added new methods
    /**
     * returns true if the property list
     * is not empty and a property in the list is selected
     */
    public boolean isPropertySelected() {
	return   ((propertiesPanel != null) && 
		 (!propertiesPanel.isSelectionEmpty()));
    }

    /**
     * returns true if the methods list
     * is not empty and a method in the list is selected
     */
    public boolean isMethodSelected() {
	return  ((methodsPanel != null) && 
		 (!methodsPanel.isSelectionEmpty()));
    }

    public void invokeMethod() {
 	if (isMethodSelected()) {
	    methodsPanel.invokeMethod();
	} else {
	    JOptionPane.showMessageDialog(this, 
		    I18N.loadString("ERR_NO_METHOD_SELECTED"), 
		    I18N.loadString("TTL_CIM_ERROR"),
		    JOptionPane.ERROR_MESSAGE);
	}
    }

    public void showQualifiers() {
	if (getSelectedIndex() == indexOfTab(asMethods.getString())) {
	    showMethodQualifiers();
	} else if (getSelectedIndex() == indexOfTab(asProps.getString())) {
	    showPropertyQualifiers();
	}
    }

    public void showMethodQualifiers() {
 	if (isMethodSelected()) {
	    methodsPanel.showQualifiers();
	} else {
	    JOptionPane.showMessageDialog(this, 
		    I18N.loadString("ERR_NO_METHOD_SELECTED"), 
		    I18N.loadString("TTL_CIM_ERROR"),
		    JOptionPane.ERROR_MESSAGE);
	}
    }

    public void showPropertyQualifiers() {
 	if (isPropertySelected()) {
	    propertiesPanel.showQualifiers();
	} else {
	    JOptionPane.showMessageDialog(this, 
		    I18N.loadString("ERR_NO_PROPERTY_SELECTED"), 
		    I18N.loadString("TTL_CIM_ERROR"),
		    JOptionPane.ERROR_MESSAGE);
	}
    }

    public void showPropertyValue() {
 	if (isPropertySelected()) {
	    propertiesPanel.showValue();
	} else {
	    JOptionPane.showMessageDialog(this, 
		    I18N.loadString("ERR_NO_PROPERTY_SELECTED"), 
		    I18N.loadString("TTL_CIM_ERROR"),
		    JOptionPane.ERROR_MESSAGE);
	}
    }

}
