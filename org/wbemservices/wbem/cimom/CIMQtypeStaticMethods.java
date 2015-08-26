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

// This file contains methods which should actually be static methods on the
// CIMQualifierType class. It deals with creation and deletion of 
// CIMQualifierType instances - but we are separating it from CIMQualifierType
// since the methods will only work within the context of the CIMOM and not
// in the context of a client application which will be using the 
// CIMQualifierType class.

package org.wbemservices.wbem.cimom;

import javax.wbem.cim.CIMNameSpace;
import javax.wbem.cim.CIMObjectPath;
import javax.wbem.cim.CIMQualifierType;
import javax.wbem.cim.CIMQualifier;
import javax.wbem.cim.CIMClass;
import javax.wbem.cim.CIMProperty;
import javax.wbem.cim.CIMMethod;
import javax.wbem.cim.CIMFlavor;
import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMSemanticException;

import java.util.Vector;
import java.util.Enumeration;

class CIMQtypeStaticMethods {

    // This needs to be moved to a CIMOMUtils, or we have to modify the
    // persistent store interface to accept object paths.
    private static String getNameSpace(CIMNameSpace ns, CIMObjectPath op) {
	String s = ns.getNameSpace() + '/' + op.getNameSpace();
	CIMNameSpace cns = new CIMNameSpace();
	cns.setNameSpace(s);
	return cns.getNameSpace();
    }

    // Assigns the default flavor to a qualifier type
    private static void assignQualifierTypeDefaultFlavor(CIMQualifierType qt) {
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


    private static void csQualifierType(CIMNameSpace nameSpace, 
					CIMObjectPath objectName,
					CIMQualifierType qt,
					boolean set) 
    throws CIMException {
	try {
	    // Should checking of scope and flavor inconsistencies be done?
	    // I think not... should be done in addScope and addFlavor.
	    if (qt.getName().length() == 0) {
		throw new CIMSemanticException(
		CIMSemanticException.INVALID_QUALIFIER_NAME,
		qt.getName());
            }

	    // following two checks should be done in setDefaultTable.
	    /*
	    if (qt.isArrayValue() && 
	    !correctArraySize(qt.getSize(), qt.getDefaultTable())) {
		throw new CIMException("Qualifier Type " + qt.getName() + 
		" array size and number of array " + "elements mismatch");
	    }
	    */
	    // check types of default values.
	    // should we throw an exception when the default qualifiers are
	    // meaningless?
	    assignQualifierTypeDefaultFlavor(qt);
	    // Check if the name already exists. We have to make sure that
	    // the operation is atomic i.e. checking for existence and then
	    // finally adding.
	    if (!set) {
		CIMOMImpl.ps.addCIMElement(getNameSpace(nameSpace, objectName), 
		qt);
            } else
	    {
		// check if qualifier is being used
		// If it is, the qualifier cannot be modified
		// else pass the request through to the persistent
		// store.
		if (checkIfQualifierInUse(nameSpace, objectName, qt)) {
		    // Qualifier is in use
		    throw new CIMException(CIMException.CIM_ERR_FAILED);
		} else {
		    CIMOMImpl.ps.setQualifierType(getNameSpace(nameSpace, 
		    				    objectName), qt);
		}
	    }

	    LogFile.add(LogFile.DEBUG, "ADD_QUALIFIER_DEBUG", qt);
	} catch (CIMException e) {
	    LogFile.add(LogFile.DEVELOPMENT, "CAUGHT_EXCEPTION", e.toString());
	    LogFile.methodReturn("addCIMElement(qualifiertype)");
	    if (CIMOMImpl.verbose) { e.printStackTrace();
            }
	    throw e;
	} catch (Exception e) {
	    if (CIMOMImpl.verbose) { e.printStackTrace();
            }
	    throw new CIMException(CIMException.CIM_ERR_FAILED, e);
	}
    }

    private static void deleteQualifierType(CIMNameSpace nameSpace, 
					CIMObjectPath objectName,
					CIMQualifierType qt)
    throws CIMException {
	try {
	    // Should checking of scope and flavor inconsistencies be done?
	    // I think not... should be done in addScope and addFlavor.
	    if (objectName.getObjectName().length() == 0) {
		throw new CIMSemanticException(
		CIMSemanticException.INVALID_QUALIFIER_NAME,
		objectName.getObjectName());
            }

	    // check if qualifier is being used
	    // If it is, the qualifier cannot be modified
	    // else pass the request through to the persistent
	    // store.
	    if (checkIfQualifierInUse(nameSpace, objectName, qt)) {
		// Qualifier is in use
		throw new CIMException(CIMException.CIM_ERR_FAILED);
	    } else {
		CIMOMImpl.ps.deleteQualifier(getNameSpace(nameSpace, 
						objectName),
						objectName.getObjectName());
	    }
	    LogFile.add(LogFile.DEBUG, "DELETE_QUALIFIER_DEBUG",
	    		objectName.getObjectName());
	} catch (CIMException e) {
	    LogFile.add(LogFile.DEVELOPMENT, "CAUGHT_EXCEPTION", e.toString());
	    LogFile.methodReturn("removeCIMElement(qualifiertype)");
	    if (CIMOMImpl.verbose) { e.printStackTrace();
            }
	    throw e;
	} catch (Exception e) {
	    if (CIMOMImpl.verbose) { e.printStackTrace();
            }
	    throw new CIMException(CIMException.CIM_ERR_FAILED, e);
	}
    }


    static void addCIMElement(CIMNameSpace nameSpace, 
					CIMObjectPath objectName,
					CIMQualifierType qt) 
					throws CIMException {
	nameSpace.setNameSpace('/'+nameSpace.getNameSpace());
	csQualifierType(nameSpace, objectName, qt, false);
    }

    static void setCIMElement(CIMNameSpace nameSpace, 
					CIMObjectPath objectName,
					CIMQualifierType qt) 
					throws CIMException {
	nameSpace.setNameSpace('/'+nameSpace.getNameSpace());
	csQualifierType(nameSpace, objectName, qt, true);
    }

    static void removeCIMElement(CIMNameSpace nameSpace, 
				CIMObjectPath objectName)
					throws CIMException {
	nameSpace.setNameSpace('/'+nameSpace.getNameSpace());
	CIMQualifierType qt = null;
	qt = CIMOMImpl.ps.getQualifierType(getNameSpace(nameSpace, 
				    objectName), 
				    objectName.getObjectName().toLowerCase());
	if (qt == null) {
	    throw new CIMException(CIMException.CIM_ERR_NOT_FOUND,
	    			    objectName.getObjectName());
	}
	deleteQualifierType(nameSpace, objectName, qt);
    }

    static CIMQualifierType getQualifierType(CIMNameSpace nameSpace, 
					CIMObjectPath objectName)
					throws CIMException {
	nameSpace.setNameSpace('/'+nameSpace.getNameSpace());
	CIMQualifierType qt;
	qt = CIMOMImpl.ps.getQualifierType(getNameSpace(nameSpace, objectName),
				   objectName.getObjectName().toLowerCase());
	return qt;
    }

    static Vector enumQualifierTypes(CIMNameSpace ns, CIMObjectPath inpath)
    throws CIMException {

	ns.setNameSpace('/'+ns.getNameSpace());
	String nameSpace = getNameSpace(ns, inpath);
	CIMObjectPath path = new CIMObjectPath();
	path.setNameSpace(nameSpace);
	path.setObjectName(inpath.getObjectName());

	Vector v;
	v = CIMOMImpl.ps.enumerateQualifierTypes(path);
	return v;
    }

    static boolean checkIfQualifierInUse(CIMNameSpace ns, 
					CIMObjectPath objectName,
					CIMQualifierType qt) 
    throws CIMException {


	String nameSpace = getNameSpace(ns, objectName);
	CIMObjectPath path = new CIMObjectPath();
	path.setNameSpace(nameSpace);
	Vector allClasses = CIMOMImpl.ps.enumerateClasses(path, true);
	if (allClasses != null && allClasses.size() != 0) {
	    Enumeration classEnum = allClasses.elements();
	    while (classEnum.hasMoreElements()) {
	    	CIMObjectPath cop = (CIMObjectPath)classEnum.nextElement();
		CIMClass cimclass = CIMOMImpl.ps.getClass(cop);
		CIMQualifier classQual = 
			cimclass.getQualifier(qt.getName());
		if (classQual != null) {
		    // Qualifier is in use
		    return true;
		}
		// The class does not have this qualifier.
		// Check properties and methods
		Vector props = cimclass.getProperties();
		if (props != null && props.size() != 0) {
		    Enumeration propEnum = props.elements();
		    while (propEnum.hasMoreElements()) {
		    	CIMProperty property = 
				(CIMProperty)propEnum.nextElement();
			CIMQualifier propQual =
				property.getQualifier(qt.getName());
			if (propQual != null) {
			    // Qualifier is in use
			    return true;
			}
		    }
		}

		// None of the properties have this qualifier
		// Check the methods
		Vector methods = cimclass.getMethods();
		if (methods != null && methods.size() != 0) {
		    Enumeration methodEnum = methods.elements();
		    while (methodEnum.hasMoreElements()) {
		    	CIMMethod meth = 
				    (CIMMethod)methodEnum.nextElement();
			CIMQualifier methQual =
				meth.getQualifier(qt.getName());
			if (methQual != null) {
			    // Qualifier is in use
			    return true;
			}
		    }
		}
	    }
	}
	return false;
    }
}

