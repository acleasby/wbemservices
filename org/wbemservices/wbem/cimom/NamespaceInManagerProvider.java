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
package org.wbemservices.wbem.cimom;

import javax.wbem.cim.CIMObjectPath;

/**
 *
 * This class is the provider for CIM_NamespaceInManager association. We deal
 * with it as a OneToManyAssociation, associating a singleton CIMOM to multiple
 * namespaces.
 *
 * @author     	Sun Microsystems, Inc.
 * @version	1.0 11/26/01
 * @since	WBEM 2.5
 */
class NamespaceInManagerProvider extends OneToManyAssocProvider {
    protected String getOneRole(CIMObjectPath assocName) {
	return "Antecedent";
    }

    protected String getManyRole(CIMObjectPath assocName) {
	return "Dependent";
    }

    protected CIMObjectPath getManyClass(CIMObjectPath assocName) {
	return NamespaceProvider.CLASSOP;
    }

    protected CIMObjectPath getOneClass(CIMObjectPath assocName) {
	return CIMOMProvider.CLASSOP;
    }
}
