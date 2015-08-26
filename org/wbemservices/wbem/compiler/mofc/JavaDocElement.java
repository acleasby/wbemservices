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
 *are Copyright © 2002 Sun Microsystems, Inc.
 *
 *All Rights Reserved.
 *
 *Contributor(s): _______________________________________
*/

package org.wbemservices.wbem.compiler.mofc;


/**
 * This class is a convenience container for JavaDoc parameters, 
 * return values, and exceptions.
 */
class JavaDocElement {

    private String		tag;
    private StringBuffer	datatype;
    private String		description;

    /**
     * Constructs a JavaDocElement
     *
     * @param	tag		tag string of the doc element
     * @param	datatype	datatype string of the doc element
     * @param	description	description of the doc element
     */
    public JavaDocElement(String tag, StringBuffer datatype, 
	String description) {

	this.tag = tag;
	this.datatype = datatype;
	this.description = description;

    } // constructor

    /**
     * This method returns the tag.
     *
     * @return	String	JavaDoc tag
     */
    public String getTag() {

	return tag;

    } // getTag

    /**
     * This method returns the datatype.
     *
     * @return	StringBuffer	datatype
     */
    public StringBuffer getType() {

	return datatype;

    } // getType

    /**
     * This method returns the description.
     *
     * @return	String	description
     */
    public String getDescription() {

	return description;

    } // getDescription

} // JavaDocElement
