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
 *Contributor(s): WBEM Solutions, Inc.
*/

package org.wbemservices.wbem.cimom.adapters.client.http;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.PasswordAuthentication;
import java.util.StringTokenizer;

import javax.wbem.client.Debug;
import javax.wbem.client.adapter.http.transport.HttpServerRequestHandler;
import javax.wbem.client.adapter.http.transport.InboundRequest;

import org.w3c.dom.Document;
import org.wbemservices.wbem.cimom.CIMOMServer;
import org.wbemservices.wbem.cimom.security.UserPasswordProvider;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

class CIMRequestHandler extends HttpServerRequestHandler {
    private String servername;
    private CIMOMServer cimom = null;
    private PasswordAuthentication auth;
    private UserPasswordProvider upp = null;

    CIMRequestHandler(CIMOMServer cimom, String serverName)
    throws Exception {
	this.cimom = cimom;
  	this.upp = this.getUserPasswordProvider();
    }
 

    public boolean checkAuthentication(InboundRequest request) {

	String str = request.getHeaderField("Authorization");
	if (str == null) {
	    request.setRespondHeaderField("WWW-Authenticate", "Basic realm=\""
                     + getRealm() + "\"");
	    try {
		Document xmlDoc = getXmlDocument(request);
	    } catch (Exception e) {}


	    return false;

	}

	auth = verifyAuthentication(str);
	return auth != null;

    }
	
    public void addResponseHeaderFields(InboundRequest request) {
	request.setRespondHeaderField("CIMOperation", "MethodResponse");
    }

    public void handleRequest(InboundRequest request) {
	try {
	    Document xmlDoc = getXmlDocument(request);
	    DataOutputStream out = 
		new DataOutputStream(request.getResponseOutputStream());
	    rpc(xmlDoc, auth, out);
	    //xmlDoc.write(out);
	} catch (SAXException e) {
	    DataOutputStream out = 
		new DataOutputStream(request.getResponseOutputStream());
	    Exception x = e.getException();
	    String message = "Parse Error with Request:\n";
	    if (x == null) { x = e;
	    }
	    if (e instanceof SAXParseException) {
		SAXParseException spe = (SAXParseException) e;
		message += "** URI: " + spe.getSystemId() + "\n";
		message += "** Line: " + spe.getLineNumber()  + "\n";
	    }

	    // Output an error to the client
	    do500Error(out, "XML Parsing error: <b>" +
		       message + "</b>");
	    Debug.trace1("HTTP: CIMRequestHandler.handleRequest: " + message);
	    Debug.trace1("HTTP: CIMRequestHandler.handleRequest", e);
	} catch (IOException e) {
	    Debug.trace1("HTTP: CIMRequestHandler.handleRequest", e);
	} catch (Exception e) {
	    Debug.trace1("HTTP: CIMRequestHandler.handleRequest", e);
	}
    }
     /** Allocate a password provider to check user authentication */
    private UserPasswordProvider getUserPasswordProvider() throws Exception {
	UserPasswordProvider tupp = null;
	try {
	    Class c = Class.forName(System.getProperty(
				UserPasswordProvider.PSWD_PROV_PROP));
	    tupp = (UserPasswordProvider) c.newInstance();
	} catch (Exception e) {
	    Debug.trace1("Http: error getting password provider", e);
	    throw e;
	}
	return (tupp);
    }

    private PasswordAuthentication verifyAuthentication(String authenticator) {

        PasswordAuthentication auth = null;
	String auth_type = null;
	String auth_value = null;
	if (authenticator != null) {
	    StringTokenizer st = new StringTokenizer(authenticator);
	    try {
		auth_type = st.nextToken();
		auth_value = st.nextToken();
	    } catch (Exception ex) {
		// Ignore exceptions; values will be null
	    }
	}
	if ((auth_type != null) && (auth_value != null) &&
	    (auth_type.equalsIgnoreCase("Basic"))) {

	    BASE64Decoder decoder = new BASE64Decoder();
	    byte ba[];
	    try {
	        ba = decoder.decodeBuffer(auth_value);
	    } catch (Exception ex) {
		ba = new byte[0];
	    }
	    int j = ba.length;
	    char ca[] = new char[j];
	    int k = 0;
	    for (int i = 0; i < j; i++) {
		ca[i] = (char) ba[i];
		if (ca[i] == ':') {
		    k = i;
                }
	    }
	    if (k > 0) {
		String username = new String(ca, 0, k);
		int len = j - k - 1;
		char password[] = new char[len];
		for (int i = 0; i < len; i++) {
		    k++;
		    password[i] = ca[k];
		}
		String userpswd = new String(password);
		if (this.upp != null) {
		    try {
			if (this.upp.authenticateUser(username, userpswd)) {
//				UserPasswordProvider.NORMAL_USER)) {
			    auth = new PasswordAuthentication(username, 
							password);
			    Debug.trace1("Http: client authenticated: " + 
							username);
			}
		    } catch (Exception ex) {
			auth = null;
		    }
		    if (auth == null) {
			Debug.trace1("Http: invalid credentials: " + username);
		    }
		}
	    }
	}

	if (auth == null) {
	    Debug.trace1("Http: invalid authentication");
	}

	return (auth);

    }

    private String getRealm() {

	// The realm for our CIMOM is wbem@<host>
	String realm = "wbem@" + servername;
	return (realm);

    }

    private void rpc(Document request, PasswordAuthentication auth, 
    	DataOutputStream out) {
        try {
            XmlResponder rsp = new XmlResponder(cimom);
	    rsp.getResponse(request, auth, out);
        } catch (IOException e) {
	    Debug.trace1("CIMRequstHandler: rpc", e);
        } catch (SAXException e) {
	    Debug.trace1("CIMRequstHandler: rpc", e);
	}
     }
}
