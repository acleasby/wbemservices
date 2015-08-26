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

package org.wbemservices.wbem.compiler.mofc;


import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;
/**
  *
  * @author      Sun Microsystems, Inc.
  * @version     1.9, 05/18/01
  * @since       WBEM 1.0
  */
public class I18N {
   
    private static String resourceBundleName = "";

    /**
     * The default Locale 
     * The locale is read at initialization time.
     */
    public static Locale locale = Locale.getDefault();


    /**
     *
     */
    public static void setResourceName(String fileName) {
	resourceBundleName = fileName;
    }

    /**
     *
     */
    public static String getResourceName() {
	return resourceBundleName;
    }


    /**
     *
     */
    public static String loadString(String id) {
	return loadString(id, resourceBundleName);
    }

    /**
     * Checks if a string is in the resource bundle
     *
     * @param id    The string ID of the string you want to retrieve
     */
    public static boolean isStringAvailable(String id) {
	return isStringAvailable(id, resourceBundleName);
    }

    
    /**
     * Checks if a string is in the resource bundle
     *
     * @param id    The string ID of the string you want to retrieve
     * @param b	    A String that represents the name of the resource bundle.
     */
    public static boolean isStringAvailable(String id, String b) {
	ResourceBundle bundle = null;
    	try {
	    bundle = ResourceBundle.getBundle(b, locale);
	} catch(MissingResourceException  e) {
	    try {
		bundle = ResourceBundle.getBundle(b, Locale.ENGLISH);
	    } catch(MissingResourceException ee) {
		return false;
	    }
	}

	if (bundle == null) {
		return false;
	} else {
	    try {
		bundle.getString(id);
	    } catch (MissingResourceException e) {
		return false;
	    }
	    return true;
	}
   }


    /**
     * load a string from the resource bundle
     *
     * @param id    The string ID of the string you want to retrieve
     * @param b	    A String that represents the name of the resource bundle.
     */
    public static String loadString(String id, String b) {
	ResourceBundle bundle = null;
    	try {
	    bundle = ResourceBundle.getBundle(b, locale);
	} catch(MissingResourceException  e) {
	    try {
		bundle = ResourceBundle.getBundle(b, Locale.ENGLISH);
	    } catch(MissingResourceException ee) {
		System.err.println("CRITICAL ERROR: Could not load resource bundle " + b);
		System.exit(-1);
	    }
	}

	if (bundle == null) {
	    //Since this is an error msg about not being able to locate
	    //the resource bundle - Do not localize these strings
	    System.err.println("CRITICAL ERROR: Could not load resource bundle " + b);
	    System.exit(-1);
	    return null;
	} else {
	    try {
		return bundle.getString(id);
	    } catch (MissingResourceException e) {
		//Since this is an error msg about not being able to locate
		//the resource bundle - Do not localize these strings
		System.err.println("CRITICAL ERROR: Could not load ID " +
				   id + " resource bundle " + b);
		System.exit(-1);
	    }
	    return null;
	}		
    }


    /**
     * load a string from the resource bundle and applies a message format
     * using a vector of Objects
     *
     * @param id	The string ID of the string you want to retrieve
     * @param values	Vector containing Objects that will be inserted in
     *			message
     * @param b		A String that represents the name of the resource 
     *			bundle.
     */
    public static String loadStringFormat(String id, Vector values, String b) {
	String msgString = loadString(id, b);
	Object[] arguments;
	int numItems = values.size();
	if (msgString != null) {
	    arguments = new Object[numItems];
	    for (int i = 0; i < numItems; i++) {
		arguments[i] = values.elementAt(i);
	    }
	    return MessageFormat.format(msgString, arguments);
	} else {
	    return null;
	}
    }

    /**
     * load a string from the resource bundle and applies a message format
     * using a vector of Objects
     *
     * @param id	The string ID of the string you want to retrieve
     * @param values	Vector containing Objects that will be inserted in
     *			message
     */
    public static String loadStringFormat(String id, Vector values) {
	return loadStringFormat(id, values, resourceBundleName);
    }

    /**
     * load a string from the resource bundle and applies a message format
     * using a array of Objects
     *
     * @param id	The string ID of the string you want to retrieve
     * @param values	Array containing Objects that will be inserted in
     *			message
     * @param b		A String that represents the name of the resource 
     *			bundle.
     */
    public static String loadStringFormat(String id, Object[] values,
					  String b) {
	String msgString = loadString(id, b);
	if (msgString != null) {
	    return MessageFormat.format(msgString, values);
	} else {
	    return null;
	}
    }

    /**
     * load a string from the resource bundle and applies a message format
     * using a array of Objects
     *
     * @param id	The string ID of the string you want to retrieve
     * @param values	Array containing Objects that will be inserted in
     *			message
     */
    public static String loadStringFormat(String id, Object[] values) {
	return loadStringFormat(id, values, resourceBundleName);
    }

    /**
     * load a string from the resource bundle and applies a message format
     * using a single Object
     *
     * @param id	The string ID of the string you want to retrieve
     * @param arg1	Object to be inserted in message
     */
    public static String loadStringFormat(String id, Object arg1) {
	Vector v = new Vector();
	v.addElement(arg1);
	return loadStringFormat(id, v);
    }
	    
    /**
     * load a string from the resource bundle and applies a message format
     * using 2 Objects
     *
     * @param id	The string ID of the string you want to retrieve
     * @param arg1	First Object to be inserted in message
     * @param arg2	Second Object to be inserted in message
     */
    public static String loadStringFormat(String id, Object arg1, 
					  Object arg2) {
	Vector v = new Vector();
	v.addElement(arg1);
	v.addElement(arg2);
	return loadStringFormat(id, v);
    }
	    
}

