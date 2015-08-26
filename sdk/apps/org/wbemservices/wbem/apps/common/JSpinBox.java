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
import javax.swing.border.*;
import javax.swing.event.DocumentListener;

import java.awt.*;
import java.awt.event.*;


/**
 * The JSpinBox is a base widget for the
 * JIntSpinBox and JTextSpinBox. JSpinBox
 * provides the common GUI, while the other
 * classes extend it to provide common
 * SpinBox behaviors.
 */
public class JSpinBox extends JPanel implements KeyListener {

    private JPanel spinBox;
    private static final Border emptyBorder = new EmptyBorder(0, 0, 0, 0);
    private static final Border lineBorder = new LineBorder(Color.black);
    public Toolkit toolkit = Toolkit.getDefaultToolkit();
    public JTextField textField;
    public JButton upButton, downButton;
    public boolean wrapping = true;
    public boolean editable = true;


    /**
     * Constructs a JSpinBox with the
     * specified number of columns 
     * displayed in the textfield.
     *
     * @param cols the number of columns to
     * display in the textfield.
     */
    public JSpinBox(int cols) {

	super();
	spinBox = this;
	createJSpinBox();
	textField.setColumns(cols);

    } // constructor


    /**
     * Creates and lays out the JSpinBox 
     * GUI components.
     */
    public void createJSpinBox() {
	spinBox.setLayout(new GridBagLayout());
	spinBox.setBorder(lineBorder);

	textField = new JTextField();
	textField.setDoubleBuffered(true);
	textField.addKeyListener(this);
	textField.setBorder(emptyBorder);
	Constraints.constrain(spinBox, textField, 0, 0, 1, 1,
				GridBagConstraints.BOTH, GridBagConstraints.WEST,
                1.0, 1.0, 0, 0, 0, 0);

	JPanel buttonPanel = new JPanel();
	buttonPanel.setLayout(new GridBagLayout());

	upButton = new JButton();
	upButton.addKeyListener(this);
	upButton.setMargin(new Insets(0, 0, 0, 0));
	upButton.setContentAreaFilled(false);
	Constraints.constrain(buttonPanel, upButton, 0, 0, 1, 1,
				GridBagConstraints.NONE, GridBagConstraints.WEST, 
                0.0, 0.0, 0, 0, 0, 0);
	downButton = new JButton();
	downButton.addKeyListener(this);
	downButton.setContentAreaFilled(false);
	downButton.setMargin(new Insets(0, 0, 0, 0));
	Constraints.constrain(buttonPanel, downButton, 0, 1, 1, 1,
				GridBagConstraints.NONE, GridBagConstraints.WEST, 
                0.0, 0.0, 0, 0, 0, 0);

	Constraints.constrain(spinBox, buttonPanel, 1, 0, 1, 1,
				GridBagConstraints.NONE, GridBagConstraints.WEST, 
                0.0, 0.0, 0, 0, 0, 0);

    } // createJSpinBox


    /**
     * Sets the enabled state of the components that comprise
     * this class.
     *
     * @param	enabled	whether the class is enabled for input
     */
    public void setEnabled(boolean enabled) {

	textField.setEnabled(enabled);
	upButton.setEnabled(enabled);
	downButton.setEnabled(enabled);

    } // setEnabled


    /**
     * Creates upButton with an up
     * arrow image and downButton with a
     * down arrow image.
     */
    public void addNotify() {

	Graphics bg;
	Polygon polygon;

	super.addNotify();

	// create the up arrow image
	Image upImage = createImage(8, 3);
	bg = upImage.getGraphics();
	polygon = new Polygon();
	polygon.addPoint(4, 0);
	polygon.addPoint(0, 3);
	polygon.addPoint(8, 3);
	bg.setColor(Color.black);
	bg.fillPolygon(polygon);
	ImageIcon upIcon = new ImageIcon(upImage);
	upButton.setIcon(upIcon);

	// create the down arrow image
	Image downImage = createImage(8, 3);
	bg = downImage.getGraphics();
	polygon = new Polygon();
	polygon.addPoint(0, 0);
	polygon.addPoint(8, 0);
	polygon.addPoint(4, 3);
	bg.setColor(Color.black);
	bg.fillPolygon(polygon);
	ImageIcon downIcon = new ImageIcon(downImage);
	downButton.setIcon(downIcon);

    } // addNotify


    /**
     * Gets the JTextField component
     * of the JSpinBox.
     *
     * @return textfield the JTextField 
     * component of the JSpinBox
     */
    public JTextField getTextField() {

	return textField;

    } // getTextField


    /**
     * Sets the JTextField component
     * of the JSpinBox.
     *
     * @param txtField the JTextField
     * component of the JSpinBox
     */
    public void setTextField(JTextField txtField) {

	textField = txtField;

    } // setTextField


    /**
     * Gets the Up button component
     * of the JSpinBox
     *
     * @return btn the Up button component
     */
    public JButton getUpButton() {

	return upButton;

    } // getUpButton


    /**
     * Sets the Up button component
     * of the JSpinBox
     *
     * @param upBtn the Up button
     * component of the JSpinBox
     */
    public void setUpButton(JButton upBtn) {

	upButton = upBtn;

    } // setUpButton


    /**
     * Gets the Down button component
     * of the JSpinBox
     *
     * @return btn the Down button component
     */
    public JButton getDownButton() {

	return downButton;

    } // getDownButton


    /**
     * Sets the Down button component
     * of the JSpinBox
     *
     * @param downBtn the Down button
     * component of the JSpinBox
     */
    public void setDownButton(JButton downBtn) {

	downButton = downBtn;

    } // setDownButton


    /**
     * Gets the wrapping property setting
     *
     * @return w whether to wrap
     */
    public boolean isWrapping() {

	return wrapping;

    } // isWrapping


    /**
     * Sets the wrapping property
     *
     * @param w whether to wrap
     */
    public void setWrapping(boolean w) {

	wrapping = w;

    } // setWrapping


    /**
     * Gets the editable property setting
     *
     * @return e whether the textfield is editable
     */
    public boolean isEditable() {

	return textField.isEditable();

    } // getEditable


    /**
     * Sets the editable property
     *
     * @param e whether the textfield is editable
     */
    public void setEditable(boolean e) {

	textField.setEditable(e);

    } // setEditable


    /**
     * KeyListener interface method that
     * listens for <up> and <down> arrow
     * key input.
     *
     * @param e the key event
     */
    public void keyTyped(KeyEvent e) {

    } // keyTyped


    /**
     * KeyListener interface method that
     * listens for <up> and <down> arrow
     * key input.
     *
     * @param e the key event
     */
    public void keyPressed(KeyEvent e) {

	if (e.getKeyCode() == KeyEvent.VK_UP) {

	    if (upButton.isEnabled()) {

		upButton.doClick();

	    } else {

		toolkit.beep();

	    }

	} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {

	    if (downButton.isEnabled()) {

		downButton.doClick();

	    } else {

		toolkit.beep();

	    }

	}

    } // keyPressed


    /**
     * KeyListener interface method that
     * listens for <up> and <down> arrow
     * key input.
     *
     * @param e the key event
     */
    public void keyReleased(KeyEvent e) {

    } // keyReleased

    /**
     * Adds a document listener to the textfield
     *
     * @param listener the document listener to add
     */
    public void addDocumentListener(DocumentListener listener) {
	textField.getDocument().addDocumentListener(listener);
    }

} // JSpinBox
