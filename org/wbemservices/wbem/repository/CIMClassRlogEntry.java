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

package org.wbemservices.wbem.repository;

import javax.wbem.cim.*;

import java.util.*;

class CIMClassRlogEntry extends CIMRlogEntry {
    private static final long serialVersionUID = -1108143086668843953L;
    protected CIMNameSpaceRlogEntry nsentry;
    private String supercname;
    private HashMap subclasses = new HashMap();
    private HashMap instances = new HashMap();
    private CIMAssocClassRlogEntry[] assocications = 
		new CIMAssocClassRlogEntry[0];
 
    public CIMClassRlogEntry(CIMNameSpaceRlogEntry nsentry, 
			String name, 
			String supercc) {
	super(name);
	this.nsentry = nsentry;
	this.supercname = supercc;
    }
    /*
    public CIMClassRlogEntry(CIMNameSpaceRlogEntry nsentry, 
			CIMClass cc)
	throws CIMException {
	super( cc.getName(), CIMOMUtils.serialize(cc));
	this.nsentry = nsentry;
	this.supercname = cc.getSuperClass();
    }
    */
    public String getSuperClassName() {
	return supercname;
    }

    public Collection getSubClassCollection() {
	return subclasses.values();
    }

    public Set getSubClassKeys() {
	return subclasses.keySet();
    }

    public Collection getInstanceCollection() {
	return instances.values();
    }

    public CIMAssocClassRlogEntry[] getAssocications() {
	return assocications;
    }
 
    public void addSubClass(CIMClassRlogEntry cs) throws CIMException {
	subclasses.put(cs.getNameKey(), cs);
	
    }
    public CIMClassRlogEntry removeSubClass(String name) {
	return (CIMClassRlogEntry)
	    subclasses.remove(CIMRlogEntry.toNameKey(name));
    }

    public void addInstance(CIMInstanceRlogEntry ci) {
	instances.put(ci.getNameKey(), ci);
    }
    public CIMInstanceRlogEntry removeInstance(String name) {
	CIMInstanceRlogEntry cientry = 
	    (CIMInstanceRlogEntry)
	    instances.remove(CIMInstanceRlogEntry.toNameKey(name));
	cientry.delete();
	return cientry;
    }

    public CIMInstanceRlogEntry getInstance(String name) {
	return (CIMInstanceRlogEntry)
		   instances.get(CIMInstanceRlogEntry.toNameKey(name));
    }

    protected void addAssociation(CIMAssocClassRlogEntry assoc) {
	CIMAssocClassRlogEntry[] temp = 
	    new CIMAssocClassRlogEntry[assocications.length+1];
	System.arraycopy(assocications, 0, temp, 0, assocications.length);
	temp[assocications.length] = assoc;
	assocications = temp;
    }
       
    protected void removeAssociation(String name) {
	int count = 0;
	for (int k = 0; k < assocications.length; k++) {
	    if (assocications[k].getName().equalsIgnoreCase(name)) {
		count++;
	    }
	}

	CIMAssocClassRlogEntry[] temp = 
	    new CIMAssocClassRlogEntry[assocications.length - count];
	for (int i = 0, j = 0; i < assocications.length; i++) {
	    if (assocications[i].getName().equalsIgnoreCase(name)) {
		continue;
	    }
	    temp[j++] = assocications[i];
	}
	assocications = temp;
    }
       
    public boolean hasClassAssociation() {
	return (assocications.length > 0);
    }
}
