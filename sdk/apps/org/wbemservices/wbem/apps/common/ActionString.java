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
 * ActionString interposes between the application code and it's resource 
 * bundle, but parses the returned string for the mnemonic indicator and 
 * removes it.  The mnemonic character is '&', as used in Windows.  This 
 * class make it easy to localize the text and associated mnemonics in a 
 * resource properties file - mnemonics can be embedded within the string, 
 * obviating the need for seperate resource strings for the mnemonics.
 *
 * @version 1.1 03/05/01
 * @author  Sun Microsystems, Inc.
 */
public class ActionString {

    private String  string;
    private int     mnemonic = 0;

    /**
     * Returns the localized message associated with the specified message key
     * from the default resource bundle.  Strip out the mnemonic indicator.  
     * Users of this class can then use getString() and getMnemonic().
     *
     * @param	key	message lookup key
     */
    public ActionString(String key) {

	string = I18N.loadString(key);
	split();

    } // constructor


    /**
     * Retrieve the localized message associated with the specified resource
     * bundle and message key.	Strip out the mnemonic indicator.  Users of 
     * this class can then use getString() and getMnemonic().
     *
     * @param bundle handle to the resource bundle from which to retrieve 
     *		     the string
     * @param key message lookup key
     */
    public ActionString(String key, String bundle) {

	string = I18N.loadString(key, bundle);
	split();

    } // constructor

    /**
     * Takes a string and strips out the mnemonic indicator.  Users of 
     * this class can then use getString() and getMnemonic().
     *
     * @param  key    string with mnemonic indicator
     *
     * @param  b      this parameter is not used.  Only here to distinguish
     *                between other constructor that takes a resource string
     */
    public ActionString(String key, boolean b) {

	string = key;
	split();

    } // constructor


    /*
     * Returns the text string without the mnemonic indicator
     *
     * @return the text string without the mnemonic character
     */
    public String getString() {

	return string;

    } // getString


    /*
     * Returns the mnemonic character
     *
     * @return the mnemonic character
     */
    public int getMnemonic() {

	return mnemonic;

    } // getMnemonic


    /**
     * Split the retrieved message into its string and mnemonic parts.
     */
    private void split() {

	int i = string.indexOf('&');
	if (i > -1) {
	    String sUpper = string.toUpperCase();
	    mnemonic = sUpper.charAt(i+1);
	    StringBuffer s = new StringBuffer();
	    s.append(string.substring(0, i));
	    s.append(string.substring(i+1, string.length()));
	    string = s.toString();
	}

    } // split

} // ActionString
