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
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.wbem.cim.CIMClass;
import javax.wbem.client.CIMClient;

import org.wbemservices.wbem.apps.common.AboutDialog;
import org.wbemservices.wbem.apps.common.ActionString;
import org.wbemservices.wbem.apps.common.CIMClientObject;
import org.wbemservices.wbem.apps.common.I18N;
import org.wbemservices.wbem.apps.common.ToolBarPanel;
import org.wbemservices.wbem.apps.common.Util;

/**
 * 
 * @version 	%I%, %G%
 * @author 	Sun Microsystems
 */


/**
 * This class is the frame for the Instance Editor.  It will contain a split
 * plane with a list of instances on the left and the properties and methods
 * on the right.  It will also contain a menu bar and tool bar that allows
 * the creation, deletion and modification of instances. 
 *
 */
public class InstanceFrame extends JFrame implements WindowListener,
    ActionListener {

    // table pane is a tabbed pane that contains a table of properties
    // and list of methods for and instance
    private CIMTableTabbedPane tablePane;
    // instance panel contains a list of instances
    InstanceActionPanel instancePanel;
    // desktop is a split pane with instancePanel on left and tablePane on right
    private JSplitPane desktop;
    private JButton btnDeleteInstance;
    private JButton btnSaveInstance;
    private JButton btnAddInstance;

    private JMenu mnuInstanceEditor;
    private JMenu mnuAction;
    private JMenu mnuProperties;
    private JMenu mnuMethods;    
    private JMenu mnuHelp;

    private JMenuItem mnuExit;
    private JMenuItem mnuAddInstance;
    private JMenuItem mnuDeleteInstance;
    private JMenuItem mnuSaveInstance;
    private JMenuItem mnuAssocTraversal;
    private JMenuItem mnuRefresh;
    private JMenuItem mnuPropQualifiers;
    private JMenuItem mnuShowValue;
    private JMenuItem mnuMethodQualifiers;
    private JMenuItem mnuInvokeMethod;
    private JMenuItem mnuHelpAbout;

    /**
     * InstanceFrame constructor
     * will show shallow enumertion of instances for a particular class
     *
     * @param parent     Frame which shows this frame
     * @param cimClient  A CIMClient object
     * @param cimClass   CIMClass for which to enumerate instances of
     */
    public InstanceFrame(Frame parent, CIMClient cimClient, 
			  CIMClass cimClass) {
	this(parent, cimClient, cimClass, false);
    }

    /**
     * InstanceFrame constructor
     * will show enumertion of instances for a particular class
     *
     * @param parent     Frame which shows this frame
     * @param cimClient  A CIMClient object
     * @param cimClass   CIMClass for which to enumerate instances of
     * @param deep       if TRUE, do deep enumeration otherwise do shallow   
     */
    public InstanceFrame(Frame parent, CIMClient cimClient, 
			  CIMClass cimClass, boolean deep) {
	super();
	setTitle(I18N.loadStringFormat("TTL_INSTANCES", cimClass.getName()));
	JPanel pane = (JPanel)getContentPane();
	pane.setLayout(new BorderLayout());
	desktop = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
	tablePane = new CIMTableTabbedPane();
	instancePanel = new InstanceActionPanel(tablePane, this, cimClass, 
						deep);
	instancePanel.setPreferredSize(new Dimension(300, 200));
	tablePane.setPreferredSize(new Dimension(300, 200));
	desktop.setLeftComponent(instancePanel);
	desktop.setRightComponent(tablePane);
	pane.add("Center", desktop);
	// create menu and tool bars
	createMenuBar();
	createToolBar(pane);
	// refresh instance list does the enmueration of the instances and
	// populates the list of instances
	instancePanel.refreshInstanceList();
	addWindowListener(this);
	setBounds(100, 100, 600, 400);
	show();
    }


    public void actionPerformed(ActionEvent e) {
	String actionCmd = e.getActionCommand();
	if (actionCmd.equals("EXIT")) {
	    dispose();
	} else if (actionCmd.equals("ABOUT_INSTANCE_EDITOR")) {
	    // show help-about dialog
	    ActionString asProd = new ActionString("MNU_INSTANCE_EDITOR");
	    String prod = asProd.getString();
	    String versionString = Version.major + "." + Version.minor + "." +
		    Version.revision + " " + Version.buildID;
	    AboutDialog dlg = new AboutDialog(this, prod, 
		versionString, Version.copyright, "");
	}		
    }

    /**
     * createMenuBar 
     * Creates menus for actions you can do in the instance editor
     * and adds the menus to this frame
     *
     */
    private void createMenuBar() {
	boolean writePermission = CIMClientObject.userHasWritePermission();
	JMenuBar menuBar = new JMenuBar();
	setJMenuBar(menuBar);
	ActionString asInstanceEditor = new ActionString(
	    "MNU_INSTANCE_EDITOR");
	ActionString asAction = new ActionString("MNU_ACTION", 
			      "org.wbemservices.wbem.apps.common.common");
// BUGFIX. Accessibility fixes
	ActionString asProperties = new ActionString("MNU_PROPERTIES");
	ActionString asMethods = new ActionString("MNU_METHODS");
//
	ActionString asHelp = new ActionString("MNU_HELP",
			      "org.wbemservices.wbem.apps.common.common");
	ActionString asExit = new ActionString("MNU_EXIT",
			      "org.wbemservices.wbem.apps.common.common");
	ActionString asRefresh = new ActionString("MNU_REFRESH",
			      "org.wbemservices.wbem.apps.common.common");
	ActionString asAdd = new ActionString("MNU_ADD_INSTANCE");
	ActionString asDelete = new ActionString("MNU_DELETE_INSTANCE");
	ActionString asSave = new ActionString("MNU_SAVE_INSTANCE");
	ActionString asAssoc = new ActionString("MNU_ASSOC_TRAVERSAL");
// BUGFIX. Accessibility fixes
	ActionString asQualifiers = new ActionString("MNU_QUALIFIERS");
	ActionString asShowValue = new ActionString("MNU_SHOW_VALUE");
	ActionString asInvokeMethod = new ActionString("MNU_INVOKE_METHOD");
//
	ActionString asHelpAbout = new ActionString(
			       "MNU_ABOUT_INSTANCE_EDITOR");
	
 
	mnuInstanceEditor = new JMenu(asInstanceEditor.getString());
	mnuInstanceEditor.setMnemonic(asInstanceEditor.getMnemonic());

	mnuAction = new JMenu(asAction.getString());
	mnuAction.setMnemonic(asAction.getMnemonic());
// BUGFIX. Accessibility fixes
	mnuProperties = new JMenu(asProperties.getString());
	mnuProperties.setMnemonic(asProperties.getMnemonic());

	mnuMethods = new JMenu(asMethods.getString());
	mnuMethods.setMnemonic(asMethods.getMnemonic());
//
	mnuHelp = new JMenu(asHelp.getString());
	mnuHelp.setMnemonic(asHelp.getMnemonic());

	menuBar.add(mnuInstanceEditor);
	menuBar.add(mnuAction);
	menuBar.add(mnuHelp);

	mnuExit = new JMenuItem(asExit.getString());
	mnuExit.setMnemonic(asExit.getMnemonic());
	mnuExit.setActionCommand("EXIT");
	mnuExit.addActionListener(this);

	mnuAddInstance = new JMenuItem(asAdd.getString());
	mnuAddInstance.setMnemonic(asAdd.getMnemonic());
	mnuAddInstance.setActionCommand("ADD_INSTANCE");
	mnuAddInstance.addActionListener(instancePanel);
	mnuAddInstance.setEnabled(writePermission);

	mnuAssocTraversal = new JMenuItem(asAssoc.getString());
	mnuAssocTraversal.setMnemonic(asAssoc.getMnemonic());
	mnuAssocTraversal.setActionCommand("ASSOC_TRAVERSAL");
	mnuAssocTraversal.addActionListener(instancePanel);
	mnuAssocTraversal.setEnabled(false);

	mnuDeleteInstance = new JMenuItem(asDelete.getString());
	mnuDeleteInstance.setMnemonic(asDelete.getMnemonic());
	mnuDeleteInstance.setActionCommand("DELETE_INSTANCE");
	mnuDeleteInstance.addActionListener(instancePanel);
	mnuDeleteInstance.setEnabled(false);

	mnuSaveInstance = new JMenuItem(asSave.getString());
	mnuSaveInstance.setMnemonic(asSave.getMnemonic());
	mnuSaveInstance.setActionCommand("SAVE_INSTANCE");
        mnuSaveInstance.addActionListener(instancePanel);
        mnuSaveInstance.setEnabled(false);

	mnuRefresh = new JMenuItem(asRefresh.getString());
	mnuRefresh.setMnemonic(asRefresh.getMnemonic());
	mnuRefresh.setActionCommand("REFRESH");
	mnuRefresh.addActionListener(instancePanel);

// BUGFIX. Accessibility fixes	
	mnuPropQualifiers = new JMenuItem(asQualifiers.getString());
 	mnuPropQualifiers.setMnemonic(asQualifiers.getMnemonic());
	mnuPropQualifiers.setActionCommand("QUALIFIERS");
	mnuPropQualifiers.addActionListener(tablePane.getPropertiesTable());

// BUGFIX. Accessibility fixes
	mnuShowValue = new JMenuItem(asShowValue.getString());
 	mnuShowValue.setMnemonic(asShowValue.getMnemonic());
	mnuShowValue.setActionCommand("SHOW_VALUE");
	mnuShowValue.addActionListener(tablePane.getPropertiesTable());

	mnuMethodQualifiers = new JMenuItem(asQualifiers.getString());
 	mnuMethodQualifiers.setMnemonic(asQualifiers.getMnemonic());
	mnuMethodQualifiers.setActionCommand("QUALIFIERS");
	mnuMethodQualifiers.addActionListener(tablePane.getMethodsTable());

	mnuInvokeMethod = new JMenuItem(asInvokeMethod.getString());
 	mnuInvokeMethod.setMnemonic(asInvokeMethod.getMnemonic());
	mnuInvokeMethod.setActionCommand("INVOKE_METHOD");
	mnuInvokeMethod.addActionListener(tablePane.getMethodsTable());
//
	mnuHelpAbout = new JMenuItem(asHelpAbout.getString());
	mnuHelpAbout.setMnemonic(asHelpAbout.getMnemonic());
	mnuHelpAbout.setActionCommand("ABOUT_INSTANCE_EDITOR");
	mnuHelpAbout.addActionListener(this);


	mnuInstanceEditor.add(mnuExit);

	mnuAction.add(mnuAddInstance);
	mnuAction.add(mnuDeleteInstance);
	mnuAction.add(mnuSaveInstance);
	mnuAction.addSeparator();
// BUGFIX. Accessibility fixes
	mnuAction.add(mnuProperties);
	mnuAction.add(mnuMethods);
	mnuAction.addSeparator();
//
	mnuAction.add(mnuAssocTraversal);
	mnuAction.addSeparator();
	mnuAction.add(mnuRefresh);

// BUGFIX. Accessibility fixes
	mnuProperties.add(mnuPropQualifiers);
	mnuProperties.add(mnuShowValue);
	mnuMethods.add(mnuMethodQualifiers);
	mnuMethods.add(mnuInvokeMethod);
//
	mnuHelp.add(mnuHelpAbout);

    }


    /**
     * createToolBar 
     * Creates a tool bar containing button that will perform actions 
     * you can do in the instance editor
     *
     */
    private void createToolBar(JPanel pane) {

	ToolBarPanel toolbar = new ToolBarPanel();


	btnAddInstance = toolbar.createButton(Util.loadImageIcon(
			     "instances.gif"), 
			     I18N.loadString("TIP_ADD_INSTANCE"),
			     "ADD_INSTANCE",
			     instancePanel);

	btnAddInstance.setEnabled(CIMClientObject.userHasWritePermission());


	btnDeleteInstance = toolbar.createButton(Util.loadImageIcon(
			     "delete.gif"),
			     I18N.loadString("TIP_DELETE_INSTANCE"), 
			     "DELETE_INSTANCE", 
			     instancePanel);
	btnDeleteInstance.setEnabled(false);

	btnSaveInstance = toolbar.createButton(Util.loadImageIcon("save.gif"),
			     I18N.loadString("TIP_SAVE_INSTANCE"), 
			     "SAVE_INSTANCE", 
			     instancePanel);
	btnSaveInstance.setEnabled(false);

	toolbar.createButton(Util.loadImageIcon("refresh.gif"), 
			     I18N.loadString("TIP_REFRESH_INSTANCES"), 
			     "REFRESH",
			     instancePanel);

	pane.add("North", toolbar);

    }

    public void windowActivated(WindowEvent evt) {}
    public void windowClosed(WindowEvent evt) {}

    // if editing instance, save it
    public void windowClosing(WindowEvent evt) {
	if (btnSaveInstance.isEnabled()) {
	    instancePanel.saveCurrentInstance(true);
	}
    }

    public void windowDeactivated(WindowEvent evt) {}
    public void windowDeiconified(WindowEvent evt) {}
    public void windowIconified(WindowEvent evt) {}

    // when window opens, set location of divider on splitPane
    public void windowOpened(WindowEvent evt) {
	desktop.setDividerLocation(0.4);
    }

    /**
     * Will enable/disable the button and menu that allow an instance to 
     * be deleted.  This is determined by whether the current user has 
     * write priveleges to the current namespace
     */
    public void enableDeleteInstanceMenu(boolean b) {
	mnuDeleteInstance.setEnabled(b);
	btnDeleteInstance.setEnabled(b);
    }

    /**
     * Will enable/disable the button and menu that allow an instance to 
     * be saved.  This is determined by whether an instance has been
     * modified but not saved.
     *
     */
    public void enableSaveInstanceMenu(boolean b) {
	mnuSaveInstance.setEnabled(b);
	btnSaveInstance.setEnabled(b);
    }

    /**
     * Will enable/disable the Association Traversal menu.  This will be
     * determined by whether the current instance has any references
     *
     */
    public void enableAssocMenu(boolean b) {
	mnuAssocTraversal.setEnabled(b);
    }

// BUGFIX. Accessibility fixes

    /**
     * Will get called every time the instance list gets updated
     *
     */
    public void enablePropMenu(boolean haveInstances) {
	boolean propSelected = haveInstances && tablePane.isPropertySelected();
	boolean methodSelected = haveInstances && tablePane.isMethodSelected();
	mnuPropQualifiers.setEnabled(propSelected);
	mnuShowValue.setEnabled(propSelected);
	mnuMethodQualifiers.setEnabled(methodSelected);
	mnuInvokeMethod.setEnabled(methodSelected);
    }

} 

