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
 *The Initial Developer of the Original Code is: *Sun Microsystems, Inc.
 *
 *Portions created by: Sun Microsystems, Inc.
 *are Copyright � 2001 Sun Microsystems, Inc.
 *
 *All Rights Reserved.
 *
 *Contributor(s):  WBEM Solutions, Inc.
*/

package org.wbemservices.wbem.repository;

import org.wbemservices.wbem.cimom.ReadersWriter;
import org.wbemservices.wbem.cimom.repository.RepositoryIF;

import javax.wbem.cim.*;
import javax.wbem.client.CIMOMHandle;
import javax.wbem.provider.EventProvider;
import javax.wbem.query.QueryExp;
import javax.wbem.query.SelectExp;
import javax.wbem.query.SelectList;
import javax.wbem.query.WQLParser;
import java.io.*;
import java.util.*;

/*
 * CIMWBEM persistent store APIs
 */
public class PSRlogImpl implements RepositoryIF, EventProvider {

    private static final int CIMQYALIFY = 0;
    private static final int CIMCLASS = 1;
    private static final int CIMINSTANCE = 2;

    // EventHelper object contains all the event generation logic.
    private EventHelper eventHelper;

    private static interface LogRecord extends Serializable {
        void apply(PSRlogImpl psImpl);
    }

    private static class CreateNameSpaceLogObj implements LogRecord {
        private static final long serialVersionUID = -3217549711344318384L;
        private String namespace;

        public CreateNameSpaceLogObj(String namespace) {
            this.namespace = namespace;
        }

        public void apply(PSRlogImpl psImpl) {
            try {
                psImpl.createNameSpaceDo(namespace);
            } catch (CIMException e) {
        /* this exception should never occur when recovering  */
            }
        }
    }

    private static class DeleteNameSpaceLogObj implements LogRecord {
        private static final long serialVersionUID = 6349423596857633706L;
        private String namespace;

        public DeleteNameSpaceLogObj(String namespace) {
            this.namespace = namespace;
        }

        public void apply(PSRlogImpl psImpl) {
            try {
                psImpl.deleteNameSpaceDo(namespace);
            } catch (CIMException e) {
		/* this exception should never occur when recovering  */
            }
        }
    }

    private static class AddCIMQualifierLogObj implements LogRecord {
        private static final long serialVersionUID = 9136358929100936537L;
        protected String namespace;
        protected String name;
        protected PersistentStore.PersistObject po;
        protected transient CIMRlogEntry entry = null;

        public AddCIMQualifierLogObj(String namespace, String name,
                PersistentStore.PersistObject po) {
            this.namespace = namespace;
            this.name = name;
            this.po = po;
        }

        public void apply(PSRlogImpl psImpl) {
            try {
                psImpl.store.addPersistObject(po);
                applyDo(psImpl);
                entry.setID(po.getID());
            } catch (CIMException e) {
		/* this exception should never occur when recovering  */
            }
        }

        protected void applyDo(PSRlogImpl psImpl)
                throws CIMException {
            entry = psImpl.addCIMQualifierTypeDo(namespace, name);
        }

        protected CIMElement getCIMElement(PSRlogImpl psImpl)
                throws CIMException {
            try {
                byte[] bytes = psImpl.store.readObjectBytes(po.getID());
                return (CIMElement) PSRlogImpl.deserialize(bytes);

            } catch (IOException e) {
                System.out.println("exception:" + e);
                e.printStackTrace();
                throw new java.lang.RuntimeException
                        ("object id = " + po.getID() + " does not exist");
            }
        }

    }

    private static class SetCIMQualifierLogObj extends AddCIMQualifierLogObj {
        private static final long serialVersionUID = 6509220164924254555L;

        public SetCIMQualifierLogObj(String namespace, String name,
                PersistentStore.PersistObject po) {
            super(namespace, name, po);
        }

        protected void applyDo(PSRlogImpl psImpl)
                throws CIMException {
            entry = psImpl.setQualifierTypeDo(namespace, name);
        }
    }

    private static class SetCIMClassLogObj extends AddCIMQualifierLogObj {
        private static final long serialVersionUID = 4842199887990665842L;
        protected String otherName;

        public SetCIMClassLogObj(String namespace, String name,
                String otherName,
                PersistentStore.PersistObject po) {
            super(namespace, name, po);
            this.otherName = otherName;
        }

        protected void applyDo(PSRlogImpl psImpl)
                throws CIMException {
            entry = psImpl.setClassDo(namespace, name, otherName);
        }
    }

    private static class SetCIMInstanceLogObj extends SetCIMClassLogObj {
        private static final long serialVersionUID = 8152734320755230951L;

        public SetCIMInstanceLogObj(String namespace, String name,
                String otherName,
                PersistentStore.PersistObject po) {
            super(namespace, name, otherName, po);

        }

        protected void applyDo(PSRlogImpl psImpl)
                throws CIMException {
            entry = psImpl.setInstanceDo(namespace, name, otherName);
        }
    }

    private static class AddCIMClassLogObj extends AddCIMQualifierLogObj {
        private static final long serialVersionUID = -7848952153471853442L;
        protected String otherName;
        protected boolean assoc;

        public AddCIMClassLogObj(String namespace, String name,
                String otherName, boolean assoc,
                PersistentStore.PersistObject po) {

            super(namespace, name, po);
            this.assoc = assoc;
            this.otherName = otherName;
        }

        protected void applyDo(PSRlogImpl psImpl)
                throws CIMException {

            CIMClass cc = assoc ? (CIMClass) getCIMElement(psImpl) : null;
            entry = psImpl.addCIMClassDo(namespace, name,
                    otherName, assoc, cc);
        }
    }

    private static class AddCIMInstanceLogObj extends AddCIMClassLogObj {
        private static final long serialVersionUID = -3727326644253863113L;

        //protected String otherName;
        public AddCIMInstanceLogObj(String namespace, String name,
                String otherName, boolean assoc,
                PersistentStore.PersistObject po) {

            super(namespace, name, otherName, assoc, po);
        }

        protected void applyDo(PSRlogImpl psImpl)
                throws CIMException {
            CIMInstance ci = assoc ?
                    (CIMInstance) getCIMElement(psImpl) : null;
            entry = psImpl.addCIMInstanceDo(namespace, name, otherName, ci);
        }
    }

    private static class DeleteQualifierLogObj implements LogRecord {
        private static final long serialVersionUID = -4910178553526207227L;
        private String namespace;
        private String qtName;

        public DeleteQualifierLogObj(String namespace, String qtName) {
            this.namespace = namespace;
            this.qtName = qtName;
        }

        public void apply(PSRlogImpl psImpl) {
            try {
                psImpl.deleteQualifier(namespace, qtName);
            } catch (CIMException e) {
		/* this exception should never occur when recovering  */
            }
        }
    }

    private static class DeleteClassLogObj implements LogRecord {
        private static final long serialVersionUID = -6444092897893756582L;
        private String namespace;
        private String className;

        public DeleteClassLogObj(String namespace, String className) {
            this.namespace = namespace;
            this.className = className;
        }

        public void apply(PSRlogImpl psImpl) {
            try {
                psImpl.deleteClass(namespace, className);
            } catch (CIMException e) {
		/* ingore  */
            }
        }
    }

    private static class DeleteInstanceLogObj implements LogRecord {
        private static final long serialVersionUID = -244838496512419329L;
        private String namespace;
        private String iname;

        public DeleteInstanceLogObj(String namespace, String iname) {
            this.namespace = namespace;
            this.iname = iname;
        }

        public void apply(PSRlogImpl psImpl) {
            try {
                psImpl.deleteInstance(namespace, iname);
            } catch (CIMException e) {
		/* ingore  */
            }
        }
    }

    private class LocalLogHandler extends LogHandler {

        /**
         * Simple constructor
         */
        public LocalLogHandler() {
        }

        /* Overrides snapshot() defined in ReliableLog's LogHandler class. */
        public void snapshot(OutputStream out) throws IOException {
            takeSnapshot(out);
        }

        /* Overrides recover() defined in ReliableLog's LogHandler class. */
        public void recover(InputStream in)
                throws IOException, ClassNotFoundException {
            recoverSnapshot(in);
        }

        /**
         * Required method implementing the abstract applyUpdate()
         * defined in ReliableLog's associated LogHandler class.
         */
        public void applyUpdate(Object logRecObj) {
            ((LogRecord) logRecObj).apply(PSRlogImpl.this);
        }
    }

    private class SnapshotThread extends Thread {

        /**
         * Create a daemon thread
         */
        public SnapshotThread() {
            super("snapshot thread");
            setDaemon(true);
        }

        public void run() {
            try {
                concurrentObj.readLock();
            } catch (ReadersWriter.ConcurrentLockException e) {
                return;
            }
            try {
                while (!isInterrupted()) {
                    try {
                        concurrentObj.readerWait(snapshotNotifier,
                                Long.MAX_VALUE);
                        try {
                            log.snapshot();
                            logFileSize = 0;
                        } catch (InterruptedIOException e) {
                            return;
                        } catch (Exception e) {
                            if (e instanceof LogException &&
                                    ((LogException) e).detail instanceof
                                            InterruptedIOException) {
                                return;
                            }
                            e.printStackTrace();
                        }
                    } catch (ReadersWriter.ConcurrentLockException e) {
                        return;
                    }
                }
            } finally {
                concurrentObj.readUnlock();
            }
        }
    }

    public static final String CIMROOT = "/";
    public static final String TOP = "top";
    public static final String CIMBASE = "root/cimv2";
    /**
     * Log format version
     */
    private static final int LOG_VERSION = 1;

    public static boolean verbose = false;

    private static HashMap nameSpacesMap = null;
    private ReliableLog log;
    private boolean inRecovery;
    private ReadersWriter concurrentObj;
    private final Object snapshotNotifier = new Object();
    /**
     * Log File must contain this many records before snapshot allowed
     */
    private int logToSnapshotThresh = 200;
    private int logFileSize = 0;
    private final Thread snapshotter = new SnapshotThread();
    private PersistentStore store = null;
    private String logpath = null;

    /*
     * Constructor 
     */
    public PSRlogImpl(String hostname, ReadersWriter concurrentObj)
            throws CIMException {
        try {
            this.concurrentObj = concurrentObj;
            eventHelper = new EventHelper(concurrentObj);
            init();
        } catch (Exception e) {
            if (verbose) {
                e.printStackTrace();
            }
            throw new CIMException(CIMException.CIM_ERR_FAILED, e.toString());
        }
    }

    /*
     * Constructor 
     */
    public PSRlogImpl(String hostname, ReadersWriter concurrentObj,
            String logpath) throws CIMException {
        try {
            this.concurrentObj = concurrentObj;
            this.logpath = logpath;
            init();
        } catch (Exception e) {
            if (verbose) {
                e.printStackTrace();
            }
            throw new CIMException(CIMException.CIM_ERR_FAILED, e.toString());
        }
    }

    /*
     * Create the specified namespace.
     *
     * @parm namespace    name of  namespace that needs to be created.
     */
    public void createNameSpace(String namespace) throws CIMException {
        try {
            concurrentObj.writeLock();
            createNameSpaceDo(namespace);
        } finally {
            concurrentObj.writeUnlock();
        }
    }

    private void createNameSpaceDo(String namespace) throws CIMException {
        String ns = namespace.replace('\\', '/');
        try {
            if ((ns == null) || (ns.length() == 0) || ns.equals(CIMROOT)) {
                throw new CIMNameSpaceException(
                        CIMException.CIM_ERR_ALREADY_EXISTS, namespace);
            }

            //	    String parent = ns.substring(0, ns.lastIndexOf("/"));
            if (namespace.length() == 0) {
                namespace = CIMROOT;
            }

            CIMNameSpaceRlogEntry nsentry =
                    (CIMNameSpaceRlogEntry) nameSpacesMap.get(ns);

            if (nsentry != null) {
                throw new CIMNameSpaceException(
                        CIMException.CIM_ERR_ALREADY_EXISTS, namespace);
            }
            nsentry = new CIMNameSpaceRlogEntry(ns);
            nsentry.addClass(
                    new CIMClassRlogEntry(nsentry, TOP, ""));
            putNameSpaceEntryToMap(nsentry);
            addLogRecord(new CreateNameSpaceLogObj(namespace));

        } catch (CIMException e) {
            if (verbose) {
                e.printStackTrace();
            }
            throw e;
        } catch (Exception e) {
            if (verbose) {
                e.printStackTrace();
            }
            throw new CIMException(CIMException.CIM_ERR_FAILED, e.toString());
        }
    }

    /*
    * Add this qualifier to the specified namespace
    *
    * @param namespace    name of namespace that contains this qualifier
    * @param qt    the qualifier to be added
    */
    public void addCIMElement(String namespace, CIMQualifierType qt)
            throws CIMException {
        try {
            concurrentObj.writeLock();
            addCIMElementDO(namespace, qt);
        } finally {
            concurrentObj.writeUnlock();
        }
    }

    private void addCIMElementDO(String namespace, CIMQualifierType qt)
            throws CIMException {
        String qtName = qt.getName();
        CIMRlogEntry qtentry =
                addCIMQualifierTypeDo(namespace, qtName);
        createAddElementLogRecord(namespace, qtentry, qt);
    }

    private CIMRlogEntry addCIMQualifierTypeDo(String namespace, String qtName)
            throws CIMException {
        try {
            CIMNameSpaceRlogEntry nsentry =
                    getNameSpaceEntryFromMap(namespace);
            CIMQualifierTypeRlogEntry qtentry =
                    nsentry.getQualifierType(qtName);

            if (qtentry != null) {
                throw new CIMQualifierTypeException(
                        CIMException.CIM_ERR_ALREADY_EXISTS, qtName);
            }
            qtentry = new CIMQualifierTypeRlogEntry(namespace, qtName);
            nsentry.addQualifierType(qtentry);
            return qtentry;
        } catch (CIMException e) {
            if (verbose) {
                e.printStackTrace();
            }
            throw e;
        } catch (Exception e) {
            if (verbose) {
                e.printStackTrace();
            }
            throw new CIMException(CIMException.CIM_ERR_FAILED, e.toString());
        }

    }

    private void createAddElementLogRecord(String namespace, CIMRlogEntry entry,
            CIMElement element)
            throws CIMException {
        createLogRecord(namespace, entry, element, true);
    }

    private void createSetElementLogRecord(String namespace, CIMRlogEntry entry,
            CIMElement element)
            throws CIMException {
        createLogRecord(namespace, entry, element, false);
    }

    private void createLogRecord(String namespace, CIMRlogEntry entry,
            CIMElement element, boolean add)
            throws CIMException {
        String otherName = null;
        // <PJA> 16-Jan-2003
        // BUGFIX 655351 getInstance fails for associators
        // use new key for instances
        String name = InstanceNameUtils.getNameForElement(element);
        int type = CIMQYALIFY;
        boolean assoc = false;
        LogRecord rec = null;
        entry.createPersistObject(serialize(element));

        PersistentStore.PersistObject po = entry.getPersistObject();

        if (element instanceof CIMQualifierType) {
            type = CIMQYALIFY;
            rec = add ? new AddCIMQualifierLogObj(namespace, name, po) :
                    new SetCIMQualifierLogObj(namespace, name, po);
        } else if (element instanceof CIMClass) {
            type = CIMCLASS;
            assoc = ((CIMClass) element).isAssociation();
            otherName = ((CIMClass) element).getSuperClass();
            if (add) {
                rec = new AddCIMClassLogObj(namespace, name, otherName,
                        assoc, po);
            } else {
                rec = new SetCIMClassLogObj(namespace, name, otherName, po);
            }
        } else if (element instanceof CIMInstance) {
            type = CIMINSTANCE;
            otherName = ((CIMInstance) element).getClassName();
            if (add) {
                assoc = entry instanceof CIMAssocInstanceRlogEntry;
                rec = new AddCIMInstanceLogObj(namespace, name,
                        otherName, assoc, po);
            } else {
                rec = new SetCIMInstanceLogObj(namespace, name, otherName, po);
            }

        }
        addLogRecord(rec);
    }

    /*
    * Add this class to the specified namespace
    *
    * @param namespace    name of namespace that contains this class
    * @param cc    the class to be added
    */
    public void addCIMElement(String namespace, CIMClass cc)
            throws CIMException {
        try {
            concurrentObj.writeLock();
            addCIMElementDO(namespace, cc);
        } finally {
            concurrentObj.writeUnlock();
        }
    }

    private void addCIMElementDO(String namespace, CIMClass cc)
            throws CIMException {
        try {
            String cname = cc.getName();
            String supercc = cc.getSuperClass();
            boolean assoc = cc.isAssociation();
            CIMRlogEntry ccentry =
                    addCIMClassDo(namespace, cname, supercc, assoc, cc);
            createAddElementLogRecord(namespace, ccentry, cc);

            // See if somebody interested in class creation for this namespace
            eventHelper.classCreation(namespace, cc);
        } catch (CIMException e) {
            throw e;
        }
    }

    private CIMRlogEntry addCIMClassDo(String namespace, String cname,
            String supercc, boolean assoc, CIMClass cc)
            throws CIMException {

        if (supercc.length() == 0) {
            supercc = TOP;
        }

        try {
            CIMNameSpaceRlogEntry nsentry =
                    getNameSpaceEntryFromMap(namespace);

            CIMClassRlogEntry ccentry = nsentry.getClass(cname);

            if (ccentry != null) {
                throw new CIMClassException(
                        CIMException.CIM_ERR_ALREADY_EXISTS, cname);
            }

            CIMClassRlogEntry supccentry = nsentry.getClass(supercc);
            if (supccentry == null) {
                throw new CIMClassException
                        (CIMException.CIM_ERR_NOT_FOUND, supercc);
            }
            if (assoc) {
                ccentry = new CIMAssocClassRlogEntry(nsentry,
                        cname, supercc);
                ((CIMAssocClassRlogEntry) ccentry).createAssciations(cc);
            } else {
                ccentry = new CIMClassRlogEntry(nsentry, cname, supercc);
            }

            // Update the parent class
            supccentry.addSubClass(ccentry);
            nsentry.addClass(ccentry);
            return ccentry;
        } catch (CIMException e) {
            if (verbose) {
                e.printStackTrace();
            }
            throw e;
        } catch (Exception e) {
            if (verbose) {
                e.printStackTrace();
            }
            throw new CIMException(CIMException.CIM_ERR_FAILED, e.toString());
        }
    }

    /*
    * Add this instance to the specified namespace
    *
    * @param namespace    name of namespace that contains this instance
    * @param ci    the instance to be added
    */
    public void addCIMElement(String namespace, CIMInstance ci)
            throws CIMException {
        try {
            concurrentObj.writeLock();
            addCIMElementDo(namespace, ci);
        } finally {
            concurrentObj.writeUnlock();
        }
    }

    private void addCIMElementDo(String namespace, CIMInstance ci)
            throws CIMException {
        String cname = ci.getClassName();
        // <PJA> 16-Jan-2003
        // BUGFIX 655351 getInstance fails for associators
        // use new key for instances
        String iname = InstanceNameUtils.getInstanceNameKey(ci);
        CIMRlogEntry entry =
                addCIMInstanceDo(namespace, iname, cname, ci);
        createAddElementLogRecord(namespace, entry, ci);

        // See if need to create an indication for this instance
        eventHelper.instanceAddition(namespace.toLowerCase(), ci);
    }

    private CIMRlogEntry addCIMInstanceDo(String namespace,
            String iname, String cname, CIMInstance ci)
            throws CIMException {

        try {
            CIMNameSpaceRlogEntry nsentry =
                    getNameSpaceEntryFromMap(namespace);

            CIMClassRlogEntry ccentry = nsentry.getClass(cname);

            if (ccentry == null) {
                throw new CIMClassException
                        (CIMException.CIM_ERR_NOT_FOUND, cname);
            }

            CIMInstanceRlogEntry cientry = ccentry.getInstance(iname);

            if (cientry != null) {
                throw new CIMInstanceException(
                        CIMException.CIM_ERR_ALREADY_EXISTS, iname);
            }

            if (ccentry instanceof CIMAssocClassRlogEntry) {
                cientry = new CIMAssocInstanceRlogEntry(nsentry, iname);
                // call validateInstanceAssociations to make sure
                // addInstanceAssociations can be successful
                CIMAssocInstanceRlogEntry
                        associentry = (CIMAssocInstanceRlogEntry) cientry;
                associentry.validateInstanceAssociations();
                associentry.createAssciations(ci);
                associentry.addInstanceAssociations(cname, associentry);
            } else {
                cientry = new CIMInstanceRlogEntry(nsentry, iname);
            }
            ccentry.addInstance(cientry);
            return cientry;
        } catch (CIMException e) {
            if (verbose) {
                e.printStackTrace();
            }
            throw e;
        } catch (Exception e) {
            if (verbose) {
                e.printStackTrace();
            }
            throw new CIMException(CIMException.CIM_ERR_FAILED, e.toString());
        }
    }

    /*
     * Replace the existing qualifier with the specified one
     *
     * @param namespace    name of namespace that contains this qualifier
     * @param qt    the new qualifier to be added
     */
    public void setQualifierType(String namespace, CIMQualifierType qt)
            throws CIMException {
        try {
            concurrentObj.writeLock();
            setQualifierTypeDo(namespace, qt);
        } finally {
            concurrentObj.writeUnlock();
        }
    }

    private void setQualifierTypeDo(String namespace, CIMQualifierType qt)
            throws CIMException {

        String qtName = qt.getName();
        CIMRlogEntry entry = setQualifierTypeDo(namespace, qtName);
        createSetElementLogRecord(namespace, entry, qt);
    }

    private CIMRlogEntry setQualifierTypeDo(String namespace,
            String qtName)
            throws CIMException {

        try {
            CIMNameSpaceRlogEntry nsentry =
                    getNameSpaceEntryFromMap(namespace);

            CIMQualifierTypeRlogEntry qtentry =
                    nsentry.getQualifierType(qtName);

            if (qtentry == null) {
                qtentry =
                        new CIMQualifierTypeRlogEntry(namespace, qtName);
                nsentry.addQualifierType(qtentry);
            }
            return qtentry;
        } catch (CIMException e) {
            if (verbose) {
                e.printStackTrace();
            }
            throw e;
        } catch (Exception e) {
            if (verbose) {
                e.printStackTrace();
            }
            throw new CIMException(CIMException.CIM_ERR_FAILED, e.toString());
        }
    }

    /*
     * Replace the existing class with the specified one
     *
     * @param namespace    name of namespace that contains this class
     * @param cc    the new class to be added
     */
    public void setClass(String namespace, CIMClass cc) throws CIMException {
        try {
            concurrentObj.writeLock();
            setClassDo(namespace, cc);
        } finally {
            concurrentObj.writeUnlock();
        }
    }

    private void setClassDo(String namespace, CIMClass cc)
            throws CIMException {

        CIMObjectPath classop = new CIMObjectPath(cc.getName(), namespace);
        CIMClass oldcc = getClassDo(classop.getNameSpace(),
                classop.getObjectName());
        String name = cc.getName();
        String supercc = cc.getSuperClass();
        CIMRlogEntry entry = setClassDo(namespace, name, supercc);
        createSetElementLogRecord(namespace, entry, cc);

        // See if somebody is interested in the class mod. for this namespace
        eventHelper.classModification(namespace, oldcc, cc);
    }

    private CIMRlogEntry setClassDo(String namespace,
            String cname, String supercc)
            throws CIMException {

        if (supercc.length() == 0) {
            supercc = TOP;
        }

        try {
            CIMNameSpaceRlogEntry nsentry =
                    getNameSpaceEntryFromMap(namespace);

            CIMClassRlogEntry ccentry =
                    nsentry.getClass(cname);
            if (ccentry == null) {
                ccentry = new CIMClassRlogEntry(nsentry, cname, supercc);
                nsentry.addClass(ccentry);
            }
            return ccentry;
        } catch (CIMException e) {
            e.printStackTrace();
            if (verbose) {
                e.printStackTrace();
            }
            throw e;
        } catch (Exception e) {
            if (verbose || true) {
                e.printStackTrace();
            }
            throw new CIMException(CIMException.CIM_ERR_FAILED, e.toString());
        }
    }

    /*
     * Replace the existing instance with the specified one
     *
     * @param namespace    name of namespace that contains this instance
     * @param qt    the new instance to be added
     */
    public void setInstance(String namespace, CIMInstance ci)
            throws CIMException {
        try {
            concurrentObj.writeLock();
            setInstanceDo(namespace, ci);
        } finally {
            concurrentObj.writeUnlock();
        }
    }

    private void setInstanceDo(String namespace, CIMInstance ci)
            throws CIMException {

        String cname = ci.getClassName();
        // <PJA> 16-Jan-2003
        // BUGFIX 655351 getInstance fails for associators
        // use new key for instances
        String iname = InstanceNameUtils.getInstanceNameKey(ci);
        CIMRlogEntry entry = setInstanceDo(namespace, iname, cname);

        // Get the old instance before it gets updated
        CIMInstance oldci = (CIMInstance) deserialize(entry.getValue());

        createSetElementLogRecord(namespace, entry, ci);

        // See if need to create an indication for this instance
        eventHelper.instanceModification(namespace.toLowerCase(), oldci, ci);

    }

    private CIMRlogEntry setInstanceDo(String namespace, String iname,
            String cname)
            throws CIMException {

        try {
            CIMNameSpaceRlogEntry nsentry =
                    getNameSpaceEntryFromMap(namespace);

            CIMClassRlogEntry ccentry =
                    nsentry.getClass(cname);
            if (ccentry == null) {
                throw new CIMClassException
                        (CIMException.CIM_ERR_NOT_FOUND, cname);
            }

            CIMInstanceRlogEntry cientry =
                    ccentry.getInstance(iname);

            if (cientry == null) {
                cientry = new CIMInstanceRlogEntry(nsentry, iname);
                ccentry.addInstance(cientry);
            }
            return cientry;
        } catch (CIMException e) {
            if (verbose) {
                e.printStackTrace();
            }
            throw e;
        } catch (Exception e) {
            if (verbose) {
                e.printStackTrace();
            }
            throw new CIMException(CIMException.CIM_ERR_FAILED, e.toString());
        }
    }

    /*
     * Retrieve the qualifier that specified by the CIMObjectPath
     *
     * @param op    the CIMObjectPath of this qualifier
     * @return CIMQualifierType    the retrived CIMQualifierType object
     */
    public CIMQualifierType getQualifierType(CIMObjectPath op)
            throws CIMException {
        return getQualifierType(op.getNameSpace(), op.getObjectName());
    }

    /*
     * Retrieve the qualifier that specified by the qualifier name
     *
     * @param namespace    name of namespace that contains this qualifier
     * @param qtName    the name of this qualifier
     * @return CIMQualifierType    the retrived CIMQualifierType object
     */
    public CIMQualifierType getQualifierType(String namespace, String qtName)
            throws CIMException {
        try {
            concurrentObj.readLock();
            return getQualifierTypeDo(namespace, qtName);
        } finally {
            concurrentObj.readUnlock();
        }
    }

    private CIMQualifierType getQualifierTypeDo(String namespace, String qtName)
            throws CIMException {

        CIMQualifierType qt = null;
        CIMQualifierTypeRlogEntry qtentry = null;
        try {

            CIMNameSpaceRlogEntry nsentry =
                    getNameSpaceEntryFromMap(namespace);

            qtentry = nsentry.getQualifierType(qtName);
            if (qtentry != null) {
                qt = (CIMQualifierType)
                        deserialize(qtentry.getValue());
            }
        } catch (CIMException e) {
            if (verbose) {
                e.printStackTrace();
            }
            throw e;
        } catch (Exception e) {
            if (verbose) {
                e.printStackTrace();
            }
            throw new CIMException(CIMException.CIM_ERR_FAILED, e.toString());
        }
        return qt;
    }

    /*
     * Retrieve the class that specified by the CIMObjectPath
     *
     * @param op    the CIMObjectPath of this class
     * @return CIMClass    the retrived CIMClass object
     */
    public CIMClass getClass(CIMObjectPath op)
            throws CIMException {
        return getClass(op.getNameSpace(), op.getObjectName());
    }

    /*
     * Retrieve the class that specified by the class name
     *
     * @param namespace    name of namespace that contains this class
     * @param className    the name of this class
     * @return CIMClass    the retrived CIMClass object
     */
    public CIMClass getClass(String namespace, String className)
            throws CIMException {
        try {
            concurrentObj.readLock();
            return getClassDo(namespace, className);
        } finally {
            concurrentObj.readUnlock();
        }
    }

    private CIMClass getClassDo(String namespace, String className)
            throws CIMException {

        CIMClass cc = null;
        CIMClassRlogEntry ccentry = null;
        try {
            CIMNameSpaceRlogEntry nsentry =
                    getNameSpaceEntryFromMap(namespace);
            ccentry = nsentry.getClass(className);
            if (ccentry != null) {
                cc = (CIMClass) deserialize(ccentry.getValue());
            }
        } catch (CIMException e) {
            return null;
        } catch (Exception e) {
            if (verbose) {
                e.printStackTrace();
            }
            throw new CIMException(CIMException.CIM_ERR_FAILED, e.toString());
        }
        return cc;
    }

    /*
     * Retrieve the instance that specified by the CIMObjectPath
     *
     * @param op    the CIMObjectPath of this instance
     * @return CIMInstance    the retrived CIMInstance object
     */
    public CIMInstance getInstance(CIMObjectPath op)
            throws CIMException {

        String namespace = op.getNameSpace();
        String className = op.getObjectName();

        CIMClass cc = getClass(namespace, className);

        CIMInstance nci = cc.newInstance();
        nci.updatePropertyValues(op.getKeys());

        // <PJA> 16-Jan-2003
        // BUGFIX 655351 getInstance fails for associators
        // use new key for instances
        CIMInstance ci = getInstance(namespace, InstanceNameUtils.getInstanceNameKey(nci));

        // See if must generate read indication
        eventHelper.instanceRead(namespace.toLowerCase(), ci);

        return ci;
    }

    /*
     * Retrieve the instance that specified by the instance name
     *
     * @param namespace    name of namespace that contains this instance
     * @param iname    the name of this instance
     * @return CIMInstance    the retrived CIMInstance object
     */
    public CIMInstance getInstance(String namespace, String iname)
            throws CIMException {
        try {
            concurrentObj.readLock();
            return getInstanceDo(namespace, iname);
        } finally {
            concurrentObj.readUnlock();
        }
    }

    private CIMInstance getInstanceDo(String namespace, String iname)
            throws CIMException {

        String cname = iname.substring(0, iname.indexOf(':'));
        CIMInstance ci = null;
        CIMInstanceRlogEntry cientry = null;

        try {

            CIMNameSpaceRlogEntry nsentry =
                    getNameSpaceEntryFromMap(namespace);

            CIMClassRlogEntry ccentry =
                    nsentry.getClass(cname);

            if (ccentry == null) {
                throw new CIMClassException
                        (CIMException.CIM_ERR_NOT_FOUND, cname);
            }

            if ((cientry = ccentry.getInstance(iname)) != null) {
                ci = (CIMInstance) deserialize(cientry.getValue());
            }
        } catch (CIMException e) {
            if (verbose) {
                e.printStackTrace();
            }
            throw e;
        } catch (Exception e) {
            if (verbose) {
                e.printStackTrace();
            }
            throw new CIMException(CIMException.CIM_ERR_FAILED, e.toString());
        }
        return ci;
    }

    /*
     * Delete the specified qualifier
     *
     * @param op    the CIMObjectPath of this qualifier
     */
    public void deleteQualifier(CIMObjectPath op)
            throws CIMException {
        deleteQualifier(op.getNameSpace(), op.getObjectName());
    }

    /*
     * Delete the specified qualifier
     *
     * @param namespace    name of namespace that contains this qualifier
     * @param qtName    the name of this qualifier
     */
    public void deleteQualifier(String namespace, String qtName)
            throws CIMException {
        try {
            concurrentObj.writeLock();
            deleteQualifierDo(namespace, qtName);
        } finally {
            concurrentObj.writeUnlock();
        }
    }

    private void deleteQualifierDo(String namespace, String qtName)
            throws CIMException {

        try {
            CIMNameSpaceRlogEntry nsentry =
                    getNameSpaceEntryFromMap(namespace);
            nsentry.removeQualifierType(qtName);
            addLogRecord(new DeleteQualifierLogObj(namespace, qtName));
        } catch (CIMException e) {
            if (verbose) {
                e.printStackTrace();
            }
            throw e;
        } catch (Exception e) {
            if (verbose) {
                e.printStackTrace();
            }
            throw new CIMException(CIMException.CIM_ERR_FAILED, e.toString());
        }
    }

    /*
     * Delete the specified namespace
     *
     * @param namespace    name of namespace that needs to be deleted
     */
    public void deleteNameSpace(String namespace)
            throws CIMException {
        try {
            concurrentObj.writeLock();
            deleteNameSpaceDo(namespace);
        } finally {
            concurrentObj.writeUnlock();
        }
    }

    private void deleteNameSpaceDo(String namespace)
            throws CIMException {

        if (namespace == null || namespace.length() == 0) {
            throw new CIMNameSpaceException(
                    CIMException.CIM_ERR_INVALID_NAMESPACE, " ");
        }

        String ns = namespace.replace('\\', '/');

        try {
            CIMNameSpaceRlogEntry nsentry =
                    (CIMNameSpaceRlogEntry) nameSpacesMap.get(ns);
            if (nsentry != null) {
                nameSpacesMap.remove(ns);
            } else {
                throw new CIMNameSpaceException(
                        CIMException.CIM_ERR_NOT_FOUND, namespace);
            }
            addLogRecord(new DeleteNameSpaceLogObj(namespace));
        } catch (CIMException e) {
            if (verbose) {
                e.printStackTrace();
            }
            throw e;
        } catch (Exception e) {
            if (verbose) {
                e.printStackTrace();
            }
            throw new CIMException(CIMException.CIM_ERR_FAILED, e.toString());
        }
    }

    /*
     * Delete the specified class
     *
     * @param op    the CIMObjectPath of this class
     */
    public void deleteClass(CIMObjectPath op)
            throws CIMException {
        CIMClass cc = getClass(op);
        String namespace = op.getNameSpace();
        deleteClass(namespace, op.getObjectName());

        // See if somebody is interested in class deletion for this namespace
        eventHelper.classDeletion(namespace, cc);
    }

    /*
     * Delete the specified class
     *
     * @param namespace    name of namespace that contains this class
     * @param className    the name of this class
     */
    public void deleteClass(String namespace, String className)
            throws CIMException {
        try {
            concurrentObj.writeLock();
            deleteClassDo(namespace, className);
        } finally {
            concurrentObj.writeUnlock();
        }
    }

    private void deleteClassDo(String namespace, String className)
            throws CIMException {

        try {
            CIMNameSpaceRlogEntry nsentry =
                    getNameSpaceEntryFromMap(namespace);

            CIMClassRlogEntry ccentry = nsentry.getClass(className);
            if (ccentry == null) {
                throw new CIMClassException(
                        CIMException.CIM_ERR_NOT_FOUND, className);
            }

            if (ccentry.hasClassAssociation()) {
                throw new CIMException(CIMException.CIM_ERR_FAILED, className);
            }

            String supercc = ccentry.getSuperClassName();
            CIMClassRlogEntry superentry = nsentry.getClass(
                    (supercc.length() == 0) ? TOP : supercc);

            if (superentry == null) {
                throw new CIMClassException
                        (CIMException.CIM_ERR_NOT_FOUND, supercc);
            }

            validateAssociationClass(ccentry);
            superentry.removeSubClass(className);
            nsentry.removeClass(className);
            // Remove associations from referenced classes
            if (ccentry instanceof CIMAssocClassRlogEntry) {

                ((CIMAssocClassRlogEntry) ccentry).removeClassAssociations();
            }

            ArrayList enum=new ArrayList(ccentry.getSubClassCollection());
            for (int i = 0; i < enum.size();
            i++){
                ccentry = (CIMClassRlogEntry) enum.get(i);
                ArrayList v = new ArrayList(ccentry.getSubClassCollection());
                for (int j = 0; j < v.size(); j++) {
                    enum.add(v.get(j));
                }
                nsentry.removeClass(ccentry.getNameKey());
                // Remove associations from referenced classes
                if (ccentry instanceof CIMAssocClassRlogEntry) {
                    ((CIMAssocClassRlogEntry) ccentry).removeClassAssociations();
                }
            }
            addLogRecord(new DeleteClassLogObj(namespace, className));
        } catch (CIMException e) {
            if (verbose) {
                e.printStackTrace();
            }
            throw e;
        } catch (Exception e) {
            if (verbose) {
                e.printStackTrace();
            }
            throw new CIMException(CIMException.CIM_ERR_FAILED, e.toString());
        }
    }

    void validateAssociationClass(CIMClassRlogEntry entry)
            throws CIMException {
        if (entry instanceof CIMAssocClassRlogEntry) {
            ((CIMAssocClassRlogEntry) entry).validateClassAsociations();
        }
        ArrayList enum=new ArrayList(entry.getSubClassCollection());
        for (int i = 0; i < enum.size();
        i++){
            CIMClassRlogEntry ccentry = (CIMClassRlogEntry) enum.get(i);
            ArrayList v = new ArrayList(ccentry.getSubClassCollection());
            for (int j = 0; j < v.size(); j++) {
                enum.add(v.get(j));
            }
            // validate associations from referenced classes
            if (ccentry instanceof CIMAssocClassRlogEntry) {
                ((CIMAssocClassRlogEntry) ccentry).validateClassAsociations();
            }
        }
    }

    /*
     * Delete the specified instance
     *
     * @param op    the CIMObjectPath of this instance
     */
    public void deleteInstance(CIMObjectPath op)
            throws CIMException {

        String namespace = op.getNameSpace();
        String className = op.getObjectName();

        CIMClass cc = getClass(namespace, className);
        CIMInstance nci = cc.newInstance();
        nci.updatePropertyValues(op.getKeys());
        // <PJA> 16-Jan-2003
        // BUGFIX 655351 getInstance fails for associators
        // use new key for instances
        CIMInstance ci = getInstance(namespace,
                InstanceNameUtils.getInstanceNameKey(nci));
        if (ci == null) {
            throw new CIMInstanceException(
                    CIMException.CIM_ERR_NOT_FOUND, nci.getObjectPath());
        }

        // Remove binding
        // <PJA> 16-Jan-2003
        // BUGFIX 655351 getInstance fails for associators
        // use new key for instances
        deleteInstance(namespace, InstanceNameUtils.getInstanceNameKey(ci));

        // See if need to create an indication for this instance
        eventHelper.instanceDeletion(namespace.toLowerCase(), ci);
    }

    /*
     * Delete the specified instance
     *
     * @param namespace    name of namespace that contains this instance
     * @param iname the name of this class
     */
    public void deleteInstance(String namespace, String iname)
            throws CIMException {
        try {
            concurrentObj.writeLock();
            deleteInstanceDo(namespace, iname);
        } finally {
            concurrentObj.writeUnlock();
        }

    }

    private void deleteInstanceDo(String namespace, String iname)
            throws CIMException {

        String cname = iname.substring(0, iname.indexOf(':'));
        try {
            CIMNameSpaceRlogEntry nsentry =
                    getNameSpaceEntryFromMap(namespace);

            CIMClassRlogEntry ccentry = nsentry.getClass(cname);

            if (ccentry == null) {
                throw new CIMClassException
                        (CIMException.CIM_ERR_NOT_FOUND, cname);
            }

            CIMInstanceRlogEntry cientry = ccentry.getInstance(iname);

            if (cientry.hasAssociation()) {
                throw new CIMException(CIMException.CIM_ERR_FAILED, iname);
            }
            if (cientry instanceof CIMAssocInstanceRlogEntry) {
                ((CIMAssocInstanceRlogEntry)
                        cientry).removeInstanceAssociations(cname, iname);
            }
            ccentry.removeInstance(iname);
            addLogRecord(new DeleteInstanceLogObj(namespace, iname));
        } catch (CIMException e) {
            if (verbose) {
                e.printStackTrace();
            }
            throw e;
        } catch (Exception e) {
            if (verbose) {
                e.printStackTrace();
            }
            throw new CIMException(CIMException.CIM_ERR_FAILED, e.toString());
        }
    }

    /*
     * Enumerate the specified namespace
     *
     * @param namespace    name of namesapce that needs to be enumerated
     * @param deep    true for deep enumeration; false for shallow enumeration
     * @return Vector    list of name of namespaces
     */
    public Vector enumerateNameSpace(String namespace, boolean deep)
            throws CIMException {

        try {
            concurrentObj.readLock();
            Vector ns = new Vector();
            enumNameSpaceDo(ns);
            return ns;
        } finally {
            concurrentObj.readUnlock();
        }
    }

    /*
     * Enumerate all namespaces
     *
     * @return Vector    list of names of all namespaces
     */
    public Vector enumerateNameSpace()
            throws CIMException {

        try {
            concurrentObj.readLock();
            Vector ns = new Vector();
            enumNameSpaceDo(ns);
            return ns;
        } finally {
            concurrentObj.readUnlock();
        }
    }

    /*
     * Enumerate the specified namespace
     *
     * @param namespace    name of namesapce that needs to be enumerated
     * @param deep    true for deep enumeration; false for shallow enumeration
     * @param Vector    list of name of namespaces
     */
    /*    private void enumNameSpaceDo(String namespace, boolean deep, Vector nslist)
	throws CIMException {
	enumNameSpaceDo(nsList);
    }
    */
    /*
     * Enumerate the specified namespace
     *
     */
    private void enumNameSpaceDo(Vector nslist)
            throws CIMException {

        ArrayList enum=new
                ArrayList(nameSpacesMap.values());
        for (int i = 0; i < enum.size();
        i++){
            CIMNameSpaceRlogEntry ns =
                    (CIMNameSpaceRlogEntry) enum.get(i);
            nslist.add(ns.getName());
        }
    }

    /*
    private void enumNameSpaceDo(String namespace, boolean deep, Vector nslist)
	throws CIMException {

	if (namespace == null || namespace.length() == 0) {
	    namespace = CIMROOT;
	}

	nslist.addElement(namespace);
	try {
	    CIMNameSpaceRlogEntry nsentry = 
		getNameSpaceEntryFromMap(namespace); 
	    ArrayList enum = new 
		ArrayList(nsentry.getSubNameSpaceCollection());
	    for (int i = 0; i < enum.size(); i++) {
		CIMNameSpaceRlogEntry subns = 
		    (CIMNameSpaceRlogEntry)enum.get(i);
		ArrayList a = 
		    new ArrayList(subns.getSubNameSpaceCollection());
		if (deep) {
		    for (int j = 0; j < a.size(); j++) {
			enum.add(a.get(j));
		    }
		}
		nslist.add(subns.getName());
	    }
	    // remove the namespace which is being enumerated.
	    nslist.remove(0);
	} catch (CIMException e) {
	    if (verbose) e.printStackTrace();
	    throw e;
	} catch (Exception e) {
	    if (verbose) e.printStackTrace();
	    throw new CIMException(CIMException.CIM_ERR_FAILED, e.toString());
	} 
    }
    */

    /*
     * Enumerate qualifiers of the specified namespace
     *
     * @param op    the CIMObjectPath of namesapce 
     * @return Vector    list of CIMObjectPath of qualifiers
     */
    public Vector enumerateQualifierTypes(CIMObjectPath op)
            throws CIMException {
        return enumerateQualifierTypes(op.getNameSpace());
    }

    /*
     * Enumerate qualifiers of the specified namespace
     *
     * @param namespace    name of namespace that needs to be enumerated
     * @return Vector    list of CIMQualifierTypes
     */
    public Vector enumerateQualifierTypes(String namespace)
            throws CIMException {
        try {
            concurrentObj.readLock();
            return enumerateQualifierTypesDo(namespace);
        } finally {
            concurrentObj.readUnlock();
        }
    }

    private Vector enumerateQualifierTypesDo(String namespace)
            throws CIMException {

        Vector qualifiers = new Vector();
        try {
            CIMNameSpaceRlogEntry nsentry =
                    getNameSpaceEntryFromMap(namespace);
            ArrayList a = new ArrayList(nsentry.getQualifierCollection());
            CIMQualifierType qt = null;
            for (int i = 0; i < a.size(); i++) {
                CIMQualifierTypeRlogEntry qtentry =
                        (CIMQualifierTypeRlogEntry) a.get(i);
                if (qtentry != null) {
                    qt = (CIMQualifierType)
                            deserialize(qtentry.getValue());
                }
                qualifiers.addElement(qt);
                //    qualifiers.addElement(
                //	new CIMObjectPath(qtentry.getName(), namespace));
            }

        } catch (CIMException e) {
            if (verbose) {
                e.printStackTrace();
            }
            throw e;
        } catch (Exception e) {
            if (verbose) {
                e.printStackTrace();
            }
            throw new CIMException(CIMException.CIM_ERR_FAILED, e.toString());
        }
        return qualifiers;
    }

    /*
     * Enumerate classes of the specified class
     *
     * @param op    the CIMObjectPath of this class
     * @param deep    true for deep enumeration; false for shallow enumeration
     * @return Vector    list of CIMObjectPath of classes
     */
    public Vector enumerateClasses(CIMObjectPath op, boolean deep)
            throws CIMException {
        return (enumerateClasses(op.getNameSpace(), op.getObjectName(), deep));
    }

    /*
     * Enumerate classes of the specified class
     *
     * @param namespace    name of namespace that contains this class
     * @param className    name of this class
     * @param deep    true for deep enumeration; false for shallow enumeration
     * @return Vector    list of CIMObjectPath of classes
     */
    public Vector enumerateClasses(String namespace, String className,
            boolean deep) throws CIMException {

        try {
            concurrentObj.readLock();
            return enumClassesDo(namespace, className, deep, true, false);
        } finally {
            concurrentObj.readUnlock();
        }
    }

    /*
     * Enumerate classes of the specified class
     *
     * @param op    the CIMObjectPath of this class
     * @param deep    true for deep enumeration; false for shallow enumeration
     * @param localonly    true for class contains only local elements
     * @return Vector   list of CIMClass of classes
     */
    public Vector enumerateClasses(CIMObjectPath op, boolean deep,
            boolean localonly) throws CIMException {
        return (enumerateClasses(op.getNameSpace(),
                op.getObjectName(), deep, localonly));
    }

    /*
     * Enumerate classes of the specified class
     *
     * @param namespace    name of namespace that contains this class
     * @param className    name of this class
     * @param deep    true for deep enumeration; false for shallow enumeration
     * @param localonly    true for class conatins only local elements
     * @return Vector   list of CIMClass of classes
     */
    public Vector enumerateClasses(String namespace,
            String className,
            boolean deep,
            boolean localonly)
            throws CIMException {
        try {
            concurrentObj.readLock();
            return enumClassesDo(namespace, className,
                    deep, false, localonly);
        } finally {
            concurrentObj.readUnlock();
        }
    }

    /*
     * Enumerate classes of the specified class
     *
     * @param namespace    name of namespace that contains this class
     * @param className    name of this class
     * @param deep    true for deep enumeration; false for shallow enumeration
     * @param needop    true for returning CIMObjectPath; false for CIMClass
     * @param localonly    true for class contains only local elements
     * @return Vector   list of CIMObjectPath/CIMClass of classes
     */
    private Vector enumClassesDo(String namespace,
            String className,
            boolean deep,
            boolean needop,
            boolean localonly)
            throws CIMException {

        Vector classList = new Vector();

        if ((className == null) || className.length() == 0 ||
                (className.equalsIgnoreCase(TOP))) {
            className = TOP;
        }

        try {
            CIMNameSpaceRlogEntry nsentry =
                    getNameSpaceEntryFromMap(namespace);
            CIMClassRlogEntry ccentry = nsentry.getClass(className);
            if (ccentry == null) {
                throw new CIMClassException(
                        CIMException.CIM_ERR_NOT_FOUND, className);
            }

            ArrayList enum=new ArrayList(ccentry.getSubClassCollection());
            for (int i = 0; i < enum.size();
            i++){
                ccentry = (CIMClassRlogEntry) enum.get(i);
                if (deep) {
                    ArrayList v =
                            new ArrayList(ccentry.getSubClassCollection());
                    for (int j = 0; j < v.size(); j++) {
                        enum.add(v.get(j));
                    }
                }
                if (needop) {
                    classList.addElement(new
                            CIMObjectPath(ccentry.getName(), namespace));
                } else {
                    CIMClass cc = (CIMClass) deserialize
                            (ccentry.getValue());
                    classList.addElement(localonly ? cc.localElements() : cc);
                }
            }
        } catch (CIMException e) {
            if (verbose) {
                e.printStackTrace();
            }
            throw e;
        } catch (Exception e) {
            if (verbose) {
                e.printStackTrace();
            }
            throw new CIMException(CIMException.CIM_ERR_FAILED, e.toString());
        }
        return classList;
    }

    /*
     * Enumerate instances of the specified class
     *
     * @param op    the CIMObjectPath of this class
     * @param deep    true for deep enumeration; false for shallow enumeration
     * @return Vector   list of CIMObjectPath of instances
     */
    public Vector enumerateInstances(CIMObjectPath op, boolean deep)
            throws CIMException {
        return (enumerateInstances(op.getNameSpace(),
                op.getObjectName(), deep));
    }

    /*
     * Enumerate instances of the specified class
     *
     * @param namespace    name of namespace that contains this class
     * @param className    name of this classs
     * @param deep    true for deep enumeration; false for shallow enumeration
     * @return Vector   list of CIMObjectPath of instances
     */
    public Vector enumerateInstances(String namespace, String className,
            boolean deep) throws CIMException {
        return (enumInstances(namespace, className, deep, true, false));
    }

    /*
     * Enumerate instances of the specified class
     *
     * @param op    the CIMObjectPath of this class
     * @param deep    true for deep enumeration; false for shallow enumeration
     * @param localonly    true for class contains only local elements
     * @return Vector   list of CIMInstance of instances
     */
    public Vector enumerateInstances(CIMObjectPath op, boolean deep,
            boolean localonly) throws CIMException {
        final Vector instanceList =
                enumerateInstances(op.getNameSpace(), op.getObjectName(),
                        deep, localonly);
        // pass this to the thread
        eventHelper.instanceEnumerate(op, instanceList);

        return instanceList;
    }

    /*
     * Enumerate instances of the specified class
     *
     * @param namespace    name of namespace that contains this class
     * @param className    name of this classs
     * @param deep    true for deep enumeration; false for shallow enumeration
     * @param localonly    true for class contains only local elements
     * @return Vector   list of CIMInsatnce of instances
     */
    public Vector enumerateInstances(String namespace,
            String className,
            boolean deep,
            boolean localonly)
            throws CIMException {
        return (enumInstances(namespace, className, deep, false, localonly));
    }

    /*
     * Enumerate instances of the specified class
     *
     * @param namespace    name of namespace that contains this class
     * @param className    name of this classs
     * @param deep    true for deep enumeration; false for shallow enumeration
     * @param needop    true for returning CIMObjectPath; false for CIMClass
     * @param localonly    true for class contains only local elements
     * @return Vector   list of CIMObjectPath/CIMInstance of instances
     */
    private Vector enumInstances(String namespace,
            String className,
            boolean deep,
            boolean needop,
            boolean localonly)
            throws CIMException {
        try {
            concurrentObj.readLock();
            return enumInstancesDo(namespace, className, deep, needop,
                    localonly);
        } finally {
            concurrentObj.readUnlock();
        }
    }

    private Vector enumInstancesDo(String namespace,
            String className,
            boolean deep,
            boolean needop,
            boolean localonly)
            throws CIMException {

        Vector instanceList = new Vector();

        if ((className == null) || (className.equalsIgnoreCase(TOP))) {
            className = TOP;
        }

        try {
            CIMNameSpaceRlogEntry nsentry =
                    getNameSpaceEntryFromMap(namespace);

            CIMClassRlogEntry ccentry = nsentry.getClass(className);

            if (ccentry == null) {
                throw new CIMClassException(
                        CIMException.CIM_ERR_NOT_FOUND, className);
            }

            // Get instances associated with the class
            getInstanceList(namespace, ccentry, needop, instanceList);

            // Get instances associated with the subclasses
            if (deep) {
                ArrayList enum=new ArrayList(ccentry.getSubClassCollection());
                for (int i = 0; i < enum.size();
                i++){
                    ccentry = (CIMClassRlogEntry) enum.get(i);
                    ArrayList v =
                            new ArrayList(ccentry.getSubClassCollection());
                    for (int j = 0; j < v.size(); j++) {
                        enum.add(v.get(j));
                    }
                    getInstanceList(namespace, (CIMClassRlogEntry) enum.get(i),
                            needop, instanceList);
                }
            }
        } catch (CIMException e) {
            if (verbose) {
                e.printStackTrace();
            }
            throw e;
        } catch (Exception e) {
            if (verbose) {
                e.printStackTrace();
            }
            throw new CIMException(CIMException.CIM_ERR_FAILED, e.toString());
        }
        return instanceList;
    }

    /*
     * Construct a list of CIMObjectPath/CIMInstance of the specified class
     *   
     * @param namespace name of namespace that contains this class
     * @param className    name of this class
     * @param list    list of CIMObjectPath/CIMInstance
     * @param needop    true for returning CIMObjectPath; false for CIMClass
     */
    private void getInstanceList(String namespace, CIMClassRlogEntry ccentry,
            boolean needop, Vector list)
            throws CIMException {
        try {
            Iterator iter = ccentry.getInstanceCollection().iterator();
            while (iter.hasNext()) {
                CIMInstanceRlogEntry cientry =
                        (CIMInstanceRlogEntry) iter.next();
                CIMInstance ci =
                        (CIMInstance) deserialize(cientry.getValue());
                if (needop) {
                    CIMObjectPath op = new CIMObjectPath(ci.getClassName(), ci.getKeys());
                    op.setNameSpace(namespace);
                    list.addElement(op);
                } else {
                    list.addElement(ci);
                }
            }
        } catch (Exception e) {
            throw new CIMException(CIMException.CIM_ERR_FAILED, e.toString());
        }
    }

    static CIMNameSpaceRlogEntry getNameSpaceEntryFromMap(String namespace)
            throws CIMException {
        String ns = namespace.replace('\\', '/');
        CIMNameSpaceRlogEntry nsentry =
                (CIMNameSpaceRlogEntry) nameSpacesMap.get(ns);
        if (nsentry == null) {
            throw new CIMNameSpaceException(
                    CIMException.CIM_ERR_INVALID_NAMESPACE, namespace);
        }
        return nsentry;
    }

    private void putNameSpaceEntryToMap(CIMNameSpaceRlogEntry nsentry) {
        nameSpacesMap.put(nsentry.getName(), nsentry);
    }

    /*
     * Execute a query statemnet
     *
     * @param namespace    name of namespace that the query operates against
     * @param stmt    the query statement
     * @param String    WQL
     * @param cc	CIMClass to exec the query on
     * @return Vector    List of selected CIMElement
     */
    public Vector execQuery(CIMObjectPath op,
            String stmt,
            String queryType,
            CIMClass cc)
            throws CIMException {
        String namespace = op.getNameSpace();
        ByteArrayInputStream in = new ByteArrayInputStream(stmt.getBytes());
        WQLParser parser = new WQLParser(in);
        Vector result = new Vector();
        try {
            SelectExp q = (SelectExp) parser.querySpecification();
            SelectList attrs = q.getSelectList();
            QueryExp where = q.getWhereClause();
            Vector v = enumerateInstances(namespace,
                    cc.getName().toLowerCase(), false, false);

            // filtering the instances
            for (int i = 0; i < v.size(); i++) {
                if ((where == null) ||
                        (where.apply((CIMInstance) v.elementAt(i)) == true)) {
                    result.addElement(attrs.apply((CIMInstance) v.elementAt(i)));
                }
            }

        } catch (CIMException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CIMException(CIMException.CIM_ERR_FAILED, e.toString());
        }

        return result;
    }

    private void init() throws IOException {

        if (logpath == null) {
            logpath = System.getProperty("logdir",
                    System.getProperty("logparent", "/var/sadm/wbem")
                            + File.separator + "logr");
        }
        log = new ReliableLog(logpath, new LocalLogHandler());
        store = new PersistentStore(logpath + "/store");
        CIMRlogEntry.setPersistentStore(store);
        inRecovery = true;
        log.recover();
        inRecovery = false;
        if (nameSpacesMap == null) {
            nameSpacesMap = new HashMap();
        }
        log.snapshot();
        snapshotter.start();
    }

    private void takeSnapshot(OutputStream out) throws IOException {
        ObjectOutputStream stream = new ObjectOutputStream(out);
        stream.writeUTF(PSRlogImpl.class.getName());
        stream.writeInt(LOG_VERSION);
        stream.writeObject(nameSpacesMap);
        store.addToSnapshot(stream);
        stream.writeObject(null);
        stream.flush();
    }

    private void recoverSnapshot(InputStream in)
            throws IOException, ClassNotFoundException {
        ObjectInputStream stream = new ObjectInputStream(in);
        if (!PSRlogImpl.class.getName().equals(stream.readUTF())) {
            throw new IOException("log from wrong implementation");
        }
        if (stream.readInt() != LOG_VERSION) {
            throw new IOException("wrong log format version");
        }
        nameSpacesMap = (HashMap) stream.readObject();
        store.recoverFromSnapshot(stream);

    }

    private void addLogRecord(LogRecord rec) {
        try {
            if (!inRecovery) {
                log.update(rec, true);
                if (++logFileSize >= logToSnapshotThresh) {
                    concurrentObj.waiterNotify(snapshotNotifier);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * Enumerate CIM Objects that are associated to a specific source CIM Object
     *
     * @param op	  the source CIM Object
     * @param assocClass  the CIM Class act as a filter on the results 
     * @param resultClass the CIM Class act as a filter on the results
     * @param role	  the Property name act as a filter on the results
     * @param resultRole  the Property name act as a filter on the results
     * @return Vector	  List of CIM Classes
     */

    public Vector associatorClassNames(CIMObjectPath op,
            String assocClass,
            String resultClass,
            String role,
            String resultRole)
            throws CIMException {
        return classAssociators(op, assocClass, resultClass, role,
                resultRole, false, false, null, true, true);
    } 

    /*
    * Enumerate CIM Objects that are associated to a specific source CIM Object
    *
    * @param op	  	 the source CIM Object
    * @param assocClass  the CIM Class act as a filter on the results 
    * @param resultClass the CIM Class act as a filter on the results
    * @param role	 the Property name act as a filter on the results
    * @param resultRole  the Property name act as a filter on the results
    * @param includeQualifiers if true all Qualifiers are 
    * @param includeClassOrigin if true the CLASSORIGIN property is
				included in the result	
    * @param propertyList List of Property names
    * @return Vector	  List of CIM Classes
    */

    public Vector associatorsClass(CIMObjectPath op,
            String assocClass,
            String resultClass,
            String role,
            String resultRole,
            boolean includeQualifiers,
            boolean includeClassOrigin,
            String propertyList[])
            throws CIMException {
        return classAssociators(op, assocClass, resultClass, role,
                resultRole, includeQualifiers, includeClassOrigin,
                propertyList, false, true);
    }

    /*
    * Enumerate CIM Objects that are associated to a specific source CIM Object
    *
    * @param op	  	 the source CIM Object
    * @param resultClass the CIM Class act as a filter on the results
    * @param role	  the Property name act as a filter on the results
    * @param includeQualifiers if true all Qualifiers are included 
    * @param includeClassOrigin if true the CLASSORIGIN property is
				included in the result	
    * @param propertyList List of Property names
    * @return Vector	  List of CIM Classes
    */

    public Vector reference(CIMObjectPath op,
            String resultClass,
            String role,
            boolean includeQualifiers,
            boolean includeClassOrigin,
            String propertyList[])
            throws CIMException {
        return classAssociators(op, resultClass, null, role,
                null, includeQualifiers, includeClassOrigin,
                propertyList, false, false);
    }
				 
    /*
     * Enumerate CIM Objects that are associated to a specific source CIM Object
     *
     * @param op	  the source CIM Object
     * @param resultClass the CIM Class act as a filter on the results
     * @param role	  the Property name act as a filter on the results
     * @return Vector	  List of CIM Classes
     */

    public Vector referenceNames(CIMObjectPath op,
            String resultClass,
            String role)
            throws CIMException {
        return classAssociators(op, resultClass, null, role,
                null, false, false, null, true, false);
    }

    private Vector classAssociators(CIMObjectPath op,
            String assocClass,
            String resultClass,
            String role,
            String resultRole,
            boolean includeQualifiers,
            boolean includeClassOrigin,
            String propertyList[],
            boolean needop,
            boolean includeAssociatedClass)
            throws CIMException {
        try {
            concurrentObj.readLock();
            return classAssociatorsDo(op, assocClass, resultClass, role,
                    resultRole, includeQualifiers,
                    includeClassOrigin, propertyList,
                    needop, includeAssociatedClass);
        } finally {
            concurrentObj.readUnlock();
        }

    }

    private Vector classAssociatorsDo(CIMObjectPath op,
            String assocClass,
            String resultClass,
            String role,
            String resultRole,
            boolean includeQualifiers,
            boolean includeClassOrigin,
            String propertyList[],
            boolean needop,
            boolean includeAssociatedClass)
            throws CIMException {

        Vector v = new Vector();
        if (role != null && role.length() != 0 &&
                resultRole != null && resultRole.length() != 0 &&
                role.equalsIgnoreCase(resultRole)) {
            return v;
        }

        String ns = op.getNameSpace();
        String cname = op.getObjectName();
        ns = ns.replace('\\', '/');
        CIMNameSpaceRlogEntry nsentry = getNameSpaceEntryFromMap(ns);
        ccAssociators(nsentry, cname, assocClass, resultClass, role,
                resultRole, includeQualifiers, includeClassOrigin,
                propertyList, v, needop, includeAssociatedClass);
        return v;
    }

    private void ccAssociators(CIMNameSpaceRlogEntry nsentry,
            String cname,
            String assocClass,
            String resultClass,
            String role,
            String resultRole,
            boolean includeQualifiers,
            boolean includeClassOrigin,
            String propertyList[],
            Vector v,
            boolean needop,
            boolean includeAssociatedClass)
            throws CIMException {

        ArrayList assoclist = new ArrayList();
        ArrayList resultclist = new ArrayList();

        try {
            CIMClassRlogEntry ccentry = nsentry.getClass(cname);

            if (ccentry == null) {
                throw new CIMClassException(
                        CIMException.CIM_ERR_NOT_FOUND, cname);
            }

            // deep enumerate the specified association class
            if (assocClass != null && assocClass.length() != 0) {
                assoclist = deepEnum(nsentry, assocClass);
            }

            // deep enumerate the specified result class
            if (resultClass != null && resultClass.length() != 0) {
                resultclist = deepEnum(nsentry, resultClass);
                String sc = nsentry.getClass(resultClass).getSuperClassName();
                while ((sc.length() != 0) && (!sc.equals(TOP))) {
                    resultclist.add(sc);
                    sc = nsentry.getClass(sc).getSuperClassName();
                }

            }

            CIMAssocClassRlogEntry[] list = ccentry.getAssocications();

            HashMap dups = new HashMap();
            for (int i = 0; list != null && i < list.length; i++) {
                // Check for duplicates due to reflexive associations
                if (dups.get(list[i].getName()) != null) {
                    continue;
                }

                boolean match = true;

                // match assocClass
                if (assocClass != null && assocClass.length() != 0) {
                    match = matchClassList(list[i].getName(), assoclist);
                }

                // match role
                ArrayList al = null;
                if (match) {
                    al = list[i].matchRole(cname, role, resultRole);
                }

                if (al != null && !al.isEmpty()) {
                    boolean didit = false;
                    boolean addnull = false;
                    for (int k = 0; k < al.size(); k++) {
                        if (!matchClassList((String) al.get(k), resultclist)) {
                            continue;
                        }

                        addnull = true;

                        // Note: Assoc class always return class itself
                        if (!didit) {
                            didit = true;
                            addToResultList(nsentry, list[i].getName(), true,
                                    true, null, v, false);
                            dups.put(list[i].getName(), "");
                        }

                        if (includeAssociatedClass) {
                            addToResultList(nsentry, (String) al.get(k),
                                    includeQualifiers, includeClassOrigin,
                                    propertyList, v, needop);
                        }
                    }
                    if (addnull) {
                        v.addElement(null);
                    }
                }
            }
            String supercc = ccentry.getSuperClassName();
            if ((supercc == null) || (supercc.length() == 0)) {
                return;
            }
            ccAssociators(nsentry, supercc, assocClass, resultClass, role,
                    resultRole, includeQualifiers, includeClassOrigin,
                    propertyList, v, needop, includeAssociatedClass);
        } catch (CIMException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CIMException(CIMException.CIM_ERR_FAILED, e.toString());
        }
    }

    private boolean matchClassList(String cname, ArrayList list) {
        if (list.isEmpty()) {
            return true;
        }
        for (int j = 0; j < list.size(); j++) {
            if (cname.equalsIgnoreCase((String) list.get(j))) {
                return true;
            }
        }
        return false;
    }

    private void addToResultList(CIMNameSpaceRlogEntry nsentry,
            String cname,
            boolean includeQualifiers,
            boolean includeClassOrigin,
            String propertyList[],
            Vector v,
            boolean needop)
            throws CIMException {
        try {
            if (needop) {
                v.addElement(new CIMObjectPath(cname, nsentry.getName()));
                return;
            }

            CIMClassRlogEntry ccentry = nsentry.getClass(cname);

            if (ccentry == null) {
                throw new CIMClassException(
                        CIMException.CIM_ERR_NOT_FOUND, cname);
            }

            CIMClass cc = (CIMClass) deserialize(ccentry.getValue());
            v.addElement(cc.filterProperties(propertyList, includeQualifiers,
                    includeClassOrigin));
        } catch (CIMException e) {
            throw e;
        } catch (Exception e) {
            throw new CIMException(CIMException.CIM_ERR_FAILED, e.toString());
        }
    }

    private ArrayList deepEnum(CIMNameSpaceRlogEntry nsentry, String cname)
            throws CIMException {
        CIMClassRlogEntry ccentry = nsentry.getClass(cname);
        if (ccentry == null) {
            throw new CIMClassException(
                    CIMException.CIM_ERR_NOT_FOUND, cname);
        }

        ArrayList list = new ArrayList();
        list.add(cname);
        for (int i = 0; i < list.size(); i++) {
            ccentry = nsentry.getClass((String) list.get(i));

            if (ccentry == null) {
                throw new CIMClassException(
                        CIMException.CIM_ERR_NOT_FOUND, cname);
            }

            ArrayList enum=new ArrayList(ccentry.getSubClassKeys());
            for (int j = 0; j < enum.size();
            j++){
                list.add( enum.get(j));
            }
        }
        return list;
    }

    private String getInstanceName(CIMNameSpaceRlogEntry nsentry,
            CIMObjectPath op)
            throws CIMException {

        CIMClassRlogEntry ccentry = nsentry.getClass(op.getObjectName());
        CIMClass cc = (CIMClass) deserialize(ccentry.getValue());
        CIMInstance nci = cc.newInstance();
        nci.updatePropertyValues(op.getKeys());

        // <PJA> 16-Jan-2003
        // BUGFIX 655351 getInstance fails for associators
        // use new key for instances
        return InstanceNameUtils.getInstanceNameKey(nci);
    }

    /*
     * Enumerate CIM Objects that are associated to a specific source CIM Object
     *
     * @param op	  the source CIM Object
     * @param assocClass  the CIM Class act as a filter on the results 
     * @param resultClass the CIM Class act as a filter on the results
     * @param role	  the Property name act as a filter on the results
     * @param resultRole  the Property name act as a filter on the results
     * @return Vector	  List of CIM Classes
     */

    public Vector associatorNames(CIMObjectPath op,
            CIMObjectPath objectName,
            String resultClass,
            String role,
            String resultRole)
            throws CIMException {
        return instanceAssociators(op, objectName, resultClass, role,
                resultRole, false, false, null, true, false);
    } 

    /*
    * Enumerate CIM Objects that are associated to a specific source CIM Object
    *
    * @param op	  the source CIM Object
    * @param assocClass  the CIM Class act as a filter on the results 
    * @param resultClass the CIM Class act as a filter on the results
    * @param role	  the Property name act as a filter on the results
    * @param resultRole  the Property name act as a filter on the results
    * @param includeQualifiers if true all Qualifiers are included 
    * @param includeClassOrigin if true the CLASSORIGIN property is
			   included in the result	
    * @param propertyList List of Property names
    * @return Vector	  List of CIM Classes
    */

    public Vector associators(CIMObjectPath op,
            CIMObjectPath objectName,
            String resultClass,
            String role,
            String resultRole,
            boolean includeQualifiers,
            boolean includeClassOrigin,
            String propertyList[])
            throws CIMException {
        return instanceAssociators(op, objectName, resultClass, role,
                resultRole, includeQualifiers, includeClassOrigin,
                propertyList, true, false);
    }

    /*
    * Enumerate CIM Objects that are associated to a specific source CIM Object
    *
    * @param op	  	  the source CIM Object
    * @param resultClass  the CIM Class act as a filter on the results
    * @param role	  the Property name act as a filter on the results
    * @param includeQualifiers if true all Qualifiers are included 
    * @param includeClassOrigin if true the CLASSORIGIN property is
				included in the result	
    * @param propertyList List of Property names
    * @return Vector	  List of CIM Classes
    */

    public Vector reference(CIMObjectPath op,
            CIMObjectPath objectName,
            String role,
            boolean includeQualifiers,
            boolean includeClassOrigin,
            String propertyList[])
            throws CIMException {
        return instanceAssociators(op, objectName, role,
                null, includeQualifiers, includeClassOrigin,
                propertyList, false, true);
    }
				 
    /*
    * Enumerate CIM Objects that are associated to a specific source CIM Object
    *
    * @param op	  the source CIM Object
    * @param resultClass the CIM Class act as a filter on the results
    * @param role	  the Property name act as a filter on the results
    * @return Vector	  List of CIM Classes
    */

    public Vector referenceNames(CIMObjectPath op,
            CIMObjectPath objectName,
            String role)
            throws CIMException {
        return instanceAssociators(op, objectName, role,
                null, false, false, null, true, true);
    }

    private Vector instanceAssociators(CIMObjectPath assocName,
            CIMObjectPath objectName, String role, String resultRole,
            boolean includeQualifiers, boolean includeClassOrigin,
            String propertyList[], boolean needop, boolean isRef)
            throws CIMException {

        return instanceAssociators(assocName, objectName, null, role,
                resultRole, includeQualifiers, includeClassOrigin,
                propertyList, needop, isRef);

    }

    private Vector instanceAssociators(CIMObjectPath assocName,
            CIMObjectPath objectName, String resultClass, String role,
            String resultRole, boolean includeQualifiers,
            boolean includeClassOrigin, String propertyList[], boolean needop,
            boolean isRef) throws CIMException {

        Vector v = new Vector();
        if (role != null && role.length() != 0 &&
                resultRole != null && resultRole.length() != 0 &&
                role.equalsIgnoreCase(resultRole)) {
            return v;
        }

        String ns = objectName.getNameSpace();
        String cname = objectName.getObjectName();
        String asname = assocName.getObjectName();
        ns = ns.replace('\\', '/');

        try {
            concurrentObj.readLock();
            CIMNameSpaceRlogEntry nsentry = getNameSpaceEntryFromMap(ns);
            CIMClassRlogEntry ccentry = nsentry.getClass(cname);

            if (ccentry == null) {
                throw new CIMClassException(
                        CIMException.CIM_ERR_NOT_FOUND, cname);
            }
            String iname = getInstanceName(nsentry, objectName);
            CIMInstanceRlogEntry cientry = ccentry.getInstance(iname);
            if (cientry == null) {
                throw new CIMInstanceException(
                        CIMException.CIM_ERR_NOT_FOUND, iname);
            }
            CIMAssocInstanceRlogEntry[] cimap = cientry.getAssociations(asname);

            if (cimap == null) {
                return v;
            }

            HashMap dups = new HashMap();
            for (int i = 0; i < cimap.length; i++) {

                // Check for duplicates due to reflexive associations
                if (dups.get(cimap[i].getName()) != null) {
                    continue;
                }
                dups.put(cimap[i].getName(), "");

                if (isRef) {
                    CIMObjectPath asop =
                            cimap[i].matchAssociator(iname, role, resultRole);
                    if (asop != null) {
                        if (needop) {
                            v.addElement(asop);
                        } else {
                            CIMInstance ci = getInstance(asop);
                            v.addElement(ci.filterProperties(propertyList,
                                    includeQualifiers, includeClassOrigin));
                        }
                    }
                } else {
		    /*ArrayList rv = 
			cimap[i].matchRole(iname, role, resultRole);
		    for (int k = 0; k < rv.size(); k++) {
			v.addElement(rv.get(k));
		    }*/

                    ArrayList rv =
                            cimap[i].matchRole(iname, role, resultRole);
                    // BUGFIX 640363. Repository does not process resultClass 
                    // properly for associator calls.
                    // Note: There is an opportunity for optimization here. When
                    // the CIMOM breaks up an associator call into multiple
                    // subcalls, this resultClassSet will be re-evaluated every
                    // time. If the repository interface is changed to take
                    // in this map, this overhead can be eliminated.
                    Set resultClassSet = null;
                    if ((resultClass != null) &&
                            (resultClass.length() != 0)) {
                        // resultClassSet should now contain resultClass as
                        // well as it subclasses. We use this to filter out
                        // all object paths not belong to resultClass.
                        resultClassSet = new HashSet();
                        resultClassSet.add(resultClass.toLowerCase());
                        List resultV = enumClassesDo(ns, resultClass, true, true, false);
                        Iterator resultE = resultV.iterator();
                        while (resultE.hasNext()) {
                            CIMObjectPath op = (CIMObjectPath) resultE.next();
                            resultClassSet.add(op.getObjectName().toLowerCase());
                        }
                    }
                    int size = rv.size();
                    for (int k = 0; k < size; k++) {
                        // Filter out those instance names which do not
                        // belong to resultClass
                        CIMObjectPath op = (CIMObjectPath) (rv.get(k));
                        if ((resultClassSet != null) &&
                                (!resultClassSet.contains(op.getObjectName().toLowerCase()))) {
                            // This object path does not belong to resultClass
                            // Skip it.
                            continue;
                        }
                        v.addElement(op);
                    }
                }
            }
        } catch (CIMException e) {
            if (verbose) {
                e.printStackTrace();
            }
            throw e;
        } catch (Exception e) {
            if (verbose) {
                e.printStackTrace();
            }
            throw new CIMException(CIMException.CIM_ERR_FAILED, e.toString());
        } finally {
            concurrentObj.readUnlock();
        }

        return v;
    }

    static byte[] serialize(Object obj)
            throws CIMException {

        ByteArrayOutputStream bas;
        ObjectOutputStream oos;
        byte[] bytes;

        try {
            bas = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bas);
            oos.writeObject(obj);
            oos.flush();
            bytes = bas.toByteArray();
            oos.close();
            bas.close();
        } catch (Exception e) {
            throw new CIMException(CIMException.CIM_ERR_FAILED, e.toString());
        }
        return bytes;
    }

    static Object deserialize(byte[] bytes)
            throws CIMException {

        ByteArrayInputStream bas;
        ObjectInputStream ois;
        Object obj;

        try {
            bas = new ByteArrayInputStream(bytes);
            ois = new ObjectInputStream(bas);
            obj = ois.readObject();
            ois.close();
            bas.close();
        } catch (Exception e) {
            throw new CIMException(CIMException.CIM_ERR_FAILED, e.toString());
        }
        return obj;
    }

    public void additionTriggerActivate(String namespace, String className) {
        eventHelper.additionTriggerActivate(namespace, className);
    }

    public void deletionTriggerActivate(String namespace, String className) {
        eventHelper.deletionTriggerActivate(namespace, className);
    }

    public void modificationTriggerActivate(String namespace,
            String className) {
        eventHelper.modificationTriggerActivate(namespace, className);
    }

    public void additionTriggerDeActivate(String namespace, String className) {
        eventHelper.additionTriggerDeActivate(namespace, className);
    }

    public void deletionTriggerDeActivate(String namespace, String className) {
        eventHelper.deletionTriggerDeActivate(namespace, className);
    }

    public void modificationTriggerDeActivate(String namespace,
            String className) {
        eventHelper.modificationTriggerDeActivate(namespace, className);
    }

    public void authorizeFilter(SelectExp filter, String eventType,
            CIMObjectPath classPath, String owner) throws CIMException {
        eventHelper.authorizeFilter(filter, eventType, classPath, owner);
    }

    public boolean mustPoll(SelectExp filter, String eventType,
            CIMObjectPath classPath) throws CIMException {
        return (eventHelper.mustPoll(filter, eventType, classPath));
    }

    public void activateFilter(SelectExp filter, String eventType,
            CIMObjectPath classPath, boolean firstActivation) throws CIMException {
        eventHelper.activateFilter(filter, eventType, classPath,
                firstActivation);
    }

    public void deActivateFilter(SelectExp filter, String eventType,
            CIMObjectPath classPath, boolean lastActivation) throws CIMException {
        eventHelper.deActivateFilter(filter, eventType, classPath,
                lastActivation);
    }

    public void initialize(CIMOMHandle ch) {
        // Do nothing
    }

    public void cleanup() {
        // Do nothing
    }

}
 

