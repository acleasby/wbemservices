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

package org.wbemservices.wbem.cimom.adapters.client.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.server.Unreferenced;

// This is the lease object that is passed back by the RMI adapter to the
// client. It enables the RMI adapter to use the RMI runtime to determine if
// the client lease has expired.
public class CIMRMILease_Impl extends UnicastRemoteObject 
	implements Unreferenced, Remote {

    Unreferenced mUref = null;
    // Start Constructors. This takes in an Unreferenced object. It delegates
    // the unreferenced call from the RMI runtime to this object.
    public CIMRMILease_Impl(Unreferenced pUref) 
    throws RemoteException {
        super();
	mUref = pUref;
    }
    // End Constructors

    // Start public methods.

    // Start Unreferenced interface methods

    // This method is invoked by the RMI runtime to indicate that the remote
    // client lease has expired.
    public void unreferenced() {
	mUref.unreferenced();
    }

    // End Unreferenced interface methods.

    // End public methods.

    // Start package protected methods.
    Unreferenced getUnreferenced() {
	return mUref;
    }
    // End package protected methods.
}
