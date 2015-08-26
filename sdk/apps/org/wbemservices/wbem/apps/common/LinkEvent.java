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

/**
 * LinkEvent class implement hyperlinklistener interface for 
 * JEditor pane.
 *
 * @version 1.5 11/28/00 
 * @author Sun Microsystems, Inc.
 */

import javax.swing.JEditorPane;
import java.awt.Cursor;
import java.io.IOException;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;


public class LinkEvent implements HyperlinkListener {

    private JEditorPane pane;
    public LinkEvent(JEditorPane jep) {
	pane = jep;
    }

    // implementation of hypelinkUpdate method.
    // Swing 1.0.3 vesion JEditorPane does not support
    // EventType.ENTERED and EventType.EXITED yet.

    public void hyperlinkUpdate(HyperlinkEvent he) {
	HyperlinkEvent.EventType type = he.getEventType();
	if (type == HyperlinkEvent.EventType.ENTERED) {
	   // System.out.println("entered");
	   pane.setCursor(Cursor.getDefaultCursor());
	} else if (type == HyperlinkEvent.EventType.EXITED) {
	   // System.out.println("exited");
	   pane.setCursor(Cursor.getDefaultCursor());
	} else {
	    // System.out.println("activated");
	    try {
		pane.setPage(he.getURL());
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
    }
}
	
