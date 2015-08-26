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
 *are Copyright (c) 2002 Sun Microsystems, Inc.
 *
 *All Rights Reserved.
 *
 *Contributor(s): WBEM Solutions, Inc.
*/

package javax.wbem.client.adapter.http.transport;

import java.io.IOException;
import java.net.Socket;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 *
 * @author  mh127215
 * @version 
 */
public class HttpSocketFactory implements HttpClientSocketFactory {

    private static SocketFactory fact = null;
    private static SocketFactory sslfact = null;
    private boolean useSSL = false;

    // Create a trust manager that does not validate certificate chains
    static TrustManager[] trustAllCerts = new TrustManager[]{
        new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
            public void checkClientTrusted(
                java.security.cert.X509Certificate[] certs, String authType) {
		// System.out.println(authType);
		for (int i=0; i < certs.length; i++) {
			//  System.out.println(certs[i]);
		}
            }
            public void checkServerTrusted(
                java.security.cert.X509Certificate[] certs, String authType) {
		// System.out.println(authType);
		for (int i=0; i < certs.length; i++) {
		    //  System.out.println(certs[i]);
		}
	    }
        }
    };
       /** Creates new socket factory */
    public HttpSocketFactory(String protocol) {
	// if protocol is https, create secure socket factory.  Otherwise
	// create regular socket factory
        if (protocol.toLowerCase().equals("https")) {   
            useSSL = true;
            synchronized(this) {
                if (sslfact == null) {
                    try {
                        SSLContext ctx = SSLContext.getInstance("SSLv3");
                        // Install the all-trusting trust manager
                        ctx.init(null, trustAllCerts, 
				 new java.security.SecureRandom());
                
                        // get the socket factory
                         sslfact = ctx.getSocketFactory();
                        //    sslfact = SSLSocketFactory.getDefault();
                    }
                    catch (Exception ex) {
                        if (sslfact == null) {
                            sslfact = SSLSocketFactory.getDefault();
                        }
                    }
                }
            }
	} else {
            useSSL = false;
            synchronized(this) {
                // first in creates the factory. Others just use it.
                if (fact == null) {
                    fact = SocketFactory.getDefault();
                }
            }
        }
    }
     /**
      * Creates client socket connected to the given host and port.
     */
    public Socket createSocket(String host, int port) throws IOException {
	// if useSSL is true, return secure socket.  If not, return
	// regular socket
        if (useSSL) {
            return sslfact.createSocket(host, port);
        } else {
            return fact.createSocket(host,port);
        }
    }
    /**
     * Creates layered socket on top of given base socket, for use when
     * tunneling HTTP messages through a proxy.
     */
    public Socket createTunnelSocket(Socket s) throws IOException {
        throw new IOException("not supported");
    }
}


