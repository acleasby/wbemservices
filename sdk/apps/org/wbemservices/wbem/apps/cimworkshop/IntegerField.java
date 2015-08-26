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

package org.wbemservices.wbem.apps.cimworkshop;

import javax.swing.JTextField;
import java.awt.event.KeyEvent;

/**
 * 
 *
 * @version 	1.11, 08/16/01
 * @author 	Sun Microsystems
 */

class IntegerField extends JTextField {

    private boolean signed;

    public IntegerField(String text, boolean s) {
	this(text, s, 20);
    }

    public IntegerField(String text, boolean s, int columns) {
	super(text, columns);
	signed = s;
    }

    public IntegerField(String text) {
	this(text, true, 20);
    }

    public IntegerField(String text, int columns) {
	this(text, true, columns);
    }

    protected void processKeyEvent(KeyEvent evt) {
	boolean processKey = false;
	if (evt.getModifiers() == 0) {
	    int keyChar = evt.getKeyChar();
	    if (signed) {
		int caretPos = getCaret().getDot();
		if (keyChar == '-' && caretPos == 0) {
		    processKey = true;
		}
	    }
	    if ((keyChar >= '0' && keyChar <= '9') ||
		keyChar == '\n' || keyChar == KeyEvent.VK_BACK_SPACE)  {
		processKey = true;
	    }
	}
	// Check for arrow, Home and End keys
	if (!processKey) {
	    int keyCode = evt.getKeyCode();
	    if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_RIGHT ||
		keyCode == KeyEvent.VK_END || keyCode == KeyEvent.VK_HOME) {
		processKey = true;
	    }
	}

	if (processKey) {
	    super.processKeyEvent(evt);
	}
    }

}
