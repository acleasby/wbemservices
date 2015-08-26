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

import javax.wbem.cim.CIMException;

/**
 *
 * This interface represents the logging service that is used by all the
 * internal CIMOM components. This includes all classes in the CIMOM package
 * as well as client and provider protocol adapters.
 *
 * @author      Sun Microsystems, Inc.
 * @since       WBEM 2.5
 */
public interface CIMOMLogService  {

    public static final int CATEGORY_INVALID = -1;
    public static final int APPLICATION_LOG = 0;
    public static final int SECURITY_LOG = 1;
    public static final int SYSTEM_LOG = 2;

    public static final int SEVERITY_INVALID = -1;
    public static final int INFO = 0;
    public static final int WARNING = 1;
    public static final int ERROR = 2;

    public static final String DEFAULT = "logservice";

    /**
     * @param moduleName     Name of the module writing the log record
     * @param summaryMesgID  message Id of the summary message
     * @param detailedMesgID message Id of the detailed message
     * @param args           substitution arguments for the detailed message
     * @param data           Additional data for the message.
     * @param syslog_flag    boolean indicating whether the record should be
     *                       forwarded to the system log or not.
     * @param category       category of the log record (application, system,
     *                       security). Can be one of CATEGORY_INVALID, 
     *                       APPLICATION_LOG, SECURITY_LOG, SYSTEM_LOG.
     * @param severity       severity of the log (info, warning, error). Can be 
     *                       one of SEVERETY_INVALID, INFO, WARNING, ERROR.
     * @param bundleName     Name of the property Resource bundle containing
     *                       the messages corresponding to the summary message
     *                       and detailed message IDs. If set to NULL, no
     *                       lookup is performed.
     */
    String writeLog(String moduleName, 
			String summaryMesgID,
			String detailedMesgID,
			String[] args,
			String data,
			boolean syslog_flag,
			int category,
			int severity,
			String bundleName) throws CIMException;
}
