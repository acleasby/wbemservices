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
 * This class is used when a method has a return type of JObject and
 * the calling class needs to know if it should ignore the return value
 * because the user clicked the a cancel button.
 *
 * @version 	1.1, 04/02/01
 * @author 	Sun Microsystems Inc.
 */
public class CancelObject {

    private boolean value = true;

    /**
     *  Constructor
     *
     */
    public CancelObject() {
    }

    /**
     *  Constructor
     *
     * @param b   boolean value to set
     */
    public CancelObject(boolean b) {
	value = b;
    }

    /**
     *  gets boolean value of this object
     *
     * @return   boolean value of this object
     */
    public boolean getValue() {
	return value;
    }

    /**
     *  sets boolean value of this object
     *
     * @param b   boolean value to set for this object
     */
    public void setValue(boolean b) {
	value = b;
    }

}
