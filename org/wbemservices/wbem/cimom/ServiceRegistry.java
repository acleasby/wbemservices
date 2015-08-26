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

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

/**
 *
 * This class serves a directory which contains references to services that
 * are available within the CIMOM to other CIMOM components. It has methods
 * to add and remove, and lookup services. All methods of this class are
 * static.
 *
 * @author      Sun Microsystems, Inc.
 * @since       WBEM 2.5
 */
public class ServiceRegistry {
    static private Map mServiceMap = Collections.synchronizedMap(new HashMap());

    /**
     * Adds a service to the registry.
     * @param name      The name of the service being added. If the service
     *                  name already exists, it will be replaced.
     * @param service   The service implementation. In the future this will
     *                  be a CIMOMService.
     */
    static public void addService(String name, Object service) {
	mServiceMap.put(name, service);
    }

    /**
     * Removes a service from the registry.
     * @param name      The name of the service to be removed. If the service
     *                  does not exist, the request is ignored.
     */
    static public void removeService(String name) {
	mServiceMap.remove(name);
    }

    /**
     * Retrieves a handle to the service.
     * @param     name The name of the service to be returned.
     * @return    An Object representing the service - In the future it will be
     *            a CIMOMService that is returned. If the service does not
     *            exist, a null is returned.
     */
    static public Object getService(String name) {
	return mServiceMap.get(name);
    }
}
