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
 *Contributor(s): 
 *		  WBEM Solutions, Inc.
 */

package org.wbemservices.wbem.compiler.mofc;

import javax.wbem.cim.*;
import javax.wbem.client.*;
import java.io.BufferedWriter;
import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class MofcBackend {

    private CIMOMHandle clientAPI = null;
    private CIMOMHandle rootClient = null;
    private CIM_Mofc parser;
    private UserPrincipal up = null;
    private PasswordCredential pc = null;
    public String fileName = "";
    public int lineNo = 0;
    public int parsePhase = 1;
    private String cimhost;
    private boolean setQual = false;
    private boolean setClass = false;
    private boolean setInstance = false;
    private boolean BEAN = false;
    private boolean deleteMode = false;
    private boolean XML = false;
    private boolean BUILD = false;
    private BufferedWriter xmlOut;
    private BufferedWriter bigxmlOut;
    private XmlWriter xmlwriter = new XmlWriter();
    private String qualDebug = "ADDING_QUALIFIERTYPE";
    private String classDebug = "ADDING_CLASS";
    private String instanceDebug = "ADDING_INSTANCE";
    private boolean connectToCimom = true;
    private String protocol = CIMClient.CIM_RMI;

    // The following variables are parsed from the file specified
    // in '-j' the command line argument. The PACKAGE, IMPORTS, and
    // EXCEPTIONS values are used in the capacity that their names 
    // suggest in the generated Beans. The directory is taken from
    // the -o option of the compiler
    //
    private String beanPackage = "";
    private String beanImports = "";
    private String beanExceptions = "";
    private String beanDir = "";

    // Namespace pragma can have these set for the namespace, to specify
    // delete, create, modify for UNREG and REG modes.
    private static final String NSDELETEMODE = "__delete";
    private static final String NSCREATEMODE = "__create";
    private static final String NSMODIFYMODE = "__modify";

    void HOLG_START(String a) {
        LogFile.add(LogFile.DEVELOPMENT, "METHOD_START", a);
    }

    void HOLG_PRINT(String a) {
        LogFile.add(LogFile.DEVELOPMENT, "NONE", a);
    }

    void HOLG_PRINT_ELEMENT(CIMElement a) {
        LogFile.add(LogFile.DEVELOPMENT, "NONE", a);
    }

    void HOLG_END(String a) {
        LogFile.add(LogFile.DEVELOPMENT, "METHOD_END", a);
    }

    void reportWarning(String errorType, String error, String data) {
        Integer lineNo = new Integer(parser.getCurrentLine());
        LogFile.add(LogFile.CRITICAL, "ERROR_WARNING", fileName, lineNo);
        LogFile.add(LogFile.CRITICAL, errorType);
        LogFile.add(LogFile.CRITICAL, error, new Object[] {
                data
        });
        System.err.println(
                I18N.loadStringFormat("ERROR_WARNING", fileName, lineNo));
        System.err.println(I18N.loadString(errorType));
        System.err.println(I18N.loadStringFormat(error, data));
    }

    void reportError(String errorType, String error, int exit, String data)
            throws CIMException {
        Integer lineNo = new Integer(parser.getCurrentLine());
        LogFile.add(LogFile.CRITICAL, "ERROR_LINE", fileName, lineNo);
        LogFile.add(LogFile.CRITICAL, errorType);
        LogFile.add(LogFile.CRITICAL, error, new Object[] {
                data
        });

        if (!parser.isInvoked) {
            System.err.println(
                    I18N.loadStringFormat("ERROR_LINE", fileName, lineNo));
            System.err.println(I18N.loadString(errorType));
            System.err.println(I18N.loadStringFormat(error, data));
            System.exit(exit);
        } else {
            throw new CIMException(CIMException.CIM_ERR_FAILED,
                    new Object[] { fileName, lineNo, errorType,
                            error, data });
        }
    }

    void setParsePhase(int phase) {
        // If we are in REGMODE or UNREGMODE when any phase starts, we
        // should be in create mode.
        if ((phase == 2) &&
                ((parser.mode == CIM_Mofc.REGMODE) ||
                        (parser.mode == CIM_Mofc.UNREGMODE))) {
            // reset state from phase 1.
            deleteMode = false;
            setQual = false;
            setClass = false;
            setInstance = false;
            deleteMode = false;
        }
        parsePhase = phase;
    }

    public Hashtable classAliases;
    public Hashtable instanceAliases;
    public Hashtable instanceAliasesNS;
    public Vector qualifiers;
    public Vector properties;
    public Vector methods;
    public Vector parameters;

    public boolean erroneousUnit;
    public boolean erroneousPart;
    public boolean erroneousQualifierList;

    public int refsRequired;
    public int keysRequired;

    public CIMNameSpace curNameSpace = new CIMNameSpace();
    public String curSchema = "";
    public CIMClass curClassEl;
    public CIMProperty curPropRefEl;
    public CIMMethod curMethodEl;
    public CIMQualifier curQualifierEl;
    public CIMQualifierType curQualifierTypeEl;
    public CIMParameter curParameterEl;
    public CIMInstance curInstanceEl;
    public CIMProperty curIPropertyEl;
    public String curLevel;
    public CIMDataType curType;
    public String curValueType;
    public MofcCIMValue curValues;
    public int size;
    public String curInstanceAlias;
    public String curClassAlias;

    public boolean print_MOFComp;

    public void cleanup() throws CIMException {
        if (rootClient != null) {
            rootClient.close();
        }
        if (clientAPI != null) {
            clientAPI.close();
        }
    }

    public MofcBackend(Hashtable clht, CIM_Mofc parser)
            throws java.rmi.RemoteException,
            java.net.MalformedURLException,
            java.rmi.NotBoundException,
            Exception {

        super();
        this.parser = parser;
        String standalone = (String) clht.get("standalone");

        // Should only connect to CIMOM if we're not running standalone and
        // we've not being invoked by another class.
        if (standalone != null) {
            connectToCimom = standalone.compareToIgnoreCase("true") != 0;
            if (!connectToCimom) {
                System.setProperty("logdir", (String) clht.get("logdir"));
                System.setProperty("passAllProviderCheck", "true");
            }
        }
        // check if we've been invoked
        connectToCimom = connectToCimom & !parser.isInvoked;

        String username = (String) clht.get("username");
        String password = (String) clht.get("password");

        if ((username != null) && (username.length() != 0)) {

            // Bug:4263404 Giving option of reading password from stdin
            if (password == null) {
                // Get password from standard input.
                System.out.print(I18N.loadString("PASSWORD"));
                byte a[] = new byte[20];
                System.in.read(a, 0, 20);
                for (int i = 0; i < a.length; i++) {
                    if (a[i] == (byte) '\n') {
                        a[i] = (byte) 0;
                        break;
                    }
                }
                password = new String(a);
            }
            //     End Bug:4263404

            // Create security classes for CIMClient call
            up = new UserPrincipal(username);
            pc = new PasswordCredential(password);

        }

        if (clht.get("-H") != null) {
            protocol = CIMClient.CIM_XML;
        }

        cimhost = (String) clht.get("cimhost");
        if ((cimhost == null) || (cimhost.length() == 0)) {
            cimhost = "localhost";
        }
        rootClient = connectToCimom ?
                (CIMOMHandle) new CIMClient(new CIMNameSpace(cimhost, ""), up, pc, protocol)
                : (CIMOMHandle) new LocalCIMClient(new CIMNameSpace(cimhost, ""),
                up, pc, parser.callBack);
        if (clht.get("-j") != null) {
            BEAN = true;
            beanPackage = (String) clht.get("beanPackage");
            beanImports = (String) clht.get("beanImports");
            beanExceptions = (String) clht.get("beanExceptions");
            String dir = (String) clht.get("logdir");
            beanDir = (dir == null) ? System.getProperty("user.dir") : dir;

            // generate the base Class and Interface
            //
            BeanBaseWriter beanBaseWriter = new BeanBaseWriter(beanPackage,
                    new File(beanDir));

        }

        if (clht.get("-x") != null) {
            XML = true;
        }
        if (clht.get("-b") != null) {
            BUILD = true;
        }
        if (clht.get("-Q") != null) {
            setQual = true;
            qualDebug = "SETTING_QUALIFIERTYPE";
        }
        if (clht.get("-C") != null) {
            setClass = true;
            classDebug = "SETTING_CLASS";
        }
        if (clht.get("-I") != null) {
            setInstance = true;
            instanceDebug = "SETTING_INSTANCE";
        }

        curNameSpace.setHost(cimhost);
        try {
            rootClient.createNameSpace(curNameSpace);
        } catch (CIMException e) {
        /* If the namespace already exists, or there is some
	       security access error thats ok */
            if (!e.getID().equals(CIMException.CIM_ERR_ALREADY_EXISTS) &&
                    !e.getID().equals(CIMSecurityException.CIM_ERR_ACCESS_DENIED)) {
                throw e;
            }
        }

        CIMNameSpace cns = new CIMNameSpace();
        cns.setHost(cimhost);
        clientAPI = connectToCimom ?
                (CIMOMHandle) new CIMClient(cns, up, pc, protocol) :
                (CIMOMHandle) new LocalCIMClient(cns, up, pc, parser.callBack);

        classAliases = new Hashtable();
        qualifiers = new Vector();
        properties = new Vector();
        methods = new Vector();
        instanceAliases = new Hashtable();
        instanceAliasesNS = new Hashtable();
        parameters = new Vector();

        erroneousUnit = false;
        erroneousPart = false;
        erroneousQualifierList = false;

        refsRequired = 0;
        keysRequired = 0;

        curClassEl = new CIMClass();
        curPropRefEl = new CIMProperty();
        curMethodEl = new CIMMethod();
        curQualifierEl = new CIMQualifier();
        curQualifierTypeEl = new CIMQualifierType();
        curParameterEl = new CIMParameter();
        curInstanceEl = new CIMInstance();
        curIPropertyEl = new CIMProperty();
        curType = null;
        curInstanceAlias = null;
        curClassAlias = null;
        curValueType = "";
        curValues = new MofcCIMValue();
        size = 0;

        print_MOFComp = false;
    }

    public String toString() {
        return new String(
                "MofcBackend:\n" +
                        "NamespaceTable:\n" +
                        "End of NamespaceTable\n" +
                        "QualifierTypesTable:\n" +
                        "End of QualifierTypesTable\n" +
                        "End of MofcBackend\n");
    }

    public void resetAfterSyntaxError() {
        HOLG_START("resetAfterSyntaxError");

        qualifiers = new Vector();
        properties = new Vector();
        methods = new Vector();
        parameters = new Vector();

        erroneousUnit = false;
        erroneousPart = false;
        erroneousQualifierList = false;

        refsRequired = 0;
        keysRequired = 0;

        curClassEl = new CIMClass();
        curPropRefEl = new CIMProperty();
        curMethodEl = new CIMMethod();
        curQualifierEl = new CIMQualifier();
        curParameterEl = new CIMParameter();
        curInstanceEl = new CIMInstance();
        curIPropertyEl = new CIMProperty();
        curInstanceAlias = null;
        curClassAlias = null;

        curType = null;
        curValueType = "";
        curValues = new MofcCIMValue();
        size = 0;

        HOLG_END("resetAfterSyntaxError");
    }

    //
    // Namespace level
    //
    public void switchNamespace(String newNamespace) throws CIMException {

        HOLG_START("switchNamespace");

        // when we are in REGMODE and UNREGMODE,
        // NSDELETEMODE, NSCREATEMODE, NSMODIFYMODE have different
        // semantics.
        // XXX Handling only REGMODE for now, have to add UNREGMODE
        if ((parser.mode == CIM_Mofc.REGMODE) ||
                (parser.mode == CIM_Mofc.UNREGMODE)) {
            if (newNamespace.equals(NSCREATEMODE)) {
                Debug.trace3("Going to create mode");
                // create mode
                setQual = false;
                setClass = false;
                setInstance = false;
                deleteMode = false;
                return;
            } else if (newNamespace.equals(NSMODIFYMODE)) {
                Debug.trace3("Going to set mode");
                // modify mode
                setQual = true;
                setClass = true;
                setInstance = true;
                deleteMode = false;
                return;
            } else if (newNamespace.equals(NSDELETEMODE)) {
                deleteMode = true;
                return;
            }
        }

        if (parsePhase == 1) {
            return;
        }

        // Get rid of the beginning and trailing quotes
        curNameSpace.setNameSpace(newNamespace);
        try {
            rootClient.createNameSpace(curNameSpace);
        } catch (Exception e) {
            //reportError("ERR_EXC", "ERR_NAMESPACE_CREATION", 1, e.toString());
        }
        try {
            clientAPI.close();
            clientAPI = null;
            clientAPI = connectToCimom ?
                    (CIMOMHandle) new CIMClient(curNameSpace, up, pc, protocol) :
                    (CIMOMHandle) new LocalCIMClient(curNameSpace, up, pc,
                            parser.callBack);
        } catch (Exception e) {
            if (e instanceof CIMException) {
                reportError("ERR_EXC", "ERR_CIMOM_CONNECTION", 1,
                        parser.toStringCIMException((CIMException) e));
            } else {
                reportError("ERR_EXC", "ERR_CIMOM_CONNECTION", 1, e.toString());
            }
        }

        HOLG_END("switchNamespace");
    }

    //
    // Schema level
    //
    public void assignSchemaName(String schemaName) throws CIMException {
        HOLG_START("assignSchemaName");

        if (schemaName.indexOf("_") >= 0) {
            reportError("ERR_SEM", "ERR_ILLEGAL_SCHEMA_NAME", 1, schemaName);
        } else {
            curSchema = schemaName.substring(1, schemaName.length() - 1);
        }
        HOLG_END("assignSchemaName");
    }

    //
    // Qualifier type level
    //

    public void assignQualifierTypeScope(CIMScope applies) {
        HOLG_START("assignQualifierTypeScope");
        curQualifierTypeEl.addScope(applies);
        HOLG_END("assignQualifierTypeScope");
    }

    public void assignQualifierTypeFlavor(CIMFlavor newFlavor) {
        HOLG_START("assignQualifierTypeFlavor");
        curQualifierTypeEl.addFlavor(newFlavor);
        HOLG_END("assignQualifierTypeFlavor");
    }

    public void addQualifierType() throws CIMException {
        HOLG_START("addQualifierType");
        if (erroneousPart) {
            erroneousUnit = true;
        }
        if (!erroneousUnit) {
            HOLG_PRINT_ELEMENT(curQualifierTypeEl);
            if (parsePhase == 2) {
                try {
                    LogFile.add(LogFile.DEBUG,
                            qualDebug,
                            curQualifierTypeEl.getName());
                    if (parser.isVerbose()) {
                        System.out.println(I18N.loadStringFormat(qualDebug,
                                curQualifierTypeEl.getName()));
                    }
                    if (setQual) {
                        clientAPI.setQualifierType(new CIMObjectPath(
                                curQualifierTypeEl.getName()), curQualifierTypeEl);
                    } else {
                        clientAPI.createQualifierType(new CIMObjectPath(
                                curQualifierTypeEl.getName()), curQualifierTypeEl);
                    }
                    if (XML) {
                        //xmlwriter.writeQualifierType(curQualifierTypeEl);
                        if (BUILD) {
                            xmlwriter.writeBigXml(curQualifierTypeEl, bigxmlOut);
                        }
                    }
                } catch (CIMException e) {
                    if (e.getID().equals(
                            CIMQualifierTypeException.CIM_ERR_ALREADY_EXISTS) &&
                            !setQual || e.getID().equals(
                            CIMQualifierTypeException.CIM_ERR_NOT_FOUND) &&
                            setQual) {
                        reportWarning("ERR_SEM",
                                "ERR_SETTING_QUALIFIER_TYPE",
                                parser.toStringCIMException(e));
                    } else {
                        reportError("ERR_SEM",
                                "ERR_SETTING_QUALIFIER_TYPE",
                                1, parser.toStringCIMException(e));
                    }
                } catch (Exception e) {
                    reportError("ERR_EXC", "ERR_SETTING_QUALIFIER_TYPE",
                            1, e.toString());
                }
            }
        }
        curQualifierTypeEl = new CIMQualifierType();
        HOLG_END("addQualifierType");
    }

    //
    // Qualifier level
    //

    public void assignQualifierNameType(String qualifierName) {
        HOLG_START("assignQualifierNameType");
        curQualifierEl.setName(qualifierName);
        HOLG_PRINT(qualifierName);
        HOLG_END("assignQualifierNameType");
    }

    public void assignQualifierParameter(boolean arrayType) {
        HOLG_START("assignQualifierParameter");
        if (!erroneousQualifierList) {
            if (!curValues.isEmpty()) {
                CIMValue cv;
                if (arrayType) {
                    cv = new CIMValue(curValues.vVector);
                } else {
                    cv = new CIMValue(curValues.firstElement());
                }
                curQualifierEl.setValue(cv);
            }
        }
        curValues = new MofcCIMValue();
        HOLG_END("assignQualifierParameter");
    }

    public void assignQualifierFlavor(CIMFlavor newFlavor) {
        HOLG_START("assignQualifierFlavor");
        curQualifierEl.addFlavor(newFlavor);
        HOLG_END("assignQualifierFlavor");
    }

    public void addQualifier() {
        HOLG_START("addQualifier");
        if (!erroneousQualifierList) {
            HOLG_PRINT_ELEMENT(curQualifierEl);
            qualifiers.addElement(curQualifierEl);
        }
        curQualifierEl = new CIMQualifier();
        HOLG_END("addQualifier");
    }

    public void checkQualifierList() throws CIMException {
        HOLG_START("checkQualifierList");
        if (!erroneousQualifierList) {
            if (qualifiers.contains(new CIMQualifier("ASSOCIATION"))) {
                if (!((CIMQualifier) qualifiers.firstElement()).getName().equalsIgnoreCase("association")) {
                    erroneousQualifierList = true;
                    reportError("ERR_SEM", "ERR_ASSOC_QUALIFIER_MISUSE", 1, "");
                }
            }
        }
        HOLG_END("checkQualifierList");
    }

    //
    // Class/association level
    //

    public void assignClassName(String className) throws CIMException {
        HOLG_START("assignClassName");
        if (className.indexOf("_") < 0) {
            if (curSchema.length() == 0) {
                reportError("ERR_SEM", "ERR_ILLEGAL_SCHEMA_NAME", 1, curSchema);
            } else {
                className = curSchema + "_" + className;
            }
        }
        curClassEl.setName(className);
        HOLG_PRINT(className);
        HOLG_END("assignClassName");
    }

    public void assignClassAlias(String aliasName) {
        HOLG_START("assignClassAlias");
        //curClassEl.setAlias(aliasName);
        // check for duplicate alias names
        if (parsePhase == 1) {
            curClassAlias = aliasName;
        }
        HOLG_END("assignClassAlias");
    }

    public void assignSuperclassName(String superclassName) {
        HOLG_START("assignSuperclassName");
        curClassEl.setSuperClass(superclassName);
        HOLG_PRINT(superclassName);
        HOLG_END("assignSuperclassName");
    }

    public void assignClassQualifiers() {
        HOLG_START("assignClassQualifiers");
        int tempIndex;
        if (!erroneousUnit) {
            // assign Qualifier ASSOCIATION
            CIMQualifier qe;
            tempIndex = qualifiers.indexOf(new CIMQualifier("association"));
            if (tempIndex >= 0) {
                qe = (CIMQualifier) qualifiers.elementAt(tempIndex);
            } else {
                qe = null;
            }
            if (qe != null) {
                CIMValue Tmp = qe.getValue();
                // We are assuming that the default for assoc. qualifier
                // is false according to the CIM spec. Hence having no
                // value indicates that it is false.
                if ((Tmp != null) && Tmp.equals(CIMValue.TRUE)) {
                    curClassEl.setIsAssociation(true);
                    curLevel = "ASSOCIATION";
                }
            }
            if (!erroneousPart) {
                curClassEl.setQualifiers(qualifiers);
            }
        }
        qualifiers = new Vector();
        HOLG_END("assignClassQualifiers");
    }

    public void setFile(BufferedWriter out) {
        xmlOut = out;
    }

    public void setBigFile(BufferedWriter out) {
        bigxmlOut = out;
    }

    public void addClass() throws CIMException {
        HOLG_START("addClass");
        HOLG_START("addClass");
        curClassEl.setProperties(properties);
        curClassEl.setMethods(methods);
        HOLG_PRINT_ELEMENT(curClassEl);

        // Catch the error in the first parse phase.
        if (parser.mode == CIM_Mofc.REGMODE && deleteMode) {
            throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER,
                    NSDELETEMODE);
        }
        if (parsePhase == 2) {
            try {
                LogFile.add(LogFile.DEBUG, classDebug, curClassEl.getName());
                if (parser.isVerbose()) {
                    System.out.println(I18N.loadStringFormat(classDebug,
                            curClassEl.getName()));
                }

                if (deleteMode) {
                    clientAPI.deleteClass(new CIMObjectPath(
                            curClassEl.getName()));
                } else if (setClass) {
                    clientAPI.setClass(new CIMObjectPath(curClassEl.getName()),
                            curClassEl);
                } else {
                    // XXX
                    clientAPI.createClass(new CIMObjectPath(
                            curClassEl.getName()), curClassEl);
                }

                if (XML) {
                    CIMClass curClass = clientAPI.getClass(new CIMObjectPath(
                            curClassEl.getName()), false, true, true, null);
                    xmlwriter.writeClass(curClass);
                    if (BUILD) {
                        xmlwriter.writeAllClasses(curClass, xmlOut);
                        xmlwriter.writeBigXml(curClass, bigxmlOut);
                    }
                }
                if (BEAN) {

                    CIMClass curClass = clientAPI.getClass(
                            new CIMObjectPath(curClassEl.getName()),
                            true, true, true, null);
                    BeanGenerator bg = new BeanGenerator(clientAPI,
                            curClass,
                            beanPackage,
                            beanImports,
                            beanExceptions,
                            new File(beanDir));

                }

            } catch (CIMException e) {

                // Report a warning if we are trying to create and
                // the class already exists, or we're trying to
                // modify/delete and the class doesnt exist.
                if (e.getID().equals(CIMClassException.CIM_ERR_ALREADY_EXISTS) &&
                        !setClass ||
                        e.getID().equals(CIMClassException.CIM_ERR_NOT_FOUND) &&
                                (setClass || deleteMode)) {
                    reportWarning("ERR_SEM",
                            "ERR_EXC_SET_CLASS", parser.toStringCIMException(e));

                    if (XML && e.getID().equals(
                            CIMClassException.CIM_ERR_ALREADY_EXISTS)
                            && !setClass) {
                        try {
                            CIMClass curClass = clientAPI.getClass(new
                                            CIMObjectPath(curClassEl.getName()),
                                    false, true, true, null);
                            xmlwriter.writeClass(curClass);
                            if (BUILD) {
                                xmlwriter.writeAllClasses(curClass, xmlOut);
                                xmlwriter.writeBigXml(curClass, bigxmlOut);
                            }
                        } catch (CIMException e2) {
                        }
                    }
                    if (BEAN && e.getID().equals(
                            CIMClassException.CIM_ERR_ALREADY_EXISTS)) {

                        CIMClass curClass = clientAPI.getClass(
                                new CIMObjectPath(curClassEl.getName()),
                                true, true, true, null);
                        BeanGenerator bg = new BeanGenerator(clientAPI,
                                curClass,
                                beanPackage,
                                beanImports,
                                beanExceptions,
                                new File(beanDir));

                    }

                } else {
                    reportError("ERR_SEM", "ERR_EXC_SET_CLASS", 1,
                            parser.toStringCIMException(e));
                }
            } catch (Exception e) {
                reportError("ERR_EXC", "ERR_EXC_SET_CLASS", 1, e.toString());
            }
        } else {
            if (curClassAlias != null) {
                classAliases.put(curClassAlias, curClassEl.getName());
            }
        }
        keysRequired = 0;
        curClassEl = new CIMClass();
        curClassAlias = null;
        properties = new Vector();
        methods = new Vector();
        HOLG_END("addClass");
    }

    //
    // Property/reference/method level
    //
    public void assignFeatureName(String featureName) throws CIMException {
        HOLG_START("assignFeatureName");
        if ((properties.contains(new CIMProperty(featureName))) ||
                (methods.contains(new CIMMethod(featureName)))) {
            erroneousPart = true;
            reportError("ERR_SEM", "ERR_FEATURE_REDEFINED", 1,
                    curClassEl.getName());
        } else {
            curPropRefEl.setName(featureName);
            curMethodEl.setName(featureName);
            HOLG_PRINT(featureName);
        }
        HOLG_END("assignFeatureName");
    }

    //
    // Property Level
    //
    public void assignPropertyQualifiers() {
        HOLG_START("assignPropertyQualifiers");
        if (!erroneousPart) {
            curPropRefEl.setQualifiers(qualifiers);
            CIMQualifier cq = curPropRefEl.getQualifier("key");
            if (cq != null) {
                CIMValue cv = cq.getValue();
                if (cv != null) {
                    if (cv.getValue().equals(new Boolean(true))) {
                        curPropRefEl.setKey(true);
                    }
                } else {
                    curPropRefEl.setKey(true);
                }
            }
        }
        qualifiers = new Vector();
        HOLG_END("assignPropertyQualifiers");
    }

    public void addProperty() {
        HOLG_START("addProperty");
        if (erroneousUnit) {
            erroneousPart = true;
        }
        if (!erroneousPart) {
            HOLG_PRINT_ELEMENT(curPropRefEl);
            verifyQualifiers(curPropRefEl);
            properties.addElement(curPropRefEl);
        } else {
            erroneousUnit = true;
        }
        curPropRefEl = new CIMProperty();
        HOLG_END("addProperty");
    }

    //
    // Reference Level
    //
    public void assignRefClassName(CIMDataType refClassType) {
        HOLG_START("assignRefClassName");
        curPropRefEl.setType(refClassType);
        HOLG_PRINT(refClassType.toString());
        HOLG_END("assignRefClassName");
    }

    public void assignReferenceQualifiers() throws CIMException {
        HOLG_START("assignReferenceQualifiers");
        if (!erroneousPart) {
            try {
                curPropRefEl.setQualifiers(qualifiers);
            } catch (Exception e) {
                //XXX: Need to catch what type of exception and give user
                // real info
                reportError("ERR_EXC", "ERR_EXC_SET_QUAL", 1, e.toString());
            }
        }
        qualifiers = new Vector();
        HOLG_END("assignReferenceQualifiers");
    }

    public void addReference() {
        HOLG_START("addReference");
        if (erroneousUnit) {
            erroneousPart = true;
        }
        if (!erroneousPart) {
            refsRequired--;
            HOLG_PRINT_ELEMENT(curPropRefEl);
            properties.addElement(curPropRefEl);
        }
        curPropRefEl = new CIMProperty();
        HOLG_END("addReference");
    }

    //
    // Method Level
    //
    public void assignMethodQualifiers() {
        HOLG_START("assignMethodQualifiers");
        curMethodEl.setQualifiers(qualifiers);
        qualifiers = new Vector();
        HOLG_END("assignMethodQualifiers");
    }

    public void assignMethodParameters() {
        HOLG_START("assignMethodParameters");
        if (!erroneousPart) {
            curMethodEl.setParameters(parameters);
        }
        parameters = new Vector();
        HOLG_END("assignMethodParameters");
    }

    public void addMethod() {
        HOLG_START("addMethod");
        if (erroneousUnit) {
            erroneousPart = true;
        }
        if (!erroneousPart) {
            methods.addElement(curMethodEl);
            HOLG_PRINT(curMethodEl.getName());
        } else {
            erroneousUnit = true;
        }
        curMethodEl = new CIMMethod();
        HOLG_END("addMethod");
    }

    //
    // Parameter level
    //
    public void assignParameterName(String ParameterName) throws CIMException {
        HOLG_START("assignParameterName");
        if (parameters.contains(new CIMParameter(ParameterName))) {
            reportError("ERR_SEM", "ERR_PARAMETER_EXISTS", 2, ParameterName);
        } else {
            curParameterEl.setName(ParameterName);
            HOLG_PRINT(ParameterName);
        }
        HOLG_END("assignParameterName");
    }

    public void assignParameterQualifiers() {
        HOLG_START("assignParameterQualifiers");
        if (!erroneousPart) {
            curParameterEl.setQualifiers(qualifiers);
        }
        qualifiers = new Vector();
        HOLG_END("assignParameterQualifiers");
    }

    public void addParameter() {
        HOLG_START("addParameter");
        if (erroneousUnit) {
            erroneousPart = true;
        }
        if (!erroneousPart) {
            parameters.addElement(curParameterEl);
            HOLG_PRINT(curParameterEl.getName());
        } else {
            erroneousUnit = true;
        }
        curParameterEl = new CIMParameter();
        HOLG_END("addParameter");
    }

    //
    // Instance level
    // 
    public void assignInstanceClass(String instanceClass) {
        HOLG_START("assignInstanceClass");
        curInstanceEl.setClassName(instanceClass);
        HOLG_PRINT(instanceClass);
        HOLG_END("assignInstanceClass");
    }

    public void assignInstanceAlias(String aliasName) {
        HOLG_START("assignInstanceAlias");
        //curInstanceEl.setAlias(aliasName);
        if (parsePhase == 1) {
            curInstanceAlias = aliasName;
        }
        HOLG_END("assignInstanceAlias");
    }

    public void assignInstanceQualifiers() {
        HOLG_START("assignInstanceQualifiers");
        if (!erroneousUnit) {
            curInstanceEl.setQualifiers(qualifiers);
        }
        qualifiers = new Vector();
        HOLG_END("assignInstanceQualifiers");
    }

    //CHANGED
    public void assignInstancePropertyQualifiers() {
        HOLG_START("addInstancePropertyQualifiers");
        //NOT IMPLEMENTED
        HOLG_END("addInstancePropertyQualifiers");
    }

    public void addInstanceProperty() {
        HOLG_START("addInstanceProperty");
        if (erroneousUnit) {
            erroneousPart = true;
        }
        if (!erroneousPart) {
            //Need to get the real datatype since the instance doesn't include it
            try {
                CIMClass cClass = clientAPI.getClass(curInstanceEl.getObjectPath(),
                        false, false, false, null);
                CIMProperty cp = cClass.getProperty(curIPropertyEl.getName());
                if (cp.isReference()) {
                    CIMValue cv = cp.getValue();
                    if (curIPropertyEl.getValue().getValue() instanceof String) {
                        CIMValue cv2 = new CIMValue(new CIMObjectPath((String) curIPropertyEl.getValue().getValue()), cv.getType());
                        cp.setValue(cv2);
                        curIPropertyEl = cp;
                    }
                }
            } catch (Exception e) {
                if (parsePhase == 1) {
                    return;
                }
                //some error ignore and let error come back from server
            }
            HOLG_PRINT_ELEMENT(curIPropertyEl);
            properties.addElement(curIPropertyEl);
        } else {
            erroneousUnit = true;
        }
        curIPropertyEl = new CIMProperty();
        HOLG_END("addInstanceProperty");
    }

    public void addInstance() throws CIMException {
        HOLG_START("addInstance");
        if (erroneousPart) {
            erroneousUnit = true;
        }
        if (!erroneousUnit) {
            curInstanceEl.setProperties(properties);
            if (parsePhase == 2) {
                try {
                    LogFile.add(LogFile.DEBUG, instanceDebug,
                            curInstanceEl.getObjectPath());
                    if (parser.isVerbose()) {
                        System.out.println(I18N.loadStringFormat(instanceDebug,
                                curInstanceEl.getObjectPath()));
                    }

                    if (deleteMode) {
                        // Determine the object path of the instance.
                        CIMObjectPath instanceOp = new CIMObjectPath();
                        instanceOp.setObjectName(curInstanceEl.getClassName());
                        // We should be just getting the key value pairs
                        // here, but we are not guaranteed that the key
                        // properties will be set correctly. Ideally we
                        // can get the class definition and determine them,
                        // but we're avoiding the overhead for now.
                        instanceOp.setKeys(curInstanceEl.getProperties());
                        clientAPI.deleteInstance(instanceOp);
                    } else if (setInstance) {
                        clientAPI.setInstance(new CIMObjectPath(
                                curInstanceEl.getClassName()), curInstanceEl);
                    } else {
                        clientAPI.createInstance(new CIMObjectPath(
                                curInstanceEl.getClassName()), curInstanceEl);
                    }
                } catch (CIMException e) {
                    // If the element already exists for a create call or
                    // if the element does not exist for an update/delete
                    // call, we report a warning.
                    if (e.getID().equals(
                            CIMInstanceException.CIM_ERR_ALREADY_EXISTS) &&
                            !setInstance ||
                            e.getID().equals(CIMInstanceException.CIM_ERR_NOT_FOUND)
                                    && (setInstance || deleteMode)) {
                        reportWarning("ERR_SEM", "ERR_EXC_SET_INST",
                                parser.toStringCIMException(e));
                    } else {
                        reportError("ERR_SEM", "ERR_EXC_SET_INST", 1,
                                parser.toStringCIMException(e));
                    }
                } catch (Exception e) {
                    reportError("ERR_EXC", "ERR_EXC_SET_INST", 1, e.toString());
                }
            } else {
                if (curInstanceAlias != null) {
                    // check for duplicates
                    instanceAliases.put(curInstanceAlias, curInstanceEl);
                    instanceAliasesNS.put(curInstanceAlias, curNameSpace);
                }
            }
            HOLG_PRINT_ELEMENT(curInstanceEl);
        }
        curInstanceEl = new CIMInstance();
        curInstanceAlias = null;
        properties = new Vector();
        HOLG_END("addInstance");
    }

    public CIMObjectPath getInstanceName(String aliasName) throws CIMException {
        if (parsePhase == 1) {
            return new CIMObjectPath();
        }

        Object o = null;
        CIMInstance ci = null;
        o = instanceAliases.get(aliasName);

        if (o == null) {
            reportError("ERR_SEM", "NO_SUCH_ALIAS", 1, aliasName);
        }

        if (o instanceof CIMObjectPath) {
            return (CIMObjectPath) o;
        } else {
            ci = (CIMInstance) o;
        }
        CIMObjectPath op = new CIMObjectPath(ci.getClassName());
        // set the namespace also.
        CIMInstance nci = null;
        try {
            CIMClass cc = clientAPI.getClass(op, false, true, true, null);
            nci = cc.newInstance();
        } catch (Exception e) {
            reportError("ERR_SEM", "ERR_NO_SUCH_CLASS", 1, ci.getClassName());
        }
        nci.updatePropertyValues(ci.getProperties());
        op = new CIMObjectPath(ci.getClassName(), "");
        op.setKeys(nci.getKeys());
        op.setNameSpace((
                (CIMNameSpace) instanceAliasesNS.get(aliasName)).getNameSpace());
        instanceAliases.put(aliasName, op);
        return op;
    }

    public void verifyQualifiers(CIMProperty cp) {
        boolean hasValue = false, hasValuemap = false;
        int numValues = -1, numValuemaps = 0;
        boolean hasBitValue = false, hasBitmap = false;
        int numBitValues = -1, numBitmaps = 0;
        Vector v = cp.getQualifiers();
        Enumeration e = v.elements();
        while (e.hasMoreElements()) {
            CIMQualifier cq = (CIMQualifier) e.nextElement();
            if (cq.getName().equalsIgnoreCase("values")) {
                hasValue = true;
                CIMValue cv = cq.getValue();
                Vector vv = (Vector) cv.getValue();
                numValues = vv.capacity();
            }
            if (cq.getName().equalsIgnoreCase("valuemap")) {
                hasValuemap = true;
                CIMValue cv = cq.getValue();
                Vector vv = (Vector) cv.getValue();
                numValuemaps = vv.capacity();
            }
            if (cq.getName().equalsIgnoreCase("BitMap")) {
                hasBitValue = true;
                CIMValue cv = cq.getValue();
                Vector vv = (Vector) cv.getValue();
                numBitValues = vv.capacity();
            }
            if (cq.getName().equalsIgnoreCase("BitValues")) {
                hasBitmap = true;
                CIMValue cv = cq.getValue();
                Vector vv = (Vector) cv.getValue();
                numBitmaps = vv.capacity();
            }
        }
        if (hasValue && hasValuemap) {
            //check number of array
            if (numValues != numValuemaps) {
                try {
                    reportError("ERR_SEM", "ERR_ILLEGAL_VALUES", 1,
                            this.curClassEl.getName() + "."
                                    + cp.getName() + " Values/ValueMap qualifiers don't have " +
                                    "the same number of elements (Values has " + numValues +
                                    " ValueMap has " + numValuemaps + ")");
                } catch (Exception eig) {
                }
            }
        }
        if (hasBitValue && hasBitmap) {
            //check number of array
            if (numBitValues != numBitmaps) {
                try {
                    reportError("ERR_SEM", "ERR_ILLEGAL_VALUES", 1,
                            this.curClassEl.getName() + "."
                                    + cp.getName() + " BitMap/BitValues qualifiers don't have "
                                    + "the same number of elements (Values has " + numBitValues
                                    + " ValueMap has " + numBitmaps + ")");
                } catch (Exception eig) {
                }
            }
        }

    }
}
