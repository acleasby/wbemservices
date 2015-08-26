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

package org.wbemservices.wbem.cimom;


import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMObjectPath;
import javax.wbem.cim.CIMInstance;
import javax.wbem.cim.CIMProperty;
import javax.wbem.cim.CIMValue;
import javax.wbem.query.SelectExp;
import javax.wbem.client.Debug;

import java.util.Vector;
import java.util.Map;
import java.util.TreeMap;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Collection;
import java.util.Iterator;

/*
 * This class takes in activations that must be polled for. It handles polling
 * for instance modification, creation and deletions.
 */
class IndicationPoller {

    private final static int ADDTRIGGERFLAG = 1;
    private final static int MODIFYTRIGGERFLAG = 2;
    private final static int DELETETRIGGERFLAG = 4;
    private javax.wbem.client.ProviderCIMOMHandle pch = null;

    // Using tree map to save space
    Map classMap = new TreeMap();

    IndicationPoller(javax.wbem.client.ProviderCIMOMHandle pch) {
	this.pch = pch;
    }

    private class PollerThread extends Thread {
	private int flag = 0;
	private CIMObjectPath classPath;
	private Enumeration pve;
	private Map instanceMap;

	synchronized void setFlag(int flag) {
	    this.flag = flag;
	}

	synchronized int getFlag() {
	    return flag;
	}

	PollerThread (int flag, String tns, String className) {
	    try {
		LogFile.add(LogFile.DEVELOPMENT, "DEBUG_VALUE", 
		"New poller thread flag for class", className);
	    } catch (CIMException e) {
	    }
	    this.flag = flag;
	    classPath = new CIMObjectPath(className);
	    classPath.setNameSpace(tns);
	}

	private boolean instanceEqual(CIMInstance inst1, CIMInstance inst2) {
	    if (inst1 == null) {
		if (inst2 != null) {
		    return false;
		} else {
		    return true;
		}
	    } else {
		if (inst2 == null) {
		    return false;
		} 
	    }
	    Enumeration enum = inst1.getProperties().elements();
	    while (enum.hasMoreElements()) {
		CIMProperty cp = (CIMProperty)enum.nextElement();
		CIMValue v1 = cp.getValue();
		CIMValue v2 = null;
		try {
		    v2 = inst2.getProperty(cp.getName()).getValue();
		    if (v1 == null) {
			if (v2 != null) {
			    return false;
			}
		    } else {
			if (v2 == null) {
			    return false;
			} else {
			    if (!v1.equals(v2)) {
				return false;
			    }
			}
		    }
		} catch (Exception e) {
		    // property not found
                    Debug.trace2("Caught Exception in IndicationPoller.instanceEqual", e);
		    return false;
		}
	    }
	    return true;
	}

	public void run() {

	    try {
		//pve = pch.enumInstances(classPath, false, false);
		pve = pch.enumerateInstances(classPath, false, false, true,
					     true, null);
	    } catch (Exception e) {
		// Must log this, should we return, or continue?
		// Returning for now, must deactivate this activation
		e.printStackTrace();
		try {
		    LogFile.add(LogFile.DEVELOPMENT, "DEBUG_VALUE", 
			"Exception in poll", e);
		} catch (CIMException e1) {
		}
	    }

	    do {
		try {
		    Thread.sleep(EventService.pollInterval);
		} catch (InterruptedException ie) {
		    // Go back and check for flag
		}

		int tflag = getFlag();
		if (tflag == 0) {
		    // no more need to poll
		    try {
			LogFile.add(LogFile.DEVELOPMENT, "DEBUG_VALUE", 
			"Stopping poll for", classPath);
		    } catch (CIMException e) {
		    }
		    return;
		}

		boolean addPoll = (tflag & ADDTRIGGERFLAG) != 0;
		boolean deletePoll = (tflag & DELETETRIGGERFLAG) != 0;
		boolean modifyPoll = (tflag & MODIFYTRIGGERFLAG) != 0;
		String tns = classPath.getNameSpace();

		Enumeration cve = null;
		try {
		    //cve = pch.enumInstances(classPath, false, false);
		    cve = pch.enumerateInstances(classPath, false, false, true,
						 true, null);
		} catch (Exception e) {
		    e.printStackTrace();
		    // Must log this, should we return, or continue?
		    // We continue for now.
		    try {
			LogFile.add(LogFile.DEVELOPMENT, "DEBUG_VALUE", 
			"Exception in poll", e);
		    } catch (CIMException e1) {
		    }
		    continue;
		}

		instanceMap = new HashMap();
		while (pve.hasMoreElements()) {
		    CIMInstance ci = (CIMInstance)pve.nextElement();
		    // shouldnt have to do this - the provider is misbehaving
		    instanceMap.put(ci.getObjectPath(), ci);
		}
		Vector previousVector = new Vector();
		while (cve.hasMoreElements()) {
		    CIMInstance ci = (CIMInstance)cve.nextElement();
		    previousVector.addElement(ci);
		    CIMInstance pci = 
		    (CIMInstance)instanceMap.remove(ci.getObjectPath());
		    if (addPoll && pci == null) {
			// this is a new instance
			CIMInstance indication = new CIMInstance();
			indication.setClassName(
			FilterActivation.INSTANCEADDITION);
			CIMProperty cp = new CIMProperty();
			cp.setName("SourceInstance");
			cp.setValue(new CIMValue(ci));
			Vector v = new Vector();
			v.addElement(cp);
			indication.setProperties(v);
			EventService.eventService.deliverEvent(tns, 
			indication, true);
		    }

		    if (modifyPoll && (pci != null)) {
			// this is a modify
			// Must do there is a slight problem here - we first 
			// need to check if at least one property has been 
			// modified only then can we generate the modification.
			if (instanceEqual(pci, ci)) {
			    continue;
			}
			CIMInstance indication = new CIMInstance();
			indication.setClassName(
			FilterActivation.INSTANCEMODIFICATION);
			CIMProperty cp = new CIMProperty();
			cp.setName("SourceInstance");
			cp.setValue(new CIMValue(ci));
			Vector v = new Vector();
			v.addElement(cp);
			cp = new CIMProperty();
			cp.setName("PreviousInstance");
			cp.setValue(new CIMValue(pci));
			v.addElement(cp);
			indication.setProperties(v);
			EventService.eventService.deliverEvent(tns, 
			indication, true);
		    }
		}

		pve = previousVector.elements();

		if (!deletePoll) {
		    continue;
		}

		// Whatever is left over in the instanceMap have been deleted
		Collection values = instanceMap.values();
		Iterator i = values.iterator();

		while (i.hasNext()) {
		    CIMInstance ci = (CIMInstance)i.next();
		    CIMInstance indication = new CIMInstance();
		    indication.setClassName(
		    FilterActivation.INSTANCEDELETION);
		    CIMProperty cp = new CIMProperty();
		    cp.setName("SourceInstance");
		    cp.setValue(new CIMValue(ci));
		    Vector v = new Vector();
		    v.addElement(cp);
		    indication.setProperties(v);
		    EventService.eventService.deliverEvent(tns, 
		    indication, true);
		}
	    } while (true);
	}
    }

    private void doActivation(FilterActivation.SubActivation sa,
    int flag) throws CIMException {
	String tns = 
	sa.getParentActivation().getTargetNameSpace().getNameSpace();
	String fullClassName = tns + ":" + sa.getClassName();
	PollerThread pt = (PollerThread)classMap.get(fullClassName);
	if (pt == null) {
	    // This class has no polling on it yet.

	    // This could be an array of providers in the future, if we allow
	    // multiple providers for a class
	    pt = new PollerThread(flag, tns, sa.getClassName());
	    classMap.put(fullClassName, pt);
	    pt.start();
	} else {
	    pt.setFlag(pt.getFlag() | flag);
	}
    }

    private void doDeActivation(FilterActivation.SubActivation sa,
    int flag) throws CIMException {
	String tns = 
	sa.getParentActivation().getTargetNameSpace().getNameSpace();
	String fullClassName = tns + ":" + sa.getClassName();
	PollerThread pt = (PollerThread)classMap.get(fullClassName);
	if (pt == null) {
	    // Shouldnt happen, ignore for now
	    return;
	} else {
	    int currFlag = pt.getFlag();
	    if ((currFlag & flag) == 0) {
		// This means the flag was not set, this is an error condition,
		// we shouldnt be trying to deactivate something we didnt
		// activate. Ignore for now.
	    } else {
		pt.setFlag(currFlag - flag);
		if (pt.getFlag() == 0) {
		    // This poller thread is no longer needed
		    classMap.remove(fullClassName);
		}
	    }
	}
    }

    void additionTriggerActivate(FilterActivation.SubActivation sa)
    throws CIMException {
	doActivation(sa, ADDTRIGGERFLAG);
    }

    void modificationTriggerActivate(FilterActivation.SubActivation sa)
    throws CIMException {
	doActivation(sa, MODIFYTRIGGERFLAG);
    }

    void deletionTriggerActivate(FilterActivation.SubActivation sa)
    throws CIMException {
	doActivation(sa, DELETETRIGGERFLAG);
    }

    void additionTriggerDeActivate(FilterActivation.SubActivation sa)
    throws CIMException {
	doDeActivation(sa, ADDTRIGGERFLAG);
    }

    void modificationTriggerDeActivate(FilterActivation.SubActivation sa)
    throws CIMException {
	doDeActivation(sa, MODIFYTRIGGERFLAG);
    }

    void deletionTriggerDeActivate(FilterActivation.SubActivation sa) 
    throws CIMException {
	doDeActivation(sa, DELETETRIGGERFLAG);
    }

    public void authorizeFilter(SelectExp filter, String eventType, 
    CIMObjectPath classPath, String owner) throws CIMException {
	//pch.enumInstances(classPath, false, false);
	pch.enumerateInstances(classPath, false, false, true, true, null);
    }
}
