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

package org.wbemservices.wbem.cimom.adapters.client.http;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMInstance;
import javax.wbem.cimxml.CIMXml;
import javax.wbem.cimxml.CIMXmlFactory;
import javax.wbem.client.CIMEvent;
import javax.wbem.client.CIMExportIndication;
import javax.wbem.client.adapter.http.transport.HttpClientConnection;
import javax.wbem.client.adapter.http.transport.HttpClientSocketFactory;
import javax.wbem.client.adapter.http.transport.HttpSocketFactory;
import javax.wbem.client.adapter.http.transport.OutboundRequest;
import javax.wbem.provider.CIMProvider;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.wbemservices.wbem.cimom.CommonServerSecurityContext;
import org.wbemservices.wbem.cimom.EventService;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/** A simple HTTP server, for serving up jar and class files. */
public class HttpIndicationHandler implements EventService.IndicationHandler {

    private final static String ns =
                "http://www.dmtf.org/cim/mapping/http/v1.0";
    private static boolean useMPost = false;
    private Map connMap = new HashMap();
    private CIMXml xmlImpl = CIMXmlFactory.getCIMXmlImpl();
    private boolean debug = false;
    private String prefix = "";
    private final static String defaultScheme = "http"; 

// The following enables XML tracing
    private final static String TRACEFILE = "CIMOMXML_Trace.txt";
    private FileOutputStream fout = null;
    private PrintStream psout = null;

    public HttpIndicationHandler() {
    }

    public CommonServerSecurityContext getSecurityContext(
					CIMInstance handlerInstance) {
	return null;
    }

    public void deliverEvent(CIMEvent e, CIMInstance handlerInstance)
    throws CIMException {
	Document request;
	URL dest = null;

	try {
	    request = xmlImpl.getXmlRequest(
			new CIMExportIndication(e.getIndication()));
            String d = (String)handlerInstance.getProperty("Destination").getValue().getValue();            
            try { 
	       dest = new URL(d);
            } catch (MalformedURLException me) {
                if (d.startsWith("//")) {
                    d = defaultScheme + ":" + d;
                } else {
                    d = defaultScheme + "://" + d;
                }
                dest = new URL(d);
            }
	    // XXX Temporary fix for problem where indications are being lost.
	    // A more permanent fix of the underlying problem needs to be found.
	    // If an exception is thrown, try one more time to deliver this 
	    // event
	    try {
		deliverEventRequest(request, dest);
	    } catch (Exception ex) {
		deliverEventRequest(request, dest);
	    }
	} catch (Exception ex) {
	    throw new CIMException(CIMException.CIM_ERR_FAILED, ex);
	} 
    }

    private void writeDocumentToOutputStream(Document request, PrintStream out) {
	TransformerFactory tFactory = TransformerFactory.newInstance();
	Transformer transformer;
	try {
	    transformer = tFactory.newTransformer();
	    DocumentType docType = request.getDoctype();
	    if (docType != null) {
		String systemId =docType.getSystemId();
		if (systemId != null) {
		    String systemValue = (new File(systemId)).getName();
		    transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, 
			systemValue);
	    	}
	    }

	    DOMSource source = new DOMSource(request);
	    StreamResult result = new StreamResult(out);
	    transformer.transform(source, result);
	} catch (TransformerConfigurationException e) {
	    e.printStackTrace();
	} catch (TransformerException e) {
	    e.printStackTrace();
	}
    }
    private synchronized void deliverEventRequest(Document request, URL url)
    throws CIMException, IOException {
        PrintStream out = null;
        String host = url.getHost();
        int port =  url.getPort();
        HttpClientSocketFactory factory = new HttpSocketFactory(url.getProtocol());
        OutboundRequest outRequest = null;
	String key = host + ":" + port;
	HttpClientConnection conn = (HttpClientConnection)connMap.get(key);

        for (int i = 0; i < 2; i++) {
            try {
                if (conn == null) {
                    conn = new HttpClientConnection(host, port, factory,
                                                    useMPost);
		    connMap.put(key, conn);
		}
		outRequest = conn.newRequest(url.getPath());
                out = new PrintStream(
		    outRequest.getRequestOutputStream(), false, "UTF8");
                setRequestHeaders(outRequest, 
				xmlImpl.getXmlRequestHeaders(request));
                outRequest.endWriteHeader();
		writeDocumentToOutputStream(request, out);
                out.flush();
                out.close();
                break;
            } catch (IOException e) {
                conn.shutdown(true);
                conn = null;
		connMap.put(key, conn);
                if (i == 1)
                    throw e;
            }
        }

	try {

	    if (debug) {
		dumpRequest(request, outRequest);
	    }

	    Document resp = getResponse(outRequest);

	    if (debug) {
		dumpResponse(resp, outRequest);
	    }
	} catch (SAXException e) {
	    if (useMPost) {
	 	if (conn != null) {
		    conn.shutdown(true);
		    conn = null;
		    connMap.put(key, conn);
		} 
		useMPost = false;
		deliverEventRequest(request, url);
	    } else {
		throw new CIMException(CIMException.CIM_ERR_FAILED, e);
	    }
	} catch (Exception e) {
	    throw new CIMException(CIMException.CIM_ERR_FAILED, e);
	} 
    }

    public void ping(CIMInstance handlerInstance) throws CIMException {
    }

    // The following appends XML trace to a file
    private void dumpRequest(Document request, OutboundRequest outRequest)
        throws IOException {

        fout = new FileOutputStream(TRACEFILE, true);
	psout = new PrintStream(fout);
	psout.println(">>>>>>>>>>>>>>>>>>>>>>>" +
			   " START OF REQUEST  " +
			   ">>>>>>>>>>>>>>>>>>>>>>>");
        outRequest.dumpOutHeader(psout);
        if (request != null) {
	    writeDocumentToOutputStream(request, psout);
        }
		psout.println(">>>>>>>>>>>>>>>>>>>>>>>" +
			   " END OF REQUEST  " +
			   ">>>>>>>>>>>>>>>>>>>>>>>");
		fout.close();
		psout.close();

		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>" +
			   " START OF REQUEST  " +
			   ">>>>>>>>>>>>>>>>>>>>>>>");
        outRequest.dumpOutHeader(System.out);
        if (request != null) {
	    writeDocumentToOutputStream(request, System.out);
        }
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>" +
			   " END OF REQUEST  " +
			   ">>>>>>>>>>>>>>>>>>>>>>>");
    }

    private void dumpResponse(Document response,
                              OutboundRequest outRequest)
        throws IOException {
// The following appends XML trace to a file
		fout = new FileOutputStream(TRACEFILE, true);
		psout = new PrintStream(fout);
		psout.println("<<<<<<<<<<<<<<<<<<<<<<<" +
				   " START OF RESPONSE  " +
				   "<<<<<<<<<<<<<<<<<<<<<<<");
		outRequest.dumpInHeader(psout);
		psout.println("\n");
		if (response != null) {
		    writeDocumentToOutputStream(response, psout);
		}
		psout.println("<<<<<<<<<<<<<<<<<<<<<<<" +
				   " END OF RESPONSE  " +
				   "<<<<<<<<<<<<<<<<<<<<<<<");
		fout.close();
		psout.close();

		System.out.println("<<<<<<<<<<<<<<<<<<<<<<<" +
			   " START OF RESPONSE  " +
			   "<<<<<<<<<<<<<<<<<<<<<<<");

        outRequest.dumpInHeader(System.out);

        System.out.println("\n");
        if (response != null) {
	    writeDocumentToOutputStream(response, System.out);
        }
		System.out.println("<<<<<<<<<<<<<<<<<<<<<<<" +
			   " END OF RESPONSE  " +
			   "<<<<<<<<<<<<<<<<<<<<<<<");
    }

    private Document getResponse(OutboundRequest outRequest)
    throws IOException, SAXException, ParserConfigurationException {
        Document d = null;
	InputStream contentInput =
			outRequest.getResponseInputStream();
	try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder builder = dbf.newDocumentBuilder();
            
            InputSource in = new InputSource();
            
            //parser.setDocumentHandler(builder);
	    //TODO: Set a resolver here????
            in.setByteStream(contentInput);
            d = builder.parse(in);
	    if (outRequest.getResponseCode() >= 
			HttpURLConnection.HTTP_BAD_REQUEST) {
                throw new IOException();
            }

            return (Document)d;
	} finally {
	    contentInput.close();
	}
    }

    private void setRequestHeaders(OutboundRequest outRequest,
                             Map headers) throws CIMException {
	if (useMPost) {
	    setPrefix();
	    outRequest.addHeaderField("Man", ns + ";ns=" + this.prefix);
	    prefix += "-";
	} else {
	    prefix = "";
	}
	Iterator it = headers.entrySet().iterator();
	while (it.hasNext()) {
	    Map.Entry entry = (Map.Entry)it.next();
	    outRequest.addHeaderField(prefix + (String)entry.getKey(),
					(String)entry.getValue());
	 }
    }

    private void setPrefix() {
	Random generator = new Random();
	int rand = Math.abs(generator.nextInt() % 100);
	this.prefix = Integer.toString(rand);
	this.prefix = (prefix.length() == 1 ? "0" + this.prefix : this.prefix);
    }
    
    /** Get the Name of the CIM Class that
     *
     */
    public String getClassName() {
	return "CIM_ListenerDestinationCIMXML";
    }
    
    /** Get the CIM Provider for the
     *
     *
     */
    public CIMProvider getCIMProvider() {
        return (CIMProvider)null;
    }
    
}
