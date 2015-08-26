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
 *are Copyright ¨ 2002 Sun Microsystems, Inc.
 *
 *All Rights Reserved.
 *
 *Contributor(s): _______________________________________
*/

    import java.util.*;
    import javax.wbem.cim.CIMClass;
    import javax.wbem.cim.CIMException;
    import javax.wbem.cim.CIMInstance;
    import javax.wbem.cim.CIMObjectPath;
    import javax.wbem.cim.CIMProperty;
    import javax.wbem.cim.CIMValue;
    import javax.wbem.cim.*;
    import javax.wbem.client.CIMOMHandle;
    import javax.wbem.client.ProviderCIMOMHandle;
    import javax.wbem.client.*;
    import javax.wbem.provider.CIMInstanceProvider;
    import javax.wbem.provider.CIMMethodProvider;
    import javax.wbem.provider.EventProvider;
    import javax.wbem.provider.*;
    import javax.wbem.query.SelectExp;


    public class Class_E_Provider implements CIMInstanceProvider, 
	EventProvider, Authorizable, CIMMethodProvider  {
	/* Save a handle to the CIMOM. */
        protected ProviderCIMOMHandle pcimom = null;
        protected CIMInstanceProvider ip = null;
        protected String providedName = "Class_E";
        protected boolean enableIndications = false;

        // The three intrinsic instance indication class names
        final static String INSTANCECREATION = "CIM_InstCreation";
        final static String INSTANCEDELETION = "CIM_InstDeletion";
        final static String INSTANCEMODIFICATION = "CIM_InstModification";
        final static String INSTANCEREAD = "CIM_InstRead";
        final static String INSTANCEMETHODCALL = "CIM_InstMethodCall";

        /**
         * Initialization assigns a provider handle to the CIMOM
         * so the provider can communicate with the CIMOM and
         * access the repository, and initial values to the
         * provider's properties.
         * <BR><BR>
         */
	public void initialize(CIMOMHandle cimom) throws CIMException {
	    pcimom = (ProviderCIMOMHandle) cimom;
	    ip = pcimom.getInternalCIMInstanceProvider();
	}

        /**
         * Cleanup is required only if the provider, via a CIMOM
         * handle, has created data in the repository that should
         * not persist beyond the end of the current CIMOM run,
         * ie, beyond stopping and restarting the CIMOM.
         * <P>
         * Any data in the provider's local memory, ie, objects
         * global to the provider class that the provider methods
         * created, persists only until the CIMOM is stopped, at
         * which point provider memory is deallocated.
         * <BR><BR>
         */
	public void cleanup() throws CIMException {
	}

	/**
         * From the provider, using an internal provider, return
         * from the Repository a list of instances of the class
	 * specified in the object path and class arguments.
	 */
        public CIMInstance[] enumerateInstances(CIMObjectPath op,
                boolean localOnly, boolean includeQualifiers,
                boolean includeClassOrigin, String[] propList, CIMClass cc) 
		throws CIMException {
	    String on = op.getObjectName();
            if (on.equalsIgnoreCase(providedName) == true) {
 	        CIMInstance [] instanceArray = ip.enumerateInstances(op, 
			localOnly, includeQualifiers, 
			includeClassOrigin, propList, cc);
		return instanceArray;
            }
            return null;
        }

	/**
         * From the provider, using an internal provider, return
         * from the Repository a list of names of instances of the
	 * class specified in the object path and class arguments.
	 */
        public CIMObjectPath[] enumerateInstanceNames(CIMObjectPath op,
		CIMClass cc) throws CIMException {
	    String on = op.getObjectName();
            if (on.equalsIgnoreCase(providedName) == true) {
 	        CIMObjectPath[] pathsArray = ip.enumerateInstanceNames(op, cc);
		return pathsArray;
            }
            return null;
        }

	/**
         * From the provider, using an internal provider, return
         * from the Repository an instance of the class managed by
	 * the provider, which here is the provider class itself.
	 */
        public CIMInstance getInstance(CIMObjectPath op, 
		boolean localOnly, boolean includeQualifiers, 
		boolean includeClassOrigin, String[] propertyList, CIMClass cc) 
		throws CIMException {

 	    String on = op.getObjectName();
            if (on.equalsIgnoreCase(providedName) == true) {
 	        CIMInstance instance0 = ip.getInstance(op,
			localOnly, includeQualifiers,
			includeClassOrigin, propertyList, cc);
		// Check for instance read indication
                if (checkEvent(INSTANCEREAD)) {
		    sendEvent(op, INSTANCEREAD, instance0);
		}
                if (localOnly) {
                    instance0 = instance0.localElements();
                }
                return instance0.filterProperties(propertyList,
		        includeQualifiers, includeClassOrigin);
            }
            return new CIMInstance();
        }

	/**
         * From the provider, using an internal provider, fetch
         * from the Repository a subset of instances of the class.
	 * The subset is specified by the query string argument,
	 * which is a WQL statement. 
	 */
        public CIMInstance[] execQuery(CIMObjectPath op, 
		String query, 
		String ql, 
		CIMClass cc) throws CIMException {
	    String on = op.getObjectName();
            if (on.equalsIgnoreCase(providedName) == true) {
 	        CIMInstance [] instanceArray = ip.execQuery(op, query, ql, cc);
		return instanceArray;
            }
            return null;
        }

	/**
         * From the provider, using an internal provider, create
         * in the Repository an instance of the class specified
	 * in the object path argument, which here is the
	 * provider class itself, making the instance a copy
	 * of the instance argument, which is an instance
	 * local to the client.
	 *
	 * Return the new instance's location in an object path,
	 * setting the object-name and key-pairs parts of the
	 * name of this new instance.
	 */
        public CIMObjectPath createInstance(CIMObjectPath op,
		CIMInstance ci) throws CIMException {
	    String on = op.getObjectName();
            if (on.equalsIgnoreCase(providedName) == true) {
		// Check for instance creation indication
		if (checkEvent(INSTANCECREATION)) {
		    sendEvent(op, INSTANCECREATION, ci);
		}
		return ip.createInstance(op, ci);
            }
            return new CIMObjectPath();
        }

	/**
         * From the provider, using an internal provider,
	 * delete in the Repository the instance specified
         * by the object path argument.
	 */
        public void deleteInstance(CIMObjectPath op) 
		throws CIMException {
	    String on = op.getObjectName();
            if (on.equalsIgnoreCase(providedName) == true) {
		// Check for instance deletion indication
		if (checkEvent(INSTANCEDELETION)) {
		    CIMInstance ci = pcimom.getInstance(op, false, false, false, null);
		    sendEvent(op, INSTANCEDELETION, ci);
		}
		ip.deleteInstance(op);
            }
        }

	/**
         * From the provider, using an internal provider,
         * change in the Repository the contents of the
	 * instance specified by the object path argument,
	 * using the contents of the instance argument,
	 * which is an instance local to the client.
	 */
        public void setInstance(CIMObjectPath op, CIMInstance ci,
		boolean includeQualifier, String[] propertyList)
                throws CIMException {
	    String on = op.getObjectName();
            if (on.equalsIgnoreCase(providedName) == true) {
		// Check for instance modification indication
		if (checkEvent(INSTANCEMODIFICATION)) {
		    CIMInstance ciOld = pcimom.getInstance(op, false, false, false, null);
		    sendEvent(op, ci, ciOld);
		}
 	        ip.setInstance(op, ci, includeQualifier, propertyList);
            }
        }

	/**
	 * From the provider, using a CIMOM handle, return from
	 * the Repository the result of the WBEM method executed
	 * by invokeMethod(), a WBEM method dispatcher whose
	 * interface is a generalized representation of a WBEM
	 * method.
	 */
	public CIMValue invokeMethod(CIMObjectPath op, 
		String methodName, CIMArgument[] inParams, CIMArgument[] outParams)
                throws CIMException {
	    CIMValue cv = null;

	    // Check for instance modification indication
	    if (checkEvent(INSTANCEMETHODCALL)) {
		sendEvent(op, methodName, inParams);
	    }

            if (methodName.equalsIgnoreCase("Sum")) {
		// According to the specification, order cannot be assumed
		// So check CIMArgument.name
		int length = inParams.length;
		Integer number1 = null;
		Integer number2 = null;
		for (int i = 0; i < length; i++) {
		    if (inParams[i].getName().equalsIgnoreCase("number1")) {
			cv = inParams[i].getValue();
			number1 = (Integer) cv.getValue();
		    } else if (inParams[i].getName().equalsIgnoreCase("number2")) {
			cv = inParams[i].getValue();
			number2 = (Integer) cv.getValue();
		    }
		}
		int total = number1.intValue() + number2.intValue();
		cv = new CIMValue(new Integer(total));
		outParams[0] = new CIMArgument("total", cv);

	    } else {
		throw new CIMMethodException(CIMMethodException.NO_SUCH_METHOD,
			methodName, op.getObjectName());
	    }

            return new CIMValue(new Integer(0));
        } // invokeMethod

	/**
         * Invoked by the CIMOM to test if a given event
	 * filter is allowed.  Provider must have implemented
	 * Authorizable interface.  Triggered by client 
	 * installation of event filter.
	 */
	public void authorizeFilter(SelectExp filter, String
		eventType, CIMObjectPath classPath, String
		owner) throws CIMException {
        }

	/**
         * Invoked by the CIMOM to test if a given event
	 * filter expression is allowed by the provider
	 * and if it must be polled.  Triggered by client
         * installation of event filter.
	 * NOTE: This provider asks CIMOM to poll it for event indications
	 */
	public boolean mustPoll(SelectExp filter, String
		eventType, CIMObjectPath classPath) throws
		CIMException {
            return true;
        }

	/**
         * Invoked by the CIMOM to ask the provider to start
	 * checking for events. Triggered when client "creates" an instance
	 * of association CIM_IndicationSubscription that binds a
	 * CIM_IndicationHandler instance to a CIM_IndicationFilter
	 * instance.
	 */
	public void activateFilter(SelectExp filter, String
		eventType, CIMObjectPath classPath, boolean
		firstActivation) throws CIMException {
	    enableIndications = true;
        }

	/**
         * Invoked by the CIMOM to ask the provider to stop
	 * checking for events. Triggered when client "deletes" the instance
	 * of association CIM_IndicationSubscription that binds a
	 * CIM_IndicationHandler instance to a CIM_IndicationFilter
	 * instance.
	 */
	public void deActivateFilter(SelectExp filter, String
		eventType, CIMObjectPath classPath, boolean
		lastActivation) throws CIMException {
	    enableIndications = false;
        }

	private boolean checkEvent(String eventClass)
	throws CIMException {
		return enableIndications;
	} // checkEvent

	// Deliver CIM_InstRead event to CIMOM
	private void sendEvent(CIMObjectPath op)
	throws CIMException {
	    try {
		// Create a CIM_InstRead instance
		CIMInstance indicationInstance = new CIMInstance();
		indicationInstance.setClassName(INSTANCEREAD);
	
		// Deliver the event to CIMOM
		pcimom.deliverEvent(op.getNameSpace(), indicationInstance);
	    } catch (Exception e) {
		throw new CIMProviderException(CIMProviderException.GENERAL_EXCEPTION, e.getMessage());
	    }
	} // sendEvent CIM_InstRead

	// Deliver CIM_InstCreation, CIM_InstDeletion or CIM_InstRead event to CIMOM
	private void sendEvent(CIMObjectPath op,
			String eventClass,
			CIMInstance ci)
	throws CIMException {
	    try {
		// Create a CIM_Inst* instance
		CIMInstance indicationInstance = new CIMInstance();
		CIMProperty property = null;
		Vector propertyVector = new Vector();
		indicationInstance.setClassName(eventClass);
	
		// Copy the new instance into CIM_Inst*.SourceInstance
		property = new CIMProperty("SourceInstance", new CIMValue(ci));
		propertyVector.addElement(property);
	
		// Deliver the event to CIMOM
		indicationInstance.setProperties(propertyVector);
		pcimom.deliverEvent(op.getNameSpace(), indicationInstance);
	    } catch (Exception e) {
		throw new CIMProviderException(CIMProviderException.GENERAL_EXCEPTION, e.getMessage());
	    }
	} // sendEvent CIM_InstCreation or CIM_InstDeletion

	// Deliver CIM_InstModification event to CIMOM
	private void sendEvent(CIMObjectPath op,
			CIMInstance ci,
			CIMInstance ciOld)
	throws CIMException {
	    try {
		// Create a CIM_InstModification instance
		CIMInstance indicationInstance = new CIMInstance();
		CIMProperty property = null;
		Vector propertyVector = new Vector();
		indicationInstance.setClassName(INSTANCEMODIFICATION);
	
		// Copy the new instance into CIM_InstModification.SourceInstance
		property = new CIMProperty("SourceInstance", new CIMValue(ci));
		propertyVector.addElement(property);
	
		// Copy the old instance into CIM_InstModification.PreviousInstance
		property = new CIMProperty("PreviousInstance", new CIMValue(ciOld));
		propertyVector.addElement(property);

		// Deliver the event to CIMOM
		indicationInstance.setProperties(propertyVector);
		pcimom.deliverEvent(op.getNameSpace(), indicationInstance);
	    } catch (Exception e) {
		throw new CIMProviderException(CIMProviderException.GENERAL_EXCEPTION, e.getMessage());
	    }

	} // sendEvent CIM_InstModification

	// Deliver PreCall CIM_InstMethodCall event to CIMOM
	private void sendEvent(CIMObjectPath op,
			String methodName,
			CIMArgument[] inParams)
	throws CIMException {
	    try {
		// Create a CIM_InstMethodCall instance
		CIMInstance indicationInstance = new CIMInstance();
		indicationInstance.setClassName(INSTANCEMETHODCALL);

		CIMProperty property = null;
		Vector propertyVector = new Vector();
		
		// Copy the new instance into CIM_Inst*.SourceInstance
		CIMInstance ci = pcimom.getInstance(op, false, false, false, null);
		property = new CIMProperty("SourceInstance", new CIMValue(ci));
		propertyVector.addElement(property);

		// Set CIM_InstMethodCall.MethodName = methodName
		property = new CIMProperty("MethodName", new CIMValue(methodName));
		propertyVector.addElement(property);
	
		// For PreCall, CIM_InstMethodCall.ReturnValue = NULL
		property = new CIMProperty("ReturnValue");
		propertyVector.addElement(property);
	
		// For PreCall, CIM_InstMethodCall.PreCall = TRUE
		property = new CIMProperty("PreCall", CIMValue.TRUE);
		propertyVector.addElement(property);
	
		// Set CIM_InstMethodCall.MethodParameters as embedded
		// CIMInstance with parameters as property names
		// First, create the _MethodParameters class
		CIMClass ccMethodParameters = new CIMClass("_MethodParameters");

		// Next, create _MethodParameters instance
		CIMInstance ciMethodParameters = ccMethodParameters.newInstance();

		// Add and set a property value in the _MethodParameters instance
		// for each input argument
		int length = inParams.length;
		for (int i = 0; i < length; i++) {
			String name = inParams[i].getName();
			CIMValue cv = inParams[i].getValue();
			ccMethodParameters.addProperty(new CIMProperty(name, cv));
		}

		// Set CIM_InstMethodCall.MethodParameters = _MethodParameters instance
		property = new CIMProperty("MethodParameters", new CIMValue(ciMethodParameters));
		propertyVector.addElement(property);
	
		// Deliver the event to CIMOM
	        indicationInstance.setProperties(propertyVector);
	        pcimom.deliverEvent(op.getNameSpace(), indicationInstance);
	    } catch (Exception e) {
		throw new CIMProviderException(CIMProviderException.GENERAL_EXCEPTION, e.getMessage());
	    }
	} // sendEvent CIM_InstMethodCall PreCall

    } // Class_E_Provider 
