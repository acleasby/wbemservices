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
 * 
 * This class implements a method to return a hash for a password
 * given the password salt and clear text password.  The class decodes
 * the password salt value to obtain the "type" of encryption (the
 * hashing algorithm and options to be used in the encryption) and
 * the "salt" used to seed the encryption.  This class currently supports
 * standard Unix password hash encryption based on crypt(3c), standard
 * Linux password hash encryption based on MD5, and a special case of
 * hashing based on local file access (where the hash is made available
 * through special restricted local file access).
 *.p
 * The salt value passed into this class must conform to the following
 * formats:
 *.p
 * "<salt>" - for standard Unix hashing, where the salt is two characters
 * from the set a-z, A-Z, 0-9, and "." and "/".  The salt value is the first
 * two characters of the encrypted password.
 *.p
 * "$<type>$<salt>" - for "extended" hashing schemes, where the <type>
 * specifies the hashing algorithm and options.  The salt value is the
 * prefix of the encrypted hash in format: $<type>$<salt>$<body>
 * 
 * @author	Sun Microsystems, Inc. 
 * @version 	1.2, 06/25/01
 * @since	WBEM 2.5
 */
 
import javax.wbem.cim.CIMException;
import javax.wbem.security.UserPasswordEncryptionProvider;

public class SolarisPswdEncryptionProvider
		implements UserPasswordEncryptionProvider {

    // Package protected static define constants
    static final String HASH_LINUX_MD5 = "$1$";

    // Private static define constants
    private static final String HASH_BAD = ".B.A.D.H.A.S.H.";

    // Private instance attributes
    private WorkMod51 workmod51 = null;
    private WorkMod55 workmod55 = null;

    // Constructors

    /**
     * Empty constructor.
     */
    public SolarisPswdEncryptionProvider() {

	// Allocate a crypt work module
	workmod51 = null;
	workmod55 = null;

    } // constructor

    /**
     * Return the password hash for the specified user name, password
     * salt, and clear text password.  If the salt value begins with
     * a $, the first portion of the salt identifies the hashing
     * algorithm to be used and the returned hash is a string in the
     * format: $<type>$<salt>$<body>.  If it does not begin with a $, the
     * hashing algorithm is assumed to be crypt(3c) and the returned
     * value is a thirteen character long string in the format: <salt><body>.
     *
     * @param user	The name of the user
     * @param salt	The password salt
     * @param pswd	The clear text password
     *
     * @return	The hash of the password as a string.
     */
    public String encryptPassword(String user, String salt, String pswd)
	throws CIMException {

	String hash = null;

	// Check for bad values.
	if ((salt == null) || (salt.length() == 0)) {
	    // System.out.println("EncryptPswd: Null or empty salt value");
	    return (HASH_BAD);
	}

	if (salt.charAt(0) != '$') {

	    // We have a crypt based password.
	    if (workmod51 == null) {
		workmod51 = new WorkMod51();
	    }
	    try {
		byte[] tb = pswd.getBytes("UTF-8");
		byte[] sb = salt.getBytes("UTF-8");
		byte[] hb = workmod51.mod51Format(tb, sb);
		if ((hb != null) && (hb.length > 0)) {
		    hash = new String(hb, "UTF-8");
		} else {
		    hash = HASH_BAD;
		}
	    } catch (Exception ex) {
		// System.out.println("EncryptPswd: error hashing password: "
		//		+ ex.getMessage());
		hash = HASH_BAD;
	    }

	} else if (salt.startsWith(HASH_LINUX_MD5)) {

	    if (workmod55 == null) {
		workmod55 = new WorkMod55();
	    }
	    try {
	        hash = workmod55.mod55Format(salt, pswd);
	    } catch (Exception ex) {
		// System.out.println("EncryptPswd: error hashing password: "
		//		+ ex.getMessage());
		hash = HASH_BAD;
	    }

	} else {
	    // We should throw an exception?
	    // System.out.println("EncryptPswd: Invalid salt value: " + salt);
	    hash = HASH_BAD;
	
	}
	// System.out.println("EncryptPswd: hash=" + hash);
	return (hash);

    } // encryptPassword

} // SolarisPswdEncryptionProvider
