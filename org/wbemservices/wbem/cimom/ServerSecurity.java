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
import javax.wbem.client.CIMSecurityException;
import javax.wbem.client.Debug;
import javax.wbem.security.SecurityMessage;
import javax.wbem.security.SecurityToken;
import javax.wbem.security.SecurityUtil;

import org.wbemservices.wbem.cimom.security.UserPasswordProvider;

import java.security.PublicKey;
import java.security.KeyPairGenerator;
import java.security.KeyPair;
import java.security.Signature;
import java.security.PrivateKey;
import java.security.MessageDigest;
import java.io.File;
import java.util.Arrays;

import org.wbemservices.wbem.client.adapter.rmi.RemoteCIMListener;

/**
 * Contains an implementation of challenge-response based security session setup
 * based on the user name and password. Some implementation is based on falcon's
 * Ucrp security mechanism. Also contains methods and context information about
 * the current security session.
 */
public final class ServerSecurity implements CommonServerSecurityContext {

    public final static long AUDIT_NO_SUCH_USER = -1;
    public final static long AUDIT_BAD_PASSWD = -2;
    public final static long AUDIT_SUCCESS = 1;

    private final static String WBEM_LOCAL_TYPE = "__LOCAL";
    private final static String WBEM_LOCAL_DIR  = "/var/sadm/wbem/security";
    private final static int    WBEM_LOCAL_NONCE_SIZE = 16;

    private static ThreadLocal requestSession = new ThreadLocal();
    protected static byte[] adminCred = null;
    private static boolean initialized = false;
    private static KeyPairGenerator keygen;
    private static KeyPair keypair;
    private static Signature signer;
    private static PrivateKey sprivkey;
    private static PublicKey spubkey;
    private MessageDigest md = null;
    private PublicKey cpubkey;
    private String userName = null;
    private String roleName = null;
    private String authName = null;
    private String localFile = null;
    private String clientHost = null;
    private String initialKey = "InitialKey";
    private byte[] schallenge1;
    private byte[] sessionId = null;
    private byte[] sessionKey = null;
    private byte[] decryptKey = null;
    private byte[] auditKey = null;
    private byte[] sf;
    private byte[] nameSpace;
    private String cp = null;
    private String cversion;
    private String cap = "none";
    private String capNameSpace = "__junk__";
    private boolean bLocalMode = false;

    private static UserPasswordProvider upp = null;
    private RemoteCIMListener rl=null;

    static UserPasswordProvider getUserPasswordProvider() throws Exception {
	Class c;
	if (upp == null) {
	    try {
		c = Class.forName(System.getProperty
				(UserPasswordProvider.PSWD_PROV_PROP));
		upp = (UserPasswordProvider)c.newInstance();
	    } catch (Exception e) {
		Debug.trace1("Sundigest: Error getting password provider", e);
		// LogFile.add(LogFile.CRITICAL, "NO_SECURITY_INTERFACE", 
		//		e.toString());
		throw e;
	    }
	}
	return (upp);
    }

    public ServerSecurity() throws Exception {
	if (!initialized) {
	    signer = Signature.getInstance("DSA");
	    keygen = KeyPairGenerator.getInstance("DSA");
	    keygen.initialize(1024, SecurityUtil.secrand);
	    keypair = keygen.generateKeyPair();
	    sprivkey = keypair.getPrivate();
	    spubkey = keypair.getPublic();
	    initialized = true;
	}
	upp = getUserPasswordProvider();
	md = MessageDigest.getInstance("MD5");
    }

    public ServerSecurity(String userName, String roleName, 
                String hostname, byte[] auditKey) {
        this.userName = userName;
        this.roleName = roleName;
        setClientHostName(hostname);
        ServerSecurity.setRequestSession(this);
        this.auditKey = auditKey;
    }


    public String getClientVersion() {
	return cversion;
    }

    public String getCapability() {
	return cap;
    }

    public String getCapabilityNS() {
	return capNameSpace;
    }

    public byte[] getSessionId() {
	return sessionId;
    }

    public byte[] getChallenge() {
	return schallenge1;
    }

    public String getPasswd() {
	return cp;
    }

    public byte[] getNameSpace() {
	return nameSpace;
    }

    public byte[] getShadow() {
	return sf;
    }

    public byte[] getSessionKey() {
	return sessionKey;
    }

    public PublicKey getPublicKey() {
	return spubkey;
    }

    public PublicKey getClientPublicKey() {
	return cpubkey;
    }

    public PrivateKey getPrivateKey() {
	return sprivkey;
    }

    public MessageDigest getMD() {
	return md;
    }

    public Signature getSigner() {
	return signer;
    }

    public RemoteCIMListener getListener() {
	return rl;
    }

    public void setCapability(String cap) {
	this.cap = cap;
    }

    public void setCapabilityNS(String ns) {
	this.capNameSpace = ns;
    }

    public void setClientHostName(String hostName) {
	this.clientHost = hostName;
    }

    public void setListener(RemoteCIMListener rl) {
	this.rl = rl;
    }

    /**
     * generates the challenge in response to a client's "hello" message.
     *
     * Note that the value of "f" is the encrypted UNIX password, normally
     * obtained from the getspnam(3) call. It must be passed into this call.
     * The caller should use the username in the hello message to get the
     * password.
     *
     * @param cm the "hello" message generated the client calls generateHello.
     * @param sessionId A unique id that the server assigns to the session.
     *
     * @return the "challenge" message which should be sent to the client and
     * passed into generateResponse.
     *
     */

    public SecurityMessage generateChallenge(String version,
					SecurityMessage cm, 
    					byte[] sessionId) throws CIMException
    {
	if (!cm.isHello()) {
	    throw new CIMSecurityException(CIMSecurityException.NOT_HELLO);
        }

	this.cversion = version;
	this.sessionId = sessionId;

	byte[] cchallenge1 = cm.getChallenge();
	md.reset();
	md.update(cchallenge1);
	byte[] userHash = md.digest(initialKey.getBytes());

	md.reset();
	md.update(cchallenge1);
	md.update(initialKey.getBytes());
	md.update(cm.getUserDigest());
	md.update(cm.getNameSpace());
	if (!MessageDigest.isEqual(md.digest(), cm.getChecksum())) {
	    Debug.trace1("Sundigest: user authentication; request checksum error");
	    throw new CIMSecurityException(CIMSecurityException.CHECKSUM_ERROR);
	}

	byte[] unb = SecurityUtil.extractHashedData(cm.getUserDigest(), 
							userHash);
	nameSpace = SecurityUtil.extractHashedData(cm.getNameSpace(), 
							userHash);
	userName = new String(unb);

	if (unb == null) {
	    Debug.trace1("Sundigest: user authentication; no user name");
	    // write a BSM audit record indicating failure
	    upp.auditLogin(this.clientHost, this.userName, AUDIT_NO_SUCH_USER);

	    throw new CIMSecurityException(
		CIMSecurityException.NO_SUCH_PRINCIPAL,
		null);
	}

	authName = new String(unb);
	Debug.trace3("Sundigest: user = " + authName);
	int i = authName.indexOf(':');
	if (i > 0) {
	    this.userName = authName.substring(0, i);
	    if ((authName.length() > i) &&
		(authName.substring(i+1).equals(WBEM_LOCAL_TYPE))) {
		bLocalMode = true;
	    } else {
		Debug.trace1("Sundigest: invalid user type: " + authName);
		throw new CIMSecurityException(
			CIMSecurityException.NO_SUCH_PRINCIPAL, "INVALID_TYPE");
	    }
	} else {
	    this.userName = authName;
	    bLocalMode = false;
	}

	// The specified user identity must have a password on this server.
	String shadow = upp.getEncryptedPassword(this.userName,
				UserPasswordProvider.NORMAL_USER);
	if ((shadow == null) || (shadow.length() == 0)) {
	    Debug.trace1("Sundigest: user authentication; bad user name: " +
				this.userName);
	    // write a BSM audit record indicating failure
	    upp.auditLogin(this.clientHost, this.userName, AUDIT_NO_SUCH_USER);
	    throw new CIMSecurityException(
		CIMSecurityException.NO_SUCH_PRINCIPAL, this.userName);
	}
	byte[] salt = null;
	if (bLocalMode) {

	// If authenticating in local mode, we do not use the
	// encrypted password as the shared secret.  Instead, we
	// allocate a random nonce value, hash it with the client's
	// challenge value, convert it to a hexadecimal string,
	// and write it into a local file in /var/sadm/wbem/security.
	// The file is changed such that only the authentication user
	// identity can read it.  We then create a "fake" salt value
	// consisting of the format: "$<local_type>$<filename>".
	// The nonce value is the shared secret.

	    sf = new byte[WBEM_LOCAL_NONCE_SIZE];
	    SecurityUtil.secrand.nextBytes(sf);
	    Debug.trace3("Sundigest: shared secret: " + SecurityUtil.toHex(sf));
	    byte[] hnb = SecurityUtil.hashData(sf, cchallenge1);
	    String hnx = SecurityUtil.toHex(hnb);
	    localFile = null;
	    try {
		localFile = upp.writeLocalAuthenticator(userName,
				WBEM_LOCAL_DIR, hnx);
		i = localFile.lastIndexOf(File.separatorChar);
		if (i > 0) {
		    localFile = localFile.substring(i+1);
		}
		String sSalt = "$" + WBEM_LOCAL_TYPE + "$" + localFile;
		salt = sSalt.getBytes("UTF-8");
	    } catch (Exception ex) {
		Debug.trace1("Sundigest: error writing local auth file: "
				+ ex.getMessage());
		throw new CIMSecurityException(CIMException.CIM_ERR_FAILED,
				"WRITE_LOCAL_AUTHENTICATOR");
	    }

	} else {

	// Not authenticating in local mode; use password encryption.
	// Extract the salt and body from the password encryption.
	// If the encryption begins with a "$", we have the format
	// $<type>$<salt>$<body".  Set the salt value to $<type>$<salt>.
	// If the encryption does not begin with a "$", assume a crypt
	// encryption where the first two characters are the salt.

	    if (shadow.charAt(0) != '$') {
		sf = shadow.getBytes();
		salt = new byte[] { sf[0], sf[1] };
	    } else {
		i = shadow.lastIndexOf('$');
		try {
		    sf = shadow.substring(i+1).getBytes();
		    salt = shadow.substring(0, i).getBytes();
		} catch (Exception ex) {
		    Debug.trace1("Sundigest: bad password encryption: "
		    		+ shadow);
		    throw new CIMSecurityException(
				CIMSecurityException.INVALID_CREDENTIAL);
		}
	    }
	    Debug.trace3("Sundigest: shared secret: " + new String(sf));
	}
	Debug.trace3("Sundigest: salt = " + new String(salt));


	Debug.trace3("Sundigest: request valid: " + this.userName);
	schallenge1 = new byte[16];
	SecurityUtil.secrand.nextBytes(schallenge1);

	md.reset();
	md.update(schallenge1);
	md.update(initialKey.getBytes());
	byte[] digest = md.digest();

	byte[] hashedSalt = SecurityUtil.hashData(salt, digest);
	
	md.reset();
	md.update(schallenge1);
	md.update(initialKey.getBytes());
	md.update(hashedSalt);
	// create the message
	return SecurityMessage.challenge(schallenge1, hashedSalt, 
				sessionId, md.digest(sessionId));
    }

    /**
     * generates the "result" message to a client's "response" message.
     *
     * @param cm the "response" message generated the client calls 
     * generateResponse.
     *
     * @return the "result" message which should be sent to the client.
     *
     */

    public SecurityMessage validateResponse(byte[] challenge, 
			byte[] f, PublicKey pubkey,
			byte[] sessionKey, SecurityMessage cm)
			throws CIMException
    {

        if (bLocalMode) {
            this.removeLocalFile();
        }

	if (!cm.isResponse()) {
	    throw new CIMSecurityException(CIMSecurityException.NOT_RESPONSE);
	}

	this.sessionKey = sessionKey;
	cpubkey = cm.getPublicKey();
	// first calculate the same digest as the client would have
	md.reset();
	md.update(challenge);
	md.update(initialKey.getBytes());
	byte digest[] = md.digest(f);

	md.reset();
	md.update(challenge);
	md.update(initialKey.getBytes());
	md.update(f);
	md.update(cm.getResponse());
	md.update(cm.getPublicKey().getEncoded());
	md.update(cm.getSessionId());
	if (!MessageDigest.isEqual(md.digest(),
					cm.getChecksum())) {
	    Debug.trace1("Sundigest: invalid credentials: " + userName);
	    // write a BSM audit record indicating failure
	    upp.auditLogin(this.clientHost, this.userName, AUDIT_BAD_PASSWD);

	    // This could be due to a checksum error, but it is mainly
	    // when the shadow does not match, so I'm throwing an invalid
	    // credential here.
	    throw new CIMSecurityException(
	    CIMSecurityException.INVALID_CREDENTIAL);
	}

	byte[] response = SecurityUtil.extractHashedData(cm.getResponse(), 
								digest);
	if (response == null) {
	    // write a BSM audit record indicating failure
	    upp.auditLogin(this.clientHost, this.userName, AUDIT_BAD_PASSWD);

	    throw new CIMSecurityException(
	    CIMSecurityException.INVALID_CREDENTIAL);
	}

	// Authenticate user password via user password provider
	boolean bOk = false;
	if (bLocalMode) {
	    if (Arrays.equals(response, f)) {
		bOk = true;
	    }
	} else {
	    try {
		bOk = upp.authenticateUser(userName,
			new String(response, "UTF-8"));
	    } catch (Exception utfexc) {
		Debug.trace1("Sundigest: " +
		    "error creating password string with UTF-8 converter: "
				+ utfexc.getMessage());
	    }
	}
	if (! bOk) {
	    Debug.trace1("Sundigest: invalid credentials: " + this.userName);
	    // write a BSM audit record indicating failure
	    upp.auditLogin(this.clientHost, this.userName, AUDIT_BAD_PASSWD);

	    // CIMSecurityInvalidPasswd exception
	    throw new CIMSecurityException(
	    CIMSecurityException.INVALID_CREDENTIAL);
	}
	Debug.trace1("Sundigest: client authenticated: " + userName);

	// Save the session key value before it gets incremented
	// so it can be used for decrypting data.
	// Use the first 4 bytes of the session key as the audit key.
	decryptKey = new byte[sessionKey.length];
	System.arraycopy(sessionKey, 0, decryptKey, 0, decryptKey.length);
	auditKey = new byte[4];
	System.arraycopy(sessionKey, 0, auditKey, 0, 4);

	md.reset();
	md.update(challenge);
	digest = md.digest(response);
	byte[] hashedKey = SecurityUtil.hashData(sessionKey, digest);

	md.reset();
	md.update(challenge);
	md.update(response);
	md.update(cm.getSessionId());
	md.update(pubkey.getEncoded());

	// Write a BSM audit record indicating success
	upp.auditLogin(this.clientHost, this.userName, AUDIT_SUCCESS);

	return SecurityMessage.result(cm.getSessionId(), pubkey, hashedKey, 
					md.digest(hashedKey));

    }

    public void assumeRole(String role_name, String encr_pswd)
	throws CIMException {


	// Verify that the role name is a valid role identity.

	// Verify that the password is correct.
	// Note that the role password was passed encrypted.
	String role_pswd = this.trans51Unformat(encr_pswd);
	if (! upp.authenticateRole(role_name, role_pswd, this.userName)) {
	    Debug.trace1("Solarisdigest: role assumption; : " +
			    "Could not assume role " + role_name +
			    " for user " + this.userName);
	    // What should the audit log entry be for ?
	    // Nothing exists for assuming a role.
	    upp.auditLogin(this.clientHost, role_name, AUDIT_BAD_PASSWD);
	    throw new CIMSecurityException(
			CIMSecurityException.CANNOT_ASSUME_ROLE,
			this.userName, role_name);
	}

	// All set.  Set the role name in our security context.
	this.roleName = role_name;
	Debug.trace1("Solarisdigest: role assumed: " + this.roleName);

	// Should we write a BSM audit record indicating success?
	 upp.auditLogin(this.clientHost, role_name, AUDIT_SUCCESS);
    }

    public void authenticateRequest(String[] sarray, 
                                    SecurityToken st)
        throws CIMException {

        ServerSecurity.setRequestSession(this);
        MessageDigest digester = getMD();
        byte[] ser;

        String s = "";
        for (int i = 0; i < sarray.length; i++) {
            s = s+sarray[i];
        }
        ser = s.getBytes();

        digester.reset();
        digester.update(getSessionKey());
        digester.update(ser);
        byte[] digest = digester.digest();
        if (!MessageDigest.isEqual(digest, st.getChecksum())) {
            Debug.trace1("Sundigest: method authentication; invalid digest");
            throw new CIMSecurityException(CIMSecurityException.CHECKSUM_ERROR);
        }
        /*
        if (!SecurityUtil.verifyDigest(digest, getSigner(), st.getSignature(),
                                        getClientPublicKey())) {
            throw new CIMSecurityException(
            CIMSecurityException.SIGNATURE_ERROR);
        }
        */

        // LogFile.methodReturn("authenticateRequest");
    }

    public void incSessionKey() {
	SecurityUtil.incByteArray(sessionKey);
    }

    public String trans51Unformat(String inData) {


        byte[] pwb = SecurityUtil.fromHex(inData);
        if (pwb == null) {
            return null;
        }

        // Now we can extract the decryption key.  If this key is null,
        // return null.
        if (decryptKey == null) {
            return null;
        }
        int len = decryptKey.length;

        // Now decrypt the byte value using the decryption key and
        // make into a new string value (assumes ASCII characters!).
        // First copy the key into the response byte array
        byte[] rb = new byte[len];
        System.arraycopy(decryptKey, 0, rb, 0, len);

        // XOR in the encrypted password over the key up to the zero byte
        // that marks the end of the password bytes
        boolean sw = true;
        int i, j, k;
        for (i = 0, j = 0, k = len; i < k; i++) {
            rb[i] ^= pwb[i];
            if (sw && (rb[i] == 0)) {
                sw = false;
                j = i;
            }
        }

        // Check that we have a valid password length
        if ((j < 1) || (j > MAX_DATA_SIZE)) {
            return null;
        }

        // Extract just the password bytes and convert to a string
        String sval = new String(rb, 0, j);
        return (sval);

    }

    // ===================================================================
    //
    // Static methods to get and set CommonServerSecurityContext
    //
    // ===================================================================

    public static void setRequestSession(CommonServerSecurityContext newCtx) {
        requestSession.set(newCtx);
    }

    // Package protected method to return the server security context
    // Only classes in this package can use this information!
    static CommonServerSecurityContext getRequestSession() {
	    return (CommonServerSecurityContext)(requestSession.get());
    }

    // ===================================================================
    //
    // Implementation of CommonServerSecurityContext interface
    //
    // ===================================================================

    // Get current session client user name
    public String getUserName() {
        return (this.userName);
    }

    // Get current session client role name
    public String getRoleName() {
        return (this.roleName);
    }

    // Get current session client host name
    public String getClientHostName() {
        return (this.clientHost);
    }

    // Get an audit identifier based on current session id
    public int getAuditId() {

		// Use the low order bits of the session key as the audit it.
		int i = 0;
		if (auditKey != null) {
        	i =     (((auditKey[0]) & 0x000000ff) << 24);
        	i = i | (((auditKey[1]) & 0x000000ff) << 16);
        	i = i | (((auditKey[2]) & 0x000000ff) << 8);
        	i = i |  ((auditKey[3]) & 0x000000ff);
		}

        return (i);
    }

    // ===================================================================
    //
    // Internal methods
    //
    // ===================================================================

    // For local authentication mode, remove the authenticator file.

    private void removeLocalFile() {

	if ((localFile != null) && (localFile.length() > 0)) {
	    try {
		File fd = new File(WBEM_LOCAL_DIR + File.separator +
				localFile);
		fd.delete();
	    } catch (Exception ex) {
		// Eat errors
	    }
	}
    }

    // Maximum size of an encrypted value in unencrypted characters
    // Should be one less than the session key size!
    private final static int MAX_DATA_SIZE = 15;
}
