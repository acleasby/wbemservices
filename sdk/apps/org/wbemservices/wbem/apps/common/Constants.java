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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

/*
 * Central repository for project-wide constants: colors, fonts,
 * component insets, etc.
 *
 * @version	1.3	11/16/00
 * @author	Sun Microsystems, Inc.
 */

public final class Constants {

	/**
	 * The font used the scope names in the login combo box
	 * 
	 */
	public final static Font	DOMAIN_NAME_FONT = 
		new Font("Dialog", Font.PLAIN, 11);

	/**
	 * The font that should be used for text in a
	 * warning or error dialog.
	 */
	public final static Font	ERROR_DIALOG_FONT = 
		new Font("Dialog", Font.PLAIN, 12);

	/**
	 * The color that should be used for text in a
	 * warning or error dialog.
	 */
	public final static Color	ERROR_DIALOG_TEXT_COLOR = Color.black;

	/**
	 * The color purple used by default in labels.
	 * This can be used by those widgets that aren't
	 * purple by default.
	 */
	public final static Color	PURPLE = new Color(-10066279);

	/**
	 * The standard icon highlight color
	 * This is the same as the Metal look and Feel default,
	 * see swing DefaultMetalTheme
	 */
	public final static Color ICONCOLOR = new Color(204, 204, 255);

	/**
	 * The color that should be used for text in a
	 * warning or error dialog.
	 */
	public final static Color ERROR_DIALOG_BACKGROUND_COLOR = Color.white;

	/**
	 * The font that should be used for labels in a
	 * Properties dialog.
	 */
	public final static Font	PROPS_LABEL_FONT = 
		new Font("Dialog", Font.PLAIN, 12);

	/**
	 * The color that should be used for labels in a
	 * Properties dialog.
	 */
	public final static Color	PROPS_LABEL_COLOR = Color.black;

	/**
	 * The font that should be used for read-only value fields in a
	 * Properties dialog.
	 */
	public final static Font	PROPS_RO_VALUE_FONT = 
		new Font("Dialog", Font.BOLD, 12);

	/**
	 * The color that should be used for read-only value fields in a
	 * Properties dialog.
	 */
	public final static Color	PROPS_RO_VALUE_COLOR = Color.black;

	/**
	 * The font that should be used for read-write value fields in a
	 * Properties dialog.
	 */
	public final static Font	PROPS_RW_VALUE_FONT = 
		new Font("Dialog", Font.PLAIN, 12);

	/**
	 * The font that should be used for password fields in any dialog
	 */
	public final static Font	PASSWORD_VALUE_FONT = 
		new Font("monospaced", Font.PLAIN, 12);

	/**
	 * The Dimension that should be used for a large AdminDialog
	 * with the Info panel displayed
	 */
	public final static Dimension LARGE_HELP_DLG= 
		new Dimension(650, 435);

	/**
	 * The Dimension that should be used for a large AdminDialog
	 * with the Info panel not displayed
	 */
	public final static Dimension LARGE_NOHELP_DLG=
		new Dimension(485, 435);

	/**
	 * The Dimension that should be used for a medium AdminDialog
	 * with the Info panel displayed
	 */
	public final static Dimension MEDIUM_HELP_DLG= 
		new Dimension(485, 425);

	/**
	 * The Dimension that should be used for a medium AdminDialog
	 * with the Info panel not displayed
	 */
	public final static Dimension MEDIUM_NOHELP_DLG=
		new Dimension(360, 425);

	/**
	 * The Dimension that should be used for a small AdminDialog
	 * with the Info panel displayed
	 */
	public final static Dimension SMALL_HELP_DLG=
		new Dimension(485, 240);

	/**
	 * The Dimension that should be used for a small AdminDialog
	 * with the Info panel not displayed
	 */
	public final static Dimension SMALL_NOHELP_DLG=
		new Dimension(360, 240);

	/**
	 * The Dimension that should be used for Error/Warning Dialog 
	 */
	public final static Dimension SMALL_DLG= 
		new Dimension(456, 250);

	/**
	 * The maximum dimension of the Info Panel as it appears in dialogs
	 */
	public final static Dimension	INFOPANEL_MAX_SIZE = 
		new Dimension(180, 390);

	/**
	 * The minimum dimension of the Info Panel as it appears in dialogs
	 */
	public final static Dimension	INFOPANEL_MIN_SIZE = 
		new Dimension(38, 390);

	/**
	/**
	 * Minimum left margin inset for components inside a Properties dialog
	 */
	public final static int		PROPS_LEFT_MARGIN = 5;

	/**
	 * Minimum right margin inset for components inside a Properties dialog
	 */
	public final static int	   	PROPS_RIGHT_MARGIN = 5;

	/**
	 * Minimum top margin inset for components inside a Properties dialog
	 */
	public final static int	   	PROPS_TOP_MARGIN = 20;

	/**
	 * Minimum X-spacing between components inside a Properties dialog
	 */
	public final static int		PROPS_X_GAP = 5;

	/**
	 * Minimum Y-spacing between components inside a Properties dialog
	 */
	public final static int		PROPS_Y_GAP = 10;

	/**
	 * The horizontal gap, in pixels, between successive image buttons
	 * in the client area showing the list of objects as an icon matrix.
	 */
	public final static int		ICON_LIST_X_GAP   = 10;

	/**
	 * The vertical gap, in pixels, between successive image buttons
	 * in the client area showing the list of objects as an icon matrix.
	 */
	public final static int		ICON_LIST_Y_GAP   = 10;

	/**
	 * The horizontal gap, in pixels, between successive image buttons
	 * in the toolbar.
	 */
	public final static int		TOOLBAR_X_GAP = 2;

	/**
	 * The horizontal gap, in pixels, between successive image button groups
	 * in the toolbar.
	 */
	public final static int		TOOLBAR_GROUP_GAP = 11;

	/**
	 * Minimum top margin inset for buttons in the toolbar
	 */
	public final static int	   	TOOLBAR_TOP_MARGIN = 2;

	/**
	 * Minimum bottom margin inset for buttons in the toolbar
	 */
	public final static int	   	TOOLBAR_BOTTOM_MARGIN = 3;

	/**
	 * Minimum left margin inset for the toolbar
	 */
	public final static int		TOOLBAR_LEFT_MARGIN = 5;

	/**
	 * Minimum right margin inset for the toolbar
	 */
	public final static int		TOOLBAR_RIGHT_MARGIN = 
					TOOLBAR_LEFT_MARGIN;


} // Constants
