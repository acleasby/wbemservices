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

import java.io.Serializable;
import java.util.Vector;

/**
 * 
 * 
 * This class ...
 * 
 *
 * @author	Sun Microsystems, Inc.
 * @version 	1.4, 02/26/01
 */
public class MofcCIMValue implements Serializable {

    public final static int SIZE_SINGLE=-1;
    public final static int SIZE_UNLIMITED=-2;
    public boolean isArrayVal;
    public Vector  vVector;

    public MofcCIMValue() {
	isArrayVal = false;
	vVector = new Vector();
    }

    public boolean isArrayValue() {
	return isArrayVal; 
    }

    public void setIsArrayValue(boolean ArrayValue) {
	isArrayVal = ArrayValue; 
    }

    public String toString() {
	return(new String (
			" isArrayVal: " + isArrayVal + "\n" +
			vVector+"\n"));
    }

    public void addElement(Object obj) {
	vVector.addElement(obj);
    }

    public boolean contains(Object obj) {
	return(vVector.contains(obj));
    }

    public boolean isEmpty() {
	return(vVector.isEmpty());
    }

    public int size() {
	return(vVector.size());
    }

    public Object firstElement() {
	return(vVector.firstElement());
    }
}
