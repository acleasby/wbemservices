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
 *Contributor(s): WBEM Solutions, Inc.
 */

package org.wbemservices.wbem.client;

import java.io.FileInputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMNameSpace;
import javax.wbem.client.ClientProperties;
import javax.wbem.client.Debug;
import javax.wbem.security.ClientSecurity;
import javax.wbem.security.SecurityMessage;
import javax.wbem.security.SecurityToken;
import javax.wbem.security.SecurityUtil;
import javax.wbem.security.UserPasswordEncryptionProvider;


/**
 * This class is the client side of a proprietary challenge-response
 * security mechanism which uses the Unix user name and password.
 * This class implements the ClientSecurityContext interface and is
 * created by the ClientSecurityFactory class.  Its security algorithm
 * currently requires an RMI communications service.
 *
 * @author 	Sun Microsystems, Inc.
 * @since	WBEM 1.0
 */
public class SunDigestClientSecurity extends ClientSecurity {

    // Our security mechanism name.
    // Must align with the mechanism name in the WbemDefaults properties!
    private static final String mechanism = "sundigest";

    // Define constants for WBEM local authentication
    private static final String WBEM_LOCAL_TYPE = "__LOCAL";
    private static final String WBEM_LOCAL_DIR = "/var/sadm/wbem/security";
    private static final int    WBEM_LOCAL_NONCE_SIZE = 16;
    private static final String BAD_HASH = ".B.A.D.H.A.S.H.";

    // The client PKI key pair and the digest mechanism is shared!
    // Must synchronize all methods which use the digest.
    private static boolean initialized = false;
    private static MessageDigest md = null;
    private static KeyPairGenerator keygen;
    private static KeyPair keypair;
    private static Signature signer;
    private static PrivateKey cprivkey;
    private static PublicKey cpubkey;
    private static UserPasswordEncryptionProvider pep = null;

    // Instance data for the secure session with a remote server
    private byte[] secret;
    private boolean bLocalMode;

    private PublicKey spubkey;
    private String initialKey = "InitialKey";
    private byte[] cchallenge1;
    private byte[] schallenge1;
    private byte[] sessionId = null;
    private byte[] sessionKey = null;
    private byte[] encryptKey = null;

    private static synchronized void initialize() 
	throws java.security.NoSuchAlgorithmException {

	if (!initialized) {
	    md = MessageDigest.getInstance("MD5");
	    signer = Signature.getInstance("DSA");
	    keygen = KeyPairGenerator.getInstance("DSA");
	    keygen.initialize(1024, SecurityUtil.secrand);
	    keypair = keygen.generateKeyPair();
	    cprivkey = keypair.getPrivate();
	    cpubkey = keypair.getPublic();
	    try {
		String cname = System.getProperty(
		    UserPasswordEncryptionProvider.PSWD_HASH_PROV_PROP);
		if (cname == null) {
		    cname = ClientProperties.getProperty(
			UserPasswordEncryptionProvider.PSWD_HASH_PROV_PROP);
		}
		Class c = Class.forName(cname);
		pep = (UserPasswordEncryptionProvider) c.newInstance();
	    } catch (Exception ex) {
		Debug.trace1("Error getting password encryption provider: " +
				ex.getMessage());
		throw new java.security.NoSuchAlgorithmException(
				"NO_PASSWORD_PROVIDER");
	    }
	    initialized = true;
	}

    }

    public SunDigestClientSecurity()
	throws java.security.NoSuchAlgorithmException {

        super(mechanism);
	initialize();
    }

    public SunDigestClientSecurity(CIMNameSpace ns, Principal prin, Object cred)
	throws java.security.NoSuchAlgorithmException, CIMException {

        super(mechanism);
	this.bLocalMode = false;
	this.setNameSpace(ns);
	this.setPrincipal(prin);
	this.setCredential(cred);

	initialize();
    }

    // =========================================================================
    //
    // Solaris specific public methods in the implementation subclass.
    // Here for compatibility with RMI communications service.
    // Can be removed when we move to a single "authenticate" method
    // that replaces the "assumeRole" method in the RMI comms service.
    //
    // =========================================================================

    public void setPrincipal(Principal prin) throws CIMException {

	super.setPrincipal(prin);
	//if (prin instanceof LocalUserPrincipal) {
	//   this.bLocalMode = true;
	//}
    } // setPrincipal

    public String getRoleName() {
        return null;
    }

    public String getRolePassword() {
        return null;
    }


    // =========================================================================
    //
    // Additional public methods in implementation class (mechanism dependent)
    //
    // =========================================================================

    public byte[] getSessionId() {
	return sessionId;
    }

    public byte[] getChallenge() {
	return schallenge1;
    }

    public String getUserPassword() {
        // Only support passwords of ISO-Latin 8-bit characters.
        // We build the characters explicitly to avoid the default
        // character converter on the system.
        String tPswd = "";
        if (userPswd.length > 0) {
            char [] ca = new char[userPswd.length];
            for (int i = 0; i < userPswd.length; i++) {
                ca[i] = (char) (0 | (userPswd[i] & 0xff));
            }
            tPswd = new String(ca);
        }
        return tPswd;
    }

    public byte[] getSessionKey() {
	return sessionKey;
    }

    public PublicKey getPublicKey() {
	return cpubkey;
    }

    public PublicKey getServerPublicKey() {
	return spubkey;
    }

    public PrivateKey getPrivateKey() {
	return cprivkey;
    }

    public MessageDigest getMD() {
	return md;
    }

    public Signature getSigner() {
	return signer;
    }

    public SecurityMessage generateHello() {
        synchronized (md) {
            this.cchallenge1 = new byte[16];
            SecurityUtil.secrand.nextBytes(this.cchallenge1);
            md.reset();
            md.update(this.cchallenge1);
            md.update(initialKey.getBytes());
            byte[] digest = md.digest();

            String tName = this.userName;
            if (bLocalMode) {
                tName = tName + ":" + WBEM_LOCAL_TYPE;
            }
            byte[] unb = tName.getBytes();
            byte[] nsb = this.nsPath.getBytes();
            byte[] hashedUser = SecurityUtil.hashData(unb, digest);
            byte[] hashednsb = SecurityUtil.hashData(nsb, digest);

            md.reset();
            md.update(this.cchallenge1);
            md.update(initialKey.getBytes());
            md.update(hashedUser);
            return SecurityMessage.hello(
                this.cchallenge1,
                hashedUser,
                hashednsb,
                md.digest(hashednsb));
        }
    }

    /**
     * Generates the "response" message, which is what the client sends
     * in response to the server's challenge.
     *
     * @param sm the message generated when the server calls generateChallenge.
     *
     * @return the "response" message that should be sent to the server and
     * passed into validateResponse.
     *
     */
    public SecurityMessage generateResponse(SecurityMessage sm) {
        synchronized (md) {
            int i;

            if (!sm.isChallenge()) {
                throw new IllegalArgumentException("not a challenge message");
            }

            schallenge1 = sm.getChallenge();
            md.reset();
            md.update(sm.getChallenge());
            byte[] digest = md.digest(initialKey.getBytes());
            md.reset();
            md.update(sm.getChallenge());
            md.update(initialKey.getBytes());
            md.update(sm.getSalt());
            if (!MessageDigest
                .isEqual(md.digest(sm.getSessionId()), sm.getChecksum())) {
                // CIMSecurityCheckSum exception
                throw new IllegalArgumentException("Checksum error");
            }

            byte[] salt = SecurityUtil.extractHashedData(sm.getSalt(), digest);
            sessionId = sm.getSessionId();
            if ((salt == null) || (sessionId == null)) {
                // CIMSecurityIntegrity exception
                throw new IllegalArgumentException("Null salt/session");
            }

            // Get the password encryption and extract the body portion
            // as the shared secret. This is done as follows:
            //
            // If using local mode authentication:
            //   Salt contains: $<local_type>$<filename>
            //   Extract filename, read nonce from file as hexadecimal
            //     character array, convert to byte array, unhash nonce
            //     using client challenge
            //   Unhashed nonce is the shared secret
            //
            // If not using local mode authentication:
            //   Pass salt and clear text password to encryption provider,
            //     which returns password encryption
            //   If encryption begins with a "$", format: $<type>$<salt>$<body>,
            //     extract the <body> as the shared secret
            //   If encryption does not begin with a "$", encryption is from
            //     Unix crypt, use entire encryption as the shared secret

            String sSalt = new String(salt);
            // System.out.println("RmiClient: salt = " + sSalt);
            secret = null;
            if (sSalt.startsWith("$" + WBEM_LOCAL_TYPE + "$")) {
                Debug.trace2("Local authentication for " + this.userName);
                bLocalMode = true;
                String filename = null;
                FileInputStream fis = null;
                i = WBEM_LOCAL_TYPE.length() + 2;
                try {
                    filename = WBEM_LOCAL_DIR + "/" + sSalt.substring(i);
                    Debug.trace3("Authentication file name: " + filename);
                    fis = new FileInputStream(filename);
                    byte[] bb = new byte[WBEM_LOCAL_NONCE_SIZE * 6];
                    int len = fis.read(bb);
                    if (len > 0) {
                        byte[] tb = new byte[len];
                        System.arraycopy(bb, 0, tb, 0, len);
                        String nonceHash = new String(tb, "UTF-8");
                        byte[] sb = SecurityUtil.fromHex(nonceHash);
                        secret =
                            SecurityUtil.extractHashedData(sb, cchallenge1);
                        Debug.trace3(
                            "Authentication shared secret: "
                                + SecurityUtil.toHex(secret));
                    }
                } catch (Exception ex) {
                    Debug.trace1(
                        "Error reading local auth file: " + ex.getMessage());
                    // Eat all exceptions and return a bad hash later.
                }
                try {
                    if (fis != null) {
                        fis.close();
                    }
                } catch (Exception ex) {}
            } else {
                Debug.trace2(
                    "Password based authentication for " + this.userName);
                bLocalMode = false;
                String body = null;
                String pwEncr = null;
                try {
                    pwEncr =
                        pep.encryptPassword(
                            userName,
                            sSalt,
                            new String(this.userPswd));
                } catch (Exception ex) {
                    pwEncr = BAD_HASH;
                }
                if ((pwEncr != null) && (pwEncr.length() > 0)) {
                    if (pwEncr.charAt(0) == '$') {
                        try {
                            i = pwEncr.lastIndexOf('$');
                            body = pwEncr.substring(i + 1);
                        } catch (Exception ex) {}
                    } else {
                        body = pwEncr;
                    }
                }
                if (body != null) {
                    try {
                        secret = body.getBytes("UTF-8");
                        Debug.trace3(
                            "Authentication shared secret: "
                                + new String(secret));
                    } catch (Exception ex) {}
                }
            }
            // If cannot get password hash, use a bad hash to force an
            // error from the server. Allows server to clean up.
            if (secret == null) {
                try {
                    secret = BAD_HASH.getBytes("UTF-8");
                    Debug.trace3("Authentication shared secret is bad ");
                } catch (Exception ex) {}
            }

            // calculate f, hash it with challenge, initial key,
            // then xor in password
            md.reset();
            md.update(sm.getChallenge());
            md.update(initialKey.getBytes());
            md.update(secret);
            digest = md.digest();
            byte[] pwHash = null;
            if (bLocalMode) {
                pwHash = SecurityUtil.hashData(secret, digest);
            } else {
                pwHash = SecurityUtil.hashData(this.userPswd, digest);
            }

            md.reset();
            md.update(sm.getChallenge());
            md.update(initialKey.getBytes());
            md.update(secret);
            md.update(pwHash);
            md.update(cpubkey.getEncoded());

            return SecurityMessage.response(
                pwHash,
                cpubkey,
                sessionId,
                md.digest(sessionId));
        }
    }

    /**
     * Checks the result returned by the server after the client sends a 
     * challenge. This call indicates to the client whether or not (mutual)
     * authentication was successful. 
     *
     * @param sm the message generated when the server calls generateChallenge.
     */
    public byte[] checkResult(SecurityMessage sm) {
        synchronized (md) {
            if (!sm.isResult()) {
                throw new IllegalArgumentException("not a result message");
            }

            byte[] bPswd;
            if (bLocalMode) {
                bPswd = this.secret;
            } else {
                bPswd = this.userPswd;
            }
            md.reset();
            md.update(this.getChallenge());
            md.update(bPswd);
            byte[] digest = md.digest();

            md.reset();
            md.update(this.getChallenge());
            md.update(bPswd);
            md.update(sm.getSessionId());
            spubkey = sm.getPublicKey();
            md.update(spubkey.getEncoded());
            md.update(sm.getResponse());

            if (!MessageDigest.isEqual(md.digest(), sm.getChecksum())) {
                Debug.trace1(
                    "Authentication checksum failure for " + this.userName);
                throw new IllegalArgumentException("mutual authentication failed");
            }

            sessionKey =
                SecurityUtil.extractHashedData(sm.getResponse(), digest);

            if (sessionKey == null) {
                // CIMSecurityIntegrity exception
                throw new IllegalArgumentException("Null response");
            }

            // Copy the current session key into the encryptKey used for
            // encrypting data passed to the server before it gets incremented.
            encryptKey = new byte[sessionKey.length];
            System.arraycopy(sessionKey, 0, encryptKey, 0, sessionKey.length);

            // Throwing away the password for security
            return sessionKey;
        }
    }

    public SecurityToken getSecurityToken(String[] sarray) {

        synchronized (md) {
            SecurityToken st = new SecurityToken();
            byte[] ser;

            String s = "";
            for (int i = 0; i < sarray.length; i++) {
                s = s + sarray[i];
            }
            ser = s.getBytes();

            MessageDigest digester = getMD();
            digester.reset();
            digester.update(getSessionKey());
            digester.update(ser);
            byte[] digest = digester.digest();
            st.setChecksum(digest);
            st.setSessionId(getSessionId());
            /*
	     * st.setSignature(SecurityUtil.signDigest(digest, getPrivateKey(),
	     * getSigner()));
	     */

            return st;
        }
    }

    public synchronized void incSessionKey() {
	SecurityUtil.incByteArray(sessionKey);
    }
}
