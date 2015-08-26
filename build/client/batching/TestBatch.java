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

import java.util.Enumeration;
import java.lang.String;

import javax.wbem.cim.*;
import javax.wbem.client.*;
import javax.wbem.client.UserPrincipal;
import javax.wbem.client.PasswordCredential;


public class TestBatch {
    public static void main(String args[]) throws CIMException {
	CIMClient cc = null;
	String protocol = CIMClient.CIM_XML;
	if (args.length < 4) {
	    System.out.println("Usage: TestBatch host user passwd classname " +
	    			"[rmi|http]"); 
	    System.exit(1);
	    }
	try {
	    CIMNameSpace cns = new CIMNameSpace(args[0]);

	    UserPrincipal up = new UserPrincipal(args[1]);
	    PasswordCredential pc = new PasswordCredential(args[2]);
	    if (args.length == 5 && args[4].equalsIgnoreCase("rmi")) {
	    	protocol = CIMClient.CIM_RMI;
	    }
	    cc = new CIMClient(cns, up, pc, protocol);

	    CIMObjectPath op = new CIMObjectPath(args[3]);

	    BatchCIMClient bc = new BatchCIMClient();
	    int[] ids = new int[10];

	    ids[0] = bc.enumerateInstanceNames(op);
	    ids[1] = bc.getClass(op, false, true, true, null);
	    ids[2] = bc.enumerateInstances(op, true, false, false, false, null);

	    BatchResult br = cc.performBatchOperations(bc);

	    Enumeration instanceNames = (Enumeration)br.getResult(ids[0]);
	    CIMClass cl = (CIMClass)br.getResult(ids[1]);
	    Enumeration instances = (Enumeration)br.getResult(ids[2]);

	    while (instanceNames.hasMoreElements()) {
	        System.out.println(instanceNames.nextElement());
	    }

	    System.out.println(cl.toMOF());

	    while (instances.hasMoreElements()) {
	        System.out.println(instances.nextElement());
	    }

	}
	catch (Exception e) {
	    e.printStackTrace();
	    System.out.println("Exception: "+e);
	}

	// close session.
	if (cc != null) {
	    cc.close();
	}
    }
}

