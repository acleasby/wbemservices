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

package org.wbemservices.wbem.repository;

import javax.wbem.cim.*;

import java.util.*;


// This class should be package protected. Due to a jdk serilization bug
// we have to make it public for now.
public class CIMAssocClassRlogEntry extends CIMClassRlogEntry {
    private static final long serialVersionUID = -5517300490476497950L;

    private static class AssocRole implements java.io.Serializable {
	public String cc;
	public String role;

	public AssocRole(String cc, String role) {
	    this.cc = cc;
	    this.role = role;
	}
    }

    private AssocRole[] roles = new AssocRole[0];
 
    public CIMAssocClassRlogEntry(CIMNameSpaceRlogEntry nsentry,
				  String name,
				  String supercc) {
	    super(nsentry, name, supercc);
    }

    public void createAssciations(CIMClass cc) throws CIMException  {
	roles = createAssociationClassPropList(cc);
	addClassAssociations();
    }


    public void addSubClass(CIMClassRlogEntry cs) throws CIMException {
	if (cs instanceof CIMAssocClassRlogEntry) {
	    super.addSubClass(cs);
	} else {
	    throw new CIMClassException(
		CIMClassException.CIM_ERR_INVALID_CLASS, cs.getName());
	}
    }

    private AssocRole[] createAssociationClassPropList(CIMClass cc) {
	ArrayList al = new ArrayList();
	for (Enumeration e = cc.getProperties().elements();
					e.hasMoreElements();) {
	    CIMProperty pe = (CIMProperty)e.nextElement();
	    if (pe.isReference()) {
		al.add(new AssocRole(
		   pe.getType().getRefClassName(), pe.getName()));
	    }
	}

	AssocRole[] arr = new AssocRole[al.size()];
	al.toArray(arr);
	return arr;
    }

    public void validateClassAsociations()
	throws CIMException {
	
        for (int i = 0; i < roles.length; i++) {
	    CIMClassRlogEntry cctemp = nsentry.getClass(roles[i].cc);
	    if (cctemp == null) {
		throw new CIMClassException(
		    CIMClassException.CIM_ERR_NOT_FOUND, roles[i].cc);
	    }
	}
    }

    private void addClassAssociations()
	throws CIMException {
	validateClassAsociations();
	for (int i = 0; i < roles.length; i++) {
	    CIMClassRlogEntry cctemp = nsentry.getClass(roles[i].cc);
	    cctemp.addAssociation(this);
	}
    }

    public void removeClassAssociations()
	throws CIMException {
	validateClassAsociations();
	for (int i = 0; i < roles.length; i++) {
	    CIMClassRlogEntry cctemp = nsentry.getClass(roles[i].cc);
	    cctemp.removeAssociation(getName());
	}
    }

    public ArrayList matchRole(String cname, 
			String role,
			String resultRole ) {
	ArrayList v = new ArrayList();
	boolean match = false;
	boolean rolematch = false;
	boolean isReflective = false;

	// Is it a reflective association
	for (int k = 0; k < roles.length; k++) {
	    if (roles[k].cc.equalsIgnoreCase(cname)) {

		// match role
		if (role != null && role.length() != 0) {
		    if (roles[k].role.equalsIgnoreCase(role)) {
			rolematch = true;
		    }
		} else {
		    rolematch = true;
		}

		if (match) {
		    isReflective = true;
		}
		match = true;
	    }
	}

	if (!match || !rolematch) {
	     return v;
	}

	for (int i = 0; i < roles.length; i++) {
	    if (!isReflective &&
		roles[i].cc.equalsIgnoreCase(cname)) { 
		continue;
	    }

	    // match resultRole
	    if (resultRole != null && resultRole.length() != 0 &&
		!roles[i].role.equalsIgnoreCase(resultRole)) {
		continue;
	    }

	    // filter the duplicate
	    boolean exist = false;
	    for (int k = 0; k < v.size(); k++) {
		if (roles[i].cc.equalsIgnoreCase((String)v.get(k))) {
		    exist = true;
		}
	    }

	    if (!exist) {
		v.add(roles[i].cc);
	    }
	}
	return v;
    }
}
