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

import java.util.Vector;
import java.util.Enumeration;

import javax.wbem.client.Debug;

import javax.wbem.cim.CIMClass;
import javax.wbem.cim.CIMParameter;
import javax.wbem.cim.CIMProperty;
import javax.wbem.cim.CIMMethod;

/**
 * This class generates the Java Interface source that represents
 * the specified CIMClass.
 */
class BeanInterfaceWriter implements BeanGeneratorConstants {

    // relevant data needed to generate src
    //
    private CIMClass	cimClass = null;
    private Vector	vProperties = null;
    private Vector	vMethods = null;
    private String	superClass = null;
    private String	className = null;

    /**
     * Constructs a BeanInterfaceWriter to generate Bean Intefaces from MOFs.
     *
     * @param	cimClass	the CIMClass for the MOF being compiled
     */
    public BeanInterfaceWriter(CIMClass cimClass) {

	super();
	this.cimClass = cimClass;
	Debug.trace2("Generating Java Interface source for " + 
	    cimClass.getName());

	// set I18N Bundle
	I18N.setResourceName("org.wbemservices.wbem.compiler.mofc.Compiler");

	populateClassData();
	generateInterface();

    } // constructor

    /**
     * This method gets the information from the CIMClass needed to
     * generate the Interface source code.
     */
    private void populateClassData() {

	this.superClass = BeanGenerator.getSuperClass(cimClass);
	this.className = BeanGenerator.getClassName(cimClass.getName());
	this.vProperties = cimClass.getProperties();
	this.vMethods = cimClass.getMethods();

    } // populateClassData

    /**
     * This method directs the generation of the Interface source code.
     */
    private void generateInterface() {

	StringBuffer nameBuffer = new StringBuffer(className);
	nameBuffer.append(BEAN);
	StringBuffer sb = new StringBuffer();

	sb.append(BeanGenerator.getFileHeader());
	sb.append(BeanGenerator.getPackageStatement());
	sb.append(BeanGenerator.getImportStatements());
	sb.append(getInterfaceOpen());
	sb.append(getPropertyMethods());
	sb.append(getInvokeMethods());
	sb.append(getInterfaceClose());

	BeanGenerator.writeToFile(nameBuffer, sb);

    } // generateInterface

    /**
     * This method returns the JavaDoc for the Interface and the code 
     * defining the Interface.
     *
     * @return	StringBuffer	JavaDoc and interface definition statement
     */
    private StringBuffer getInterfaceOpen() {

	Debug.trace3("Generating Interface definition for: " + className);
	// the Interface JavaDoc
	//
	String beanDesc = I18N.loadStringFormat("BEAN_INTERFACE_JAVADOC",
	    className);
	StringBuffer mofDesc = BeanGenerator.getClassDescription(cimClass);
	StringBuffer sb = BeanGenerator.getJavaDoc(beanDesc, mofDesc,
	    (Vector)null, 0);

	// the Interface definition depends on the superclass
	//
	String openLine;
	if (BeanGenerator.hasContents(superClass)) {

	    openLine = BeanGenerator.format(CHILD_INTERFACE_OPEN, className,
		superClass);

	} else {

	    openLine = BeanGenerator.format(INTERFACE_OPEN, className);

	}
	sb.append(openLine).append(OPEN_BRACE).append(RETURN);
	return (sb);

    } // getInterfaceOpen

    /**
     * This method returns the closing line of code in the Interface.
     *
     * @return	StringBuffer	closing line of code in the Interface
     */
    private StringBuffer getInterfaceClose() {

	StringBuffer sb = new StringBuffer(CLOSE_BRACE);
	sb.append(BeanGenerator.format(INTERFACE_CLOSE, className));
	return (sb);

    } // getInterfaceClose

    /**
     * This method returns all generated JavaDoc and methods related to the 
     * properties in the class. These include accessor, mutator, display name 
     * accessor, units accessor, and invokeMethods.
     *
     * @return	StringBuffer	JavaDoc and methods related to class properties
     */
    private StringBuffer getPropertyMethods() {

	Debug.trace3("Generating Interface property definitions for: " 
	    + className);
	StringBuffer sb = new StringBuffer();
	// make sure there are actually properties defined in the class.
	//
	if (!BeanGenerator.hasContents(vProperties)) {

	    return (sb);

	}
	Enumeration enumeration = vProperties.elements();
	CIMProperty cimProp;
	while (enumeration.hasMoreElements()) {

	    cimProp = (CIMProperty)enumeration.nextElement();
	    sb.append(generatePropertyMethods(cimProp));

	}
	return (sb.append(RETURN));

    } // getPropertyMethods

    /**
     * This method generates the relevant methods for the specified
     * property.
     *
     * @param	cimProp	property to generate property methods for
     * @return	properly formatted property methods
     */
    private StringBuffer generatePropertyMethods(CIMProperty cimProp) {

	StringBuffer sb = new StringBuffer();
	boolean isOverridden = BeanGenerator.hasQualifier(cimProp, OVERRIDE);
	boolean hasConstants = BeanGenerator.hasQualifier(cimProp, VALUEMAP) ||
	    BeanGenerator.hasQualifier(cimProp, VALUES);

	// don't add property methods for an overriding property.
	// somewhere up the Interface inheritance they are already
	// defined. we do need to add constants for ValueMap/Values 
	// qualified properties even if they have the Override
	// qualifier.
	//
	if (isOverridden && !hasConstants) {

	    return (sb);

	}
	cimProp.setOriginClass(className);
	if (!isOverridden) {

	    // since we don't want to special case these methods for
	    // ValueMap/Values qualified properties, let the accessor/mutator
	    // generation fall through to getConstantsMethods(), but
	    // generate Units/DisplayName methods here as appropriate.
	    //
	    if (!hasConstants) {

		sb.append(BeanGenerator.getAccessorDoc(cimProp));
		sb.append(getAccessor(cimProp));
		sb.append(BeanGenerator.getMutatorDoc(cimProp));
		sb.append(getMutator(cimProp));

	    }
	    StringBuffer tmpBuffer = BeanGenerator.getUnitsDoc(cimProp);
	    if (BeanGenerator.hasContents(tmpBuffer)) {

		sb.append(tmpBuffer).append(getUnits(cimProp));

	    }
	    tmpBuffer = BeanGenerator.getDisplayNameDoc(cimProp);
	    if (BeanGenerator.hasContents(tmpBuffer)) {

		sb.append(tmpBuffer).append(getDisplayName(cimProp));

	    }

	}
	if (hasConstants && !isOverridden) {

	    sb.append(getConstants(cimProp));
	    sb.append(getConstantsMethods(cimProp));

	}
	return (sb);

    } // generatePropertyMethods

    /**
     * This method returns the code defining the accessor method for the 
     * specified property.
     *
     * @param	cimProp	property to generate the accessor interface for
     * @return	accessor method interface
     */
    private StringBuffer getAccessor(CIMProperty cimProp) {

	Debug.trace3("Generating Interface property accessor for: " 
	    + className + "." + cimProp.getName());
	String interfaceStr = BeanGenerator.format(INTERFACE_ACCESSOR,
	    BeanGenerator.getPropertyDataType(cimProp).toString(), 
	    BeanGenerator.firstCharUpper(cimProp.getName()).toString(), 
	    BeanGenerator.getExceptions().toString());

	StringBuffer sb = BeanGenerator.getSourceLine(interfaceStr, 1);
	return (sb.append(RETURN).append(RETURN));

    } // getAccessor

    /**
     * This method returns the code defining the mutator method for the 
     * specified property.
     *
     * @param	cimProp	the property to get the mutator interface for
     * @return	mutator method interface
     */
    private StringBuffer getMutator(CIMProperty cimProp) {

	Debug.trace3("Generating Interface property mutator for: " 
	    + className + "." + cimProp.getName());
	String interfaceStr = BeanGenerator.format(INTERFACE_MUTATOR,
	    BeanGenerator.firstCharUpper(cimProp.getName()).toString(), 
	    BeanGenerator.getPropertyDataType(cimProp).toString(), 
	    BeanGenerator.getParameterName(cimProp.getName()), 
	    BeanGenerator.getExceptions().toString());

	StringBuffer sb = BeanGenerator.getSourceLine(interfaceStr, 1);
	return (sb.append(RETURN).append(RETURN));

    } // getMutator

    /**
     * This method returns the code defining the units accessor method for 
     * the specified property.
     *
     * @param	cimProp	the property to get the units interface for
     * @return	units accessor method interface
     */
    private StringBuffer getUnits(CIMProperty cimProp) {

	Debug.trace3("Generating Interface property units accessor for: " 
	    + className + "." + cimProp.getName());
	String interfaceStr = BeanGenerator.format(INTERFACE_UNITS,
	    BeanGenerator.firstCharUpper(cimProp.getName()).toString(), 
	    BeanGenerator.getExceptions().toString());

	StringBuffer sb = BeanGenerator.getSourceLine(interfaceStr, 1);
	return (sb.append(RETURN).append(RETURN));

    } // getUnits

    /**
     * This method returns the code defining the display name accessor method 
     * for the specified property.
     *
     * @param	cimProp	property to get the display name interface for
     * @return	display name accessor method interface
     */
    private StringBuffer getDisplayName(CIMProperty cimProp) {

	Debug.trace3("Generating Interface property display name accessor for: "
	    + className + "." + cimProp.getName());
	String interfaceStr = BeanGenerator.format(INTERFACE_DISPLAYNAME, 
	    BeanGenerator.firstCharUpper(cimProp.getName()).toString(), 
	    BeanGenerator.getExceptions().toString());

	StringBuffer sb = BeanGenerator.getSourceLine(interfaceStr, 1);
	return (sb.append(RETURN).append(RETURN));

    } // getDisplayName

    /**
     * This method returns the JavaDoc and code defining the public constants 
     * for the ValueMap/Values qualified property specified.
     * NOTE: This method should only be called if the property has the 
     * ValueMap and/or Values qualifiers.
     *
     * @param	cimProp	ValueMap/Values qualified property
     * @return	ValueMap/Values constants
     */
    private StringBuffer getConstants(CIMProperty cimProp) {

	// constant JavaDoc
	//
	StringBuffer sb = BeanGenerator.getJavaDoc(I18N.loadStringFormat(
	    "PROP_VALUEMAP_CONSTANTS_JAVADOC", cimProp.getName()),
	    new StringBuffer(), (Vector)null, 1);

	// constants
	//
	String prefix = cimProp.getName().toUpperCase() + VALUES_PREFIX;
	String constantName;
	String constantValue;
	Enumeration eConstantNames = 
	    (BeanGenerator.getConstantNames(cimProp, prefix)).elements();
	Enumeration eConstantValues = 
	    (BeanGenerator.getValuesConstantValues(cimProp)).elements();

	// the Enumerations have a 1-1 relationship, so looping through
	// either is acceptable
	//
	while (eConstantNames.hasMoreElements()) {

	    constantName = (String)eConstantNames.nextElement();
	    constantValue = (String)eConstantValues.nextElement();
	    sb.append(BeanGenerator.getSourceLine(BeanGenerator.format(
		VALUES_CONSTANT, constantName, constantValue), 1));

	}
	return (sb.append(RETURN));

    } // getConstants

    /**
     * This method returns the JavaDoc and code defining the public constants 
     * for the ValueMap/Values qualified invokeMethod parameter specified.
     * NOTE: This method should only be called if the parameter has the 
     * ValueMap and/or Values qualifiers.
     *
     * @param	cimParam	ValueMap/Values qualified invokeMethod parameter
     * @param	name		invokeMethod name the parameter came from
     * @return	ValueMap/Values constants
     */
    private StringBuffer getConstants(CIMParameter cimParam, String name) {

	// constant JavaDoc
	//
	StringBuffer sb = BeanGenerator.getJavaDoc(I18N.loadStringFormat(
	    "PARAM_VALUEMAP_CONSTANTS_JAVADOC", cimParam.getName(), name),
	    new StringBuffer(), (Vector)null, 1);

	// constants
	//
	String prefix = name.toUpperCase() + "_" + 
	    cimParam.getName().toUpperCase() + VALUES_PREFIX;
	String constantName;
	String constantValue;
	Enumeration eConstantNames = 
	    (BeanGenerator.getConstantNames(cimParam, prefix)).elements();
	Enumeration eConstantValues = 
	    (BeanGenerator.getValuesConstantValues(cimParam)).elements();

	// the Enumerations have a 1-1 relationship, so looping through
	// either is acceptable
	//
	while (eConstantNames.hasMoreElements()) {

	    constantName = (String)eConstantNames.nextElement();
	    constantValue = (String)eConstantValues.nextElement();
	    sb.append(BeanGenerator.getSourceLine(BeanGenerator.format(
		VALUES_CONSTANT, constantName, constantValue), 1));

	}
	return (sb.append(RETURN));

    } // getConstants

    /**
     * This method returns the JavaDoc and code defining the accessor,
     * mutator, and constant accessor methods for the specified 
     * ValueMap/Values qualified property. The following outlines the 
     * methods generated:
     *
     * public String get<Prop>(); // accessor
     * public void set<Prop>(String) throws IllegalArgumentException; // mutator
     * public String[] get<Prop>Values(); // constant accessor
     *
     * NOTE: This method should only be called when the specified property
     * has the ValueMap and/or Values qualifiers. Also, the String datatype
     * in the accessor/mutator method signatures above is String[] if the
     * property is an array datatype.
     *
     * @param	cimProp	ValueMap/Values qualified property
     * @return	ValueMap/Values JavaDoc and methods
     */
    private StringBuffer getConstantsMethods(CIMProperty cimProp) {

	StringBuffer dataType = new StringBuffer(STRING);

	// if the property is an array type, we need to use String[] as
	// the Interface datatype.
	//
	StringBuffer realType = BeanGenerator.getDataType(cimProp.getType());
	if (BeanGenerator.isArrayType(realType)) {

	    dataType = new StringBuffer(STRING_ARRAY);

	}
	StringBuffer dataTypeArr = new StringBuffer(STRING_ARRAY);
	String propName = BeanGenerator.firstCharUpper(
	    cimProp.getName()).toString();

	// accessor JavaDoc
	//
	JavaDocElement javaDocElement = new JavaDocElement(JAVADOC_RETURN_TAG,
	    dataType, I18N.loadStringFormat("ACCESSOR_RETURN_DESCRIPTION", 
	    propName));
	Vector vJavaDocElements = new Vector(1);
	vJavaDocElements.addElement(javaDocElement);
	StringBuffer sb = BeanGenerator.getJavaDoc(I18N.loadStringFormat(
	    "BEAN_ACCESSOR_JAVADOC", className, cimProp.getName()),
	    BeanGenerator.getPropertyDescription(cimProp), vJavaDocElements, 1);

	// accessor
	//
	sb.append(BeanGenerator.getSourceLine(BeanGenerator.format(
	    INTERFACE_ACCESSOR, dataType.toString(), propName,
	    BeanGenerator.getExceptions().toString()), 1));
	sb.append(RETURN).append(RETURN);

	// mutator JavaDoc
	//
	javaDocElement = new JavaDocElement(JAVADOC_PARAM_TAG, dataType,
	    I18N.loadStringFormat("MUTATOR_PARAM_DESCRIPTION", propName));
	vJavaDocElements = new Vector(2);
	vJavaDocElements.addElement(javaDocElement);
	javaDocElement = new JavaDocElement(JAVADOC_EXCEPTION_TAG, 
	    new StringBuffer(CIMEXCEPTION),
	    I18N.loadString("CIMEXCEPTION_DESCRIPTION"));
	vJavaDocElements.addElement(javaDocElement);
	sb.append(BeanGenerator.getJavaDoc(I18N.loadStringFormat(
	    "BEAN_MUTATOR_JAVADOC", className, cimProp.getName()), 
	    BeanGenerator.getPropertyDescription(cimProp), vJavaDocElements, 
	    1));

	// mutator
	//
	sb.append(BeanGenerator.getSourceLine(BeanGenerator.format(
	    INTERFACE_MUTATOR, propName, dataType.toString(), 
	    BeanGenerator.getParameterName(cimProp.getName()), 
	    THROWS_CIMEXCEPTION), 1));
	sb.append(RETURN).append(RETURN);

	// constant accessor JavaDoc
	//
	javaDocElement = new JavaDocElement(JAVADOC_RETURN_TAG, dataTypeArr,
	    I18N.loadString("VALUESACCESSOR_RETURN_DESCRIPTION"));
	vJavaDocElements = new Vector(1);
	vJavaDocElements.addElement(javaDocElement);
	sb.append(BeanGenerator.getJavaDoc(I18N.loadStringFormat(
	    "PROP_VALUES_ACCESSOR_JAVADOC", className, cimProp.getName()), 
	    BeanGenerator.getPropertyDescription(cimProp), vJavaDocElements, 
	    1));

	// constant accessor
	//
	sb.append(BeanGenerator.getSourceLine(BeanGenerator.format(
	    INTERFACE_GETVALUES, propName), 1));
	sb.append(RETURN).append(RETURN);

	return (sb);

    } // getConstantsMethods

    /**
     * This method returns the JavaDoc and code defining the constant 
     * accessor method for the specified ValueMap/Values qualified 
     * invokeMethod parameter. The following outlines the signature of 
     * the method generated:
     *
     * public String[] get<Method>_<Param>Values();
     *
     * NOTE: This method should only be called when the specified parameter
     * has the ValueMap and/or Values qualifiers.
     *
     * @param	cimParam	ValueMap/Values qualified invokeMethod parameter
     * @param	methodName	invokeMethod name
     * @return	ValueMap/Values JavaDoc and convenience method
     */
    private StringBuffer getConstantsMethod(CIMParameter cimParam,
	String methodName) {

	StringBuffer sb = new StringBuffer();
	StringBuffer dataType = new StringBuffer(STRING_ARRAY);
	String paramName = BeanGenerator.firstCharUpper(
	    cimParam.getName()).toString();
	String name = BeanGenerator.firstCharUpper(methodName).toString() +
	    "_" + paramName;

	// constant accessor JavaDoc
	//
	JavaDocElement javaDocElement = new JavaDocElement(JAVADOC_RETURN_TAG, 
	    dataType, I18N.loadString("VALUESACCESSOR_RETURN_DESCRIPTION"));
	Vector vJavaDocElements = new Vector(1);
	vJavaDocElements.addElement(javaDocElement);
	Object[] objArr = {className, methodName, paramName};
	sb.append(BeanGenerator.getJavaDoc(I18N.loadStringFormat(
	    "PARAM_VALUES_ACCESSOR_JAVADOC", objArr), 
	    new StringBuffer(), vJavaDocElements, 1));

	// constant accessor
	//
	sb.append(BeanGenerator.getSourceLine(BeanGenerator.format(
	    INTERFACE_GETVALUES, name), 1));
	sb.append(RETURN).append(RETURN);
	return (sb);

    } // getConstantsMethod

    /**
     * This method returns all generated JavaDoc and the Interface equivalent 
     * of the invokeMethods in the class. Note: Java constants will be
     * generated for Values/ValueMap qualified invokeMethod parameters.
     *
     * @return	StringBuffer	JavaDoc and methods for class invokeMethods
     */
    private StringBuffer getInvokeMethods() {

	Debug.trace3("Generating Interface invokeMethods for: " + className);
	StringBuffer sb = new StringBuffer();
	// make sure there are actually invokeMethods defined in the class.
	//
	if (!BeanGenerator.hasContents(vMethods)) {

	    return (sb);

	}
	Enumeration eMethods = vMethods.elements();
	CIMMethod cimMethod;
	CIMParameter cimParam;
	Vector vParams;
	while (eMethods.hasMoreElements()) {

	    cimMethod = (CIMMethod)eMethods.nextElement();

	    // generate Java constants for Values/ValueMap qualified method
	    // parameters
	    //
	    vParams = cimMethod.getParameters();
	    if (BeanGenerator.hasContents(vParams)) {

		Enumeration eParams = vParams.elements();
		while (eParams.hasMoreElements()) {

		    cimParam = (CIMParameter)eParams.nextElement();
		    if (BeanGenerator.hasQualifier(cimParam, VALUES) ||
			BeanGenerator.hasQualifier(cimParam, VALUEMAP)) {

			sb.append(getConstants(cimParam, cimMethod.getName()));
			sb.append(getConstantsMethod(cimParam, 
			    cimMethod.getName()));

		    }

		}

	    }
	    cimMethod.setOriginClass(className);
	    sb.append(BeanGenerator.getInvokeMethodDoc(cimMethod));
	    sb.append(getInvokeMethod(cimMethod));

	    // generate an <MethodName>Output Interface if the method
	    // has an [OUT] qualified parameter
	    //
	    if (BeanGenerator.hasOutParameter(cimMethod)) {

		BeanGenerator.writeToFile(BeanGenerator.getOutputInterfaceName(
		    cimMethod), getMethodOutputInterface(cimMethod));

	    }

	}
	// chop off the extra newline before returning
	//
	sb = new StringBuffer(sb.substring(0, sb.length() - 1));
	return (sb.append(RETURN));

    } // getInvokeMethods

    /**
     * This method returns the code defining the Interface equivalent of the 
     * invokeMethod specified.
     *
     * @param	cimMethod	the invokeMethod to get the method for
     * @return	invokeMethod interface
     */
    private StringBuffer getInvokeMethod(CIMMethod cimMethod) {

	Debug.trace3("Generating Interface invokeMethod for: " + className +
	    "." + cimMethod.getName());
	String interfaceStr = BeanGenerator.format(INTERFACE_INVOKEMETHOD,
	    BeanGenerator.getDataType(cimMethod.getType()).toString(),
	    cimMethod.getName(),
	    BeanGenerator.getInvokeMethodParams(cimMethod).toString(),
	    BeanGenerator.getInvokeMethodExceptions().toString());

	StringBuffer sb = BeanGenerator.getSourceLine(interfaceStr, 1);
	return (sb.append(RETURN).append(RETURN));

    } // getInvokeMethod

    /**
     * This method returns the <CIMClass>_<MethodName>Output Interface which 
     * is required as the last parameter of the Bean's version of the 
     * specified invokeMethod.
     *
     * The <CIMClass>BeanImpl Class that implements the generated 
     * <CIMClass>Bean Interface will contain an inner Class 
     * <CIMClass>_<MethodName>OutputImpl that implements this Interface. As 
     * the user cannot construct an instance of 
     * <CIMClass>_<MethodName>OutputImpl, they pass an null instance of 
     * <CIMClass>_<MethodName>Output as the last parameter to the Bean's 
     * equivalent of the specified invokeMethod, and the underlying Bean 
     * implementation will create the output Class and assign it to the
     * specified Interface handle.
     *
     * NOTE: This method should only be called if there is at least one [OUT]
     * qualifier method parameter.
     *
     * @param	cimMethod	invokeMethod to get 
     *				<CIMClass>_<MethodName>Output Interface for
     * @return	<CIMClass>_<MethodName>Output Interface
     */
    private StringBuffer getMethodOutputInterface(CIMMethod cimMethod) {

	StringBuffer sb = new StringBuffer();
	String interfaceName =
	    BeanGenerator.getOutputInterfaceName(cimMethod).toString();

	// generate the file header, package, and import statements
	//
	sb.append(BeanGenerator.getFileHeader());
	sb.append(BeanGenerator.getPackageStatement());
	sb.append(BeanGenerator.getImportStatements());

	// Interface JavaDoc
	//
	sb.append(BeanGenerator.getJavaDoc(I18N.loadStringFormat(
	    "METHODOUTPUT_INTERFACE_JAVADOC", cimMethod.getOriginClass(),
	    cimMethod.getName()), BeanGenerator.getMethodDescription(cimMethod),
	    (Vector)null, 0));

	// Interface definition
	//
	sb.append(BeanGenerator.getSourceLine(BeanGenerator.format(
	    METHODOUTPUT_INTERFACE_OPEN, interfaceName) + OPEN_BRACE + RETURN,
	    0));

	// output parameter accessor method(s)
	//
	CIMParameter[] outParams = BeanGenerator.getOutParameters(cimMethod);
	String paramName;
	StringBuffer dataType;
	CIMParameter cimParam;
	Vector vJavaDocElements = new Vector(1);
	for (int i = 0; i < outParams.length; i++) {
	    cimParam = outParams[i];
	    paramName = BeanGenerator.firstCharUpper(
		cimParam.getName()).toString();
	    dataType = BeanGenerator.getDataType(cimParam.getType());

	    // output parameter accessor JavaDoc
	    //
	    vJavaDocElements.addElement(new JavaDocElement(JAVADOC_RETURN_TAG,
		dataType, I18N.loadStringFormat("ACCESSOR_RETURN_DESCRIPTION",
		paramName)));
	    sb.append(BeanGenerator.getJavaDoc(BeanGenerator.format(
		I18N.loadString("METHODOUTPUT_ACCESSOR_JAVADOC"), paramName, 
		cimMethod.getOriginClass(), cimMethod.getName()), 
		new StringBuffer(), vJavaDocElements, 1));

	    // output parameter accessor
	    //
	    sb.append(BeanGenerator.getSourceLine(BeanGenerator.format(
		INTERFACE_ACCESSOR, dataType.toString(), paramName, ""), 
		1)).append(RETURN).append(RETURN);

	    vJavaDocElements.removeAllElements();

	}

	// Interface close
	//
	sb.append(BeanGenerator.getSourceLine(CLOSE_BRACE +
	    BeanGenerator.format(METHODOUTPUT_INTERFACE_CLOSE, interfaceName), 
	    0));
	return (sb);

    } // getMethodOutputInterface

} // BeanInterfaceWriter
