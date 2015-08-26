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

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @version 	1.5, 03/20/01
 * @author 	Sun Microsystems
 *
 */

public class ToolBarPanel extends JPanel implements MouseListener
{
    
    private Vector icons;
    private Color bgColor;


    public ToolBarPanel() {
	setLayout(new RowLayout(LAYOUT_ALIGNMENT.EXPAND));
	bgColor = getBackground();
	icons = new Vector();
	barLayout();
    }

    public void addComponent(Component c) {
	icons.addElement(c);
	barLayout();
    }
   
    public void setBackground(Color c) {
	bgColor = c;
	super.setBackground(c);
    }

    public void barLayout() {
	removeAll();
	add(Box.createVerticalStrut(30));
	for (Enumeration e = icons.elements(); e.hasMoreElements(); ) {
	    add((Component)e.nextElement());
	}
	
	add(Box.createHorizontalGlue());
	invalidate();
	validate();
    }

    public JButton createButton(Icon i, String tt, ActionListener al) {
	return createButton(i, tt, tt, al);
    }    	

    public JButton createButton(Icon i, String tt, String ac, 
				ActionListener al) {
	JButton newButton = new JButton(i);
	newButton.setToolTipText(tt);
	newButton.setActionCommand(ac);
	newButton.addActionListener(al);
	addButton(newButton);
	return newButton;
    }
    

    public void addButton(JButton newButton) {

	newButton.setBorderPainted(false);
	newButton.setBackground(bgColor);
	newButton.addMouseListener(this);
	addComponent(newButton);
    }

    public JLabel createLabel(String text) {
	JLabel addMe = new JLabel(text);
	addLabel(addMe);    
	return addMe;
    }

    public JLabel createLabel(Icon i) {
	JLabel addMe = new JLabel(i);
	addLabel(addMe);    
	return addMe;
    }

    public void addLabel(JLabel l) {
	addComponent(l);
    }

    public void createSpace(int space) {
	addComponent(Box.createHorizontalStrut(space));
    }

    public void mouseEntered(MouseEvent evt) {
	Component c = evt.getComponent();
	if (c instanceof JButton) {
	    ((JButton)c).setBorderPainted(true);	  
	}
    }

    public void mouseExited(MouseEvent evt) {
	Component c = evt.getComponent();
	if (c instanceof JButton) {
	    ((JButton)c).setBorderPainted(false);	  
	}
    }

    public void mouseClicked(MouseEvent evt) {
    }

    public void mousePressed(MouseEvent evt) {
    }

    public void mouseReleased(MouseEvent evt) {
    }

}
