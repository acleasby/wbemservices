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

import java.awt.*;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.SwingUtilities;
import javax.swing.ImageIcon;

import javax.wbem.cim.*;

/**
 * 
 * @author 	Sun Microsystems
 */

public class Util {

    public final static Cursor waitCursor = new Cursor(Cursor.WAIT_CURSOR);
    public final static Cursor defaultCursor = new Cursor(
	Cursor.DEFAULT_CURSOR);
    private static URLClassLoader urlCL = null;

    public static void setClassLoader(URLClassLoader cl) {
	    urlCL = cl;
    }

    public static URLClassLoader getClassLoader() {
	return urlCL;
    }

    /**
     * Positions a new window relative to the parent.  This implementation
     * centers the window in front of the parent component's frame.
     * If the parent's window cannot be found, it will center on the parent
     * component itself.  If the parent is null, it will center on the screen.
     * It assumes the window's size has already been set.
     *
     * @param w      The window to position
     * @param parent A component that is the logical parent (in another frame)
     */
    public static void positionWindow(Window w, Component parent) {
	if (w == null) {
	    return;
	}

	// find parent window
	Window pw = (Window) findParentOfType(parent, Window.class);

	Dimension psize;
	Point ploc;
	Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
	if ((pw == null) && (parent == null)) {
	    ploc = new Point(0, 0);
	    psize = screen;
	} else if (pw == null) {
	    ploc = parent.getLocationOnScreen();
	    psize = parent.getSize();
	} else {
	    ploc = pw.getLocation();
	    psize = pw.getSize();
	}

	Dimension wsize = w.getSize();
	Point wloc;

	// If new window is bigger that parent (or almost as big) the new
	// window loc should be offset slightly from the parent, else the
	// new window location should be centered in the parent window.
	int almost = 50;
	if (((psize.width - almost) >= wsize.width) ||
	    ((psize.height - almost) >= wsize.height))  {
	    // Find center of parent location
	    ploc.x += psize.width / 2;
	    ploc.y += psize.height / 2;
  
	    // New location should be centered
	    wloc = new Point();
	    wloc.x = ploc.x - wsize.width / 2;
	    wloc.y = ploc.y - wsize.height / 2;

	    // Check bounds
	    if (wloc.x + wsize.width > screen.width) {
		wloc.x = screen.width - wsize.width;
	    }
	    if (wloc.y + wsize.height > screen .height) {
		wloc.y = screen.height - wsize.height;
	    }
	    if (wloc.x < 0) {
    		wloc.x = 0;
	    }
	    if (wloc.y < 0) {
		wloc.y = 0;
	    }
	} else {
	    // New location should be offset from parent by 'offset' pixels
	    int offset = 30;

	    wloc = new Point();
	    wloc.x = ploc.x + offset;
	    wloc.y = ploc.y + offset;

	    // Check bounds
	    if (wloc.x + wsize.width > screen.width) {
		wloc.x -= (screen.width - wsize.width);
	    }
	    if (wloc.y + wsize.height > screen.height) {
		wloc.y -= (screen.height - wsize.height);
	    }
	}

	w.setLocation(wloc);
    }


    public static boolean isPopupTrigger(MouseEvent evt) {
	if (evt.isPopupTrigger()) {
	    return true;
	}
	if ((evt.getModifiers() & MouseEvent.BUTTON3_MASK) > 0) {
	    return true;
	} else {
	    return false;
	}
    }



    public static String createURLString(String urlstring) {
	char fs = System.getProperty("file.separator").charAt(0);

	StringBuffer sb = new StringBuffer("file:");

	sb.append(urlstring);
	String retval = sb.toString();
    
	if (fs != '/') {
	  retval = retval.replace('/', fs);
	}

	return retval;
    }

    /**
     * Sorts an Enumeration of objects using the toString() method of the objects
     *
     * @param enum The Enumeration of objects to sort
     * @return     Enumeration containing sorted objectsy 
     */
    public static Enumeration sortEnumeration(Enumeration enumeration) {
	Vector list = enumToVector(enumeration);
	sortVector(list);
	return list.elements();
    }

    /**
     * Sorts a Vector of objects using the toString method of the objects 
     *
     * @param list The vector to sort
     */
    public static void sortVector(Vector list) {
	Collections.sort(list, new Comparator (){
		public int compare(Object obj1, Object obj2)
                {
		    return obj1.toString().compareToIgnoreCase(obj2.toString());
                }
		
	    });
	return ;
    }



    /**
     * Traverses the parent heirarchy looking for a component of the type
     * specified. Useful for finding the parent Window or Frame.
     * Returns null if it can't find a parent of the appropriate type.
     *
     * @param c    The component to find the ancetor of.
     * @param type The class of the parent we hope to find.
     */
    public static Object findParentOfType(Component c, Class type) {
	if (c == null) {
	    return null;
	} else if (type.isInstance(c)) {
	    return c;
	} else {
	    return findParentOfType(c.getParent(), type);
	}
    }

    /**
     * Sets the cursor for a component's parent window
     */
    public static void setCursor(Component component, Cursor cursor) {
	getWindow(component).setCursor(cursor);
    }

    /**
     * Sets the wait cursor for a component's parent window
     */
    public static void setWaitCursor(Component c) {
	getWindow(c).setCursor(Cursor.getPredefinedCursor(
	    Cursor.WAIT_CURSOR));
    }

    /**
     * Sets the default cursor for a component's parent window
     */
    public static void setDefaultCursor(Component c) {
	getWindow(c).setCursor(Cursor.getPredefinedCursor(
	    Cursor.DEFAULT_CURSOR));
    }

    /**
     * Traverses the parent tree until it finds a window.
     */
    public static Window getWindow(Component component) {

	Component c = component;

	if (c instanceof Window) {
	    return (Window)c;
	} else {
	    while ((c = c.getParent()) != null) {
		if (c instanceof Window) {
		    return (Window)c;
		}
	    }
	}
	return new Window(getFrame(component));
    }

    /**
     * Traverses the parent tree until it finds a frame.
     */
    public static Frame getFrame(Component component) {
	Component c = component;

	if (c instanceof Frame) {
	    return (Frame)c;
	}

	while ((c = c.getParent()) != null) {
	    if (c instanceof Frame) {
		return (Frame)c;
	    }
	}
	return new Frame();
    }


    /**
     * Converts a Enumeration into a vector
     */
    public static Vector enumToVector(Enumeration e) {
	Vector v = new Vector();
	for (; e.hasMoreElements(); ) {
	    v.addElement(e.nextElement());
	}
	return v;
    }

    /**
     * Converts a Enumeration into an ArrayList
     */
    public static ArrayList enumToList(Enumeration e) {
	ArrayList list = new ArrayList();
	while (e.hasMoreElements()) {
	    list.add(e.nextElement());
	}
	return list;
    }

    /**
     * Converts a Enumeration into a vector
     */
    public static Point getCenterPoint(Component parent, Component child) {
	Point p = new Point();
	Rectangle parentRect = parent.getBounds();
	Rectangle childRect = child.getBounds();
	p.x = parentRect.x + (parentRect.width / 2) - (childRect.width / 2);
	p.y = parentRect.y + (parentRect.height / 2) - (childRect.height / 2);
	return p;
    }

    /**
     * wraps text to a certain number of columns
     *
     * @param textString the text to wrap
     * @param numColumns the number of columns to wrap this text to
     */
    public static String wrapText(String textString, int numColumns) {
	String NEWLINE = "\n";
	StringTokenizer stringTokenizer = new StringTokenizer(textString,
							      " \t\n", true);
	StringBuffer buff = new StringBuffer();
	String word;
	int stringLength = 0;

	while (stringTokenizer.hasMoreTokens()) {
	    word = stringTokenizer.nextToken();
	    if (word.equals(NEWLINE)) {
		stringLength = 0;
	    } else if (stringLength + word.length() > numColumns) {
		buff.append(NEWLINE);
		stringLength = 0;
	    }

	    // add word if it isn't a space at the beginning of a line
	    if (!(word.equals(" ") && buff.toString().endsWith(NEWLINE))) {
		buff.append(word);
		stringLength += word.length();	    	    
	    }
	}
	
	return buff.toString();
    }

    /**
     * wraps text to 40 columns
     *
     * @param textString the text to wrap
     */
    public static String wrapText(String textString) {
	return wrapText(textString, 40);
    }

    /**
     * Utility method for loading an image icon.
     *
     * @param   filename        the basename of the image file to load.
     *
     * @return  the image icon.
     */
    public static ImageIcon loadImageIcon(String filename) {
	return loadImageIcon(filename, "");
    }
	
    /**
     * Utility method for loading an image icon.
     *
     * @param   filename        the basename of the image file to load.
     * @param   description     description of the image icon.
     *
     * @return  the image icon.
     */
    public static ImageIcon loadImageIcon(String filename, String description) {
	if (urlCL != null) {
	    URL url = urlCL.findResource("org/wbemservices/wbem/apps/images/" + 
					 filename);
	    return new ImageIcon(url, description);
	} else {
	    return null;
	}
    } // loadImageIcon


    /**
     * Utility method for gettting relative object path 
     * from absolute object path.
     *
     * @param   op              absolute object path 
     *
     * @return  the relative object path.
     */
    public static CIMObjectPath getRelativeObjectPath(CIMObjectPath op) {
	CIMObjectPath relOP = new CIMObjectPath();
	if (op == null) {
	    return null;
	}
	relOP.setObjectName(op.getObjectName());
	relOP.setKeys(op.getKeys());
	return relOP;
    }	

    public static String getDataTypeString(CIMDataType cdt) {
	StringBuffer buff = new StringBuffer(cdt.toString());
	if (cdt.isArrayType()) {
	    buff.append("[]");
	}
	return buff.toString();
    }

    /**
     * Utility method for returning a standard namespace string with
     * no leading slashes
     *
     * @param   nsString           String representing a namespace 
     *
     * @return  standard namespace string with no leading slashes
     */
    public static String cleanNameSpaceString(String nsString) {
	CIMObjectPath op = new CIMObjectPath("", nsString);
	String nSpace = op.getNameSpace();
	// if nameSpace starts with a '//' or a '\\', remove it
	if (nSpace.startsWith("//") || nSpace.startsWith("\\\\")) {
	    nSpace = nSpace.substring(2);
	} else if (nSpace.startsWith("/") || nSpace.startsWith("\\")) {
	    nSpace = nSpace.substring(1);
	}
	return '/' + nSpace;
    }

    public static void waitOn(Component c) {
	Component rootPane = SwingUtilities.getRoot(c);
	rootPane.setEnabled(false);
	rootPane.setCursor(waitCursor);
    }

    public static void waitOff(Component c) {
	Component rootPane = SwingUtilities.getRoot(c);
	rootPane.setEnabled(true);
	rootPane.setCursor(defaultCursor);
    }

}
