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
 *are Copyright © 2002 Sun Microsystems, Inc.
 *
 *All Rights Reserved.
 *
 *Contributor(s): _______________________________________
*/

package org.wbemservices.wbem.compiler.mofc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

/**
 * This class reads the mofcomp MOF2BEAN configuration file argument.
 * (i.e. -j <filename>). It provides convenience methods for retrieving
 * the PACKAGE, IMPORTS, and EXCEPTIONS argument values.
 *
 * This class is only used by CIM_Mofc.checkCommandLine().
 */
class BeanArgReader extends BufferedReader {

    // argument prefixes
    //
    private static final String	PACKAGE = "PACKAGE=";
    private static final String	IMPORTS = "IMPORTS=";
    private static final String	EXCEPTIONS = "EXCEPTIONS=";

    private static final char	EQUALS = '=';

    // argument value variables
    //
    private String		packageName = null;
    private String		imports = null;
    private String		exceptions = null;

    /**
     * Constructs a BeanArgReader given a handle to a FileReader.
     *
     * @param fileReader handle to the FileReader for the arg file
     */
    public BeanArgReader(FileReader fileReader) {

	super(fileReader);

	// set I18N Bundle
	I18N.setResourceName("org.wbemservices.wbem.compiler.mofc.Compiler");

	// parses the file and extracts the arg values
	//
	readFile();

    } // constructor

    /**
     * This method populates the class variables from the values
     * specified in the arguments in the file.
     */
    private void readFile() {

	String line;
	int index = -1;
	try {

	    while ((line = readLine()) != null) {

		index = line.indexOf(EQUALS) + 1;

		// a line without an '=' is bogus
		//
		if (index > 0) {

		    if (line.startsWith(PACKAGE)) {

			packageName = line.substring(index);

		    } else if (line.startsWith(IMPORTS)) {

			imports = line.substring(index);

		    } else if (line.startsWith(EXCEPTIONS)) {

			exceptions = line.substring(index);

		    } else {

			printError(line);

		    }

		} else {

		    printError(line);

		}

	    }

	} catch (IOException e) {
	}

    } // readFile

    /**
     * Prints an error message concerning improper contents of the file to
     * stderr.
     *
     * @param badLine the improper line in the file
     */
    private void printError(String badLine) {

	Vector vError = new Vector(1);
	vError.addElement(badLine);
	System.err.println(I18N.loadStringFormat(
	    "ERR_BEAN_ARG_BAD", vError));

    } // printError

    /**
     * This method returns the value of the PACKAGE argument as
     * a String. If no value was specified in the argument file, 
     * NULL is returned.
     *
     * @return	String	PACKAGE argument value
     */
    public String getPackage() {

	return packageName;

    } // getPackage

    /**
     * This method returns the value of the IMPORTS argument as
     * a String. If no value was specified in the argument file, 
     * NULL is returned.
     *
     * @return	String	IMPORTS argument value
     */
    public String getImports() {

	return imports;

    } // getImports

    /**
     * This method returns the value of the EXCEPTIONS argument as
     * a String. If no value was specified in the argument file, 
     * NULL is returned.
     *
     * @return	String	EXCEPTIONS argument value
     */
    public String getExceptions() {

	return exceptions;

    } // getExceptions

} // BeanArgReader
