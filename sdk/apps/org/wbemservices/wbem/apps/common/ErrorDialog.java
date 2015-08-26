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

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * @version 	1.5, 12/19/00
 * @author  Sun Microsystems, Inc.
 */

public class ErrorDialog {
	private JDialog dialog;
	private String title = I18N.loadString("TTL_ERROR",
	    "org.wbemservices.wbem.apps.common.common");
	private final static ImageIcon errorIcon = Util.loadImageIcon(
	    "log-error.gif");
	private final static ImageIcon infoIcon = Util.loadImageIcon(
	    "log-info.gif");
	private JButton cancelBtn = new JButton(I18N.loadString("LBL_OK",
	    "org.wbemservices.wbem.apps.common.common"));
	private final static Dimension screenSize = 
	    Toolkit.getDefaultToolkit().getScreenSize();

	/**
	* Constructor to create ErrorDialog with an OK button
	* and default title of Error. NOTE: This is not suited
	* for Information dialogs.
	*
	* @param  parent parent
	* @param  errorMessage to display message 
	*/
	public ErrorDialog(JFrame parent, String errorMessage) {

		createErrorDialog(parent, title, errorMessage, false);
	}

	/**
	* Constructor to create ErrorDialog with an OK button
	*
	* @param  parent  parent
	* @param  title String for the title of the dialog 
	* @param  errorMessage 
	*/
	public ErrorDialog(JFrame parent, String title, String errorMessage) {

		createErrorDialog(parent, title, errorMessage, false);
	}

	/**
	* Constructor to create ErrorDialog with an OK button
	* to be used as an Information dialog. (i.e. Info icon)
	*
	* @param parent		parent
	* @param title		String for the title of the dialog 
	* @param errorMessage 
	* @param infoDlg	whether to use the infoIcon
	*/
	public ErrorDialog(JFrame parent, String title, String errorMessage, 
			   boolean infoDlg) {
		createErrorDialog(parent, title, errorMessage, infoDlg);
	}

	private void createErrorDialog(JFrame parent, String title, 
				       String errorMessage, 
				       boolean infoDlg) {

		// create the JDialog with the text in a FlowArea,
		// and a Cancel button to dispose.
		dialog = new JDialog(parent, title, true);
		GridBagLayout bag = new GridBagLayout();
		JPanel errorPanel = new JPanel();
		errorPanel.setLayout(bag);

		// add key listener
		SpecialKeyListener specialKeyListener = new 
							SpecialKeyListener();

		dialog.addKeyListener(specialKeyListener);

		// add the icon
		JPanel iconPanel = new JPanel();
		iconPanel.setLayout(bag);
		Constraints.constrain(errorPanel, iconPanel, 0, 0, 1, 2, 
				      GridBagConstraints.VERTICAL,
				      GridBagConstraints.WEST, 0.0, 1.0, 5, 
				      5, 5, 0);
		JLabel iconLbl = getIconLabel(infoDlg);
		Constraints.constrain(iconPanel, iconLbl, 0, 0, 1, 1, 
				      GridBagConstraints.NONE,
				      GridBagConstraints.WEST, 0.0, 0.0,
				      5, 5, 5, 5);

		JPanel invisiblePanel = new JPanel();
		Constraints.constrain(iconPanel, invisiblePanel, 0, 1, 1, 1, 
				      GridBagConstraints.VERTICAL,
				      GridBagConstraints.WEST, 0.0, 1.0, 0,
				      0, 0, 0);

		// add the text
		JPanel flowPanel = new JPanel();
		flowPanel.setLayout(bag);
		Constraints.constrain(errorPanel, flowPanel, 1, 0, 1, 1, 
				      GridBagConstraints.BOTH,
				      GridBagConstraints.WEST, 1.0, 1.0, 
				      0, 0, 0, 0);
		FlowArea flowArea = new FlowArea(errorMessage, 30, 
					         Constants.ERROR_DIALOG_FONT);
		flowArea.setSize(flowArea.getPreferredSize());
		Constraints.constrain(flowPanel, flowArea, 0, 0, 1, 1, 
				      GridBagConstraints.BOTH,
				      GridBagConstraints.WEST, 1.0, 1.0, 
				      20, 10, 20, 20);

		// add the buttons
		JPanel btnPanel = new JPanel();
		btnPanel.setLayout(bag);
		Constraints.constrain(errorPanel, btnPanel, 1, 1, 1, 1, 
				      GridBagConstraints.HORIZONTAL,
				      GridBagConstraints.WEST, 1.0, 0.0,
				      5, 5, 5, 5);

		invisiblePanel = new JPanel();
		Constraints.constrain(btnPanel, invisiblePanel, 0, 0, 1, 
				      1, GridBagConstraints.HORIZONTAL,
				      GridBagConstraints.WEST, 1.0, 0.0,
				      0, 0, 0, 0);

		cancelBtn.addActionListener(new CancelBtnListener());
		Constraints.constrain(btnPanel, cancelBtn, 1, 0, 1, 1, 
				      GridBagConstraints.NONE,
				      GridBagConstraints.WEST, 0.0, 0.0, 
				      5, 5, 5, 5);

		dialog.getContentPane().setLayout(bag);
		Constraints.constrain(dialog.getContentPane(), errorPanel, 
				      0, 0, 1, 1, GridBagConstraints.BOTH,
				      GridBagConstraints.CENTER, 1.0, 1.0,
				      0, 0, 0, 0);

		// This is a work-around for a Swing bug
		// flowArea.setSize(faDim);
		dialog.pack();
		dialog.setResizable(false);
		
		centerDialog();
		cancelBtn.requestFocus();
		dialog.setVisible(true);

	} // end of createErrorDialog

	private JLabel getIconLabel(boolean infoDlg) {
		JLabel iconLbl;
		if (infoDlg) {
			iconLbl = new JLabel(infoIcon);
		} else {
			iconLbl = new JLabel(errorIcon);
		}

		return iconLbl;
	}

	/**
	 * Centers the dialog in the screen
	 */
	public void centerDialog() {
		dialog.setLocation(screenSize.width/2 - 
				   dialog.getSize().width/2,
				   screenSize.height/2 - 
				   dialog.getSize().height/2);
	} // end centerDialog

	/**
	 * Listener for the Cancel button
	 */
	protected class CancelBtnListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			dialog.dispose();
		} // end actionPerformed
	} // class CancelButtonListener

	/**
	 * Listener for <Enter> and <Esc> keys
	 */
	protected class SpecialKeyListener extends KeyAdapter {
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				cancelBtn.doClick();
			} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				dialog.dispose();
			}
		} // end keyPressed
	} // end class SpecialKeyListener


} // ErrorDialog
