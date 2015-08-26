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

import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMClass;
import javax.wbem.cim.CIMMethod;
import javax.wbem.cim.CIMQualifier;
import javax.wbem.cim.CIMValue;
import javax.wbem.cim.CIMSemanticException;
import javax.wbem.cim.CIMMethodException;
import javax.wbem.cim.CIMParameter;
import javax.wbem.cim.CIMScope;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;

class MethodChecker {
    CIMOMUtils cu;
    // XXX factory
    //ProviderChecker pc;

    public MethodChecker(CIMOMUtils cu) {
	this.cu = cu;
	// XXX factory
	//this.pc = pc;
    }

    private void assignInheritedMethods(String namespace, CIMClass cc,
						Hashtable overrideMap,
						CIMClass superclass) {
	if (superclass != null) {
	    Vector MethodTable = superclass.getAllMethods();
	    for (Enumeration methods = MethodTable.elements(); 
				 methods.hasMoreElements();) {
		CIMMethod me = (CIMMethod) methods.nextElement();
		CIMMethod mec = (CIMMethod)me.clone();
		String om = (String)overrideMap.get(
				    me.getOriginClass()+"."+ me.getName());
		if (om != null) {
		    mec.setOverridingMethod(om);
		}
		cc.addMethod(mec);
	    }
	}
    }

    // Handle method override. Maybe we can combine it with the previous
    // method to handle methods, references and properties.
    private void handleMethodOverride(String namespace, 
					CIMClass cc, 
					CIMMethod me,
					Hashtable overrideMap,
					CIMClass superclass)
					throws CIMException {

	CIMQualifier qe;
	qe = me.getQualifier("override");
	if (qe == null) {
	    return;
        }

	CIMValue ValueTable = qe.getValue();
	String Value = null; 
	if (ValueTable != null) {
	    Value = (String)(ValueTable.getValue());
	}

	if ((ValueTable == null) || (Value == null) || Value.equals("")) {
	    throw new CIMSemanticException(
	    CIMSemanticException.NO_QUALIFIER_VALUE,
	    me.getOriginClass()+"."+me.getName(), qe.getName());
	}

	String MethodName;
	String ClassName = null;
	int trenner = Value.indexOf(".");
	MethodName = Value.substring(trenner+1, Value.length());
	String fullName = MethodName;
	if (trenner > 0) {
	    ClassName = Value.substring(0, trenner);
	    fullName = ClassName+"."+MethodName;
	}

	CIMMethod ome = null;
	// search in all SuperClasses for MethodName
	if (superclass == null) {
	    throw new CIMMethodException(
		CIMMethodException.NO_OVERRIDDEN_METHOD,
		me.getOriginClass()+"."+me.getName(), fullName);
        }

	ome = superclass.getMethod(MethodName, ClassName);
	if (ome == null) {
	    throw new CIMMethodException(
		CIMMethodException.NO_OVERRIDDEN_METHOD,
		me.getOriginClass()+"."+me.getName(), fullName);
	}

	if (((ClassName != null) &&
		!ome.getOriginClass().equalsIgnoreCase(ClassName)) ||
	    !ome.getName().equalsIgnoreCase(MethodName)) {
	    throw new CIMMethodException(
	    CIMMethodException.METHOD_OVERRIDDEN,
	    me.getOriginClass()+"."+me.getName(),
	    fullName, ome.getOriginClass()+"."+ome.getName());
	}
	String tempom;
	tempom = (String)
	    overrideMap.get(ome.getOriginClass()+"."+ome.getName());

	if (tempom != null) {
	    throw new CIMMethodException(
	    CIMMethodException.METHOD_OVERRIDDEN,
	    me.getOriginClass()+"."+me.getName(),
	    ome.getOriginClass()+"."+ome.getName(), tempom);
	}

	// Inherit the overridden method's qualifiers if necessary.
	cu.assignInheritedQualifiers(me.getQualifiers(), ome.getQualifiers());

	// Note down the overriding information
	overrideMap.put(ome.getOriginClass()+"."+
					ome.getName(),
					me.getOriginClass()+"."+
					me.getName());
	// Check if types match here.
	// Check if it is ref-ref overriding or
	// prop-prop overriding. What if the property
	// has already been overridden?
	
    }

    private void handleMethodParameters(String namespace, CIMClass cc,
    CIMMethod me) throws CIMException {
	Vector pt = me.getParameters();

	// Must check for duplicate parameters?

	for (Enumeration pes = pt.elements(); 
	pes.hasMoreElements();) {
	    CIMParameter pe = (CIMParameter) pes.nextElement();
	    Vector qt = pe.getQualifiers();
	    cu.doCommonQualifierChecks(namespace, me.getOriginClass()+"."+
	    me.getName()+":"+pe.getName(), qt, 
	    CIMScope.getScope(CIMScope.PARAMETER));

	    // Check that data types are valid, and default values
	    // match the given data types.

	}

    }

    void checkMethodsSanity(String namespace, CIMClass cc,
		CIMClass superclass) throws CIMException {
	Vector mt = cc.getMethods();
	Hashtable overrideMap = new Hashtable();
	Hashtable methodNames = new Hashtable();

	// Must check for duplicate methods

	for (Enumeration mes = mt.elements(); 
	mes.hasMoreElements();) {
	    CIMMethod me = (CIMMethod) mes.nextElement();
	    me.setOriginClass(cc.getName());
	    if (methodNames.get(me.getName().toLowerCase()) != null) {
		throw new CIMMethodException(
		CIMMethodException.CIM_ERR_INVALID_PARAMETER,
		me.getName());
	    } else {
		methodNames.put(me.getName().toLowerCase(), "");
	    }
	    Vector qt = me.getQualifiers();
	    cu.doCommonQualifierChecks(namespace, me.getOriginClass()+"."+
	    me.getName(), qt, CIMScope.getScope(CIMScope.METHOD));

	    // Check that data types are valid, and default values
	    // match the given data types.

	    handleMethodParameters(namespace, cc, me);
	    // Handle override qualifier
	    handleMethodOverride(namespace, cc, me, overrideMap, superclass);
	}
	assignInheritedMethods(namespace, cc, overrideMap, superclass);
    }
}
