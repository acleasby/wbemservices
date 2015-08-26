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

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;



/**
 * A general utility class to make it easy to apply GridBagConstraints
 * to GridBagLayout.
 * Code lifted from "Java in a Nutshell", O'Reilly & Associated, Inc.
 * and wrapped in a final class.
 * @version	1.3	11/16/00
 * @author	Sun Microsystems, Inc.
 */
public final class Constraints {

	/**
	 * This is the main constrain() method.  It has arguments for
	 * all constraints.
	 */
	public static void constrain(
			Container	container,
			Component	component,
			int			gridx,
			int			gridy,
			int			gridwidth,
			int			gridheight,
			int			fill,
			int			anchor,
			double		weightx,
			double		weighty,
			int			top,
			int			left,
			int			bottom,
			int			right)
	{
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		gbc.gridwidth = gridwidth;
		gbc.gridheight = gridheight;
		gbc.fill = fill;
		gbc.anchor = anchor;
		gbc.weightx = weightx;
		gbc.weighty = weighty;

		if (top+bottom+left+right > 0) {
		    gbc.insets = new Insets(top, left, bottom, right);
		}

		((GridBagLayout)container.getLayout()).setConstraints(
						       component, gbc);
		container.add(component);
	}


	/**
	 * This version of constrain() specifies the position of a component
	 * that does not grow and does not have margins.
	 */
	public static void constrain(
			Container	container,
			Component	component,
			int			gridx,
			int			gridy,
			int			gridwidth,
			int			gridheight)
	{
		constrain(container, component,
			gridx, gridy, gridwidth, gridheight,
			GridBagConstraints.NONE, GridBagConstraints.NORTHWEST,
			0.0, 0.0, 0, 0, 0, 0);
	}


	/**
	 * This version of constrain() specifies the position of a component
	 * that does not grow but does have margins.
	 */
	public static void constrain(
			Container	container,
			Component	component,
			int			gridx,
			int			gridy,
			int			gridwidth,
			int			gridheight,
			int			top,
			int			left,
			int			bottom,
			int			right)
	{
		constrain(container, component, 
			gridx, gridy, gridwidth, gridheight,
			GridBagConstraints.NONE, GridBagConstraints.NORTHWEST,
			0.0, 0.0, top, left, bottom, right);
	}
}
