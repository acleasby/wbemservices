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

package org.wbemservices.wbem.repository;

import java.io.IOException;

/** 
 * This class can be used to represent all exceptional conditions that
 * occur during any logging process. Whenever an exception is caught 
 * while information is being logged, the exception can be wrapped
 * in this class so as to indicate an unsuccessful log operation.
 */
public class LogException extends IOException {

    private static final long serialVersionUID = 1870528169848832111L;

    /** @serial */
    public Throwable detail;

    /**
     * Create a wrapper exception for exceptions that occur during a logging
     * operation.
     */
    public LogException() {}

    /**
     * For exceptions that occur during a logging operation, create a wrapper
     * exception with the specified description string.
     */
    public LogException(String s) {
	super(s);
    }

    /**
     * For exceptions that occur during a logging operation, create a wrapper
     * exception with the specified description string and the specified
     * nested exception.
     */
    public LogException(String s, Throwable ex) {
	super(s);
	detail = ex;
    }

    /**
     * Produce the message; including the message from the nested exception
     * if there is one.
     */
    public String getMessage() {
	if (detail == null) 
	    return super.getMessage();
	else
	    return super.getMessage() + 
		"; nested exception is: \n\t" +
		detail.toString();
    }
}
