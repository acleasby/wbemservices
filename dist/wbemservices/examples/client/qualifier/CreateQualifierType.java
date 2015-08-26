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
 * Creates qualifiers of various types in the specified namespace.
 */
public class CreateQualifierType {
    public static void main(String args[]) throws CIMException {
    CIMClient cc = null;
    String protocol = CIMClient.CIM_RMI;
    if (args.length < 6) {
        System.out.println("Usage: CreateQualifierType host user passwd parentNS " +
                   "qualifier_name test_case [rmi|http]");
        System.exit(1);
    }
    try {
        String Host= args[0];
        String ParentNameSpace = args[3];
        CIMNameSpace cns = new CIMNameSpace(Host, ParentNameSpace);
        UserPrincipal up = new UserPrincipal(args[1]);
        PasswordCredential pc = new PasswordCredential(args[2]);
        if (args.length == 7 && args[6].equalsIgnoreCase("http")) {
            protocol = CIMClient.CIM_XML;
        }
        cc = new CIMClient(cns, up, pc, protocol);
        
        // Get the qualifier name from the command line
        String QualifierName = args[4];
        String TestCase = args[5];
        CIMQualifierType qt = new CIMQualifierType(QualifierName); 
        
    if (TestCase.equalsIgnoreCase("boolean")) {
        qt.setType(CIMDataType.getPredefinedType(CIMDataType.BOOLEAN));
        qt.setDefaultValue(new CIMValue(new Boolean(false)));
        qt.addScope(CIMScope.getScope(CIMScope.ANY));
        qt.addFlavor(CIMFlavor.getFlavor(CIMFlavor.DISABLEOVERRIDE));
        
    } else if (TestCase.equalsIgnoreCase("uint32")) {
        qt.setType(CIMDataType.getPredefinedType(CIMDataType.UINT32));
        qt.setDefaultValue(new CIMValue(new UnsignedInt32(32)));
        qt.addScope(CIMScope.getScope(CIMScope.PARAMETER));
        qt.addFlavor(CIMFlavor.getFlavor(CIMFlavor.ENABLEOVERRIDE));
        
    } else if (TestCase.equalsIgnoreCase("uint64")) {
        qt.setType(CIMDataType.getPredefinedType(CIMDataType.UINT64));
        CIMValue cv = new CIMValue(new UnsignedInt64(String.valueOf(64)));
//      qt.setDefaultValue(new CIMValue(new UnsignedInt64(String.valueOf(64))));
        qt.setDefaultValue(cv);
        qt.addScope(CIMScope.getScope(CIMScope.CLASS));
        qt.addFlavor(CIMFlavor.getFlavor(CIMFlavor.ENABLEOVERRIDE));

    } else {
        System.out.println("Unknown test case");
    } // if/then/else

        System.out.println("createQualifierType(objectpath, qualifier) creates ");
        System.out.println("   qualifier " + QualifierName);
        System.out.println("   in namespace " + ParentNameSpace + " on " + Host);
        CIMObjectPath qtop = new CIMObjectPath(QualifierName);
        cc.createQualifierType(qtop, qt);

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

