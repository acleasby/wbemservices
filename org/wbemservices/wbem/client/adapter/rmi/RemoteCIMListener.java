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

package org.wbemservices.wbem.client.adapter.rmi;

import javax.wbem.client.CIMEvent;

import java.util.EventListener;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * 
 * Remote CIMListener interface to be used by RMI.
 *
 * @author      Sun Microsystems, Inc.
 * @since       WBEM 1.0
 */
public interface RemoteCIMListener extends EventListener, Remote {
    public void indicationOccured(CIMEvent e, String[] destination) 
    throws RemoteException;
    public void isAvailable() throws RemoteException;
}
