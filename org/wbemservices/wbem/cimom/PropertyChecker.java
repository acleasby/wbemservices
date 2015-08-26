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
 *Contributor(s): WBEM Solutions, Inc.
 */

package org.wbemservices.wbem.cimom;

import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMClass;
import javax.wbem.cim.CIMProperty;
import javax.wbem.cim.CIMPropertyException;
import javax.wbem.cim.CIMDataType;
import javax.wbem.cim.CIMQualifier;
import javax.wbem.cim.CIMValue;
import javax.wbem.cim.CIMScope;
import javax.wbem.cim.CIMSemanticException;
import javax.wbem.cim.CIMClassException;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;

class PropertyChecker {
    CIMOMUtils cu;
    // XXX factory
    //ProviderChecker pc;
    private Hashtable reservedSQL;

    public PropertyChecker(CIMOMUtils cu) {
	this.cu = cu;
	// XXX factory
	//this.pc = pc;

	// Bug:4263038 Check if the property name is reserved SQL
	// keyword. If we allow such property names, it would not
	// be possible to construct well formed SQL selects for
	// these properties.
	reservedSQL = new Hashtable();
	reservedSQL.put("select", "");
	reservedSQL.put("from", "");
	reservedSQL.put("where", "");
	reservedSQL.put("or", "");
	reservedSQL.put("and", "");
	reservedSQL.put("not", "");
	// bug:4263038 continues further down.
    }

    void checkPropertiesSanity(String namespace, CIMClass cc, 
    int keysRequired, int refsRequired, CIMClass superclass, 
    boolean isIndication) 
    throws CIMException {
	Vector prt = cc.getProperties();
	Hashtable overrideMap = new Hashtable();
	Hashtable propNames = new Hashtable();

	// Must check for duplicate properties/refs.

	for (Enumeration proprefs = prt.elements(); 
	proprefs.hasMoreElements();) {
	    CIMProperty pe = (CIMProperty) proprefs.nextElement();

	    if (pe.getName() == null) {
		throw new CIMPropertyException(
		CIMPropertyException.CIM_ERR_INVALID_PARAMETER);
	    }

	    CIMDataType cdt = pe.getType();
	    if (cdt == null) {
		throw new CIMPropertyException(
		CIMPropertyException.CIM_ERR_INVALID_PARAMETER,
		pe.getName());
	    }
	    int cdtt = cdt.getType();
	    if (cdtt >= CIMDataType.NULL || cdtt <= CIMDataType.INVALID) {
		throw new CIMPropertyException(
		CIMPropertyException.CIM_ERR_INVALID_PARAMETER,
		pe.getName());
	    }

	    // Bug:4263038 Check if the property name is reserved SQL
	    // keyword. If we allow such property names, it would not
	    // be possible to construct well formed SQL selects for
	    // these properties.
	    if (reservedSQL.get(pe.getName().toLowerCase()) != null) {
		throw new CIMPropertyException(
		CIMPropertyException.CIM_ERR_INVALID_PARAMETER,
		pe.getName());
	    }
	    // End bug:4263038
	    if (propNames.get(pe.getName().toLowerCase()) != null) {
		throw new CIMPropertyException(
		CIMPropertyException.CIM_ERR_INVALID_PARAMETER,
		pe.getName());
	    } else {
		propNames.put(pe.getName().toLowerCase(), "");
	    }

	    pe.setOriginClass(cc.getName());
	    Vector qt = pe.getQualifiers();
	    // Check the qualifiers
	    if (pe.isReference()) {
		if (!cc.isAssociation()) { throw new 
		    CIMPropertyException(CIMPropertyException.CLASS_REFERENCE,
		    pe.getOriginClass()+"."+pe.getName(), cc.getName());
                }

		cu.doCommonQualifierChecks(namespace, pe.getOriginClass()+"."+
		    pe.getName(), qt, CIMScope.getScope(CIMScope.REFERENCE));
		refsRequired--;
	    }
	    else {
		cu.doCommonQualifierChecks(namespace, pe.getOriginClass()+"."+
		    pe.getName(), qt, CIMScope.getScope(CIMScope.PROPERTY));
            }

	    // Check that data types are valid, and default values
	    // match the given data types.
	    try {
		pe.setValue(cu.typeConvert(pe.getType(), pe.getValue()));
	    } catch (Exception e) {
		throw new CIMPropertyException(CIMSemanticException.TYPE_ERROR,
			new Object[] {
			pe.getOriginClass()+"."+pe.getName(), cc.getName(),
			pe.getType(), pe.getValue().getType(), pe.getValue()});
	    }

	    boolean isKey = pe.isKey();

	    // Do not allow array types as keys
	    if (isKey && pe.getType().isArrayType()) {
		throw new CIMPropertyException(
		CIMException.CIM_ERR_INVALID_PARAMETER, pe);
	    }

	    // Handle override qualifier
	    boolean newKey = handlePropertyOverride(namespace, cc, pe, 
						overrideMap, superclass);

	    // See if new key is being defined. This will only happen if
	    // the property is a key and is not overriding another key.
	    // handlePropertyOverride makes sure that a key is overridden
	    // if and only if the overriding is by another key.
	    CIMQualifier qe = pe.getQualifier("override");
	    if (((qe == null) && isKey) ||
	    ((qe != null) && newKey)) {
		if (cc.isKeyed() || isIndication) {
		    throw new CIMPropertyException(
			CIMPropertyException.NEW_KEY,
			pe.getOriginClass()+"."+pe.getName(), cc.getName());
                } else {
		    keysRequired = -1;
                }
	    }
	}

	if ((keysRequired == -1) && !isIndication) {
	    cc.setIsKeyed(true);
	}
	if (keysRequired > 0) {
	    throw new CIMClassException(CIMClassException.KEY_REQUIRED,
					    cc.getName());
	}
	if (refsRequired > 0) {
	    throw new CIMClassException(CIMClassException.REF_REQUIRED,
				      cc.getName());
	}

	assignInheritedProperties(namespace, cc, overrideMap, superclass);
    }

    // Handles override qualifier for properties and references.
    private boolean handlePropertyOverride(String namespace, 
					CIMClass cc, 
					CIMProperty pe,
					Hashtable overrideMap, 
					CIMClass superclass)
					throws CIMException {

	boolean newKey = false;
	CIMQualifier qe;
	qe = pe.getQualifier("override");
	if (qe == null) {
	    return false;
        }

	CIMValue ValueTable = qe.getValue();
	String Value = null; 
	if (ValueTable != null) {
	    Value = (String)(ValueTable.getValue());
	}

	if ((ValueTable == null) || (Value == null) || Value.equals("")) {
	    throw new CIMSemanticException(
		CIMSemanticException.NO_QUALIFIER_VALUE,
		pe.getOriginClass()+"."+pe.getName(), qe.getName());
	}

	String PropertyName;
	String ClassName = null;
	// CHANGED from indexOf(":") to indexOf(".")
	int trenner = Value.indexOf(".");
	PropertyName = Value.substring(trenner+1, Value.length());
	String fullName = PropertyName;
	if (trenner > 0) {
	    ClassName = Value.substring(0, trenner);
	    fullName = ClassName+"."+PropertyName;
	}

	CIMProperty ope = null;
	// search in all SuperClasses for PropertyName
	if (superclass == null) {
	    throw new CIMPropertyException(
		CIMPropertyException.NO_OVERRIDDEN_PROPERTY,
		fullName, pe.getOriginClass()+"."+pe.getName());
	}

	ope = superclass.getProperty(PropertyName, ClassName);
	if (ope == null) {
	    throw new CIMPropertyException(
		CIMPropertyException.NO_OVERRIDDEN_PROPERTY,
		pe.getOriginClass()+"."+pe.getName(), fullName);
	}

	if (((ClassName != null) &&
		!ope.getOriginClass().equalsIgnoreCase(ClassName)) ||
	    !ope.getName().equalsIgnoreCase(PropertyName)) {
	    throw new CIMPropertyException(
	    CIMPropertyException.PROPERTY_OVERRIDDEN,
	    pe.getOriginClass()+"."+pe.getName(),
	    fullName, ope.getOriginClass()+"."+ope.getName());
	}

	// Check if the property has been overridden by
	// property in this class.
	String tempop;
	tempop = (String)
	    overrideMap.get(ope.getOriginClass()+"."+ope.getName());
	if (tempop != null) {
	    throw new CIMPropertyException(
	    CIMPropertyException.PROPERTY_OVERRIDDEN,
	    pe.getOriginClass()+"."+pe.getName(),
	    ope.getOriginClass()+"."+ope.getName(), tempop);
	}
	// Inherit the overridden property's qualifiers
	// if necessary.
	cu.assignInheritedQualifiers(pe.getQualifiers(), ope.getQualifiers());
	if (pe.getQualifier("key") != null) {
	    pe.setKey(true);
	}

	// Make sure that Key qualifiers of both are
	// the same
	boolean isKeyOpe = ope.isKey();
	boolean isKeyPe = pe.isKey();
	if (isKeyOpe && !isKeyPe) {
	    throw new CIMPropertyException(CIMPropertyException.KEY_OVERRIDE,
	    pe.getOriginClass()+"."+pe.getName(),
	    ope.getOriginClass()+"."+ope.getName());
        }

	if (isKeyPe && !isKeyOpe) {
	    newKey = true;
        }

	// Note down the overriding information
	overrideMap.put(ope.getOriginClass()+"."+ ope.getName(),
			pe.getOriginClass()+"."+ pe.getName());

	// Check if types match here.
	// Check if it is ref-ref overriding or
	// prop-prop overriding.
	return newKey;
    }

    // add all the properties in the class hierarchy.
    private void assignInheritedProperties(String namespace, CIMClass cc,
						Hashtable overrideMap,
						CIMClass superclass) {
		if (superclass != null) {
			Vector PropTable = superclass.getAllProperties();
			for (Enumeration proprefs = PropTable.elements(); 
				 proprefs.hasMoreElements();) {
				CIMProperty pe = 
				(CIMProperty) proprefs.nextElement();
				CIMProperty pec = (CIMProperty)pe.clone();
				String op = (String)overrideMap.get(
				    pe.getOriginClass()+"."+ pe.getName());
				if (op != null) {
				    pec.setOverridingProperty(op);
				}
				cc.addProperty(pec);
			}
		}
    }
}
