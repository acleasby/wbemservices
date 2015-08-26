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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.*;

import javax.wbem.cim.*;
import javax.wbem.client.CIMClient;

import org.wbemservices.wbem.apps.common.ActionString;
import org.wbemservices.wbem.apps.common.ButtonPanel;
import org.wbemservices.wbem.apps.common.CIMClientObject;
import org.wbemservices.wbem.apps.common.CIMErrorDialog;
import org.wbemservices.wbem.apps.common.ColumnLayout;
import org.wbemservices.wbem.apps.common.FlowArea;
import org.wbemservices.wbem.apps.common.I18N;
import org.wbemservices.wbem.apps.common.LAYOUT_ALIGNMENT;
import org.wbemservices.wbem.apps.common.Util;

/**
 * 
 *
 * @author 	Sun Microsystems Inc.
 */

/**
 * This class displays a dialog that allows a user to register for events for
 * a selected class.  It uses a common indication handler which is passed in.
 * All instances of indication filter and indication subscription are saved
 * so they can be removed when an event is unsubscribed.
 *
 */
public class CIMEventsPanel extends JScrollPane implements 
    ActionListener {

    protected Hashtable eventSelectionTable = null;
    protected Hashtable filterInstanceTable = null;
    protected Hashtable filterDeliveryInstanceTable = null;
    protected int eventMask = 0;
    protected CIMClass cimClass = null;
    protected CIMClient cimClient = null;
    protected CIMObjectPath indicationHandler = null;

    protected JPanel mainPanel;
    protected CardLayout card;

    JButton btnSet;

    JCheckBox btnInstCreate;
    JCheckBox btnInstDelete;
    JCheckBox btnInstModify;

    JRadioButton btnEnable;
    JRadioButton btnDisable;

    private final String CREATE = "CIM_InstCreation";
    private final String DELETE = "CIM_InstDeletion";
    private final String MODIFY = "CIM_InstModification";

    private final int NONE = 0;
    private final int LIFE_CYCLE = 1;
    private final int PROCESS_INDICATION = 2;

    protected int eventType = NONE;
    
    protected boolean eventsSupported = true;

    public CIMEventsPanel() {
	super();
	// event selection table contains a mask corresponding to the events
	// that are subscribed to for a particular class
	eventSelectionTable = new Hashtable();
	// filterInstanceTable contains CIMObjectPath of the filter instance of
	// an event subscription.  This is saved so the instance can be deleted
	// when user unsubscribes to a an event
	filterInstanceTable = new Hashtable();
	// filterDeliveryInstanceTable contains CIMObjectPath of the
	// filterDelivery instance of an event subscription.  This is saved so 
	// the instance can be deleted when user unsubscribes to a an event
	filterDeliveryInstanceTable = new Hashtable();

	JPanel panel = new JPanel(new BorderLayout());
	mainPanel = new JPanel();
	card = new CardLayout();
	mainPanel.setLayout(card);

	ActionString asSet = new ActionString("LBL_SET");
	btnSet = new JButton(asSet.getString());
	btnSet.setMnemonic(asSet.getMnemonic());
	btnSet.setActionCommand("SET");
	btnSet.addActionListener(this);
	ButtonPanel buttonPanel = new ButtonPanel();
	buttonPanel.add(btnSet);

	mainPanel.add("LIFE_CYCLE", createLifeCyclePanel());
	mainPanel.add("PROCESS_INDICATION", createProcessIndicationPanel());
	mainPanel.add("NONE", createMsgPanel("MSG_EVENT_CLASS"));
	mainPanel.add("NO_EVENTS", createMsgPanel("MSG_NO_EVENTS"));

	card.show(mainPanel, "NO_NONE");
	panel.add(mainPanel, "North");
	panel.add(buttonPanel, "South");


	setViewportView(panel);
    }


    /**
     * creates a panel with a I18 message in it.
     *
     */
    private JPanel createMsgPanel(String msg) {
	JPanel panel = new JPanel();
       	FlowArea msgArea = new FlowArea(I18N.loadString(msg), 35);
	msgArea.setBorder(BorderFactory.createEmptyBorder(15, 5, 15, 5));
	panel.add(msgArea);
	return panel;
    }


    /**
     * createLifeCyclesPanel creates a panel that will be displayed which
     * allows a user to subscribe to lifecycle events.  Creates a checkbox
     * for each type of lifecycle event.
     *
     */
    private JPanel createLifeCyclePanel() {
	JPanel panel = new JPanel(new ColumnLayout(LAYOUT_ALIGNMENT.CENTER));

	ActionString asCreate = new ActionString("LBL_INSTANCE_CREATE");
	ActionString asDelete = new ActionString("LBL_INSTANCE_DELETE");
	ActionString asModify = new ActionString("LBL_INSTANCE_MODIFY");

	btnInstCreate = new JCheckBox(asCreate.getString());
	btnInstCreate.setMnemonic(asCreate.getMnemonic()); 
	btnInstCreate.addActionListener(this);
	btnInstDelete = new JCheckBox(asDelete.getString());
	btnInstDelete.setMnemonic(asDelete.getMnemonic()); 
	btnInstDelete.addActionListener(this);
	btnInstModify = new JCheckBox(asModify.getString());
	btnInstModify.setMnemonic(asModify.getMnemonic()); 
	btnInstModify.addActionListener(this);

       	FlowArea msg = new FlowArea(I18N.loadString("MSG_SELECT_EVENTS"), 35);
	msg.setBorder(BorderFactory.createEmptyBorder(15, 5, 15, 5));
	
	JPanel cbPanel = new JPanel(new ColumnLayout(LAYOUT_ALIGNMENT.LEFT));
	
	cbPanel.add(btnInstCreate);
	cbPanel.add(btnInstDelete);
	cbPanel.add(btnInstModify);
        
	panel.add(msg);
	panel.add(cbPanel);
	return panel;
    }

    /**
     * createProcessIndicationPanel creates a panel that will be displayed which
     * allows a user to subscribe to Process Indication events.  It has 2 
     * radio buttons that allow the user to choose either enable or disable.
     * 
     */
    private JPanel createProcessIndicationPanel() {
	JPanel panel = new JPanel(new ColumnLayout(LAYOUT_ALIGNMENT.CENTER));
	ActionString asEnable = new ActionString("LBL_ENABLED");
	ActionString asDisable = new ActionString("LBL_DISABLED");

	btnEnable = new JRadioButton(asEnable.getString());
	btnEnable.setMnemonic(asEnable.getMnemonic()); 
	btnEnable.addActionListener(this);

	btnDisable = new JRadioButton(asDisable.getString());
	btnDisable.setMnemonic(asDisable.getMnemonic()); 
	btnDisable.addActionListener(this);
	
	ButtonGroup group = new ButtonGroup();
	group.add(btnEnable);
	group.add(btnDisable);

       	FlowArea msg = new FlowArea(I18N.loadString("MSG_PI_EVENTS"), 35);
	msg.setBorder(BorderFactory.createEmptyBorder(15, 5, 15, 5));
	
	JPanel cbPanel = new JPanel(new ColumnLayout(LAYOUT_ALIGNMENT.LEFT));
	
	cbPanel.add(btnEnable);
	cbPanel.add(btnDisable);
	
	panel.add(msg);
	panel.add(cbPanel);
	return panel;
    }

    /**
     * Method used to delete all events subscriptions.  This will be called when
     * user changes namespace and/or host or closes application
     *
     */
    public void deleteEventSubscriptions() {
	if ((cimClient != null)) {
	    // get CIMObjectPath of all subscription instances and delete those
	    // instances
	    Enumeration keys = filterDeliveryInstanceTable.keys();
	    while (keys.hasMoreElements()) {
		String key = (String)keys.nextElement();
		CIMObjectPath filterDelivery = (CIMObjectPath)
		    filterDeliveryInstanceTable.get(key);
		try {
		    cimClient.deleteInstance(filterDelivery);
		} catch (CIMException e) {
		    CIMErrorDialog.display(this, e);
		}
	    }
	    
	    // get CIMObjectPath of all filters instances and delete those
	    // instances
	    keys = filterInstanceTable.keys();
	    while (keys.hasMoreElements()) {
		String key = (String)keys.nextElement();
		CIMObjectPath filter = (CIMObjectPath)
		    filterInstanceTable.get(key);
		try {
		    cimClient.deleteInstance(filter);
		} catch (CIMException e) {
		    CIMErrorDialog.display(this, e);
		}
	    }

	    // get CIMObjectPath of indication handler instance and delete 
	    // that instance	    
	    if (indicationHandler != null) {
		try {
		    cimClient.deleteInstance(
		        Util.getRelativeObjectPath(indicationHandler));
		    indicationHandler = null; 
		} catch (CIMException e) {
		    CIMErrorDialog.display(this, e);
		}
	    }
	}

	// clear out list of saved instances
	filterInstanceTable.clear();
	filterDeliveryInstanceTable.clear();
	eventSelectionTable.clear();
    }

    /**
     * Set value of indication handler and CIMClient and show first panel
     * 
     * @param indHandler        The CIMObjectPath of the indication handler 
     * @param cClient  The CIMClient object
     */
    public void initializeEvents(CIMObjectPath indHandler, CIMClient cClient) {
	indicationHandler = indHandler;
	eventsSupported = (indicationHandler != null);
	if (!eventsSupported) {
	    card.show(mainPanel, "NO_EVENTS");
	} else {
	    card.show(mainPanel, "NONE");
	}
	cimClient = cClient;
    }

    /**
     * Sets the correct panel to show and selects/unselects checkboxes depending
     * on whether the class has event subscriptions already
     * 
     * @param cc     The selected class
     */
    public void setEventsSelection(CIMClass cc) {
	if ((cimClient == null) || (!eventsSupported)) {
	    return;
	}
	cimClass = cc;
	// if class is null, cannot do events
	if (cimClass == null) {
	    eventType = NONE;
	} else {
	    String name = cimClass.getName();
	    CIMQualifier qual = cc.getQualifier("Indication");
	    Boolean b = new Boolean("true");
	    // if class has indication, use process indication panel
	    if (((qual != null) && (qual.hasValue()) &&
		 (b.equals(qual.getValue().getValue())))) {
		eventType = PROCESS_INDICATION;
	    // if class not indication, use life cycle panel
	    } else {
		eventType = LIFE_CYCLE;
	    }
	    Integer i = new Integer(0);
	    
	    // if user has already subscribed to events for this class,
	    // we will mark the events selected
	    if (eventSelectionTable.containsKey(name)) {
		i = (Integer)eventSelectionTable.get(name);
	    }
	    eventMask = i.intValue();
	    initializeControls();
	}
	// show correct Panel
	switch (eventType) {
	case NONE:
	    card.show(mainPanel, "NONE");
	    break;
	case LIFE_CYCLE:
	    card.show(mainPanel, "LIFE_CYCLE");
	    break;
	case PROCESS_INDICATION:
	    card.show(mainPanel, "PROCESS_INDICATION");
	    break;
	}	    
	btnSet.setEnabled(false);
    }



    /**
     * selects/unselects the event checkboxes depending on whether this class
     * has events subscriptions already
     */
    private void initializeControls() {
	if (eventType == LIFE_CYCLE) {
	    btnInstCreate.setSelected((eventMask & 1) > 0);
	    btnInstDelete.setSelected((eventMask & 2) > 0);
	    btnInstModify.setSelected((eventMask & 4) > 0);
	} else if (eventType == PROCESS_INDICATION) {
	    btnDisable.setSelected(eventMask == 0);
	}

    }

    /**
     * Get a bit mask corresponding to which event check boxes are selected
     *
     * @return int value for bit mask of selected events
     */
    private int getSelectedMask() {
	int ret = 0;
	
	if (eventType == LIFE_CYCLE) {
	    if (btnInstCreate.isSelected()) {
		ret = ret + 1;
	    }
	    if (btnInstDelete.isSelected()) {
		ret = ret + 2;
	    }
	    if (btnInstModify.isSelected()) {
		ret = ret + 4;
	    }
	} else if (eventType == PROCESS_INDICATION) {
	    if (btnEnable.isSelected()) {
		ret = 1;
	    }
	}
	return ret;
    }
	

    public void actionPerformed(ActionEvent evt) {
	String action = evt.getActionCommand();
	if (action.equals("SET")) {
	    int i = getSelectedMask();
	    setEvents(i);
	    // if nothing checked, remove class name from list of classes
	    // with event subscriptions, otherwise add classname to list
	    // with bit mask of subscribed events
	    if (i == 0) {
		eventSelectionTable.remove(cimClass.getName());
	    } else {
		eventSelectionTable.put(cimClass.getName(), new Integer(i));
	    }
	    eventMask = i;
	    btnSet.setEnabled(false);
	} else {
	    enableSetButton();
	}
    }

    /**
     * Enables/disables set button depending on whether currently selected event
     * subscription is different then what was already selected for this class
     * has events subscriptions already
     */
    private void enableSetButton() {
	btnSet.setEnabled(eventMask != getSelectedMask());
    } 

    /**
     * If an event hasn't been subscribed to yet and the checkbox for that event
     * is selected, subscribe to that event.  If an event has already been 
     * subscribed to and the checkbox is unselected, unsubscribe to that event
     * 
     * @param newMask  bit mask corresponding to new event subscription status
     */
    private void setEvents(int newMask) {
	if (!createIndicationHandler()) {
	    return;
	}
	if (eventType == LIFE_CYCLE) {
	    if ((~(eventMask & 1) & (newMask & 1)) > 0) { 
		setLifeCycleInstance(CREATE);
	    }
	    if ((~(eventMask & 2) & (newMask & 2)) > 0) {
		setLifeCycleInstance(DELETE);
	    } 
	    if ((~(eventMask & 4) & (newMask & 4)) > 0) {
		setLifeCycleInstance(MODIFY);
	    } 
	    if (((eventMask & 1) & ~(newMask & 1)) > 0) {	   
		removeLifeCycleInstance(CREATE);
	    } 
	    if (((eventMask & 2) & ~(newMask & 2)) > 0) {
		removeLifeCycleInstance(DELETE);
	    } 
	    if (((eventMask & 4) & ~(newMask & 4)) > 0) {
		removeLifeCycleInstance(MODIFY);
	    }
	} else if (eventType == PROCESS_INDICATION) {
	    if ((eventMask == 0) && (newMask == 1)) {
		setIndicationInstance();
	    } else if ((eventMask == 1) && (newMask == 0)) {
		removeIndicationInstance();
	    }
	}
    }

    /**
     * This creates a indication filter instances and using that and the stored
     * indication handler, creates an indication subscription. 
     * 
     */
    public void addFilter(String name, String query, String queryLanguage) {
	addFilter("", "", "", name, "", query, queryLanguage);
    }

    /**
     * This creates a indication filter instances and using that and the stored
     * indication handler, creates an indication subscription. 
     * 
     */
    public void addFilter(String systemCreationClassName, String systemName,
			  String creationClassName, String name,
			  String sourceNamespace, String query,
			  String queryLanguage) {

	CIMObjectPath filter = null;
	try {
	    CIMClass cimFilter = cimClient.getClass(new CIMObjectPath(
	        "CIM_IndicationFilter"), true, true, true, null);

	    CIMInstance ci = cimFilter.newInstance();
	    
	    if ((systemCreationClassName != null) && 
		(systemCreationClassName.trim().length() > 0)) {
		ci.setProperty("SystemCreationClassName", 
			       new CIMValue(systemCreationClassName));
	    }
	
	    if ((systemName != null) && (systemName.trim().length() > 0)) {
		ci.setProperty("SystemName", new CIMValue(systemName));
	    }
	
	    if ((creationClassName != null) && 
		(creationClassName.trim().length() > 0)) {
		ci.setProperty("CreationClassName", 
			       new CIMValue(creationClassName));
	    }
	
	    if ((sourceNamespace != null) && 
		(sourceNamespace.trim().length() > 0)) {
		ci.setProperty("SourceNamespace", 
			       new CIMValue(sourceNamespace));
	    }

	    // don't set name property so it will be uniquely set byte system
	    //	    if (name != null) {
	    // ci.setProperty("Name", new CIMValue(name));
	    // }
	    if (query != null) {		
		ci.setProperty("Query", new CIMValue(query));
	    }

	    if (queryLanguage != null) {
		ci.setProperty("QueryLanguage", new CIMValue(queryLanguage));
	    } else {
		ci.setProperty("QueryLanguage", new CIMValue("WQL"));
	    }
	    
	    filter = cimClient.createInstance(new CIMObjectPath(), ci);
	    
	    CIMClass ccFilterDelivery = cimClient.getClass(new CIMObjectPath(
	        "CIM_IndicationSubscription"), true, true, true, null);

	    ci = ccFilterDelivery.newInstance();
	
	    ci.setProperty("filter", new CIMValue(filter));
	    ci.setProperty("handler", new CIMValue(indicationHandler));

	    CIMObjectPath filterDelivery = cimClient.createInstance(
	        new CIMObjectPath(), ci);


	    if (filterDelivery == null) {
		filterDelivery = new CIMObjectPath();
		filterDelivery.setNameSpace("");
		filterDelivery.setObjectName(ci.getClassName());
		filterDelivery.setKeys(ci.getKeys());
	    }

	    // save filter and filter delivery object paths for deletion
	    filterInstanceTable.put(name, Util.getRelativeObjectPath(filter));
	    filterDeliveryInstanceTable.put(name, Util.getRelativeObjectPath(
			   filterDelivery));

	} catch (CIMException e) {
	    CIMErrorDialog.display(this, e);
	    try {
		if (filter != null) {
		    cimClient.deleteInstance(
                        Util.getRelativeObjectPath(filter));
		}
	    } catch (CIMException e1) {
		// ignore, only cleanup
	    }
	    return;
	}
    }

    private void removeLifeCycleInstance(String instanceType) {
	if (cimClass == null) {
	    return;
	}	

	String cName = cimClass.getName();
	String name = instanceType + "." + cName;
	CIMObjectPath filter = null;
	CIMObjectPath filterDelivery = null;
	filter = (CIMObjectPath)filterInstanceTable.get(name);
	filterDelivery = (CIMObjectPath)filterDeliveryInstanceTable.get(name);
	try {
	    if (filterDelivery != null) {
		cimClient.deleteInstance(filterDelivery);
		filterDeliveryInstanceTable.remove(name);
	    }
	} catch (CIMException e1) {
	    // XXX might want more specific error
	    CIMErrorDialog.display(this, e1);
	}
	try {
	    if (filter != null) {
		cimClient.deleteInstance(filter);
		filterInstanceTable.remove(name);
	    }
	} catch (CIMException e) {
	    // XXX might want more specific error
	    CIMErrorDialog.display(this, e);
	}
    }

    private void removeIndicationInstance() {
	if (cimClass == null) {
	    return;
	}
	String name = cimClass.getName();
	
	CIMObjectPath filter = null;
	CIMObjectPath filterDelivery = null;
	filter = (CIMObjectPath)filterInstanceTable.get(name);
	filterDelivery = (CIMObjectPath)filterDeliveryInstanceTable.get(name);
	try {
	    if (filterDelivery != null) {
		cimClient.deleteInstance(filterDelivery);
		filterDeliveryInstanceTable.remove(name);
	    }
	    } catch (CIMException e1) {
		// XXX might want more specific error
		CIMErrorDialog.display(this, e1);
	    }
	try {
	    if (filter != null) {
		cimClient.deleteInstance(filter);
		filterInstanceTable.remove(name);
	    }
	} catch (CIMException e) {
	    // XXX might want more specific error
	    CIMErrorDialog.display(this, e);
	}
    }

    private void setIndicationInstance() {
	if (cimClass == null) {
	    return;
	}
	String cName = cimClass.getName();
	String filter = "SELECT * FROM " + cName;
	addFilter(cName, filter, "WQL");
    }

    private void setLifeCycleInstance(String instanceType) {
	if (cimClass == null) {
	    return;
	}
	String cName = cimClass.getName();
	String name = instanceType + "." + cName;
	String filter = "SELECT * FROM " +  instanceType +
	    " WHERE sourceInstance ISA " + cName;
		// 633335. 11/04/02. Cannot subscribe to lifecycle indications
		// on classes in non-default namespaces.
        CIMNameSpace ns = new CIMNameSpace();
        if (ns.getNameSpace().equals(CIMClientObject.getNameSpace())) {
            addFilter(name, filter, "WQL");
        } else {
			// Set the namespace if not root/cimv2
            addFilter("", "", "", name, CIMClientObject.getNameSpace(), filter, "WQL");
        }
    }

    private boolean createIndicationHandler() {
	if (indicationHandler == null) {
	    try {
		CIMInstance ci = cimClient.getIndicationHandler(null);
		indicationHandler = cimClient.createInstance(new CIMObjectPath(),
							     ci);
	    } catch (CIMException e) {
		return false;
	    }
	}
	return true;
    }
}




