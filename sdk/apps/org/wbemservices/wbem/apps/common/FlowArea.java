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

import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;


/**
 * The FlowArea is a simple wrapper
 * of the JTextArea. It is intended to
 * facilitate internationalization.
 *
 * Created Novemer 9, 1998
 * @author Sun Microsystems
 */
public class FlowArea extends JTextArea {

	public final static Border emptyBorder = new EmptyBorder(0, 0, 0, 0);

	/**
	 * Contructs a new FlowArea with the
	 * specified text in the specified
	 * number of columns.
	 *
	 * @param text	The text to display
	 */
	public FlowArea(String text) {
		super();
		createFlowArea(text, -1);
	} // end constructor

	/**
	 * Contructs a new FlowArea with the
	 * specified text in the specified
	 * number of columns.
	 *
	 * @param text	The text to display
	 * @param cols	The new of columns to display in
	 */ 
	public FlowArea(String text, int cols) {
		super();
		createFlowArea(text, cols);
	} // end constructor

	/**
	 * Contructs a new FlowArea with the
	 * specified text in the specified
	 * number of columns using the specified
	 * font.
	 *
	 * @param text	The text to display
	 * @param cols	The new of columns to display in
	 * @param f	The font to use
	 */
	public FlowArea(String text, int cols, Font f) {
		super();
		createFlowArea(text, cols);
		setFont(f);
	} // end constructor

	/**
	 * Common code for the constructors.
	 *
	 * @param text	The text to display
	 * @param cols	The new of columns to display in
	 */
	public void createFlowArea(String text, int cols) {

		if (cols > 0) {
			setColumns(cols);
		}
		setLineWrap(true);
		setWrapStyleWord(true);
		append(text);
		setEditable(false);
		setBorder(emptyBorder);
		setForeground(Color.black);
		setOpaque(false);

	} // end createFlowArea

	public void update(Graphics g) {
		paint(g);
	}

} // end class FlowArea
