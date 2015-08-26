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

import javax.wbem.cim.*;
import javax.wbem.client.CIMClient;
import javax.wbem.client.UserPrincipal;
import javax.wbem.client.PasswordCredential;

/**
 * Creates or updates qualifiers of various types in the specified namespace.
 */
public class SetQualifierType {
    public static void main(String args[]) throws CIMException {
	CIMClient cc = null;
	String protocol = CIMClient.CIM_XML;
	if (args.length < 6) {
	    System.out.println("Usage: SetQualifierType host user passwd parentNS " +
			       "qualifier_name test_case [rmi|http]");
	    System.exit(1);
	}
	try {
	    String Host= args[0];
	    String ParentNameSpace = args[3];
	    CIMNameSpace cns = new CIMNameSpace(Host, ParentNameSpace);
	    UserPrincipal up = new UserPrincipal(args[1]);
	    PasswordCredential pc = new PasswordCredential(args[2]);
	    if (args.length == 7 && args[6].equalsIgnoreCase("rmi")) {
	    	protocol = CIMClient.CIM_RMI;
	    }
	    cc = new CIMClient(cns, up, pc, protocol);
	    
	    // Get the qualifier name from the command line
	    String QualifierName = args[4];
	    String TestCase = args[5];
	    CIMQualifierType qt = new CIMQualifierType(QualifierName); 
	    
	if (TestCase.equalsIgnoreCase("sint32")) {
	    qt.setType(CIMDataType.getPredefinedType(CIMDataType.SINT32));
	    qt.setDefaultValue(new CIMValue(new Integer(-32)));
	    qt.addScope(CIMScope.getScope(CIMScope.METHOD));
	    qt.addFlavor(CIMFlavor.getFlavor(CIMFlavor.DISABLEOVERRIDE));
	    
	} else if (TestCase.equalsIgnoreCase("sint64")) {
	    qt.setType(CIMDataType.getPredefinedType(CIMDataType.SINT64));
	    qt.setDefaultValue(new CIMValue(new Long(-64)));
	    qt.addScope(CIMScope.getScope(CIMScope.PROPERTY));
	    qt.addFlavor(CIMFlavor.getFlavor(CIMFlavor.ENABLEOVERRIDE));
	    
	} else if (TestCase.equalsIgnoreCase("real32")) {
	    qt.setType(CIMDataType.getPredefinedType(CIMDataType.REAL32));
	    qt.setDefaultValue(new CIMValue(new Float(32.5)));
	    qt.addScope(CIMScope.getScope(CIMScope.CLASS));
	    qt.addScope(CIMScope.getScope(CIMScope.PROPERTY));
	    qt.addFlavor(CIMFlavor.getFlavor(CIMFlavor.ENABLEOVERRIDE));
	    qt.addFlavor(CIMFlavor.getFlavor(CIMFlavor.RESTRICTED));
	    
	} else if (TestCase.equalsIgnoreCase("real64")) {
	    qt.setType(CIMDataType.getPredefinedType(CIMDataType.REAL64));
	    qt.setDefaultValue(new CIMValue(new Double(64.5)));
	    qt.addScope(CIMScope.getScope(CIMScope.CLASS));
	    qt.addScope(CIMScope.getScope(CIMScope.METHOD));
	    qt.addFlavor(CIMFlavor.getFlavor(CIMFlavor.ENABLEOVERRIDE));
	    qt.addFlavor(CIMFlavor.getFlavor(CIMFlavor.TOSUBCLASS));
	    
	} else if (TestCase.equalsIgnoreCase("string")) {
	    qt.setType(CIMDataType.getPredefinedType(CIMDataType.STRING));
	    qt.setDefaultValue(new CIMValue("abcdefg"));
	    qt.addScope(CIMScope.getScope(CIMScope.CLASS));
	    qt.addScope(CIMScope.getScope(CIMScope.PARAMETER));
	    qt.addFlavor(CIMFlavor.getFlavor(CIMFlavor.TOSUBCLASS));
	    qt.addFlavor(CIMFlavor.getFlavor(CIMFlavor.TRANSLATE));
	    
	} else {
	    System.out.println("Unknown test case");
	} // if/then/else
	    System.out.println("setQualifierType(objectpath, qualifier) ");
	    System.out.println("   updates qualifier " + QualifierName);
	    System.out.println("   in namespace " + ParentNameSpace + " on " + Host);
	    System.out.println("   if already defined. Otherwise, it creates the qualifier");
	    CIMObjectPath qtop = new CIMObjectPath(QualifierName);
	    cc.setQualifierType(qtop, qt);

	} // try
	catch (Exception e) {
	    System.out.println("Exception: "+e);
	} // catch
	
	// Close the session.
	if (cc != null) {
	    cc.close();
	}	
    }
}

