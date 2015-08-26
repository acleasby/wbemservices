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

package org.wbemservices.wbem.cimom;

import javax.wbem.cim.*;
import javax.wbem.client.Debug;

/**
 * Passed by the CIMOM to the MOF compiler during mof registry and unregistry.
 * The MOF compiler calls this class when it wants to manipulate information
 * in the CIMOM - the call is in turn forwarded to the CIMOM as the appropriate 
 * create/delete/modify calls. It logs all the actions done during the registry
 * in an Unregistry handler, which in turn takes appropriate actions to handle
 * unregistry.
 * This class essentially calls its super class methods and logs the actions
 * that it performed.
 *
 * @author      Sun Microsystems, Inc.
 * @since       WBEM 2.5
 */


class MofregClient extends ProviderClient {

    private int mode;
    // The unreghandler handles all the processing to undo the registry 
    // operations. This class logs the required info to perform the undo
    // into the unreghandler.
    private UnregHandler uh;
    private boolean mustLog = true;

    // This method logs the exception that has occured.
    private void logException(Exception e) {
	CIMOMLogService ls = (CIMOMLogService)
	ServiceRegistry.getService(CIMOMLogService.DEFAULT);
	if (ls == null) {
	    return;
	}
	try {
	    ls.writeLog("Mofreg", "MOFREG_ERROR", "MOFREG_ERROR", null,
	    e.toString(), false, CIMOMLogService.SYSTEM_LOG,
	    CIMOMLogService.WARNING, null);
	} catch (Exception le) {
	    // Ignore the exception
	    Debug.trace2("logging error", le);
	}
    }

    // The constructor takes in the mode in which the mof registry runs -
    // that is, is this a register, or an unregister operation. 
    // @param cimom Handle to the  CIMOM.
    // @param mode  REGMODE or UNREGMODE.
    // @param uh    Handler to which registry actions are passed.
    public MofregClient(CIMOMImpl cimom, int mode, UnregHandler uh)
    throws CIMException {
	super(cimom, new CIMNameSpace("", ""));
	this.mode = mode;
	this.uh = uh;
	if (uh == null) {
	    // There is no unreg handler, forget logging.
	    mustLog = false;
	}
    }

    public void createNameSpace(CIMNameSpace ns)
    throws CIMException {
	super.createNameSpace(ns);
	// XXX must log this action??
    }

    public void deleteNameSpace(CIMNameSpace ns) throws CIMException {
	super.createNameSpace(ns);
	// XXX must log this action??
    }

    // Not calling the superclass delete method. We'll instead call the
    // new removeClass method that was introduced for mof unreg.
    public void deleteClass(CIMObjectPath path)	 
    throws CIMException {
	Debug.trace3("In mofreg client deleteclass");
	cimom.intMofregRemoveClass(nameSpace, path);
	// Since deleteClass is never done during mofreg, we dont log it -
	// it never has to be undone.
    }


    // Not calling the superclass delete method. We'll instead call the
    // new mofregDeleteInstance method that was introduced for mof unreg.
    public void deleteInstance(CIMObjectPath path)
    throws CIMException {
	Debug.trace3("In mofreg client deleteinstance");
	CIMInstance oldci = null;
	Debug.trace3("path for delete is "+path);
	// We need to fix the key types, especially object paths.
	// First get the class
	CIMClass cc = super.getClass(path, false);
	CIMInstance nci;
	// Create a template instance
	nci = cc.newInstance();
	// Populate its properties with those in ci
	nci.updatePropertyValues(path.getKeys());
	
	// The instance sanity check will do the proper key type conversion
	cimom.checkInstanceSanity(path.getNameSpace(), nci, cc);
	// Ok now update the path with the proper keys.
	path.setKeys(nci.getKeys());
	// Retrieve this instance because we need to store the
	// old instance being deleted.
	oldci = super.getInstance(path, false);
	if (oldci == null) {
	    throw new CIMInstanceException(CIMException.CIM_ERR_NOT_FOUND,
					   path.toString());
	}
	cimom.intdeleteInstance(nameSpace, path, true);
	
	if (mustLog) {
	    CIMObjectPath ns = new CIMObjectPath();
	    ns.setNameSpace(path.getNameSpace());
	    ns.setObjectName(path.getObjectName());
	    uh.deleteInstance(ns, oldci);
	}
    }

    public void createQualifierType(CIMObjectPath name, CIMQualifierType qt)
    throws CIMException {
	super.createQualifierType(name, qt);
	// XXX must log this action??
    }

    // for this method, we do not invoke the superclass set method. We invoke 
    // the modify class method.
    public void setClass(CIMObjectPath name, CIMClass cc)
    throws CIMException {
	CIMClass oldcc = null;
	try {
	    CIMObjectPath tname = new CIMObjectPath();
	    tname.setNameSpace(name.getNameSpace());
	    tname.setObjectName(cc.getName());
	    Debug.trace3("Trying to get old class "+tname);
	    // Get only the local elements
	    oldcc = super.getClass(tname, true);
	} catch (CIMException e) {
	    if (e.getID().equals(CIMException.CIM_ERR_NOT_FOUND)) {
		// Log this as a warning
		logException(e);
		return;
	    } else {
		throw e;
	    }
	}
	Debug.trace3("The old class is "+oldcc);
	cimom.intModifyClass(nameSpace, name, cc);
	// Log set class operation.
	if (mustLog) {
	    uh.setClass(name, oldcc, cc);
	}
	
    }

    public void createClass(CIMObjectPath name, CIMClass cc)
    throws CIMException {
        try {
	    super.createClass(name, cc);
	} catch (CIMException e) {
	    if (e.getID().equals(CIMException.CIM_ERR_ALREADY_EXISTS)) {
		// log this as a warning
		logException(e);
		return;
	    } else {
		throw e;
	    }
	}
	if (mustLog) {
	    uh.createClass(name, cc);
	}
    }

    public void setInstance(CIMObjectPath name, CIMInstance ci)
    throws CIMException {
	Debug.trace3("In mofreg client setinstance");
	CIMInstance oldci = null;
	
	// We need to fix the key types, especially object paths.
	// First get the class
	CIMObjectPath path = new CIMObjectPath();
	path.setObjectName(ci.getClassName());
	path.setNameSpace(name.getNameSpace());
	CIMClass cc = super.getClass(path, false);
	CIMInstance nci;
	// Create a template instance
	nci = cc.newInstance();
	// Populate its properties with those in ci
	nci.updatePropertyValues(ci.getProperties());
	
	// The instance sanity check will do the proper key type conversion
	cimom.checkInstanceSanity(path.getNameSpace(), nci, cc);
	// Ok now update the path with the proper keys.
	path.setKeys(nci.getKeys());
	// Retrieve this instance because we need to store the
	// old instance.
	oldci = super.getInstance(path, false);
	if (oldci == null) {
	    throw new CIMInstanceException(CIMException.CIM_ERR_NOT_FOUND,
					   path.toString());
	}
	cimom.intsetCIMElement(nameSpace, name, ci, true, true, null, true);
	if (mustLog) {
	    CIMObjectPath ns = new CIMObjectPath();
	    ns.setNameSpace(name.getNameSpace());
	    ns.setObjectName(name.getObjectName());
	    uh.setInstance(ns, oldci, ci);
	}
	// XXX log this action for undo??
    }

    public CIMObjectPath createInstance(CIMObjectPath name, CIMInstance ci)
    throws CIMException {
	CIMObjectPath op = super.createInstance(name, ci);
	if (op != null) {
	    // Keys have changed, we need the latest instance.
	    ci = super.getInstance(name, false);
	}
	// log this action for undo
	if (mustLog) {
	    uh.createInstance(name, ci);
	}
	return op;
    }
}
