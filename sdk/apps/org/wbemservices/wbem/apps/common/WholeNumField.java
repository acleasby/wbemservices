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

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import java.awt.Toolkit;

/*
 * Whole Number JTextField
 * This class constructs a JTextField with the specified
 * default value and columns displayed, that will ONLY
 * accept digit input. If a non-digit key is typed in
 * this field, a beep will sound.
 *
 * @version	1.0	11/05/98
 * @author	Sun Microsystems, Inc.
 * 
 */
public class WholeNumField extends JTextField {

    public Toolkit toolkit;
    public WholeNumField wholeNumField;
    public int numCols;
    public int maxVal;
    public boolean integerField = false;

    /**
     * WholeNumField constructs a JTextField
     * that only allows integer input.
     *
     * @param defVal the default value with
     * which to populate the field. -1 indicates
     * that the field should be empty.
     *
     * @param numCols the number of columns
     * the field should display
     */
    public WholeNumField(int defVal, int numCols) {

	    this(defVal, numCols, Integer.MAX_VALUE);

    } // end constructor 

    /**
     * WholeNumField constructs a JTextField
     * that only allows integer input.
     *
     * @param defVal the default value with
     * which to populate the field. -1 indicates
     * that the field should be empty.
     *
     * @param numCols the number of columns
     * the field should display
     *
     * @param maxVal the maximum value allowed by the
     * field. A value of -1 indicates that no maximum
     * value has been specified.
     */
    public WholeNumField(int defVal, 
			int numCols, 
			int maxVal) {
	super(numCols);
	wholeNumField = this;
	wholeNumField.numCols = numCols;
	wholeNumField.maxVal = maxVal;

	toolkit = Toolkit.getDefaultToolkit();

	// defVal == -1 means don't set a default value
	setValue(defVal);

    } // end constructor 

    /**
     * WholeNumField constructs a JTextField
     * that only allows integer input, and
     * which allows the number to be preceded
     * by a '-' sign.
     *
     * @param defVal the default value with
     * which to populate the field. -1 indicates
     * that the field should be empty.
     *
     * @param numCols the number of columns
     * the field should display
     *
     * @param maxVal the maximum value allowed by the
     * field. A value of -1 indicates that no maximum
     * value has been specified.
     *
     * @param intDocument whether the document to
     * be used should allow the number to be preceded
     * by a '-' sign.
     */
    public WholeNumField(int defVal,
			int numCols,
			int maxVal,
			boolean intDocument) {

	super(numCols);
	wholeNumField = this;
	wholeNumField.integerField = intDocument;
	wholeNumField.numCols = numCols;
	wholeNumField.maxVal = maxVal;
	toolkit = Toolkit.getDefaultToolkit();

	// defVal == -1 means don't set a default value
	setValue(defVal);
    } // end constructor

    /**
     * Puts the specified value in the field.
     * This method enables for programmatically
     * changing the value in the field.
     *
     * @param value the value with which to
     * populate the field. -1 indicates that
     * the field should be empty.
     */
    public void setValue(int value) {
	setText("");
	if (value >= 0) {
	    // no max value || max val && legal new value 
	    if ((wholeNumField.maxVal < 0) || 
		 ((wholeNumField.maxVal >= 0) && 
		 (value <= wholeNumField.maxVal))) {
		String strVal = new Integer(value).toString();
		setText(strVal);
	    } 
	}
    } // end setValue

    /**
     * Gets the current value in the field.
     *
     * @return int the current value in the
     * field. -1 indicates that the field
     * is currently empty.
     */
    public int getValue() {
	int returnVal;

	String current = getText();
	if (current.length() > 0) {
		Integer i = new Integer(current);
		returnVal = i.intValue();
		return returnVal;
	}
	return -1;

    } // end getValue

    /**
     * Returns the default document.
     */
    protected Document createDefaultModel() {
	return new WholeNumberDocument();
    } // end createDefaultModel

    class WholeNumberDocument extends PlainDocument {

	public void insertString(int offs, String str, AttributeSet a)
				throws BadLocationException {

	    Integer newInteger;

	    char[] source = str.toCharArray();
	    char [] result = new char[source.length];
	    int j = 0;
	    // 1. Make sure only numbers get inserted
	    for (int i = 0; i < result.length; i++) {
		if (Character.isDigit(source[i]) ||
			wholeNumField.integerField && (i == 0) && 
			source[i] == '-') {
		    result[j++] = source[i];
		} else {
		    toolkit.beep();
		}
	    }

	    // 2. Make sure this doesn't push the value over maxVal
	    String newVal = wholeNumField.getText().substring(0, offs);
	    newVal += new String(result, 0, j);
	    newVal += wholeNumField.getText().substring(offs);

	    if (newVal.length() > 0) {
		try {
		    newInteger = new Integer(newVal);
		} catch (NumberFormatException nfe) {
		    toolkit.beep();
		    return;
		}
		// no maxVal or legal maxVal
		if ((wholeNumField.maxVal == -1) || 
			((wholeNumField.maxVal >= 0) && 
			(newInteger.intValue() <= wholeNumField.maxVal))) {

		    super.insertString(offs, new String(result, 0, j), a);
		} else {
		    toolkit.beep();
		}
	    }
	}

    } // end class WholeNumberDocument

} // end WholeNumField
