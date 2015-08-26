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

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import javax.swing.JTextField;

/**
 * 
 * SizedIntegerDocument.java
 * Extends PlainDocument and only allows an interger value with a defined 
 * minimum and maximum values to be inserted into the document. 
 *
 * @version 	1.4, 02/13/01
 * @author 	Sun Microsystems
 */


public class SizedIntegerDocument extends PlainDocument {

    // the text field to apply this document to
    JTextField textField;
    
    
    // minimum and maximum values for the integer
    long minVal;
    long maxVal;
    
    

    /**
     * Constructs a new SizedIntegerDocument that is assigned to a 
     * JTextField with a maximum int value.  Minimum value will
     * default to 0
     *
     * @param  tf       The JTextField the docmument will be assigned to
     * @param  max      The maximum value that the integer can be.
     */
    public SizedIntegerDocument(JTextField tf, int max) {
	this(tf, 0, max);
    }

    /**
     * Constructs a new SizedIntegerDocument that is assigned to a 
     * JTextField with a minimum and maximum integer value.
     *
     * @param  tf       The JTextField the docmument will be assigned to
     * @param  max      The maximum value that the integer can be.
     * @param  min      The minimum value that the integer can be.
     */
    public SizedIntegerDocument(JTextField tf, int min, int max) {
	this(tf, (long)min, (long)max);
    }

    

    /**
     * Constructs a new SizedIntegerDocument that is assigned to a 
     * JTextField with a maximum long value.  Minimum value will
     * default to 0
     *
     * @param  tf       The JTextField the docmument will be assigned to
     * @param  max      The maximum value that the integer can be.
     */
    public SizedIntegerDocument(JTextField tf, long max) {
	this(tf, 0, max);
    }

    /**
     * Constructs a new SizedIntegerDocument that is assigned to a 
     * JTextField with a minimum and maximum integer value.
     *
     * @param  tf       The JTextField the docmument will be assigned to
     * @param  max      The maximum value that the integer can be.
     * @param  min      The minimum value that the integer can be.
     */
    public SizedIntegerDocument(JTextField tf, long min, long max) {
	textField = tf;
	minVal = min;
	maxVal = max; 
    }


    /**
     * Sets the minimum and maximum range for the integer
     *
     * @param  max      The maximum value that the integer can be.
     * @param  min      The minimum value that the integer can be.
     */
    public void setRange(long min, long max) {
	minVal = min;
	maxVal = max;
	// ignore empty JTextField 
	if (textField.getText().trim().length() == 0) {
	    return;
	}
	try {
	    // try to parse string long TextField
	    long longVal = Long.parseLong(textField.getText());

	    // if current value is outside of min or max value,
	    // change value to be at threshold
	    if (longVal < minVal) {
		textField.setText(String.valueOf(minVal));
	    } else if (longVal > maxVal) {
		textField.setText(String.valueOf(maxVal));
	    }
	} catch (NumberFormatException e) {
	    // if cannot parse Long, set to min value
	    textField.setText(String.valueOf(minVal));
	    return;
	}
    }

    /**
     * Sets the minimum value for the integer
     *
     * @param  min      The minimum value that the integer can be.
     */
    public void setMin(long min) {
	minVal = min;
    }
    
    /**
     * Sets the maximum value for the integer
     *
     * @param  max      The maximum value that the integer can be.
     */
    public void setMax(long max) {
	maxVal = max;
    }

    /**
     * Parses through the string to determine if the value is an integer and
     * that it falls within the minimum and maximum threshold
     *
     * @param  offset   Current offset character being added to string
     * @param  s        String in the JTextField
     * @param  aSet     Attribute Set of string
     */
    public void insertString(int offset, String s, AttributeSet aSet) throws
                             BadLocationException {
	// get the current value in the text field
	StringBuffer buf  = new StringBuffer(textField.getText());
	// get the value that would result if the character was added
	buf.insert(offset, s);
	// if buf contain only a negitive sign and we allow negative
	// numbers, allow string to insert
	if (!buf.toString().equals("-") || (minVal >= 0)) {
	    try {
		// try to parse string into an int value
		long longVal = Long.parseLong(buf.toString());
		// if string is an long value, make sure is is between the
		// set min and max values
		if (minVal != maxVal) {
		    if ((longVal < minVal) || (longVal > maxVal)) {
			// if too big or small, don't insert string
			return;
		    }
		}
	    } catch (NumberFormatException e) {
		// if cannot parse into an long, don't insert string
		return;
	    }
	}
	// if exceptable int value, insert string
	super.insertString(offset, s, aSet);	    
	
    }
}
