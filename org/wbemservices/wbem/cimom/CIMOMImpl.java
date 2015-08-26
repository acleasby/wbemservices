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
 *are Copyright � 2001 Sun Microsystems, Inc.
 *
 *All Rights Reserved.
 *
 *Contributor(s): WBEM Solutions, Inc.
 */

package org.wbemservices.wbem.cimom;

import org.wbemservices.wbem.cimom.security.UserPasswordProvider;
import org.wbemservices.wbem.cimom.util.DynClassLoader;
import org.wbemservices.wbem.repository.PSRlogImpl;

import javax.wbem.cim.*;
import javax.wbem.client.*;
import javax.wbem.provider.*;
import javax.wbem.query.*;
import javax.wbem.security.SecurityUtil;
import java.io.*;
import java.lang.reflect.Method;
import java.util.*;

//XXX: temp until we fix Registry Registration (command lIne -R)

/**
 * This is the class which implements all the CIMOM functionality.
 *
 * @author Sun Microsystems, Inc.
 * @version 1.212, 02/28/02
 * @since WBEM 1.0
 */
class CIMOMImpl implements CIMInstanceProvider,
        CIMAssociatorProvider,
        CIMMethodProvider,
        CIMOMServer {

    final static String SYSTEMNS = "/root/system";
    private final static String SECURITYNS = "/root/security";
    final static String DEFAULTNS = "/root/cimv2";
    final static String INTEROPNS = "/interop";
    final static String READ = "read";
    private final static String WRITE = "write";
    private final static String OUTPARAM = "OUT";
    private final static String INPARAM = "IN";
    private final static String MODULENAME = "CIMOM";
    private final static String BUNDLENAME = "org.wbemservices.wbem.cimom.CIMOM";

    private Hashtable serviceClass;
    // XXX factory
    //private Hashtable serviceProvider;
    //private Hashtable providers;
    private String dbHost = "";
    static boolean verbose = false;

    static PSRlogImpl ps;
    private final ReadersWriter concurrentObj = new ReadersWriter();
    // XXX factory
    // private ProviderChecker provCheck = null;
    private ClassChecker classCheck = null;
    private CIMOMUtils cu = null;

    private boolean firstTime = true;
    private Object firstTimeSemaphore = new Object();
    private static boolean inBuild = false;
    private final static long MEMORY_THRESH = 1000000;

    private final Runtime rt = Runtime.getRuntime();
    private UserPasswordProvider upp = null;

    private EventService eventService = null;

    // XXX factory
    private static ProviderAdapterFactory mProvFactory = null;

    private Mofregistry mofreg = null;

    private void checkIndication(CIMClass cc) throws CIMException {

        CIMQualifier cqe = cc.getQualifier("indication");
        if ((cqe != null) && (cqe.getValue().equals(CIMValue.TRUE))) {
            throw new CIMClassException(
                    CIMClassException.CIM_ERR_FAILED,
                    cc.getName(), "Indication");
        }
    }

    private void checkAbstractOrIndication(CIMClass cc) throws CIMException {

        checkIndication(cc);

        CIMQualifier cqe = cc.getQualifier("abstract");
        if ((cqe != null) && (cqe.getValue().equals(CIMValue.TRUE))) {
            throw new CIMClassException(
                    CIMClassException.ABSTRACT_INSTANCE,
                    cc.getName());
        }
    }

    private String logMessage(
            String summaryMesgID,
            String detailedMesgID,
            String[] args,
            String data,
            boolean syslog_flag,
            int category,
            int severity) {
        CIMOMLogService ls = (CIMOMLogService)
                ServiceRegistry.getService(CIMOMLogService.DEFAULT);
        if (ls == null) {
            return null;
        }
        String logResult = null;
        try {
            logResult =
                    ls.writeLog(MODULENAME, summaryMesgID, detailedMesgID, args,
                            data, syslog_flag, category, severity, BUNDLENAME);
        } catch (Exception le) {
            // Ignore the exception
            Debug.trace2("logging error", le);
        }
        return logResult;
    }

    private void checkMemory() throws CIMException {
        Debug.trace3("CIMOMImpl checkMemory CALLED");
        Debug.trace3("FREE MEMORY " + rt.freeMemory());
        Debug.trace3("TOTAL MEMORY " + rt.totalMemory());
        if (rt.freeMemory() < MEMORY_THRESH) {
            rt.gc();
            if (rt.freeMemory() < MEMORY_THRESH) {
                Debug.trace3("CIMOMImpl checkMemory CALLED");
                Debug.trace3("FREE MEMORY " + rt.freeMemory());
                Debug.trace3("TOTAL MEMORY " + rt.totalMemory());
                throw new CIMException(CIMException.CIM_ERR_LOW_ON_MEMORY);
            }
        }
    }

    /*
     * Start inner class section.
     * The internal providers are internal private classes so that they 
     * have better access to the CIMOM internals.
     *
     */
    // This class is a holder for providers that deal with CIMOM classes.
    private class CIMOMProviderHolder implements
            InternalProviderAdapter.InternalServiceProvider {
        // WBEMServices_ObjectManager is a subclass of CIM_ObjectManager,
        private static final String WBEMSERVICES_OBJECTMANAGER =
                "WBEMServices_ObjectManager";
        private static final String CIM_NAMESPACE = "CIM_Namespace";
        private static final String CIM_NS_IN_MANAGER =
                "CIM_NamespaceInManager";

        private CIMProvider cimomProvider = new CIMOMProvider(mofreg);
        private CIMProvider namespaceProvider = new NamespaceProvider();
        private CIMProvider nsInManagerProvider =
                new NamespaceInManagerProvider();

        // Returns the names of the internal providers.
        public String[] getProviderNames() {
            return new String[] { WBEMSERVICES_OBJECTMANAGER, CIM_NAMESPACE,
                    CIM_NS_IN_MANAGER };
        }

        // For the given providerName, returns the actual provider.
        public CIMProvider getProvider(String providerName) throws
                CIMException {
            if (providerName.equalsIgnoreCase(CIM_NAMESPACE)) {
                return namespaceProvider;
            } else if (providerName.equalsIgnoreCase(CIM_NS_IN_MANAGER)) {
                return nsInManagerProvider;
            } else {
                return cimomProvider;
            }
        }
    }

    // This is an associators implementation for the internal providers
    private Vector commonAssociators(CIMObjectPath assocName,
            CIMObjectPath objectName,
            String resultClass,
            String role,
            String resultRole,
            boolean includeQualifiers,
            boolean includeClassOrigin,
            String propertyList[]) throws CIMException {

        Vector tvops = null;

        tvops = ps.associators(assocName, objectName, resultClass, role,
                resultRole, includeQualifiers, includeClassOrigin,
                propertyList);

        // Now retrieve each instance separately.
        Vector tv = new Vector();
        CIMNameSpace tns = new CIMNameSpace("", "");
        Enumeration e1 = tvops.elements();
        while (e1.hasMoreElements()) {
            CIMObjectPath tvop = (CIMObjectPath) e1.nextElement();

            CIMInstance ci = intgetInstance(tns, tvop, false, includeQualifiers,
                    includeClassOrigin, propertyList, true);
            ci = ci.filterProperties(propertyList, includeQualifiers,
                    includeClassOrigin);
            tv.addElement(ci);
        }
        return tv;
    }

    public CIMOMImpl(String args[]) throws Exception {

        if (System.getProperty("org.wbemservices.wbem.build") != null) {
            inBuild = true;
        }
        I18N.setResourceName(BUNDLENAME);
        parseCommandLine(args);

        // Initialize debug tracing if enabled
        initTrace();

        // XXX factory
        mProvFactory = new ProviderAdapterFactory(new ProviderClient(this));

        firstTimeInit();
    }

    public CIMOMImpl() throws Exception {

        if (System.getProperty("org.wbemservices.wbem.build") != null) {
            inBuild = true;
        }
        I18N.setResourceName(BUNDLENAME);
        try {
            ps = new PSRlogImpl(dbHost, concurrentObj);
            cu = new CIMOMUtils(ps);
            classCheck = new ClassChecker(cu);

            // Initialize debug tracing if enabled
            initTrace();

            // XXX factory
            mProvFactory = new ProviderAdapterFactory(new ProviderClient(this));

        } catch (Exception e) {

            Debug.trace2("Got exception", e);
            throw e;
        }

    }

    private CIMObjectPath createObjectPath(String cns, CIMInstance ci) {
        CIMObjectPath cop = new CIMObjectPath();
        cop.setObjectName(ci.getClassName());
        cop.setKeys(ci.getKeys());
        cop.setNameSpace(cns);
        return cop;
    }

    private CIMObjectPath createObjectPath(CIMNameSpace cns, CIMObjectPath op) {
        CIMObjectPath cop = new CIMObjectPath();
        cop.setObjectName(op.getObjectName());
        cop.setKeys(op.getKeys());
        cop.setNameSpace(getNameSpace(cns, op));
        return cop;
    }

    void delayedCapabilityCheck(CIMMethod me, boolean providerCall,
            String operation, String namespace)
            throws CIMException {
        // We dont do any check if the provider has called us
        if (providerCall) {
            return;
        }
        // get method provider, if there is one (checks if the method has a 
        // specific authorizing provider
        Authorizable cp = null;
        if (me != null) {
            // If me is null, it means that the default check needs to be
            // invoked.
            try {
                cp = CIMOMImpl.getProviderFactory().getAuthorizableProvider(
                        namespace, me);
            } catch (CIMException ce) {
                // if we get a NOT_AUTHORIZABLE_PROVIDER exception, it
                // means the provider was found, but it doesnt want to do
                // authorization - so we do the default check. Otherwise,
                // we throw the exception.
                if (!ce.getID().equals(
                        CIMProviderException.NOT_AUTHORIZABLE_PROVIDER)) {
                    throw ce;
                }
            }
        }
        if (cp != null) {
            // we dont need to do any check if the provider is
            // Authorizable.
            return;
        } else {
            if (ServerSecurity.getRequestSession() == null) {
                new ServerSecurity("root", "", "cimom", new byte[4]);
            }
            // apply the default CIMOM check.
            verifyCapabilities((ServerSecurity)
                            ServerSecurity.getRequestSession(),
                    operation,
                    namespace);
        }
    }

    void delayedCapabilityCheck(CIMProperty pe, boolean providerCall,
            String operation, String namespace)
            throws CIMException {
        // We dont do any check if the provider has called us
        if (providerCall) {
            return;
        }
        // get property provider, if there is one (checks if the property has a 
        // specific authorizing provider
        Authorizable cp = null;
        if (pe != null) {
            // If me is null, it means that the default check needs to be
            // invoked.
            try {
                cp = CIMOMImpl.getProviderFactory().getAuthorizableProvider(
                        namespace, pe);
            } catch (CIMException ce) {
                // if we get a NOT_AUTHORIZABLE_PROVIDER exception, it
                // means the provider was found, but it doesnt want to do
                // authorization - so we do the default check. Otherwise,
                // we throw the exception.
                if (!ce.getID().equals(
                        CIMProviderException.NOT_AUTHORIZABLE_PROVIDER)) {
                    throw ce;
                }
            }
        }
        if (cp != null) {
            // we dont need to do any check if the provider is
            // Authorizable.
            return;
        } else {
            // apply the default CIMOM check.
            if (ServerSecurity.getRequestSession() == null) {
                new ServerSecurity("root", "", "cimom", new byte[4]);
            }
            verifyCapabilities((ServerSecurity)
                            ServerSecurity.getRequestSession(),
                    operation,
                    namespace);
        }
    }

    void delayedCapabilityCheck(CIMClass cc, boolean providerCall,
            String operation, String namespace)
            throws CIMException {

        // We dont do any check if the provider has called us
        if (providerCall) {
            return;
        }
        // XXX factory
        // changed first param to CIMClass, now get provider
        Authorizable cp = null;
        if (cc != null) {
            // If cc is null, it means that the default check needs to be
            // invoked, and no provider needs to be found.
            try {
                cp = CIMOMImpl.getProviderFactory().getAuthorizableProvider(
                        namespace, cc);
            } catch (CIMException ce) {
                // if we get a NOT_AUTHORIZABLE_PROVIDER exception, it
                // means the provider was found, but it doesnt want to do
                // authorization - so we do the default check. Otherwise,
                // we throw the exception.
                if (!ce.getID().equals(
                        CIMProviderException.NOT_AUTHORIZABLE_PROVIDER)) {
                    throw ce;
                }
            }
        }
        if (cp != null) {
            // we dont need to do any check if the provider is
            // Authorizable.
            return;
        } else {
            // apply the default CIMOM check.
            if (ServerSecurity.getRequestSession() == null) {
                new ServerSecurity("root", "", "cimom", new byte[4]);
            }
            verifyCapabilities((ServerSecurity)
                            ServerSecurity.getRequestSession(),
                    operation,
                    namespace);
        }
    }

    private void verifyCapabilities(ServerSecurity ss, String rw,
            CIMNameSpace nameSpace)
            throws CIMException {
        CIMNameSpace validNS = new CIMNameSpace();
        validNS.setNameSpace(nameSpace.getNameSpace() + '/' +
                nameSpace.getNameSpace());
        String ns = validNS.getNameSpace().replace('\\', '/');
        String parentNS = ns.substring(0, ns.lastIndexOf('/'));
        verifyCapabilities(ss, rw, parentNS);
    }

    public void verifyCapabilities(ServerSecurity ss, String operation,
            String namespace) throws CIMException {

        if (inBuild) {
            return;
        }

        CIMNameSpace cns = new CIMNameSpace();
        cns.setNameSpace('/' + namespace);
        namespace = cns.getNameSpace();
        namespace = namespace.substring(1, namespace.length());

        LogFile.methodEntry("verifyCapabilities");
        if (ss.getUserName().equals(SecurityUtil.ADMIN)) {
            LogFile.methodReturn("verifyCapabilities");
            return;
        }

        if (namespace.equals(ss.getCapabilityNS())) {
            checkReadWritePermission(operation, ss);
            LogFile.methodReturn("verifyCapabilities");
            return;
        }

        CIMClass nsAcl = ps.getClass(SECURITYNS, "solaris_namespaceacl");
        if (nsAcl == null) {
            setCapability("rw", namespace, ss);
            LogFile.methodReturn("verifyCapabilities");
            return;
        }

        CIMClass userAcl = ps.getClass(SECURITYNS, "solaris_useracl");
        if (userAcl == null) {
            setCapability("rw", namespace, ss);
            LogFile.methodReturn("verifyCapabilities");
            return;
        }

        CIMInstance nsAclInstance = nsAcl.newInstance();
        CIMInstance userAclInstance = userAcl.newInstance();
        CIMProperty nspace = new CIMProperty("nspace");
        CIMProperty username = new CIMProperty("username");

        LogFile.add(LogFile.DEVELOPMENT, "DEBUG_VALUE", "namespace", namespace);
        LogFile.add(LogFile.DEVELOPMENT, "DEBUG_VALUE", "username",
                ss.getUserName());
        LogFile.add(LogFile.DEVELOPMENT, "DEBUG_VALUE", "operation", operation);

        nspace.setValue(new CIMValue(namespace));
        username.setValue(new CIMValue(ss.getUserName()));

        userAclInstance.updatePropertyValue(nspace);
        userAclInstance.updatePropertyValue(username);

        CIMInstance ci = ps.getInstance(createObjectPath(SECURITYNS,
                userAclInstance));

        if (ci != null) {
            CIMValue cv = ci.getProperty("capability").getValue();
            String cap = (String) cv.getValue();
            setCapability(cap, namespace, ss);
            LogFile.add(LogFile.DEVELOPMENT, "DEBUG_VALUE", "user capability",
                    cap);
            checkReadWritePermission(operation, ss);

            LogFile.methodReturn("verifyCapabilities");
            return;
        }

        nsAclInstance.updatePropertyValue(nspace);
        ci = ps.getInstance(createObjectPath(SECURITYNS, nsAclInstance));
        if (ci != null) {
            CIMValue cv = ci.getProperty("capability").getValue();
            String cap = (String) cv.getValue();
            setCapability(cap, namespace, ss);
            LogFile.add(LogFile.DEVELOPMENT, "DEBUG_VALUE", "ns capability",
                    cap);
            checkReadWritePermission(operation, ss);
            return;
        }

        // default rights - read only
        setCapability("r", namespace, ss);
        if (!operation.equals("read")) {
            throw new CIMSecurityException(
                    CIMSecurityException.CIM_ERR_ACCESS_DENIED);
        }
    }

    private void checkIPropertiesSanity(String namespace, CIMInstance ci)
            throws CIMException {
        String lcns = namespace.toLowerCase();
        Vector prt = ci.getProperties();

        // Must check for duplicate properties/refs.
        CIMProperty props[] = new CIMProperty[prt.size()];
        prt.toArray(props);
        for (int i = 0; i < props.length; i++) {
            CIMProperty pe = props[i];
            try {
                pe.setValue(cu.typeConvert(pe.getType(), pe.getValue()));
            } catch (Exception e) {
                throw new CIMPropertyException(CIMSemanticException.TYPE_ERROR,
                        new Object[] {
                                pe.getOriginClass() + "." + pe.getName(), ci.getClassName(),
                                pe.getType(), pe.getValue().getType(), pe.getValue() });
            }
        }
    }

    // This is invoked by mofreg too. It needs to do type conversion for
    // instances it receives from a MOF file.
    void checkInstanceSanity(String namespace, CIMInstance ci,
            CIMClass cc)
            throws CIMException {

        CIMQualifier qe;
        qe = cc.getQualifier("abstract");
        if (qe == null) {
            qe = cu.createDefaultQualifier(namespace, "abstract");
        }

        if (qe.getValue().equals(CIMValue.TRUE)) {
            throw new CIMClassException(
                    CIMClassException.ABSTRACT_INSTANCE, cc.getName());
        }

        // Vector qualifiers = ci.getQualifiers();

        // doCommonQualifierChecks(namespace, qualifiers,
        //	    CIMScope.getScope(CIMScope.INSTANCE));

        checkIPropertiesSanity(namespace, ci);
    }

    /**
     * Initialize system env paths:
     * logparent  wbem log parent directory
     * is taken from command line, wbemInstallDir.properties
     * or set to /var/sadm/wbem
     * libwbemdir  location of CIMOM jar files
     * propdir  Property files directory
     * org.wbemservices.wbem.cimom.pswdprov  user password provider class
     * exception java.Exception if there is a problem opening the streams.
     */
    private Properties initializeSysEnvPaths() throws Exception {
        String libwbemPath = null;
        String instlogParent = "/var/sadm/wbem";

	/*
     * wbemInstallDir.properties is created during the webstart-installer
	 * wbem instalation and is placed in "user.home" directory.
	 * It contains paths for cimom files and log parent dir. 
	 * For example if installed on Linux "/usr" directory,
	 * /root/wbemInstallDir.properties contains:
	 *  #Installer - WBEM Install Directories
	 *  #Wed Jul 26 14:57:41 EDT 2000
	 *  SYSTEMPLATFORM=UNIX
	 *  LIBWBEMPATH=/usr/sadm/lib/wbem
	 *  WBEMLOGPARENT=/var/sadm/wbem
	 *  WBEMINSTALLDIRECTORY=/usr
	 */
        Properties wbeminstaller = new Properties();
        try {
            File instfile = new File(System.getProperty("user.home") +
                    File.separator + "wbemInstallDir.properties");
            if (instfile.exists()) {
                FileInputStream fin = new FileInputStream(instfile);
                wbeminstaller.load(fin);
                libwbemPath = (String) wbeminstaller.get("LIBWBEMPATH");
                instlogParent = (String) wbeminstaller.get("WBEMLOGPARENT");
                fin.close();
            }
        } catch (Exception ex) {
            Debug.trace2("Got exception", ex);
        }
        if (libwbemPath == null) {
            libwbemPath = System.getProperty("propdir", "/usr/sadm/lib/wbem");
        }
        System.setProperty("libwbemdir", libwbemPath);

        String wbemLogParent = System.getProperty("logparent", instlogParent);
        // logdir is set only at the build time. Check if it is build time
        String buildtimeLogdir = System.getProperty("logdir");
        if (buildtimeLogdir != null) {
            wbemLogParent = buildtimeLogdir + File.separator + "..";
        }
        System.setProperty("logparent", wbemLogParent);

        String pdpath = System.getProperty("propdir", libwbemPath);
        System.setProperty("propdir", pdpath);
        BufferedInputStream bin = new BufferedInputStream(
                new FileInputStream(pdpath + "/cimom.properties"));
        Properties p = new Properties();
        p.load(bin);
        String pwlibpath = p.getProperty(UserPasswordProvider.PSWD_PROV_PROP);
        if (pwlibpath == null) {
            pwlibpath = "org.wbemservices.wbem.cimom.SolarisUserPasswordProvider";
        }
        System.setProperty(UserPasswordProvider.PSWD_PROV_PROP, pwlibpath);
        return p;
    }

    /**
     * Get datastore files generated from the build
     * exception java.Exception if there is a problem opening the streams.
     */
    private void getDatastore() throws Exception {
        String libwbemPath = System.getProperty("libwbemdir",
                "/usr/sadm/lib/wbem");
        String wbemlogPath = System.getProperty("logparent",
                "/var/sadm/wbem") + File.separator + "logr";
        try {
            RandomAccessFile fin1 = new RandomAccessFile(libwbemPath +
                    File.separator + "Logfile.1", "r");
            RandomAccessFile fout1 = new RandomAccessFile(wbemlogPath +
                    File.separator + "Logfile.1", "rw");
            copyFile(fin1, fout1, fin1.length());
            fin1.close();
            fout1.close();
            RandomAccessFile fin2 = new RandomAccessFile(libwbemPath +
                    File.separator + "Snapshot.1", "r");
            RandomAccessFile fout2 = new RandomAccessFile(wbemlogPath +
                    File.separator + "Snapshot.1", "rw");
            copyFile(fin2, fout2, fin2.length());
            fin2.close();
            fout2.close();
            RandomAccessFile fin3 = new RandomAccessFile(libwbemPath +
                    File.separator + "Version_Number", "r");
            RandomAccessFile fout3 = new RandomAccessFile(wbemlogPath +
                    File.separator + "Version_Number", "rw");
            copyFile(fin3, fout3, fin3.length());
            fin3.close();
            fout3.close();
            RandomAccessFile fin4 = new RandomAccessFile(libwbemPath +
                    File.separator + "store", "r");
            RandomAccessFile fout4 = new RandomAccessFile(wbemlogPath +
                    File.separator + "store", "rw");
            copyFile(fin4, fout4, fin4.length());
            fin4.close();
            fout4.close();
        } catch (Exception ex) {
            Debug.trace2("Got exception", ex);
        }
    }

    private void firstTimeInit() throws Exception {

        if (firstTime) {
            firstTime = false;
            Properties props = initializeSysEnvPaths();
            // This section of code takes care of persistent store
            // migration from JavaSpaces to Reliable log. It can
            // actually be used to invoke any initializations that
            // need to be performed after an install.
            if (System.getProperty("logdir") == null) {
                // If logdir is not set, it means we are not in
                // the build, we are running live.

                File notFirsttimeDir = new File(System.getProperty("logparent",
                        "/var/sadm/wbem") + File.separator +
                        "logr" + File.separator + "notFirstTime");
                if (!(notFirsttimeDir.exists())) {
                    if (!(notFirsttimeDir.mkdirs())) {
                        System.out.println("Could not create directory: "
                                + notFirsttimeDir);
                    }
                    getDatastore();
                }
            }

            // This is the first time the CIMOM is being called.
            try {
                ps = new PSRlogImpl(dbHost, concurrentObj);
                cu = new CIMOMUtils(ps);
                // XXX factory
                // provCheck = new ProviderChecker(this);
                classCheck = new ClassChecker(cu);
                // update the classpath
                updateClasspath();
                // Start the provider adapters
                mProvFactory.startAdapters();
                // initialize services
                initializeServices(props);
            } catch (Exception e) {
                Debug.trace2("Got exception", e);
                System.out.println(e);
                throw e;
            }
        }
    }

    public Vector enumerateClasses(String version,
            CIMNameSpace currNs,
            CIMObjectPath path,
            Boolean deep,
            Boolean localOnly,
            Boolean includeQualifiers,
            Boolean includeClassOrigin,
            ServerSecurity ss)
            throws CIMException {
        try {
            LogFile.methodEntry("enumerateClasses");
            verifyCapabilities(ss, READ, getNameSpace(currNs, path));
            Vector v = intenumClass(currNs, path, deep.booleanValue(),
                    localOnly.booleanValue());
            Enumeration e = v.elements();
            Vector tv = new Vector();
            while (e.hasMoreElements()) {
                CIMClass cc = (CIMClass) e.nextElement();
                tv.addElement(cc.filterProperties(null,
                        includeQualifiers.booleanValue(),
                        includeClassOrigin.booleanValue()));
            }
            LogFile.methodReturn("enumerateClasses");
            return tv;
        } catch (CIMException e) {
            Debug.trace2("Got exception", e);
            throw e;
        } catch (Throwable e) {
            Debug.trace2("Got exception", e);
            throw new CIMException(CIMException.CIM_ERR_FAILED, e);
        }

    }

    public Vector enumerateClassNames(String version,
            CIMNameSpace currNs,
            CIMObjectPath path,
            Boolean deep,
            ServerSecurity ss)
            throws CIMException {
        try {
            LogFile.methodEntry("enumerateClassNames");
            verifyCapabilities(ss, READ, getNameSpace(currNs, path));
            Vector rt = intenumClass(currNs, path, deep.booleanValue());
            LogFile.methodReturn("enumerateClassNames");
            return rt;
        } catch (CIMException e) {
            Debug.trace2("Got exception", e);
            throw e;
        } catch (Throwable e) {
            Debug.trace2("Got exception", e);
            throw new CIMException(CIMException.CIM_ERR_FAILED, e);
        }
    }

    private List getDynasty(CIMNameSpace ns, CIMObjectPath className)
            throws CIMException {
        List l = new ArrayList();
        CIMObjectPath tcn = new CIMObjectPath();
        tcn.setNameSpace(className.getNameSpace());
        tcn.setObjectName(className.getObjectName());
        while (true) {
            CIMClass cc = intgetClass(ns, tcn, true);
            if (cc == null) {
                throw new CIMClassException(CIMClassException.CIM_ERR_NOT_FOUND,
                        className.getObjectName().toLowerCase());
            }
            l.add(cc.getName());
            if (cc.getSuperClass().length() == 0) {
                break;
            }
            tcn.setObjectName(cc.getSuperClass());
        }
        return l;
    }

    public Vector enumerateInstances(String version,
            CIMNameSpace currNs,
            CIMObjectPath path,
            Boolean deep,
            Boolean localOnly,
            Boolean includeQualifiers,
            Boolean includeClassOrigin,
            String propertyList[],
            ServerSecurity ss)
            throws CIMException {
        try {
            LogFile.methodEntry("enumerateInstances");
            verifyCapabilities(ss, READ, getNameSpace(currNs, path));
            Vector v = intenumInstances(currNs, path, true,
                    localOnly.booleanValue(), false);
            Enumeration e = v.elements();
            Vector tv = new Vector();
            List classList = null;
            boolean deepValue = deep.booleanValue();
            if (!deepValue) {
                classList = getDynasty(currNs, path);
            }
            while (e.hasMoreElements()) {
                CIMInstance ci = (CIMInstance) e.nextElement();
                if (localOnly.booleanValue()) {
                    ci = ci.localElements();
                }
                if (!deepValue) {
                    ci = ci.localElements(classList);
                }
                tv.addElement(ci.filterProperties(propertyList,
                        includeQualifiers.booleanValue(),
                        includeClassOrigin.booleanValue()));
            }
            LogFile.methodReturn("enumerateInstances");
            return tv;
        } catch (CIMException e) {
            Debug.trace2("Got exception", e);
            throw e;
        } catch (Throwable e) {
            Debug.trace2("Got exception", e);
            throw new CIMException(CIMException.CIM_ERR_FAILED, e);
        }
    }

    public Vector enumerateInstanceNames(String version,
            CIMNameSpace currNs,
            CIMObjectPath path,
            ServerSecurity ss) throws CIMException {
        try {
            LogFile.methodEntry("enumerateInstanceNames");
            verifyCapabilities(ss, READ, getNameSpace(currNs, path));
            Vector rt = intenumInstances(currNs, path, true, false);
            LogFile.methodReturn("enumerateInstanceNames");
            return rt;
        } catch (CIMException e) {
            Debug.trace2("Got exception", e);
            throw e;
        } catch (Throwable e) {
            Debug.trace2("Got exception", e);
            throw new CIMException(CIMException.CIM_ERR_FAILED, e);
        }
    }

    public CIMClass getClass(String version,
            CIMNameSpace currNs,
            CIMObjectPath path,
            Boolean localOnly,
            Boolean includeQualifiers,
            Boolean includeClassOrigin,
            String propertyList[],
            ServerSecurity ss)
            throws CIMException {

        LogFile.methodEntry("getClass");
        verifyCapabilities(ss, READ, getNameSpace(currNs, path));
        CIMClass cc = intgetClass(currNs, path, localOnly.booleanValue());
        LogFile.methodReturn("getClass");

        if (cc == null) {
            throw new CIMClassException(CIMException.CIM_ERR_NOT_FOUND,
                    path.getObjectName());
        }
        return cc.filterProperties(propertyList,
                includeQualifiers.booleanValue(),
                includeClassOrigin.booleanValue());
    }

    /**
     * Client interface to CIMOM for getInstance.
     */
    public CIMInstance getInstance(String version,
            CIMNameSpace currNs,
            CIMObjectPath path,
            Boolean localOnly,
            Boolean includeQualifiers,
            Boolean includeClassOrigin,
            String propertyList[],
            ServerSecurity ss) throws CIMException {
        try {
            LogFile.methodEntry("getInstance");
            CIMInstance ci = intgetInstance(currNs, path,
                    localOnly.booleanValue(), includeQualifiers.booleanValue(),
                    includeClassOrigin.booleanValue(), propertyList, false);
            LogFile.methodReturn("getInstance");
            if (ci == null) {
                throw new CIMInstanceException(CIMException.CIM_ERR_NOT_FOUND,
                        path.toString());
            }
            return ci.filterProperties(propertyList,
                    includeQualifiers.booleanValue(),
                    includeClassOrigin.booleanValue());
        } catch (CIMException e) {
            Debug.trace2("Got exception", e);
            throw e;
        } catch (Throwable e) {
            Debug.trace2("Got exception", e);
            throw new CIMException(CIMException.CIM_ERR_FAILED, e);
        }
    }

    public void close(String version, ServerSecurity ss)
            throws CIMException {
        LogFile.methodEntry("close");
        LogFile.methodReturn("close");
    }

    public void deleteNameSpace(String version, CIMNameSpace parent,
            CIMNameSpace nameSpace,
            ServerSecurity ss)
            throws CIMException {
        try {
            LogFile.methodEntry("secDeleteNameSpace");
            verifyCapabilities(ss, WRITE, parent);
            CIMNSStaticMethods.deleteNameSpace(parent, nameSpace);
            LogFile.methodReturn("secDeleteNameSpace");
        } catch (CIMException e) {
            Debug.trace2("Got exception", e);
            throw e;
        } catch (Throwable e) {
            Debug.trace2("Got exception", e);
            throw new CIMException(CIMException.CIM_ERR_FAILED, e);
        }
    }

    public void createNameSpace(String version,
            CIMNameSpace parent,
            CIMNameSpace nameSpace,
            ServerSecurity ss)
            throws CIMException {
        try {
            verifyCapabilities(ss, WRITE, parent);
            checkMemory();
            LogFile.methodEntry("secCreateNameSpace");
            CIMNSStaticMethods.createNameSpace(parent, nameSpace);
            LogFile.methodReturn("secCreateNameSpace");
        } catch (CIMException e) {
            Debug.trace2("Got exception", e);
            throw e;
        } catch (Throwable e) {
            Debug.trace2("Got exception", e);
            throw new CIMException(CIMException.CIM_ERR_FAILED, e);
        }
    }

    public void createQualifierType(String version,
            CIMNameSpace nameSpace,
            CIMObjectPath objectName,
            CIMQualifierType qt,
            ServerSecurity ss)
            throws CIMException {
        try {
            verifyCapabilities(ss, WRITE,
                    getNameSpace(nameSpace, objectName));
            checkMemory();
            LogFile.methodEntry("createQualiferType");
            CIMQtypeStaticMethods.addCIMElement(nameSpace, objectName, qt);
            LogFile.methodReturn("createQualifierType");
        } catch (CIMException e) {
            Debug.trace2("Got exception", e);
            throw e;
        } catch (Throwable e) {
            Debug.trace2("Got exception", e);
            throw new CIMException(CIMException.CIM_ERR_FAILED, e);
        }
    }

    public void setQualifierType(String version,
            CIMNameSpace nameSpace,
            CIMObjectPath objectName,
            CIMQualifierType qt,
            ServerSecurity ss)
            throws CIMException {

        try {
            verifyCapabilities(ss, WRITE,
                    getNameSpace(nameSpace, objectName));
            checkMemory();
            LogFile.methodEntry("setQualifierType");
            CIMQtypeStaticMethods.setCIMElement(nameSpace, objectName, qt);
            LogFile.methodReturn("setQualifierType");
        } catch (CIMException e) {
            Debug.trace2("Got exception", e);
            throw e;
        } catch (Throwable e) {
            Debug.trace2("Got exception", e);
            throw new CIMException(CIMException.CIM_ERR_FAILED, e);
        }
    }

    private void csClass(CIMNameSpace nameSpace,
            CIMObjectPath objectName,
            CIMClass cc,
            boolean set)
            throws CIMException {
        try {

            if (!set) {
                CIMObjectPath tpath = new CIMObjectPath();
                tpath.setNameSpace(objectName.getNameSpace());
                tpath.setObjectName(cc.getName());
                CIMClass icc = intgetClass(nameSpace, tpath, false);
                if (icc != null) {
                    throw new CIMClassException(
                            CIMException.CIM_ERR_ALREADY_EXISTS,
                            cc.getName());
                }
                classCheck.checkClassSanity(getNameSpace(nameSpace,
                        objectName), cc, null);
                ps.addCIMElement(getNameSpace(nameSpace, objectName), cc);
            } else {
                CIMClass icc = intgetClass(nameSpace, objectName, false);
                if (icc == null) {
                    throw new CIMClassException(
                            CIMException.CIM_ERR_NOT_FOUND, cc.getName());
                }
                icc = icc.localElements();
                Vector v = intenumClass(nameSpace, objectName, false);
                if (v.size() > 0) {
                    throw new CIMClassException(
                            CIMException.CIM_ERR_CLASS_HAS_CHILDREN,
                            cc.getName());
                }
                v = ps.enumerateInstances(createObjectPath(nameSpace,
                        objectName), false);
                if (v.size() > 0) {
                    throw new CIMClassException(
                            CIMException.CIM_ERR_CLASS_HAS_INSTANCES,
                            cc.getName());
                }
                v = cc.getQualifiers();

                CIMQualifier cqs[] = new CIMQualifier[v.size()];
                v.toArray(cqs);

                v = icc.getQualifiers();
                int i;
                for (i = 0; i < cqs.length; i++) {
                    v.removeElement(cqs[i]);
                    v.addElement(cqs[i]);
                }
                icc.setQualifiers(v);

                v = cc.getProperties();
                CIMProperty cps[] = new CIMProperty[v.size()];
                v.toArray(cps);

                v = icc.getProperties();
                for (i = 0; i < cps.length; i++) {
                    CIMProperty cp = cps[i];
                    CIMProperty icp = icc.getProperty(cp.getName(),
                            cp.getOriginClass());
                    if (icp == null) {
                        v.addElement(cp);
                        continue;
                    }

                    // The property exists in icc, so we must check the
                    // change in qualifiers
                    Vector qv = cp.getQualifiers();
                    CIMQualifier pcqs[] = new CIMQualifier[qv.size()];
                    qv.toArray(pcqs);

                    qv = icp.getQualifiers();
                    for (int j = 0; j < pcqs.length; j++) {
                        CIMQualifier pcq = pcqs[j];
                        qv.removeElement(pcq);
                        qv.addElement(pcq);
                    }
                    cp.setQualifiers(qv);
                    v.removeElement(cp);
                    v.addElement(cp);
                }
                icc.setProperties(v);

                v = cc.getMethods();
                CIMMethod cms[] = new CIMMethod[v.size()];
                v.toArray(cms);

                v = icc.getMethods();
                for (i = 0; i < cms.length; i++) {
                    CIMMethod cm = cms[i];
                    CIMMethod icm = icc.getMethod(cm.getName(),
                            cm.getOriginClass());
                    if (icm == null) {
                        v.addElement(cm);
                        continue;
                    }

                    // The method exists in icc, so we must check the
                    // change in qualifiers
                    Vector qv = cm.getQualifiers();
                    CIMQualifier mcqs[] = new CIMQualifier[qv.size()];
                    qv.toArray(mcqs);

                    qv = icm.getQualifiers();
                    for (int j = 0; j < mcqs.length; j++) {
                        CIMQualifier mcq = mcqs[j];
                        qv.removeElement(mcq);
                        qv.addElement(mcq);
                    }
                    cm.setQualifiers(qv);

                    v.removeElement(cm);
                    v.addElement(cm);
                }
                icc.setMethods(v);
                classCheck.checkClassSanity(getNameSpace
                        (nameSpace, objectName), icc, null);
                ps.setClass(getNameSpace(nameSpace, objectName), icc);
                cc = icc;
            }
            // XXX factory
	    /*
	    if (cc.getName().equalsIgnoreCase("Solaris_LogRecord")) {
		// We have the log service.
		CIMProvider ip =
		provCheck.checkInstanceProvider(
		    getNameSpace(nameSpace, objectName), cc);
		if (ip != null) {
		    // We have found the log service provider.
		    serviceClass.put("Solaris_LogRecord", cc);
		    serviceProvider.put("Solaris_LogRecord", ip);
		}
	    }
	    */

            LogFile.add(LogFile.DEBUG, "ADD_CLASS_DEBUG", cc);
        } catch (CIMException e) {
            LogFile.add(LogFile.DEVELOPMENT, "CAUGHT_EXCEPTION", e.toString());
            LogFile.methodReturn("addCIMElement(class)");
            Debug.trace2("Got exception", e);
            throw e;
        } catch (Throwable e) {
            Debug.trace2("Got exception", e);
            e.printStackTrace();
            throw new CIMException(CIMException.CIM_ERR_FAILED, e.toString());
        }

    }

    void intaddCIMElement(CIMNameSpace nameSpace, CIMObjectPath objectName,
            CIMClass cc)
            throws CIMException {
        nameSpace.setNameSpace('/' + nameSpace.getNameSpace());
        csClass(nameSpace, objectName, cc, false);
    }

    void intsetCIMElement(CIMNameSpace nameSpace, CIMObjectPath objectName,
            CIMClass cc)
            throws CIMException {
        nameSpace.setNameSpace('/' + nameSpace.getNameSpace());
        csClass(nameSpace, objectName, cc, true);
    }

    /**
     * Adds a class to the CIMOM Repository
     */
    public void createClass(String version,
            CIMNameSpace nameSpace,
            CIMObjectPath objectName,
            CIMClass cc,
            ServerSecurity ss)
            throws CIMException {
        try {
            checkMemory();
            LogFile.methodEntry("createClass");
            verifyCapabilities(ss, WRITE,
                    getNameSpace(nameSpace, objectName));
            objectName.setObjectName(cc.getName());
            intaddCIMElement(nameSpace, objectName, cc);
            LogFile.methodReturn("createClass");
        } catch (ReadersWriter.ConcurrentLockException e) {
            throw new CIMException(CIMException.CIM_ERR_FAILED, e);
        }
    }

    public void setClass(String version,
            CIMNameSpace nameSpace,
            CIMObjectPath objectName,
            CIMClass cc,
            ServerSecurity ss)
            throws CIMException {
        try {
            checkMemory();
            LogFile.methodEntry("setClass");
            verifyCapabilities(ss, WRITE,
                    getNameSpace(nameSpace, objectName));
            objectName.setObjectName(cc.getName());
            intsetCIMElement(nameSpace, objectName, cc);
            LogFile.methodReturn("setClass");
        } catch (ReadersWriter.ConcurrentLockException e) {
            throw new CIMException(CIMException.CIM_ERR_FAILED, e);
        }
    }

    CIMObjectPath csInstance(CIMNameSpace nameSpace,
            CIMObjectPath objectName,
            CIMInstance ci,
            boolean set,
            boolean providerCall,
            boolean includeQualifiers,
            String[] propertyList,
            boolean checkForProvider)
            throws CIMException {

        CIMObjectPath retValue = null;

        try {
            CIMClass cc;
            cc = intgetClass(nameSpace, objectName, false);
            if (cc == null) {
                throw new CIMInstanceException(
                        CIMInstanceException.CIM_ERR_INVALID_CLASS,
                        objectName.getObjectName());
            }

            checkAbstractOrIndication(cc);

            CIMInstanceProvider ip = null;
            if (checkForProvider) {
                ip = CIMOMImpl.getProviderFactory().getInstanceProvider(
                        getNameSpace(nameSpace, objectName), cc);
            }

            delayedCapabilityCheck(cc, providerCall, WRITE,
                    getNameSpace(nameSpace, objectName));
            CIMInstance nci;
            nci = cc.newInstance();
            nci.updatePropertyValues(ci.getProperties());
            objectName.setKeys(nci.getKeys());

            if (propertyList != null) {
                List newPropList = new ArrayList();
                // Make sure that the class contains all the properties
                // in the property list
                for (int i = 0; i < propertyList.length; i++) {
                    if (cc.getProperty(propertyList[i]) == null) {
                        throw new CIMPropertyException(
                                CIMException.CIM_ERR_NO_SUCH_PROPERTY,
                                propertyList[i]);
                    }
                    if (ci.getProperty(propertyList[i]) == null) {
                        // This means that the propertyList has a property
                        // that is not in the passed in instance. As per
                        // spec this should be ignored
                    } else {
                        newPropList.add(propertyList[i]);
                    }
                }

                int newSize = newPropList.size();
                if (newSize != propertyList.length) {
                    // Some properties were ignored, make a new propertyList
                    propertyList = new String[newSize];
                    propertyList = (String[]) newPropList.toArray(propertyList);
                }
            }

            checkInstanceSanity(getNameSpace(nameSpace, objectName), nci, cc);

            if (ip != null) {
                if (set) {
                    ip.setInstance(createObjectPath(getNameSpace(nameSpace,
                                    objectName), nci), nci, includeQualifiers,
                            propertyList);
                } else {
                    retValue = ip.createInstance(createObjectPath(
                            getNameSpace(nameSpace, objectName), nci), nci);
                }
                return retValue;
            } else {
                CIMInstance inci;
                if (checkForProvider) {
                    // Use the normal getInstance.
                    inci = intgetInstance(nameSpace, objectName, cc, false,
                            includeQualifiers, true, propertyList,
                            false);
                } else {
                    // Get it straight from the repository
                    inci = ps.getInstance(createObjectPath(nameSpace,
                            objectName));
                }
                if ((inci == null) && set) {
                    throw new CIMInstanceException(
                            CIMException.CIM_ERR_NOT_FOUND,
                            objectName.toString());
                }

                if ((inci != null) && !set) {
                    throw new CIMInstanceException(
                            CIMException.CIM_ERR_ALREADY_EXISTS,
                            objectName.toString());
                }

                if (set) {
                    repositorySetInstance(nameSpace, objectName, nci, inci, cc,
                            includeQualifiers, propertyList, checkForProvider);
                    return null;
                }
            }

            Vector vProp = nci.getProperties();
            CIMProperty[] properties = new CIMProperty[vProp.size()];
            vProp.toArray(properties);

            retValue = new CIMObjectPath();
            retValue.setKeys(nci.getKeys());
            retValue.setNameSpace(getNameSpace(nameSpace, objectName));
            retValue.setObjectName(nci.getClassName());
            ps.addCIMElement(getNameSpace(nameSpace, objectName), nci);
            return retValue;
        } catch (CIMException e) {
            Debug.trace2("Got exception", e);
            throw e;
        } catch (Throwable e) {
            Debug.trace2("Got exception", e);
            throw new CIMException(CIMException.CIM_ERR_FAILED, e);
        }
    }

    CIMObjectPath intaddCIMElement(CIMNameSpace nameSpace,
            CIMObjectPath objectName, CIMInstance ci,
            boolean providerCall) throws CIMException {
        nameSpace.setNameSpace('/' + nameSpace.getNameSpace());
        return csInstance(nameSpace, objectName, ci, false, providerCall,
                true, null, true);
    }

    void intsetCIMElement(CIMNameSpace nameSpace, CIMObjectPath objectName,
            CIMInstance ci, boolean providerCall,
            boolean includeQualifiers,
            String[] propertyList,
            boolean checkForProvider)
            throws CIMException {
        nameSpace.setNameSpace('/' + nameSpace.getNameSpace());
        csInstance(nameSpace, objectName, ci, true, providerCall,
                includeQualifiers, propertyList, checkForProvider);
    }

    public CIMObjectPath createInstance(String version,
            CIMNameSpace nameSpace,
            CIMObjectPath objectName,
            CIMInstance ci,
            ServerSecurity ss)
            throws CIMException {
        String className = ci.getClassName();
        checkMemory();
        LogFile.methodEntry("createInstance");
        objectName.setObjectName(ci.getClassName().toLowerCase());
        LogFile.methodReturn("createInstance");
        CIMObjectPath op =
                intaddCIMElement(nameSpace, objectName, ci, false);
        return op;

    }

    public void setInstance(String version,
            CIMNameSpace nameSpace,
            CIMObjectPath objectName,
            CIMInstance ci,
            ServerSecurity ss)
            throws CIMException {
        String className = ci.getClassName();
        checkMemory();
        LogFile.methodEntry("setInstance");
        objectName.setObjectName(ci.getClassName().toLowerCase());
        intsetCIMElement(nameSpace, objectName, ci, false, true, null, true);
        LogFile.methodReturn("setInstance");
    }

    public void setInstance(String version,
            CIMNameSpace nameSpace,
            CIMObjectPath objectName,
            CIMInstance ci,
            boolean includeQualifiers,
            String[] propertyList,
            ServerSecurity ss)
            throws CIMException {
        String className = ci.getClassName();
        checkMemory();
        LogFile.methodEntry("setInstance");
        objectName.setObjectName(ci.getClassName().toLowerCase());
        intsetCIMElement(nameSpace, objectName, ci, false, includeQualifiers,
                propertyList, true);
        LogFile.methodReturn("setInstance");
    }

    void intsetProperty(CIMNameSpace nameSpace, CIMObjectPath objectName,
            String propertyName, CIMValue newValue,
            boolean providerCall) throws CIMException {

        try {
            nameSpace.setNameSpace('/' + nameSpace.getNameSpace());
            CIMClass cc;

            if (objectName == null || objectName.getObjectName() == null) {
                throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER);
            }
            cc = intgetClass(nameSpace, objectName, false);
            if (cc == null) {
                throw new CIMInstanceException(
                        CIMInstanceException.CIM_ERR_INVALID_CLASS,
                        objectName.getObjectName());
            }

            checkAbstractOrIndication(cc);

            CIMProperty propElem = cc.getProperty(propertyName);
            if (propElem == null) {
                // Cannot find this property
                throw new CIMPropertyException(CIMException.CIM_ERR_NOT_FOUND,
                        propertyName);
            }
            // Set the value
            propElem.setValue(newValue);

            CIMInstance nci;
            nci = cc.newInstance();
            nci.updatePropertyValues(objectName.getKeys());
            nci.updatePropertyValue(propElem);
            objectName.setKeys(nci.getKeys());
            objectName.setObjectName(cc.getName());

            // store the factory since we potentially use it in several places
            ProviderAdapterFactory factory = CIMOMImpl.getProviderFactory();
            // Indicates that we have or have not checked rights
            boolean rightsChecked = false;

            // check rights if we have to
            if (!rightsChecked) {
                // If we do this check then it means there is no property 
                // provider so we are dealing with the class provider
                delayedCapabilityCheck(cc, providerCall, WRITE,
                        getNameSpace(nameSpace, objectName));
            }

            // the Instance provider
            CIMInstanceProvider ip = factory.getInstanceProvider(
                    getNameSpace(nameSpace, objectName), cc);

            if (ip != null) {
                String[] propertyList = { propertyName };
                ip.setInstance(createObjectPath(getNameSpace(nameSpace,
                        objectName), nci), nci, true, propertyList);
            } else {
                CIMInstance inci = null;
                // Check if an instance already exists.
                inci = ps.getInstance(createObjectPath(
                        getNameSpace(nameSpace, objectName), nci));
                if (inci != null) {
                    inci.updatePropertyValue(propElem);
                    checkInstanceSanity(getNameSpace(nameSpace, objectName),
                            inci, cc);
                    ps.setInstance(getNameSpace(nameSpace, objectName), inci);
                    return;
                } else {
                    // Cannot find the instance whose property we want to
                    // set.
                    throw new CIMInstanceException(
                            CIMException.CIM_ERR_NOT_FOUND,
                            nci.getObjectPath());
                }
            }

        } catch (CIMException e) {
            LogFile.add(LogFile.DEVELOPMENT, "CAUGHT_EXCEPTION", e.toString());
            LogFile.methodReturn("setProperty");
            Debug.trace2("Got exception", e);
            throw e;
        } catch (Throwable e) {
            Debug.trace2("Got exception", e);
            throw new CIMException(CIMException.CIM_ERR_FAILED, e);
        }
    }

    public void setProperty(String version,
            CIMNameSpace nameSpace,
            CIMObjectPath objectName,
            String propertyName,
            CIMValue newValue,
            ServerSecurity ss)
            throws CIMException {
        checkMemory();
        LogFile.methodEntry("setProperty");
        intsetProperty(nameSpace, objectName, propertyName,
                newValue, false);
        LogFile.methodReturn("setProperty");
    }

    Vector intexecQuery(CIMNameSpace nameSpace, CIMObjectPath relNS,
            String query, String ql, boolean providerCall)
            throws CIMException {

        if (!ql.equalsIgnoreCase(CIMClient.WQL)) {
            // We only support WQL for now.
            throw new CIMException(
                    CIMException.CIM_ERR_QUERY_LANGUAGE_NOT_SUPPORTED, ql);
        }

        nameSpace.setNameSpace('/' + nameSpace.getNameSpace());
        String tnameSpace = getNameSpace(nameSpace, relNS);
        CIMObjectPath path = new CIMObjectPath();
        path.setNameSpace(tnameSpace);
        // we need these keys, so that client apps can have a back door scoping
        // set up here if they want.
        path.setKeys(relNS.getKeys());

        // Find what class is part of the query
        WQLParser parser = new WQLParser(
                new ByteArrayInputStream(query.getBytes()));
        SelectExp exp = null;
        try {
            exp = (SelectExp) parser.querySpecification();
        } catch (Exception e) {
            throw new CIMException(CIMException.CIM_ERR_INVALID_QUERY,
                    query, e.toString());
        }
        NonJoinExp nj = (NonJoinExp) exp.getFromClause();
        QualifiedAttributeExp aexp = nj.getAttribute();
        path.setObjectName(aexp.getAttrClassName());
        relNS.setObjectName(aexp.getAttrClassName());

        Vector v = new Vector();
        Vector fv = new Vector();
        CIMInstance[] tv;

        v = intenumClass(nameSpace, relNS, true);

        String on = path.getObjectName();
        if ((on != null) && (on.length() != 0) &&
                (!on.equals(PSRlogImpl.TOP))) {
            v.insertElementAt(path, 0);
        }

        CIMObjectPath ops[] = new CIMObjectPath[v.size()];
        v.toArray(ops);
        for (int i = 0; i < ops.length; i++) {
            CIMObjectPath op = ops[i];
            // back door
            op.setKeys(path.getKeys());
            CIMClass cc;
            LogFile.add(LogFile.DEVELOPMENT, "DEBUG_VALUE", "op",
                    op.getObjectName());
            String tns = nameSpace.getNameSpace();
            nameSpace.setNameSpace("");
            cc = intgetClass(nameSpace, op, false);
            nameSpace.setNameSpace(tns);
            if (cc == null) {
                throw new CIMClassException(
                        CIMClassException.CIM_ERR_NOT_FOUND,
                        op.getObjectName());
            }
            // XXX factory
            //CIMProvider ip = provCheck.checkInstanceProvider(tnameSpace, cc);

            // Check rights first
            delayedCapabilityCheck(cc, providerCall, READ, tnameSpace);
            // get the provider
            CIMInstanceProvider ip =
                    CIMOMImpl.getProviderFactory().getInstanceProvider(
                            nameSpace.getNameSpace(), cc);

            if (ip != null) {
                // There is an instance provider, invoke it
                op.setObjectName(cc.getName());
                tv = ip.execQuery(op, query, ql, cc);
            } else {
                // There is no instance provider
                Vector tmpV = ps.execQuery(op, query, ql, cc);
                tv = new CIMInstance[tmpV.size()];
                tmpV.toArray(tv);
            }

            if (tv == null) {
                continue;
            }

            // @@@ we need find what type object in tv Vector
            //Object objs[] = tv.toArray();
            SelectList attrs = exp.getSelectList();
            if (tv.length != 0 && tv[0] == null) {
                QueryExp wc = exp.getWhereClause();
                for (int j = 1; j < tv.length; j++) {
                    CIMInstance tci = tv[j];
                    if ((wc == null) ||
                            (wc.apply(tci) == true)) {
                        fv.addElement(attrs.apply(tci));
                    }
                }
            } else {
                for (int j = 0; j < tv.length; j++) {
                    fv.addElement(tv[j]);
                }
            }
        }
        return fv;
    }

    public Vector execQuery(String version,
            CIMNameSpace nameSpace,
            CIMObjectPath relNS,
            String query,
            String ql,
            ServerSecurity ss)
            throws CIMException {
        try {
            LogFile.methodEntry("execQuery");
            Vector r = intexecQuery(nameSpace, relNS, query, ql, false);
            LogFile.methodReturn("execQuery");
            return r;
        } catch (CIMException e) {
            Debug.trace2("Got exception", e);
            throw e;
        } catch (Throwable e) {
            Debug.trace2("Got exception", e);
            throw new CIMException(CIMException.CIM_ERR_FAILED, e);
        }

    }

    Vector intassociators(CIMNameSpace inpnameSpace,
            CIMObjectPath objectName,
            String assocClass,
            String resultClass,
            String role,
            String resultRole,
            boolean biq,
            boolean bic,
            String propertyList[],
            boolean providerCall) throws CIMException {

        if (objectName == null || objectName.getObjectName() == null) {
            throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER);
        }

        if ((resultClass == null) && (propertyList != null)) {
            throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER);
        }

        boolean classOnly = objectName.getKeys() == null ||
                objectName.getKeys().size() == 0;

        CIMNameSpace ns = new CIMNameSpace();
        ns.setNameSpace('/' + inpnameSpace.getNameSpace());
        String nameSpace = getNameSpace(ns, objectName);

        CIMObjectPath path = new CIMObjectPath();
        path.setNameSpace(nameSpace);
        path.setObjectName(objectName.getObjectName());
        path.setKeys(objectName.getKeys());

        CIMObjectPath assocPath = new CIMObjectPath();
        assocPath.setNameSpace(nameSpace);
        assocPath.setObjectName(assocClass);

        LogFile.add(LogFile.DEVELOPMENT, "DEBUG_VALUE", "path", path);
        LogFile.add(LogFile.DEVELOPMENT, "DEBUG_VALUE", "assocPath",
                assocPath);

        Vector v = new Vector();
        Vector fv = new Vector();
        CIMInstance[] tArray = null;

        // invoke the PS method associators(objectName, assocPath, resultClass,
        // role, resultRole, biq, bic, propertyList
        // XXX v = intenumClass(ns, inpath, deep);
        v = ps.associatorsClass(path, assocClass, resultClass, role,
                resultRole, biq, bic, propertyList);

        // v now contains all the association classes and the 
        // corresponding associated classes.
        for (Enumeration e = v.elements(); e.hasMoreElements(); ) {

            // Get the association class. We expect the class to have all 
            // its qualifiers and
            // proeprties and class origins, since this may be required to 
            // find providers, etc.
            CIMClass cc = (CIMClass) (e.nextElement());

            while (e.hasMoreElements()) {
                // Read until we see a null. All the elements before the
                // null are classes that are referred to by the association
                // class
                Object o = e.nextElement();
                if (o == null) {
                    break;
                }

                if (classOnly) {
                    fv.addElement(o);
                }
            }

            if (classOnly) {
                continue;
            }

            // Check rights first
            delayedCapabilityCheck(cc, providerCall, READ, nameSpace);
            // get the provider
            CIMAssociatorProvider ap =
                    CIMOMImpl.getProviderFactory().getAssociatorProvider(
                            nameSpace, cc);
            CIMObjectPath tempOp = new CIMObjectPath();
            tempOp.setObjectName(cc.getName());
            tempOp.setNameSpace(nameSpace);
            LogFile.add(LogFile.DEVELOPMENT, "DEBUG_VALUE", "tempOp",
                    tempOp);
            // do we have an associator provider
            if (ap != null) {
                try {
                    tArray = ap.associators(tempOp, path, resultClass, role,
                            resultRole, biq, bic, propertyList);
                } catch (CIMException ex) {
                    if (ex.getID().equals(CIMException.CIM_ERR_NOT_FOUND)) {
                        // Ignore this one, the provider hasn't heard of
                        // this instance
                        tArray = null;
                    } else {
                        throw ex;
                    }
                }
                // No associator provider, do we have an instance provider
            } else if (CIMOMImpl.getProviderFactory().getInstanceProvider(
                    nameSpace, cc) != null) {
                // Oh well, I'll just pretend there are no associators
                continue;
            } else {
                // There is no instance provider. The PS gives us a list
                // of object paths to the associated instances but not
                // the actual instance itself, since the instance may
                // actually be residing in a provider.
                Vector tvops = null;

                try {
                    tvops = ps.associators(tempOp, path, resultClass, role,
                            resultRole, biq, bic, propertyList);
                } catch (CIMException ex) {
                    if (ex.getID().equals(CIMException.CIM_ERR_NOT_FOUND)) {
                        // Ignore this one, the repository hasnt heard of
                        // this instance
                        tvops = new Vector();
                    } else {
                        throw ex;
                    }
                }

                // Now retrieve each instance separately.
                tArray = new CIMInstance[tvops.size()];
                CIMNameSpace tns = new CIMNameSpace("", "");
                Enumeration e1 = tvops.elements();
                int count = 0;
                while (e1.hasMoreElements()) {
                    CIMObjectPath tvop = (CIMObjectPath) e1.nextElement();

                    // I dont want the delayedCapabilityCheck, since its
                    // already been done
                    CIMInstance ci = intgetInstance(tns, tvop, false, true,
                            true, propertyList, true);
                    ci = ci.filterProperties(propertyList, biq, bic);
                    tArray[count] = ci;
                    count++;
                }
            }

            if (tArray == null) {
                continue;
            }

            for (int j = 0; j < tArray.length; j++) {
                fv.addElement(tArray[j]);
            }
        }

        return fv;
    }

    public Vector associators(String version,
            CIMNameSpace nameSpace,
            CIMObjectPath objectName,
            String assocClass,
            String resultClass,
            String role,
            String resultRole,
            Boolean includeQualifiers,
            Boolean includeClassOrigin,
            String propertyList[],
            ServerSecurity ss) throws CIMException {
        try {
            LogFile.methodEntry("associators");
            Vector rt = intassociators(nameSpace, objectName, assocClass,
                    resultClass, role, resultRole,
                    includeQualifiers.booleanValue(),
                    includeClassOrigin.booleanValue(), propertyList, false);
            LogFile.methodReturn("associators");
            return rt;
        } catch (CIMException e) {
            Debug.trace2("Got exception", e);
            throw e;
        } catch (Throwable e) {
            Debug.trace2("Got exception", e);
            throw new CIMException(CIMException.CIM_ERR_FAILED, e);
        }

    }

    Vector intassociatorNames(CIMNameSpace inpnameSpace,
            CIMObjectPath objectName,
            String assocClass,
            String resultClass,
            String role,
            String resultRole,
            boolean providerCall) throws CIMException {

        if (objectName == null || objectName.getObjectName() == null) {
            throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER);
        }

        boolean classOnly = objectName.getKeys() == null ||
                objectName.getKeys().size() == 0;

        CIMNameSpace ns = new CIMNameSpace();
        ns.setNameSpace('/' + inpnameSpace.getNameSpace());
        String nameSpace = getNameSpace(ns, objectName);

        CIMObjectPath path = new CIMObjectPath();
        path.setNameSpace(nameSpace);
        path.setObjectName(objectName.getObjectName());
        path.setKeys(objectName.getKeys());

        CIMObjectPath assocPath = new CIMObjectPath();
        assocPath.setNameSpace(nameSpace);
        assocPath.setObjectName(assocClass);

        LogFile.add(LogFile.DEVELOPMENT, "DEBUG_VALUE", "path", path);
        LogFile.add(LogFile.DEVELOPMENT, "DEBUG_VALUE", "assocPath", assocPath);

        Vector v = new Vector();
        Vector fv = new Vector();
        CIMObjectPath[] tv = null;

        // invoke the PS method 
        v = ps.associatorClassNames(path, assocClass, resultClass, role,
                resultRole);

        // v now contains all the association classes and the names
        // of the associated classes.
        for (Enumeration e = v.elements(); e.hasMoreElements(); ) {

            // Get the association class
            CIMClass cc = (CIMClass) (e.nextElement());
            while (e.hasMoreElements()) {
                // Read until we see a null. All the elements before the
                // null are classes that are referred to by the association
                // class
                Object o = e.nextElement();
                if (o == null) {
                    break;
                }

                if (classOnly) {
                    fv.addElement(o);
                }
            }

            if (classOnly) {
                continue;
            }

            // XXX factory
            // CIMProvider ip = provCheck.checkInstanceProvider(nameSpace, cc);
            // check rights first
            delayedCapabilityCheck(cc, providerCall, READ, nameSpace);

            CIMAssociatorProvider ap =
                    CIMOMImpl.getProviderFactory().getAssociatorProvider(
                            nameSpace, cc);

            CIMObjectPath tempOp = new CIMObjectPath();
            tempOp.setObjectName(cc.getName());
            tempOp.setNameSpace(nameSpace);
            LogFile.add(LogFile.DEVELOPMENT, "DEBUG_VALUE", "tempOp",
                    tempOp);

            if (ap != null) {
                try {
                    tv = ap.associatorNames(tempOp, path, resultClass,
                            role, resultRole);
                } catch (CIMException ex) {
                    if (ex.getID().equals(CIMException.CIM_ERR_NOT_FOUND)) {
                        // Ignore this one, the provider hasnt heard of
                        // this instance
                        tv = null;
                    } else {
                        throw ex;
                    }
                }
            } else if (CIMOMImpl.getProviderFactory().getInstanceProvider(
                    nameSpace, cc) != null) {
                // Oh well, I'll just pretend there are no associators
                // XXX revisist this logic?
                continue;
            } else {
                try {
                    Vector tmpV = ps.associatorNames(tempOp, path, resultClass,
                            role, resultRole);
                    tv = new CIMObjectPath[tmpV.size()];
                    tmpV.toArray(tv);
                } catch (CIMException ex) {
                    if (ex.getID().equals(CIMException.CIM_ERR_NOT_FOUND)) {
                        // Ignore this one, the repository hasnt heard of
                        // this instance
                        tv = null;
                    } else {
                        throw ex;
                    }
                }
            }

            if (tv == null) {
                continue;
            }

            //Object[] objs = new Object[tv.size()];
            //tv.toArray(objs);
            for (int j = 0; j < tv.length; j++) {
                fv.addElement(tv[j]);
            }
        }

        return fv;
    }

    public Vector associatorNames(String version,
            CIMNameSpace nameSpace,
            CIMObjectPath objectName,
            String assocClass,
            String resultClass,
            String role,
            String resultRole,
            ServerSecurity ss) throws CIMException {
        try {
            LogFile.methodEntry("associatorNames");
            Vector rt = intassociatorNames(nameSpace, objectName, assocClass,
                    resultClass, role, resultRole, false);
            LogFile.methodReturn("associatorNames");
            return rt;
        } catch (CIMException e) {
            Debug.trace2("Got exception", e);
            throw e;
        } catch (Throwable e) {
            Debug.trace2("Got exception", e);
            throw new CIMException(CIMException.CIM_ERR_FAILED, e);
        }

    }

    Vector intreferences(CIMNameSpace inpnameSpace,
            CIMObjectPath objectName,
            String assocClass,
            String role,
            boolean biq,
            boolean bic,
            String propertyList[],
            boolean providerCall) throws CIMException {

        if (objectName == null || objectName.getObjectName() == null) {
            throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER);
        }

        if ((assocClass == null) && (propertyList != null)) {
            throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER);
        }

        boolean classOnly = objectName.getKeys() == null ||
                objectName.getKeys().size() == 0;

        CIMNameSpace ns = new CIMNameSpace();
        ns.setNameSpace('/' + inpnameSpace.getNameSpace());
        String nameSpace = getNameSpace(ns, objectName);

        CIMObjectPath path = new CIMObjectPath();
        path.setNameSpace(nameSpace);
        path.setObjectName(objectName.getObjectName());
        path.setKeys(objectName.getKeys());

        CIMObjectPath assocPath = new CIMObjectPath();
        assocPath.setNameSpace(nameSpace);
        assocPath.setObjectName(assocClass);

        LogFile.add(LogFile.DEVELOPMENT, "DEBUG_VALUE", "path", path);
        LogFile.add(LogFile.DEVELOPMENT, "DEBUG_VALUE", "assocPath", assocPath);

        Vector v = new Vector();
        Vector fv = new Vector();
        CIMInstance[] tArray = null;

        v = ps.reference(path, assocClass, role, biq, bic, propertyList);

        // v now contains all the associaton classes
        for (Enumeration e = v.elements(); e.hasMoreElements(); ) {

            // Get the association class. We expect the class to have all 
            // its qualifiers and
            // proeprties and class origins, since this may be required to 
            // find providers, etc.
            CIMClass cc = (CIMClass) (e.nextElement());
            // Get rid of the null
            e.nextElement();

            if (classOnly) {
                // Apply the appropriate filter here
                fv.addElement(cc.filterProperties(propertyList, biq,
                        bic));
                continue;
            }
            // XXX factory
            //CIMProvider ip = provCheck.checkInstanceProvider(nameSpace, cc);
            // check rights
            delayedCapabilityCheck(cc, providerCall, READ, nameSpace);
            CIMAssociatorProvider ap =
                    CIMOMImpl.getProviderFactory().getAssociatorProvider(
                            nameSpace, cc);

            CIMObjectPath tempOp = new CIMObjectPath();
            tempOp.setObjectName(cc.getName());
            tempOp.setNameSpace(nameSpace);
            LogFile.add(LogFile.DEVELOPMENT, "DEBUG_VALUE", "tempOp",
                    tempOp);

            if (ap != null) {
                try {
                    tArray = ap.references(tempOp, path, role, biq, bic,
                            propertyList);
                } catch (CIMException ex) {
                    if (ex.getID().equals(CIMException.CIM_ERR_NOT_FOUND)) {
                        // Ignore this one, the provider hasnt heard of
                        // this instance
                        tArray = null;
                    } else {
                        throw ex;
                    }
                }
            } else if (CIMOMImpl.getProviderFactory().getInstanceProvider(
                    nameSpace, cc) != null) {
                // Oh well, I'll just pretend there are no associators
                // XXX revisist this logic?
                continue;
            } else {
                try {
                    Vector tv = ps.reference(tempOp, path, role, biq, bic,
                            propertyList);
                    tArray = new CIMInstance[tv.size()];
                    tArray = (CIMInstance[]) tv.toArray(tArray);
                } catch (CIMException ex) {
                    if (ex.getID().equals(CIMException.CIM_ERR_NOT_FOUND)) {
                        // Ignore this one, the repository hasnt heard of
                        // this instance
                        tArray = null;
                    } else {
                        throw ex;
                    }
                }
            }

            if (tArray == null) {
                continue;
            }

            for (int j = 0; j < tArray.length; j++) {
                fv.addElement(tArray[j]);
            }
        }

        return fv;
    }

    public Vector references(String version,
            CIMNameSpace nameSpace,
            CIMObjectPath objectName,
            String resultClass,
            String role,
            Boolean includeQualifiers,
            Boolean includeClassOrigin,
            String propertyList[],
            ServerSecurity ss) throws CIMException {
        try {
            LogFile.methodEntry("references");
            Vector rt = intreferences(nameSpace, objectName, resultClass, role,
                    includeQualifiers.booleanValue(),
                    includeClassOrigin.booleanValue(),
                    propertyList, false);
            LogFile.methodReturn("references");
            return rt;
        } catch (CIMException e) {
            Debug.trace2("Got exception", e);
            throw e;
        } catch (Throwable e) {
            Debug.trace2("Got exception", e);
            throw new CIMException(CIMException.CIM_ERR_FAILED, e);
        }

    }

    Vector intreferenceNames(CIMNameSpace inpnameSpace,
            CIMObjectPath objectName,
            String assocClass,
            String role,
            boolean providerCall) throws CIMException {

        if (objectName == null || objectName.getObjectName() == null) {
            throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER);
        }

        boolean classOnly = objectName.getKeys() == null ||
                objectName.getKeys().size() == 0;

        CIMNameSpace ns = new CIMNameSpace();
        ns.setNameSpace('/' + inpnameSpace.getNameSpace());
        String nameSpace = getNameSpace(ns, objectName);

        CIMObjectPath path = new CIMObjectPath();
        path.setNameSpace(nameSpace);
        path.setObjectName(objectName.getObjectName());
        path.setKeys(objectName.getKeys());

        CIMObjectPath assocPath = new CIMObjectPath();
        assocPath.setNameSpace(nameSpace);
        assocPath.setObjectName(assocClass);

        LogFile.add(LogFile.DEVELOPMENT, "DEBUG_VALUE", "path", path);
        LogFile.add(LogFile.DEVELOPMENT, "DEBUG_VALUE", "assocPath", assocPath);

        Vector v = new Vector();
        Vector fv = new Vector();
        CIMObjectPath[] tv = null;

        v = ps.referenceNames(path, assocClass, role);
        for (Enumeration e = v.elements(); e.hasMoreElements(); ) {

            // Get the association class
            CIMClass cc = (CIMClass) e.nextElement();
            // Get rid of the null
            e.nextElement();
            if (classOnly) {
                CIMObjectPath rName = new CIMObjectPath();
                rName.setNameSpace(nameSpace);
                rName.setObjectName(cc.getName());
                fv.addElement(rName);
                continue;
            }
            // XXX factory
            // CIMProvider ip = provCheck.checkInstanceProvider(nameSpace, cc);
            // check rights
            delayedCapabilityCheck(cc, providerCall, READ, nameSpace);

            CIMAssociatorProvider ap =
                    CIMOMImpl.getProviderFactory().getAssociatorProvider(
                            nameSpace, cc);

            CIMObjectPath tempOp = new CIMObjectPath();
            tempOp.setObjectName(cc.getName());
            tempOp.setNameSpace(nameSpace);
            LogFile.add(LogFile.DEVELOPMENT, "DEBUG_VALUE", "tempOp",
                    tempOp);

            if (ap != null) {
                try {
                    tv = ap.referenceNames(tempOp, path, role);
                } catch (CIMException ex) {
                    if (ex.getID().equals(CIMException.CIM_ERR_NOT_FOUND)) {
                        // Ignore this one, the provider hasnt heard of
                        // this instance
                        tv = null;
                    } else {
                        throw ex;
                    }
                }
            } else if (CIMOMImpl.getProviderFactory().getInstanceProvider(
                    nameSpace, cc) != null) {
                // Oh well, I'll just pretend there are no associators
                // XXX revisit this logic?
                continue;
            } else {
                try {
                    Vector tmpV = ps.referenceNames(tempOp, path, role);
                    tv = new CIMObjectPath[tmpV.size()];
                    tmpV.toArray(tv);
                } catch (CIMException ex) {
                    if (ex.getID().equals(CIMException.CIM_ERR_NOT_FOUND)) {
                        // Ignore this one, the provider hasnt heard of
                        // this instance
                        tv = null;
                    } else {
                        throw ex;
                    }
                }
            }

            if (tv == null) {
                continue;
            }

            //Object[] objs = new Object[tv.size()];
            //tv.toArray(objs);
            for (int j = 0; j < tv.length; j++) {
                fv.addElement(tv[j]);
            }
        }

        return fv;
    }

    public Vector referenceNames(String version,
            CIMNameSpace nameSpace,
            CIMObjectPath objectName,
            String resultClass,
            String role,
            ServerSecurity ss) throws CIMException {
        try {
            Vector rt = intreferenceNames(nameSpace, objectName, resultClass,
                    role, false);
            return rt;
        } catch (CIMException e) {
            Debug.trace2("Got exception", e);
            throw e;
        } catch (Throwable e) {
            Debug.trace2("Got exception", e);
            throw new CIMException(CIMException.CIM_ERR_FAILED, e);
        }

    }

    // ci - the instance to set
    // oci - the existing instance
    // cc - the class that the instance belongs to
    private void repositorySetInstance(CIMNameSpace nameSpace,
            CIMObjectPath objectName, CIMInstance ci, CIMInstance oci, CIMClass cc,
            boolean includeQualifiers, String[] propertyList,
            boolean checkForProvider)
            throws CIMException {

        if (propertyList == null) {
            oci.updatePropertyValues(ci.getProperties());
        } else {
            for (int i = 0; i < propertyList.length; i++) {
                CIMProperty cp = ci.getProperty(propertyList[i]);
                if (cp == null) {
                    // ignore this
                    continue;
                }

                String originClass = cp.getOriginClass();

                if ((originClass == null) || (originClass.length() == 0)) {
                    oci.setProperty(cp.getName(), cp.getValue());
                } else {
                    oci.updatePropertyValue(cp);
                }
            }
        }

        checkInstanceSanity(getNameSpace(nameSpace, objectName), oci, cc);

        ps.setInstance(getNameSpace(nameSpace, objectName), oci);

        if (!checkForProvider) {
            // Ok no need for contacting the providers
            return;
        }

    }

    CIMClass intgetClass(CIMNameSpace nameSpace, CIMObjectPath objectName,
            boolean localOnly) throws CIMException {
        nameSpace.setNameSpace('/' + nameSpace.getNameSpace());
        try {
            CIMClass cc = ps.getClass(getNameSpace(nameSpace, objectName),
                    objectName.getObjectName().toLowerCase());

            if (cc == null) {
                return null;
            }
            if (localOnly) {
                return cu.getLocal(cc);
            }
            return cc;
        } catch (CIMException e) {
            Debug.trace2("Got exception", e);
            throw e;
        } catch (Throwable e) {
            Debug.trace2("Got exception", e);
            throw new CIMException(CIMException.CIM_ERR_FAILED, e);
        }
    }

    private CIMInstance intgetInstance(CIMNameSpace nameSpace,
            CIMObjectPath objectName,
            CIMClass cc,
            boolean localOnly,
            boolean includeQualifiers,
            boolean includeClassOrigin,
            String propertyList[],
            boolean providerCall)
            throws CIMException {

        LogFile.methodEntry("intgetInstance");
        nameSpace.setNameSpace('/' + nameSpace.getNameSpace());

        checkAbstractOrIndication(cc);

        CIMInstance nci = cc.newInstance();
        nci.updatePropertyValues(objectName.getKeys());

        // Copy the objectName
        CIMObjectPath tempOp = new CIMObjectPath();
        tempOp.setNameSpace(objectName.getNameSpace());
        tempOp.setObjectName(objectName.getObjectName());
        tempOp.setKeys(nci.getKeys());

        CIMInstance ci = null;
        boolean fromProvider = false;
        // XXX factory
	/*
	CIMProvider ip = 
	    provCheck.checkInstanceProvider(getNameSpace(nameSpace,
							 tempOp), cc);
	*/
        delayedCapabilityCheck(cc, providerCall, READ,
                getNameSpace(nameSpace, tempOp));
        CIMInstanceProvider ip =
                CIMOMImpl.getProviderFactory().getInstanceProvider(
                        getNameSpace(nameSpace, tempOp), cc);

        if (ip != null) {
            // there is an instance provider;
            tempOp.setObjectName(cc.getName());

            CIMObjectPath tempop = createObjectPath(nameSpace, tempOp);

            ci = ip.getInstance(tempop, localOnly, includeQualifiers,
                    includeClassOrigin, propertyList, cc);

            fromProvider = true;
        } else {
            ci = ps.getInstance(createObjectPath(
                    getNameSpace(nameSpace, tempOp), nci));
            if (ci != null) {
                ci = ci.filterProperties(propertyList, includeQualifiers,
                        includeClassOrigin);
            }
        }

        if (ci == null) {
            return null;
        }

        // Need to check if the keys match, in case the provider misbehaves
        nci.updatePropertyValues(ci.getProperties());

        // If instance is from the PS, then we do not need to check for
        // sanity.
        if (fromProvider) {
            checkInstanceSanity(getNameSpace(nameSpace, tempOp), nci, cc);
        }

        if (localOnly) {
            return cu.getLocal(nci);
        }

        return nci;
    }

    CIMInstance intgetInstance(CIMNameSpace nameSpace,
            CIMObjectPath objectName,
            boolean localOnly,
            boolean includeQualifiers,
            boolean includeClassOrigin,
            String propertyList[],
            boolean providerCall)
            throws CIMException {

        LogFile.methodEntry("intgetInstance");
        nameSpace.setNameSpace('/' + nameSpace.getNameSpace());

        CIMClass cc;
        cc = intgetClass(nameSpace, objectName, false);
        if (cc == null) {
            throw new CIMClassException(
                    CIMClassException.CIM_ERR_INVALID_CLASS,
                    objectName.getObjectName());
        }
        return intgetInstance(nameSpace, objectName, cc, localOnly,
                includeQualifiers, includeClassOrigin, propertyList, false);
    }

    private boolean isTrueQual(CIMQualifier cq) {
        if (cq == null) {
            return false;
        }
        CIMValue cv = cq.getValue();
        if (cv == null) {
            // We always have a value set, but we'll check just in case.
            // for boolean qualifiers, null means true;
            return true;
        }
        Boolean b = (Boolean) cv.getValue();
        if (b.equals(Boolean.TRUE)) {
            return true;
        } else {
            return false;
        }
    }

    // This method returns the result of the method invocation as a Vector
    // The Vector contains a CIMValue which is the return value of the method,
    // followed by CIMArguments, which are the output parameters.
    Vector intinvokeMethod(CIMNameSpace nameSpace,
            CIMObjectPath objectName,
            String methodName,
            Vector inParams,
            CIMArgument[] inArgs,
            boolean providerCall)
            throws CIMException {
        nameSpace.setNameSpace('/' + nameSpace.getNameSpace());
        try {
            if (objectName == null || objectName.getObjectName() == null) {
                throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER);
            }
            String ns = getNameSpace(nameSpace, objectName);
            CIMClass cc = intgetClass(nameSpace, objectName, false);
            if (cc == null) {
                throw new CIMException(CIMException.CIM_ERR_INVALID_CLASS,
                        objectName.getObjectName());
            }

            int trenner = methodName.indexOf(".");
            String ClassName = null;
            String MethodName = methodName.substring(trenner + 1,
                    methodName.length());
            String fullName = MethodName;
            if (trenner >= 0) {
                ClassName = methodName.substring(0, trenner);
                fullName = ClassName + "." + MethodName;
            }

            CIMMethod me = cc.getMethod(MethodName, ClassName);

            if (me == null) {
                throw new CIMMethodException(
                        CIMMethodException.NO_SUCH_METHOD,
                        fullName, cc.getName());
            }

            int numOutParams = 0;
            boolean needValues = false;
            if (inParams != null) {
                // populate the input CIM arguments. We assume the values are
                // passed in the same order of the method arguments
                needValues = true;
                // this means that we have to populate the CIMArgument 
                // array
                Iterator i = me.getParameters().iterator();
                Iterator iInParams = inParams.iterator();
                List cimArgList = new ArrayList();
                while (i.hasNext()) {
                    CIMParameter cp = (CIMParameter) i.next();
                    if (isTrueQual(cp.getQualifier(INPARAM))) {
                        // This is an input qualifier
                        Object value = null;
                        // If there are no more parameters we'll default
                        // the CIMArgument value to null.
                        if (iInParams.hasNext()) {
                            value = iInParams.next();
                        }
                        if ((value != null) && !(value instanceof CIMValue)) {
                            throw new
                                    CIMException(CIMException.CIM_ERR_INVALID_PARAMETER,
                                    "Not a CIM value", value);
                        }
                        CIMArgument ca = new CIMArgument(cp.getName());
                        // should we check the type here?
                        ca.setValue((CIMValue) value);
                        cimArgList.add(ca);
                    }

                    if (isTrueQual(cp.getQualifier(OUTPARAM))) {
                        numOutParams++;
                    }
                }

                inArgs = new CIMArgument[cimArgList.size()];
                inArgs = (CIMArgument[]) cimArgList.toArray(inArgs);
            } else {
                Iterator i = me.getParameters().iterator();
                while (i.hasNext()) {
                    CIMParameter cp = (CIMParameter) i.next();
                    if (isTrueQual(cp.getQualifier(OUTPARAM))) {
                        numOutParams++;
                    }
                }
            }

            // indicates if we have checked access rights
            boolean rightsChecked = false;

            // see if there is a provider for this method
            CIMMethodProvider mp =
                    CIMOMImpl.getProviderFactory().getMethodProvider(ns, me, cc);
            if (mp == null) {
                // Nope, see if there is a method provider for the class
                mp =
                        CIMOMImpl.getProviderFactory().getMethodProvider(ns, cc);
                if (mp == null) {
                    // No method provider
                    throw new CIMProviderException(
                            CIMProviderException.NO_METHOD_PROVIDER,
                            cc.getName(), me.getName());
                }
            } else {
                // checking rights
                rightsChecked = true;
                // check rights
                delayedCapabilityCheck(me, providerCall, WRITE, ns);
            }

            // check rights if we have to
            if (!rightsChecked) {
                // If we do this check then it means there is no method specific
                // provider so we are dealing with the class provider
                delayedCapabilityCheck(cc, providerCall, WRITE,
                        getNameSpace(nameSpace, objectName));
            }

            Vector outParams = new Vector();
            CIMArgument[] outParamArray = new CIMArgument[numOutParams];

            CIMObjectPath tempop = new CIMObjectPath();
            tempop.setObjectName(cc.getName());
            tempop.setNameSpace(ns);
            tempop.setKeys(objectName.getKeys());

            CIMValue cv = mp.invokeMethod(tempop, methodName, inArgs,
                    outParamArray);

            outParams.add(cv);
            for (int j = 0; j < outParamArray.length; j++) {
                if (needValues) {
                    if (outParamArray[j] == null) {
                        // We wont allow null arguments to be returned.
                        throw new CIMException(
                                CIMException.CIM_ERR_INVALID_PARAMETER,
                                "Null parameter being returned at index " + j);
                    }
                    outParams.add(outParamArray[j].getValue());
                } else {
                    outParams.add(outParamArray[j]);
                }
            }
            LogFile.methodReturn("invokeMethod");
            return outParams;
        } catch (CIMException e) {
            Debug.trace2("Got exception", e);
            throw e;
        } catch (Throwable e) {
            Debug.trace2("Got exception", e);
            throw new CIMException(CIMException.CIM_ERR_FAILED, e);
        }

    }

    public Vector invokeMethod(String version,
            CIMNameSpace nameSpace,
            CIMObjectPath objectName,
            String methodName,
            Vector inParams,
            ServerSecurity ss)
            throws CIMException {
        LogFile.methodEntry("invokeMethod");
        return intinvokeMethod(nameSpace, objectName,
                methodName, inParams, null, false);
    }

    public Vector invokeMethod(String version,
            CIMNameSpace nameSpace,
            CIMObjectPath objectName,
            String methodName,
            CIMArgument[] inArgs,
            ServerSecurity ss)
            throws CIMException {
        LogFile.methodEntry("invokeMethod");
        return intinvokeMethod(nameSpace, objectName,
                methodName, null, inArgs, false);
    }

    public CIMInstance getInstance(String version,
            CIMNameSpace nameSpace,
            CIMObjectPath objectName,
            boolean localOnly,
            ServerSecurity ss)
            throws CIMException {
        try {
            LogFile.methodEntry("getInstance");
            CIMInstance ci = intgetInstance(nameSpace, objectName,
                    localOnly, true, true, null, false);
            LogFile.methodReturn("getInstance");
            if (ci == null) {
                throw new CIMInstanceException(CIMException.CIM_ERR_NOT_FOUND,
                        objectName.toString());
            }
            return ci;
        } catch (CIMException e) {
            Debug.trace2("Got exception", e);
            throw e;
        } catch (Throwable e) {
            Debug.trace2("Got exception", e);
            throw new CIMException(CIMException.CIM_ERR_FAILED, e);
        }
    }

    CIMValue intgetProperty(CIMNameSpace nameSpace,
            CIMObjectPath objectName,
            String propertyName,
            boolean providerCall)
            throws CIMException {

        LogFile.methodEntry("intgetProperty");

        nameSpace.setNameSpace('/' + nameSpace.getNameSpace());
        CIMClass cc;
        cc = intgetClass(nameSpace, objectName, false);
        if (cc == null) {
            throw new CIMClassException(
                    CIMClassException.CIM_ERR_INVALID_CLASS,
                    objectName.getObjectName());
        }

        checkAbstractOrIndication(cc);

        CIMProperty propElem = cc.getProperty(propertyName);
        if (propElem == null) {
            // Cannot find this property
            throw new CIMPropertyException(CIMException.CIM_ERR_NOT_FOUND,
                    propertyName);
        }

        CIMInstance nci = cc.newInstance();
        nci.updatePropertyValues(objectName.getKeys());
        objectName.setKeys(nci.getKeys());
        objectName.setObjectName(cc.getName());

        CIMObjectPath tempop = createObjectPath(nameSpace, objectName);

        delayedCapabilityCheck(cc, providerCall, WRITE,
                getNameSpace(nameSpace, objectName));

        // No property provider, get the instance provider
        CIMInstance ci = null;
        CIMInstanceProvider ip =
                CIMOMImpl.getProviderFactory().getInstanceProvider(
                        getNameSpace(nameSpace, objectName), cc);
        if (ip != null) {
            //could optomize this to only get the prop we want
            ci = ip.getInstance(tempop, false, false, false, null, cc);
        } else {
            ci = ps.getInstance(createObjectPath(
                    getNameSpace(nameSpace, objectName), nci));
        }

        if (ci == null) {
            LogFile.methodReturn("intgetProperty");
            return null;
        }

        propElem = ci.getProperty(propertyName);
        if (propElem == null) {
            // Somehow the provider messed up!
            throw new CIMProviderException(CIMException.CIM_ERR_NOT_FOUND,
                    propertyName);
        }

        LogFile.methodReturn("intgetProperty");
        return propElem.getValue();
    }

    public CIMValue getProperty(String version,
            CIMNameSpace nameSpace,
            CIMObjectPath objectName,
            String propertyName,
            ServerSecurity ss)
            throws CIMException {
        try {
            LogFile.methodEntry("getProperty");
            LogFile.methodReturn("getProperty");
            return intgetProperty(nameSpace, objectName, propertyName, false);
        } catch (CIMException e) {
            Debug.trace2("Got exception", e);
            throw e;
        } catch (Throwable e) {
            Debug.trace2("Got exception", e);
            throw new CIMException(CIMException.CIM_ERR_FAILED, e);
        }
    }

    public CIMQualifierType getQualifierType(String version,
            CIMNameSpace nameSpace,
            CIMObjectPath objectName,
            ServerSecurity ss)
            throws CIMException {

        try {
            CIMQualifierType qt =
                    CIMQtypeStaticMethods.getQualifierType(nameSpace,
                            objectName);
            if (qt == null) {
                throw new CIMQualifierTypeException(
                        CIMException.CIM_ERR_NOT_FOUND,
                        objectName.getObjectName());
            }

            return qt;
        } catch (CIMException e) {
            Debug.trace2("Got exception", e);
            throw e;
        } catch (Throwable e) {
            Debug.trace2("Got exception", e);
            throw new CIMException(CIMException.CIM_ERR_FAILED, e);
        }
    }

    private String getNameSpace(CIMNameSpace ns, CIMObjectPath op) {
        CIMNameSpace cns = new CIMNameSpace();

        /** 6225658 - SNIA Client API not setting CLASSNAME **/
        if (ns != null) {
            StringBuffer s = new StringBuffer(ns.getNameSpace());
            s.append('/');
            if (op != null) {
                s.append(op.getNameSpace());
            }
            cns.setNameSpace(s.toString());
        }
        return cns.getNameSpace();
    }

    void intdeleteClass(CIMNameSpace ns, CIMObjectPath inpath)
            throws CIMException {
        ns.setNameSpace('/' + ns.getNameSpace());
        String nameSpace = getNameSpace(ns, inpath);
        CIMObjectPath path = new CIMObjectPath();
        path.setNameSpace(nameSpace);
        path.setObjectName(inpath.getObjectName());

        CIMClass cc = intgetClass(ns, inpath, false);
        String temp = path.getObjectName();
        if ((temp == null) || (temp.equalsIgnoreCase("top"))) {
            temp = "";
        }
        if ((cc == null) && (temp.length() != 0) &&
                (!temp.equalsIgnoreCase("top"))) {
            throw new CIMClassException(CIMClassException.CIM_ERR_NOT_FOUND,
                    path.getObjectName().toLowerCase());

        }

        Vector v1 = intenumClass(ns, inpath, false);
        if (v1.size() > 0) {
            throw new CIMClassException(
                    CIMException.CIM_ERR_CLASS_HAS_CHILDREN,
                    cc.getName());
        }

        Vector tmpV = ps.enumerateInstances(path, false);
        if (tmpV.size() > 0) {
            throw new CIMClassException(
                    CIMException.CIM_ERR_CLASS_HAS_INSTANCES,
                    cc.getName());
        }

        ps.deleteClass(path);
    }

    public void deleteClass(String version, CIMNameSpace ns,
            CIMObjectPath path,
            ServerSecurity ss)
            throws CIMException {

        try {
            LogFile.methodEntry("deleteClass");
            verifyCapabilities(ss, WRITE,
                    getNameSpace(ns, path));
            intdeleteClass(ns, path);
            LogFile.methodReturn("deleteClass");
        } catch (CIMException e) {
            Debug.trace2("Got exception", e);
            throw e;
        } catch (Throwable e) {
            Debug.trace2("Got exception", e);
            throw new CIMException(CIMException.CIM_ERR_FAILED, e);
        }
    }

    void intdeleteInstance(CIMNameSpace ns, CIMObjectPath inpath,
            boolean providerCall) throws CIMException {

        ns.setNameSpace('/' + ns.getNameSpace());
        String nameSpace = getNameSpace(ns, inpath);
        CIMObjectPath path = new CIMObjectPath();
        path.setNameSpace(nameSpace);
        path.setObjectName(inpath.getObjectName());
        path.setKeys(inpath.getKeys());

        CIMClass cc;
        cc = intgetClass(ns, inpath, false);
        if (cc == null) {
            throw new CIMClassException(
                    CIMClassException.CIM_ERR_NOT_FOUND, path.getObjectName());
        }

        checkAbstractOrIndication(cc);

        // XXX factory
        //CIMProvider ip = provCheck.checkInstanceProvider(nameSpace, cc);
        // Check rights
        delayedCapabilityCheck(cc, providerCall, WRITE, nameSpace);
        CIMInstanceProvider ip =
                CIMOMImpl.getProviderFactory().getInstanceProvider(
                        nameSpace, cc);

        if (ip == null) {
            path.setObjectName(cc.getName());
            ps.deleteInstance(path);
        } else {
            ip.deleteInstance(path);
        }
    }

    public void deleteInstance(String version, CIMNameSpace ns,
            CIMObjectPath path, ServerSecurity ss)
            throws CIMException {
        try {
            LogFile.methodEntry("deleteInstance");
            intdeleteInstance(ns, path, false);
            LogFile.methodReturn("deleteInstance");
        } catch (CIMException e) {
            Debug.trace2("Got exception", e);
            throw e;
        } catch (Throwable e) {
            Debug.trace2("Got exception", e);
            throw new CIMException(CIMException.CIM_ERR_FAILED, e);
        }
    }

    public void deleteQualifierType(String version, CIMNameSpace ns,
            CIMObjectPath path, ServerSecurity ss)
            throws CIMException {
        try {
            verifyCapabilities(ss, WRITE, getNameSpace(ns, path));
            checkMemory();
            LogFile.methodEntry("deleteQualifierType");
            CIMQtypeStaticMethods.removeCIMElement(ns, path);
            LogFile.methodReturn("deleteQualifiertype");
        } catch (CIMException e) {
            Debug.trace2("Got exception", e);
            throw e;
        } catch (Throwable e) {
            Debug.trace2("Got exception", e);
            throw new CIMException(CIMException.CIM_ERR_FAILED, e);
        }
    }

    public Vector enumNameSpace(String version, CIMNameSpace ns,
            CIMObjectPath path, boolean deep,
            ServerSecurity ss)
            throws CIMException {

        try {
            LogFile.methodEntry("enumNameSpace");
            verifyCapabilities(ss, READ,
                    getNameSpace(ns, path));
            Vector rt = CIMNSStaticMethods.enumNameSpace(ns, path, deep);
            LogFile.methodReturn("enumNameSpace");
            return rt;
        } catch (CIMException e) {
            Debug.trace2("Got exception", e);
            throw e;
        } catch (Throwable e) {
            Debug.trace2("Got exception", e);
            throw new CIMException(CIMException.CIM_ERR_FAILED, e);
        }

    }

    Vector intenumClass(CIMNameSpace ns, CIMObjectPath inpath,
            boolean deep) throws CIMException {

        ns.setNameSpace('/' + ns.getNameSpace());
        String nameSpace = getNameSpace(ns, inpath);
        CIMObjectPath path = new CIMObjectPath();
        path.setNameSpace(nameSpace);
        /** 6225658 - SNIA Client API not setting CLASSNAME **/
        if (inpath != null) {
            path.setObjectName(inpath.getObjectName());
        }
        return ps.enumerateClasses(path, deep);
    }

    Vector intenumClass(CIMNameSpace ns, CIMObjectPath inpath,
            boolean deep, boolean localOnly) throws CIMException {

        ns.setNameSpace('/' + ns.getNameSpace());
        String nameSpace = getNameSpace(ns, inpath);
        CIMObjectPath path = new CIMObjectPath();
        path.setNameSpace(nameSpace);
        /** 6225658 - SNIA Client API not setting CLASSNAME **/
        if (inpath != null) {
            path.setObjectName(inpath.getObjectName());
        }
        return ps.enumerateClasses(path, deep, localOnly);
    }

    Vector intenumInstances(CIMNameSpace ns, CIMObjectPath inpath,
            boolean deep, boolean providerCall)
            throws CIMException {
        ns.setNameSpace('/' + ns.getNameSpace());
        String nameSpace = getNameSpace(ns, inpath);
        CIMObjectPath path = new CIMObjectPath();
        path.setNameSpace(nameSpace);
        path.setObjectName(inpath.getObjectName());
        // we need these keys, so that client apps can have a back door scoping
        // set up here if they want.
        path.setKeys(inpath.getKeys());

        Vector v = new Vector();
        Vector fv = new Vector();
        CIMObjectPath[] tv;

        if (deep) {
            v = intenumClass(ns, inpath, deep);
        }
        String on = path.getObjectName();
        if ((on != null) && (on.length() != 0) &&
                (!on.equals(PSRlogImpl.TOP))) {
            v.insertElementAt(path, 0);
        }
        CIMObjectPath[] ops = new CIMObjectPath[v.size()];
        v.toArray(ops);
        for (int i = 0; i < ops.length; i++) {
            CIMObjectPath op = ops[i];
            // back door
            op.setKeys(path.getKeys());
            CIMClass cc;
            LogFile.add(LogFile.DEVELOPMENT, "DEBUG_VALUE", "op",
                    op.getObjectName());
            String tns = ns.getNameSpace();
            ns.setNameSpace("");
            cc = intgetClass(ns, op, false);
            ns.setNameSpace(tns);
            if (cc == null) {
                throw new CIMClassException(
                        CIMClassException.CIM_ERR_NOT_FOUND,
                        op.getObjectName());
            }
            checkIndication(cc);
            // XXX factory
            //CIMProvider ip = provCheck.checkInstanceProvider(nameSpace, cc);
            // We should actually call verifyCapability for the persistent
            // store case and cache the result.
            delayedCapabilityCheck(cc, providerCall, READ, nameSpace);
            CIMInstanceProvider ip =
                    CIMOMImpl.getProviderFactory().getInstanceProvider(
                            nameSpace, cc);
            if (ip != null) {
                // There is an instance provider, invoke it
                op.setObjectName(cc.getName());
                tv = ip.enumerateInstanceNames(op, cc);
            } else {
                // There is no instance provider
                Vector tmpV = ps.enumerateInstances(op, false);
                tv = new CIMObjectPath[tmpV.size()];
                tmpV.toArray(tv);
            }

            if (tv == null) {
                continue;
            }

            // @@@ what kind object is in tv?
            //Object objs[] = new Object[tv.size()];
            //tv.toArray(objs);
            for (int j = 0; j < tv.length; j++) {
                fv.addElement(tv[j]);
            }
        }
        return fv;
    }

    public Vector enumInstances(String version, CIMNameSpace ns,
            CIMObjectPath path,
            boolean deep,
            ServerSecurity ss) throws CIMException {
        try {
            Boolean bln = (deep ? Boolean.TRUE : Boolean.FALSE);
            return intenumInstances(ns, path, deep, false);
        } catch (CIMException e) {
            Debug.trace2("Got exception", e);
            throw e;
        } catch (Throwable e) {
            Debug.trace2("Got exception", e);
            throw new CIMException(CIMException.CIM_ERR_FAILED, e);
        }
    }

    Vector intenumInstances(CIMNameSpace ns, CIMObjectPath inpath,
            boolean deep, boolean localOnly, boolean providerCall)
            throws CIMException {

        ns.setNameSpace('/' + ns.getNameSpace());
        String nameSpace = getNameSpace(ns, inpath);
        CIMObjectPath path = new CIMObjectPath();
        path.setNameSpace(nameSpace);
        path.setObjectName(inpath.getObjectName());
        // we need these keys, so that client apps can have a back door scoping
        // set up here if they want.
        path.setKeys(inpath.getKeys());

        Vector v = new Vector();
        Vector fv = new Vector();
        CIMInstance[] tv = null;

        if (deep) {
            v = intenumClass(ns, inpath, deep);
        }
        String on = path.getObjectName();
        if ((on != null) && (on.length() != 0) &&
                (!on.equals(PSRlogImpl.TOP))) {
            v.insertElementAt(path, 0);
        }
        CIMObjectPath[] ops = new CIMObjectPath[v.size()];
        v.toArray(ops);
        for (int i = 0; i < ops.length; i++) {
            CIMObjectPath op = ops[i];
            // back door
            op.setKeys(path.getKeys());
            CIMClass cc;
            LogFile.add(LogFile.DEVELOPMENT, "DEBUG_VALUE", "op",
                    op.getObjectName());
            String tns = ns.getNameSpace();
            ns.setNameSpace("");
            cc = intgetClass(ns, op, false);
            ns.setNameSpace(tns);
            if (cc == null) {
                throw new CIMClassException(CIMClassException.CIM_ERR_NOT_FOUND,
                        op.getObjectName());
            }
            checkIndication(cc);
            // XXX factory
            //CIMProvider ip = provCheck.checkInstanceProvider(nameSpace, cc);
            // check rights
            delayedCapabilityCheck(cc, providerCall, READ, nameSpace);

            CIMInstanceProvider ip =
                    CIMOMImpl.getProviderFactory().getInstanceProvider(
                            nameSpace, cc);

            if (ip != null) {
                // There is an instance provider, invoke it
                op.setObjectName(cc.getName());
                tv = ip.enumerateInstances(op, localOnly,
                        true /*includeQualifiers */,
                        true /*includeClassOrigin*/,
                        null /*propertyList */,
                        cc);
            } else {
                // There is no instance provider
                Vector tmpV = ps.enumerateInstances(op, false, localOnly);
                tv = new CIMInstance[tmpV.size()];
                tmpV.toArray(tv);
            }

            if (tv == null) {
                continue;
            }

            //Object[] objs = new Object[tv.size()];
            //tv.toArray(objs);
            for (int j = 0; j < tv.length; j++) {
                fv.addElement(tv[j]);
            }
        }
        return fv;
    }

    public Vector enumInstances(String version, CIMNameSpace ns,
            CIMObjectPath path,
            boolean deep,
            boolean localOnly,
            ServerSecurity ss) throws CIMException {
        try {
            Boolean bln = (deep ? Boolean.TRUE : Boolean.FALSE);
            return intenumInstances(ns, path, deep, localOnly, false);
        } catch (CIMException e) {
            Debug.trace2("Got exception", e);
            throw e;
        } catch (Throwable e) {
            Debug.trace2("Got exception", e);
            throw new CIMException(CIMException.CIM_ERR_FAILED, e);
        }
    }

    public Vector enumQualifierTypes(String version, CIMNameSpace ns,
            CIMObjectPath path, ServerSecurity ss)
            throws CIMException {
        try {
            LogFile.methodEntry("enumQualifierTypes");
            verifyCapabilities(ss, READ,
                    getNameSpace(ns, path));
            Vector rt =
                    CIMQtypeStaticMethods.enumQualifierTypes(ns, path);
            LogFile.methodReturn("enumQualifierTypes");
            return rt;
        } catch (CIMException e) {
            Debug.trace2("Got exception", e);
            throw e;
        } catch (Throwable e) {
            Debug.trace2("Got exception", e);
            throw new CIMException(CIMException.CIM_ERR_FAILED, e);
        }
    }

    // Makes sure that none of the subclasses have semantic errors because
    // of the modifications to the parent class. 
    // We dont have to check the keys here because,
    // the key of the parent class should not have changed. The class to be
    // checked (cc) should only have the elements defined locally in it. This
    // is because when a class is added it only has local elements and semantic
    // checks are based on that.
    private void checkSubClassModification(CIMNameSpace ns, CIMObjectPath op,
            CIMClass cc, CIMClass parentClass, List modifiedClassList)
            throws CIMException {
        CIMObjectPath lop = new CIMObjectPath();
        lop.setNameSpace(op.getNameSpace());
        lop.setObjectName(cc.getName());
        // check the sanity of the class, with the modified parent class
        classCheck.checkClassSanity(getNameSpace(ns, lop), cc, parentClass);
        // Now check sanity for each of the subclasses.
        Vector v = intenumClass(ns, lop, false, false);
        Enumeration subClasses = v.elements();
        modifiedClassList.add(cc);
        while (subClasses.hasMoreElements()) {
            CIMClass scc = (CIMClass) subClasses.nextElement();
            // pass in the class to be checked, and the list, so that once
            // the class after semantic checking, can be added into the list.
            checkSubClassModification(ns, lop, scc.localElements(), cc,
                    modifiedClassList);
        }
    }

    // Taking the modified class and the old class, determine what properties
    // can be 'migrated'. Find those properties from the new class that exist
    // in the old, and have same types. A list of properties which must be
    // migrated are returned. Each name takes two list entries, the originclass,
    // and the actual name.
    List determineUpdateProps(CIMClass newcc, CIMClass oldcc) {
        Map propMap = new HashMap();
        Enumeration e = oldcc.getProperties().elements();
        // Place the old properties in a map for easy lookup.
        while (e.hasMoreElements()) {
            CIMProperty cp = (CIMProperty) e.nextElement();
            String key = cp.getOriginClass().toLowerCase() + ":" + cp.getName();
            propMap.put(key, cp);
        }
        e = newcc.getProperties().elements();
        List updateList = new ArrayList();
        while (e.hasMoreElements()) {
            CIMProperty cp = (CIMProperty) e.nextElement();
            String key = cp.getOriginClass().toLowerCase() + ":" + cp.getName();
            CIMProperty oldcp = (CIMProperty) propMap.get(key);
            if (oldcp == null) {
                // this property doesnt exist in the old class, so we
                // dont need to migrate the property.
                continue;
            }
            if (!oldcp.getType().equals(cp.getType())) {
                // this property doesnt match in the old class, so we
                // dont need to migrate the property.
                // XXX We may want to do type conversion for numeric types,
                // but we wont for now.
                continue;
            }
            updateList.add(cp.getOriginClass());
            updateList.add(cp.getName());
        }
        return updateList;
    }

    // This is a variation on the CIMInstance updatePropertyValues. The 
    // difference here is that we only update some of the properties, which are
    // specified in the updateList. The reason we use it in modify is that
    // the newInsatnce may have new properties that dont exist in the old one,
    // or have different types. Likewise, some may be missing.  We pass in the
    // updateList which has been determined by the determineUpdateProp method.
    private void updateModPropertyValues(CIMInstance newci, CIMInstance oldci,
            List updateList) {
        Iterator i = updateList.iterator();
        while (i.hasNext()) {
            String originClass = (String) i.next();
            String pName = (String) i.next();
            CIMProperty newcp = newci.getProperty(pName, originClass);
            newcp.setValue(oldci.getProperty(pName, originClass).getValue());
        }
    }

    // This will do the actual modification of the classes in the 
    // modifiedClassList, as well as update the instances.
    private void updateInstances(CIMNameSpace ns, CIMObjectPath op,
            List modifiedClassList) throws CIMException {
        // Going in reverse order, because I'd rather leave a subclass in an
        // inconsistent state. Could've used transactions here.
        CIMObjectPath lop = new CIMObjectPath();
        lop.setNameSpace(getNameSpace(ns, op));
        for (int i = modifiedClassList.size() - 1; i >= 0; i--) {
            CIMClass cc = (CIMClass) modifiedClassList.get(i);
            // Set the class name to input class name.
            lop.setObjectName(cc.getName());
            CIMClass oldcc = intgetClass(new CIMNameSpace("", ""), lop, false);
            // find which properties can be migrated
            List propsToUpdate = determineUpdateProps(cc, oldcc);
            // update the class
            ps.setClass(lop.getNameSpace(), cc);
            // We will directly go the the repository here, because we're only
            // interested in the instances of the repository
            Enumeration instEnum = ps.enumerateInstances(lop, false).elements();
            // update each instance
            while (instEnum.hasMoreElements()) {
                // Create a default instance
                CIMInstance newci = cc.newInstance();
                CIMObjectPath oldip = (CIMObjectPath) instEnum.nextElement();
                CIMInstance oldci = ps.getInstance(oldip);
                updateModPropertyValues(newci, oldci, propsToUpdate);
                ps.setInstance(lop.getNameSpace(), newci);
            }
        }
    }

    /*
     * This method implements the semantics of a modify class operation as
     * specified in the CIM Operations over HTTP spec. The CIMOM currently will
     * not consider a class modification which involves changing the keys as
     * being a consistent modification. This is because modification of keys
     * results in classes which are weakly associated to the class to become
     * invalid, the instances will most likely become invalid and associations
     * instances referring to the modified class instances will also become 
     * invalid.
     * The basic logic is as follows:
     * Check if the modified class passes semantic checks.
     * Make sure keys are not being changed.
     * Recursively go through each subclass, making sure that they pass the
     * semantic checks with the newly modified superclass.
     * Go through all repository instances for the updated classes and update
     * the instances.
     */
    void intModifyClass(CIMNameSpace nameSpace, CIMObjectPath objectName,
            CIMClass cc)
            throws CIMException {
        // Make local copies of namespace, objectName, so we can modify them.
        CIMNameSpace lns = new CIMNameSpace();
        // Make this an absolute path
        lns.setNameSpace("//" + nameSpace.getNameSpace());
        CIMObjectPath lop = new CIMObjectPath();
        // Set the class name to input class name.
        lop.setObjectName(cc.getName());
        lop.setNameSpace(objectName.getNameSpace());
        // Get the current class from the repository
        CIMClass icc = intgetClass(lns, lop, false);
        if (icc == null) {
            // The class to be modified does not exist, throw an exception
            throw new CIMClassException(
                    CIMException.CIM_ERR_NOT_FOUND, cc.getName());
        }
        // Store the current keys to verify that they did not change
        Vector oldKeys = icc.getKeys();
        // check the sanity of the modified class
        classCheck.checkClassSanity(getNameSpace(lns, lop), cc, null);
        // Check the keys here.
        classCheck.compareKeys(oldKeys, cc.getKeys());
        // recursively check the sanity of subclasses. If we had transactions,
        // we wouldnt have to do this, just update the classes and if any fail,
        // rollback.
        Vector v = intenumClass(lns, lop, false, false);
        Enumeration subClasses = v.elements();
        List modifiedClassList = new ArrayList();
        modifiedClassList.add(cc);
        while (subClasses.hasMoreElements()) {
            CIMClass scc = (CIMClass) subClasses.nextElement();
            // pass in the class to be checked, and the list, so that once
            // the class has passed semantic checking, 
            // it can be added into the list.
            checkSubClassModification(lns, lop, scc.localElements(), cc,
                    modifiedClassList);
        }
        // Ok all the semantic checks passed, modifiedClassList has the
        // appropriately modifed classes. Now update the classes and their 
        // instances.
        updateInstances(lns, lop, modifiedClassList);
    }

    // This is a variation of deleteClass that is used with reg and unreg
    // and should probably be used with the latest JSR. It checks if it
    // has any associated classes or subclasses. If so, the deletion cannot be 
    // performed. If not, the class is deleted along with its static instances.
    void intMofregRemoveClass(CIMNameSpace ns, CIMObjectPath inpath)
            throws CIMException {
        CIMNameSpace tns = new CIMNameSpace();
        tns.setNameSpace('/' + ns.getNameSpace());
        tns.setHost(ns.getHost());
        String nameSpace = getNameSpace(ns, inpath);
        CIMObjectPath path = new CIMObjectPath();
        path.setNameSpace(nameSpace);
        path.setObjectName(inpath.getObjectName());

        CIMClass cc = intgetClass(tns, inpath, false);
        String temp = path.getObjectName();
        if ((temp == null) || (temp.equalsIgnoreCase("top"))) {
            temp = "";
        }
        if ((cc == null) && (temp.length() != 0) &&
                (!temp.equalsIgnoreCase("top"))) {
            throw new CIMClassException(CIMClassException.CIM_ERR_NOT_FOUND,
                    path.getObjectName().toLowerCase());

        }

        Vector v1 = intenumClass(ns, inpath, false);
        if (v1.size() > 0) {
            throw new CIMClassException(
                    CIMException.CIM_ERR_CLASS_HAS_CHILDREN,
                    cc.getName());
        }
	/*
	   This check may make things too restrictive - we wouldnt have
	   had an issue if the repository supported transactions.

	// Check if this class has associators.
	v1 = (Vector)intassociators(new CIMNameSpace("",""), path,
				null, null, null, null, false, false, null,
				false).elementAt(0);

	// XXX We need a new CLASS_HAS_ASSOCIATORS exception here.
	if (v1.size() > 0) {
	    System.out.println("Class has associators");
	    throw new CIMClassException(
	    CIMException.CIM_ERR_CLASS_HAS_CHILDREN,
					cc.getName());
	}
	*/

        // Retreive the instances from the repository.
        Enumeration instEnum = ps.enumerateInstances(path, false).elements();
        // remove each instance
        while (instEnum.hasMoreElements()) {
            CIMObjectPath ip = (CIMObjectPath) instEnum.nextElement();
            ps.deleteInstance(ip);
        }
	/* XXX factory
	if (inpath.getObjectName().equalsIgnoreCase("Solaris_LogRecord")) {
	    // We are removing the log service.
	    serviceClass.remove("Solaris_LogRecord");
	    serviceProvider.remove("Solaris_LogRecord");
	}
	*/
        ps.deleteClass(path);

    }

    // Initialize debug tracing if enabled through system properties
    private void initTrace() {

        // Get debug level and device; pass to trace open.
        // We set the trace file base name for the server side.
        String level = System.getProperty("wbem.debug.level");
        String device = System.getProperty("wbem.debug.device");
        if ((device != null) && (device.equalsIgnoreCase("file"))) {
            device = "wbem_server";
        }
        Debug.traceOpen(level, device);
        Debug.trace1("Starting CIMOM server...");
    }

    // By now, persisistent store and CIMOMUtils are available
    private void initializeServices(Properties props) throws Exception {
        // XXX Do we need to introduce a new BASEDIR property or wbemroot
        // property
        String libdir = System.getProperty("propdir", "/usr/sadm/lib/wbem");
        String basedir = libdir + File.separatorChar + ".." +
                File.separatorChar + ".." + File.separatorChar + ".." +
                File.separatorChar + "..";
        mofreg = Mofregistry.getMofregistry(this,
                basedir + File.separatorChar + "var" + File.separatorChar + "sadm" +
                        File.separatorChar + "wbem" + File.separatorChar + "logr",
                "regDir", "unregDir", "failDir", "preReg", "preUnreg");
        // This HAS to be done ... because, all our internal methods
        // we invoke to setup initial services also use logging ...
        LogFile.initialize(new ProviderClient(this));
        initLogService();

        // Lets log the startup
        StringBuffer sb = new StringBuffer();
        sb.append(Version.major);
        sb.append(".");
        sb.append(Version.minor);
        sb.append(".");
        sb.append(Version.revision);
        String versionString = sb.toString();
        String[] logArgs = { versionString, Version.buildID };
        logMessage("STARTMSG", "STARTMSGDETAIL", logArgs, null,
                false, CIMOMLogService.SYSTEM_LOG, CIMOMLogService.INFO);
        Debug.trace1("Starting CIMOM " + versionString);

        upp = ServerSecurity.getUserPasswordProvider();
        // XXX Event service!!
        eventService = new EventService(ps, cu, this,
                new ProviderClient(this), props);
        ProviderAdapterFactory factory = CIMOMImpl.getProviderFactory();
        ClientProtocolAdapterService cpaService =
                new ClientProtocolAdapterService(new ProviderClient(this), this);
        // Registering the internal provider
        InternalProviderAdapter ppa = new InternalProviderAdapter();
        ppa.serviceProviderArray =
                new InternalProviderAdapter.InternalServiceProvider[]
                        { new CIMOMProviderHolder(), factory, eventService, cpaService };
        ppa.cimom = this;
        factory.RegisterProtocolProvider("internal", ppa, true);

        // perform MOF registry. 
        try {
            mofreg.mofReg();
        } catch (Exception e) {
            // Ignore mofreg failures, we may need to log this.
            Debug.trace2("Mofreg failure", e);
            mofreg.logException(e);
        }
        cpaService.startAdapters();
    }

    // Initialize the log service. In the future, we'll read the
    // required info from a MOF file, and the service registers
    // itself with the service registry. But for now we do it here.
    private void initLogService() {
        try {
            Class c = Class.forName("org.wbemservices.wbem.utility.log.CIMOMLogUtil");
            Class[] paramTypes = { javax.wbem.client.CIMOMHandle.class };
            Object[] paramList = { new ProviderClient(this) };
            Method initMethod = c.getMethod("getInstance", paramTypes);
            CIMOMLogService ls =
                    (CIMOMLogService) initMethod.invoke(null, paramList);
            ServiceRegistry.addService(CIMOMLogService.DEFAULT, ls);
        } catch (Throwable e) {
            Debug.trace1("Log service init failed", e);
        }
    }

    public static void main(String args[]) {

        try {
            CIMOMImpl cimom = new CIMOMImpl(args);

            //XXX: Shouldn't be here ... will log in init	
            String argsString = "";
            if (args.length > 0) {
                argsString = args[0];
            }
            for (int i = 1; i < args.length; i++) {
                argsString = argsString.concat(" " + args[i]);
            }
            LogFile.add(LogFile.INFORMATIONAL, "COMMAND_LINE_STR", argsString);
        } catch (Exception e) {
            Debug.trace2("Got exception", e);
            System.out.println(e);
            System.exit(1);
        } catch (Error e) {
            Debug.trace2("Got exception", e);
            System.out.println(e);
            System.exit(1);
        }
    }

    private void parseCommandLine(String args[]) {
        int i = 0;
        while (i < args.length) {
            if (args[i].equalsIgnoreCase("-help")) {
                System.out.println(I18N.loadString("COMMAND_LINE_ARGS"));
                System.exit(0);
            }
            // NOTE: I did not make the second char on this 
            //      argument programatic on purpose - There are 
            //      going to be some changes to the logging class!
            if (args[i].equalsIgnoreCase("-l0")) {
                LogFile.setLevel(LogFile.CRITICAL);
                LogFile.start();
            }
            if (args[i].equalsIgnoreCase("-l1")) {
                LogFile.setLevel(LogFile.WARNING);
                LogFile.start();
            }
            if (args[i].equalsIgnoreCase("-l2")) {
                LogFile.setLevel(LogFile.INFORMATIONAL);
                LogFile.start();
            }
            if (args[i].equalsIgnoreCase("-l3")) {
                LogFile.setLevel(LogFile.DEBUG);
                LogFile.start();
            }
            // This is an unpublished parameter
            if (args[i].equalsIgnoreCase("-lDev")) {
                LogFile.setLevel(LogFile.DEVELOPMENT);
                verbose = true;
                PSRlogImpl.verbose = true;
                LogFile.start();
            }
            if (args[i].equalsIgnoreCase("-s")) {
                i++;
                if (i >= args.length) {
                    System.out.println(I18N.loadString("INVALID_ARG"));
                    System.out.println(I18N.loadString("COMMAND_LINE_ARGS"));
                    throw new IllegalArgumentException(
                            I18N.loadString("INVALID_ARG") + "\n" +
                                    I18N.loadString("COMMAND_LINE_ARGS"));
                }
                dbHost = args[i];
            }
            if (args[i].equalsIgnoreCase("-version")) {
                System.out.println(CIMOMVersion.getProductName() + " " +
                        CIMOMVersion.getVersion()
                        + " (" + CIMOMVersion.getBuildDate() + ")");
                System.exit(0);
            }
            i++;
        }
    }

    /* 
     * The CIMOM itself is an instance provider. So we implement the instance
     * provider interfaces. The advantage of this is that we can model internal
     * services such as events in CIM and get the CIMOM itself to handle
     * service requests, without additional logic.
     * Currently all instance details are stored in the persistent store
     * itself, so all enumerations, gets, etc can be handled via the persistent
     * store. The only extra handling would be when we set and delete.
     */

    /*
     * initialize();
     */
    public void initialize(CIMOMHandle ch) throws CIMException {
        // Do nothing, we will never be initialized!
    }

    public void cleanup() throws CIMException {
        // Do nothing, we will never be cleaned up!
    }

    /**
     * InstanceProvider.enumerateInstanceNames()
     */
    public CIMObjectPath[] enumerateInstanceNames(CIMObjectPath op,
            CIMClass cc)
            throws CIMException {
        Vector v = ps.enumerateInstances(op, false);
        CIMObjectPath[] copArray = new CIMObjectPath[v.size()];
        v.toArray(copArray);
        return copArray;

    }

    /**
     * InstanceProvider.enumerateInstances
     */
    public CIMInstance[] enumerateInstances(CIMObjectPath op,
            boolean localOnly,
            boolean includeQualifiers,
            boolean includeClassOrigin,
            String[] propList,
            CIMClass cc)
            throws CIMException {
        Vector v = ps.enumerateInstances(op, false, localOnly);
        //XXX: Should walk through the vector and filter each element
        int j = v.size();
        CIMInstance[] ciArray = new CIMInstance[j];
        for (int i = 0; i < j; i++) {
            CIMInstance ci = (CIMInstance) v.elementAt(i);
            if (localOnly) {
                ci = ci.localElements();
            }
            ci = ci.filterProperties(propList, includeQualifiers,
                    includeClassOrigin);
            ciArray[i] = ci;
        }
        return ciArray;
    }

    /**
     * InstanceProvider.getInstance
     */
    public CIMInstance getInstance(CIMObjectPath op,
            boolean localOnly,
            boolean includeQualifiers,
            boolean includeClassOrigin,
            String[] propList,
            CIMClass cc)
            throws CIMException {
        boolean solaris_useracl =
                op.getObjectName().equalsIgnoreCase("solaris_useracl");
        boolean solaris_namespaceacl =
                op.getObjectName().equalsIgnoreCase("solaris_namespaceacl");
        if (solaris_useracl || solaris_namespaceacl) {
            Enumeration e = op.getKeys().elements();
            while (e.hasMoreElements()) {
                CIMProperty cp = (CIMProperty) e.nextElement();
                if (cp.getName().equalsIgnoreCase("nspace")) {
                    // Normalize the value and strip off the leading slash
                    String namespace = (String) cp.getValue().getValue();
                    CIMNameSpace tns = new CIMNameSpace("", '/' + namespace);
                    namespace = tns.getNameSpace();
                    namespace = namespace.substring(1, namespace.length());
                    cp.setValue(new CIMValue(namespace));
                }
            }
        }
        CIMInstance ci = ps.getInstance(op);
        if (ci == null) {
            throw new CIMException(CIMException.CIM_ERR_NOT_FOUND, op);
        }
        if (localOnly) {
            return cu.getLocal(ci);
        }
        return ci.filterProperties(propList,
                includeQualifiers, includeClassOrigin);
    }

    private void aclCheck(CIMInstance ci) throws CIMException {
        // The solaris_user ACL check should be changed later to an 
        // internal provider. Bug fix 4263148
        boolean solaris_useracl =
                ci.getClassName().equalsIgnoreCase("solaris_useracl");
        boolean solaris_namespaceacl =
                ci.getClassName().equalsIgnoreCase("solaris_namespaceacl");
        if (solaris_useracl) {
            String username =
                    (String) ci.getProperty("username").getValue().getValue();
            // If the passwd is null, the user does not exist
            if (upp.getEncryptedPassword(username,
                    UserPasswordProvider.ANY_USER_TYPE) == null) {
                throw new CIMSecurityException(
                        CIMSecurityException.NO_SUCH_PRINCIPAL, username);
            }
        }
        // End bug fix 4263148

        // Bug fix 4263200
        if (solaris_useracl || solaris_namespaceacl) {
            String tnamespace =
                    (String) ci.getProperty("nSpace").getValue().getValue();
            CIMNameSpace cns = new CIMNameSpace("", '/' + tnamespace);
            tnamespace = cns.getNameSpace();
            tnamespace = tnamespace.substring(1, tnamespace.length());
            if (tnamespace.equals(SECURITYNS)) {
                CIMProperty capProp = ci.getProperty("capability");
                String cap = null;
                if (capProp != null) {
                    CIMValue cv = capProp.getValue();
                    if (cv != null) {
                        cap = (String) cv.getValue();
                    }
                }

                if (cap == null) {
                    throw new CIMException
                            (CIMException.CIM_ERR_INVALID_PARAMETER,
                                    tnamespace + ":" + cap);
                }

                if (!cap.equals("r") && !cap.equals("rw")) {
                    throw new CIMException
                            (CIMException.CIM_ERR_INVALID_PARAMETER,
                                    tnamespace + ":" + cap);
                }
            }
            ci.setProperty("nSpace", new CIMValue(tnamespace));
        }
        // End of Bug fix 4263200
    }

    public CIMValue invokeMethod(CIMObjectPath op, String methodName,
            CIMArgument[] inArgs, CIMArgument[] outArgs) throws CIMException {
        // The only methods supported right now are shutdown, registerMOF. 
        if (op.getNameSpace().equalsIgnoreCase(SYSTEMNS) &&
                op.getObjectName().equalsIgnoreCase("Solaris_CIMOM")) {

            if (methodName.equalsIgnoreCase("registerMOF")) {
                Debug.trace2("registerMOF method invoked");
                try {
                    mofreg.mofReg();
                } catch (CIMException ce) {
                    Debug.trace1("registerMOF exception", ce);
                    throw ce;
                } catch (Exception e) {
                    Debug.trace1("registerMOF exception", e);
                    throw new CIMException(CIMException.CIM_ERR_FAILED,
                            e.toString());
                }
                Debug.trace2("registerMOF method completed");
                return new CIMValue(new Byte((byte) 0));
            }

            // It must be the shutdown method.

            // We spawn a new thread saw that the handler can return to 
            // the client.
            new Thread() {
                public void run() {
                    try {
                        // We need to get exclusive access of the CIMOM so
                        // that other critical threads like snapshot
                        // can complete if they are running.
                        // We should get it immediately most times, since
                        // the invokeMethod would only be called if
                        // there was a writeLock

                        concurrentObj.writeLock();
                        // Give time for response to get back to the client
                        Thread.sleep(2000);
                        System.exit(0);
                    } catch (Exception e) {
                        System.out.println(e);
                    } catch (Error e) {
                        System.out.println(e);
                    } finally {
                        // Exit anyway
                        System.exit(1);
                    }
                }
            }.start();
            return new CIMValue(new Byte((byte) 0));

        } else {
            throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
        }
    }

    public CIMObjectPath createInstance(CIMObjectPath op, CIMInstance ci)
            throws CIMException {

        if (op.getNameSpace().equalsIgnoreCase(SYSTEMNS) &&
                ci.getClassName().equalsIgnoreCase("Solaris_CIMOM")) {
            throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
        }
        aclCheck(ci);
        ps.addCIMElement(op.getNameSpace(), ci);
        return null;
    }

    public void setInstance(CIMObjectPath op, CIMInstance ci,
            boolean includeQualifier, String[] propertyList)
            throws CIMException {

        if (op.getNameSpace().equalsIgnoreCase(SYSTEMNS) &&
                ci.getClassName().equalsIgnoreCase("Solaris_CIMOM")) {
            throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
        }
        aclCheck(ci);

        if ((propertyList != null) || (includeQualifier != true)) {
            intsetCIMElement(new CIMNameSpace("", ""), op, ci, true,
                    includeQualifier, propertyList, false);
        } else {
            // We want to maintain exactly the old behavior to avoid
            // compatibility issues with existing providers.
            ps.setInstance(op.getNameSpace(), ci);
        }
    }

    public void deleteInstance(CIMObjectPath op)
            throws CIMException {
        if (op.getNameSpace().equalsIgnoreCase(SYSTEMNS) &&
                op.getObjectName().equalsIgnoreCase("Solaris_CIMOM")) {
            throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
        }
        ps.deleteInstance(op);
    }

    /**
     * InstanceProvider.execQuery()
     */
    public CIMInstance[] execQuery(CIMObjectPath op, String query,
            String ql, CIMClass cc)
            throws CIMException {

        Vector v = ps.execQuery(op, query, ql, cc);

        CIMInstance[] ciArray = new CIMInstance[v.size()];
        v.toArray(ciArray);
        return ciArray;
    }

    public CIMInstance[] associators(CIMObjectPath assocName,
            CIMObjectPath objectName,
            String resultClass,
            String role,
            String resultRole,
            boolean includeQualifiers,
            boolean includeClassOrigin,
            String propertyList[]) throws CIMException {

        if ((objectName == null) || (objectName.getKeys() == null)) {
            // This instance cannot be found.
            throw new CIMException(CIMException.CIM_ERR_NOT_FOUND, objectName,
                    "Need an instance name");
        }
        Vector v = commonAssociators(assocName, objectName, resultClass, role,
                resultRole, includeQualifiers,
                includeClassOrigin, propertyList);
        CIMInstance[] ciArray = new CIMInstance[v.size()];
        return (CIMInstance[]) v.toArray(ciArray);
    }

    /**
     * javax.wbem.provider.Associator.associatorNames
     */
    public CIMObjectPath[] associatorNames(CIMObjectPath assocName,
            CIMObjectPath objectName,
            String resultClass,
            String role,
            String resultRole) throws CIMException {
        Vector v = ps.associatorNames(assocName, objectName, resultClass,
                role, resultRole);
        CIMObjectPath[] copArray = new CIMObjectPath[v.size()];
        v.toArray(copArray);
        return copArray;
    }

    public CIMInstance[] references(CIMObjectPath assocName,
            CIMObjectPath objectName,
            String role,
            boolean includeQualifiers,
            boolean includeClassOrigin,
            String propertyList[]) throws CIMException {
        if ((objectName == null) || (objectName.getKeys() == null)) {
            // This instance cannot be found.
            throw new CIMException(CIMException.CIM_ERR_NOT_FOUND, objectName,
                    "Need an instance name");
        }
        Vector v = ps.reference(assocName, objectName, role, includeQualifiers,
                includeClassOrigin, propertyList);
        CIMInstance[] ciArray = new CIMInstance[v.size()];
        return (CIMInstance[]) v.toArray(ciArray);
    }

    /**
     * javax.wbem.provider.Associator.referenceNames
     */
    public CIMObjectPath[] referenceNames(CIMObjectPath assocName,
            CIMObjectPath objectName,
            String role) throws CIMException {
        Vector v = ps.referenceNames(assocName, objectName, role);
        CIMObjectPath[] copArray = new CIMObjectPath[v.size()];
        v.toArray(copArray);
        return copArray;
    }

    private void checkReadWritePermission(String operation, ServerSecurity ss)
            throws CIMSecurityException {
        checkPermission(operation, ss, true);
        checkPermission(operation, ss, false);

    }

    private void checkPermission(String operation, ServerSecurity ss,
            boolean bRead)
            throws CIMSecurityException {
        String op = bRead ? READ : WRITE;
        String opCap = bRead ? "r" : "w";

        String cap = ss.getCapability();
        if (operation.equals(op) &&
                !cap.equalsIgnoreCase(opCap) &&
                !cap.equalsIgnoreCase("rw")) {
            throw new CIMSecurityException(
                    CIMSecurityException.CIM_ERR_ACCESS_DENIED);
        }
    }

    private void setCapability(String rw, String namespace, ServerSecurity ss) {
        ss.setCapability(rw);
        ss.setCapabilityNS(namespace);
    }

    /**
     * A utility method to copy one stream into another.
     *
     * @param in     The source stream.
     * @param out    The destination stream.
     * @param length How much data to copy from the source vector.  If this
     *               is greather than or equal to than the length of the
     *               source stream, then the entire stream will be read.
     * @throws java.io.IOException if there is a problem copying the streams.
     */
    public static final void copyFile(DataInput in, DataOutput out, long length)
            throws IOException {
        boolean fileCopyComplete = false;
	/*
	 * Use a 5 KB buffer to perform the copy.
	 */
        byte[] copyBuffer = new byte[5 * 1024];

        long totalBytesCopied = 0;
        while (!fileCopyComplete) {
            int copySize = (int) (length - totalBytesCopied);
            if (copySize > copyBuffer.length) {
                copySize = copyBuffer.length;
            }
            in.readFully(copyBuffer, 0, copySize);

            out.write(copyBuffer, 0, copySize);

            totalBytesCopied += copySize;
            fileCopyComplete = (totalBytesCopied >= length);
        }
    }

    /**
     * This function is used to retrieve the adapter factory, so that
     * providers can be found
     */
    static ProviderAdapterFactory getProviderFactory() {
        return mProvFactory;
    }

    private void updateClasspath() throws Exception {
        // get the dynamic class loader
        ClassLoader cl = this.getClass().getClassLoader();
        if (cl instanceof DynClassLoader) {
            DynClassLoader dcl = (DynClassLoader) cl;
            // Populate the provider paths
            CIMObjectPath provPath = new CIMObjectPath("WBEMServices_Classpath");
            provPath.setNameSpace(INTEROPNS);
            // find the matching names
            CIMObjectPath[] ops = enumerateInstanceNames(provPath, null);
            // There will only be one property in each key, that is the path
            // that we want.
            for (int i = 0; i < ops.length; i++) {
                CIMObjectPath pathOp = ops[i];
                CIMProperty pathProp =
                        (CIMProperty) pathOp.getKeys().elementAt(0);
                String url = (String) (pathProp.getValue().getValue());
                dcl.addToClassPath(url);
            }
        } else {
            Debug.trace2("DynClassLoader not used, will not search for CIMOM classpaths");
        }
    }
}
