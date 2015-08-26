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

import java.util.Calendar;
import java.text.SimpleDateFormat;

public class CIMOMVersion {

    private static int major = 2;
    private static int minor = 2;
    private static int patchHigh = 100;
    private static int patchLow  = 0;
    private static Calendar cal = Calendar.getInstance(); 

    private static String copyright = 
    "Copyright (c) 2000 Sun Microsystems, Inc.\nAll rights reserved.\n";
    private static String productName = "CIM Object Manager";

    public CIMOMVersion() {
	cal.set(2000, Calendar.JANUARY, 1);
    }

    public static String getBuildDate() {
	SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
	return formatter.format(cal.getTime());
    }

    public String getCopyright() {
	return copyright;
    }
    
    public static String getProductName() {
	return productName;
    }
    
    public static String getVersion() {
	return major + "." + minor + "." + patchHigh + "." + patchLow;
    }

    public String toString() {
	return getVersion(); 
    }
}
