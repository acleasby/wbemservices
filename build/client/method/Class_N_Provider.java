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

    import javax.wbem.cim.*;
    import javax.wbem.client.CIMOMHandle;
    import javax.wbem.client.ProviderCIMOMHandle;
    import javax.wbem.provider.CIMInstanceProvider;

    public class Class_N_Provider implements CIMInstanceProvider {
	/* Save a handle to the CIMOM. */
        protected ProviderCIMOMHandle pcimom = null;
        protected CIMInstanceProvider ip = null;
        protected String providedName = "Class_N";

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

    }
