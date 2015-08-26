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

import javax.wbem.cim.*;
import javax.wbem.client.*;
import javax.wbem.provider.*;

import java.util.*;


/**
 * @author     Sun Microsystems, Inc.
 * @since      WBEM 1.0
 */
public class SimpleInstanceProvider implements CIMInstanceProvider {

    private static int loop = 0;

    public void initialize(CIMOMHandle cimom) 
    throws CIMException {
    }

    public void cleanup() 
    throws CIMException {
    }

    public CIMInstance[] enumerateInstances(CIMObjectPath op, 
					      boolean localOnly, 
					      boolean includeQualifiers, 
					      boolean includeClassOrigin, 
					      String[] propList,
					      CIMClass cc)
    throws CIMException {
	return null;
    }

    /*
     * enumInstances:
     * The instance names are returned.
     * Deep or shallow enumeration is possible, however
     * currently the CIMOM only asks for shallow enumeration.
     */
    public CIMObjectPath[] enumerateInstanceNames(CIMObjectPath op, CIMClass cc)
    throws CIMException {
	if (op.getObjectName().equalsIgnoreCase("Ex_SimpleInstanceProvider")) {

	    Vector instances = new Vector();
	    CIMObjectPath cop = new CIMObjectPath(op.getObjectName(),
						 op.getNameSpace());
	    if (loop == 0) {
		cop.addKey("First", new CIMValue("red"));
		cop.addKey("Last", new CIMValue("apple"));
		instances.addElement(cop);
		loop += 1;
	    } else {
		cop.addKey("First", new CIMValue("red"));
		cop.addKey("Last", new CIMValue("apple"));
		instances.addElement(cop);

		cop = new CIMObjectPath(op.getObjectName(),
						 op.getNameSpace());
		cop.addKey("First", new CIMValue("green"));
		cop.addKey("Last", new CIMValue("apple"));
		instances.addElement(cop);
	    }

	    CIMObjectPath[] copArray = new CIMObjectPath[instances.size()];
	    instances.toArray(copArray);
	    return copArray;
	}
	return null;
    }

    public CIMInstance getInstance(CIMObjectPath op, 
				   boolean localOnly,
				   boolean includeQualifiers,
				   boolean includeClassOrigin,
				   String[] propertyList,
				   CIMClass cc)
    throws CIMException {
	if (op.getObjectName().equalsIgnoreCase("Ex_SimpleInstanceProvider")) {
	    CIMInstance ci = cc.newInstance();
	    ci.setProperty("First", new CIMValue("yellow"));
	    ci.setProperty("Last", new CIMValue("apple"));
	    if (localOnly) {
		ci = ci.localElements();
	    }
	    return ci.filterProperties(propertyList, 
					includeQualifiers,
					includeClassOrigin);
	}
	return new CIMInstance();
    }

    public CIMInstance[] execQuery(CIMObjectPath op, String query, 
				   String ql, CIMClass cc)
    throws CIMException {
	throw(new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED));
    }

    public void setInstance(CIMObjectPath op, CIMInstance ci, 
	boolean includeQualifiers, String[] propList)
		throws CIMException {
	throw(new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED));
    }

    public CIMObjectPath createInstance(CIMObjectPath op, CIMInstance ci)
		throws CIMException {
	throw(new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED));
    }

    public void deleteInstance(CIMObjectPath cp)
		throws CIMException {
	throw(new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED));
    }
}

