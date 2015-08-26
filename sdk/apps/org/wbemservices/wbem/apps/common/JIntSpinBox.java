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

import javax.swing.text.*;

import java.awt.*;
import java.awt.event.*;


/**
 * The JIntSpinBox extends the JSpinBox to provide SpinBox behavior for 
 * selecting an integer from a range of integers
 *
 * NOTE: Only chars 0-9 and, if the floor is less than 0, '-' are accepted
 */
public class JIntSpinBox extends JSpinBox {

    /**
     * Minimum size for the class.
     */
    private final static Dimension	MIN_SIZE = new Dimension(34, 17);

    private JSpinBox	spinBox;
    public int		floor;
    public int		ceiling;
    public int		increment = 1;
    private boolean	bDoubleAught = false;


    /**
     * Constructs a JIntSpinBox with the specified number of columns 
     * displayed in the textfield, and with the specified integer range
     *
     * NOTE: The textfield is editable, and wrapping is turned on by default
     *
     * @param cols the number of columns to display in the textfield
     *
     * @param floor the low integer value
     *
     * @param ceiling the high integer value
     */
    public JIntSpinBox(int cols, int floor, int ceiling) {

	this(cols, floor, ceiling, 1);

    } // constructor


    /**
     * Constructs a JIntSpinBox with the specified number of columns 
     * displayed in the textfield, with the specified integer range, and the
     * specified increment value
     *
     * NOTE: The textfield is editable, and wrapping is turned on by default
     *
     * @param cols the number of columns to display in the textfield
     *
     * @param floor the low integer value
     *
     * @param ceiling the high integer value
     *
     * @param increment the value to adjust up/down
     */
    public JIntSpinBox(int cols, int floor, int ceiling, int increment) {

	super(cols);
	spinBox = this;
	spinBox.setMinimumSize(MIN_SIZE);
	setIncrement(increment);
	addListeners(floor, ceiling);

    } // constructor


    /**
     * Adds the ActionListeners to the Up and Down button components
     *
     * @param f floor value
     *
     * @param c ceiling value
     */
    private void addListeners(int f, int c) {

	spinBox.getUpButton().addActionListener(new UpBtnListener());
	spinBox.getDownButton().addActionListener(new DownBtnListener());
	floor = f;
	ceiling = c;
	spinBox.getTextField().setDocument(new IntOnlyDocument());

    } // addListeners


    /**
     * Gets the floor property setting
     *
     * @return f the floor value
     */
    public int getFloor() {

	if ((floor == 0) && getDoubleAught()) {

	    Integer i = new Integer(00);
	    return (i.intValue());

	}
	return floor;

    } // getFloor


    /**
     * Sets the floor property
     *
     * @param f the floor value
     */
    public void setFloor(int f) {

	floor = f;

    } // setFloor


    /**
     * Gets the ceiling property setting
     *
     * @return c the ceiling value
     */
    public int getCeiling() {

	if ((ceiling == 0) && getDoubleAught()) {

	    Integer i = new Integer(00);
	    return (i.intValue());

	}
	return ceiling;

    } // getCeiling


    /**
     * Sets the ceiling property
     *
     * @param c the ceiling value
     */
    public void setCeiling(int c) {

	ceiling = c;

    } // setCeiling


    /**
     * Gets the increment property setting
     *
     * @return i the increment value
     */
    public int getIncrement() {

	return increment;

    } // getIncrement


    /**
     * Sets the increment property
     *
     * @param i the increment value
     */
    public void setIncrement(int i) {

	increment = i;

    } // setIncrement


    /**
     * Gets an int from the text in the JIntSpinBox textField component
     *
     * @return i the int value from textField
     */
    public synchronized int getIntValue() {

	Integer integer;
	// if the textfield is empty, return 0 as the associated int value
	//
	if (getStringValue().equals("")) {

	    return 0;

	}
	try {

	    integer = new Integer(getStringValue());

	} catch (NumberFormatException nfe) {

	    return increment;

	}
	return integer.intValue();

    } // getIntValue


    /**
     * Sets an integer value for the text in the JIntSpinBox textField 
     * component
     *
     * @param i the int value for the textField
     */
    public void setIntValue(int i) {

	int val = i;
	if (getDoubleAught() && (val == 0)) {

	    spinBox.getTextField().setText("00");

	} else {

	    if (val < floor) {
		val = floor;
	    } else if (val > ceiling) {
		val = ceiling;
	    }
	    spinBox.getTextField().setText(Integer.toString(val));

	}

    } // setIntValue


    /**
     * Gets the text from the JIntSpinBox textField component
     *
     * @return str the string value from textField
     */
    public synchronized String getStringValue() {

	return spinBox.getTextField().getText();

    } // getStringValue


    /**
     * Removes the current value
     */
    public void removeValue() {

	spinBox.getTextField().setText("");

    } // removeValue


    /**
     * Overrides the component's setEnabled method to control the enabled
     * state of the widget.
     *
     * @param	enabled	true enables the widget, false disables it
     */
    public void setEnabled(boolean enabled) {

	spinBox.getUpButton().setEnabled(enabled);
	spinBox.getDownButton().setEnabled(enabled);
	spinBox.getTextField().setEnabled(enabled);

    } // setEnabled


    /**
     * Sets whether the spinbox supports '00' instead of just '0'.
     *
     * @param	bDoubleAught	true displays double 0, false displays single 0
     */
    public void setDoubleAught(boolean bDoubleAught) {

	this.bDoubleAught = bDoubleAught;

    } // setDoubleAught


    /**
     * Gets whether the spinbox supports '00' instead of just '0'.
     *
     * @return	boolean	true displays double 0, false displays single 0
     */
    public boolean getDoubleAught() {

	return bDoubleAught;

    } // getDoubleAught


    /**
     * Document class for the JIntSpinBox textField component that only 
     * allows digit input within the floor - ceiling range, and if the floor
     * is less than 0, '-' as the first character.
     */
    public class IntOnlyDocument extends PlainDocument {

	public void insertString(int offs, String str, AttributeSet a)
					throws BadLocationException {

	    Integer newInteger;

	    char [] source = str.toCharArray();
	    char [] result = new char[source.length];
			int j = 0;

	    // make sure only digits and, if the floor is less than 0,
	    // '-' get inserted
	    for (int i = 0; i < result.length; i++) {

		if (Character.isDigit(source[i]) ||
			(i == 0 && source[i] == '-' && floor < 0)) {

		    result[j++]  = source[i];

		} else {

		    spinBox.toolkit.beep();
		    return;

		}

	    }

	    // make sure this doesn't push it above the ceiling
	    String newVal = getStringValue().substring(0, offs);
	    newVal += new String(result, 0, j);
	    newVal += getStringValue().substring(offs);
	    if (newVal.length() > 0) {

		if (newVal.equals("-") && j == 1) {

		    super.insertString(offs, new String(result, 0, j), a);

		} else {

		    try {

			newInteger = new Integer(newVal);

		    } catch (NumberFormatException nfe) {

			spinBox.toolkit.beep();
			return;

		    }

		    int newInt = newInteger.intValue();
		    if (newInt <= getCeiling()) {

			super.insertString(offs, new String(result, 0, j), a);

			// for a non-wrapping editable spinbox, we should
			// check and see if the user did something that
			// requires enabling the up/down buttons
			if (!spinBox.isWrapping() && spinBox.isEditable()) {

			    if (newInt < getCeiling() &&
				!spinBox.getUpButton().isEnabled()) {

				spinBox.getUpButton().setEnabled(true);

			    } else if (newInt > getFloor() &&
					!spinBox.getDownButton().isEnabled()) {

				spinBox.getDownButton().setEnabled(true);

			    }

			}

		    } else {

			super.insertString(offs, null, a);

		    }

		}

	    }

	} // insertString

    } // IntOnlyDocument


    /**
     * Called by UpBtnListener when the up button is clicked.
     */
    private synchronized void doUp() {

	Integer newInteger;
	if (getStringValue().equals("")) {

	    newInteger = new Integer(getFloor());
	    setIntValue(newInteger.intValue());

	} else {

	    newInteger = new Integer(getIntValue() + getIncrement());
	    if (newInteger.intValue() <= getCeiling()) {

		// this check is needed in the case that you click 
		// up when the field has a value of Integer.MAX_VALUE
		if (newInteger.intValue() < getFloor()) {

		    setIntValue(getFloor());

		} else {

		    setIntValue(newInteger.intValue());

		}

		if (!spinBox.isWrapping() && 
			newInteger.intValue() == getCeiling()) {

		    spinBox.getUpButton().setEnabled(false);

		}
		if (!spinBox.getDownButton().isEnabled()) {

		    spinBox.getDownButton().setEnabled(true);

		}

	    } else if (spinBox.isWrapping()) {

		newInteger = new Integer(getFloor());
		setIntValue(newInteger.intValue());

	    } else {

		spinBox.getUpButton().setEnabled(false);
		spinBox.toolkit.beep();
		return;

	    }

	}

    } // doUp


    /**
     * Listener that handles modifying what's
     * displayed in the JIntSpinBox textField
     * component when the Up button is pressed
     */
    public class UpBtnListener implements ActionListener {

	public void actionPerformed(ActionEvent e) {

	    doUp();

	} // actionPerformed

    } // UpBtnListener


    /**
     * Called by DownBtnListener when the down button is clicked.
     */
    private synchronized void doDown() {

	Integer newInteger;
	if (getStringValue().equals("")) {

		newInteger = new Integer(getCeiling());
		setIntValue(newInteger.intValue());

	} else {

	    newInteger = new Integer(getIntValue() - getIncrement());
	    if (newInteger.intValue() >= getFloor()) {

		setIntValue(newInteger.intValue());
		if (!spinBox.isWrapping() && 
			newInteger.intValue() == getFloor()) {

		    spinBox.getUpButton().setEnabled(false);

		}
		if (!spinBox.getUpButton().isEnabled()) {

		    spinBox.getUpButton().setEnabled(true);

		}

	    } else if (spinBox.isWrapping()) {

		newInteger = new Integer(getCeiling());
		setIntValue(newInteger.intValue());

	    } else {

		spinBox.getDownButton().setEnabled(false);
		spinBox.toolkit.beep();
		return;

	    }

	}

    } // doDown


    /**
     * Listener that handles modifying what's
     * displayed in the JIntSpinBox textField
     * component when the Down button is pressed
     */
    public class DownBtnListener implements ActionListener {

	public void actionPerformed(ActionEvent e) {

	    doDown();

	} // actionPerformed

    } // DownBtnListener


} // JIntSpinBox
