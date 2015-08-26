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
import javax.swing.event.*;

import java.awt.*;
import java.awt.event.*;

import org.wbemservices.wbem.apps.common.*;

/**
 * 
 * @author 	Sun Microsystems
 */

public class QueryInputDialog extends AdminDialog implements DocumentListener {

    private JButton execBtn, cancelBtn;
    private GenInfoPanel infoPanel;
    String queryString = null;
    JTextArea textArea;

    public QueryInputDialog(Frame parent) {
	super(parent, I18N.loadString("TTL_QUERY_STRING"), false);

	infoPanel = super.getInfoPanel();

	cancelBtn = this.getCancelBtn();
	cancelBtn.addActionListener(new OKCancelButtonListener());
	execBtn = this.getOKBtn();
	execBtn.setText(I18N.loadString("LBL_EXECUTE"));
	execBtn.addActionListener(new OKCancelButtonListener());

	JPanel mainPanel = getRightPanel();	
	mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 15, 15, 15));
	mainPanel.setLayout(new ColumnLayout(LAYOUT_ALIGNMENT.EXPAND));

	ActionString asQueryString = new ActionString("LBL_QUERY_STRING");
	JLabel lQueryString = new JLabel(asQueryString.getString() + ":");
	lQueryString.setDisplayedMnemonic(asQueryString.getMnemonic());

	textArea = new JTextArea(4, 25);
	textArea.setWrapStyleWord(true);

	lQueryString.setLabelFor(textArea);
	textArea.getDocument().addDocumentListener(this);
	textArea.addFocusListener(new ContextHelpListener(getInfoPanel(),
				  "cimworkshop", "QueryString_010.htm"));

	this.setDefaultFocusListener(new ContextHelpListener(
				     infoPanel, "cimworkshop", 
				     "QueryString_000.htm"), true);
	execBtn.addFocusListener(new ContextHelpListener(infoPanel, 
			         "cimworkshop", "QueryString_000.htm"));
	cancelBtn.addFocusListener(new ContextHelpListener(infoPanel, 
				   "cimworkshop", "QueryString_000.htm"));

	mainPanel.add(lQueryString);
	mainPanel.add(new JScrollPane(textArea));
	
	pack();
	setLocation(Util.getCenterPoint(parent, this));
	setVisible(true);	
    }

    public void okClicked() {	
	queryString = textArea.getText();
	dispose();
    }
    
    public void cancelClicked() {
	queryString = null;
    	dispose();
    }
    
    class OKCancelButtonListener implements ActionListener {
	public void actionPerformed(ActionEvent e) {
	    if (e.getSource() == cancelBtn) { // cancel button
		cancelClicked();
	    } else if (e.getSource() == execBtn) { // OK button
		okClicked();
	    }
	}
    }
    
    public void changedUpdate(DocumentEvent evt) {
	setOKEnabled();
    }

    public void insertUpdate(DocumentEvent evt) {
	setOKEnabled();
    }

    public void removeUpdate(DocumentEvent evt) {
	setOKEnabled();
    }

    private void setOKEnabled() {
	execBtn.setEnabled(textArea.getText().trim().length() != 0);
    }

    public String getQueryString() {
	return queryString;
    }

// BUGFIX. Accessibility fixes
    // when window opens, request focus on text area
    public void windowOpened(WindowEvent evt) {
	super.windowOpened(evt);
	textArea.requestFocus();
    }
    
}

