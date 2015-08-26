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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

/**
 * GenInfoPanel class displays help in the left hand side of 
 * a dialog box. This class uses JEditorPane, to display html
 * file, with hyper-link support. This class also has a behaviour
 * of dynamically closing/opening  the Info panel when Info button
 * is pressed . 
 *
 * @version	1.6	11/16/00
 * @author	Sun Microsystems, Inc.
 */ 

public class GenInfoPanel extends JPanel {

    public final static int MAIN_WINDOW = 0;
    public final static int PROPERTY_DIALOG = 1;

    private InfoBar		infoBar;
    private JButton		infoButton;
    private JEditorPane	editorPane;
    private JScrollPane	infoScroller;

    JPanel infoP = this;

    /**
     * Constructor
     *
     * @param	title	text diplayed on the north side of the panel.
     * @param	type	either MAIN_WINDOW if info panel used at bottom of
     *			the application main window, or PROPERTY_DIALOG if
     *			used inside a dialog.
     */
    public GenInfoPanel(String title, int type) {
	this(title, type, null);
    } // constructor


    /**
     * Constructor
     *
     * @param	title	text diplayed on the north side of the panel.
     * @param	type	either MAIN_WINDOW if info panel used at bottom of
     *			the application main window, or PROPERTY_DIALOG if
     *			used inside a dialog.
     * @param	hideButtonListener  if used inside a dialog, the dialog must provide
     *			an ActionListener that manage dynamic changing of
     *			dialog size based on whether or not the info panel
     *			is opened or closed.
     */
    public GenInfoPanel(String title, int type, ActionListener 
			hideButtonListener) {

	setLayout(new BorderLayout());
	setBackground(Color.white);

	// Set up the top area to hold the title bar
	//
	infoBar = new InfoBar(title, type, hideButtonListener);
	add("North", infoBar); 

	// Set up the bottom area to hold the help text
	//
	editorPane = new JEditorPane();
	editorPane.setEditable(false);
	editorPane.addHyperlinkListener(new LinkEvent(editorPane));
	infoScroller = new JScrollPane(editorPane);
	add("Center", infoScroller);

    } // end constructor


    /**
     * Sets the html text in the infoPanel.
     *
     * @param	url	the URL which will be displayed in the pane.
     */
    public void setUrl(URL url) {
	try {
	    editorPane.setPage(url);
	} catch (MalformedURLException e) {
	    System.out.println("Malformed URL: " +e);
	} catch (IOException e) {
	    System.out.println("IOException: " + e);
	}
    } // setUrl


    /**
     * Sets the text to appear in the title area
     *
     * @param	s	the title text
     */
    public void setTitle(String s) {
	infoBar.setTitleText(s);
    } // setTitle


    /**
     * Returns a handle to the info panel toggle button
     *
     * @return	the info panel toggle button
     */
    public JButton getInfoButton() {
	return infoButton;
    } // getInfoButton
	    

    /**
     * This method is responsible for showing and hiding
     * the info panel.
     *
     * @param	hide	true to hide the info panel, false to show it
     */
    public void hideInfoPanel(boolean hide) {

	infoBar.setState(hide);
	if (hide) {
	    remove(infoScroller);
	    setBackground(new Color(204, 204, 204));
	} else {
	    add("Center", infoScroller);
	    setBackground(Color.white);
	}

    } // hideInfoPanel


    /**
     * Set the focus listener you want invoked when focus is on the infoBar's
     * toggle button (if it exists), the title text, or the html renderer pane.
     * Optionally, force the focus on the infoPanel - in most cases you'll want
     * this, since you're most likely calling this method immediately before
     * making the dialog visible.  If forced, focus will be on the toggle 
     * button,if it exists, otherwise the html renderer pane.
     *
     * @param	l		the focus listener
     * @param	force	true forces initial focus on the infoPanel
     */
    public void setFocusListener(FocusListener l, boolean force) {
	// If toggle button exists, set focus listener for it
	if (infoBar.getHideButton() != null) {
	    infoBar.getHideButton().addFocusListener(l);
	}

	// Set focus listener for title label.  Note that since a label can 
	// button,get focus, we fake it by using a mouse listener and watching 
	// button,presses.
	infoBar.getTitleLabel().addMouseListener(new LabelFocusListener(l));

	// If requested, force focus
	if (force) {
	    if (infoBar.getHideButton() != null) {
		infoBar.getHideButton().requestFocus();
	    } else {
		editorPane.requestFocus();
	    }
	}
    } // setFocusListener

    public JButton getHideButton() {
	return infoBar.getHideButton();
    }


    /**
     * A focus handler for clicks on the title label.
     */
    class LabelFocusListener extends MouseAdapter {
	private FocusListener listener;

	public LabelFocusListener(FocusListener l) {
	    listener = l;
	} // constructor

	public void mousePressed(MouseEvent event) {
	    if (infoBar.getHideButton() != null) {
		infoBar.getHideButton().requestFocus();
	    } else {
		editorPane.requestFocus();
	    }
	} // mousePressed

    } // LabelFocusListener

    
    /**
     * InfoBar class has TwoStateButton with an 'i' image on it, and
     * a title string.  It user RowLayout, so that toggle action keeps
     * the infobutton in top left corner.
     */
    class InfoBar extends JPanel {
    
	private ImageIcon	infoIcon;
	private JLabel		titleLabel;
	protected TwoStateButton hide = null;

	/**
	 *
	 */
	public InfoBar(String title, int type, ActionListener listener) {

	    setLayout(new BorderLayout());
	    JPanel titleArea = new JPanel();
	    titleArea.setLayout(new RowLayout(LAYOUT_ALIGNMENT.EXPAND));

	    JLabel icon = null;
	    if (type == GenInfoPanel.PROPERTY_DIALOG) {
		infoIcon = Util.loadImageIcon("i_small.gif", "InfoGif");

		hide = new TwoStateButton(infoIcon);
		hide.setPreferredSize(new Dimension(infoIcon.getIconWidth() + 
		    10, infoIcon.getIconHeight() + 10));
		hide.setState(true);
		hide.setActionCommand("infopanel");
		hide.setToolTipText(I18N.loadString("TIP_HIDE_HELP",
		    "org.wbemservices.wbem.apps.common.common")); 

		if (listener != null) {
			hide.addActionListener(listener); 
		}
	    } else if (type == GenInfoPanel.MAIN_WINDOW) {
		icon = new JLabel(infoIcon);
		icon.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 0));
	    }

	    titleLabel = new JLabel(title);
	    titleLabel.setFont(new Font("dialog", Font.BOLD, 12));
	    if (type == GenInfoPanel.PROPERTY_DIALOG) {
		Dimension psize = new Dimension(135, 
			titleLabel.getPreferredSize().height);
		titleLabel.setPreferredSize(psize);
		titleLabel.setBorder(BorderFactory.createMatteBorder(
		    0, 0, 1, 0,	new Color(204, 204, 255)));
	    } else if (type == GenInfoPanel.MAIN_WINDOW) {
		titleLabel.setForeground(Color.black); 
	    }

	    if (hide != null) {
		titleArea.add(hide);
	    }

	    if (icon != null) {
		titleArea.add(icon);
	    }

	    titleArea.setBackground(Color.white);
	    titleArea.add(titleLabel);
	    add("West", titleArea);

	    setBackground(Color.white);

	    if (type == GenInfoPanel.MAIN_WINDOW) {
		JButton topics = new JButton(I18N.loadString(
		    "LBL_CONTEXT_HELP", "org.wbemservices.wbem.apps.common.common")); 
		topics.setMargin(new Insets(0, 0, 0, 0));
		topics.setActionCommand("help topics");
		topics.addActionListener(listener);
		add("East", topics);

		Border outer = BorderFactory.createMatteBorder(0, 0, 1, 0,
				new Color(204, 204, 255));
		Border inner = BorderFactory.createEmptyBorder(2, 0, 2, 50);
		setBorder(BorderFactory.createCompoundBorder(outer, inner));
	    } else if (type == GenInfoPanel.PROPERTY_DIALOG) {
		setBorder(BorderFactory.createEmptyBorder(3, 5, 0, 5));
	    }

	} // constructor


	/**
	 * This method is responsible for changing the state of the
	 * infobar between it's expanded and collapsed states
	 *
	 * @param	collapsed	true to collapse, false to collapse
	 */
	public void setState(boolean collapsed) {

	    if (collapsed) {
		hide.setState(!false);
		hide.setBorder(BorderFactory.createRaisedBevelBorder());
		hide.setBorderPainted(true);
		hide.setToolTipText(I18N.loadString("TIP_SHOW_HELP",
				    "org.wbemservices.wbem.apps.common.common")); 
		remove(titleLabel);
		setBackground(new Color(204, 204, 204)); 
	    } else {
		setBackground(Color.white);
		hide.setBorder(BorderFactory.createLoweredBevelBorder());
		hide.setState(true);
		hide.setToolTipText(I18N.loadString("TIP_HIDE_HELP",
				    "org.wbemservices.wbem.apps.common.common")); 
		add(titleLabel); 
	    } 

	} // setState


	/**
	 * Set the title text for the infobar
	 *
	 * @param	s	the title text
	 */
	public void setTitleText(String s) {
	    titleLabel.setText(s);
	} // setTitleText


	/**
	 *
	 */
	public JButton getHideButton() {
		return hide;
	}  // getHideButton


	/**
	 *
	 */
	public JLabel getTitleLabel() {
		return titleLabel;
	} // getTitleLabel

    } // InfoBar

} // GenInfoPanel
