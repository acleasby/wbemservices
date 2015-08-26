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
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

/**
 * 
 *
 * @version 	1.6, 08/16/01
 * @author 	Sun Microsystems
 */

public class ClassSelectionDialog extends AdminDialog implements 
    TreeSelectionListener {

    private JButton okBtn, cancelBtn, findBtn;
    protected String className = "";
    private JPanel mainPanel;  
    private ClassTreePane treePane;

    public ClassSelectionDialog(Frame parent) {
	super(parent, I18N.loadString("TTL_CLASS_SELECTION"), false);

	treePane = new ClassTreePane();

	treePane.addTreeSelectionListener(this);

	okBtn = this.getOKBtn();
	okBtn.addActionListener(new OKCancelButtonListener());

	cancelBtn = this.getCancelBtn();
	cancelBtn.addActionListener(new OKCancelButtonListener());

	ActionString asFind = new ActionString("TIP_FIND_CLASS");
	findBtn = new JButton(asFind.getString());
	findBtn.setMnemonic(asFind.getMnemonic());
	findBtn.setActionCommand("FIND_CLASS");
        findBtn.addActionListener(treePane);	

	JPanel btnPanel = this.getbuttonPanel();
	btnPanel.add(findBtn);

	mainPanel = getRightPanel();	
	mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 15, 15, 15));
	mainPanel.setLayout(new ColumnLayout(LAYOUT_ALIGNMENT.EXPAND));

	mainPanel.add(treePane);

	GenInfoPanel infoPanel = this.getInfoPanel();		
	this.setDefaultFocusListener(new ContextHelpListener(
		infoPanel, "cimworkshop", "ClassSelection_000.htm"), true);
	
	okBtn.setEnabled(!treePane.isRootSelected());

	treePane.refreshTree();
	pack();
	setLocation(Util.getCenterPoint(parent, this));
	setVisible(true);

    }

    public void okClicked() {	
	className = treePane.getSelectedNodeString();
	if (className == null) {
	    JOptionPane.showMessageDialog(this, I18N.loadString(
					  "ERR_NO_CLASS_SELECTED"), 
					  I18N.loadString("TTL_CIM_ERROR"), 
					  JOptionPane.ERROR_MESSAGE);
	    return;
	}
	dispose();
    }
    
    public void cancelClicked() {
	className = "";
    	dispose();
    }
    
    class OKCancelButtonListener implements ActionListener {
	public void actionPerformed(ActionEvent e) {
	    if (e.getSource() == cancelBtn) { // cancel button
		cancelClicked();
	    } else if (e.getSource() == okBtn) { // OK button
		okClicked();
	    }
	}
    }
    
    public void valueChanged(TreeSelectionEvent e) {
	okBtn.setEnabled(!treePane.isRootSelected());
    }

    public String getClassName() {
	return className;
    }

}

