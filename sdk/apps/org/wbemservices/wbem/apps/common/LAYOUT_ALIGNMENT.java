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

package org.wbemservices.wbem.apps.common;

/**
 * 
 *
 * @version 	1.4, 11/16/00
 * @author 	Sun Microsystems
 */

public class LAYOUT_ALIGNMENT {
  
    public final static LAYOUT_ALIGNMENT CENTER = new LAYOUT_ALIGNMENT();
    public final static LAYOUT_ALIGNMENT LEFT   = new LAYOUT_ALIGNMENT();
    public final static LAYOUT_ALIGNMENT RIGHT  = new LAYOUT_ALIGNMENT();
    public final static LAYOUT_ALIGNMENT TOP    = new LAYOUT_ALIGNMENT();
    public final static LAYOUT_ALIGNMENT BOTTOM = new LAYOUT_ALIGNMENT();
    public final static LAYOUT_ALIGNMENT EXPAND = new LAYOUT_ALIGNMENT();
    public final static LAYOUT_ALIGNMENT FIT    = new LAYOUT_ALIGNMENT();
  
    private LAYOUT_ALIGNMENT() { }
}
