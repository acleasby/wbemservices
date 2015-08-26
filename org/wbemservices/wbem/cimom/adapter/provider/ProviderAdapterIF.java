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
package org.wbemservices.wbem.cimom.adapter.provider;

import javax.wbem.provider.CIMProvider;

/**
 * This is an interface which all adapters must return from the 'getProvider'
 * call. The functions allow the CIMOM to detemrine what kind of provider
 * it is dealing with.
 * @author  Jim M.
 * @version 01/03/01
 */
public interface ProviderAdapterIF {

    /**
     * Indicates that the provider implements the CIMInstanceProvider interface
     * @return true if it does implement the interface or false otherwise
     */
    public boolean isInstanceProvider();

    /**
     * Indicates that the provider implements the CIMMethodProvider interface
     * @return true if it does implement the interface or false otherwise
     */
    public boolean isMethodProvider();

    /**
     * Indicates that the provider implements the CIMIndicationProvider interface
     * @return true if it does implement the interface or false otherwise
     */
    public boolean isCIMIndicationProvider();

    /**
     * Indicates that the provider implements the EventProvider interface
     * @return true if it does implement the interface or false otherwise
     */
    public boolean isEventProvider();

    /**
     * Indicates that the provider implements the Authorizable interface
     * @return true if it does implement the interface or false otherwise
     */
    public boolean isAuthorizable();

    /**
     * Indicates that the provider implements the CIMAssociatorProvider interface
     * @return true if it does implement the interface or false otherwise
     */
    public boolean isAssociatorProvider();

    /**
     * This function returns the underlying provider, which will be used by the
     * CIMOM to execute methods in that provider
     * @return An instance which implements at least one of the CIMProvider
     * interfaces, it <B>MUST</B> implement the interfaces specified by the
     * various isXXX functions of this interface.  For example: If
     * 'isInstanceProvider' returns true then the CIMProvider returned from this
     * function <B>HAS TO</B> implement the 'InstanceProvider' interface
     */
    public CIMProvider getProvider();
}
