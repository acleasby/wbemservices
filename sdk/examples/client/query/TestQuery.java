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

import java.io.IOException;
import java.util.Enumeration;

import javax.wbem.cim.*;
import javax.wbem.client.*;
import javax.wbem.client.UserPrincipal;
import javax.wbem.client.PasswordCredential;

/**
 * This class will perform an CIMClient.execQuery using a WQL query string that
 * is passed on the command line.  If a query isn't passed on the command line,
 * the user will be prompted for the query
 */
public class TestQuery {

    public TestQuery(String args[]) {
	    CIMClient cc = null;
	if ((args.length < 4) || (args.length > 5)) {
	    System.out.println("Usage: TestQuery hostname user passwd " +
			       "protocol(rmi|http) 'WQL_query'");
	    System.exit(1);
	    }
	try {
	    String queryString;
	    // If no transfer protocol is given, HTTP is the default 
	    String protocol = CIMClient.CIM_XML;

	    // created a CIMNameSpace object with the hostname given.  It
	    // will default to the root/cimv2 namespace.
	    CIMNameSpace cns = new CIMNameSpace(args[0]);

	    // create UserPrincipal and PasswordCredential objects with
	    // the username and password given.  If this user does not
	    // have write privileges to the root/cimv2 namespace, this
	    // example program will not work
	    UserPrincipal up = new UserPrincipal(args[1]);
	    PasswordCredential pc = new PasswordCredential(args[2]);

	    // check to see if user specified rmi protocol
	    if (args[3].equalsIgnoreCase("rmi")) {
	    	protocol = CIMClient.CIM_RMI;
	    }

	    // check if user specified query.  If not, prompt for query
	    if (args.length == 5) {
		queryString = args[4];
	    } else {
		System.out.println("enter WQL query string:");
		StringBuffer buf = new StringBuffer("");
		int c;
		try {
		    while ((c = System.in.read()) != -1) {
			char ch = (char) c;
			if (ch == '\n') {
			    break;
			}
			buf.append(ch);
		    }
		} catch (IOException e) {
		    e.printStackTrace();
		}
		queryString = buf.toString();
	    }
		

	    // create a CIMClient object 
	    cc = new CIMClient(cns, up, pc, protocol);

	    // execute query
	    System.out.println("query string: " + queryString); 
	    Enumeration e = cc.execQuery(new CIMObjectPath(), queryString,
				       CIMClient.WQL);
	    System.out.println("resulting instances are:"); 
	    // print out the resulting instances
	    while(e.hasMoreElements()) {
		CIMInstance ci = (CIMInstance)e.nextElement();
		System.out.println(ci);
	    }


	} catch (CIMException e) {
	    e.printStackTrace();
	}

	// close session.
	if (cc != null) {
	    try {
		cc.close();
	    } catch (CIMException e) {
		System.out.println("Error closing CIMClient"); 
	    }
	}
    }


    public static void main(String args[]) {
	new TestQuery(args);
    }
}


