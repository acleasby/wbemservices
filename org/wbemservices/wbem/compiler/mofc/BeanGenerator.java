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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.wbem.cim.CIMClass;
import javax.wbem.cim.CIMDataType;
import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMMethod;
import javax.wbem.cim.CIMObjectPath;
import javax.wbem.cim.CIMParameter;
import javax.wbem.cim.CIMProperty;
import javax.wbem.cim.CIMQualifier;
import javax.wbem.client.CIMOMHandle;
import javax.wbem.client.Debug;

/**
 * This class generates the Java source (Bean and Interface) that represents
 * the specified CIMClass.
 */
class BeanGenerator implements BeanGeneratorConstants {

    // relevant data needed to generate src
    //
    private static CIMOMHandle	cimomHandle = null;
    private CIMClass		cimClass = null;
    private static StringBuffer	fileHeader = null;
    private static StringBuffer	packageStatement = null;
    private static StringBuffer	importStatements = null;
    private static StringBuffer	exceptions = null;
    private static StringBuffer	invokeMethodExceptions = null;

    private static String[]	keywordArr = {"abstract", "boolean", "break", 
	"byte", "case", "catch", "char", "class", "const", "continue", 
	"default", "do", "double", "else", "extends", "final", "finally", 
	"float", "for", "goto", "if", "implements", "import", "instanceof", 
	"int", "interface", "long", "native", "new", "package", "private", 
	"protected", "public", "return", "short", "static", "strictfp", 
	"super", "switch", "synchronized", "this", "throw", "throws", 
	"transient", "try", "void", "volatile", "while"};
    private static String	lastKeyword = null;
    private final static String invalidChars =
	"!@#$%^&*(-){[]}+=?/\"'<,>.:;\\|        ";

    // config file argument value variables
    //
    private static String	packageName = null;
    private static Vector	vImports = null;
    private static Vector	vExceptions = null;
    private static File		outputDir = null;

    /**
     * Constructs a BeanGenerator to generate Interfaces and Beans from MOFs.
     *
     * @param	cimomHandle	handle to the CIMOM
     * @param	cimClass	the CIMClass for the MOF being compiled
     * @param	packageName	the Java package name
     * @param	imports		the colon-separated Java imports
     * @param	exceptions	the colon-separated Java exceptions
     * @param	outputDir	the output directory to store the generated src
     */
    public BeanGenerator(CIMOMHandle cimomHandle,
        CIMClass cimClass,
        String packageName,
        String imports,
        String exceptions,
        File outputDir) {

        super();
        BeanGenerator.cimomHandle = cimomHandle;
        this.cimClass = cimClass;
        BeanGenerator.packageName = packageName;
        BeanGenerator.outputDir = outputDir;
        Debug.trace2("Generating Java Bean source for " + cimClass.getName());

        BeanGenerator.vImports = parseColonList(imports);
        BeanGenerator.vExceptions = parseColonList(exceptions);

        // set I18N Bundle
        I18N.setResourceName("org.wbemservices.wbem.compiler.mofc.Compiler");

        BeanInterfaceWriter beanInterfaceWriter = new BeanInterfaceWriter(
            cimClass);
        BeanClassWriter beanClassWriter = new BeanClassWriter(
            cimClass);

    } // constructor

    /**
     * This method creates a ".java" suffixed file in the specified directory
     * that contains the specified contents. NOTE: If a file already exists in
     * the directory, it will be overwritten.
     *
     * @param	fileName	file name to create in the output directory
     * @param	fileContents	content to put in the file
     * @param	beanDir		directory in which to create the file
     */
    public static void writeToFile(StringBuffer fileName, 
	StringBuffer fileContents, File beanDir) {

	fileName.append(JAVA_FILE);
	Debug.trace3("Generating source file: " + fileName.toString());
	try {

	    File fileToWrite = new File(beanDir.getAbsolutePath(),
		fileName.toString());
	    if (fileToWrite.exists()) {

		fileToWrite.delete();

	    }
	    try {

//		FileWriter fileWriter = new FileWriter(fileToWrite, true);
		String pathName = beanDir.getAbsolutePath() + File.separator 
			+ fileName.toString();
		FileWriter fileWriter = new FileWriter(pathName, true);
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		bufferedWriter.write(fileContents.toString());
		bufferedWriter.newLine();
		bufferedWriter.close();

	    } catch (IOException ioe) {

		System.err.println(I18N.loadStringFormat("CANNOT_CREATE_FILE",
		    fileName.toString()));
		System.exit(1);

	    }

	} catch (NullPointerException npe) {

	    // this only gets throw if an empty string is passed to the
	    // constructor
	    npe.printStackTrace();

	}

    } // writeToFile

    /**
     * This method creates a ".java" suffixed file in the user specified 
     * directory that contains the specified contents. NOTE: If a file already 
     * exists in the directory, it will be overwritten.
     *
     * @param	fileName	file name to create in the output directory
     * @param	fileContents	content to put in the file
     */
    public static void writeToFile(StringBuffer fileName, 
	StringBuffer fileContents) {

	writeToFile(fileName, fileContents, outputDir);

    } // writeToFile

    /**
     * This method returns a string containing the name of the specified
     * CIM class' superclass in the generated Bean/Interface source.
     *
     * @param	cimClass	class to get the superclass name of
     * @return	superclass name of the given class
     */
    public static String getSuperClass(CIMClass cimClass) {

	String superClass = getClassName(cimClass.getSuperClass());
	// null or empty super class indicates the class is at the 
	// root of the inheritance tree. CIMBean or CIMBeanImpl is
	// therefore the superclass used in the generated source.
	//
	if (!hasContents(superClass)) {

	    superClass = "CIM";

	}
	return (superClass);

    } // getSuperClass

    /**
     * This method returns a string containing an appropriate class name,
     * based on the given class name, for the generated source files. Since
     * the MOFs are case-insensitive its possible that case differences
     * in the same class name will cause compilation problems in our generated
     * Java source. This method attempts to minimize the problem by enforcing 
     * some Java coding conventions as follows:
     *
     * - ensure that the first character is uppercase
     * - ensure that any character following an underscore is uppercase
     *
     * @param	name	class name to generate appropriate Java class name from
     * @return	appropriate Java class name
     */
    public static String getClassName(String name) {

	if (!hasContents(name)) {

	    return (name);

	}
	// ensure that the first character is uppercase
	//
	name = firstCharUpper(name).toString();

	// ensure that any character following an underscore is uppercase
	//
	StringBuffer sb = new StringBuffer();
	StringTokenizer tokenizer = new StringTokenizer(name, "_");
	String token;
	while (tokenizer.hasMoreTokens()) {

	    token = tokenizer.nextToken();
	    sb.append(firstCharUpper(token)).append("_");

	}

	// remove trailing underscore
	//
	if ((sb.toString()).lastIndexOf("_") == sb.length() - 1) {

	    sb = new StringBuffer(sb.substring(0, (sb.toString()).lastIndexOf("_")));

	}
	return (sb.toString());

    } // getClassName

    /**
     * This method returns the JavaDoc for the accessor method 
     * generated from the specified CIMProperty.
     *
     * @param	cimProp	the property to generate JavaDoc for
     * @return	the accessor method JavaDoc
     */
    public static StringBuffer getAccessorDoc(CIMProperty cimProp) {

	Debug.trace3("Generating accessor method JavaDoc for property: " +
	    cimProp.getOriginClass() + "." + cimProp.getName());
	String accessorDesc = I18N.loadStringFormat("BEAN_ACCESSOR_JAVADOC",
	    cimProp.getOriginClass(), cimProp.getName());
	StringBuffer mofDescBuffer = getPropertyDescription(cimProp);

	// create a JavaDocElement for the return value
	//
	Vector vJavaDocElements = new Vector(1 + vExceptions.size());
	JavaDocElement javaDocElement = new JavaDocElement(JAVADOC_RETURN_TAG,
	    getPropertyDataType(cimProp), I18N.loadStringFormat(
	    "ACCESSOR_RETURN_DESCRIPTION", cimProp.getName()));
	vJavaDocElements.addElement(javaDocElement);

	// create JavaDocElement(s) for the exceptions thrown by the method
	//
	addExceptionsToVector(vJavaDocElements, vExceptions);

	StringBuffer sb = getJavaDoc(accessorDesc, mofDescBuffer, 
	    vJavaDocElements, 1);
	return (sb);

    } // getAccessorDoc

    /**
     * This method returns the JavaDoc for the mutator method 
     * generated from the specified CIMProperty.
     *
     * @param	cimProp	the property to generate JavaDoc for
     * @return	the mutator method JavaDoc
     */
    public static StringBuffer getMutatorDoc(CIMProperty cimProp) {

	Debug.trace3("Generating mutator method JavaDoc for property: " +
	    cimProp.getOriginClass() + "." + cimProp.getName());
	String mutatorDesc = I18N.loadStringFormat("BEAN_MUTATOR_JAVADOC",
	    cimProp.getOriginClass(), cimProp.getName());
	StringBuffer mofDescBuffer = getPropertyDescription(cimProp);

	// create a JavaDocElement for the param value
	//
	Vector vJavaDocElements = new Vector(1 + vExceptions.size());
	JavaDocElement javaDocElement = new JavaDocElement(JAVADOC_PARAM_TAG,
	    getPropertyDataType(cimProp), I18N.loadStringFormat(
	    "MUTATOR_PARAM_DESCRIPTION", cimProp.getName()));
	vJavaDocElements.addElement(javaDocElement);

	// create JavaDocElement(s) for the exceptions thrown by the method
	//
	addExceptionsToVector(vJavaDocElements, vExceptions);

	StringBuffer sb = getJavaDoc(mutatorDesc, mofDescBuffer, 
	    vJavaDocElements, 1);
	return (sb);

    } // getMutatorDoc

    /**
     * This method returns the JavaDoc for the units accessor method 
     * generated from the specified CIMProperty.
     *
     * @param	cimProp	the property to generate JavaDoc for
     * @return	the units accessor method JavaDoc
     */
    public static StringBuffer getUnitsDoc(CIMProperty cimProp) {

	Debug.trace3("Generating units accessor method JavaDoc for property: "
	    + cimProp.getOriginClass() + "." + cimProp.getName());
	StringBuffer sb = new StringBuffer();
	if (hasQualifier(cimProp, UNITS)) {

	    String unitsDesc = I18N.loadStringFormat("BEAN_UNITS_JAVADOC",
		cimProp.getOriginClass(), cimProp.getName());
	    StringBuffer mofDesc = getPropertyDescription(cimProp);

	    // create a JavaDocElement for the return value
	    //
	    Vector vJavaDocElements = new Vector(1 + vExceptions.size());
	    JavaDocElement javaDocElement = new JavaDocElement(
		JAVADOC_RETURN_TAG, new StringBuffer(STRING),
		I18N.loadStringFormat("UNITS_RETURN_DESCRIPTION",
		cimProp.getName()));
	    vJavaDocElements.addElement(javaDocElement);

	    // create JavaDocElement(s) for the exceptions thrown by the method
	    //
	    addExceptionsToVector(vJavaDocElements, vExceptions);

	    sb.append(getJavaDoc(unitsDesc, mofDesc, vJavaDocElements, 1));

	}
	return sb;

    } // getUnitsDoc

    /**
     * This method returns the JavaDoc for the display name accessor method 
     * generated from the specified CIMProperty.
     *
     * @param	cimProp	the property to generate JavaDoc for
     * @return	the display name accessor method JavaDoc
     */
    public static StringBuffer getDisplayNameDoc(CIMProperty cimProp) {

	Debug.trace3(
	    "Generating display name accessor method JavaDoc for property: " +
	    cimProp.getOriginClass() + "." + cimProp.getName());
	StringBuffer sb = new StringBuffer();
	if (hasQualifier(cimProp, DISPLAYNAME)) {

	    String displayNameDesc = I18N.loadStringFormat(
		"BEAN_DISPLAYNAME_JAVADOC", cimProp.getOriginClass(), 
		cimProp.getName());
	    StringBuffer mofDesc = getPropertyDescription(cimProp);

	    // create a JavaDocElement for the return value
	    //
	    Vector vJavaDocElements = new Vector(1 + vExceptions.size());
	    JavaDocElement javaDocElement = new JavaDocElement(
		JAVADOC_RETURN_TAG, new StringBuffer(STRING),
		I18N.loadStringFormat("DISPLAYNAME_RETURN_DESCRIPTION",
		cimProp.getName()));
	    vJavaDocElements.addElement(javaDocElement);

	    // create JavaDocElement(s) for the exceptions thrown by the method
	    //
	    addExceptionsToVector(vJavaDocElements, vExceptions);

	    sb.append(getJavaDoc(displayNameDesc, mofDesc, vJavaDocElements, 
		1));

	}
	return (sb);

    } // getDisplayNameDoc

    /**
     * This method returns the JavaDoc for the invokeMethod 
     * generated from the specified CIMMethod.
     *
     * @param	cimMethod	the invokeMethod to generate JavaDoc for
     * @return	the invokeMethod JavaDoc
     */
    public static StringBuffer getInvokeMethodDoc(CIMMethod cimMethod) {

	Debug.trace3("Generating invokeMethod JavaDoc for property: " +
	    cimMethod.getOriginClass() + "." + cimMethod.getName());
	String methodDesc = I18N.loadStringFormat("BEAN_INVOKEMETHOD_JAVADOC",
	    cimMethod.getOriginClass(), cimMethod.getName());
	StringBuffer mofDesc = getMethodDescription(cimMethod);

	// create a JavaDocElement for the return value
	//
	Vector vJavaDocElements = new Vector(1 + vExceptions.size());
	JavaDocElement javaDocElement = new JavaDocElement(JAVADOC_RETURN_TAG,
	    getDataType(cimMethod.getType()),
	    I18N.loadStringFormat("INVOKEMETHOD_RETURN_DESCRIPTION",
	    cimMethod.getName()));
	vJavaDocElements.addElement(javaDocElement);

	// create JavaDocElement(s) for the exceptions thrown by the method
	//
	addExceptionsToVector(vJavaDocElements, vExceptions);

	// prepend the JavaDocElements for the parameters in the Vector
	//
	insertParamsInVector(vJavaDocElements, cimMethod.getParameters());

	StringBuffer sb = getJavaDoc(methodDesc, mofDesc, vJavaDocElements, 1);
	return (sb);

    } // getInvokeMethodDoc

    /**
     * This method returns the auto-generated code file header.
     *
     * @return	StringBuffer	auto-generated code JavaDoc file header
     */
    public static StringBuffer getFileHeader() {

	Debug.trace3("Generating file header");
	if (!hasContents(fileHeader)) {

	    String timeStamp = (new Date()).toString();
	    String versionStr = BeanGenerator.format(VERSION_FORMAT, 
		Integer.toString(Version.major), 
		Integer.toString(Version.minor),
		Integer.toString(Version.revision));

	    fileHeader = getJavaDoc(I18N.loadStringFormat("FILE_HEADER", 
		versionStr, timeStamp), new StringBuffer(), (Vector)null, 
		0).append(RETURN);

	}
	return (fileHeader);

    } // getFileHeader

    /**
     * This method returns the Java source for the specified Java package 
     * name.
     *
     * @param	packageName	the Java package name
     * @return	Java source for the package statement
     */
    public static StringBuffer getPackageStatement(String packageName) {

	Debug.trace3("Generating package statement");
	StringBuffer packageStatement = new StringBuffer(
	    format(PACKAGE_STATEMENT, packageName)).append(RETURN);

	return (packageStatement);

    } // getPackageStatement

    /**
     * This method returns the Java source for the user specified Java package.
     *
     * @return	StringBuffer	the Java package statement
     */
    public static StringBuffer getPackageStatement() {

	if (!hasContents(packageStatement)) {

	    packageStatement = getPackageStatement(packageName);

	}
	return (packageStatement);

    } // getPackageStatement

    /**
     * This method returns the Java source for the import strings in the 
     * specified container.
     * 
     * @param	v	container of Java import strings
     * @return	Java source for the import statements
     */
    public static StringBuffer getImportStatements(Vector v) {

	Debug.trace3("Generating import statement(s)");
	StringBuffer importStatements = new StringBuffer(DEFAULT_IMPORTS);

	// add the specified import statements
	//
	if (hasContents(v)) {

	    Enumeration eImports = v.elements();
	    String currentImport;
	    while (eImports.hasMoreElements()) {

		currentImport = (String)eImports.nextElement();
		importStatements.append(format(IMPORT_STATEMENT, 
		    currentImport));

	    }

	}
	importStatements.append(RETURN);
	return (importStatements);

    } // getImportStatements

    /**
     * This method returns the Java source for the user specified Java imports.
     *
     * @return	String	Java import statements
     */
    public static StringBuffer getImportStatements() {

	if (!hasContents(importStatements)) {

	    importStatements = getImportStatements(vImports);

	}
	return (importStatements);

    } // getImportStatements

    /**
     * This method returns the description from the MOF of the specified 
     * CIMProperty.
     *
     * @param	cimProp	property to get the description of
     * @return	description of the property in the MOF
     */
    public static StringBuffer getPropertyDescription(CIMProperty cimProp) {

	Debug.trace3("Generating description for property: " +
	    cimProp.getOriginClass() + "." + cimProp.getName());
	StringBuffer sb = new StringBuffer();
	if (hasQualifier(cimProp, DESCRIPTION)) {

	    sb.append(replaceNewlines((String)(cimProp.getQualifier(
		DESCRIPTION).getValue().getValue())));

	}
	return (sb);

    } // getPropertyDescription

    /**
     * This method returns true if there is already a CIMProperty in the
     * Vector specified with the same name as the given CIMProperty.
     * NOTE: This method is necessary as vProps.contains(cimProp) always
     * returns false. I can only assume the CIMElement.equals() implementation
     * uses a different criteria.
     *
     * @param	vProps		container of properties
     * @param	cimProp	property to check contents of container for
     * @return	true if the property is in the container
     */
    private static boolean hasProperty(Vector vProps, CIMProperty cimProp) {

	boolean bHasProperty = false;
	Enumeration eProps = vProps.elements();
	String propName = cimProp.getName();
	CIMProperty currentProp;
	while (eProps.hasMoreElements() && !bHasProperty) {

	    currentProp = (CIMProperty)eProps.nextElement();
	    if (propName.equalsIgnoreCase(currentProp.getName())) {

		bHasProperty = true;

	    }

	}
	return (bHasProperty);

    } // hasProperty

    /**
     * This method returns the datatype originally defined for the 
     * specified Override qualified property. This is needed so that 
     * the BeanImpl class knows to which type to cast an overriden 
     * property. In order to determine original datatype definition, 
     * the class inheritance must be climbed.
     *
     * @param	cimClass	CIM class the property is overriden in
     * @param	cimProp   	property that is qualified as Override
     * @return	datatype originally defined for the property
     */
    public static StringBuffer getOverriddenDataType(CIMClass cimClass,
	CIMProperty cimProp) {

	CIMClass superClass = null;
	CIMProperty superProp = null;
	String propName = cimProp.getName();
	if (hasQualifier(cimProp, OVERRIDE)) {

	    propName = getQualifier(cimProp, OVERRIDE);

	}
	String[] propArr = {propName};
	String superClassName = cimClass.getSuperClass();
	CIMObjectPath objectPath = new CIMObjectPath(superClassName);

	// climb the class inheritance tree until the *original*
	// definition of the property is found.
	//
	while (superProp == null || hasQualifier(superProp, OVERRIDE)) {

	    try {

		superClass = cimomHandle.getClass(objectPath, true, true, true,
		    propArr);
		superProp = superClass.getProperty(propArr[0]);

	    } catch (CIMException cex) {
	    }
	    superClassName = superClass.getSuperClass();
	    objectPath = new CIMObjectPath(superClassName);

	}
	return (getPropertyDataType(superProp));

    } // getOverriddenDataType

    /**
     * This method returns the datatype used in the Bean for the 
     * specified CIMProperty.
     *
     * @param	cimProp	property to get the datatype of
     * @return	the property datatype
     */
    public static StringBuffer getPropertyDataType(CIMProperty cimProp) {

	StringBuffer sb = getDataType(cimProp.getType());
	// remove the array suffix from the datatype of ValueMap/Values
	// qualifier properties
	//
	if ((hasQualifier(cimProp, VALUEMAP) || hasQualifier(cimProp, VALUES))
	    && isArrayType(sb)) {

	    sb = new StringBuffer(sb.substring(0, sb.length() - 2));

	}
	return (sb);

    } // getPropertyDataType

    /**
     * This method returns true if the specified string is an array
     * datatype as indicated by the presence of a '[]' suffix.
     *
     * @param	dataType	datatype string
     * @return	true if the the datatype is an array type
     */
    public static boolean isArrayType(StringBuffer dataType) {

	return ((dataType.toString()).indexOf(ARRAY_SUFFIX) > 0);

    } // isArrayType

    /**
     * This method returns true if the specified CIMParameter has the
     * specified qualifier.
     *
     * @param	cimParam	parameter to check for a qualifier
     * @param	qualName	qualifier name to check for
     * @return	true if the parameter has the qualifier
     */
    public static boolean hasQualifier(CIMParameter cimParam, String qualName) {

	CIMQualifier cimQual = cimParam.getQualifier(qualName);
	return (cimQual != null);

    } // hasQualifier

    /**
     * This method returns true if the specified CIMProperty has the
     * specified qualifier.
     *
     * @param	cimProp    property to check for a qualifier
     * @param	qualName   qualifier name to check for
     * @return	true if the property has the qualifier
     */
    public static boolean hasQualifier(CIMProperty cimProp, String qualName) {

	CIMQualifier cimQual = cimProp.getQualifier(qualName);
	return (cimQual != null);

    } // hasQualifier

    /**
     * This method returns true if the specified CIMMethod has the
     * specified qualifier.
     *
     * @param	cimMethod	method to check for a qualifier
     * @param	qualName	qualifier name to check for
     * @return	true if the method has the qualifier
     */
    public static boolean hasQualifier(CIMMethod cimMethod, String qualName) {

	CIMQualifier cimQual = cimMethod.getQualifier(qualName);
	return (cimQual != null);

    } // hasQualifier

    /**
     * This method returns true if the specified CIMClass has the
     * specified qualifier.
     *
     * @param	cimClass	class to check for a qualifier
     * @param	qualName	qualifier name to check for
     * @return	true if the class has the qualifier
     */
    public static boolean hasQualifier(CIMClass cimClass, String qualName) {

	CIMQualifier cimQual = cimClass.getQualifier(qualName);
	return (cimQual != null);

    } // hasQualifier

    /**
     * This method returns the string associated with the specified qualifier.
     * The hasQualifier(cimProp) method should be called first to check that
     * its worthwhile to get the qualifier value, but an empty string is
     * returned if there isn't such a qualifier. NOTE: Not all qualifiers
     * return String values, so care should be taken when this method is
     * used. (e.g. ValueMap and Values return Vector)
     *
     * @param	cimProp	  property to get the qualifier value from
     * @param	qualName	qualifier name to get the value of
     * @return	qualifier value
     */
    public static String getQualifier(CIMProperty cimProp, 
	String qualName) {

	String qualStr = "";
	if (hasQualifier(cimProp, qualName)) {

	    qualStr = (String)(
		cimProp.getQualifier(qualName).getValue().getValue());

	}
	return (qualStr);

    } // getQualifier

    /**
     * This method returns the string associated with the specified qualifier.
     * The hasQualifier(cimClass) method should be called first to check that
     * its worthwhile to get the qualifier value, but an empty string is
     * returned if there isn't such a qualifier. NOTE: Not all qualifiers
     * return String values, so care should be taken when this method is
     * used.
     *
     * @param	cimClass	CIM class to get the qualifier value from
     * @param	qualName	qualifier name to get the value of
     * @return	qualifier value
     */
    public static String getQualifier(CIMClass cimClass, String qualName) {

	String qualStr = "";
	if (hasQualifier(cimClass, qualName)) {

	    qualStr = (String)(
		cimClass.getQualifier(qualName).getValue().getValue());

	}
	return (qualStr);

    } // getQualifier

    /**
     * This method returns the description from the MOF of the specified 
     * CIMMethod.
     *
     * @param	cimMethod	method to get the description of
     * @return	description of the method in the MOF
     */
    public static StringBuffer getMethodDescription(CIMMethod cimMethod) {

	Debug.trace3("Generating description for method: " +
	    cimMethod.getOriginClass() + "." + cimMethod.getName());
	StringBuffer sb = new StringBuffer();
	if (hasQualifier(cimMethod, DESCRIPTION)) {

	    sb.append(replaceNewlines((String)(cimMethod.getQualifier(
		DESCRIPTION).getValue().getValue())));

	}
	return (sb);

    } // getMethodDescription

    /**
     * This method returns the description from the MOF of the specified 
     * CIMClass.
     *
     * @param	cimClass	class to get the description of
     * @return	description of the class in the MOF
     */
    public static StringBuffer getClassDescription(CIMClass cimClass) {

	Debug.trace3("Generating description for class: " + cimClass.getName());
	StringBuffer sb = new StringBuffer();
	if (hasQualifier(cimClass, DESCRIPTION)) {

	    sb.append(replaceNewlines((String)(cimClass.getQualifier(
		DESCRIPTION).getValue().getValue())));

	}
	return (sb);

    } // getClassDescription

    /**
     * This method replaces each newline character in the specified String
     * with space.
     *
     * @param	str	string to replace newline characters in
     * @return	comparable string with replaced newline characters
     */
    private static String replaceNewlines(String str) {

	StringBuffer buffer = new StringBuffer(str);
	int index = (buffer.toString()).indexOf(RETURN);
	while (index > -1) {

	    buffer.replace(index, index + 1, SPACE);
	    index = (buffer.toString()).indexOf(RETURN);

	}
	return (buffer.toString());

    } // replaceNewlines

    /**
     * This method returns the parameters taken by the Bean's version of 
     * the specified invokeMethod. The string comma separates the 
     * parameters so that it can be substituted in the generated Interface 
     * and Bean version of the method between the method's parentheses - 
     * e.g. "({0})". Note: The datatype of Values/ValueMap qualified 
     * parameters are mapped to String.
     *
     * @param	cimMethod	the vector to check for contents
     * @return	parameters for the specified invokeMethod
     */
    public static StringBuffer getInvokeMethodParams(CIMMethod cimMethod) {

	StringBuffer sb = new StringBuffer();
	Vector vParams = cimMethod.getParameters();
	if (!hasContents(vParams)) {

	    return (sb);

	}

	// add the [IN] qualified parameters to the parameter string
	//
	Enumeration eParams = vParams.elements();
	CIMParameter param;
	StringBuffer dataType;
	boolean hasValues;
	while (eParams.hasMoreElements()) {

	    param = (CIMParameter)eParams.nextElement();
	    if (isInParameter(param)) {

		// map Values/ValueMap qualified parameters are mapped to
		// String, *or* String[] if its an array type.
		//
		hasValues = hasQualifier(param, VALUES) || 
		    hasQualifier(param, VALUEMAP);
		dataType = getDataType(param.getType());
		if (hasValues) {

		    dataType = (isArrayType(dataType) ? 
			new StringBuffer(STRING_ARRAY) : 
			new StringBuffer(STRING));

		}
		sb.append(format(METHOD_PARAM, 
		    dataType.toString(),
		    getParameterName(param.getName())));

	    }

	}

	// tack on an <Method>Output[] as the last parameter, or remove 
	// the trailing comma. NOTE: The following are the String
	// capitalization rules followed by the Bean and Interface method
	// equivalent of an invokeMethod:
	//
	// 1. the first letter of the method name is lowercase regardless
	// of how it was spelled in the MOF. this in done in accordance with
	// the Java coding conventions. (e.g. FooMethod() in MOF => fooMethod()
	// in Bean)
	//
	// 2. the first letter of the parameter output container class
	// <Methd>Output is uppercase. (e.g. FooMethodOutput)
	//
	// 3. the first letter of the parameter itself is lowercase.
	//
	// The following is an example of an invokeMethod signature in a
	// generated Interface and Bean.
	//    public void fooMethod(FooMethodOutput[] fooMethodOutput)
	//
	// NOTE: The Output param is an array so we can add an OutputImpl
	// to the array, since the user can't access this class, only the
	// Output Interface. Otherwise, we can't find the implementation
	// details and change the reference passed into the method.
	// 
	if (hasOutParameter(cimMethod)) {

	    sb.append(format(OUTPUT_PARAM, getOutputInterfaceName(
		cimMethod).toString() + ARRAY_SUFFIX,
		getOutputParameterName(cimMethod).toString()));

	} else {

	    sb = new StringBuffer(sb.substring(0, (sb.toString()).lastIndexOf(",")));

	}
	return (sb);

    } // getInvokeMethodParams

    /**
     * This method returns a parameter name that doesn't clash with any
     * reserved Java keywords.
     *
     * @param	str	string to create a parameter name for
     * @return	parameter name that doesn't clash with Java keywords
     */
    public static String getParameterName(String str) {

	str = (firstCharLower(str).toString());
	if (isKeyword(str)) {

	    str = firstCharUpper(str).toString();

	}
	return (str);

    } // getParameterName

    /**
     * This method returns a container of the ValueMap/Values constant
     * names.
     *
     * @param	cimProp	property to get ValueMap/Values constants for
     * @param	prefix	constant name prefix
     * @return	container of Java constant names
     */
    public static Vector getConstantNames(CIMProperty cimProp, String prefix) {

	Vector vConstantNames = getValuesConstantValues(cimProp);
	return (getConstantNames(vConstantNames, prefix));

    } // getConstantNames

    /**
     * This method returns a container of the ValueMap/Values constant
     * names.
     *
     * @param	cimParam	property to get ValueMap/Values constants for
     * @param	prefix		constant name prefix
     * @return	container of Java constant names
     */
    public static Vector getConstantNames(CIMParameter cimParam, 
	String prefix) {

	Vector vConstantNames = getValuesConstantValues(cimParam);
	return (getConstantNames(vConstantNames, prefix));

    } // getConstantNames

    /**
     * This method returns a container of the ValueMap/Values constant
     * names. In order to produce valid Java constant names, the following 
     * is performed on each String in the container:
     *
     * - all characters are capitalized.
     * - the prefix is included in the constant name to indicate which 
     *   property it is intended to be used with, as well as to ensure 
     *   that a class with multiple ValueMap/Values qualified properties 
     *   with the same constant names don't clash.
     * - invalid characters for Java variable names are converted to '_'.
     * - the '+' character if present is converted to 'PLUS'. Simply 
     *   removing it like other reserved Java characters can result in
     *   name clashes.
     * - an int starting with 1 is appended to a constant name that
     *   clashes with an existing constant name in the container. This is
     *   required since we remove case sentivity in order to conform to the
     *   Java coding standard of capitalizing constant variable names.
     *   (e.g. "UltraSparcIII" and "UltraSparcIIi" would clash)
     *
     * @param	vConstantNames	getValuesConstantValues() return value
     * @param	prefix	constant name prefix
     * @return	container of Java constant names
     */
    public static Vector getConstantNames(Vector vConstantNames, 
	String prefix) {

	// if the container returned is empty, there aren't any constants
	// to be generated.
	//
	if (hasContents(vConstantNames)) {

	    Vector vNewConstantNames = new Vector(vConstantNames.size());
	    Enumeration eConstantNames = vConstantNames.elements();
	    String currentName;
	    String newName = "";
	    while (eConstantNames.hasMoreElements()) {

		// replace any occurrences of '+' with "plus"
		//
		currentName = replacePlus((String)eConstantNames.nextElement());

		// replace any invalid Java characters with '_'
		//
		StringTokenizer st = new StringTokenizer(currentName,
		    invalidChars);
		while (st.hasMoreTokens()) {

		    newName += st.nextToken() + "_";

		}
		// chop off the trailing underscore
		//
		int lastUnderscore = newName.lastIndexOf("_");
		if (lastUnderscore > 0) {

		    newName = newName.substring(0, lastUnderscore);

		}
		newName = newName.toUpperCase();

		// verify that we didn't introduce a name clash by removing
		// case-sensitivity before adding the constant name to the
		// return container
		//
		int suffix = 1;
		String tmpNewName = newName;
		while (vNewConstantNames.contains(newName)) {

		    newName = tmpNewName + Integer.toString(suffix);
		    suffix++;

		}
		// add the unique constant name to the return container
		//
		vNewConstantNames.addElement(newName);
		newName = "";

	    }
	    vConstantNames = prependPrefix(vNewConstantNames, prefix);

	}
	return vConstantNames;

    } // getConstantNames

    /**
     * This method returns a container of the strings used to generate 
     * the Values constant values for the given property.
     * NOTE: The way in which the constant values are generated depends
     * on what combination of the ValueMap/Values qualifiers the 
     * property has.
     *
     * @param	cimProp	property to get constant values for
     * @return	container of string constant values
     */
    public static Vector getValuesConstantValues(CIMProperty cimProp) {

	Vector vConstantValues = new Vector();
	boolean bValueMap = hasQualifier(cimProp, VALUEMAP);
	boolean bValues = hasQualifier(cimProp, VALUES);

	// use the Values qualifier if present, ValueMap qualifier otherwise
	//
	if (bValues) {

	    vConstantValues = 
		(Vector)cimProp.getQualifier(VALUES).getValue().getValue();

	} else if (bValueMap) {

	    // the property better have the ValueMap qualifier, otherwise
	    // this method should not have been called
	    //
	    vConstantValues = 
		(Vector)cimProp.getQualifier(VALUEMAP).getValue().getValue();

	}
	return (vConstantValues);

    } // getValuesConstantValues

    /**
     * This method returns a container of the strings used to generate 
     * the Values constant values for the given invokeMethod parameter.
     * NOTE: The way in which the constant values are generated depends
     * on what combination of the ValueMap/Values qualifiers the 
     * invokeMethod parameter has.
     *
     * @param	cimParam	parameter to get constant values for
     * @return	container of string constant values
     */
    public static Vector getValuesConstantValues(CIMParameter cimParam) {

	Vector vConstantValues = new Vector();
	boolean bValueMap = hasQualifier(cimParam, VALUEMAP);
	boolean bValues = hasQualifier(cimParam, VALUES);

	// use the Values qualifier if present, ValueMap qualifier otherwise
	//
	if (bValues) {

	    vConstantValues = 
		(Vector)cimParam.getQualifier(VALUES).getValue().getValue();

	} else if (bValueMap) {

	    // the property better have the ValueMap qualifier, otherwise
	    // this method should not have been called
	    //
	    vConstantValues = 
		(Vector)cimParam.getQualifier(VALUEMAP).getValue().getValue();

	}
	return (vConstantValues);

    } // getValuesConstantValues

    /**
     * This method returns a container of the strings used to generate 
     * the ValueMap constant values for the given property.
     * NOTE: If the specified property does not have the ValueMap qualifier,
     * it must have the Values qualifier and a default ValueMap will be
     * generated and returned.
     *
     * @param	cimProp	property to get constant values for
     * @return	container of string constant values
     */
    public static Vector getValueMapConstantValues(CIMProperty cimProp) {

	Vector vConstantValues = new Vector();
	if (BeanGenerator.hasQualifier(cimProp, VALUEMAP)) {

	    vConstantValues = 
		(Vector)cimProp.getQualifier(VALUEMAP).getValue().getValue();

	} else {

	    Vector vValues = 
		(Vector)cimProp.getQualifier(VALUES).getValue().getValue();
	    vConstantValues = generateValueMap(vValues.size());

	}
	return (vConstantValues);

    } // getValueMapConstantValues

    /**
     * This method returns a container of the strings used to generate 
     * the ValueMap constant values for the given invokeMethod parameter.
     * NOTE: If the specified parameter does not have the ValueMap qualifier,
     * it must have the Values qualifier and a default ValueMap will be
     * generated and returned.
     *
     * @param	cimParam	parameter to get constant values for
     * @return	container of string constant values
     */
    public static Vector getValueMapConstantValues(CIMParameter cimParam) {

	Vector vConstantValues = new Vector();
	if (BeanGenerator.hasQualifier(cimParam, VALUEMAP)) {

	    vConstantValues = 
		(Vector)cimParam.getQualifier(VALUEMAP).getValue().getValue();

	} else {

	    Vector vValues = 
		(Vector)cimParam.getQualifier(VALUES).getValue().getValue();
	    vConstantValues = generateValueMap(vValues.size());

	}
	return (vConstantValues);

    } // getValueMapConstantValues

    /**
     * This method returns a container of Strings that serves as the
     * equivalent of a default ValueMap qualifier.
     * 
     * @param	size	size of the ValueMap qualifier to generate
     * @return	container of 0 indexed Strings
     */
    private static Vector generateValueMap(int size) {

	Vector vValueMap = new Vector(size);
	for (int i = 0; i < size; i++) {

	    vValueMap.addElement(Integer.toString(i));

	}
	return (vValueMap);

    } // generateValueMap

    /**
     * This replaces any occurrences of '+' in the given string with
     * "plus". This is needed in order to generate ValueMap constants
     * that avoid name collisions.
     *
     * @param   str  string to remove occurrences of '+' from
     * @return  string with "plus" in place of occurrences of '+'
     */
    private static String replacePlus(String str) {

	if (str.indexOf('+') < 0) {

	    return (str);

	}
        StringBuffer sb = new StringBuffer();
	StringTokenizer tokenizer = new StringTokenizer(str, "+");
	while (tokenizer.hasMoreTokens()) {

	    sb.append(tokenizer.nextToken()).append(PLUS);

	}
	return (sb.toString());

    } // replacePlus

    /**
     * This method prepends the specified prefix to the specified container
     * of strings and returns a container of the resultng strings.
     *
     * @param	vContainer	container of strings to prepend prefix strings to
     * @param	prefix	prefix string to prepend to strings in container
     * @return	container of strings with prepended prefix string
     */
    private static Vector prependPrefix(Vector vContainer, String prefix) {

	Vector vNewContainer = new Vector(vContainer.size());
	Enumeration eContainer = vContainer.elements();
	String currentElement;
	while (eContainer.hasMoreElements()) {

	    currentElement = prefix + (String)eContainer.nextElement();
	    vNewContainer.addElement(currentElement);

	}
	return (vNewContainer);

    } // prependPrefix

    /**
     * This method returns true if the specified CIMParameter is qualified 
     * as [IN].
     *
     * @param	cimParam	parameter to check the [IN] qualifier of
     * @return	true if the parameter is qualified as [IN]
     */
    public static boolean isInParameter(CIMParameter cimParam) {

	return (hasQualifier(cimParam, IN));

    } // isInParameter

    /**
     * This method returns true if the specified CIMParameter is qualified 
     * as [OUT].
     *
     * @param	cimParam	parameter to check the [OUT] qualifier of
     * @return	true if the parameter is qualified as [OUT]
     */
    public static boolean isOutParameter(CIMParameter cimParam) {

	return (hasQualifier(cimParam, OUT));

    } // isOutParameter

    /**
     * This method returns true if the specified invokeMethod has at least 
     * one [IN] qualified parameter.
     *
     * @param	method	the invokeMethod to check for [IN] parameters
     * @return	true if the invokeMethod has > 0 [IN] params
     */
    public static boolean hasInParameter(CIMMethod method) {

	return (getInParameters(method).length > 0);

    } // hasInParameter

    /**
     * This method returns true if the specified invokeMethod has at least 
     * one [OUT] qualified parameter.
     *
     * @param	method	the invokeMethod to check for [OUT] parameters
     * @return	true if the invokeMethod has > 0 [OUT] params
     */
    public static boolean hasOutParameter(CIMMethod method) {

	return (getOutParameters(method).length > 0);

    } // hasOutParameter

    /**
     * This method returns a Vector of the [IN] qualified parameters of 
     * the specified invokeMethod.
     *
     * @param	method	the invokeMethod to return the [IN] params of
     * @return			contains the [IN] qualified CIMParameters
     */
    public static CIMParameter[] getInParameters(CIMMethod method) {

	Vector vInParams = new Vector();
	Vector vParams = method.getParameters();

	if (hasContents(vParams)) {
            Enumeration eParams = vParams.elements();
            while (eParams.hasMoreElements()) {
                CIMParameter param = (CIMParameter)eParams.nextElement();
                if (isInParameter(param)) {
                    vInParams.addElement(param);
                }
            }
	}
	return (CIMParameter[])vInParams.toArray(new CIMParameter[0]);
    } // getInParameters

    /**
     * This method returns a Vector of the [OUT] qualified parameters of 
     * the specified invokeMethod.
     *
     * @param	method	the invokeMethod to return the [OUT] params of
     * @return			contains the [OUT] qualified CIMParameters
     */
    public static CIMParameter[] getOutParameters(CIMMethod method) {

	Vector vOutParams = new Vector();
        Vector vParams = method.getParameters();
    
        if (hasContents(vParams)) {
                Enumeration eParams = vParams.elements();
                while (eParams.hasMoreElements()) {
                    CIMParameter param = (CIMParameter)eParams.nextElement();
                    if (isOutParameter(param)) {
                        vOutParams.addElement(param);
                    }
                }
        }
        return (CIMParameter[])vOutParams.toArray(new CIMParameter[0]);
    } // getOutParameters

    /**
     * This method returns true if the specified string is a reserved
     * Java keyword, false otherwise.
     *
     * @param	str	string to compare to reserved Java keywords
     * @return	true if the string is a keyword, false otherwise
     */
    private static boolean isKeyword(String str) {

	// quick check of keyword last matched. the class generator won't
	// be far behind the interface generator.
	//
	if (hasContents(lastKeyword) && lastKeyword.equals(str)) {

	    return (true);

	}

	// compare the string to the array of Java keywords. hold onto the
	// keyword match for the above quick check next time.
	//
	boolean bKeyword = false;
	for (int i = 0; i < keywordArr.length; i++) {

	    if (str.equals(keywordArr[i])) {

		lastKeyword = keywordArr[i];
		bKeyword = true;
		break;

	    }

	}
	return (bKeyword);

    } // isKeyword

    /**
     * This method returns the name of the Class generated for accessing
     * output from the [OUT] qualified parameter(s) of the specified
     * invokeMethod. NOTE: Regardless of the spelling of the invokeMethod
     * in the MOF, the <CIMClass>_<MethodName>OutputImpl Class name 
     * capitalizes the first letter of the method name.
     * 
     * @param	cimMethod	invokeMethod to get Output Class name of
     * @return	Output Class name
     */
    public static StringBuffer getOutputClassName(CIMMethod cimMethod) {

	StringBuffer sb = new StringBuffer();
	sb.append(format(METHODOUTPUT_CLASS, cimMethod.getOriginClass(),
	    firstCharUpper(cimMethod.getName()).toString()));
	return (sb);

    } // getOutputClassName

    /**
     * This method returns the name of the Interface generated for accessing
     * output from the [OUT] qualified parameter(s) of the specified 
     * invokeMethod. NOTE: Regardless of the spelling of the invokeMethod in
     * the MOF, the <CIMClass>_<MethodName>Output Interface name capitalizes 
     * the first letter of the method name.
     *
     * @param	cimMethod	invokeMethod to get Output Interface name of
     * @return	Output Interface name
     */
    public static StringBuffer getOutputInterfaceName(CIMMethod cimMethod) {

	StringBuffer sb = new StringBuffer();
	sb.append(format(METHODOUTPUT_INTERFACE, cimMethod.getOriginClass(),
	    firstCharUpper(cimMethod.getName()).toString()));
	return (sb);

    } // getOutputInterfaceName

    /**
     * This method returns the parameter name of the 
     * <CIMClass>_<MethodName>Output Interface in the Bean and Interface 
     * equivalent of the specified invokeMethod. NOTE: The parameter name 
     * is equivalent to the Interface name with the exception that the 
     * first character in the string is lowercase.
     *
     * @param	cimMethod	invokeMethod to get the output parameter for
     * @return	invokeMethod's output parameter name 
     */
    public static StringBuffer getOutputParameterName(CIMMethod cimMethod) {

	StringBuffer outputParamName = 
	    firstCharLower(getOutputInterfaceName(cimMethod).toString());
	StringBuffer sb = new StringBuffer(outputParamName.toString());
	return (sb);

    } // getOutputParameterName

    /**
     * This method returns the Bean equivalent name of the specified 
     * CIMProperty. The Bean name begins with a lowercase first character.
     *
     * @param	cimProp	property to get the name of
     * @return	correct case-sensitive name of the property
     */
    public static StringBuffer getPropertyName(CIMProperty cimProp) {

	return (firstCharLower(cimProp.getName()));

    } // getPropertyName

    /**
     * This method returns the Bean equivalent name of the specified 
     * CIMParameter. The Bean name begins with a lowercase first character.
     *
     * @param	cimParam	parameter to get the name of
     * @return	correct case-sensitive name of the parameter
     */
    public static StringBuffer getParameterName(CIMParameter cimParam) {

	return (new StringBuffer(getParameterName(cimParam.getName())));

    } // getParameterName

    /**
     * This method returns a StringBuffer containing the specified string 
     * with the first character uppercase.
     *
     * @param	str		string to convert first character to uppercase
     * @return	string with first character uppercase
     */
    public static StringBuffer firstCharUpper(String str) {

	String firstChar = str.substring(0, 1);
	StringBuffer sb = new StringBuffer(firstChar.toUpperCase());
	return (sb.append(str.substring(1)));

    } // firstCharUpper

    /**
     * This method returns a StringBuffer containing the specified string 
     * with the first character lowercase.
     *
     * @param	str		string to convert first character to lowercase
     * @return	string with first character lowercase
     */
    public static StringBuffer firstCharLower(String str) {

	String firstChar = str.substring(0, 1);
	StringBuffer sb = new StringBuffer(firstChar.toLowerCase());
	return (sb.append(str.substring(1)));

    } // firstCharLower

    /**
     * This method returns true if the specified Vector has contents.
     *
     * @param   v  the vector to check for contents
     * @return  true if the Vector is non-null and has > 0 item(s)
     */
    public static boolean hasContents(Vector v) {

    return ((v != null) && (v.size() > 0));

    } // hasContents

    /**
     * This method returns true if the specified Vector has contents.
     *
     * @param   oa  the vector to check for contents
     * @return  true if the Vector is non-null and has > 0 item(s)
     */
    public static boolean hasContents(Object[] oa) {

    return ((oa != null) && (oa.length > 0));

    } // hasContents

    /**
     * This method returns true if the specified String has contents.
     *
     * @param	s	the string to check for contents
     * @return	true if the string is non-null and is non-empty
     */
    public static boolean hasContents(String s) {

	return ((s != null) && (!s.equals("")));

    } // hasContents

    /**
     * This method returns true if the specified StringBuffer has contents.
     *
     * @param	sb	the string to check for contents
     * @return	true if the string is non-null and is non-empty
     */
    public static boolean hasContents(StringBuffer sb) {

	return ((sb != null) && (sb.length() > 0));

    } // hasContents

    /**
     * This method returns a StringBuffer broken as needed with the specified
     * break StringBuffer. This provides compliance with the jstyle 
     * convention of wrapping lines in Java source files at 80 characters.
     * This method takes care not to break lines of source code between quoted
     * strings as this would result in compilation errors.
     *
     * @param	str	string to wrap as needed
     * @param	breakStr	string to use in line breaks
     * @param	indentLength	number of chars the line has been indented
     * @return	string with breaks appropriately inserted
     */
    private static StringBuffer getWrappedString(StringBuffer str, 
	StringBuffer breakStr, int indentLength) {

	// the initial amount of available space on the line is 80 characters
	// less the length of the initial indentation of the line, which is
	// not the same as the length of the break string. once wrapping 
	// begins, this needs to be updated to reflect the longer length of
	// the break string.
	//
	int lineLength = 80 - indentLength - 1;
	int wrappedLength = 80 - getTokenLength(breakStr.toString()) - 1;
	if (getTokenLength(str.toString()) < lineLength) {

	    return (str);

	}

	// it is not good enough to put as close to 80 characters on a line
	// as possible. source lines that contain quotes have the potential of
	// causing compilation errors if the line is wrapped before an end
	// quote has been reached. quotes encountered in comment lines can
	// safely be ignored.
	//
	StringTokenizer tokenizer = new StringTokenizer(str.toString(), SPACE);
	StringBuffer returnBuffer = new StringBuffer();
	String currentToken;
	int tokenLength = 0;
	int bufferLength = 0;
	boolean hasWrapped = false;
	StringBuffer currentBuffer = new StringBuffer();
	while (tokenizer.hasMoreTokens()) {

	    currentToken = tokenizer.nextToken();
	    while (hasOneQuote(currentToken) && tokenizer.hasMoreTokens()) {

		// append tokens until the close quote is found
		//
		currentToken += SPACE + tokenizer.nextToken();

	    }
	    tokenLength = getTokenLength(currentToken);

	    // add the token if there's still room on the current line.
	    // NOTE: The concession is made here that if the 1st token is longer
	    // than the available space on the line, you might as well just
	    // add it. the real fix for this problem will be to break lines
	    // by parenthesis as a fall back. most of the times this is 
	    // encountered is due to casting long class variable names to long
	    // class names.
	    //
	    if ((!hasWrapped && tokenLength > lineLength) ||
		(!hasWrapped && tokenLength + bufferLength < lineLength) ||
		(hasWrapped && tokenLength + bufferLength < wrappedLength)) {

		currentBuffer.append(currentToken);
		currentBuffer.append(SPACE);

	    } else {

		// no more room on the current line, so tack on the
		// break and start a new line. NOTE: the amount of space
		// available on the line changes once you've wrapped for
		// the first time.
		//
		hasWrapped = true;
		currentBuffer.append(breakStr);
		returnBuffer.append(currentBuffer);
		currentBuffer = new StringBuffer(currentToken);
		currentBuffer.append(SPACE);

	    }
	    bufferLength = getTokenLength(currentBuffer.toString());

	}

	// tack on anything that may be left in the current line, but don't
	// mess up the indentation of the next line in the file by leaving
	// a trailing space.
	//
	if (currentBuffer.toString().endsWith(RETURN + SPACE)) {

	    currentBuffer = new StringBuffer(currentBuffer.substring(0, 
		currentBuffer.length() - 1));

	}
	returnBuffer.append(currentBuffer);
	return (returnBuffer);

    } // getWrappedString

    /**
     * This method returns true if the specified String contains exactly one
     * double quote substring.
     *
     * @param	str	string to check for quote substring
     * @return	true if there is exactly one quote substring
     */
    private static boolean hasOneQuote(String str) {

	int first = str.indexOf(QUOTE);
	int last = str.lastIndexOf(QUOTE);

	return ((first > -1) && (first == last));

    } // hasOneQuote

    /**
     * This method returns the number of spaces the given String will
     * require when written to a file. This accounts for tab expansion.
     *
     * @param	str	token to get the length of
     * @return	length accounting for tab expansion of the string
     */
    private static int getTokenLength(String str) {

	int index = 0;
	if (str == null || str.length() == 0) {

	    return (index);

	}
	int numTabs = 0;
	index = str.indexOf(TAB, index);
	while (index > -1) {

	    index = str.indexOf(TAB, index + 1);
	    numTabs++;

	}
	return ((8 * numTabs) - numTabs + str.length());

    } // getTokenLength

    /**
     * This method returns the appropriate JStyle indentation string
     * for the specified number of levels of indentation.
     *
     * @param	indent		number of levels of indentation
     * @return	JStyle indentation string
     */
    public static StringBuffer getIndentString(int indent) {

	StringBuffer sb = new StringBuffer();

	// add the correct number of tabs
	//
	int numTabs = indent / 2;
	for (int i = 0; i < numTabs; i++) {

	    sb.append(TAB);

	}

	// add the correct number of spaces as needed
	//
	if (indent % 2 != 0) {

	    sb.append(SPACES);

	}
	return (sb);

    } // getIndentString

    /**
     * This method returns the specified string with the specified
     * Java source code indentation prepended. Each level of indentation
     * equates to 4 spaces.
     *
     * @param	source		line of source code
     * @param	indent		levels of indentation to prepend the source
     * @return	properly indented line of source code
     */
    public static StringBuffer getSourceLine(String source, int indent) {

	StringBuffer sb = getIndentString(indent);
	StringBuffer breakBuffer = getBreakString(source, indent);
	return (sb.append(getWrappedString(new StringBuffer(source), 
	    breakBuffer, getTokenLength(sb.toString()))));

    } // getSourceLine

    /**
     * This method returns the appropriate break string for the specified
     * source string with the given level of indentation. The benefit that
     * this method provides is that it check for single line comments in
     * the source string and appends a single line comment to the indentation
     * string so that compilation errors are not encountered if the line
     * needs to be wrapped. (e.g. due to the length of a commented class name)
     *
     * @param	source		source line to get the break string for
     * @param	indent		level of indentation needed for wrapping
     * @return	break string
     */
    private static StringBuffer getBreakString(String source, int indent) {

	StringBuffer breakBuffer = new StringBuffer(RETURN);
	breakBuffer.append(getIndentString(indent + 1));
	if (source.indexOf(LINE_COMMENT) > -1) {

	    breakBuffer.append(LINE_COMMENT).append(SPACE);

	}
	return (breakBuffer);

    } // getBreakString

    /**
     * This method returns a string containing a properly formatted JavaDoc 
     * comment.
     *
     * @param	beanDesc	the Bean description of the method
     * @param	mofDesc	the MOF description of the method
     * @param	vJavaDocElements		JavaDocElements to include, null if none
     * @param	indent		levels of indentaion for the JavaDoc comment
     * @return	the properly formatted JavaDoc
     */
    public static StringBuffer getJavaDoc(String beanDesc, StringBuffer mofDesc,
	Vector vJavaDocElements, int indent) {

	// open the JavaDoc comment
	//
	StringBuffer indentBuffer = getIndentString(indent);
	StringBuffer sb = new StringBuffer(indentBuffer.toString());
	sb.append(JAVADOC_OPEN);
	indentBuffer.append(SPACE);
	sb.append(indentBuffer).append(JAVADOC_BODY);
	int indentLength = getTokenLength(indentBuffer.toString()) + 
	    JAVADOC_BODY.length();

	// add the JavaDoc comment description
	//
	StringBuffer breakBuffer = new StringBuffer(RETURN);
	breakBuffer.append(indentBuffer).append(JAVADOC_BODY);
	sb.append(getWrappedString(new StringBuffer(beanDesc), breakBuffer,
	    indentLength));

	// add the MOF description as applicable
	//
	if (hasContents(mofDesc)) {

	    // add the MOF's description of the method
	    //
	    sb.append(breakBuffer).append(breakBuffer);
	    sb.append(getWrappedString(mofDesc, breakBuffer, 
		indentLength));

	}

	// add the parameter, return value, and exception comment
	//
	if (hasContents(vJavaDocElements)) {

	    // add a blank line
	    //
	    sb.append(breakBuffer);

	    Enumeration eJavaDocElements = vJavaDocElements.elements();
	    JavaDocElement currentElement;
	    while (eJavaDocElements.hasMoreElements()) {

		currentElement = (JavaDocElement)eJavaDocElements.nextElement();
		sb.append(breakBuffer);
		sb.append(getWrappedString(new StringBuffer(format(
		    JAVADOC_ELEMENT, currentElement.getTag().toString(), 
		    currentElement.getType().toString(),
		    currentElement.getDescription())), breakBuffer,
		    indentLength));

	    }

	}

	// close the JavaDoc comment
	//
	sb.append(RETURN).append(indentBuffer).append(JAVADOC_CLOSE);
	return (sb);

    } // getJavaDoc

    /**
     * This method populates a Vector with the contents of a
     * colon separated string of parameters.
     *
     * @param	str	colon separated list of parameters
     * @return	vector of parameters in the string
     */
    public static Vector parseColonList(String str) {

	StringTokenizer tokenizer = new StringTokenizer(str, ":");
	Vector vReturn = new Vector(tokenizer.countTokens());
	while (tokenizer.hasMoreTokens()) {

	    vReturn.addElement(tokenizer.nextToken());

	}
	return vReturn;

    } // parseColonList

    /**
     * This method returns a string representing the Java datatype
     * comparable to the specified CIM datatype.
     *
     * @param	cimDataType	datatype defined in the MOF
     * @return	the corresponding Java datatype
     */
    public static StringBuffer getDataType(CIMDataType cimDataType) {

	switch (cimDataType.getType()) {

	    case CIMDataType.BOOLEAN: return new StringBuffer(BOOLEAN);
	    case CIMDataType.BOOLEAN_ARRAY: return new StringBuffer(
		BOOLEAN_ARRAY);

	    case CIMDataType.CHAR16: return new StringBuffer(CHAR);
	    case CIMDataType.CHAR16_ARRAY: return new StringBuffer(CHAR_ARRAY);

	    case CIMDataType.DATETIME: return new StringBuffer(DATETIME);

	    case CIMDataType.REAL32: return new StringBuffer(REAL32);
	    case CIMDataType.REAL32_ARRAY: return new StringBuffer(
		REAL32_ARRAY);

	    case CIMDataType.REAL64: return new StringBuffer(REAL64);
	    case CIMDataType.REAL64_ARRAY: return new StringBuffer(
		REAL64_ARRAY);

	    case CIMDataType.SINT8: return new StringBuffer(SINT8);
	    case CIMDataType.SINT8_ARRAY: return new StringBuffer(SINT8_ARRAY);

	    case CIMDataType.SINT16: return new StringBuffer(SINT16);
	    case CIMDataType.SINT16_ARRAY: return new StringBuffer(
		SINT16_ARRAY);

	    case CIMDataType.SINT32: return new StringBuffer(SINT32);
	    case CIMDataType.SINT32_ARRAY: return new StringBuffer(
		SINT32_ARRAY);

	    case CIMDataType.SINT64: return new StringBuffer(SINT64);
	    case CIMDataType.SINT64_ARRAY: return new StringBuffer(
		SINT64_ARRAY);

	    case CIMDataType.STRING: return new StringBuffer(STRING);
	    case CIMDataType.STRING_ARRAY: return new StringBuffer(
		STRING_ARRAY);

	    case CIMDataType.UINT8: return new StringBuffer(UINT8);
	    case CIMDataType.UINT8_ARRAY: return new StringBuffer(UINT8_ARRAY);

	    case CIMDataType.UINT16: return new StringBuffer(UINT16);
	    case CIMDataType.UINT16_ARRAY: return new StringBuffer(
		UINT16_ARRAY);

	    case CIMDataType.UINT32: return new StringBuffer(UINT32);
	    case CIMDataType.UINT32_ARRAY: return new StringBuffer(
		UINT32_ARRAY);

	    case CIMDataType.UINT64: return new StringBuffer(UINT64);
	    case CIMDataType.UINT64_ARRAY: return new StringBuffer(
		UINT64_ARRAY);

	    default:
		if (cimDataType.isReferenceType()) {

		    return (new StringBuffer(
			cimDataType.getRefClassName()).append(BEAN));

		} else {

		    return (new StringBuffer(
			"UNKNOWN DATATYPE: ").append(cimDataType.toString()));

		}

	}

    } // getDataType

    /**
     * This method creates JavaDocElements for Exceptions and adds
     * those elements to the end of the specified Vector.
     *
     * @param	vJavaDocElements	container of JavaDocElements
     * @param	vExceptions	container of Exceptions
     */
    private static void addExceptionsToVector(Vector vJavaDocElements,
	Vector vExceptions) {

	// the vector's capacity should be set at creation time to
	// indicate whether there are exceptions that will need to 
	// be added.
	if (vJavaDocElements.capacity() > 1) {

	    String currentException;
	    Enumeration enExceptions = vExceptions.elements();
	    JavaDocElement javaDocElement;
	    while (enExceptions.hasMoreElements()) {

		currentException = (String)enExceptions.nextElement();
		javaDocElement = new JavaDocElement(JAVADOC_EXCEPTION_TAG,
		    new StringBuffer(currentException), "");
		vJavaDocElements.addElement(javaDocElement);

	    }

	}

    } // addExceptionsToVector

    /**
     * This method creates JavaDocElements from the CIMParameters in
     * the specified Vector and inserts them at the beginning of 
     * another specified Vector. These elements are created after the 
     * JavaDocElements for the return value and Exceptions, but since 
     * parameters should appear before those elements in the JavaDoc, 
     * they are inserted in the correct order at the beginning of the 
     * Vector.
     *
     * @param	vJavaDocElements	container of JavaDocElements
     * @param	vParams	container of CIMParameters
     */
    private static void insertParamsInVector(Vector vJavaDocElements,
	Vector vParams) {

	// if there are no parameters, there's nothing to insert
	//
	if (!hasContents(vParams)) {

	    return;

	}

	// any CIMParameter with an [IN] qualifier is included
	// as an input parameter in the invokeMethod
	//
	Enumeration en = vParams.elements();
	CIMParameter cimParam;
	int insertCounter = 0;
	JavaDocElement javaDocElement;
	while (en.hasMoreElements()) {

	    cimParam = (CIMParameter)en.nextElement();
	    if (isInParameter(cimParam)) {

		javaDocElement = new JavaDocElement(JAVADOC_PARAM_TAG,
		    getDataType(cimParam.getType()),
		    I18N.loadStringFormat("INVOKEMETHOD_PARAM_DESCRIPTION",
		    cimParam.getName()));
		vJavaDocElements.insertElementAt(javaDocElement, insertCounter);
		insertCounter++;

	    }

	}

    } // insertParamsInVector

    /**
     * This method returns the exceptions thrown by a method.
     *
     * @return	StringBuffer	formatted exceptions
     */
    public static StringBuffer getExceptions() {

	if (!hasContents(exceptions) && hasContents(vExceptions)) {

	    exceptions = new StringBuffer(THROWS);
	    Enumeration eExceptions = vExceptions.elements();
	    while (eExceptions.hasMoreElements()) {

		exceptions.append((String)eExceptions.nextElement());
		exceptions.append(COMMA);

	    }
	    // remove the trailing comma
	    //
	    exceptions = new StringBuffer(exceptions.substring(0, 
		exceptions.length() - 2));

	}
	return (exceptions);

    } // getExceptions

    /**
     * This method returns the exceptions that must be thrown by a method.
     * Note: getExceptions() is not adequate for getting invokeMethod 
     * exceptions as CIMException must additionally be thrown.
     *
     * @return	StringBuffer	formatted exceptions for invokeMethods
     */
    public static StringBuffer getInvokeMethodExceptions() {

	// invoke methods have to throw CIMException in addition to
	// the user defined exceptions
	//
	if (!hasContents(invokeMethodExceptions)) {

	    // handle the case that there weren't any user defined exceptions
	    //
	    if (!hasContents(getExceptions())) {

		invokeMethodExceptions = new StringBuffer(THROWS_CIMEXCEPTION);

	    } else {

		invokeMethodExceptions = new StringBuffer(
		    getExceptions().toString());
		invokeMethodExceptions.append(COMMA).append(CIMEXCEPTION);

	    }

	}
	return (invokeMethodExceptions);

    } // getInvokeMethodExceptions

    /**
     * This method is a convenience wrapper of MessageFormat.format()
     * for use with constants defined in BeanGeneratorConstants.java.
     *
     * @param	pattern	pattern string
     * @param	arg1	argument to substitute in the pattern string
     * @return	formatted string
     */
    public static String format(String pattern, String arg1) {

	String[] args = {arg1};
	return (MessageFormat.format(pattern, args));

    } // format

    /**
     * This method is a convenience wrapper of MessageFormat.format()
     * for use with constants defined in BeanGeneratorConstants.java.
     *
     * @param	pattern	pattern string
     * @param	arg1	argument to substitute in the pattern string
     * @param	arg2	argument to substitute in the pattern string
     * @return	formatted string
     */
    public static String format(String pattern, String arg1, String arg2) {

	String[] args = {arg1, arg2};
	return (MessageFormat.format(pattern, args));

    } // format

    /**
     * This method is a convenience wrapper of MessageFormat.format()
     * for use with constants defined in BeanGeneratorConstants.java.
     *
     * @param	pattern	pattern string
     * @param	arg1	argument to substitute in the pattern string
     * @param	arg2	argument to substitute in the pattern string
     * @param	arg3	argument to substitute in the pattern string
     * @return	formatted string
     */
    public static String format(String pattern, String arg1, String arg2,
	String arg3) {

	String[] args = {arg1, arg2, arg3};
	return (MessageFormat.format(pattern, args));

    } // format

    /**
     * This method is a convenience wrapper of MessageFormat.format()
     * for use with constants defined in BeanGeneratorConstants.java.
     *
     * @param	pattern	pattern string
     * @param	arg1	argument to substitute in the pattern string
     * @param	arg2	argument to substitute in the pattern string
     * @param	arg3	argument to substitute in the pattern string
     * @param	arg4	argument to substitute in the pattern string
     * @return	formatted string
     */
    public static String format(String pattern, String arg1, String arg2,
	String arg3, String arg4) {

	String[] args = {arg1, arg2, arg3, arg4};
	return (MessageFormat.format(pattern, args));

    } // format

} // BeanGenerator

