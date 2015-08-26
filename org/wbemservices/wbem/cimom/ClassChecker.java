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
 *are Copyright ï¿½ 2001 Sun Microsystems, Inc.
 *
 *All Rights Reserved.
 *
 *Contributor(s): _______________________________________
*/

package org.wbemservices.wbem.cimom;

import javax.wbem.cim.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * This class does all semantic checks related to a class.
 *
 * @author Sun Microsystems, Inc.
 * @since WBEM 1.0
 */

class ClassChecker {
    CIMOMUtils cu;
    // XXX factory
    // ProviderChecker pc;
    PropertyChecker propCheck;
    MethodChecker methCheck;
    final static String INDICATIONQUALIFIER = "indication";

    public ClassChecker(CIMOMUtils cu) {
        this.cu = cu;
        // factory
        // this.pc = pc;
        propCheck = new PropertyChecker(cu);
        methCheck = new MethodChecker(cu);
    }

    // This is used during class modification. We compare the keys of the old
    // and new versions of the class and make sure that they are the same.
    void compareKeys(Vector oldKeys, Vector newKeys) throws
            CIMException {
        Map keyMap = new HashMap();
        Enumeration e = oldKeys.elements();
        if (oldKeys.size() != newKeys.size()) {
            throw new CIMPropertyException(CIMPropertyException.NEW_KEY,
                    "", "");
        }
        while (e.hasMoreElements()) {
            CIMProperty cp = (CIMProperty) e.nextElement();
            String key = cp.getOriginClass().toLowerCase() + ":" + cp.getName();
            keyMap.put(key, cp);
        }
        e = newKeys.elements();
        while (e.hasMoreElements()) {
            CIMProperty cp = (CIMProperty) e.nextElement();
            String key = cp.getOriginClass().toLowerCase() + ":" + cp.getName();
            CIMProperty oldcp = (CIMProperty) keyMap.get(key);
            if (oldcp == null) {
                throw new CIMPropertyException(CIMPropertyException.NEW_KEY,
                        key, "");
            }
            if (!oldcp.getType().equals(cp.getType())) {
                throw new CIMPropertyException(CIMPropertyException.NEW_KEY,
                        key, "");
            }
        }
    }

    // Check the sanity of class cc in namespace. If parentClass is passed in,
    // use parentClass, otherwise get the parent class from the repository.
    // parentClass is passed in when we want to make sure that the modification
    // of the parentClass does not cause semantic check failures in the child
    // class.
    void checkClassSanity(String namespace, CIMClass cc, CIMClass parentClass)
            throws CIMException {

        if (cc.getName().indexOf("_") < 0) {
            throw new CIMClassException(
                    CIMClassException.CIM_ERR_INVALID_PARAMETER,
                    cc.getName());
        }

        int keysRequired = 0;
        int refsRequired = 0;
        CIMQualifier qe;
        CIMScope curLevel = new CIMScope(CIMScope.CLASS);
        CIMClass superclass = parentClass;
        // if parentClass was not passed in, and the class has a superclass,
        // get it.
        if ((superclass == null) && (cc.getSuperClass().length() != 0)) {
            superclass =
                    cu.getPS().getClass(namespace, cc.getSuperClass().toLowerCase());
        }

        if ((superclass == null) && (cc.getSuperClass().length() != 0)) {
            throw new CIMClassException(
                    CIMClassException.CIM_ERR_INVALID_SUPERCLASS,
                    cc.getName(), cc.getSuperClass());
        }

        Vector qualifiers = cc.getQualifiers();
        // Inherit superclass qualifiers
        if (superclass != null) {
            cu.assignInheritedQualifiers(qualifiers,
                    superclass.getQualifiers());
        }

        // Check if this is an association. If it is an association, see
        // if we need to have references defined.
        qe = cc.getQualifier("association");
        if (qe == null) {
            qe = cu.createDefaultQualifier(namespace, "association");
        } else {
            cu.checkQualifier(namespace, qe);
        }
        CIMValue Tmp = qe.getValue();

        // Assuming that default value of ASSOCIATION qualifier is
        // false, (as per the CIM spec) i.e. to be true, it must be
        // explicitly initialized to value true.
        if ((Tmp != null) && Tmp.equals(CIMValue.TRUE)) {
            cc.setIsAssociation(true);
            curLevel = new CIMScope(CIMScope.ASSOCIATION);
            if ((superclass == null) || (!superclass.isAssociation())) {
                // if an association class has no superclass/the superclass
                // is not an association, we need to have at least two
                // references defined for the class.
                refsRequired = 2;
            }
        }

        cu.doCommonQualifierChecks(namespace, cc.getName(), qualifiers,
                curLevel);

        // Propagation of Key feature. If the superclass is keyed so is
        // this class.
        if (superclass != null) {
            cc.setIsKeyed(superclass.isKeyed());
        } else {
            cc.setIsKeyed(false);
        }

        qe = cc.getQualifier("abstract");
        if (qe == null) {
            qe = cu.createDefaultQualifier(namespace, "abstract");
        }
        if (qe.getValue().equals(CIMValue.TRUE)) {
            // If the current class is abstract and it has a
            // superclass make sure that the superclass is also
            // abstract
            if (superclass != null) {
                qe = superclass.getQualifier("abstract");
            }
        } else {
            // If the class is not abstract and the superclass is keyed
            // no more keys should be defined, otherwise at least one key
            // is required.
            if (cc.isKeyed()) {
                keysRequired = -1;
            } else {
                keysRequired = 1;
            }
        }

        // Check if this is an indication. If it is an indication, we do
        // not want keys.
        boolean isIndication = false;

        try {
            qe = cc.getQualifier(INDICATIONQUALIFIER);
            if (qe == null) {
                qe = cu.createDefaultQualifier(namespace, INDICATIONQUALIFIER);
            } else {
                cu.checkQualifier(namespace, qe);
            }
            Tmp = qe.getValue();
            // Assuming that default value of INDICATION qualifier is
            // false, (as per the CIM spec) i.e. to be true, it must be
            // explicitly initialized to value true.
            if ((Tmp != null) && Tmp.equals(CIMValue.TRUE)) {
                keysRequired = -1;
                isIndication = true;
            }
        } catch (CIMException indException) {
            // No indication qualifier in this namespace. Indications will
            // not be recognized. isIndication will remain false.
        }

        propCheck.checkPropertiesSanity(namespace, cc, keysRequired,
                refsRequired, superclass, isIndication);
        methCheck.checkMethodsSanity(namespace, cc, superclass);
    }
}
