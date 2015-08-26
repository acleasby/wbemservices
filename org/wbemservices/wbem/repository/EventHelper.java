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
 *are Copyright © 2002 Sun Microsystems, Inc.
 *
 *All Rights Reserved.
 *
 *Contributor(s): WBEM Solutions, Inc.
 */

package org.wbemservices.wbem.repository;

import java.util.*;

import javax.wbem.client.*;
import javax.wbem.cim.*;
import javax.wbem.cim.CIMException;
import javax.wbem.query.*;
import org.wbemservices.wbem.cimom.*;

/*
 * CIMWBEM Event Delivery methods for use by repository
 */
public class EventHelper {

    public static boolean verbose = false;
    
    private Map classIndicationMap = Collections.synchronizedMap(new HashMap());
    private ReadersWriter concurrentObj;

    /*
     * Constructor 
     */
    public EventHelper(ReadersWriter concurrentObj) {
        this.concurrentObj = concurrentObj;
    }

    /*
     * Deliver Class Creation event if handler exists.
     *
     * @param namespace the namespace where to deliver event
     * @param cc	the CIMClass object representing the class
     */
    public void classCreation(String namespace, CIMClass cc) {

	try {
	    if (classIndicationMap.get(namespace+"::"+
				    FilterActivation.CLASSCREATION) != null) {

		CIMInstance indication = new CIMInstance();
		indication.setClassName(FilterActivation.CLASSCREATION);

		CIMProperty cp = new CIMProperty();
		cp.setName("ClassDefinition");
		cp.setValue(new CIMValue(cc));
		Vector v = new Vector();
		v.addElement(cp);
		    
		cp = new CIMProperty();
		cp.setName("IndicationTime");
		cp.setType(CIMDataType.getPredefinedType(CIMDataType.DATETIME));
		v.addElement(cp);

		indication.setProperties(v);
		EventService.eventService.deliverEvent(namespace, indication);
	    }
	} catch (Exception e) {
	    Debug.trace2("classCreation Event Exception:\n" + e.getMessage());
	}
    }

    /*
     * Deliver Class Deletion event if handler exists.
     *
     * @param namespace the namespace where to deliver event
     * @param cc	the CIMClass object representing the class
     */
    public void classDeletion(String namespace, CIMClass cc) {

	try {
	    if (classIndicationMap.get(namespace+"::"+
		FilterActivation.CLASSDELETION) != null) {

		CIMInstance indication = new CIMInstance();
		indication.setClassName(FilterActivation.CLASSDELETION);
		CIMProperty cp = new CIMProperty();
		cp.setName("ClassDefinition");
		cp.setValue(new CIMValue(cc));
		Vector v = new Vector();
		v.addElement(cp);

		cp = new CIMProperty();
		cp.setName("IndicationTime");
		cp.setType(CIMDataType.getPredefinedType(CIMDataType.DATETIME));
		v.addElement(cp);

		indication.setProperties(v);
		EventService.eventService.deliverEvent(namespace, indication);
	    }
	} catch (Exception e) {
	    Debug.trace2("classDeletion Event Exception:\n" + e.getMessage());
	}
    }

    /*
     * Deliver Class Modification event if handler exists.
     *
     * @param namespace the namespace where to deliver event
     * @param oldcc	the CIMClass object representing the original class
     * @param cc	the CIMClass object representing the new class
     */
     public void classModification(String namespace, CIMClass oldcc, 
				   CIMClass cc) {

	try {
	    if (classIndicationMap.get(namespace+"::"+
				FilterActivation.CLASSMODIFICATION) != null) {

		 CIMInstance indication = new CIMInstance();
		 indication.setClassName(FilterActivation.CLASSMODIFICATION);

		 CIMProperty cp = new CIMProperty();
		 cp.setName("ClassDefinition");
		 cp.setValue(new CIMValue(cc));
		 Vector v = new Vector();
		 v.addElement(cp);
		 
		 cp = new CIMProperty();
		 cp.setName("IndicationTime");
		 cp.setType(CIMDataType.getPredefinedType(CIMDataType.DATETIME));
		 v.addElement(cp);
		 
		 cp = new CIMProperty();
		 cp.setName("PreviousClassDefinition");
		 cp.setValue(new CIMValue(oldcc));
		 v.addElement(cp);

		 indication.setProperties(v);
		 EventService.eventService.deliverEvent(namespace, indication);
	    }
	} catch (Exception e) {
	    Debug.trace2("classModification Event Exception:\n" + 
			e.getMessage());
	}
     }
     
    /*
     * Deliver Instance Read event if handler exists.
     *
     * @param namespace the namespace where to deliver event
     * @param ci	the CIMInstance object representing the instance
     */
    public void instanceRead(String namespace, CIMInstance ci) {

	try {
	    if (ci != null && classIndicationMap.get(namespace+":"+
				    ci.getClassName().toLowerCase()+":"+
				    FilterActivation.INSTANCEREAD) != null) {

		CIMInstance indication = new CIMInstance();
		indication.setClassName(FilterActivation.INSTANCEREAD);
		CIMProperty cp = new CIMProperty();
		cp.setName("SourceInstance");
		cp.setValue(new CIMValue(ci));
		cp.setType(CIMDataType.getPredefinedType(CIMDataType.STRING));
		Vector v = new Vector();
		v.addElement(cp);
		indication.setProperties(v);
		EventService.eventService.deliverEvent(namespace, indication);
	    }
	} catch (Exception e) {
	    Debug.trace2("instanceRead Event Exception:\n" + e.getMessage());
	}
    }

    /*
     * Deliver Instance Read event for enumerated instances if handler exists.
     *
     * @param op		the CIMObjectPath of the instances
     * @param instanceList	the vector containing the enumerated instances
     */
    public void instanceEnumerate(CIMObjectPath op, final Vector instanceList) {

	try {
	    final String localns = op.getNameSpace().toLowerCase();
	    if (classIndicationMap.get(localns+":"+
				    op.getObjectName().toLowerCase()+":"+
				    FilterActivation.INSTANCEREAD) != null) {
		new Thread() {
		    public void run() {
			Enumeration e1 = instanceList.elements();
			while (e1.hasMoreElements()) {
			    CIMInstance ci = (CIMInstance)e1.nextElement();
			    instanceRead(localns, ci);
			}
		    }
		}.start();

	    }
	} catch (Exception e) {
	    Debug.trace2("instanceEnumerate Event Exception:\n" + 
			e.getMessage());
	}
    }

    /*
     * Deliver Instance Addition event if handler exists.
     *
     * @param namespace the namespace where to deliver event
     * @param ci	the CIMInstance object representing the instance
     */
    public void instanceAddition(String namespace, CIMInstance ci) {

	try {
	    if (additionTriggerSet.contains(namespace+":"+
					    ci.getClassName().toLowerCase())) {

		CIMInstance indication = new CIMInstance();
		indication.setClassName(FilterActivation.INSTANCEADDITION);

		CIMProperty cp = new CIMProperty();
		cp.setName("SourceInstance");
		cp.setValue(new CIMValue(ci));
		cp.setType(CIMDataType.getPredefinedType(CIMDataType.STRING));
		Vector v = new Vector();
		v.addElement(cp);
		indication.setProperties(v);
		EventService.eventService.deliverEvent(namespace, indication);
	    }
	} catch (Exception e) {
	    Debug.trace2("instanceAddition Event Exception:\n" + 
			e.getMessage());
	}
    }

    /*
     * Deliver Instance Deletion event if handler exists.
     *
     * @param namespace the namespace where to deliver event
     * @param ci	the CIMInstance object representing the instance
     */
    public void instanceDeletion(String namespace, CIMInstance ci) {

	try {
	    if (deletionTriggerSet.contains(namespace+":"+
					    ci.getClassName().toLowerCase())) {

		CIMInstance indication = new CIMInstance();
		indication.setClassName(FilterActivation.INSTANCEDELETION);
		CIMProperty cp = new CIMProperty();
		cp.setName("SourceInstance");
		cp.setValue(new CIMValue(ci));
		cp.setType(CIMDataType.getPredefinedType(CIMDataType.STRING));
		Vector v = new Vector();
		v.addElement(cp);
		indication.setProperties(v);
		EventService.eventService.deliverEvent(namespace, indication);
	    }
	} catch (Exception e) {
	    Debug.trace2("instanceDeletion Event Exception:\n" + 
			e.getMessage());
	}
    }

    /*
     * Deliver Instance Modification event if handler exists.
     *
     * @param namespace the namespace where to deliver event
     * @param ci	the CIMInstance object representing the old instance
     * @param ci	the CIMInstance object representing the new instance
     */
    public void instanceModification(String namespace, CIMInstance oldci,
				     CIMInstance ci) {

	try {
	    if (modificationTriggerSet.contains(namespace+":"+
					    ci.getClassName().toLowerCase())) {

		CIMInstance indication = new CIMInstance();
		indication.setClassName(FilterActivation.INSTANCEMODIFICATION);
		CIMProperty cp = new CIMProperty();
		cp.setName("SourceInstance");
		cp.setValue(new CIMValue(ci));
		cp.setType(CIMDataType.getPredefinedType(CIMDataType.STRING));
		Vector v = new Vector();
		v.addElement(cp);
		cp = new CIMProperty();
		cp.setName("PreviousInstance");
		cp.setValue(new CIMValue(oldci));
		cp.setType(CIMDataType.getPredefinedType(CIMDataType.STRING));
		v.addElement(cp);
		indication.setProperties(v);
		EventService.eventService.deliverEvent(namespace, indication);
	    }
	} catch (Exception e) {
	    Debug.trace2("instanceModification Event Exception:\n" + 
			e.getMessage());
	}
    }

    Set additionTriggerSet = new TreeSet();
    public void additionTriggerActivate(String namespace, String className) {
	concurrentObj.writeLock();
	additionTriggerSet.add(namespace+":"+className);
	concurrentObj.writeUnlock();
    }

    Set deletionTriggerSet = new TreeSet();
    public void deletionTriggerActivate(String namespace, String className) {
	concurrentObj.writeLock();
	deletionTriggerSet.add(namespace+":"+className);
	concurrentObj.writeUnlock();
    }

    Set modificationTriggerSet = new TreeSet();
    public void modificationTriggerActivate(String namespace, String 
						className) {
	concurrentObj.writeLock();
	modificationTriggerSet.add(namespace+":"+className);
	concurrentObj.writeUnlock();
    }

    public void additionTriggerDeActivate(String namespace, String className) {
	concurrentObj.writeLock();
	additionTriggerSet.remove(namespace+":"+className);
 	concurrentObj.writeUnlock();
    }

    public void deletionTriggerDeActivate(String namespace, String className) {
	concurrentObj.writeLock();
	deletionTriggerSet.remove(namespace+":"+className);
  	concurrentObj.writeUnlock();
   }

    public void modificationTriggerDeActivate(String namespace, String 
						className) {
	concurrentObj.writeLock();
	modificationTriggerSet.remove(namespace+":"+className);
  	concurrentObj.writeUnlock();
   }

    public void authorizeFilter(SelectExp filter, String eventType,
    CIMObjectPath classPath, String owner) throws CIMException {
	//System.out.println("Shouldnt be called - repository doesn't authorize");
    }

    public boolean mustPoll(SelectExp filter, String eventType,
    CIMObjectPath classPath) throws CIMException {
	// we'll handle it
	//System.out.println("This shouldnt be called");
	return false;
    }

    public void activateFilter(SelectExp filter, String eventType, 
    CIMObjectPath classPath, boolean firstActivation) throws CIMException {
	if (firstActivation) {
	    classIndicationMap.put(classPath.getNameSpace()+":"+
	    classPath.getObjectName().toLowerCase()+":"+eventType, "");
	}
    }

    public void deActivateFilter(SelectExp filter, String eventType, 
    CIMObjectPath classPath, boolean lastActivation) throws CIMException {
	if (lastActivation) {
	    classIndicationMap.remove(classPath.getNameSpace()+":"+
	    classPath.getObjectName()+":"+eventType);
	}
    }
}
