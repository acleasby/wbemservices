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

import java.io.IOException;

class CIMRlogEntry implements java.io.Serializable {
    private static final long serialVersionUID =4012529228189L;
    protected String name;
    protected static  String toNameKey(String s) {
	return s.toLowerCase();

    }
    protected static PersistentStore store;
    //protected transient SoftReference ref = null; 

    private long objectID = -1;
    static public void setPersistentStore( PersistentStore store ) {
	CIMRlogEntry.store = store;

    }

    public CIMRlogEntry() {

    }

    public CIMRlogEntry(String name) {
	this.name = name;
    }

    public String getName() {
	return name;
    }

    public String getNameKey() {
	return toNameKey(name);
    }

    public boolean equals(Object anObject) {
	if (this == anObject)
	    return true;

	if (anObject != null && (anObject instanceof CIMRlogEntry)) {
	    CIMRlogEntry e = (CIMRlogEntry)anObject;
	    return e.name.equals(name);
	}

	return false;
    }

    public int hashCode() {
	return name.hashCode();
    }

    public byte[] getValue() {
	if(objectID == -1)
	    return null;
	try {
	    byte[] bytes = null;
	    /*
	    if(ref != null && (bytes = (byte[])ref.get()) != null) {	    
		return bytes;
	    }
	    */
	    bytes = store.readObjectBytes(objectID);
	    return bytes;
	} catch (IOException e) {
	    System.out.println("exception:" + e);
	    e.printStackTrace();
	    return null;
	}
    }

    private void deletePersistObject() {
	if(objectID != -1) 
	    store.deleteObject(objectID);
	objectID = -1;
    }

    public void createPersistObject(byte[] bytes) {
	try {
	    // in order to make the reliable log recoveing to work
	    // we must update the store with the same order as defined
	    // in AddCIMQualifierLogObj.apply.
	    // so we have to write the store first then delete the 
	    // previous persiste object.
	    if(bytes == null || bytes.length == 0) {
		delete();
		return;
	    }

	    long id = store.writeObjectBytes(bytes);
	    setID(id);
	    //ref = new SoftReference(bytes);

	} catch(IOException e) {
	    System.out.println("exception:" + e);
	    e.printStackTrace();

	}

    }

    public void setID(long id) {
	delete();
	objectID = id;
    }

    public long getID() {
	return objectID;
    }

    public void delete() {
	if(objectID != -1) 
	    store.deleteObject(objectID);
	objectID = -1;
    }

    public PersistentStore.PersistObject getPersistObject() {
	if(objectID == -1) {
	    throw new java.lang.RuntimeException
		("CIMRlogEntry contains no object");
	}
	return store.getPersistObject(objectID);
    }


}
