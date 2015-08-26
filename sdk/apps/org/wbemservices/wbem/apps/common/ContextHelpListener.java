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

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.net.MalformedURLException;

/**
 * A general focus listener used to display context-sensitive help when a
 * component gain input focus.  There should be one unique instance of this
 * class associated with a given component.
 *
 * @author	Sun Microsystems, Inc.
 */
public class ContextHelpListener implements FocusListener {

    private GenInfoPanel	infoPanel;
    private String			appPath;
    private String			helpName;
    protected String prefix;


    /**
     * Uniquely instantiate a focus listener and associate it with the given
     * help file.
     *
     * @param	iPanel	        the info panel in which to render the help file
     * @param	aPath		path to app's directory containing help files, 
     *				relative to classes/com/sun/admin
     * @param	hName	        the full name of the HTML help file including 
     *				the ".html" extension
     */
    public ContextHelpListener(GenInfoPanel iPanel,
			       String aPath, String hName) {
	super();
	this.infoPanel = iPanel;
	this.helpName = hName;
	this.appPath = DefaultProperties.helpLoc;

    } // constructor

    /**
     * Uniquely instantiate a focus listener and associate it with the given
     * help file.
     *
     * @param	iPanel	        the info panel in which to render the help file
     * @param	hName	        the full name of the HTML help file including 
     *				the ".html" extension
     */
    public ContextHelpListener(GenInfoPanel iPanel, String hName) {

	this(iPanel, hName, DefaultProperties.helpLoc);
    } // constructor


    public void focusGained(FocusEvent e) {
	try {
	    infoPanel.setUrl(DefaultProperties.getHelpUrl(helpName));
	} catch (MalformedURLException ex) {
	    // I guess just dump the exception, since we don't want an
	    // annoying error dialog every time the focus is changed.
	    ex.printStackTrace();
	}
    } // focusGained


    public void focusLost(FocusEvent e) {

    } // focusLost

} // ContextHelpListener
