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

/*import java.rmi.*;*/
import javax.wbem.client.*;
import javax.wbem.cim.CIMNameSpace;
import javax.wbem.cim.CIMObjectPath;
import javax.wbem.cim.CIMException;
import java.util.Enumeration;

/* 
 * This verification program does a deep enumeration
 * on a class; i.e. if the CIMOM is running properly,
 * the names of all subclasses will be displayed.
 * Command line arguments are:
 * args[0] - hostname CIMOM is running on
 * args[1] - class name to enumerate
 * args[2] - optional transfer protocol (default is http)
 */

public class EnumClasses {
    public static void main(String args[]) throws CIMException {
	CIMClient cc = null;
	CIMObjectPath cop = null;
	String protocol = CIMClient.CIM_XML;
	if (args.length < 2) {
	    System.out.println("Usage: EnumClasses hostname user password classname " +
			       "[rmi|http]");
	    System.exit(1);
    }
	try {
	    CIMNameSpace cns = new CIMNameSpace(args[0]);
	    UserPrincipal lup = new UserPrincipal(args[1]);
	    PasswordCredential lpc = new PasswordCredential(args[2]);
	    if (args.length == 5 && args[4].equalsIgnoreCase("rmi")) {
	    	protocol = CIMClient.CIM_RMI;
	    }
	    cc = new CIMClient(cns, lup, lpc, protocol);

	    // Get the class name from the command line
	    cop = new CIMObjectPath(args[3]);
	    // Do a deep enumeration of the class
	    Enumeration e = cc.enumerateClassNames(cop, true);
	    // Will print out all the subclasses of the class.
	    for (; e.hasMoreElements();)
	    System.out.println(e.nextElement());
	} // try
	catch (Exception e) {
	    System.out.println("Exception: "+e);
	} // catch

	// close session.
	if (cc != null) {
	    cc.close();
	} // if
    } // main
} // EnumClasses
