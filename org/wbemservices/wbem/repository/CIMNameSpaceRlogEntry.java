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

class CIMNameSpaceRlogEntry extends CIMRlogEntry {

    private static final long serialVersionUID = 8598716484732411839L;
    private HashMap subnspaces = new HashMap();
    private HashMap qualifiers =  new HashMap();
    private HashMap classes = new HashMap();

    public CIMNameSpaceRlogEntry( String namespace) {
	super(namespace);
    }

    public Collection getSubNameSpaceCollection() {
	return subnspaces.values();
    }

    public Collection getQualifierCollection() {
	return qualifiers.values();
    }

    public Collection getClassCollection() {
	return classes.values();
    }

    public void addSubNameSpace(CIMNameSpaceRlogEntry nsentry) {
	subnspaces.put(nsentry.getNameKey(), nsentry);
    }
    public CIMNameSpaceRlogEntry removeSubNameSpace(String name) {
	return (CIMNameSpaceRlogEntry)
	    subnspaces.remove(CIMRlogEntry.toNameKey(name));
    }
    public CIMNameSpaceRlogEntry getSubNameSpace(String name) {
	return (CIMNameSpaceRlogEntry)
	    subnspaces.get(CIMRlogEntry.toNameKey(name));
    }


    public void addQualifierType(CIMQualifierTypeRlogEntry qtentry) {
	qualifiers.put(qtentry.getNameKey(), qtentry);
    }
    public CIMQualifierTypeRlogEntry removeQualifierType(String name) {
	CIMQualifierTypeRlogEntry qtentry = (CIMQualifierTypeRlogEntry)
	    qualifiers.remove(CIMRlogEntry.toNameKey(name));
	qtentry.delete();
	return qtentry;
    }
    public CIMQualifierTypeRlogEntry getQualifierType(String name) {
	return (CIMQualifierTypeRlogEntry)
	    qualifiers.get(CIMRlogEntry.toNameKey(name));
    }

    public void addClass(CIMClassRlogEntry cc) {
	classes.put(cc.getNameKey(), cc);
    }
    public CIMClassRlogEntry removeClass(String name) {
	CIMClassRlogEntry ccentry =  (CIMClassRlogEntry)
	    classes.remove(CIMRlogEntry.toNameKey(name));
	ccentry.delete();
	return ccentry;
    }

    public CIMClassRlogEntry getClass(String name) {
	return (CIMClassRlogEntry)classes.get(CIMRlogEntry.toNameKey(name));
    }

    public void delete() {
	deleteEntries(classes);
	deleteEntries(qualifiers);

    }


    private void deleteEntries(HashMap map) {
	Collection c = map.values();
	Iterator iter = c.iterator();
	while(iter.hasNext()) {
	    CIMRlogEntry entry = (CIMRlogEntry)iter.next();
	    if(entry instanceof CIMClassRlogEntry) {
		if(entry.getName().equals(PSRlogImpl.TOP))
		   continue;
	    }
	    entry.delete();
	}
    }
}
