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

package org.wbemservices.wbem.client.adapter.rmi;

import javax.wbem.cim.CIMObjectPath;
import javax.wbem.cim.CIMScope;
import javax.wbem.cim.CIMParameter;
import javax.wbem.cim.CIMNameSpace;
import javax.wbem.cim.CIMMethod;
import javax.wbem.cim.CIMInstance;
import javax.wbem.cim.CIMFlavor;
import javax.wbem.cim.CIMDataType;
import javax.wbem.cim.CIMDateTime;
import javax.wbem.cim.CIMQualifier;
import javax.wbem.cim.CIMQualifierType;
import javax.wbem.cim.CIMValue;
import javax.wbem.cim.CIMClass;
import javax.wbem.cim.CIMProperty;

import java.util.Vector;
import java.util.Iterator;
import java.util.Enumeration;
import java.io.Serializable;

/** 
 * This class generates a check sum string for CIM elements. It does not invoke
 * toString methods, since they are subject to change and can cause version
 * compatibility problems.
 * 
 * 
 * @author	Sun Microsystems, Inc. 
 * @since	WBEM 1.0
 */
 
public class CheckSumGen implements Serializable {

    final static long serialVersionUID = 200;

    // Type values to reduce instanceof checks
    private final static int QUALS = 0;
    private final static int CPROPERTIES = 1;
    private final static int IPROPERTIES = 2;
    private final static int METHODS = 3;
    private final static int VALVECTOR = 4;
    private final static int PARAMETER = 5;
    private final static int FLAVOR = 6;
    private final static int SCOPE = 7;
    private final static String NULL = "null";
    final static String INDENT = "    ";
    final static String TAB    = "\t";
    final static String QUOTE  = "\"";
    final static String SPACE  = " ";
    final static String COLON  = ":";
    final static String LINE   = "\n";
    // CIMKeywords (Reserved words)
    private final static String ANY = "any";
    private final static String ASSOCIATION = "association";
    private final static String CLASS = "class";
    private final static String DISABLEOVERRIDE = "disableoverride";
    private final static String DT_BOOL = "boolean";
    private final static String DT_CHAR16 = "char16";
    private final static String DT_DATETIME = "datetime";
    private final static String DT_REAL32 = "real32";
    private final static String DT_REAL64 = "real64";
    private final static String DT_SINT16 = "sint16";
    private final static String DT_SINT32 = "sint32";
    private final static String DT_SINT64 = "sint64";
    private final static String DT_SINT8 = "sint8";
    private final static String DT_STR = "string";
    private final static String DT_UINT16 = "uint16";
    private final static String DT_UINT32 = "uint32";
    private final static String DT_UINT64 = "uint64";
    private final static String DT_UINT8 = "uint8";
    private final static String ENABLEOVERRIDE = "enableoverride";
    private final static String CIMFLAVOR = "Flavor";
    private final static String INDICATION = "indication";
    private final static String INSTANCE = "instance";
    private final static String METHOD = "method";
    private final static String OF = "of";
    private final static String CIMPARAMETER = "parameter";
    private final static String PROPERTY = "property";
    private final static String QUALIFIER = "qualifier";
    private final static String REFERENCE = "reference";
    private final static String RESTRICTED = "restricted";
    private final static String CIMSCOPE = "Scope";
    private final static String TOSUBCLASS = "tosubclass";
    private final static String TRANSLATABLE = "translatable";

    boolean cimclass = true;

    public CheckSumGen() {
    }

    public String vectorToMOFString(Vector v, int type) {
        return vectorToMOFString(v, INDENT, false, false, true, type);
    }
 
    public String vectorToMOFString(Vector v, String i, int type) {
        return vectorToMOFString(v, i, false, false, true, type);
    } 
    
    public String vectorToMOFString(Vector v, boolean b, int type) {
        return vectorToMOFString(v, INDENT, b, false, true, type);
    }

    public String vectorToMOFString(Vector v, boolean b, boolean d, int type) {
        return vectorToMOFString(v, INDENT, b, d, true, type);
    }
 
    public String vectorToMOFString(Vector v, boolean b, boolean d, boolean l,
    int type) {
        return vectorToMOFString(v, INDENT, b, d, l, type);
    }

    public String vectorToMOFString(Vector v, String indent, 
					   boolean bracketed,
					   boolean delim, 
					   boolean lf,
					   int type) {
	String delimeter = "";
	if (lf) {
	    delimeter = "," + LINE;
	} else {
	    delimeter = ", ";
	}
	 
	StringBuffer str = new StringBuffer("");
        if (v != null && !v.isEmpty()) {
	    if (bracketed) {
		str.append(indent);
		str.append("[");
	    }
	    int maxIndex = v.size() - 1;
            for (int x = 0; x <= maxIndex; x++) {
                if (x > 0) {
		    if (delim) {	
                    	str = str.append(delimeter + indent);
                    } else {
		        str = str.append(LINE);
		    }
		}

		Object elem = v.elementAt(x);
		switch (type) {
		    case QUALS: str.append(toString((CIMQualifier)elem));
				break;
		    case CPROPERTIES: str.append(toString((CIMProperty)elem, 
								true));
				break;
		    case IPROPERTIES: str.append(toString((CIMProperty)elem, 
								false));
				break;
		    case METHODS: str.append(toString((CIMMethod)elem));
				break;
		    case PARAMETER: str.append(toString((CIMParameter)elem));
				break;
		    case FLAVOR: str.append(toString((CIMFlavor)elem));
				break;
		    case SCOPE: str.append(toString((CIMScope)elem));
				break;
		    // The QUOTE should only be here for type string, but
		    // its a bug in the initial method, so we have to
		    // retain it for backward compatibility
		    case VALVECTOR: str.append(QUOTE);
				str.append(values(elem));
				str.append(QUOTE);
				break;
		}
            }
	    if (bracketed) {
	        str = str.append("]" + LINE);
	    }
        } 
	return str.toString();
    }

    /* 
     * This has been specifically introduced to handle vectors of parameters
     * Copied and modified the toString code of AbstractCollection
     */
    public String toString(Vector v) {
	if (v == null) {
	    return NULL;
	}
        StringBuffer buf = new StringBuffer();
        Iterator e = v.iterator();
        buf.append("[");
        int maxIndex = v.size() - 1;
        for (int i = 0; i <= maxIndex; i++) {
	    Object o = e.next();
	    // We actually only support CIMValue, but some people may have
	    // been passing in other stuff. We'll take care of CIMProperty
	    // for now. All other types become sensitive to their toString
	    // methods and could land up with checksum errors if they are
	    // changed.
	    if (o instanceof CIMValue) {
		buf.append(toString((CIMValue)o));
	    } else if (o instanceof CIMProperty) {
		buf.append(toString((CIMProperty)o, false));
	    } else {
		// If the toString methods of the input object has been changed
		// a checksum error will occur.
        	buf.append(o.toString());
	    }
            if (i < maxIndex) {
                buf.append(", ");
            }
        }
        buf.append("]");
        return buf.toString();
    }
    
    public String toString(CIMClass cc) {
	if (cc == null) {
	    return NULL;
	}
        String cl = CLASS + SPACE + cc.getName();
        if (cc.getSuperClass().length() > 0) {
            cl = cl.concat(COLON + cc.getSuperClass());
        }
        return new String(vectorToMOFString(cc.getQualifiers(), true, 
						true, QUALS) +
                          cl + LINE + "{" + LINE +
                          vectorToMOFString(cc.getProperties(), CPROPERTIES) 
			  + LINE +
                          vectorToMOFString(cc.getMethods(), METHODS) +
                          LINE + "};");

    }

    private String values(Object o) {
	if (o == null) {
	    return NULL;
	}
	if (o instanceof CIMObjectPath) {
	    return toString((CIMObjectPath)o);
	} else if (o instanceof CIMDateTime) {
	    return toString((CIMDateTime)o);
	} else if (o instanceof CIMInstance) {
	    // This one is new with embedded objects
	    return toString((CIMInstance)o);
	}
	// The rest are numbers or strings, and its fine to invoke their
	// toString methods
	return o.toString();
    }
 
    public String toString(CIMValue cv) {
	String s = NULL;
	if (cv == null) {
	    return NULL;
	}
	Object o = cv.getValue();
        if (o != null) {
	    if (cv.getType() != null && cv.getType().isArrayType()) {
		s = vectorToMOFString((Vector)o, "", false, true, false, 
		VALVECTOR);
	    } else {
            	String q = "";
            	if ((new CIMDataType(CIMDataType.STRING)).equals(
			cv.getType())) {
                    q = QUOTE;
            	}
            	s = q + values(o) + q;
	    }
        } 
        return s;
    }

    public String toString(CIMQualifierType cqt) {
	if (cqt == null) {
	    return NULL;
	}
	String scopeStr = "";
        String flavorStr = "";
        String valueStr = "";
	if (!cqt.getScope().isEmpty()) {
            scopeStr = CIMSCOPE + "(" + 
		       vectorToMOFString(cqt.getScope(), "", false, 
				         true, false, SCOPE) + ")";
	}

        if (!cqt.getFlavor().isEmpty()) {
            flavorStr = ", " + CIMFLAVOR + "(" + 
		       vectorToMOFString(cqt.getFlavor(), "", false, 
					 true, false, FLAVOR) + ")";
        }

        if (!cqt.getType().isArrayType()) {
            valueStr = " = " + toString(cqt.getDefaultValue());
        }
        return new String(QUALIFIER + SPACE + 
		          cqt.getName() + " : " + toString(cqt.getType()) + 
			  valueStr + ", " + scopeStr + flavorStr + ";");
    }

    public String toString(CIMQualifier cq) {
	if (cq == null) {
	    return NULL;
	}
        if (cq.getValue() == null) {
            return new String(cq.getName());
        } else {
            return new String(cq.getName() + "(" + toString(cq.getValue()) + 
			      ")");
        }
    }

    public String toString(CIMDateTime cdt) {
	if (cdt == null) {
	    return NULL;
	}
	return cdt.toString();
    }

    public String toString(CIMDataType cdt) {
	if (cdt == null) {
	    return NULL;
	}
        switch (cdt.getType()) {
            case CIMDataType.UINT8:  
		return new String(DT_UINT8);
            case CIMDataType.SINT8:  
		return new String(DT_SINT8);
            case CIMDataType.UINT16: 
		return new String(DT_UINT16);
            case CIMDataType.SINT16: 
		return new String(DT_SINT16);
            case CIMDataType.UINT32: 
		return new String(DT_UINT32);
            case CIMDataType.SINT32: 
		return new String(DT_SINT32);
            case CIMDataType.UINT64: 
		return new String(DT_UINT64);
            case CIMDataType.SINT64: 
		return new String(DT_SINT64);
            case CIMDataType.REAL32: 
		return new String(DT_REAL32);
            case CIMDataType.REAL64: 
		return new String(DT_REAL64);
            case CIMDataType.STRING: 
		return new String(DT_STR);
            case CIMDataType.CHAR16: 
		return new String(DT_CHAR16);
            case CIMDataType.DATETIME: 
		return new String(DT_DATETIME);
            case CIMDataType.BOOLEAN: 
		return new String(DT_BOOL);
            case CIMDataType.UINT8_ARRAY:  
		return new String(DT_UINT8 + "[]");
            case CIMDataType.SINT8_ARRAY:  
		return new String(DT_SINT8 + "[]");
            case CIMDataType.UINT16_ARRAY: 
		return new String(DT_UINT16+"[]");
            case CIMDataType.SINT16_ARRAY: 
		return new String(DT_SINT16+"[]");
            case CIMDataType.UINT32_ARRAY: 
		return new String(DT_UINT32+"[]");
            case CIMDataType.SINT32_ARRAY: 
		return new String(DT_SINT32+"[]");
            case CIMDataType.UINT64_ARRAY: 
		return new String(DT_UINT64+"[]");
            case CIMDataType.SINT64_ARRAY: 
		return new String(DT_SINT64+"[]");
            case CIMDataType.REAL32_ARRAY: 
		return new String(DT_REAL32+"[]");
            case CIMDataType.REAL64_ARRAY: 
		return new String(DT_REAL64+"[]");
            case CIMDataType.STRING_ARRAY: 
		return new String(DT_STR+"[]");
            case CIMDataType.CHAR16_ARRAY: 
		return new String(DT_CHAR16+"[]");
            case CIMDataType.DATETIME_ARRAY: 
		return new String(DT_DATETIME+"[]");
            case CIMDataType.BOOLEAN_ARRAY: 
		return new String(DT_BOOL+"[]");
            case CIMDataType.NULL: 
		return new String(NULL);
            case CIMDataType.REFERENCE: 
		return new String(REFERENCE + 
				  "(" + cdt.getRefClassName() + ")");
            default: return "";
        }
    }

    public String toString(CIMFlavor cf) {
	if (cf == null) {
	    return NULL;
	}
	switch (cf.getFlavor()) {
            case (CIMFlavor.ENABLEOVERRIDE): 
		return (ENABLEOVERRIDE);
            case (CIMFlavor.DISABLEOVERRIDE): 
		return (DISABLEOVERRIDE);
            case (CIMFlavor.RESTRICTED): 
		return (RESTRICTED);
            case (CIMFlavor.TOSUBCLASS): 
		return (TOSUBCLASS);
            case (CIMFlavor.TRANSLATE): 
		return (TRANSLATABLE);
            default: 
		return ("UNKNOWN");
        }
    }

    public String toString(CIMInstance ci) {
	if (ci == null) {
	    return NULL;
	}
	cimclass = false;
	String tmp = new String(INSTANCE + SPACE + 
				OF + SPACE + 
				ci.getClassName() +
			 	SPACE + "{" + LINE + 
                          	vectorToMOFString(ci.getProperties(), 
				IPROPERTIES) + 
                          	LINE + "};");
	cimclass = true;
	return tmp;
    }

    public String toString(CIMMethod cp) {
	if (cp == null) {
	    return NULL;
	}
	return new String(INDENT + toString(cp.getType()) + " " + cp.getName() +
				"(" + vectorToMOFString(cp.getParameters(), 
				"", false, true, false, PARAMETER) + ");");
    }

    public String toString(CIMNameSpace ns) {
	if (ns == null) {
	    return NULL;
	}
	return "//" + ns.getHost() + '/' + ns.getNameSpace();
    }

    /* 
     * This has been specifically introduced to handle vectors of parameter
     * qualifiers
     */
    private String pqToString(Vector v) {
	if (v == null) {
	    return NULL;
	}
        StringBuffer buf = new StringBuffer();
        Iterator e = v.iterator();
        buf.append("[");
        int maxIndex = v.size() - 1;
        for (int i = 0; i <= maxIndex; i++) {
	    Object o = e.next();
	    buf.append(toString((CIMQualifier)o));
            if (i < maxIndex) {
                buf.append(", ");
            }
        }
        buf.append("]");
        return buf.toString();
    }

    public String toString(CIMParameter cp) {
	if (cp == null) {
	    return NULL;
	}
	return new String(pqToString(cp.getQualifiers()) +
                          SPACE + toString(cp.getType()) + 
			  SPACE + cp.getName());

    }

    public String toString(CIMProperty cp, boolean cimclass) {
	if (cp == null) {
	    return NULL;
	}
	if (cimclass) {
            return new String(vectorToMOFString(
	    	cp.getQualifiers(), TAB, true, true, true, QUALS) +
            	INDENT + toString(cp.getType()) + SPACE + cp.getName() + ";");
	} else {
            return new String(
            	INDENT + cp.getName() + " = " + toString(cp.getValue()) + ";");
	}
    }

    public String toString(CIMScope cs) {
	if (cs == null) {
	    return NULL;
	}
	switch (cs.getScope()) {
            case CIMScope.SCHEMA     : return ("schema");
            case CIMScope.CLASS      : return (CLASS);
            case CIMScope.ASSOCIATION: return (ASSOCIATION);
            case CIMScope.INDICATION : return (INDICATION);
            case CIMScope.PROPERTY   : return (PROPERTY);
            case CIMScope.REFERENCE  : return (REFERENCE);
            case CIMScope.METHOD     : return (METHOD);
            case CIMScope.PARAMETER  : return (CIMPARAMETER);
            case CIMScope.ANY        : return (ANY);
            default            	     : return ("UNKNOWN");
        }
    }

    public String toString(CIMObjectPath op) {
	if (op == null) {
	    return NULL;
	}
	Vector keys = op.getKeys();
	String nameSpace = op.getNameSpace();
	String objectName = op.getObjectName();
	if ((keys == null) || (keys.size() == 0)) {
	    if (objectName != null && objectName.length() != 0) {
		return nameSpace + '/' + objectName;
	    } else {
		return nameSpace;
	    }
	} else {
	    String s = "";
	    int i = 0;
	    for (Enumeration e = keys.elements(); e.hasMoreElements(); ) {
		CIMProperty pe = (CIMProperty)e.nextElement();
		if (pe != null) {
		    String oc = pe.getOriginClass();
		    if ((oc != null) && (oc.length() != 0)) {
			s = s + oc + "." + pe.getName();
		    } else {
			s = s + pe.getName();
		    }
		    s = s + "=" + toString(pe.getValue()) + ":";
		    i++;
		}
	    }
	    if (i > 0) {
		return nameSpace + '/' + objectName + ":" +
		s.substring(0, s.lastIndexOf(":"));
	    } else {
		return nameSpace + '/' + objectName;
	    }
	}
    }

}
