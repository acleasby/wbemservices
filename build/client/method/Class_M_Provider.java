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

    import javax.wbem.cim.CIMArgument;
    import javax.wbem.cim.*;
    import javax.wbem.client.CIMOMHandle;
    import javax.wbem.client.ProviderCIMOMHandle;
    import javax.wbem.provider.CIMMethodProvider;
    import javax.wbem.provider.CIMInstanceProvider;

    public class Class_M_Provider implements CIMInstanceProvider, CIMMethodProvider {
	/* Save a handle to the CIMOM. */
        protected ProviderCIMOMHandle pcimom = null;
        protected CIMInstanceProvider ip = null;
        protected String providedName = "Class_M";

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

        public CIMInstance[] enumerateInstances(CIMObjectPath op,
                boolean localOnly, boolean includeQualifiers,
                boolean includeClassOrigin, String[] propList, CIMClass cc) 
		throws CIMException {
	throw(new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED));
        }

        public CIMObjectPath[] enumerateInstanceNames(CIMObjectPath op,
		CIMClass cc) throws CIMException {
	throw(new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED));
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
	throw(new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED));
        }

        public CIMInstance[] execQuery(CIMObjectPath op, 
		String query, 
		String ql, 
		CIMClass cc) throws CIMException {
	throw(new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED));
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
		ip.deleteInstance(op);
            }
        }

        public void setInstance(CIMObjectPath op, CIMInstance ci,
		boolean includeQualifier, String[] propertyList)
                throws CIMException {
	throw(new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED));
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
	    } else if (methodName.equalsIgnoreCase("NextTwo")) {
		cv = inParams[0].getValue();
		Integer start = new Integer(cv.toString());
		int first = start.intValue() + 1;
		cv = new CIMValue(new Integer(first));
		// According to the specification, order cannot be assumed
		// To demonstrate, place second as the 1st element
		outParams[1] = new CIMArgument("first", cv);
		int second = first + 1;
		cv = new CIMValue(new Integer(second));
		outParams[0] = new CIMArgument("second", cv);
	    } else if (methodName.equalsIgnoreCase("Greeting")) {
		String greeting = "Hello";
		cv = new CIMValue(greeting);
		outParams[0] = new CIMArgument("greeting", cv);
	    } else if (methodName.equalsIgnoreCase("Reference")) {
	    } else {
		throw new CIMMethodException(CIMMethodException.NO_SUCH_METHOD,
			methodName, op.getObjectName());
	    }

            return new CIMValue(new Integer(0));
        } // invokeMethod

    }
