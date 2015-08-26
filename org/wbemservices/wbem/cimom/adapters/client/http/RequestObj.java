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
 *are Copyright (c) 2001 Sun Microsystems, Inc.
 *
 *All Rights Reserved.
 *
 *Contributor(s): _______________________________________
*/

package org.wbemservices.wbem.cimom.adapters.client.http;

import java.util.Hashtable;
import java.util.Enumeration;

public class RequestObj {

    // the method name
    private String methodLine;
    
    // hashtable to hold header values
    private Hashtable headers;

    // the body of the message
    private String body;
    
    // constructor
    public RequestObj () {
        headers = new Hashtable();
    }
    
    // set the method name
    public void setMethodLine (String mLine) {
        methodLine = mLine.trim();
    }    
    
    // set the headers up
    public void addHeader (String line) {
	    // Parse the header name and value
	    int colon = line.indexOf(":");
	    if (colon < 0) {
	        System.out.println("ERROR: Wrong Header Line: " +  line );
	        return;
	    }
        
	    String name = line.substring(0, colon).trim();
            /*
             * Per section 4.2 of the HTTP spec, header field names should be
             * case insensitive, as such we shoudl store these as lower case
             * strings.
             */
            name = name.toLowerCase();
            
	    String value = line.substring(colon + 1).trim();

//	    System.out.println("HEADER: " +name + " " + value);
	    headers.put(name, value);
    }    
    
    public void setBody (String body) {
        this.body = body;
    }
    
    // see if the headers hash has a value
    public boolean hasValue(String key) {
        boolean b;
        /*
         * per section 4.2 of the HTTP spec, header fields should be case
         * insensitive, as such we store them in the hashtable in lowercase.
         * therefore we must convert the key to be lowercase
         */
        key = key.toLowerCase();
        
        if (headers.containsKey(key)) {
            b = true;
        } else { 
            b = false;
        }
        return b;
    }             
            
    // return the headers value
    public String getHeaderValue (String key) {
        String value;
        /*
         * per section 4.2 of the HTTP spec, header fields should be case
         * insensitive, as such we store them in the hashtable in lowercase.
         * therefore we must convert the key to be lowercase
         */
        key = key.toLowerCase();
        
	value=(String)headers.get(key);
        return value;
    }             
    
    public String getBody () {
        return this.body;
    }
    
    public String getMethodName () {
        if (methodLine == null) {
            return null;
        }
            
        String name = null;
        // we only care about post right now so
        // only the fist 4 chars matter to us
        name = methodLine.substring(0, 4);  
        
        return name;  
    }
    
    public void print () {
    
        System.out.println ("================REQUEST================");
        
        if(methodLine == null) {
		    System.out.println("ERROR no method line");
            return;
	    }
        
        System.out.println ("Method Name: " + methodLine);
	    
        if(headers == null) {
		    System.out.println("ERROR no response header, expecting header");
            return;
	    }
            
	    Enumeration e=headers.keys();
	    while( e.hasMoreElements()) {
		    String key=(String)e.nextElement();
		    System.out.println("Key value: " + key);
		    String value=(String)headers.get(key);
		    if( value==null || value.indexOf( value ) <0 ) { 
		        System.out.println("No value present:");
                    } else {    
		        System.out.println("Value: " + value);
                    }
        }   
        
        if (this.body != null) {
		    System.out.println(" -------Body --------");
		    System.out.println(body);
        }    
        else {
		    System.out.println("ERROR no body");
        }
            
        System.out.println ("================END REQUEST================");
                    
    } // end print
}
