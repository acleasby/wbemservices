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

import java.util.*;

class CIMInstanceRlogEntry extends CIMRlogEntry {

    private static final long serialVersionUID = 5105951032964717298L;

    static protected String toNameKey(String s) {
	return s;
    }

    protected CIMNameSpaceRlogEntry nsentry;
    private HashMap assocications = new HashMap();
    
    public CIMInstanceRlogEntry(
		CIMNameSpaceRlogEntry nsentry,
		String name) {
	super(name);
	this.nsentry = nsentry;
    }
    /*
    public CIMInstanceRlogEntry(
		CIMNameSpaceRlogEntry nsentry,
		CIMInstance ci)
	throws CIMException {
	super(ci.getName(),CIMOMUtils.serialize(ci));
	this.nsentry = nsentry;
    }
    */
    public String getNameKey() {
	return name;
    }

    public CIMAssocInstanceRlogEntry[] getAssociations(String name) {
	return (CIMAssocInstanceRlogEntry[])assocications.get(
				name.toLowerCase());
    }

    protected void addAssociations(String name, 
			CIMAssocInstanceRlogEntry ci) {
	String assoc = name.toLowerCase();
	CIMAssocInstanceRlogEntry[] temp;
	CIMAssocInstanceRlogEntry[] list = 
	    (CIMAssocInstanceRlogEntry[])assocications.get(assoc);
	if (list == null || list.length == 0) {
	    temp = new CIMAssocInstanceRlogEntry[] {ci};
	} else {
	    temp = new CIMAssocInstanceRlogEntry[list.length+1];
	    System.arraycopy(list, 0, temp, 0, list.length);
	    temp[list.length] = ci;
	    assocications.remove(assoc);
	}
	assocications.put(assoc, temp);
    }

    protected void removeAssociations(String cname, String iname) {
	String assoc = cname.toLowerCase();
	CIMAssocInstanceRlogEntry[] list = 
	    (CIMAssocInstanceRlogEntry[])assocications.get(assoc);

	if (list == null) 
	    return;

	int count = 0;
	for (int k = 0; k < list.length; k++) {
	    if (list[k].getName().equals(iname)) {
		count++;
	    } 
	}

	CIMAssocInstanceRlogEntry[] temp = 
	    new CIMAssocInstanceRlogEntry[list.length - count];
	for (int i = 0, j = 0; i < list.length; i++) {
	    if (list[i].getName().equals(iname)) {
		continue;
	    }
	    temp[j++] = list[i];
	}
	assocications.remove(assoc);
	if (temp.length > 0) {
	    assocications.put(assoc, temp);
	}
    }

    public boolean hasAssociation() {
	return !assocications.isEmpty();
    }
}
