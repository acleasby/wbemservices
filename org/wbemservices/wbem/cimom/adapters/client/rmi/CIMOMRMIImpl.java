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
 *Contributor(s): _______________________________________
*/
package org.wbemservices.wbem.cimom.adapters.client.rmi;

import org.wbemservices.wbem.cimom.*;
import org.wbemservices.wbem.cimom.adapter.client.ClientProtocolAdapterIF;
import org.wbemservices.wbem.client.adapter.rmi.CheckSumGen;
import org.wbemservices.wbem.client.adapter.rmi.RemoteCIMListener;

import javax.wbem.cim.*;
import javax.wbem.client.*;
import javax.wbem.client.Version;
import javax.wbem.security.SecurityMessage;
import javax.wbem.security.SecurityToken;
import javax.wbem.security.SecurityUtil;
import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.HashMap;
import java.util.Vector;

public class CIMOMRMIImpl implements ClientProtocolAdapterIF {

    // Map of client contexts and the session ids.
    private HashMap sessionMap = new HashMap();
    private CheckSumGen csg = new CheckSumGen();
    private final static boolean verbose = false;

    private final static String READ = "read";
    private final static String WRITE = "write";
    private final static String PORT = "PortNumber";
    private final static String RMIPORT = "5987";
    private final static String RMIPORTPROP = "org.wbemservices.wbem.rmiport";

    CIMOMServer comp;

    class RMIIndicationHandler implements EventService.IndicationHandler {
        private RemoteCIMListener getRemoteListener(CIMInstance
                handlerInstance) throws CIMException {
            String sessionID = (String)
                    handlerInstance.getProperty(EventService.HANDLERNAMEPROP).
                            getValue().getValue();
            ServerSecurity ss = (ServerSecurity) sessionMap.get(sessionID);
            if (ss == null) {
                Debug.trace2("No session " + sessionID);
                throw new CIMException(CIMException.CIM_ERR_FAILED,
                        "No session ", sessionID);
            }
            return ss.getListener();
        }

        public CommonServerSecurityContext getSecurityContext(CIMInstance
                handlerInstance) {
            String sessionID = (String)
                    handlerInstance.getProperty(EventService.HANDLERNAMEPROP).
                            getValue().getValue();
            return (CommonServerSecurityContext) sessionMap.get(sessionID);
        }

        public void deliverEvent(CIMEvent e, CIMInstance handlerInstance)
                throws CIMException {
            RemoteCIMListener rl = getRemoteListener(handlerInstance);
            if (rl == null) {
                Debug.trace2("No remote listener");
                throw new CIMException(CIMException.CIM_ERR_FAILED,
                        "No remote listener");
            }
            try {
                rl.indicationOccured(e, null);
            } catch (Exception ex) {
                throw new CIMException(CIMException.CIM_ERR_FAILED,
                        ex);
            }
        }

        public void ping(CIMInstance handlerInstance)
                throws CIMException {
            RemoteCIMListener rl = getRemoteListener(handlerInstance);
            if (rl == null) {
                Debug.trace2("No remote listener");
                throw new CIMException(CIMException.CIM_ERR_FAILED,
                        "No remote listener");
            }
            try {
                rl.isAvailable();
            } catch (Exception ex) {
                throw new CIMException(CIMException.CIM_ERR_FAILED,
                        ex);
            }
        }
    }

    public CIMOMRMIImpl() {
    }

    public void initialize(CIMOMServer cimom, CIMOMHandle ch) {
        this.comp = cimom;
        DeliveryHandler.registerIndicationHandler(
                EventService.JRMIDELIVERYCLASS, new RMIIndicationHandler());
    }

    public int startService(CIMInstance cpa) {
        int rmiport;
        try {
            CIMValue cv = cpa.getProperty(PORT).getValue();
            rmiport = ((UnsignedInt32) cv.getValue()).intValue();
        } catch (NullPointerException ex) {
            Debug.trace2("Got exception getting the RMI port number", ex);
            rmiport = Integer.parseInt(System.getProperty(RMIPORTPROP,
                    RMIPORT));
        }
        try {
            try {
                LocateRegistry.createRegistry(rmiport);
            } catch (RemoteException e) {
                // This could happen if another createRegistry has already
                // been done.
                Debug.trace2("Ignoring createRegistry exception", e);
            }
            System.setSecurityManager(new RMISecurityManager());
            String hostAddress =
                    InetAddress.getLocalHost().getHostAddress();

            // WARNING!  HACK COMING UP!
            //   java.rmi.Naming in Java 1.4 (thru at least build b59) has
            //   a bug (#4434794) such that "rmi://<hostaddress>:port" fails.
            //   but "rmi://<hostname>:port" works.  As a workaround, we try
            //   it with hostAddress first, and if that fails, try it with
            //   hostName before giving up.
            //
            for (int i = 1; i <= 2; i++) {
                try {
                    Naming.rebind("rmi://" + hostAddress + ":" + rmiport
                            + "/CIMOM_" + "1", new CIMOM_1Impl(this));
                } catch (Exception e) {
                    if (i == 1) {
                        hostAddress =
                                InetAddress.getLocalHost().getHostName();
                    } else {
                        throw e;
                    }
                }
            }
        } catch (Exception e) {
            //XXX: Want to throw exception here
            //System.out.println("REGISTRY_ERROR");
            return -1;
        }
        return 0;
    }

    public int stopService() {
        return 0;
    }

    /*
    XXX: Remove
        public CIMOMImpl getCIMOMImp() {
        return comp;

        }
    */
    public SecurityMessage hello(String version,
            SecurityMessage cm, String clientHost)
            throws CIMException {
        try {
            ServerSecurity ss = new ServerSecurity();
            ss.setClientHostName(clientHost);
            String us = CIMOMUtils.getUniqueString();
            SecurityMessage sm =
                    ss.generateChallenge(version, cm, us.getBytes());
            synchronized (sessionMap) {
                sessionMap.put(us, ss);
            }
            return sm;
        } catch (CIMException e) {
            if (verbose) {
                e.printStackTrace();
            }
            throw e;
        } catch (Exception e) {
            if (verbose) {
                e.printStackTrace();
            }
            throw new CIMException(CIMException.CIM_ERR_FAILED, e);
        }
    }

    // just repeat the 40 bits
    private byte[] convertTo16(byte[] sessionKey) {
        byte[] output = new byte[16];
        System.arraycopy(sessionKey, 0, output, 0, 5);
        System.arraycopy(sessionKey, 0, output, 5, 5);
        System.arraycopy(sessionKey, 0, output, 10, 5);
        output[15] = sessionKey[0];
        return output;
    }

    SecurityMessage credentials(String version,
            SecurityMessage cm, String clientHost)
            throws CIMException {
        try {
            SecurityMessage sm = null;
            ServerSecurity ss = getServerSecurity(
                    new String(cm.getSessionId()));
            ss.setClientHostName(clientHost);
            synchronized (ss) {
                byte[] sessionKey = new byte[5];
                SecurityUtil.secrand.nextBytes(sessionKey);
                sessionKey = convertTo16(sessionKey);
                try {
                    sm = ss.validateResponse(ss.getChallenge(),
                            ss.getShadow(), ss.getPublicKey(),
                            sessionKey, cm);
                } catch (CIMException e) {
                    synchronized (sessionMap) {
                        sessionMap.remove(new String(cm.getSessionId()));
                    }
                    if (verbose) {
                        e.printStackTrace();
                    }
                    throw e;
                } catch (Exception e) {
                    if (verbose) {
                        e.printStackTrace();
                    }
                    throw new CIMException(CIMException.CIM_ERR_FAILED, e);
                }
                return sm;
            }
        } catch (Error e) {
            throw e;
        }
    }

    void close(String version, SecurityToken st)
            throws CIMException {
        String[] oarray = { version, "close" };
        ServerSecurity ss;
        try {
            ss = getServerSecurity(new String(st.getSessionId()));
        } catch (CIMSecurityException e) {
            return;
        }
        synchronized (ss) {
            serverSecurityAuthInc(ss, oarray, st);
            synchronized (sessionMap) {
                sessionMap.remove(new String(st.getSessionId()));
            }
        }
        comp.close(version, ss);

    }

    void deleteNameSpace(String version, CIMNameSpace parent,
            CIMNameSpace nameSpace,
            SecurityToken st)
            throws CIMException {
        String[] oarray = { version, "deleteNameSpace", csg.toString(parent),
                csg.toString(nameSpace) };
        CIMNameSpace validNS = new CIMNameSpace();
        if (nameSpace == null) {
            nameSpace = new CIMNameSpace("", "");
        }
        validNS.setNameSpace(parent.getNameSpace() + '/' +
                nameSpace.getNameSpace());
        String ns = validNS.getNameSpace().replace('\\', '/');
        String parentNS = ns.substring(0, ns.lastIndexOf('/'));
        ServerSecurity ss =
                serverSecurityDo(oarray, st, WRITE, parentNS, true);
        comp.deleteNameSpace(version, parent, nameSpace, ss);
    }

    void createNameSpace(String version,
            CIMNameSpace parent, CIMNameSpace nameSpace,
            SecurityToken st)
            throws CIMException {
        String[] oarray = { version, "createNameSpace", csg.toString(parent),
                csg.toString(nameSpace) };
        CIMNameSpace validNS = new CIMNameSpace();
        if (nameSpace == null) {
            nameSpace = new CIMNameSpace("", "");
        }
        validNS.setNameSpace(parent.getNameSpace() + '/' +
                nameSpace.getNameSpace());
        String ns = validNS.getNameSpace().replace('\\', '/');
        String parentNS = ns.substring(0, ns.lastIndexOf('/'));
        ServerSecurity ss =
                serverSecurityDo(oarray, st, WRITE, parentNS, true);
        comp.createNameSpace(version, parent, nameSpace, ss);

    }

    void createQualifierType(String version,
            CIMNameSpace nameSpace,
            CIMObjectPath objectName,
            CIMQualifierType qt,
            SecurityToken st)
            throws CIMException {

        String[] oarray = { version, "createQualifierType",
                csg.toString(nameSpace),
                csg.toString(objectName),
                csg.toString(qt) };
        ServerSecurity ss = serverSecurityDo(oarray, st, WRITE,
                getNameSpace(nameSpace, objectName), true);

        comp.createQualifierType(version, nameSpace, objectName, qt, ss);
    }

    /**
     * Adds a class to the CIMOM Repository
     */
    void createClass(String version,
            CIMNameSpace nameSpace,
            CIMObjectPath objectName,
            CIMClass cc,
            SecurityToken st)
            throws CIMException {

        String[] oarray = { version, "createClass", csg.toString(nameSpace),
                csg.toString(objectName), csg.toString(cc) };
        ServerSecurity ss = serverSecurityDo(oarray, st, WRITE,
                getNameSpace(nameSpace, objectName), true);
        comp.createClass(version, nameSpace, objectName, cc, ss);
    }

    CIMObjectPath createInstance(String version,
            CIMNameSpace nameSpace,
            CIMObjectPath objectName,
            CIMInstance ci,
            SecurityToken st)
            throws CIMException {

        String[] oarray = { version, "createInstance",
                csg.toString(nameSpace), csg.toString(objectName),
                csg.toString(ci) };
        ServerSecurity ss = serverSecurityDo(oarray, st, WRITE,
                getNameSpace(nameSpace, objectName), false);
        return comp.createInstance(version, nameSpace, objectName,
                ci, ss);
    }

    void setQualifierType(String version, CIMNameSpace nameSpace,
            CIMObjectPath objectName, CIMQualifierType qt,
            SecurityToken st)
            throws CIMException {
        String[] oarray = { version, "setQualifierType",
                csg.toString(nameSpace),
                csg.toString(objectName),
                csg.toString(qt) };
        ServerSecurity ss = serverSecurityDo(oarray, st, WRITE,
                getNameSpace(nameSpace, objectName), true);
        comp.setQualifierType(version, nameSpace, objectName, qt, ss);

    }

    /**
     * Adds a class to the CIMOM Repository
     */
    void setClass(String version, CIMNameSpace nameSpace,
            CIMObjectPath objectName,
            CIMClass cc,
            SecurityToken st)
            throws CIMException {
        String[] oarray = { version, "setClass", csg.toString(nameSpace),
                csg.toString(objectName), csg.toString(cc) };

        ServerSecurity ss = serverSecurityDo(oarray, st, WRITE,
                getNameSpace(nameSpace, objectName), true);
        comp.setClass(version, nameSpace, objectName, cc, ss);
    }

    void setInstance(String version,
            CIMNameSpace nameSpace,
            CIMObjectPath objectName,
            CIMInstance ci,
            SecurityToken st)
            throws CIMException {

        String[] oarray = { version, "setInstance", csg.toString(nameSpace),
                csg.toString(objectName), csg.toString(ci) };
        ServerSecurity ss = serverSecurityDo(oarray, st, WRITE,
                getNameSpace(nameSpace, objectName), false);
        comp.setInstance(version, nameSpace, objectName, ci, ss);
    }

    void setInstance(String version,
            CIMNameSpace nameSpace,
            CIMObjectPath objectName,
            CIMInstance ci,
            boolean includeQualifiers,
            String[] propertyList,
            SecurityToken st)
            throws CIMException {

        String[] oarray = { version, "setInstance", csg.toString(nameSpace),
                csg.toString(objectName), csg.toString(ci) };
        ServerSecurity ss = serverSecurityDo(oarray, st, WRITE,
                getNameSpace(nameSpace, objectName), false);
        comp.setInstance(version, nameSpace, objectName, ci, includeQualifiers,
                propertyList, ss);
    }

    void setProperty(String version,
            CIMNameSpace nameSpace,
            CIMObjectPath objectName,
            String propertyName,
            CIMValue cv,
            SecurityToken st)
            throws CIMException {

        String[] oarray = { version, "setProperty", csg.toString(nameSpace),
                csg.toString(objectName), propertyName,
                csg.toString(cv) };
        ServerSecurity ss = serverSecurityDo(oarray, st, WRITE,
                getNameSpace(nameSpace, objectName), false);
        comp.setProperty(version, nameSpace, objectName,
                propertyName, cv, ss);
    }

    Vector execQuery(String version, CIMNameSpace nameSpace,
            CIMObjectPath relNS,
            String query,
            String ql,
            SecurityToken st)
            throws CIMException {

        String[] oarray = { version, "execQuery", csg.toString(nameSpace),
                csg.toString(relNS), query, ql + "" };
        ServerSecurity ss = serverSecurityDo(oarray, st, READ,
                getNameSpace(nameSpace, relNS), false);
        return comp.execQuery(version, nameSpace, relNS, query,
                ql, ss);
    }
/*
    CIMClass getClass(String version, CIMNameSpace nameSpace, 
			     CIMObjectPath objectName, boolean localOnly,
			     SecurityToken st) 
    throws CIMException {
	String[] oarray = {version, "getClass", csg.toString(nameSpace),
			   csg.toString(objectName)};
	ServerSecurity ss = serverSecurityDo(oarray, st, READ,
			     getNameSpace(nameSpace, objectName), true);
	return comp.getClass(version, nameSpace, objectName, localOnly, ss);
    }
*/

    Vector invokeMethod(String version, CIMNameSpace nameSpace,
            CIMObjectPath objectName, String methodName,
            Vector inParams, SecurityToken st)
            throws CIMException {
        String[] oarray = { version, "invokeMethod", csg.toString(nameSpace),
                csg.toString(objectName), methodName,
                csg.toString(inParams) };

        ServerSecurity ss = serverSecurityDo(oarray, st, WRITE,
                getNameSpace(nameSpace, objectName), false);

        return comp.invokeMethod(version, nameSpace, objectName, methodName,
                inParams, ss);
    }

    Vector invokeMethod(String version, CIMNameSpace nameSpace,
            CIMObjectPath objectName, String methodName,
            CIMArgument[] inArgs, SecurityToken st)
            throws CIMException {
        String[] oarray = { version, "invokeMethod", csg.toString(nameSpace),
                csg.toString(objectName), methodName };

        ServerSecurity ss = serverSecurityDo(oarray, st, WRITE,
                getNameSpace(nameSpace, objectName), false);

        return comp.invokeMethod(version, nameSpace, objectName, methodName,
                inArgs, ss);
    }

    /**
     * @deprecated Used for Sun CIMAPI version 1.0 - 2.5
     */
/*
    CIMInstance getInstance(String version, 
			    CIMNameSpace nameSpace, 
			    CIMObjectPath objectName, 
			    boolean localOnly,
			    SecurityToken st)
    	throws CIMException {

	String[] oarray = {version, "getInstance", csg.toString(nameSpace),
			   csg.toString(objectName)};
	ServerSecurity ss = serverSecurityDo(oarray, st, READ,
			     getNameSpace(nameSpace, objectName), false);
	return comp.getInstance(version, nameSpace, objectName,
				localOnly, ss);
    }
*/
    CIMValue getProperty(String version, CIMNameSpace nameSpace,
            CIMObjectPath objectName, String propertyName,
            SecurityToken st)
            throws CIMException {
        String[] oarray = { version, "getProperty", csg.toString(nameSpace),
                csg.toString(objectName), propertyName };
        ServerSecurity ss = serverSecurityDo(oarray, st, READ,
                getNameSpace(nameSpace, objectName), false);
        return comp.getProperty(version, nameSpace, objectName,
                propertyName, ss);
    }

    CIMQualifierType getQualifierType(String version,
            CIMNameSpace nameSpace,
            CIMObjectPath objectName,
            SecurityToken st)
            throws CIMException {
        String[] oarray = { version, "getQualifierType",
                csg.toString(nameSpace),
                csg.toString(objectName) };
        ServerSecurity ss = serverSecurityDo(oarray, st, READ,
                getNameSpace(nameSpace,
                        objectName), true);
        return comp.getQualifierType(version, nameSpace, objectName,
                ss);
    }

    void deleteClass(String version, CIMNameSpace ns, CIMObjectPath path,
            SecurityToken st)
            throws CIMException {
        String[] oarray = { version, "deleteClass", csg.toString(ns),
                csg.toString(path) };

        ServerSecurity ss = serverSecurityDo(oarray, st, WRITE,
                getNameSpace(ns, path), true);
        comp.deleteClass(version, ns, path, ss);
    }

    void deleteInstance(String version, CIMNameSpace ns,
            CIMObjectPath path, SecurityToken st)
            throws CIMException {
        String[] oarray = { version, "deleteInstance",
                csg.toString(ns), csg.toString(path) };

        ServerSecurity ss = serverSecurityDo(oarray, st, WRITE,
                getNameSpace(ns, path), false);
        comp.deleteInstance(version, ns, path, ss);

    }

    void deleteQualifierType(String version, CIMNameSpace ns,
            CIMObjectPath path, SecurityToken st)
            throws CIMException {
        String[] oarray = { version, "deleteQualifierType",
                csg.toString(ns),
                csg.toString(path) };
        ServerSecurity ss = serverSecurityDo(oarray, st, WRITE,
                getNameSpace(ns, path), true);
        comp.deleteQualifierType(version, ns, path, ss);

    }

    Vector enumNameSpace(String version, CIMNameSpace ns,
            CIMObjectPath path, boolean deep,
            SecurityToken st)
            throws CIMException {
        Boolean bln = (deep ? Boolean.TRUE : Boolean.FALSE);
        String[] oarray = { version, "enumNameSpace",
                csg.toString(ns), csg.toString(path),
                bln.toString() };
        ServerSecurity ss = serverSecurityDo(oarray, st, READ,
                getNameSpace(ns, path), true);

        return comp.enumNameSpace(version, ns, path, deep, ss);

    }

    /*
        Vector enumClass(String version, CIMNameSpace ns, CIMObjectPath path,
                    boolean deep, SecurityToken st)
        throws CIMException {
        Boolean bln = (deep ? Boolean.TRUE : Boolean.FALSE);
        String[] oarray = {version, "enumClass", csg.toString(ns),
                   csg.toString(path), bln.toString()};
        ServerSecurity ss = serverSecurityDo(oarray, st, READ,
                             getNameSpace(ns, path), true);
        return comp.enumClass(version, ns, path, deep, ss);
        }

        Vector enumClass(String version, CIMNameSpace ns, CIMObjectPath path,
                    boolean deep, boolean localOnly, SecurityToken st)
        throws CIMException {
        Boolean bln = (deep ? Boolean.TRUE : Boolean.FALSE);
        String[] oarray = {version, "enumClass", csg.toString(ns),
                   csg.toString(path), bln.toString()};
        ServerSecurity ss = serverSecurityDo(oarray, st, READ,
                             getNameSpace(ns, path), true);
        return comp.enumClass(version, ns, path, deep, localOnly, ss);

        }

        Vector enumInstances(String version, CIMNameSpace ns,
                    CIMObjectPath path, boolean deep,
                    SecurityToken st)
        throws CIMException {
        Boolean bln = (deep ? Boolean.TRUE : Boolean.FALSE);
        String[] oarray = {version, "enumInstances",
                   csg.toString(ns), csg.toString(path),
                   bln.toString()};
        ServerSecurity ss = serverSecurityDo(oarray, st, READ,
                     getNameSpace(ns, path), false);
        return comp.enumInstances(version, ns, path, deep, ss);
        }

        Vector enumInstances(String version, CIMNameSpace ns,
                 CIMObjectPath path, boolean deep,
                 boolean localOnly, SecurityToken st)
        throws CIMException {
        Boolean bln = (deep ? Boolean.TRUE : Boolean.FALSE);
        String[] oarray = {version, "enumInstances",
                   csg.toString(ns), csg.toString(path),
                   bln.toString()};
        ServerSecurity ss = serverSecurityDo(oarray, st, READ,
                             getNameSpace(ns, path), false);
        return comp.enumInstances(version, ns, path, deep, localOnly, ss);
        }
    */
    Vector enumQualifierTypes(String version, CIMNameSpace ns,
            CIMObjectPath path, SecurityToken st)
            throws CIMException {
        String[] oarray = { version, "enumQualifierTypes",
                csg.toString(ns), csg.toString(path) };
        ServerSecurity ss = serverSecurityDo(oarray, st, READ,
                getNameSpace(ns, path), true);
        return comp.enumQualifierTypes(version, ns, path, ss);

    }

    ServerSecurity getServerSecurity(String s)
            throws CIMSecurityException {
        synchronized (sessionMap) {
            ServerSecurity ss = (ServerSecurity) sessionMap.get(s);
            if (ss == null) {// Security exception must be thrown
                throw new CIMSecurityException(
                        CIMSecurityException.NO_SUCH_SESSION, s);
            }
            return ss;
        }
    }

    private void serverSecurityAuthInc(ServerSecurity ss, String[] oarray,
            SecurityToken st)
            throws CIMException {
        ss.authenticateRequest(oarray, st);
        ss.incSessionKey();
    }

    private ServerSecurity serverSecurityDo(String[] oarray, SecurityToken st,
            String rw, String nameSpace,
            boolean verify)
            throws CIMException {
        try {
            ServerSecurity ss =
                    getServerSecurity(new String(st.getSessionId()));
            synchronized (ss) {
                serverSecurityAuthInc(ss, oarray, st);
                return ss;
            }
        } catch (CIMException e) {
            if (verbose) {
                e.printStackTrace();
            }
            throw e;
        }
    }

    private String getNameSpace(CIMNameSpace ns, CIMObjectPath op) {
        if (ns == null) {
            // treat it as an empty namespace
            ns = new CIMNameSpace("", "");
        }
        if (op == null) {
            // treat it as an empty object path
            op = new CIMObjectPath();
        }
        String s = ns.getNameSpace() + '/' + op.getNameSpace();
        CIMNameSpace cns = new CIMNameSpace();
        cns.setNameSpace(s);
        return cns.getNameSpace();
    }

    public void assumeRole(String version,
            String roleName,
            String rolePasswd,
            SecurityToken st) throws CIMException {
        try {
            String[] oarray = { version, "assumeRole", roleName,
                    rolePasswd };
            ServerSecurity ss = getServerSecurity(new String(st.getSessionId()));
            synchronized (ss) {
                ss.authenticateRequest(oarray, st);
                ss.incSessionKey();
                ss.assumeRole(roleName, rolePasswd);
            }
        } catch (CIMException e) {
            throw e;
        } catch (Exception e) {
            throw new CIMException(CIMException.CIM_ERR_FAILED, e);
        }
    }

    public Vector getVersion(String version, SecurityToken st)
            throws CIMException {

        Vector outp = new Vector();
        outp.addElement(new Integer(Version.major));
        outp.addElement(new Integer(Version.minor));
        return outp;
    }

    public Vector performOperations(String version, CIMOperation[] batchedOps,
            SecurityToken st) throws CIMException {
        StringBuffer buff = new StringBuffer();
        Vector retObjs = new Vector();

        for (int i = 0; i < batchedOps.length; i++) {
            buff.append(batchedOps[i].getClass().getName());
        }

        String[] oarray = { version, buff.toString() };
        ServerSecurity ss = serverSecurityDo(oarray, st, READ,
                "", true);

        for (int i = 0; i < batchedOps.length; i++) {
            CIMOperation op = batchedOps[i];

            CIMOMOperation cimomOp =
                    BatchOperationFactory.getCIMOMOperation(
                            comp,
                            ss,
                            op,
                            version);
            cimomOp.run();
            retObjs.addElement(cimomOp.getResult());
        }
        return retObjs;
    }

    public Vector enumerateClasses(String version,
            CIMNameSpace nSpace,
            CIMObjectPath oPath,
            boolean deep,
            boolean localOnly,
            boolean includeQualifiers,
            boolean includeClassOrigin,
            SecurityToken st)
            throws CIMException {

        CIMEnumClassOp cimop = null;
        EnumClassOperation oper = null;

        cimop = new CIMEnumClassOp(oPath,
                deep,
                localOnly,
                includeQualifiers,
                includeClassOrigin);
        cimop.setNameSpace(nSpace);

        CheckSumGen csg = new CheckSumGen();
        String[] oarray = { version, "enumerateClasses",
                csg.toString(nSpace), csg.toString(oPath),
                (deep ? Boolean.TRUE : Boolean.FALSE).toString() };

        ServerSecurity ss = serverSecurityDo(oarray, st, READ,
                getNameSpace(nSpace, oPath), true);

        oper = new EnumClassOperation(comp, ss, cimop, version);

        oper.run();

        Object result = oper.getResult();

        if (result instanceof CIMException) {
            throw (CIMException) result;
        } else if (result instanceof Exception) {
            throw new CIMException(CIMException.CIM_ERR_FAILED, result);
        }

        return (Vector) result;
    }

    public Vector enumerateClassNames(String version,
            CIMNameSpace nSpace,
            CIMObjectPath oPath,
            boolean deep,
            SecurityToken st)
            throws CIMException {

        CIMEnumClassNamesOp cimop = null;
        EnumClassNamesOperation oper = null;

        cimop = new CIMEnumClassNamesOp(oPath, deep);
        cimop.setNameSpace(nSpace);

        CheckSumGen csg = new CheckSumGen();
        String[] oarray = { version, "enumerateClassNames",
                csg.toString(nSpace), csg.toString(oPath),
                (deep ? Boolean.TRUE : Boolean.FALSE).toString() };

        ServerSecurity ss = serverSecurityDo(oarray, st, READ,
                getNameSpace(nSpace, oPath), true);

        oper = new EnumClassNamesOperation(comp, ss, cimop, version);

        oper.run();

        Object result = oper.getResult();

        if (result instanceof CIMException) {
            throw (CIMException) result;
        } else if (result instanceof Exception) {
            throw new CIMException(CIMException.CIM_ERR_FAILED, result);
        }

        return (Vector) result;

    }

    public Vector enumerateInstances(String version,
            CIMNameSpace nSpace,
            CIMObjectPath oPath,
            boolean deep,
            boolean localOnly,
            boolean includeQualifiers,
            boolean includeClassOrigin,
            String[] propertyList,
            SecurityToken st)
            throws CIMException {

        CIMEnumInstancesOp cimop = null;
        EnumInstancesOperation oper = null;

        cimop = new CIMEnumInstancesOp(oPath,
                deep,
                localOnly,
                includeQualifiers,
                includeClassOrigin,
                propertyList);
        cimop.setNameSpace(nSpace);

        CheckSumGen csg = new CheckSumGen();
        String[] oarray = { version, "enumerateInstances",
                csg.toString(nSpace), csg.toString(oPath),
                (deep ? Boolean.TRUE : Boolean.FALSE).toString() };

        ServerSecurity ss = serverSecurityDo(oarray, st, READ,
                getNameSpace(nSpace, oPath), true);

        oper = new EnumInstancesOperation(comp, ss, cimop, version);

        oper.run();

        Object result = oper.getResult();

        if (result instanceof CIMException) {
            throw (CIMException) result;
        } else if (result instanceof Exception) {
            throw new CIMException(CIMException.CIM_ERR_FAILED, result);
        }

        return (Vector) result;
    }

    public Vector enumerateInstanceNames(String version,
            CIMNameSpace nSpace,
            CIMObjectPath oPath,
            SecurityToken st)
            throws CIMException {

        EnumInstanceNamesOperation oper = null;
        CIMEnumInstanceNamesOp cimop = null;

        cimop = new CIMEnumInstanceNamesOp(oPath);
        cimop.setNameSpace(nSpace);

        CheckSumGen csg = new CheckSumGen();
        String[] oarray = { version, "enumerateInstanceNames",
                csg.toString(nSpace), csg.toString(oPath) };

        ServerSecurity ss = serverSecurityDo(oarray, st, READ,
                getNameSpace(nSpace, oPath), true);

        oper = new EnumInstanceNamesOperation(comp, ss, cimop, version);

        oper.run();

        Object result = oper.getResult();

        if (result instanceof CIMException) {
            throw (CIMException) result;
        } else if (result instanceof Exception) {
            throw new CIMException(CIMException.CIM_ERR_FAILED, result);
        }

        return (Vector) result;
    }

    public CIMClass getClass(String version,
            CIMNameSpace nSpace,
            CIMObjectPath oPath,
            boolean localOnly,
            boolean includeQualifiers,
            boolean includeClassOrigin,
            String[] propertyList,
            SecurityToken st)
            throws CIMException {

        GetClassOperation oper = null;
        CIMGetClassOp cimop = null;

        cimop = new CIMGetClassOp(oPath,
                localOnly,
                includeQualifiers,
                includeClassOrigin,
                propertyList);
        cimop.setNameSpace(nSpace);

        CheckSumGen csg = new CheckSumGen();
        String[] oarray = { version, "getClass",
                csg.toString(nSpace), csg.toString(oPath),
                (localOnly ? Boolean.TRUE : Boolean.FALSE).toString() };

        ServerSecurity ss = serverSecurityDo(oarray, st, READ,
                getNameSpace(nSpace, oPath), true);

        oper = new GetClassOperation(comp, ss, cimop, version);

        oper.run();

        Object result = oper.getResult();

        if (result instanceof CIMException) {
            throw (CIMException) result;
        } else if (result instanceof Exception) {
            throw new CIMException(CIMException.CIM_ERR_FAILED, result);
        }

        return (CIMClass) result;
    }

    public CIMInstance getInstance(String version,
            CIMNameSpace nSpace,
            CIMObjectPath oPath,
            boolean localOnly,
            boolean includeQualifiers,
            boolean includeClassOrigin,
            String[] propertyList,
            SecurityToken st)

            throws CIMException {

        GetInstanceOperation oper = null;
        CIMGetInstanceOp cimop = null;

        cimop = new CIMGetInstanceOp(oPath,
                localOnly,
                includeQualifiers,
                includeClassOrigin,
                propertyList);

        cimop.setNameSpace(nSpace);

        CheckSumGen csg = new CheckSumGen();
        String[] oarray = { version, "getInstance",
                csg.toString(nSpace), csg.toString(oPath),
                (localOnly ? Boolean.TRUE : Boolean.FALSE).toString() };

        ServerSecurity ss = serverSecurityDo(oarray, st, READ,
                getNameSpace(nSpace, oPath), true);

        oper = new GetInstanceOperation(comp, ss, cimop, version);

        oper.run();

        Object result = oper.getResult();

        if (result instanceof CIMException) {
            throw (CIMException) result;
        } else if (result instanceof Exception) {
            throw new CIMException(CIMException.CIM_ERR_FAILED, result);
        }

        return (CIMInstance) result;
    }

    public Vector associators(String version, CIMNameSpace nSpace,
            CIMObjectPath oPath,
            String assocClass,
            String resultClass,
            String role,
            String resultRole,
            boolean includeQualifiers,
            boolean includeClassOrigin,
            String[] propertyList,
            SecurityToken st)
            throws CIMException {

        CIMAssociatorsOp cimop = null;
        AssociatorsOperation oper = null;

        cimop = new CIMAssociatorsOp(oPath,
                assocClass,
                resultClass,
                role,
                resultRole,
                includeQualifiers,
                includeClassOrigin,
                propertyList);
        cimop.setNameSpace(nSpace);

        CheckSumGen csg = new CheckSumGen();
        String[] oarray = { version, "associators",
                csg.toString(nSpace), csg.toString(oPath),
                assocClass,
                resultClass, role, resultRole };

        ServerSecurity ss = serverSecurityDo(oarray, st, READ,
                getNameSpace(nSpace, oPath), true);

        oper = new AssociatorsOperation(comp, ss, cimop, version);

        oper.run();

        Object result = oper.getResult();

        if (result instanceof CIMException) {
            throw (CIMException) result;
        } else if (result instanceof Exception) {
            throw new CIMException(CIMException.CIM_ERR_FAILED, result);
        }

        return (Vector) result;
    }

    public Vector associatorNames(String version, CIMNameSpace nSpace,
            CIMObjectPath oPath,
            String assocClass,
            String resultClass,
            String role,
            String resultRole,
            SecurityToken st)
            throws CIMException {

        AssociatorNamesOperation oper = null;
        CIMAssociatorNamesOp cimop = null;

        cimop = new CIMAssociatorNamesOp(oPath,
                assocClass,
                resultClass,
                role,
                resultRole);
        cimop.setNameSpace(nSpace);

        CheckSumGen csg = new CheckSumGen();
        String[] oarray = { version, "associatorNames",
                csg.toString(nSpace), csg.toString(oPath),
                assocClass, resultClass, role, resultRole };

        ServerSecurity ss = serverSecurityDo(oarray, st, READ,
                getNameSpace(nSpace, oPath), true);

        oper = new AssociatorNamesOperation(comp, ss, cimop, version);

        oper.run();

        Object result = oper.getResult();

        if (result instanceof CIMException) {
            throw (CIMException) result;
        } else if (result instanceof Exception) {
            throw new CIMException(CIMException.CIM_ERR_FAILED, result);
        }

        return (Vector) result;
    }

    public Vector references(String version,
            CIMNameSpace nSpace,
            CIMObjectPath oPath,
            String resultClass,
            String role,
            boolean includeQualifiers,
            boolean includeClassOrigin,
            String[] propertyList,
            SecurityToken st)
            throws CIMException {
        ReferencesOperation oper = null;
        CIMReferencesOp cimop = null;

        cimop = new CIMReferencesOp(oPath,
                resultClass,
                role,
                includeQualifiers,
                includeClassOrigin,
                propertyList);
        cimop.setNameSpace(nSpace);

        CheckSumGen csg = new CheckSumGen();
        String[] oarray = { version, "references",
                csg.toString(nSpace), csg.toString(oPath),
                resultClass, role };

        ServerSecurity ss = serverSecurityDo(oarray, st, READ,
                getNameSpace(nSpace, oPath), true);

        oper = new ReferencesOperation(comp, ss, cimop, version);

        oper.run();

        Object result = oper.getResult();

        if (result instanceof CIMException) {
            throw (CIMException) result;
        } else if (result instanceof Exception) {
            throw new CIMException(CIMException.CIM_ERR_FAILED, result);
        }

        return (Vector) result;
    }

    public Vector referenceNames(String version,
            CIMNameSpace nSpace,
            CIMObjectPath oPath,
            String resultClass,
            String role,
            SecurityToken st)
            throws CIMException {

        ReferenceNamesOperation oper = null;
        CIMReferenceNamesOp cimop = null;

        cimop = new CIMReferenceNamesOp(oPath, resultClass, role);
        cimop.setNameSpace(nSpace);

        CheckSumGen csg = new CheckSumGen();
        String[] oarray = { version, "referenceNames",
                csg.toString(nSpace), csg.toString(oPath),
                resultClass, role };

        ServerSecurity ss = serverSecurityDo(oarray, st, READ,
                getNameSpace(nSpace, oPath), true);

        oper = new ReferenceNamesOperation(comp, ss, cimop, version);

        oper.run();

        Object result = oper.getResult();

        if (result instanceof CIMException) {
            throw (CIMException) result;
        } else if (result instanceof Exception) {
            throw new CIMException(CIMException.CIM_ERR_FAILED, result);
        }

        return (Vector) result;
    }

    public void setListener(String version,
            RemoteCIMListener rl,
            SecurityToken st) throws CIMException {

        ServerSecurity ss = getServerSecurity(new String(st.getSessionId()));
        try {
            synchronized (ss) {
                ss.setListener(rl);
            }
        } catch (Exception ex) {
            throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER);
        }
    }
}
