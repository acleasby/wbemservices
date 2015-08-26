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

package org.wbemservices.wbem.apps.common;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

/**
 * 
 *
 * @author 	Sun Microsystems
 */

public class AboutDialog extends JDialog implements ActionListener {

// BUGFIX. Accessibility fixes
    JButton ok;

    public AboutDialog(Frame frame, String productName, String productVersion,
		       String copyright, String iconName) {
	super(frame, true);


	setTitle(I18N.loadStringFormat("LBL_ABOUT", productName));
	JPanel pane = (JPanel)getContentPane();
	pane.setLayout(new BorderLayout());
	DlgTextArea productNameArea = new DlgTextArea(this, productName);
	DlgTextArea productVersionArea = new DlgTextArea(this, productVersion);
	DlgTextArea copyrightArea = new DlgTextArea(this, copyright);
	JPanel infoPanel = new JPanel(new ColumnLayout(LAYOUT_ALIGNMENT.LEFT));
	infoPanel.add(productNameArea);
	infoPanel.add(copyrightArea);
	infoPanel.add(productVersionArea);

	JPanel buttonPanel = new JPanel();
// BUGFIX. Accessibility fixes
	ok = new JButton(I18N.loadString("LBL_OK",
					 "org.wbemservices.wbem.apps.common.common"));
	ok.setActionCommand("ok");
	ok.addActionListener(this);
	buttonPanel.add(ok);
	pane.add("Center", infoPanel);
	pane.add("South", buttonPanel);

	if ((iconName != null) && (iconName.trim().length() > 0)) {
	    try {
		ImageIcon productIcon = Util.loadImageIcon(iconName);
		pane.add("West", new JLabel(productIcon));
	    } catch (Exception e) {
		// ignore, just don't have icon
	    }
	}

	// Add the <esc> & <Enter> listeners to the dialog.
	DisposeListener OKListener = new DisposeListener();

// BUGFIX. Accessibility fixes
	pane.registerKeyboardAction(OKListener,
				    KeyStroke.getKeyStroke(
				    KeyEvent.VK_ESCAPE, 0, true),
				    JComponent.WHEN_IN_FOCUSED_WINDOW);

	pane.registerKeyboardAction(OKListener,
				    KeyStroke.getKeyStroke(
				    KeyEvent.VK_ENTER, 0, true),
				    JComponent.WHEN_IN_FOCUSED_WINDOW);
	
	//getRootPane().setDefaultButton(ok);
//
	pack();
	Util.positionWindow(this, frame);
	setVisible(true);
    }

// BUGFIX. Accessibility fixes
    /**
     * Listener that will dispose of the dialog
     */
    protected class DisposeListener implements ActionListener {
	public void actionPerformed(ActionEvent e) {
	    dispose();
	} // end actionPerformed
    } // end class DisposeListener
//

    public void actionPerformed(ActionEvent e) {
	String actionCmd = e.getActionCommand();
	if (actionCmd.equals("ok")) {
	    dispose();
	}
    }

    public class DlgTextArea extends JTextArea {
	public DlgTextArea(JDialog dlg, String text) {
	    super(text);
	    setEditable(false);
	    setBackground(dlg.getBackground());
	}

	public Insets getInsets() {
	    return new Insets(5, 5, 5, 5);
	}

	

    }
}


