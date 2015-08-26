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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.*;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * AdminDialog class is derived from JDialog and provides you
 * with a GenInfoPanel on the left, and OK and Cancel buttons at
 * the bottom.  This class also decides the size of the right hand
 * side pane, and left hand side info panel, and the overall size of
 * the dialog. 
 *
 * @author	Sun Microsystems, Inc.
 */

public class AdminDialog extends JDialog implements ActionListener,
    WindowListener {

    private JButton		okBtn, cancelBtn, applyBtn = null;
    private JPanel		rootPanel, leftPanel, rightPanel;
    private GenInfoPanel	infoPanel;
    protected AdminDialog	adminDlg;
    protected boolean	hidden;
    protected JPanel	buttonPanel;
    public final static Border emptyBorder5 = new EmptyBorder(5, 5, 5, 5);
    private Dimension	dlgSizeHelp, dlgSizeNoHelp;
    private boolean		cancelBtnHasFocus = false;


    /** 
     * Creates the generic dialog, with embedded infopanel, an OK button, a
     * a Cancel button, and an optional Apply button.  If need for the Apply
     * button is based on conditions not known until after initialization,
     * allow the constructor to create it, and then use the getButtonPanel
     * and getApplyBtn methods to remove the button from the panel.
     *
     * @param	frame		the parent frame of the dialog,
     * @param	title		the dialog title
     * @param	showApply	if true, creates an Apply button 
     *				between the OK and Cancel buttons
     */
    public AdminDialog(Frame frame, String title, boolean showApply) {

	super(frame, title, true);
	adminDlg = this;
	getContentPane().setLayout(new BorderLayout());
	rootPanel = new JPanel();
	rootPanel.setLayout(new BorderLayout());

	infoPanel = new GenInfoPanel(I18N.loadString("LBL_CONTEXT_HELP",
			"org.wbemservices.wbem.apps.common.common"),
			GenInfoPanel.PROPERTY_DIALOG, this); 

	leftPanel = new JPanel();
	leftPanel.setLayout(new BorderLayout());
	leftPanel.add("Center", infoPanel);

	// Client-specific components go in right-side panel
	rightPanel = new JPanel();
	rightPanel.setBorder(emptyBorder5);
	rightPanel.setLayout(new BorderLayout());


	// Since the buttons are to be right-justified in the dialog's
	// bottom button panel, create a seperate button subpanel to 
	// layout the buttons.  Button panel contains Ok and Cancel buttons 
	// sandwiched around an optional Apply button.
	//
	buttonPanel = new JPanel();
	buttonPanel.setLayout(new GridBagLayout());

	okBtn = new JButton(I18N.loadString("LBL_OK",
			    "org.wbemservices.wbem.apps.common.common"));
	okBtn.setFocusPainted(true);
	Constraints.constrain(buttonPanel, okBtn,
		GridBagConstraints.RELATIVE, 0, 1, 1, 0, 0, 0, 0);

	if (showApply) {
	    ActionString asApply = new ActionString("LBL_APPLY",
		"org.wbemservices.wbem.apps.common.common");
	    applyBtn = new JButton(asApply.getString());
	    applyBtn.setMnemonic(asApply.getMnemonic());
	    applyBtn.setFocusPainted(true);
	    Constraints.constrain(buttonPanel, applyBtn,
			GridBagConstraints.RELATIVE, 0, 1, 1, 0, 5, 0, 0);
	}

	cancelBtn = new JButton(I18N.loadString("LBL_CANCEL",
			    "org.wbemservices.wbem.apps.common.common"));
	cancelBtn.setFocusPainted(true);
	Constraints.constrain(buttonPanel, cancelBtn,
		GridBagConstraints.RELATIVE, 0, 1, 1, 0, 5, 0, 0);

	cancelBtn.addFocusListener(new FocusListener() {
	    public void focusGained(FocusEvent e) {
		cancelBtnHasFocus = true;
	    }

	    public void focusLost(FocusEvent e) {
		cancelBtnHasFocus = false;
	    }
	});

	// Top area contains left/right panels
	JPanel topPanel = new JPanel();
	topPanel.setLayout(new BorderLayout());
	topPanel.add("West", leftPanel);
	topPanel.add("Center", rightPanel);

	// Bottom area contains right-justified button subpanel
	JPanel bottomPanel = new JPanel();
	bottomPanel.setLayout(new GridBagLayout());
	bottomPanel.setBorder(BorderFactory.createLineBorder(Color.black));
	Constraints.constrain(bottomPanel, buttonPanel, 0, 0, 1, 1, 
			      GridBagConstraints.NONE,
		    	      GridBagConstraints.EAST, 1.0, 0.0, 5, 
			      0, 5, 10);

	// Add top and bottom areas to rootPanel.
	rootPanel.add("Center", topPanel);
	rootPanel.add("South", bottomPanel);

	// Add the <esc> & <Enter> listeners to the dialog.
	EscKeyListener escListener = new EscKeyListener();
	EnterKeyListener enterListener = new EnterKeyListener();

	rootPanel.registerKeyboardAction(escListener,
					 KeyStroke.getKeyStroke(
					 KeyEvent.VK_ESCAPE, 0, true),
					 JComponent.WHEN_IN_FOCUSED_WINDOW);

	rootPanel.registerKeyboardAction(enterListener,
					 KeyStroke.getKeyStroke(
					 KeyEvent.VK_ENTER, 0, true),
					 JComponent.WHEN_IN_FOCUSED_WINDOW);

	getContentPane().add("Center", rootPanel);


	// set size
	dlgSizeNoHelp = Constants.LARGE_NOHELP_DLG;
	dlgSizeHelp = Constants.LARGE_HELP_DLG;
	setSize(getMySize(hidden));

	addWindowListener(this);

	// Force a toggle action on the infoPanel's toggle button, 
	// so the infoPanel
	// gets sized properly.  Note that the boolean 'hidden' contains
	// the proper state we want the infoPanel to be, so to force the 
	// toggle action result to indicate this state, we set it to the 
	// unary before forcing the action.
	//
	hidden = !hidden;
	ActionEvent ev = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, 
					 "infopanel");
	actionPerformed(ev);

    } // end of constructor


    /**
     * Return handle to the info panel.
     *
     * @return	the info panel
     */
    public GenInfoPanel getInfoPanel() {
	return infoPanel;
    } // getInfoPanel


    /**
     * Return handle to the OK button.
     *
     * @return	the OK button
     */ 
    public JButton getOKBtn() {
	return okBtn;
    } // getOkBtn


    /**
     * Return handle to the Apply button.
     *
     * @return	the Apply button if exists, otherwise null
     */
    public JButton getApplyBtn() {
	return applyBtn;
    } // getApplyBtn


    /**
     * Return handle to the Cancel button.
     *
     * @return	the Cancel button
     */
    public JButton getCancelBtn() {
	return cancelBtn;
    } // getCancelBtn


    /**
     * Returns the right panel of the generic dialog, to which the client 
     * of this class can add it's own components.
     *
     * @return	handle to the panel
     */
    public JPanel getRightPanel() {
	return rightPanel;
    } // getRightPanel


    /**
     * Returns the bottom buttonPanel of the generic dialog.  Client of 
     * this class may want to use this if they want to add more than the 
     * three buttons provided by default.
     */
    public JPanel getbuttonPanel() {
	return buttonPanel;
    } // getbuttonPanel
    

    /**
     * Action handler when info panel toggle button is pressed.
     */
    public void actionPerformed(ActionEvent evt) {
	if (evt.getActionCommand().equals("infopanel")) {
	    hidden = !hidden;
	    infoPanel.hideInfoPanel(hidden);
	    if (hidden) {
		infoPanel.setPreferredSize(
		Constants.INFOPANEL_MIN_SIZE);
		setSize(getMySize(hidden));
		invalidate();
		validate();
	    } else {
		infoPanel.setPreferredSize(
		Constants.INFOPANEL_MAX_SIZE);
		setSize(getMySize(hidden));
		invalidate();
		validate();
	    }
	} 
    } // actionPerformed


    /**
     * Derived classes should override this method and return SMALL, 
     * MEDIUM, or LARGE dimensions for the dialog size, depending on 
     * whether or not this info panel is displayed.  The fixed sizes 
     * are intended to promote consistency amongst all dialogs.
     *
     * @param	isHidden	indicates whether or not the info panel is 
     *		visible
     * @return	the dialog dimension
     */
    public Dimension getMySize(boolean isHidden) {
	if (isHidden) {
	    // return Constants.LARGE_NOHELP_DLG;
	    return dlgSizeNoHelp;
	} else {
	    // return Constants.LARGE_HELP_DLG;
	    return dlgSizeHelp;
	}
    } // getMySize

    /**
     * Set size the the dlg (for getMySize)
     * This fixes bug: 4217401 
     * This is for i18n...the dlg gets packed, and then calls this 
     * method so that pressing the 'I' button restores correct size.
     *
     * @param	newDim Dimension passed in for the size the dlg	
     */
    public void setMySize(Dimension newDim) {
	if (hidden) {
	    dlgSizeNoHelp = newDim;
	    dlgSizeHelp = new Dimension((newDim.width + 
		Constants.INFOPANEL_MAX_SIZE.width), 
		newDim.height);
	} else {
	    dlgSizeHelp = newDim;
	    dlgSizeNoHelp = new Dimension((newDim.width - 
			    Constants.INFOPANEL_MAX_SIZE.width + 
			    Constants.INFOPANEL_MIN_SIZE.width), 
			    newDim.height);
	}
    }

    /**
     * Derived classes can use this method to add help to its info panel.
     *
     * @param	path	the path to the help file
     */
    public void addHelpToInfoPanel(String path) {

	try {
	    infoPanel.setUrl(new URL(path));
	} catch (MalformedURLException ex) {
	    // I guess just dump the exception, since we don't want an
	    // annoying error dialog every time the focus is changed.
	    ex.printStackTrace();
	}
    } // addHelpToInfoPanel


    /**
     * Set the default focus listener for the dialog.  Currently, we only
     * need to set this for the infoPanel, but we could easily do so for
     * other areas of the dialog.
     * Optionally, force the focus on the infoPanel - in most cases you'll want
     * this, since you're most likely calling this method immediately before
     * making the dialog visible.
     *
     * @param	l	the focus listener
     * @param	force	true forces initial focus on the infoPanel
     */
    public void setDefaultFocusListener(FocusListener l, boolean force) {
	infoPanel.setFocusListener(l, force);
// BUGFIX. Accessibility fixes
	okBtn.addFocusListener(l);
	cancelBtn.addFocusListener(l);
    } // setDefaultFocusListener

    /**
     * This defines the dialog's behavior
     * on <Enter>. Subclasses should override
     * this method if they need to do more than
     * simply click the OK button.
     */
    public void onEnterKey() {
	okBtn.doClick();
    } // end onEnterKey

    /**
     * Listener that disposes of the dialog
     * when the <esc> key is typed.
     */
    protected class EscKeyListener implements ActionListener {
	public void actionPerformed(ActionEvent e) {
	    cancelClicked();
	} // end actionPerformed
    } // end class EscKeyListener

    /**
     * Listener that calls the AdminDialog's
     * onEnterKey() when <Enter> is typed.
     */
    protected class EnterKeyListener implements ActionListener {
	public void actionPerformed(ActionEvent e) {
	    if (cancelBtnHasFocus) {
		cancelClicked();
	    } else {
		adminDlg.onEnterKey();
	    }
	} // end actionPerformed
    } // end class EnterKeyListener

    // window listener events
    public void windowActivated(WindowEvent evt) {}
    public void windowClosed(WindowEvent evt) {}
    public void windowClosing(WindowEvent evt) {
	cancelClicked();
    }
    public void windowDeactivated(WindowEvent evt) {}
    public void windowDeiconified(WindowEvent evt) {}
    public void windowIconified(WindowEvent evt) {}
    public void windowOpened(WindowEvent evt) {
	infoPanel.getHideButton().requestFocus();
    }

    /**
     * 
     * when <cancel> button is clicked.
     */
    public void cancelClicked() {
	adminDlg.dispose();
    }

} // AdminDialog
