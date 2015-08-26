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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import org.wbemservices.wbem.apps.common.ActionString;
import org.wbemservices.wbem.apps.common.AdminDialog;
import org.wbemservices.wbem.apps.common.ContextHelpListener;
import org.wbemservices.wbem.apps.common.GenInfoPanel;
import org.wbemservices.wbem.apps.common.I18N;

import javax.wbem.cim.CIMObjectPath;
import javax.wbem.client.CIMClient;

/**
 * 
 *
 * @author 	Sun Microsystems
 */

public class ReferenceTraversalDialog extends AdminDialog  {

    private JButton okBtn;
    private GenInfoPanel infoPanel;
// BUGFIX. Accessibility fixes
    private ReferenceTree tree;

    private JPanel mainPanel;    

    public ReferenceTraversalDialog(Frame parent, CIMObjectPath op,
				    CIMClient cimClient) {
	super(parent, I18N.loadString("TTL_ASSOC_TRAVERSAL"), false);

	infoPanel = super.getInfoPanel();

	okBtn = this.getOKBtn();
	okBtn.addActionListener(new OKCancelButtonListener());

	JPanel btnPanel = getbuttonPanel();

	btnPanel.removeAll();
	btnPanel.add(okBtn);
	mainPanel = getRightPanel();	
	mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 15, 15, 15));
// BUGFIX. Accessibility fixes
	mainPanel.setLayout(new BorderLayout());

	ActionString asAssoc = new ActionString("LBL_ASSOCIATIONS");
	JLabel lAssoc = new JLabel(asAssoc.getString() + ":");
	lAssoc.setDisplayedMnemonic(asAssoc.getMnemonic());

	tree = new ReferenceTree(op, cimClient);
//
	tree.setPreferredSize(new Dimension(300, 200));
	this.setDefaultFocusListener(new ContextHelpListener(
		infoPanel, "cimworkshop", "AssocTraversal_000.htm"), true);	
	
// BUGFIX. Accessibility fixes
	lAssoc.setLabelFor(tree.getTree());
	lAssoc.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

	mainPanel.add("North", lAssoc);
	mainPanel.add("Center", tree);
//
	pack();
	setVisible(true);
    }

// BUGFIX. Accessibility fixes
    // when window opens, request focus on tree and select root node
    public void windowOpened(WindowEvent evt) {
	super.windowOpened(evt);
	JTree rTree = tree.getTree();
	rTree.requestFocus();
	rTree.setSelectionRow(0);
    }

    class OKCancelButtonListener implements ActionListener {
	public void actionPerformed(ActionEvent e) {
	    if (e.getSource() == okBtn) { // OK button
		dispose();
	    }
	}
    }

}

