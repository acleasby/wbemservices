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
 *Contributor(s): AppIQ, Inc.____________________________
*/

package org.wbemservices.wbem.repository;

import javax.wbem.cim.*;

import java.util.*;

class CIMAssocInstanceRlogEntry extends CIMInstanceRlogEntry {

    private static final long serialVersionUID = -2882512481198693722L;
    
    private static class AssocInsRole implements java.io.Serializable {
	public CIMClassRlogEntry ccentry;
	public String ci;
	public String role;
	public CIMObjectPath op;

	public AssocInsRole(CIMClassRlogEntry ccentry,
			String ci, String role, CIMObjectPath op) {
	    this.ccentry = ccentry;
	    this.ci = ci;
	    this.role = role;
	    this.op = op;
	}
    }

    private CIMObjectPath sp;
    private AssocInsRole[] roles = new AssocInsRole[0];

    public CIMAssocInstanceRlogEntry(
			CIMNameSpaceRlogEntry nsentry,
			String name) {
	super(nsentry, name);
    }

    public void createAssciations(CIMInstance ci) throws CIMException {
	sp = new CIMObjectPath(ci.getClassName(),
			       ci.getKeys());
	sp.setNameSpace(nsentry.getName());
	roles = createAssociationInsPropList(ci);
    }

    private AssocInsRole[] createAssociationInsPropList(CIMInstance ci)
	throws CIMException {
	ArrayList v = new ArrayList();
	try {
	    for (Enumeration e = ci.getProperties().elements();
					e.hasMoreElements();) {
		CIMProperty pe = (CIMProperty)e.nextElement();
		CIMValue cv = pe.getValue();

		// BUGFIX. 07/11/02. Ignore non-REF properties
		if (pe.isReference()){
			// REFs must be initialized
			if (cv == null || cv.getValue() == null) {
			    throw new CIMException(
				CIMPropertyException.CIM_ERR_INVALID_PARAMETER, pe);
			}

			Object op = cv.getValue();
			if (op instanceof CIMObjectPath) {
			    String ccstr = ((CIMObjectPath)op).getObjectName();
                            String ns = ((CIMObjectPath)op).getNameSpace();
                            if (ns.charAt(0) != '/') {
                                ns = '/' + ns;
                            }
                            CIMNameSpaceRlogEntry nsentryTMP = 
				PSRlogImpl.getNameSpaceEntryFromMap(ns);
			    CIMClassRlogEntry cctemp =
				nsentryTMP.getClass(ccstr);
			    CIMClass cc =
				(CIMClass)PSRlogImpl.deserialize(cctemp.getValue());
			    CIMInstance nci = cc.newInstance();
			    nci.updatePropertyValues(((CIMObjectPath)op).getKeys());
			    // <PJA> 16-Jan-2003
			    // BUGFIX 655351 getInstance fails for associators
			    // use new key for instances
			    v.add(new AssocInsRole(cctemp, InstanceNameUtils.getInstanceNameKey(nci), 
					pe.getName(), (CIMObjectPath)op));
			}
		}
	    }
	} catch (CIMException e) {
	    throw e;
	} catch (Exception e) {
	    throw new CIMException(CIMException.CIM_ERR_FAILED, e.toString());
	}
	AssocInsRole[] arr = new AssocInsRole[v.size()];
	v.toArray(arr);
	return arr;
    }

    public void validateInstanceAssociations()
	throws CIMException {
	for (int i = 0; i < roles.length; i++) {
	    CIMInstanceRlogEntry citemp =
		roles[i].ccentry.getInstance(roles[i].ci);
	    if (citemp == null) {
		throw new CIMInstanceException(
		    CIMInstanceException.CIM_ERR_NOT_FOUND, roles[i].ci);
	    }
	}
    }

    public void addInstanceAssociations(String assoc,
			CIMAssocInstanceRlogEntry ciref)
	throws CIMException {
	validateInstanceAssociations();
	for (int i = 0; i < roles.length; i++) {
	    CIMInstanceRlogEntry citemp =
		roles[i].ccentry.getInstance(roles[i].ci);
	    citemp.addAssociations(assoc, ciref);
	}
    }

    public void removeInstanceAssociations(String assoc, String iname)
	throws CIMException {
	validateInstanceAssociations();
	for (int i = 0; i < roles.length; i++) {
	    CIMInstanceRlogEntry citemp =
		roles[i].ccentry.getInstance(roles[i].ci);
	    citemp.removeAssociations(assoc, iname);
	}
    }

    public CIMObjectPath matchAssociator(String iname,
					String role,
					String resultRole) {
	boolean roleMatch = false;
	boolean resultRoleMatch = false;

	if (resultRole == null|| resultRole.length() == 0) {
	    resultRoleMatch = true;
	}

	for (int i = 0; i < roles.length; i++) {
	    if (!roleMatch && roles[i].ci.equals(iname)) {
		if (role == null || role.length() == 0) {
		    roleMatch = true;
		}
		if (!roleMatch &&
		    roles[i].role.equalsIgnoreCase(role)) {
		    roleMatch = true;
		}
	    }
	    if (!resultRoleMatch &&
		roles[i].role.equalsIgnoreCase(resultRole)) {
		resultRoleMatch = true;
	    }
	}
	if (roleMatch && resultRoleMatch) {
	    CIMObjectPath tempOp = new CIMObjectPath(
		sp.getObjectName(), sp.getNameSpace());
	    tempOp.setKeys(sp.getKeys());
	    return tempOp;
	} else {
	    return null;
	}
    }

    public ArrayList matchRole(String iname,
				String role,
				String resultRole) {
	ArrayList v = new ArrayList();
	ArrayList arr = new ArrayList();
	boolean isReflective = false;
	boolean match = false;
	boolean rolematch = false;

	// Is it a reflexive association
	for (int k = 0; k < roles.length; k++) {
	    if (roles[k].ci.equals(iname)) {

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

	match = false;
	for (int i = 0; i < roles.length; i++) {
	    if (!isReflective &&
		roles[i].ci.equals(iname)) {
		continue;
	    }

	    // match resultRole
	    if (resultRole != null && resultRole.length() != 0 &&
		!roles[i].role.equalsIgnoreCase(resultRole)) {
		continue;
	    }

	    // filter the duplicate
	    boolean exist = false;
	    for (int k = 0; k < arr.size(); k++) {
		if (roles[i].ci.equals(arr.get(k))) {
		    exist = true;
		}
	    }

	    if (!exist) {
		arr.add(roles[i].ci);
		v.add(roles[i].op);
	    }
	}
	return v;
    }
}
