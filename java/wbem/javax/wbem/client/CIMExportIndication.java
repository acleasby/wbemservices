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
 *are Copyright (c) 2002 Sun Microsystems, Inc.
 *
 *All Rights Reserved.
 *
 *Contributor(s): _______________________________________
*/

package javax.wbem.client;

import javax.wbem.cim.CIMInstance;

/**
 * The CIMExportIndication class is used to deliver an CIM Indication 
 * to a CIM Listener.
 * 
 * @author      Sun Microsystems, Inc.
 * @version     1.0 11/20/01
 * @since       WBEM 2.5
 */
public class CIMExportIndication implements CIMExport {

    private CIMInstance indication;

    private final static long serialVersionUID = 4901733723417572223L;

    /**
     * Creates a CIMExportIndication instance with the specified 
     * CIM Indication.
     */
    public CIMExportIndication(CIMInstance indication) {
	this.indication = indication;
    }

    /**
     * Get the CIM Indciation.
     */
    public CIMInstance getIndication() {
	return indication;
    }
}
