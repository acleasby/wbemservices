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
import javax.swing.Icon;
import javax.swing.JButton;

/**
 * 
 *
 * @version 	1.4, 11/17/00
 * @author 	Sun Microsystems
 */

public class TwoStateButton extends JButton {

    private boolean pressed = false;

    public TwoStateButton(Icon i) { 
	super(i);
	setBorder(BorderFactory.createLoweredBevelBorder());
	setBorderPainted(pressed);
    }

    public void setState(boolean b) {
	if (b != pressed) {
	    changeState();
	}
    }


    public void changeState() {
	pressed = !pressed;

	if (pressed) {
	    setBorderPainted(true);
	} else {
	    setBorderPainted(false);
	}
    }
    
    public boolean getState() {
	return pressed;
    }
}
