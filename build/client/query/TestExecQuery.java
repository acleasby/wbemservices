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
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.Enumeration;

import javax.wbem.cim.*;
import javax.wbem.cim.CIMObjectPath;
import javax.wbem.client.*;
import javax.wbem.client.UserPrincipal;
import javax.wbem.client.PasswordCredential;

/**
 * This class will perform an CIMClient.execQuery using a WQL query string 
 * found in a file specified on the command line.  
 */
public class TestExecQuery {
    public static void main(String args[]) throws CIMException {
	CIMClient cc = null;
	CIMObjectPath cop = null;
	String protocol = CIMClient.CIM_XML;
	if (args.length < 4) {
	    System.out.println("Usage: TestExecQuery hostname user passwd FILE [rmi|http]");
	    System.out.println("    where FILE contains the WQL_query");
	    System.exit(1);
	    }
	try {
	    FileInputStream finput = null;
	    InputStreamReader sinput = null;
	    BufferedReader breader = null;
	    String queryString = null;
	    CIMNameSpace cns = new CIMNameSpace(args[0]);
	    UserPrincipal up = new UserPrincipal(args[1]);
	    PasswordCredential pc = new PasswordCredential(args[2]);
	    String FileName = args[3];
	    if (args.length == 4 && args[4].equalsIgnoreCase("rmi")) {
	    	protocol = CIMClient.CIM_RMI;
	    }

	try {
	    // Get the query from the file
	    finput = new FileInputStream(FileName);
	    sinput = new InputStreamReader(finput);
	    breader = new BufferedReader(sinput);
	    queryString = breader.readLine();
	    breader.close();
	    finput.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}

	    // Connect to CIMOM
	    cc = new CIMClient(cns, up, pc, protocol);

	    // Empty CIMObjectPath means use the current namespace
	    cop = new CIMObjectPath();

	    // execute query
	    System.out.println("execQuery(objectpath, queryString, WQL) returns");
	    System.out.println("    all instances of classes and subclasses");
	    System.out.println("    that satisfy the query = " + queryString); 
	    Enumeration e = cc.execQuery(cop, queryString, CIMClient.WQL);
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
}


