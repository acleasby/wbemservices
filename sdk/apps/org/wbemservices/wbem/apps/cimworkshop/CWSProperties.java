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


import java.util.Properties;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;



/**
 * load and restore properties
 *
 */
public class CWSProperties {

    private static Properties props = new Properties();


    /**
     * Gets a String-type property.
     *
     * @param prop  The name of the property.
     */
    public static String getProperty(String prop) {
	return props.getProperty(prop);
    }

    /**
     * Gets a String-type property.  If not found, return default 
     *
     * @param prop  The name of the property.
     * @param defaultProp  The default value if property not found.
     */
    public static String getProperty(String prop, String defaultProp) {
	return  props.getProperty(prop, defaultProp);
    }

    public static void setProperty(String prop, String val) {
	props.put(prop, val);
    }

    /**
     * Gets a property as an integer.
     *
     * @param prop  The property name.
     * @return  The value of the property.  0 if not parsable as an
     *          integer.
     */
    public static int getInt(String prop) {
	try {
	    return Integer.parseInt(getProperty(prop));
	} catch (NumberFormatException ex) {
	    return 0;
	}
    }

    /**
     * Gets a property as an integer.
     *
     * @param prop  The property name.
     * @param defaultProp  The property value if property not found.
     * @return  The value of the property.  Returns default if 
     *          not parsable as an integer.
     */
    public static int getInt(String prop, int defaultProp) {
	try {
	    return Integer.parseInt(getProperty(prop));
	} catch (NumberFormatException ex) {
	    return defaultProp;
	}
    }

    public static void setProperty(String prop, int i) {
	props.put(prop, String.valueOf(i));
    }


    /**
     * Gets a property as a boolean.  Assumes words starting with
     * 't' or 'y' (true or yes) as true values.  All other values
     * are assumed to be false.
     * (Note: Capital 'T' and 'Y' characters will not be recongnized
     *        appropriately)
     *
     * @param prop  The property name.
     * @return  The boolean value ofthe property.
     */
    public static boolean getBoolean(String prop) {
	boolean b = false;
	String s = getProperty(prop);
	if ((s != null) && (s.startsWith("t") || s.startsWith("y"))) {
	    b = true;
	}
	return b;
    }

    public static void setProperty(String prop, boolean b) {
	props.put(prop, String.valueOf(b));
    }


    /**
     * Gets a property that specifies a font.
     *
     * @param font  The name of the font property
     * @see java.awt.Font#getFont(java.lang.String)
     */
    public static Font getFont(String font) {
	return Font.decode(getProperty(font));
    }


    /**
     * Gets a property specifying a dimension.  Assume two properties
     * exist named prop.width and prop.height (where prop is the property
     * name passed to this method).  It uses these two numbers as the
     * width and height of the generated dimension.
     *
     * @param prop  The name of the property.
     * @return  A dimension -- null if properties not found or
     *          not parsable as integers.
     */
    public static Dimension getSize(String prop) {
	try {
	    int width = Integer.parseInt(getProperty(prop+".width"));
	    int height = Integer.parseInt(getProperty(prop+".height"));
	    return (new Dimension(width, height));
	} catch (NumberFormatException ex) {
	    return null;
	}
    }

    public static void setProperty(String prop, Dimension d) {
	props.put(prop+".width", String.valueOf(d.width));
	props.put(prop+".height", String.valueOf(d.height));
    }


    /**
     * Gets a property specifying a point.  Assumes two properties
     * exist named prop.x and prop.y (where prop is the property name
     * passed to this method).  It uses these two numbers as the x
     * and y coordinates of the generated Point.
     *
     * @param prop  The name of the property.
     * @return  A point -- null if properties not found or not
     *          parsable as integers.
     */
    public static Point getPoint(String prop) {
	try {
	    int x = Integer.parseInt(getProperty(prop+".x"));
	    int y = Integer.parseInt(getProperty(prop+".y"));
	    return (new Point(x, y));
	} catch (NumberFormatException ex) {
	    return null;
	}
    }

    public static void setProperty(String prop, Point p) {
	props.put(prop+".x", String.valueOf(p.x));
	props.put(prop+".y", String.valueOf(p.y));
    }


    /**
     * Gets a property specifying a rectangle.  Assumes four
     * properties exist named prop.x, prop.y, prop.width and
     * prop.height (where prop is the property name passed to
     * this method).  It uses these four numbers to generate
     * the returned Rectangle.
     *
     * @param prop  The name of the property
     * @return  A rectangle -- null if properties not found or
     *          not parsable as integers.
     */
    public static Rectangle getRect(String prop) {
	Point p = getPoint(prop);
	Dimension d = getSize(prop);
	if ((p == null) || (d == null)) {
	    return null;
	} else {
	    return new Rectangle(p, d);
	}
    }

    public static void setProperty(String prop, Rectangle r) {
	setProperty(prop, r.getLocation());
	setProperty(prop, r.getSize());
    }


    /**
     * A convenience method that gets a rectangle and checks if the
     * point specified by x and y is inside the rectangle.
     *
     * @see #getRect(java.lang.String)
     * @param prop  The property name.
     * @param x     The x coordinate of the point.
     * @param y     The y coordinate of the point.
     */
    public static boolean contains(String prop, int x, int y) {
	Rectangle r = getRect(prop);
	if (r == null) {
	    return false;
	} else {
	    return (r.contains(x, y));
	}
    }


    /**
     * Gets a property specifying insets.  Assumes four properties
     * exist named prop.top, prop.left, prop.bottom and prop.right
     * (where prop is the property name passed to this method). It
     * uses these four numbers to generate the returned Insets obj
     *
     * @param prop  The name of the property
     * @return  Insets -- null if properties not found or
     *          not parsable as integers.
     */
    public static Insets getInsets(String prop) {
	try {
	    int top = Integer.parseInt(getProperty(prop+".top"));
	    int left = Integer.parseInt(getProperty(prop+".left"));
	    int bottom = Integer.parseInt(getProperty(prop+".bottom"));
	    int right = Integer.parseInt(getProperty(prop+".right"));
	    return (new Insets(top, left, bottom, right));
	} catch (NumberFormatException ex) {
	    return null;
	}
    }
}
