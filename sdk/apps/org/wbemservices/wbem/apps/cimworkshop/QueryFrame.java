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

import javax.wbem.client.CIMClient;

import org.wbemservices.wbem.apps.common.ActionString;
import org.wbemservices.wbem.apps.common.I18N;
import org.wbemservices.wbem.apps.common.ToolBarPanel;
import org.wbemservices.wbem.apps.common.Util;

/**
 * 
 *
 * @author 	Sun Microsystems
 */

public class QueryFrame extends JFrame implements ActionListener, 
   WindowListener {

    private CIMTableTabbedPane tablePane;
    QueryActionPanel queryPanel;
    JSplitPane desktop;
// BUGFIX. Accessibility fixes
    private JMenuItem mnuRefresh, mnuExit;

    public QueryFrame(Frame parent, CIMClient cimClient, String query) {
	super();
	
	setTitle(I18N.loadString("TTL_EXEC_QUERY"));
	JPanel pane = (JPanel)getContentPane();
	pane.setLayout(new BorderLayout());
	desktop = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
	tablePane = new CIMTableTabbedPane();
	queryPanel = new QueryActionPanel(tablePane, this, query); 

	queryPanel.setPreferredSize(new Dimension(300, 200));
	tablePane.setPreferredSize(new Dimension(300, 200));
	desktop.setLeftComponent(queryPanel);
	desktop.setRightComponent(tablePane);
	pane.add("Center", desktop);
	createMenuBar();
	createToolBar(pane);
	queryPanel.refreshInstanceList();
	addWindowListener(this);
	setBounds(100, 100, 600, 400);
	show();
    }


    public void actionPerformed(ActionEvent e) {
	String actionCmd = e.getActionCommand();
	if (actionCmd.equals("EXIT")) {
	    dispose();
	}
    }

    private void createMenuBar() {
	JMenuBar menuBar = new JMenuBar();
	setJMenuBar(menuBar);
	ActionString asAction = new ActionString("MNU_ACTION", 
			      "org.wbemservices.wbem.apps.common.common");
	ActionString asRefresh = new ActionString("MNU_REFRESH",
			      "org.wbemservices.wbem.apps.common.common");
// BUGFIX. Accessibility fixes
	ActionString asExit = new ActionString("MNU_EXIT",
			      "com.sun.wbem.apps.common.common");

	JMenu mnuAction = new JMenu(asAction.getString());
	mnuAction.setMnemonic(asAction.getMnemonic());

	menuBar.add(mnuAction);

	mnuRefresh = new JMenuItem(asRefresh.getString());
	mnuRefresh.setMnemonic(asRefresh.getMnemonic());
	mnuRefresh.setActionCommand("REFRESH");
	mnuRefresh.addActionListener(queryPanel);

// BUGFIX. Accessibility fixes
	mnuExit = new JMenuItem(asExit.getString());
	mnuExit.setMnemonic(asExit.getMnemonic());
	mnuExit.setActionCommand("EXIT");
	mnuExit.addActionListener(this);

	mnuAction.add(mnuRefresh);
	mnuAction.add(mnuExit);
//
    }


    private void createToolBar(JPanel pane) {

	ToolBarPanel toolbar = new ToolBarPanel();

	toolbar.createButton(Util.loadImageIcon("refresh.gif"), 
			     I18N.loadString("TIP_REFRESH_INSTANCES"), 
			     "REFRESH",
			     queryPanel);

	pane.add("North", toolbar);

    }


    public void windowActivated(WindowEvent evt) {}
    public void windowClosed(WindowEvent evt) {}

    public void windowClosing(WindowEvent evt) {}

    public void windowDeactivated(WindowEvent evt) {}
    public void windowDeiconified(WindowEvent evt) {}
    public void windowIconified(WindowEvent evt) {}

    // when window opens, set location of divider on splitPane
    public void windowOpened(WindowEvent evt) {
	desktop.setDividerLocation(0.4);
    }

}

