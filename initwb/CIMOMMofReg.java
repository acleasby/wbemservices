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
 *are Copyright ¨ 2003 Sun Microsystems, Inc.
 *
 *All Rights Reserved.
 *
 *Contributor(s): _______________________________________
*/

import javax.wbem.cim.*;
import javax.wbem.client.*;

/**
 * A tool to register mof file in a CIMOM
 */
public class CIMOMMofReg {
    
    /**
     * The main method
     * 
     * @param args the arguments
     */
    public static void main(String args[]) {

	CIMClient cc = null;
	try {
	    UserPrincipal up = new UserPrincipal();

	    cc = new CIMClient(new CIMNameSpace(args[0], "root/system"),
			up, new PasswordCredential());

		CIMArgument[] inArgs = new CIMArgument[0];
		CIMArgument[] outArgs = new CIMArgument[0];
	    cc.invokeMethod(new CIMObjectPath("WBEMServices_ObjectManager"), "registerMOF", 
	    inArgs, outArgs);

	} catch (Exception e) {
	    System.out.println(e);
	    e.printStackTrace();
	    System.exit(1);
	}

	try {
	    cc.close();
	} catch (Exception e) {
	    // Ignore the exception
	}

	System.exit(0);
    }
}
