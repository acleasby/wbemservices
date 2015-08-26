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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * 
 *
 * @version 	1.4, 11/17/00
 * @author 	Sun Microsystems
 */

// this class selects all the text in a JTextField when it has focus and
// unselects the text when it loses focus
public class TextFieldFocusListener implements FocusListener {
    public void focusGained(FocusEvent evt) {
	if (evt.getComponent() instanceof JTextField) {
	    JTextField tf = (JTextField) evt.getComponent();
	    if (tf.isEditable() && (tf.getText().length() > 0)) {
		tf.setCaretPosition(tf.getText().length());
		tf.selectAll();
	    }
	}
    }    
    public void focusLost(FocusEvent evt) {
	if (evt.getComponent() instanceof JTextField) {
	    JTextField tf = (JTextField) evt.getComponent();
	    tf.select(0, 0);
	}
    }    
}
