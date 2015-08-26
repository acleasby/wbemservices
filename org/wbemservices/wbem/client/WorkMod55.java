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
 * This class is a Java implementation of the MD5 based password
 * encryption for Linux; typically noted as the "$1" encryption type.
 * The mod55Format method accepts the password salt (which should
 * include the "$1$" prefix) and the clear text password.  The resulting
 * encryption is in the format: $1$<salt>$<encryption>, where the
 * salt is no more than 8 characters and the encryption is 22 characters.
 * 
 */
 
import java.security.MessageDigest;
import java.util.Arrays;

public final class WorkMod55 {

    private static final String MD5_PREFIX = "$1$";

    private static final char[] BASE64 = {
	'.', '/', '0', '1', '2', '3', '4', '5',
	'6', '7', '8', '9', 'A', 'B', 'C', 'D',
	'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
	'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
	'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b',
	'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
	'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
	's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
   	};

    private MessageDigest md;

    // Constructors

    /**
     * Empty constructor.
     */
    public WorkMod55() {

	md = null;

    } // constructor

    /**
     * Return the password hash for the specified password and salt.
     * The salt must begin with the special prefix "$1$" indicating
     * the MD5 based Linux password encryption.  The salt must be a
     * string value not more than eight characters in length.  The
     * password is the clear text password.  The result is a password
     * encryption in the format: $1$<salt>$<encryption>.
     *
     * @param salt	The password salt
     * @param pswd	The clear text password
     *
     * @return	The hash of the password as a string.
     */
    public synchronized String mod55Format(String salt, String pswd)
	throws Exception {

	// Get the salt and password as byte arrays.
	if ((salt == null) || (salt.length() < 4)) {
	    throw new IllegalArgumentException("Invalid salt");
	}
	if (! salt.startsWith(MD5_PREFIX)) {
	    throw new IllegalArgumentException("Invalid salt");
	}
	String tsalt = salt.substring(3);
	if (tsalt.length() > 8) {
	    tsalt = tsalt.substring(0, 8);
	}
	byte[] sb = tsalt.getBytes("UTF-8");
	if ((pswd == null) || (pswd.length() == 0)) {
	    throw new IllegalArgumentException("Invalid password");
	}
	byte[] pb = pswd.getBytes("UTF-8");

	// System.out.println("WorkMod55: salt = " + new String(sb));
	// System.out.println("WorkMod55: pswd = " + new String(pb));
	if (md == null) {
	    md = MessageDigest.getInstance("MD5");
	}

	// Compute secondary digest result.
	md.reset();
	md.update(pb);
	md.update(sb);
	md.update(pb);
	byte[] r1 = md.digest();

	// Compute start of digest
	md.reset();
	md.update(pb);
	md.update(MD5_PREFIX.getBytes("UTF-8"));
	md.update(sb);

	// Add one byte of secondary digest for each byte of password
	int i;
	for (i = pb.length; i > 16; i -= 16) {
	    md.update(r1);
	}
	if (i > 0) {
	    byte[] tb = new byte[i];
	    System.arraycopy(r1, 0, tb, 0, i);
	    md.update(tb);
	}
	Arrays.fill(r1, (byte) 0x00);

	// Special processing for key length bits
	byte[] r2 = { 0x00 };
	byte[] r3 = { pb[0] };
	for (i = pb.length; i > 0; i >>= 1) {
	    if ((i & 1) == 0) {
		md.update(r3);
	    } else {
		md.update(r2);
	    }
	}

	// Special round loop to jumble the hash.
	// Should the number of rounds be a parameter?
	byte[] r4 = md.digest();
	for (i = 0; i < 1000; i++) {
	    md.reset();
	    if ((i & 1) != 0) {
		md.update(pb);
	    } else {
		md.update(r4);
	    }
	    if ((i % 3) != 0) {
		md.update(sb);
	    }
	    if ((i % 7) != 0) {
		md.update(pb);
	    }
	    if ((i & 1) != 0) {
		md.update(r4);
	    } else {
		md.update(pb);
	    }
	    r4 = md.digest();
	}

	// Resulting digest is converted to 22 characters using
	// Base64 encoding.
	String encr =
		this.getEncoding(r4[0], r4[6], r4[12], 4) +
		this.getEncoding(r4[1], r4[7], r4[13], 4) +
		this.getEncoding(r4[2], r4[8], r4[14], 4) +
		this.getEncoding(r4[3], r4[9], r4[15], 4) +
		this.getEncoding(r4[4], r4[10], r4[5], 4) +
		this.getEncoding((byte) 0, (byte) 0, r4[11], 2);
	md.reset();
	Arrays.fill(r4, (byte) 0x00);

	// Now put it all together: $1$<salt>$<encryption>
	String result = MD5_PREFIX + tsalt + "$" + encr;
	// System.out.println("WorkMod55: encr = " + result);
	return (result);

    } // mod55Format

    // Get Base64 encoding characters for bytes
    private String getEncoding(byte b1, byte b2, byte b3, int num) {

	StringBuffer sb = new StringBuffer(num);
	int val = ((b1 << 16) & 0x00ff0000) + ((b2 << 8) & 0x0000ff00) +
			(b3 & 0x000000ff);
	for (int i = 0; i < num; i++) {
	    sb = sb.append(BASE64[(val & 0x0000003f)]);
	    val = (val >> 6);
	}
	return (sb.toString());

    } // getEncoding

} // WorkMod55
