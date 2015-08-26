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
 *are Copyright © 2003 Sun Microsystems, Inc.
 *
 *All Rights Reserved.
 *
 *Contributor(s): _______________________________________
*/

package org.wbemservices.wbem.client;


/**
 * Implements the UNIX crypt(3) function, based on a direct port of the
 * libc crypt function.
 *
 * <p>
 * From the crypt man page:
 * <p>
 * crypt() is the password encryption routine, based on the NBS
 * Data  Encryption  Standard,  with variations intended (among
 * other things) to frustrate use of  hardware  implementations
 * of the DES for key search.
 * <p>
 * The first argument to crypt() is  normally  a  user's  typed
 * password.   The  second  is a 2-character string chosen from
 * the set [a-zA-Z0-9./].  the  salt string is used to perturb 
 * the DES algorithm in one
 * of 4096 different ways, after which the password is used  as
 * the  key  to  encrypt  repeatedly  a  constant  string.  The
 * returned value points to the encrypted password, in the same
 * alphabet as the salt.  The first two characters are the salt
 * itself.
 * 
 */

public class WorkMod51 {

/* EXPORT DELETE START */

/* EXPORT DELETE END */

    /**
     * Creates a new object for use with the crypt method.
     *
     */

    public WorkMod51() 
    {
	// does nothing at this time
	super();
    }

    /**
     * Implements the libc crypt(3) function. 
     *
     * @param arg1 the password to "encrypt".
     *
     * @param arg2 the salt to use.
     *
     * @return A new byte[13] array that contains the encrypted
     * password. The first two characters are the salt.
     *
     */

    public synchronized byte[] mod51Format(byte[] arg1, byte[] arg2) {
	byte[] iobuf = new byte[13];

/* EXPORT DELETE START */

/* EXPORT DELETE END */
 	return(iobuf);
    }

    /**
     * program to test the crypt routine.
     *
     * The first parameter is the cleartext password, the second is
     * the salt to use. The salt should be two characters from the 
     * set [a-zA-Z0-9./]. Outputs the crypt result.
     *
     * @param arg command line arguments.
     *
     */
      
    public static void main(String arg[]) {

	if (arg.length!=2) {
	    System.err.println("usage: WorkMod51 string seed");
	    System.exit(1);
	}

	WorkMod51 c = new WorkMod51();
	byte result[] = c.mod51Format(arg[0].getBytes(), arg[1].getBytes());
	for (int i=0; i<result.length; i++) {
	    System.out.println(" "+i+" "+(char)result[i]);
	}
    }
}
