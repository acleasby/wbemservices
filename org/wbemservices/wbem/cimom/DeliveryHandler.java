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


import javax.wbem.cim.CIMObjectPath;

import java.util.Map;
import java.util.HashMap;

/*
 * The class handles the different indication delivery mechanisms.
 * Each delivery mechanism has a handler which actually takes care
 * of delivering the indication to the final destination. All these are taken 
 * care of here. 
 */
public class DeliveryHandler {

    private static Map handlerMap = new HashMap();

    /**
     * Register a callback which returns indication handlers for the specific
     * protocol. Invoked by the individual protocol handlers.
     * @param indicationHandlerClass The name of CIM Handler class to which 
     * the returned indication handlers belong.
     * @param indicationHandler The handle which returns appropriate 
     * instances of indicationHandlerClass. This is protocol dependent.
     */
     public static void registerIndicationHandler(String indicationHandlerClass, 
     EventService.IndicationHandler indicationHandler) {
	handlerMap.put(indicationHandlerClass.toLowerCase(), indicationHandler);
     }

    /*
     * Returns the appropriate delivery mechanism for the given
     * delivery type.
     */
    static EventService.IndicationHandler 
    getIndicationHandler(String handlerType) {

	return (EventService.IndicationHandler)
	handlerMap.get(handlerType.toLowerCase());
    }

    static boolean isTransient(CIMObjectPath deliveryOp) {
	// Currently we only handle RMI, which is transient.
	return true;
    }
}
