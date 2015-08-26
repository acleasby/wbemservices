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
 *are Copyright © 2003 Sun Microsystems, Inc.
 *
 *All Rights Reserved.
 *
 *Contributor(s): _______________________________________
*/

package org.wbemservices.wbem.repository;

import java.io.*;
import java.util.*;

class PersistentStore {
    private String fileName;
    private long fileLength;
    private Object[] maps = new Object[4];
    private TreeMap deletedObjectsBySize = new TreeMap();
    private HashMap deletedObjectsByOffset = new HashMap();
    private HashMap deletedObjectsByEnd= new HashMap();
    private HashMap objectIdMap = new HashMap();


    static class PersistObject implements Comparable, Serializable {
	private static final long serialVersionUID =-8265199713085209820L;  
	static long currentID = 0;
	private long id;
	private long offset;
	private int length;
	private boolean bReadyToUse = true;
	
	public PersistObject(long offset, int length) {
	    this.id = currentID++;
	    this.offset = offset;
	    this.length = length;
	}
	
	public int compareTo(Object o) {
	    PersistObject po = (PersistObject)o;
	    if(this == po)
		return 0;
	    if(bReadyToUse && !po.bReadyToUse)
		return 1;
	    if(!bReadyToUse && po.bReadyToUse)
		return -1;
	    if(length == po.length) 
		return id > po.id ? 1:-1;
	    return length > po.length ? 1 : -1;
	}
	
	public long getID() {
	    return id;
	}
	
    }

    public PersistentStore(String fileName)throws IOException {
	this.fileName = fileName;
	this.fileLength = 0;
	maps[0] = deletedObjectsBySize;
	maps[1] = deletedObjectsByOffset;
	maps[2] = deletedObjectsByEnd;
	maps[3] = objectIdMap;
	RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
	fileLength = raf.length();
	raf.close();

    }
    
    
    public byte[] readObjectBytes(long objId) throws IOException {
	PersistObject po = getObjectFromId(objId);
	byte[] bytes = new byte[po.length];
	read(bytes, po.offset);
	return bytes;
    }
    
    public long writeObjectBytes(byte[] bytes)
	throws IOException {
	    int length = bytes.length;
	    PersistObject po = createPersistObject(length);
	    write(po.offset, bytes);
	    putObjectToIdMap(po);
	    return po.id;
    }
    
    public void deleteObject(long id) {
	PersistObject po = getObjectFromId(id);
	if(po == null ) {
	    System.out.println("invaid object id: " + id);
	    return;
	}
	removeObjectFromIdMap(id);
	addDeletedObject(po, false);

   }
    
    private void joinDeleteObject() {
	HashMap tmp = (HashMap)deletedObjectsByOffset.clone();
	Iterator iter = tmp.values().iterator();
	while(iter.hasNext()) {
	    PersistObject po = (PersistObject)iter.next();
	    if(deletedObjectsByOffset.get(new Long (po.offset)) == null)
		continue; // po already has been joined
	    removeDeleteObject(po);
	    // check to see if we can join 
	    PersistObject poJoin = (PersistObject)
		deletedObjectsByOffset.get(new Long (po.offset+ po.length));
	    if(poJoin != null) {
		//join down
		removeDeleteObject(poJoin);
		po.length += poJoin.length;
	    }
	    addDeletedObject(po, false);
	    poJoin = (PersistObject)
		deletedObjectsByEnd.get(new Long(po.offset));
	    if(poJoin != null) {
		//join up
		removeDeleteObject(po);
		removeDeleteObject(poJoin);
		poJoin.length += po.length;
		addDeletedObject(poJoin, false);
	    }
	}
    }

    public void addToSnapshot(ObjectOutputStream stream) 
    throws IOException {
	joinDeleteObject();
	// mark all deleted object ready for re-use
	Iterator iter = deletedObjectsByOffset.values().iterator();
	while(iter.hasNext()) {
	    PersistObject po = (PersistObject)iter.next();
	    deletedObjectsBySize.remove(po);
	    po.bReadyToUse = true;
	    deletedObjectsBySize.put(po, po);
	}
	// check to see if we can shrink the file
	PersistObject poTemp = (PersistObject)
	    deletedObjectsByEnd.get(new Long(fileLength));
	if(poTemp != null && poTemp.bReadyToUse) {
	    RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
	    raf.setLength(poTemp.offset);
	    fileLength = poTemp.offset;
	    raf.close();
	    removeDeleteObject(poTemp);
	}
 	stream.writeLong(PersistObject.currentID);
	stream.writeObject(maps);
   }
    
    public void recoverFromSnapshot(ObjectInputStream stream)
	throws IOException, ClassNotFoundException {
	    PersistObject.currentID = stream.readLong();
	    maps = (Object[])stream.readObject();
	    deletedObjectsBySize = (TreeMap)maps[0];
	    deletedObjectsByOffset = (HashMap)maps[1];
	    deletedObjectsByEnd = (HashMap)maps[2];
	    objectIdMap = (HashMap)maps[3];
    }
    
    public PersistObject getPersistObject(long id) {
	return getObjectFromId(id);

    }

    public void addPersistObject(PersistObject po) {
	putObjectToIdMap(po);
	createPersistObject(po.length);
    }

    private PersistObject createPersistObject(int length) {
	PersistObject po = new PersistObject(fileLength, length);
	if(deletedObjectsBySize.isEmpty())
	    return po;
	PersistObject poDeleted =
	    (PersistObject)deletedObjectsBySize.lastKey();
	if(!poDeleted.bReadyToUse || poDeleted.length < po.length )
	    return po;
	removeDeleteObject(poDeleted);
	po.offset = poDeleted.offset;
	poDeleted.offset = po.offset + length;
	poDeleted.length -= po.length;
	addDeletedObject(poDeleted, true);
	return po;
    }

	// BUGFIX 775882. Before the repository grew forever.
	// bReadyToUse flag was added as input argument so that
	// deleted space can be reclaimed.
    private void addDeletedObject(PersistObject po, boolean ready) {
	if(po.length == 0 )
	    return;
	po.bReadyToUse = ready;
	Long endObj = new Long(po.offset + po.length);
	deletedObjectsBySize.put(po,po);
	deletedObjectsByOffset.put(new Long(po.offset), po );
	deletedObjectsByEnd.put(endObj, po);
    }

    private void removeDeleteObject(PersistObject po) {
	deletedObjectsBySize.remove(po);
	deletedObjectsByOffset.remove(new Long(po.offset));
	deletedObjectsByEnd.remove(new Long(po.offset + po.length));
    }

    private PersistObject getObjectFromId(long id) {
	return (PersistObject) objectIdMap.get(new Long(id));
    }

    private void putObjectToIdMap(PersistObject po) {
	objectIdMap.put(new Long(po.id),po);
    }

    private void removeObjectFromIdMap(long id) {
	objectIdMap.remove(new Long(id));
    }

    private void write (long pos, byte[] bytes) throws IOException {
	RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
	/*****
	if(pos != fileLength) {
	    System.out.println("write offset:" + pos);
	}
	******/
	raf.seek(pos);
	raf.write(bytes);
	fileLength = raf.length();
	raf.close();
    }
	
    private void read(byte[] rb, long pos) throws IOException {
	RandomAccessFile raf = new RandomAccessFile(fileName, "r");
	raf.seek(pos);
	raf.read(rb);
	raf.close();
    }



}
