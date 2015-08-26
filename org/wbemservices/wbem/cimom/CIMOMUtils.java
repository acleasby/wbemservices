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

package org.wbemservices.wbem.cimom;

import javax.wbem.cim.CIMObjectPath;
import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMQualifier;
import javax.wbem.cim.CIMScope;
import javax.wbem.cim.CIMDataType;
import javax.wbem.cim.CIMDateTime;
import javax.wbem.cim.UnsignedInt8;
import javax.wbem.cim.UnsignedInt16;
import javax.wbem.cim.UnsignedInt32;
import javax.wbem.cim.UnsignedInt64;
import javax.wbem.cim.CIMValue;
import javax.wbem.cim.CIMQualifierType;
import javax.wbem.cim.CIMSemanticException;
import javax.wbem.cim.CIMQualifierTypeException;
import javax.wbem.cim.CIMClassException;
import javax.wbem.cim.CIMPropertyException;
import javax.wbem.cim.CIMMethodException;
import javax.wbem.cim.CIMFlavor;
import javax.wbem.cim.CIMProperty;
import javax.wbem.cim.CIMMethod;
import javax.wbem.cim.CIMClass;
import javax.wbem.cim.CIMInstance;

import java.math.BigDecimal;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Calendar;
import java.util.Hashtable;

//XXX: This is a bug. It should use the CIMOM interface
//and NEVER talk directly to the repository
import org.wbemservices.wbem.repository.PSRlogImpl;

public class CIMOMUtils {

    private static int uniqueInt = 0;
    PSRlogImpl ps;


    //    CIMOMUtils(PSRlogImpl ps) {
    CIMOMUtils(PSRlogImpl ps) {
	this.ps = ps;
    }

    PSRlogImpl getPS() {
	return ps;
    }

    private Object intTypeConvert(int type, Object value) {
	if (value == null) {
	    return null;
	}
	Number number = null;
	if (value instanceof String) {
	    String svalue = (String)value;
	    switch (type) {
		case CIMDataType.CHAR16 : 
		throw new IllegalArgumentException();
		case CIMDataType.DATETIME :
		return new CIMDateTime(svalue);
		case CIMDataType.BOOLEAN :
		throw new IllegalArgumentException();
		case CIMDataType.REFERENCE :
		// We actually need to verify that each of the key types
		// also have valid values, and not just check for
		// syntactic correctness.
		return new CIMObjectPath(svalue);
	    }
	    // All other types must be numbers;
	    number = new BigDecimal(svalue);
	}
	// Datetime and Boolean can only be created from a string.
	if ((type == CIMDataType.DATETIME) || (type == CIMDataType.BOOLEAN)) {
	    throw new IllegalArgumentException();
        }
	// All other types must be numbers;
	if (number == null) { number = (Number)value;
        }
	switch (type) {
	    case CIMDataType.UINT8: 
		return new UnsignedInt8(number.shortValue());
	    case CIMDataType.SINT8: return new Byte(number.byteValue());
	    case CIMDataType.UINT16: 
		return new UnsignedInt16(number.intValue());
	    case CIMDataType.SINT16: return new Short(number.shortValue());
	    case CIMDataType.UINT32: 
		return new UnsignedInt32(number.longValue());
	    case CIMDataType.SINT32: return new Integer(number.intValue());
	    case CIMDataType.UINT64: 
		String s = number.toString();
		int dotIndex = s.indexOf(".");
		if (dotIndex == 0) { s = "0";
                }
		if (dotIndex > 0) { s = s.substring(0, dotIndex);
                }
		if (s.charAt(0) == '-') { s = s.substring(1);
                }
		return new UnsignedInt64(s);
	    case CIMDataType.SINT64: return new Long(number.longValue());
	    case CIMDataType.REAL32: return new Float(number.floatValue());
	    case CIMDataType.REAL64: return new Double(number.doubleValue());
	    case CIMDataType.CHAR16: 
		return new Character((char)number.intValue());
	    default: throw new 
	    IllegalArgumentException();
	}
    }

    CIMValue typeConvert(CIMDataType dt, CIMValue cv) {

	if ((cv == null) || (dt == null)) {
	    return cv;
	}

	Object value = cv.getValue();

	int cvt = CIMDataType.findType(value);
	if (cvt == CIMDataType.NULL) {
	    // Make sure that this is not a type-null vector and the data
	    // type is a scalar. This situation is illegal
	    if ((value instanceof Vector) && !dt.isArrayType()) {
		throw new IllegalArgumentException(cv.toString());
	    }
	    return cv;
	}

	int cdt = dt.getType();
	if (cvt == cdt) {
	    return cv;
	}

	// Cannot convert array to non-array and vice versa
	if (dt.isArrayType() != cv.getType().isArrayType()) {
	    throw new IllegalArgumentException(cv.toString());
	}

	if (dt.getType() == cv.getType().getType()) {
	    return cv;
	}

	if (value instanceof Vector) {
	    cdt = CIMDataType.findSimpleType(cdt);
	    Vector nv = new Vector();
	    for (Enumeration e = ((Vector)value).elements(); 
			e.hasMoreElements();) {
		nv.addElement(intTypeConvert(cdt, e.nextElement()));
	    }
	    return new CIMValue(nv, dt);
	}

	return new CIMValue(intTypeConvert(cdt, value));
    }

    // This method checks if the qualifier type for the given qualifier
    // exists, and also populates default values if necessary. This
    // is currently being used for the association qualifier. 
    void checkQualifier(String namespace, CIMQualifier cq) 
			throws CIMException {

	CIMQualifierType cqt = 
	ps.getQualifierType(namespace.toLowerCase(), 
	cq.getName().toLowerCase());
	if (cqt == null) {
	    throw new CIMSemanticException(
	    CIMSemanticException.NO_SUCH_QUALIFIER1, cq.getName());
	}
	// Set the default values as required
	CIMDataType dt = cqt.getType();
	// For boolean qualifier types, if no value is defined, the value
	// is set to true.
	if (!cq.hasValue() && 
		dt.equals(CIMDataType.getPredefinedType(CIMDataType.BOOLEAN))) {
	    cq.setValue(CIMValue.TRUE);
	    cq.setDefaults(cqt);
	} else {
	    cq.setDefaults(cqt);
	}
	// Take care of assigning flavors, default or otherwise
	// What happens when the flavors are different?
	// Check if the array size is correct
    }

    CIMQualifier createDefaultQualifier(String namespace, String qName) 
			throws CIMException {
	CIMQualifierType cqt = ps.getQualifierType(namespace.toLowerCase(),
							qName.toLowerCase());
	if (cqt == null) {
	    throw new CIMSemanticException(
	    CIMSemanticException.NO_SUCH_QUALIFIER1, qName);
	}
	return (new CIMQualifier(qName, cqt));
    }

    // Must be synchronized because we need exclusive access to uniqueInt.
    public static synchronized String getUniqueString() {
	Calendar c = Calendar.getInstance();
	// I'm assuming that there will not be more than 4 billion requests
	// for a unique string in one second. And when daylight savings time
	// is turned off, there should not be more than 4 billion requests
	// in that extra hour. Unique string is required during session set
	// up and during evernt registration.
	uniqueInt++;
	StringBuffer buffer = new StringBuffer();
	buffer.append(c.get(Calendar.YEAR));
	buffer.append(":");
	buffer.append(c.get(Calendar.MONTH));
        buffer.append(":");
        buffer.append(c.get(Calendar.DATE));
        buffer.append(":");
        buffer.append(c.get(Calendar.HOUR));
        buffer.append(":");
        buffer.append(c.get(Calendar.AM_PM));
        buffer.append(":");
        buffer.append(c.get(Calendar.MINUTE));
        buffer.append(":");
        buffer.append(c.get(Calendar.SECOND));
        buffer.append(":");
        buffer.append(uniqueInt);
	return buffer.toString();
    }

    // Do we have to check for duplicate qualifiers. Probably not - if we
    // take care of that during vector creation. 
    void doCommonQualifierChecks(String namespace, String elementName,
		Vector qualifiers, CIMScope scope) throws CIMException {
	if (qualifiers == null) {
	    return;
	}
	Hashtable ht = new Hashtable();
	String lcns = namespace.toLowerCase();
	for (Enumeration qe = qualifiers.elements(); qe.hasMoreElements();) {

	    CIMQualifier cq = (CIMQualifier)qe.nextElement();
	    String tlc = cq.getName().toLowerCase();
	    if (ht.get(tlc) != null) {
		throw new CIMSemanticException(
		CIMException.CIM_ERR_INVALID_PARAMETER, cq);
	    }
	    ht.put(tlc, tlc);
	    CIMQualifierType cqt = 
	    ps.getQualifierType(lcns, tlc);
	    if (cqt == null) {
		throw new CIMSemanticException(
		CIMSemanticException.NO_SUCH_QUALIFIER2, elementName,
					cq.getName());
	    }
	    // Set the default values as required
	    CIMDataType dt = cqt.getType();
	    // For boolean qualifier types, if no value is defined, the value
	    // is set to true.
	    if (!cq.hasValue() && 
		dt.equals(CIMDataType.getPredefinedType(CIMDataType.BOOLEAN))) {
		cq.setValue(CIMValue.TRUE);
		cq.setDefaults(cqt);
	    } else {
		try {
		    cq.setValue(typeConvert(cqt.getType(), cq.getValue()));
		} catch (Exception e) {
		    throw new CIMQualifierTypeException(
		    CIMSemanticException.TYPE_ERROR,
			new Object[] {
			cqt.getName(), "",
			cqt.getType(), cq.getValue().getType(), cq.getValue()});
		}
		cq.setDefaults(cqt);
	    }
	    // Take care of assigning flavors, default or otherwise
	    assignQualifierDefaultFlavor(cq);

	    // Check if the array size is correct
	    /*
	    if (cqt.isArrayValue() && 
		!correctArraySize(cqt.getSize(), cq.getValue())) {
		throw new CIMException("Qualifier " + cq.getName() + 
		" array size and number of array " + "elements mismatch");
	    }
	    */
	    // Check whether the scope of the qualifiers corresponds to scope 
	    if (!cqt.hasScope(scope)) {
		if (scope.equals(CIMScope.getScope(CIMScope.CLASS)) ||
		    scope.equals(CIMScope.getScope(CIMScope.ASSOCIATION))) {
		    throw new CIMClassException(
			CIMSemanticException.SCOPE_ERROR, elementName, 
			cq.getName(), scope);
                }
		
		if (scope.equals(CIMScope.getScope(CIMScope.PROPERTY)) ||
		    scope.equals(CIMScope.getScope(CIMScope.REFERENCE))) {
		    throw new CIMPropertyException(
			CIMSemanticException.SCOPE_ERROR, elementName, 
			cq.getName(), scope);
                }

		if (scope.equals(CIMScope.getScope(CIMScope.METHOD)) ||
		    scope.equals(CIMScope.getScope(CIMScope.PARAMETER))) {
		    throw new CIMMethodException(
			CIMSemanticException.SCOPE_ERROR, elementName, 
			cq.getName(), scope);
                }

		// default
		throw new CIMSemanticException(
		CIMSemanticException.SCOPE_ERROR, elementName, cq.getName(), 
					scope);
	    }
	    // If association qualifier exists, it should be the first one.
	}
    }

    /*
    // Inherit all superclass qualifiers. 
    // If a qualifier has tosubclass flavor and the same qualifier exists
    // in the superclass, then
    //       If the qualifier has enableoverride flavor
    //            the qualifier in the new class is left as is.
    //       If the qualifier does not have enable override flavor
    //            the qualifier from the superclass overwrites that of the
    //            new class
    // If the qualifier with tosubclass flavor does not exist in the subclass
    // just add the qualifier.
    */
    void assignInheritedQualifiers(Vector qualifiers, 
				Vector inheritedQualifiers) {
	if (inheritedQualifiers != null) {
	    if (qualifiers == null) {
		qualifiers = new Vector();
	    }
	    // for all Qualifier of SuperClass
	    Enumeration iterator = inheritedQualifiers.elements();
	    while (iterator.hasMoreElements()) {
		CIMQualifier pos = (CIMQualifier)iterator.nextElement();
		if (pos.hasFlavor(CIMFlavor.getFlavor(CIMFlavor.TOSUBCLASS))) {
		    if (qualifiers.contains(new CIMQualifier(pos.getName()))) {
			if (pos.hasFlavor(CIMFlavor.getFlavor(
			CIMFlavor.ENABLEOVERRIDE))) {
			    // absolute and complete overriding 
			    // of qualifier of superclass
			} else {
			    qualifiers.removeElement(new 
			    CIMQualifier(pos.getName()));
			    // reportError ?? Should we add a brand new
			    // qualifier? Sharing qualifier with
			    // parent class may be dangerous
			    qualifiers.addElement(pos.clone());
			}
		    } else {
			// Should we make make a new qualifier and add?
			qualifiers.addElement(pos.clone());
		    }
		}
	    }
	}
    }

    // Assigns the default flavor to a qualifier 
    private void assignQualifierDefaultFlavor(CIMQualifier qt) {
	if (qt == null) {
	    return;
	}
	if (!qt.hasFlavor(new CIMFlavor(CIMFlavor.ENABLEOVERRIDE))) {
	    if (!qt.hasFlavor(new CIMFlavor(CIMFlavor.DISABLEOVERRIDE))) {
		if (!qt.hasFlavor(new CIMFlavor(CIMFlavor.RESTRICTED))) {
		    qt.addFlavor(new CIMFlavor(CIMFlavor.ENABLEOVERRIDE));
		}
	    }
	}
	if (!qt.hasFlavor(new CIMFlavor(CIMFlavor.TOSUBCLASS))) {
	    if (!qt.hasFlavor(new CIMFlavor(CIMFlavor.RESTRICTED))) {
    		qt.addFlavor(new CIMFlavor(CIMFlavor.TOSUBCLASS));
	    }
	}
    }

    private Vector getLocalProps(Vector props, String className) {
	Vector v = new Vector();
	Enumeration e = props.elements();
	while (e.hasMoreElements()) {
	    CIMProperty cp = (CIMProperty)e.nextElement();
	    if (cp.getOriginClass().equalsIgnoreCase(className)) {
		v.addElement(cp);
            }
	}
	return v;
    }

    private Vector getLocalMethods(Vector methods, String className) {
	Vector v = new Vector();
	Enumeration e = methods.elements();
	while (e.hasMoreElements()) {
	    CIMMethod cm = (CIMMethod)e.nextElement();
	    if (cm.getOriginClass().equalsIgnoreCase(className)) {
		v.addElement(cm);
            }
	}
	return v;
    }

    CIMInstance getLocal(CIMInstance ci) {
	if (ci == null) {
	    return null;
	}
	ci.setProperties(getLocalProps(ci.getProperties(), ci.getClassName()));
	return ci;
    }

    CIMClass getLocal(CIMClass cc) {
	if (cc == null) {
	    return null;
	}
	cc.setProperties(getLocalProps(cc.getProperties(), cc.getName()));
	cc.setMethods(getLocalMethods(cc.getMethods(), cc.getName()));
	return cc;
    }
}
