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

// This file contains methods which should actually be static methods on the
// CIMNameSpace class. It deals with creation and deletion of 
// CIMNameSpace instances - but we are separating it from CIMNameSpace
// since the methods will only work within the context of the CIMOM and not
// in the context of a client application which will be using the 
// CIMNameSpace class.

package org.wbemservices.wbem.cimom;

import java.util.Vector;
import java.util.StringTokenizer;

import javax.wbem.cim.CIMNameSpace;
import javax.wbem.cim.CIMObjectPath;
import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMNameSpaceException;

class CIMNSStaticMethods {

    private static String getNameSpace(CIMNameSpace ns, CIMObjectPath op) {
	String s = ns.getNameSpace() + '/' + op.getNameSpace();
	CIMNameSpace cns = new CIMNameSpace();
	cns.setNameSpace(s);
	return cns.getNameSpace();
    }

    static void createNameSpace(CIMNameSpace parent, CIMNameSpace nameSpace)
    throws CIMException {

	LogFile.methodEntry("createNameSpace");
	parent.setNameSpace('/'+parent.getNameSpace());
	CIMNameSpace validNS = new CIMNameSpace();
	validNS.setNameSpace(parent.getNameSpace()+'/'+
			     nameSpace.getNameSpace());
	LogFile.add(LogFile.DEBUG, "CREATE_NAMESPACE", validNS.getNameSpace());
	StringTokenizer p = new StringTokenizer(validNS.getNameSpace(), "/");
	String token = null;
	CIMException finalException = null;
	if (p.hasMoreTokens()) {
	    token = '/'+p.nextToken();
	    try {
		CIMOMImpl.ps.createNameSpace(token);
	    } catch (CIMException e) {
		finalException = e;
	    }
	} else {
	    LogFile.methodReturn("createNameSpace");
	    throw new CIMNameSpaceException(
	              CIMNameSpaceException.CIM_ERR_INVALID_PARAMETER,
		      validNS.getNameSpace());
	}

	while (p.hasMoreTokens()) {
	    token = token+'/'+p.nextToken();
	    finalException = null;
	    try {
		CIMOMImpl.ps.createNameSpace(token);
	    } catch (CIMException e) {
		finalException = e;
	    }
	}
	if (finalException != null) {
	    LogFile.methodReturn("createNameSpace");
	    throw finalException;
	}
	LogFile.methodReturn("createNameSpace");
    }

    static void deleteNameSpace(CIMNameSpace parent,
			CIMNameSpace nameSpace)
    throws CIMException {
	LogFile.methodEntry("deleteNameSpace");
	parent.setNameSpace('/'+parent.getNameSpace());
	CIMNameSpace validNS = new CIMNameSpace();
	validNS.setNameSpace(parent.getNameSpace()+'/'+
				nameSpace.getNameSpace());
	LogFile.add(LogFile.DEBUG, "DELETE_NAMESPACE", validNS.getNameSpace());
	try {
	    CIMOMImpl.ps.deleteNameSpace(validNS.getNameSpace());
	} catch (CIMException e) {
	    LogFile.methodReturn("deleteNameSpace");
	    if (CIMOMImpl.verbose) {
		e.printStackTrace();
	    }
	    throw e;
	} catch (Exception e) {
		e.printStackTrace();
		throw new CIMException(CIMException.CIM_ERR_FAILED, e);
	}
	LogFile.methodReturn("deleteNameSpace");
    }

    static Vector enumNameSpace(CIMNameSpace ns, CIMObjectPath path, 
				boolean deep)
    throws CIMException {
	ns.setNameSpace('/'+ns.getNameSpace());
	Vector v;
	String nameSpace = getNameSpace(ns, path);
	v = CIMOMImpl.ps.enumerateNameSpace(nameSpace, deep);
	Vector v2 = new Vector();
	int k = v.size();
	for (int i = 0; i < k; i++) {
	    v2.addElement(new CIMObjectPath("", (String)v.elementAt(i)));
	}
	return v2;
    }


}

