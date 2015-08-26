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

import java.awt.Frame;
import java.util.Vector;

import javax.wbem.cim.CIMDataType;
import javax.wbem.cim.CIMObjectPath;

import org.wbemservices.wbem.apps.common.Util;


/**
 * 
 * @version 	1.5, 08/16/01
 * @author 	Sun Microsystems
 */


public class CIMValueDialog {
    
    public CIMValueDialog() {
    }

				    
    public static Object showDialog(Frame frame, Object currentValue, 
				    String currentName, CIMDataType dataType, 
				    boolean editable) {
	CIMEditDialog editDialog;
	String valueString = "";

	if (currentValue != null) {
	    valueString = currentValue.toString();
	}
	String currentType = Util.getDataTypeString(dataType);
	switch (dataType.getType()) {
	case CIMDataType.BOOLEAN:
	    editDialog = new TrueFalseDialog(frame,
					     currentName, currentType,
					     (Boolean)currentValue, 
					     editable);
	    break;
	case CIMDataType.STRING:
	    editDialog = new TextFieldDialog(frame,
					     currentName, currentType, 
					     valueString, editable);
	    break;
	case CIMDataType.UINT8:
	case CIMDataType.UINT16:
	case CIMDataType.UINT32:
	case CIMDataType.UINT64:
	case CIMDataType.SINT8:
	case CIMDataType.SINT16:
	case CIMDataType.SINT32:
	case CIMDataType.SINT64:
	    editDialog = new IntegerFieldDialog(frame,
						currentName, currentType, 
						valueString, editable);
	    break;
	case CIMDataType.REAL32:
	case CIMDataType.REAL64:
	    editDialog = new RealNumberFieldDialog(frame,
						   currentName, currentType, 
						   valueString, editable);
	    break;
	case CIMDataType.DATETIME:
	    editDialog = new DateTimeDialog(frame,
					    currentName, currentType, 
					    valueString, editable);
	    break;
	case CIMDataType.CHAR16:
	    editDialog = new CharFieldDialog(frame,
					     currentName, currentType, 
					     valueString, editable);
	    break;
	case CIMDataType.UINT8_ARRAY:
	case CIMDataType.UINT16_ARRAY:
	case CIMDataType.UINT32_ARRAY:
	case CIMDataType.UINT64_ARRAY:
	case CIMDataType.SINT8_ARRAY:
	case CIMDataType.SINT16_ARRAY:
	case CIMDataType.SINT32_ARRAY:
	case CIMDataType.SINT64_ARRAY:
	case CIMDataType.STRING_ARRAY:
	case CIMDataType.BOOLEAN_ARRAY:
	case CIMDataType.REAL32_ARRAY:
	case CIMDataType.REAL64_ARRAY:
	case CIMDataType.DATETIME_ARRAY:
	case CIMDataType.CHAR16_ARRAY:
	    editDialog = new ArrayEditDialog(frame,
					     currentName, currentType, 
					     (Vector)currentValue, 
					     editable);
	    break;
	case CIMDataType.REFERENCE:
	    editDialog = new ReferenceEditDialog(frame,
						 currentName,  
						 dataType, 
						 (CIMObjectPath)
						 currentValue,
						 editable);
	    break;
	default:
	    editDialog = new TextFieldDialog(frame,
					     currentName, currentType, 
					     valueString, editable);
	    break;
	}
	
	return editDialog.getValueObject();
	
    }
}
