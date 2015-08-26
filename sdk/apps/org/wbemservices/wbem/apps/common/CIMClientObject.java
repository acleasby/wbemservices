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

package org.wbemservices.wbem.apps.common;

import javax.wbem.cim.*;
import javax.wbem.client.CIMClient;
import javax.wbem.client.UserPrincipal;
import javax.wbem.client.PasswordCredential;

/*
 * @(#)CIMClientObject.java	1.24 01/08/17
 *
 * Copyright (c) 08/17/01 Sun Microsystems, Inc.  All Rights Reserved.
 */


public class CIMClientObject {
    private static String hostName = "";
    private static String nameSpace = "";
    private static String userName = "";
    private static String password = "";
    private static String interopNS = "/interop";
    private static CIMClient cimClient = null;
    private final static int READ = 1;
    private final static int WRITE = 2;
    private final static int READ_WRITE = 3;
    private static int userPermission = READ;
    private static String protocol = null;
    private static String latestNameSpace = "";

    private static CIMClient createClient(String hName, 
					  String nSpace, 
					  String uName,
					  String pw) {
	return createClient(hName, nSpace, uName, pw, null);
    }

    private static CIMClient createClient(String hName, 
					String nSpace, 
					String uName,
					String pw,
					String proto) {
	try {
	    CIMNameSpace cimNS;
	    if ((nSpace != null) && (nSpace.trim().length() > 0)) {
		cimNS = new CIMNameSpace(hName, nSpace);
	    } else {		
		cimNS = new CIMNameSpace(hName);
	    }
	    latestNameSpace = cimNS.getNameSpace();
	    UserPrincipal up = null;
	    if ((uName != null) && (uName.trim().length() > 0)) {
		up = new UserPrincipal(uName);
	    }
	    PasswordCredential pc = null;
	    if ((pw != null) && (pw.trim().length() > 0)) {
		pc = new PasswordCredential(pw);
	    }
	    CIMClient cc = new CIMClient(cimNS, up, pc);
	    return cc;
	} catch (CIMException e) {
	    CIMErrorDialog.display(null, e);
	    return null;
	}

    }

    public static CIMClient createInteropClient() {
	CIMClient cc = null;
	if ((hostName != null) && (userName != null) &&
	    (password != null)) {
	    cc = createClient(hostName, interopNS, userName,
			      password, protocol);
				
	}	    
	return cc;
    }

    public static String getInteropNameSpace() {
	return interopNS;
    }

    public static void setInteropNameSpace(String ns) {
	if (ns != null) {
	    interopNS = ns;
	}
    }
						    
    public static CIMClient changeNameSpace(String nSpace) {
	return createClient(hostName, nSpace, 
			       userName, password);
    }	

    public static boolean initialize(String nSpace, String proto) {
	CIMClient cc = createClient(hostName, nSpace, userName, password, 
				    proto);
	if (cc != null) {
	    protocol = proto;
	    cimClient = cc;
	    nameSpace = latestNameSpace;
	    initializeUserPermission(nameSpace, userName);
	    return true;
	} else {
	    return false;
	}
    }

    public static boolean initialize(String nSpace) {
	return initialize(nSpace, protocol);
    }

    public static boolean initialize(String hName,
				     String nSpace,
				     String uName,
				     String pw,
				     String proto) {
	CIMClient cc = createClient(hName, nSpace, uName, pw, proto);
	if (cc != null) {
	    cimClient = cc;
	    protocol = proto;
	    hostName = hName;
	    nameSpace = latestNameSpace;
	    userName = uName;
	    password = pw;
	    initializeUserPermission(nameSpace, uName);
	    return true;
	} else {
	    return false;
	}

    }

    public static boolean initialize(String hName,
				     String nSpace,
				     String uName,
				     String pw) {
	return initialize(hName, nSpace, uName, pw, protocol);
    }
    
    /**
     * determines if we used the XML protocol to make the CIMClient connection 
     *
     * @return boolean  true if we used the XML protocol, otherwise false
     */
    public static boolean isXML() {
	return protocol.equals(CIMClient.CIM_XML);
    }

    /**
     * Returns the name of the user that was used to make the CIMClient
     * connection
     *
     * @return String  the user name
     */
    public static String getUserName() {
	return userName;
    }

    /**
     * Returns the name of the host that was used to make the CIMClient
     * connection
     *
     * @return String  the host name
     */
    public static String getHostName() {
	return hostName;
    }

    /**
     * Returns the name of the namespace that this CIMClient is connected to
     *
     * @return String  the namespace
     */
    public static String getNameSpace() {
	return nameSpace;
    }


    /**
     * Returns the CIMClient object
     *
     * @return CIMClient  the CIMClient that we used to connect to the CIM Object
     *                    Manager
     */
    public static CIMClient getClient() {
	return cimClient;
    }


    /**
     * Used to determine if user that was used to make this CIMClient connection
     * has read permissions to the namespace it is connected to
     *
     * @return boolean  true if the user used to connect has read privileges to
     *                  the connected namespace
     */
    public static boolean userHasReadPermission() {
	return ((userPermission & READ) > 0);
    }

    /**
     * Used to determine if user that was used to make this CIMClient connection
     * has write permissions to the namespace it is connected to
     *
     * @return boolean  true if the user used to connect has write privileges to
     *                  the connected namespace
     */
    public static boolean userHasWritePermission() {
	return ((userPermission & WRITE) > 0);
    }

    private static void initializeUserPermission(String nSpace, String uName) {
	userPermission = getUserPermission(nSpace, uName);    
    }

    // gets the permissions a user has to a particular namespace.   Return
    // values can be READ, WRITE, READ_WRITE or NONE
    private static int getUserPermission(String nSpace, String uName) {
	// since there currently is no interoperatable way to determine 
	// name space permissions, we will always return READ_WRITE.  This
	// function will be implemented when the functionality is available
	return READ_WRITE;
    }
}
