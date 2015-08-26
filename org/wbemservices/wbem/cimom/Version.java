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
 *are Copyright © 2002 Sun Microsystems, Inc.
 *
 *All Rights Reserved.
 *
 *Contributor(s): _______________________________________
*/

package org.wbemservices.wbem.cimom;

// This file represents the version of the CIMOM. During the build,
// Version.java is generated with the build date and time.
final class Version {
    // Disallow any instances from being created
    private Version() {
    }
    // Major version of the CIMOM.
    final static int major = 1;
    // Minor version of the CIMOM.
    final static int minor = 0;
    // This has to be changed anytime we feel some significant change has
    // been made to this minor version of the CIMOM
    final static int revision = 0;
    final static String productName = "WBEM Services WBEM Server";
    // The build id is a build date in mm/dd/yy:HH:MM format
    final static String buildID = "11/01/04:10:48";
}

