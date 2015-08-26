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

package org.wbemservices.wbem.cimom.adapter.client;

import org.wbemservices.wbem.cimom.CIMOMServer;
import javax.wbem.client.CIMOMHandle;
import javax.wbem.cim.CIMInstance;

/*
 * ClientProtocolAdapterIF
 *
 * The Client Protocol Adapter Interface is ... 
 * 
 *
 * @author Sun Microsystems, Inc. 
 * @Since CIMOM 2.5
 */
public interface ClientProtocolAdapterIF {

    /**
     * This method is called by the CIMOM to initialize the adapter.
     * @param cimom This interface consists of the CIMOM operations.
     * It is assumed that the protocol adapter performs security authentication
     * on client requests and then passes the associated security information 
     * across this interface to handle client requests.
     * @param ch This is a handle that the protocol adapter can use to gain
     * access to the CIMOM.
     */
    public void initialize(CIMOMServer cimom, CIMOMHandle ch);

    /**
     * This method will be called by the CIMOM to start the Protocol
     * Adapter. It is required that the protocol adapter can be started
     * and stopped on demand.  
     * @param ci an instance of 
     * WBEMServices_ObjectManagerClientProtocolAdapter class.
     */
    public int startService(CIMInstance ci);

    public int stopService();

}
