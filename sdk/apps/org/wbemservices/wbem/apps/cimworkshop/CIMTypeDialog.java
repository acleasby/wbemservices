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

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import org.wbemservices.wbem.apps.common.ButtonPanel;
import org.wbemservices.wbem.apps.common.I18N;

/**
 * 
 *
 * @version 	1.11, 08/16/01
 * @author 	Sun Microsystems
 */

public class CIMTypeDialog extends JDialog implements ActionListener {

    protected JButton ok;
    protected JButton cancel;
    protected String typeString;
    protected JPanel pane;
    protected JTextArea msg;
    protected JList list;

    public CIMTypeDialog(Frame frame, String currentType) {
	super(frame, true);
	pane = (JPanel)getContentPane();
	pane.setLayout(new BorderLayout());

	setTitle(I18N.loadString("TTL_CIM_TYPES"));

	JPanel topPane = new JPanel(new BorderLayout()) {
	    public Insets getInsets() {
		return new Insets(5, 5, 5, 5);
	    }
	};
	
	pane.add("North", topPane);
	JPanel centerPane = new JPanel() {
	    public Insets getInsets() {
		return new Insets(5, 5, 5, 5);
	    }
	};

	CIMTypes cimTypes = new CIMTypes();
	list = new JList(cimTypes.getTypes());
	JScrollPane scrollPane = new JScrollPane(list);
	list.setVisibleRowCount(7);
	list.getSelectionModel().setSelectionMode(
	    ListSelectionModel.SINGLE_SELECTION);
	list.setSelectedValue(currentType, true);
	centerPane.add(scrollPane);
	pane.add("Center", centerPane);



	ButtonPanel buttonPanel = new ButtonPanel();
	ok = new JButton(I18N.loadString("LBL_OK",
			 "org.wbemservices.wbem.apps.common.common"));
	ok.addActionListener(this);
	cancel = new JButton(I18N.loadString("LBL_CANCEL",
			     "org.wbemservices.wbem.apps.common.common"));
	cancel.addActionListener(this);
	buttonPanel.addButton(ok);
	buttonPanel.addButton(cancel);
	pane.add("South", buttonPanel);
    	pack();
	setVisible(true);
    }


    public void actionPerformed(ActionEvent evt) {
	if (evt.getSource() == ok) {
	    typeString = (String)list.getSelectedValue();
	    dispose();
	} else if (evt.getSource() == cancel) {
	    typeString = null;
	    dispose();
	}
    }


    public String getSelectedType() {
	return typeString;
    }

}
