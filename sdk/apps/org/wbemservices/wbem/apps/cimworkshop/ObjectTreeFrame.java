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

import javax.wbem.client.CIMEvent;
import javax.wbem.client.CIMListener;
import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.*;

/**
 *
 * @author 	Sun Microsystems
 */
public class ObjectTreeFrame extends JFrame implements WindowListener,
    ItemListener, ActionListener, TreeSelectionListener {

    private ClassTreeActionPane treePane;
    private CIMTableTabbedPane tablePane;
    private JSplitPane desktop;
    private JSplitPane fullPanel;
    private JComboBox nameSpaceList;
    private String prevNameSpace = "";
    private NameSpaceDialog nsDlg;
    private DefaultComboBoxModel cbModel = null;
    private boolean repopulating = false;

    private JTextArea eventOutputArea;

    private JMenu mnuWorkshop;
    private JMenu mnuAction;
// BUGFIX. Accessibility fixes
    private JMenu mnuNamespace;
    private JMenu mnuProperties;
    private JMenu mnuMethods;
//
    private JMenu mnuHelp;

    private JMenuItem mnuChangeHost;
    private JMenuItem mnuChangeNamespace;
    private JMenuItem mnuExit;
    private JMenuItem mnuAddClass;
    private JMenuItem mnuDeleteClass;
    private JMenuItem mnuExecQuery;
    private JMenuItem mnuFindClass;
    private JMenuItem mnuInstances;
// BUGFIX. Accessibility fixes
    private JMenuItem mnuClassQualifiers;
    private JMenuItem mnuPropertyQualifiers;
    private JMenuItem mnuMethodQualifiers;
    private JMenuItem mnuQualifierType;
//
    private JMenuItem mnuRefresh;
    private JMenuItem mnuHelpAbout;
    private JMenuItem mnuAssocTraversal;
// BUGFIX. Accessibility fixes
    private JMenuItem mnuShowPropertyValue;

    private JButton btnInstances;
    private JButton btnQualifiers;
    private JButton btnAddClass;

    public ObjectTreeFrame() {
	super();
	setTitle(I18N.loadStringFormat("TTL_CIM_WORKSHOP",
	    I18N.loadString("LBL_NOT_CONNECTED")));
	JPanel pane = (JPanel)getContentPane();
	pane.setLayout(new BorderLayout());
	addWindowListener(this);

	eventOutputArea = new JTextArea();
	eventOutputArea.setWrapStyleWord(true);
	eventOutputArea.setEditable(false);

	// fullPanel conatins desktop and events panel
	fullPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
	fullPanel.setOneTouchExpandable(true);

	// desktop contains class tree and tabbed pane.
	desktop = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);

	tablePane = new CIMTableTabbedPane() {
	    public Insets getInsets() {
    		return new Insets(5, 5, 5, 5);

	    }
	};

	tablePane.addEventsTab();

	treePane = new ClassTreeActionPane(this, tablePane) {
	    public Insets getInsets() {
    		return new Insets(5, 5, 5, 5);
	    }
	};
	treePane.addTreeSelectionListener(this);

	desktop.setLeftComponent(treePane);
	desktop.setRightComponent(tablePane);

	createMenuBar();
	createToolBar(pane);

	fullPanel.setTopComponent(desktop);
	eventOutputArea.setBorder(BorderFactory.createBevelBorder(
				  BevelBorder.LOWERED));
	JScrollPane spEventOutput = new JScrollPane(eventOutputArea);
	ActionString asEventOutput = new ActionString("LBL_EVENT_OUTPUT");
	JLabel lEventOutput = new JLabel(asEventOutput.getString() + ":");
	lEventOutput.setDisplayedMnemonic(asEventOutput.getMnemonic());
	spEventOutput.setColumnHeaderView(lEventOutput);
	lEventOutput.setLabelFor(eventOutputArea);

	fullPanel.setBottomComponent(spEventOutput);

	pane.add("Center", fullPanel);
	setSize(750, 550);
	Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
	Point screenCenter = new Point(d.width / 2, d.height / 2);
	setLocation(screenCenter.x - (getSize().width / 2), 
	    screenCenter.y - (getSize().height / 2));

	
    }

    /**
     * Gets called to login into cimworkshop
     *
     * @return boolean   True if succesfully logged on, otherwise false
     */
    public boolean login() {
	boolean b = false;
	Util.waitOn(this);
	// getHostInfo calls the login dialog to get host, namespace,  
	// username and password info.  returns false is user cancels
	// out of dialog
	if (treePane.getHostInfo()) {
	    this.show();
	    // set divider between classTreePane and tabbed pane
	    desktop.setDividerLocation(0.4);	    
	    b = true;
	}
	Util.waitOff(this);
	return b;
    }

    /**
     * This function gets called when logging into a new host.  It will modify 
     * any objects in this frame that need to be initialized or refreshed  
     * when a user changes hosts.
     *
     */
    public void initializeNewHost() {
	boolean userCanWrite = CIMClientObject.userHasWritePermission();
	enableWritableMenuItems(userCanWrite);
	setTitle(I18N.loadStringFormat("TTL_CONNECTED", 
				       CIMClientObject.getHostName()));
	populateNameSpaceComboBox();
	// reset previous name space;
	prevNameSpace = "";
	// select namespace in combo box
	setSelectedNameSpace(Util.cleanNameSpaceString(
			     CIMClientObject.getNameSpace()));
	// initialize class tree with classes from current name space
	//	treePane.initializeClassTree();
	initializeEventListener();
    }

    private void populateNameSpaceComboBox() {
	repopulating = true;
	Enumeration e = NameSpaceDialog.getAllNameSpaces();
	cbModel.removeAllElements();
	while (e.hasMoreElements()) {
	    CIMObjectPath op = (CIMObjectPath)e.nextElement();
// BUGFIX. Accessibility fixes
	    String ns = op.getNameSpace();
	    cbModel.addElement(ns);
	}
	repopulating = false;
    }

    /**
     * enables menu items that should be enbled when user has write privileges
     * to current namespace
     *
     * @param b   boolean value for whether to enable menu items
     */
    public void enableWritableMenuItems(boolean b) {
	mnuAddClass.setEnabled(b);
	mnuDeleteClass.setEnabled(b);
	btnAddClass.setEnabled(b);
    }

    /**
     * gets called when user selects a class in the class tree
     */
    public void valueChanged(TreeSelectionEvent e) {
	Util.setWaitCursor(this);
	// get name of class
	String path = e.getPath().getLastPathComponent().toString();
	boolean enable = true;
	CIMClass cimClass = null;
	if (treePane.isRootSelected()) {
	    // if root node is selected, clear out property tables
	    enable = false;
	    treePane.populateTables(null);
	} else {
	    // create new object path of className
	    CIMObjectPath op = new CIMObjectPath(path);	
	    try {
		CIMClient cimClient = CIMClientObject.getClient();
		if (cimClient != null) {
		    // populate tables with info from selected class
    		    cimClass = cimClient.getClass(op, false, true, true, null);
		    treePane.populateTables(cimClass);
		} else {
		    return;
		}
	    } catch (CIMException exc) {
		// error getting class
		CIMErrorDialog.display(this, exc);
	    }
	}

	// enable some menus only if root node IS NOT selected
	mnuAssocTraversal.setEnabled(enable);
// BUGFIX. Accessibility fixes
	mnuClassQualifiers.setEnabled(enable);
	mnuDeleteClass.setEnabled(enable && 
				  CIMClientObject.userHasWritePermission());
	btnQualifiers.setEnabled(enable);
// BUGFIX. Accessibility fixes
	mnuPropertyQualifiers.setEnabled(enable && 
					 tablePane.isPropertySelected());
	mnuShowPropertyValue.setEnabled(enable &&
					 tablePane.isPropertySelected());
	mnuMethodQualifiers.setEnabled(enable &&
					 tablePane.isMethodSelected());
//
	// enable instance button and menus only if not namespace 
	// selected and class is keyed
	if (cimClass != null) {
	    enable = enable && cimClass.isKeyed();
	}
	btnInstances.setEnabled(enable);
	mnuInstances.setEnabled(enable);
	treePane.enableInstanceMenuItem(enable);
 	Util.setDefaultCursor(this);

    }

    /**
     * creates menu bar and menu items
     */
    private void createMenuBar() {
	JMenuBar menuBar = new JMenuBar();
	setJMenuBar(menuBar);
	ActionString asWorkshop = new ActionString("MNU_WORKSHOP");
	ActionString asAction = new ActionString("MNU_ACTION",
			      "org.wbemservices.wbem.apps.common.common");
// BUGFIX. Accessibility fixes
	ActionString asNamespace = new ActionString("MNU_NAMESPACE");
	ActionString asProperties = new ActionString("MNU_PROPERTIES");
	ActionString asMethods = new ActionString("MNU_METHODS");
//
	ActionString asHelp = new ActionString("MNU_HELP",
			      "org.wbemservices.wbem.apps.common.common");
	ActionString asRefresh = new ActionString("MNU_REFRESH_CLASS");
	ActionString asExit = new ActionString("MNU_EXIT",
			      "org.wbemservices.wbem.apps.common.common");
	ActionString asHost = new ActionString("MNU_CHANGE_HOST");
// BUGFIX. Accessibility fixes
	ActionString asChangeNamespace = new ActionString(
					 "MNU_CHANGE_NAMESPACE");
	ActionString asAdd = new ActionString("MNU_ADD_CLASS");
	ActionString asDelete = new ActionString("MNU_DELETE_CLASS");
	ActionString asFind = new ActionString("MNU_FIND_CLASS");
	ActionString asInstances = new ActionString("MNU_INSTANCES");
// BUGFIX. Accessibility fixes
	ActionString asClassQualifiers = new ActionString(
					 "MNU_CLASS_QUALIFIERS");
	ActionString asQualifiers = new ActionString("MNU_QUALIFIERS");
// BUGFIX. Accessibility fixes
	ActionString asQualifierType = new ActionString(
				       "MNU_ADD_QUALIFIER_TYPE");
	ActionString asAssoc = new ActionString("MNU_ASSOC_TRAVERSAL");
	ActionString asExecQuery = new ActionString("MNU_EXEC_QUERY");
	ActionString asHelpAbout = new ActionString("MNU_ABOUT_CIMWORKSHOP");
	ActionString asClearEventOutput = new ActionString(
            "MNU_CLEAR_EVENT_OUTPUT");
// BUGFIX. Accessibility fixes
	ActionString asShowValue = new ActionString("MNU_SHOW_VALUE");

	mnuWorkshop = new JMenu(asWorkshop.getString());
	mnuWorkshop.setMnemonic(asWorkshop.getMnemonic());
	mnuAction = new JMenu(asAction.getString());
	mnuAction.setMnemonic(asAction.getMnemonic());
// BUGFIX. Accessibility fixes
	mnuNamespace = new JMenu(asNamespace.getString());
	mnuNamespace.setMnemonic(asNamespace.getMnemonic());
	mnuProperties = new JMenu(asProperties.getString());
	mnuProperties.setMnemonic(asProperties.getMnemonic());
	mnuMethods = new JMenu(asMethods.getString());
	mnuMethods.setMnemonic(asMethods.getMnemonic());
//
	mnuHelp = new JMenu(asHelp.getString());
	mnuHelp.setMnemonic(asHelp.getMnemonic());

	menuBar.add(mnuWorkshop);
	menuBar.add(mnuAction);
	menuBar.add(mnuHelp);

	mnuChangeHost = new JMenuItem(asHost.getString());
	mnuChangeHost.setMnemonic(asHost.getMnemonic());
	mnuChangeHost.addActionListener(treePane);
	mnuChangeHost.setActionCommand("hostDialog");

// BUGFIX. Accessibility fixes
	mnuChangeNamespace = new JMenuItem(asChangeNamespace.getString());
	mnuChangeNamespace.setMnemonic(asChangeNamespace.getMnemonic());
//
	mnuChangeNamespace.addActionListener(this);
	mnuChangeNamespace.setActionCommand("CHANGE_NAMESPACE");

	mnuExit = new JMenuItem(asExit.getString());
	mnuExit.setMnemonic(asExit.getMnemonic());
	mnuExit.setActionCommand("EXIT");
	mnuExit.addActionListener(this);

	mnuAddClass = new JMenuItem(asAdd.getString());
	mnuAddClass.setMnemonic(asAdd.getMnemonic());
	mnuAddClass.setActionCommand("ADD_CLASS");
	mnuAddClass.addActionListener(treePane);

	mnuDeleteClass = new JMenuItem(asDelete.getString());
	mnuDeleteClass.setMnemonic(asDelete.getMnemonic());
	mnuDeleteClass.setActionCommand("DELETE_CLASS");
	mnuDeleteClass.addActionListener(treePane);

	mnuFindClass = new JMenuItem(asFind.getString());
	mnuFindClass.setMnemonic(asFind.getMnemonic());
	mnuFindClass.setActionCommand("FIND_CLASS");
	mnuFindClass.addActionListener(treePane);

	mnuInstances = new JMenuItem(asInstances.getString());
	mnuInstances.setMnemonic(asInstances.getMnemonic());
	mnuInstances.setActionCommand("INSTANCES");
	mnuInstances.addActionListener(treePane);

// BUGFIX. Accessibility fixes
	mnuClassQualifiers = new JMenuItem(asClassQualifiers.getString());
	mnuClassQualifiers.setMnemonic(asClassQualifiers.getMnemonic());
	mnuClassQualifiers.setActionCommand("QUALIFIERS");
	mnuClassQualifiers.addActionListener(treePane);

	mnuPropertyQualifiers = new JMenuItem(asQualifiers.getString());
	mnuPropertyQualifiers.setMnemonic(asQualifiers.getMnemonic());
	mnuPropertyQualifiers.setActionCommand("QUALIFIERS");
	mnuPropertyQualifiers.addActionListener(tablePane.getPropertiesTable());

	mnuShowPropertyValue = new JMenuItem(asShowValue.getString());
	mnuShowPropertyValue.setMnemonic(asShowValue.getMnemonic());
	mnuShowPropertyValue.setActionCommand("SHOW_VALUE");
	mnuShowPropertyValue.addActionListener(tablePane.getPropertiesTable());

	mnuMethodQualifiers = new JMenuItem(asQualifiers.getString());
	mnuMethodQualifiers.setMnemonic(asQualifiers.getMnemonic());
	mnuMethodQualifiers.setActionCommand("QUALIFIERS");
	mnuMethodQualifiers.addActionListener(tablePane.getMethodsTable());

	mnuQualifierType = new JMenuItem(asQualifierType.getString());
	mnuQualifierType.setMnemonic(asQualifierType.getMnemonic());
	mnuQualifierType.setActionCommand("ADD_QUALIFIER_TYPE");
	mnuQualifierType.addActionListener(treePane);
//
	mnuAssocTraversal = new JMenuItem(asAssoc.getString());
	mnuAssocTraversal.setMnemonic(asAssoc.getMnemonic());
	mnuAssocTraversal.setActionCommand("ASSOC_TRAVERSAL");
	mnuAssocTraversal.addActionListener(treePane);

	mnuExecQuery = new JMenuItem(asExecQuery.getString());
	mnuExecQuery.setMnemonic(asExecQuery.getMnemonic());
	mnuExecQuery.setActionCommand("EXEC_QUERY");
	mnuExecQuery.addActionListener(treePane);

	mnuRefresh = new JMenuItem(asRefresh.getString());
	mnuRefresh.setMnemonic(asRefresh.getMnemonic());
	mnuRefresh.setActionCommand("REFRESH");
	mnuRefresh.addActionListener(treePane);

	JMenuItem mnuClearEventOutput = new JMenuItem(
            asClearEventOutput.getString());
	mnuClearEventOutput.setMnemonic(asClearEventOutput.getMnemonic());
	mnuClearEventOutput.setActionCommand("CLEAR_EVENT_OUTPUT");
	mnuClearEventOutput.addActionListener(this);

	mnuHelpAbout = new JMenuItem(asHelpAbout.getString());
	mnuHelpAbout.setMnemonic(asHelpAbout.getMnemonic());
	mnuHelpAbout.setActionCommand("ABOUT_CIMWORKSHOP");
	mnuHelpAbout.addActionListener(this);

// BUGFIX. Accessibility fixes
	mnuNamespace.add(mnuQualifierType);
	mnuProperties.add(mnuPropertyQualifiers);
	mnuProperties.add(mnuShowPropertyValue);
	mnuMethods.add(mnuMethodQualifiers);
//
	mnuWorkshop.add(mnuChangeHost);
	mnuWorkshop.addSeparator();
	mnuWorkshop.add(mnuChangeNamespace);
	mnuWorkshop.addSeparator();
	mnuWorkshop.add(mnuExit);

	mnuAction.add(mnuAddClass);
	mnuAction.add(mnuDeleteClass);
	mnuAction.add(mnuFindClass);
	mnuAction.addSeparator();
	mnuAction.add(mnuInstances);
// BUGFIX. Accessibility fixes
	mnuAction.add(mnuClassQualifiers);
	mnuAction.add(mnuAssocTraversal);
	mnuAction.add(mnuExecQuery);
	mnuAction.addSeparator();
// BUGFIX. Accessibility fixes
	mnuAction.add(mnuNamespace);
	mnuAction.add(mnuProperties);
	mnuAction.add(mnuMethods);
	mnuAction.addSeparator();
//
	mnuAction.add(mnuRefresh);
	mnuAction.add(mnuClearEventOutput);
	mnuHelp.add(mnuHelpAbout);

    }


    /**
     * creates buttons and controls that go onto tool bar
     */
    private void createToolBar(JPanel pane) {

	ToolBarPanel toolbar = new ToolBarPanel();


	toolbar.createButton(Util.loadImageIcon("new_host.gif"), 
			     I18N.loadString("TIP_HOSTNAME"),
			     "hostDialog", treePane);

	cbModel = new DefaultComboBoxModel();
	nameSpaceList = new JComboBox(cbModel);
	nameSpaceList.addItemListener(this);
	nameSpaceList.setEditable(false);

	toolbar.addComponent(nameSpaceList);
	toolbar.createButton(Util.loadImageIcon("chng_namespace.gif"), 
			     I18N.loadString("TIP_NAMESPACE"), 
			     "CHANGE_NAMESPACE", this);


	toolbar.createButton(Util.loadImageIcon("find_class.gif"),
			     I18N.loadString("TIP_FIND_CLASS"), 
			     "FIND_CLASS", treePane);

	btnAddClass = toolbar.createButton(Util.loadImageIcon("addclass.gif"),
			     I18N.loadString("TIP_ADD_CLASS"), 
			     "ADD_CLASS", treePane);

	btnInstances = toolbar.createButton(Util.loadImageIcon("instances.gif"),
			     I18N.loadString("TIP_INSTANCES"),
			     "INSTANCES", treePane);

// BUGFIX. Accessibility fixes
	btnQualifiers = toolbar.createButton(Util.loadImageIcon(
			     "qualifiers.gif"), 
			     I18N.loadString("TIP_CLASS_QUALIFIERS"),
			     "QUALIFIERS", treePane);

	toolbar.createButton(Util.loadImageIcon("refresh.gif"), 
			     I18N.loadString("TIP_REFRESH_CLASS"), 
			     "REFRESH", treePane);

	pane.add("North", toolbar);

    }

    /**
     * gets called when ItemEvent happens.  This will happen when either
     * a) namespace comboBox has selection change or b) NameSpaceDialog sends
     * ItemsEvent 
     */
    public void itemStateChanged(ItemEvent e) {
	// event "select name space" get sent from NameSpaceDialog when
	// new namespace is selected
	if (e.getItem() == "select name space") {
	    populateNameSpaceComboBox();
	    nsDlg.setVisible(false);
	    setSelectedNameSpace(nsDlg.getSelectedNameSpace());
	    nsDlg.dispose();
	    Util.waitOff(this);
	// event "dialog cancel when user cancels out of NameSpaceDialog
	// still need to populate combobox because user could have added
	// or deleted namespaces in the dialog
	} else if (e.getItem().equals("dialog cancel")) {
	    populateNameSpaceComboBox();
	    Util.waitOff(this);
	    // ItemEvent.SELECTED get sent when namespace is selected in  
	    // the namespace comboBox	    
	} else if (e.getStateChange() == ItemEvent.SELECTED) {
	    setSelectedNameSpace((String)e.getItem());
	}	    
    }

    /**
     * will change namespace selection in namespace combo box
     *
     * @param nSpace namespace to select in combo box
     */
    public void setSelectedNameSpace(String nSpace) {
	// don't select new namespace if in process of populating namespace 
	// combo box (getting around bug where selection event happens when 
	// adding new items to combobox model)
	if (repopulating) {
	    return;
	}
	// make sure we have a valid namespace
	CIMClient cc= CIMClientObject.changeNameSpace("");
	try {
	    cc.enumQualifierTypes(new CIMObjectPath("", "/" + nSpace));
	} catch (CIMException exc) {
	    if (exc.getID().equals("CIM_ERR_INVALID_NAMESPACE")) {
		JOptionPane.showMessageDialog(this, 
	            I18N.loadStringFormat("ERR_INVALID_NAMESPACE", nSpace),
		    I18N.loadString("TTL_CIM_ERROR"), 
	            JOptionPane.ERROR_MESSAGE);
	    }

	    // if invalid namespace, re-populate combo box and then select 
	    // previous name space
	    populateNameSpaceComboBox();
	    nSpace = prevNameSpace;
	}

	if (!(prevNameSpace.equals(nSpace))) {
	    // change namespace in CIMClientObject
	    if (CIMClientObject.initialize(nSpace)) {
		treePane.initializeClassTree();
	    }
	}

	prevNameSpace = nSpace;
	cbModel.setSelectedItem(nSpace);
    }

    /**
     * initializes listener to listen for CIM Events
     */
    public void initializeEventListener() {
	// before we initialize event listener with new cimClient, delete old
	// events (if any) with old cimClient.
	tablePane.deleteEventSubscriptions();
	CIMClient cc = CIMClientObject.getClient();

	// if we cannot get CIM_Indication filter, create CIMListener 
	// or create indiaction handler get assume namespace doesn't 
	// support events.
	try {
	    CIMClass cimFilter = cc.getClass(new CIMObjectPath(
	        "CIM_IndicationFilter"), true, true, true, null);
	    cc.addCIMListener(
	        new CIMListener() {
		    public void indicationOccured(CIMEvent e) {
			String event = e.getIndication().toString();
			eventOutputArea.append(event + "\n");
		    }
		});
	    CIMInstance ci = cc.getIndicationListener(null);
	    CIMObjectPath iHandler = cc.createInstance(new CIMObjectPath(),
						       ci);
	    tablePane.initializeEvents(iHandler, cc);
	} catch (CIMException e) {
	    tablePane.initializeEvents(null, cc);
	    return;
	}	
    }

    /**
     * called when new Action Event happens
     */
    public void actionPerformed(ActionEvent e) {
	String action = e.getActionCommand();
	// show dialog that allows user to select new namespace
	if (action.equals("CHANGE_NAMESPACE")) {
	    Util.waitOn(this);
	    nsDlg = new NameSpaceDialog(this);
	    nsDlg.addItemListener(this);
	    nsDlg.setVisible(true);
	// show about dialog
	} else if (action.equals("ABOUT_CIMWORKSHOP")) {
	    Util.setWaitCursor(this);
	    String versionString = Version.major + "." + Version.minor + "." +
		    Version.revision + " " + Version.buildID;
	    AboutDialog dlg = new AboutDialog(this, 
		Version.productName, versionString,
		Version.copyright, null);
	    Util.setDefaultCursor(this);
	} else if (action.equals("CLEAR_EVENT_OUTPUT")) {
	    eventOutputArea.setText("");
	} else if (action.equals("EXIT")) {
	    exitCIMWorkshop();
	}
		
    }


    /**
     * if window is closing, exit cimworkshop
     */
    public void windowClosing(WindowEvent evt) {
	exitCIMWorkshop();
    }

    /**
     * performs cleanup and disposes of frame
     */
    private void exitCIMWorkshop() {
	if (nsDlg != null) {
	    nsDlg.dispose();
	}
	// remove any event subscriptions done in this session
	tablePane.deleteEventSubscriptions();
	dispose();
	System.exit(0);
    }

    /**
     * when window opens, set divider position between class info and 
     * event output
     */
    public void windowOpened(WindowEvent evt) {
	fullPanel.setDividerLocation(0.8);
    }

    public void windowClosed(WindowEvent evt) {
    }

    public void windowDeiconified(WindowEvent evt) {
    }

    public void windowActivated(WindowEvent evt) {
    }

    public void windowIconified(WindowEvent evt) {
    }

    public void windowDeactivated(WindowEvent evt) {
    }
}
