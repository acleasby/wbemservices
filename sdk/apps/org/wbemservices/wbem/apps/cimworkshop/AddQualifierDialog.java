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
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Vector;

import org.wbemservices.wbem.apps.common.ActionString;
import org.wbemservices.wbem.apps.common.ButtonPanel;
import org.wbemservices.wbem.apps.common.I18N;
import org.wbemservices.wbem.apps.common.Util;


/**
 * 
 *
 * @author 	Sun Microsystems
 */

// BUGFIX. Added WindowListener arg for accessibility fixes
public class AddQualifierDialog extends JDialog implements ActionListener,
    WindowListener {

    protected JButton ok;
    protected JButton cancel;
    protected String nameString;
    protected JPanel pane;
    protected JList list;

    public AddQualifierDialog(Frame parent, Vector qTypes) {
	super(parent, true);
	setTitle(I18N.loadString("TTL_ADD_QUALIFIER"));
	pane = (JPanel)getContentPane();
	pane.setLayout(new BorderLayout());
	JPanel topPane = new JPanel(new BorderLayout()) {
	    public Insets getInsets() {
		return new Insets(10, 10, 10, 10);
	    }
	};

	JTextArea message = new JTextArea(I18N.loadString(
					  "MSG_ADD_QUALIFIER"));
	message.setColumns(40);
	message.setLineWrap(true);
	message.setWrapStyleWord(true);
	message.setBackground(this.getBackground());
	message.setEditable(false);

	topPane.add("Center", message);

	pane.add("North", topPane);




	JPanel centerPane = new JPanel(new BorderLayout()) {
	    public Insets getInsets() {
		return new Insets(5, 5, 5, 5);
	    }
	};

	ActionString asName = new ActionString("LBL_NAME",
			       "org.wbemservices.wbem.apps.common.common");
	JLabel lName = new JLabel(asName.getString() + ":");
	lName.setDisplayedMnemonic(asName.getMnemonic());
	
	list = new JList(qTypes);
	
	JScrollPane scrollPane = new JScrollPane(list);
	list.setVisibleRowCount(7);
	list.getSelectionModel().setSelectionMode(
	    ListSelectionModel.SINGLE_SELECTION);
// BUGFIX. Accessibility fixes
	//list.setSelectedIndex(0);

	lName.setLabelFor(list);
	
	centerPane.add("North", lName);
	centerPane.add("Center", scrollPane);
	
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
	addWindowListener(this);

// BUGFIX. Accessibility fixes
	// Add the <esc> & <Enter> listeners to the dialog.
	EscKeyListener escListener = new EscKeyListener();
	EnterKeyListener enterListener = new EnterKeyListener();

	pane.registerKeyboardAction(escListener,
				    KeyStroke.getKeyStroke(
				    KeyEvent.VK_ESCAPE, 0, true),
				    JComponent.WHEN_IN_FOCUSED_WINDOW);

	pane.registerKeyboardAction(enterListener,
				    KeyStroke.getKeyStroke(
				    KeyEvent.VK_ENTER, 0, true),
				    JComponent.WHEN_IN_FOCUSED_WINDOW);
//
	pack();
	setLocation(Util.getCenterPoint(parent, this));
	setVisible(true);

    }

// BUGFIX. Accessibility fixes
    /**
     * Listener that will click the cancel button
     * when the <esc> key is typed.
     */
    protected class EscKeyListener implements ActionListener {
	public void actionPerformed(ActionEvent e) {
	    cancel.doClick();
	} // end actionPerformed
    } // end class EscKeyListener

    /**
     * Listener that will click the ok button
     * when the <enter> key is typed.
     */
    protected class EnterKeyListener implements ActionListener {
	public void actionPerformed(ActionEvent e) {
	    ok.doClick();
	} // end actionPerformed
    } // end class EscKeyListener
//

    public void actionPerformed(ActionEvent evt) {
	if (evt.getSource() == ok) {
	    nameString = (String)list.getSelectedValue();
	    dispose();
	} else if (evt.getSource() == cancel) {
	    nameString = null;
	    dispose();
	}
    }

    public String getName() {
	return nameString;
    }

// BUGFIX. Accessibility fixes
    public void windowActivated(WindowEvent evt) {}
    public void windowClosed(WindowEvent evt) {}
    public void windowClosing(WindowEvent evt) {}
    public void windowDeactivated(WindowEvent evt) {}
    public void windowDeiconified(WindowEvent evt) {}
    public void windowIconified(WindowEvent evt) {}

    // when window opens, request focus on list
    public void windowOpened(WindowEvent evt) {
	list.requestFocus();
       	if (list.getModel().getSize() > 0) {
	  list.setSelectedIndex(0);
	}
	    
    }

}


