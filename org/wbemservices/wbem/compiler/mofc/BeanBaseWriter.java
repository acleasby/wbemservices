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

import java.io.File;
import java.util.Vector;

/**
 * This class generates the base source files for 'mofcomp -j'. This base
 * source includes: 
 *
 * CIMBean: the base Java Interface
 * CIMBeanImpl: the base Java Bean.
 */
class BeanBaseWriter implements BeanGeneratorConstants {

    // class variables
    //
    private StringBuffer	fileHeader = null;
    private StringBuffer	packageStatement = null;

    private JavaDocElement	javaDocElement = null;
    private Vector		vConstructorDoc = null;
    private Vector		vGetCIMOMHandle = null;
    private Vector		vSetCIMOMHandle = null;
    private Vector		vGetCIMInstance = null;
    private Vector		vSetCIMInstance = null;
    private Vector		vUpdateDoc = null;
    private Vector		vGetPropertyDoc = null;
    private Vector		vGetArrayPropertyDoc = null;
    private Vector		vGetAssociationPropertyDoc = null;
    private Vector		vSetPropertyDoc = null;
    private Vector		vSetArrayPropertyDoc = null;
    private Vector		vObjPathDoc = null;
    private Vector		vGetArrayIndex = null;
    private Vector		vBeanKeysDoc = null;
    private Vector		vVersionDoc = null;
    private Vector		vStringDoc = null;

    /**
     * Constructs a BeanArgReader given a handle to a FileReader.
     * 
     * @param beanPackage the Java package name
     * @param beanDir directory to put generated output in
     */
    public BeanBaseWriter(String beanPackage, File beanDir) {

	super();
	this.fileHeader = BeanGenerator.getFileHeader();
	this.packageStatement = BeanGenerator.getPackageStatement(beanPackage);

	// set I18N Bundle
	//
	I18N.setResourceName("org.wbemservices.wbem.compiler.mofc.Compiler");

	// initialize the JavaDocElement containers for the methods
	//
	initJavaDocElements();

	// generate the CIMBean Interface
	//
	BeanGenerator.writeToFile(new StringBuffer(CIMBEAN), 
	    getCIMBeanInterface(), beanDir);

	// generate the CIMBeanImpl Class
	//
	BeanGenerator.writeToFile(new StringBuffer(CIMBEANIMPL),
	    getCIMBeanImplClass(), beanDir);

    } // constructor

    /**
     * This methods initializes the containers of JavaDocElements used
     * to generate the JavaDoc comments for the methods in CIMBean and
     * CIMBeanImpl that take parameters or return a value.
     */
    private void initJavaDocElements() {

	// CIMBeanImpl() JavaDocElements
	//
	vConstructorDoc = new Vector(2);
	javaDocElement = new JavaDocElement(JAVADOC_PARAM_TAG,
	    new StringBuffer(CIMOMHANDLE),
	    I18N.loadString("CIMOM_PARAM_DESCRIPTION"));
	vConstructorDoc.addElement(javaDocElement);
	javaDocElement = new JavaDocElement(JAVADOC_PARAM_TAG,
	    new StringBuffer(CIMINSTANCE),
	    I18N.loadString("INSTANCE_PARAM_DESCRIPTION"));
	vConstructorDoc.addElement(javaDocElement);

	// getCIMOMHandle() JavaDocElements
	//
	vGetCIMOMHandle = new Vector(1);
	javaDocElement = new JavaDocElement(JAVADOC_RETURN_TAG,
	    new StringBuffer(CIMOMHANDLE),
	    I18N.loadString("CIMOM_PARAM_DESCRIPTION"));
	vGetCIMOMHandle.addElement(javaDocElement);

	// setCIMOMHandle() JavaDocElements
	//
	vSetCIMOMHandle = new Vector(1);
	javaDocElement = new JavaDocElement(JAVADOC_PARAM_TAG,
	    new StringBuffer(CIMOMHANDLE),
	    I18N.loadString("CIMOM_PARAM_DESCRIPTION"));
	vSetCIMOMHandle.addElement(javaDocElement);

	// getCIMInstance() JavaDocElements
	//
	vGetCIMInstance = new Vector(1);
	javaDocElement = new JavaDocElement(JAVADOC_RETURN_TAG,
	    new StringBuffer(CIMINSTANCE),
	    I18N.loadString("INSTANCE_PARAM_DESCRIPTION"));
	vGetCIMInstance.addElement(javaDocElement);

	// setCIMInstance() JavaDocElements
	//
	vSetCIMInstance = new Vector(1);
	javaDocElement = new JavaDocElement(JAVADOC_PARAM_TAG,
	    new StringBuffer(CIMINSTANCE),
	    I18N.loadString("INSTANCE_PARAM_DESCRIPTION"));
	vSetCIMInstance.addElement(javaDocElement);

	// update(String Object) JavaDocElements
	//
	vUpdateDoc = new Vector(2);
	javaDocElement = new JavaDocElement(JAVADOC_PARAM_TAG,
	    new StringBuffer(STRING), 
	    I18N.loadString("UPDATE_STRINGPARAM_DESCRIPTION"));
	vUpdateDoc.addElement(javaDocElement);
	javaDocElement = new JavaDocElement(JAVADOC_PARAM_TAG, 
	    new StringBuffer(OBJECT),
	    I18N.loadString("UPDATE_OBJECTPARAM_DESCRIPTION"));
	vUpdateDoc.addElement(javaDocElement);

	// getProperty JavaDocElements
	//
	vGetPropertyDoc = new Vector(2);
	javaDocElement = new JavaDocElement(JAVADOC_PARAM_TAG,
	    new StringBuffer(STRING),
	    I18N.loadString("GETPROPERTY_PARAM_DESCRIPTION"));
	vGetPropertyDoc.addElement(javaDocElement);
	javaDocElement = new JavaDocElement(JAVADOC_RETURN_TAG,
	    new StringBuffer(OBJECT),
	    I18N.loadString("GETPROPERTY_RETURN_DESCRIPTION"));
	vGetPropertyDoc.addElement(javaDocElement);

	// getArrayProperty JavaDocElements
	//
	vGetArrayPropertyDoc = new Vector(4);
	javaDocElement = new JavaDocElement(JAVADOC_PARAM_TAG,
	    new StringBuffer(STRING),
	    I18N.loadString("ARRAYPROPERTY_STRPARAM_DESCRIPTION"));
	vGetArrayPropertyDoc.addElement(javaDocElement);
	javaDocElement = new JavaDocElement(JAVADOC_PARAM_TAG,
	    new StringBuffer(STRING_ARRAY),
	    I18N.loadString("ARRAYPROPERTY_STRARRPARAM_DESCRIPTION"));
	vGetArrayPropertyDoc.addElement(javaDocElement);
	javaDocElement = new JavaDocElement(JAVADOC_PARAM_TAG,
	    new StringBuffer(OBJECT_ARRAY),
	    I18N.loadString("ARRAYPROPERTY_OBJARRPARAM_DESCRIPTION"));
	vGetArrayPropertyDoc.addElement(javaDocElement);
	javaDocElement = new JavaDocElement(JAVADOC_RETURN_TAG,
	    new StringBuffer(STRING_ARRAY),
	    I18N.loadString("GETARRAYPROPERTY_RETURN_DESCRIPTION"));
	vGetArrayPropertyDoc.addElement(javaDocElement);

	// getAssociationProperty JavaDocElements
	//
	vGetAssociationPropertyDoc = new Vector(2);
	javaDocElement = new JavaDocElement(JAVADOC_PARAM_TAG,
	    new StringBuffer(CIMOBJECTPATH),
	    I18N.loadString("GETOBJECTPATH_RETURN_DESCRIPTION"));
	vGetAssociationPropertyDoc.addElement(javaDocElement);
	javaDocElement = new JavaDocElement(JAVADOC_PARAM_TAG,
	    new StringBuffer(CIMBEANIMPL),
	    I18N.loadString("GETASSOCPROPERTY_BEANPARAM_DESCRIPTION"));
	vGetAssociationPropertyDoc.addElement(javaDocElement);

	// setProperty JavaDocElements
	//
	vSetPropertyDoc = new Vector(2);
	javaDocElement = new JavaDocElement(JAVADOC_PARAM_TAG,
	    new StringBuffer(STRING),
	    I18N.loadString("SETPROPERTY_STRINGPARAM_DESCRIPTION"));
	vSetPropertyDoc.addElement(javaDocElement);
	javaDocElement = new JavaDocElement(JAVADOC_PARAM_TAG,
	    new StringBuffer(OBJECT),
	    I18N.loadString("SETPROPERTY_OBJECTPARAM_DESCRIPTION"));
	vSetPropertyDoc.addElement(javaDocElement);

	// setArrayProperty JavaDocElements
	//
	vSetArrayPropertyDoc = new Vector(4);
	javaDocElement = new JavaDocElement(JAVADOC_PARAM_TAG,
	    new StringBuffer(STRING),
	    I18N.loadString("ARRAYPROPERTY_STRPARAM_DESCRIPTION"));
	vSetArrayPropertyDoc.addElement(javaDocElement);
	javaDocElement = new JavaDocElement(JAVADOC_PARAM_TAG,
	    new StringBuffer(STRING_ARRAY),
	    I18N.loadString("ARRAYPROPERTY_STRARRPARAM_DESCRIPTION"));
	vSetArrayPropertyDoc.addElement(javaDocElement);
	javaDocElement = new JavaDocElement(JAVADOC_PARAM_TAG,
	    new StringBuffer(OBJECT_ARRAY),
	    I18N.loadString("ARRAYPROPERTY_OBJARRPARAM_DESCRIPTION"));
	vSetArrayPropertyDoc.addElement(javaDocElement);
	javaDocElement = new JavaDocElement(JAVADOC_PARAM_TAG,
	    new StringBuffer(STRING_ARRAY),
	    I18N.loadString("SETARRAYPROPERTY_STRARR_DESCRIPTION"));
	vSetArrayPropertyDoc.addElement(javaDocElement);

	// getBeanKeys() JavaDocElement
	//
	vBeanKeysDoc = new Vector(1);
	javaDocElement = new JavaDocElement(JAVADOC_RETURN_TAG,
	    new StringBuffer(STRING_ARRAY),
	    I18N.loadString("KEYS_RETURN_DESCRIPTION"));
	vBeanKeysDoc.addElement(javaDocElement);

	// getObjectPath() JavaDocElement
	//
	vObjPathDoc = new Vector(1);
	javaDocElement = new JavaDocElement(JAVADOC_RETURN_TAG,
	    new StringBuffer(CIMOBJECTPATH),
	    I18N.loadString("GETOBJECTPATH_RETURN_DESCRIPTION"));
	vObjPathDoc.addElement(javaDocElement);

	// getArrayIndex() JavaDocElements
	//
	vGetArrayIndex = new Vector(3);
	javaDocElement = new JavaDocElement(JAVADOC_PARAM_TAG,
	    new StringBuffer(OBJECT_ARRAY),
	    I18N.loadString("GETARRINDEX_ARRAY_DESCRIPTION"));
	vGetArrayIndex.addElement(javaDocElement);
	javaDocElement = new JavaDocElement(JAVADOC_PARAM_TAG,
	    new StringBuffer(OBJECT),
	    I18N.loadString("GETARRINDEX_OBJ_DESCRIPTION"));
	vGetArrayIndex.addElement(javaDocElement);
	javaDocElement = new JavaDocElement(JAVADOC_RETURN_TAG,
	    new StringBuffer(INT),
	    I18N.loadString("GETARRINDEX_RETURN_DESCRIPTION"));
	vGetArrayIndex.addElement(javaDocElement);

	// getVersion() JavaDocElement
	//
	vVersionDoc = new Vector(1);
	javaDocElement = new JavaDocElement(JAVADOC_RETURN_TAG,
	    new StringBuffer(STRING),
	    I18N.loadString("VERSION_RETURN_DESCRIPTION"));
	vVersionDoc.addElement(javaDocElement);

	// toString() JavaDocElement
	//
	vStringDoc = new Vector(1);
	javaDocElement = new JavaDocElement(JAVADOC_RETURN_TAG,
	    new StringBuffer(STRING),
	    I18N.loadString("TOSTRING_RETURN_DESCRIPTION"));
	vStringDoc.addElement(javaDocElement);

    } // initJavaDocElements

    /**
     * This method returns the content of the CIMBean base Interface.
     *
     * @return	StringBuffer	content of the CIMBean Interface
     */
    private StringBuffer getCIMBeanInterface() {

	// file header
	//
	StringBuffer sb = new StringBuffer(fileHeader.toString());

	// package statement
	//
	sb.append(packageStatement);

	// import statements
	//
	sb.append(CIMBEAN_IMPORTS).append(RETURN);

	// Interface JavaDoc
	//
	sb.append(BeanGenerator.getJavaDoc(I18N.loadString(
	    "CIMBEAN_INTERFACE_JAVADOC"), new StringBuffer(), (Vector)null, 
	    0));

	// Interface open
	//
	sb.append(BeanGenerator.getSourceLine(BeanGenerator.format(
	    INTERFACE_OPEN, CIM) + OPEN_BRACE + RETURN, 0));

	// getCIMOMHandle() JavaDoc
	//
	sb.append(BeanGenerator.getJavaDoc(I18N.loadString(
	    "CIMBEAN_GETCIMOMHANDLE_JAVADOC"), new StringBuffer(), 
	    vGetCIMOMHandle, 1));

	// getCIMOMHandle()
	//
	sb.append(BeanGenerator.getSourceLine(CIMBEAN_GETCIMOMHANDLE, 
	    1)).append(RETURN);

	// setCIMOMHandle() JavaDoc
	//
	sb.append(BeanGenerator.getJavaDoc(I18N.loadString(
	    "CIMBEAN_SETCIMOMHANDLE_JAVADOC"), new StringBuffer(), 
	    vSetCIMOMHandle, 1));

	// setCIMOMHandle()
	//
	sb.append(BeanGenerator.getSourceLine(CIMBEAN_SETCIMOMHANDLE, 
	    1)).append(RETURN);

	// getCIMInstance() JavaDoc
	//
	sb.append(BeanGenerator.getJavaDoc(I18N.loadString(
	    "CIMBEAN_GETCIMINSTANCE_JAVADOC"), new StringBuffer(), 
	    vGetCIMInstance, 1));

	// getCIMInstance()
	//
	sb.append(BeanGenerator.getSourceLine(CIMBEAN_GETCIMINSTANCE, 
	    1)).append(RETURN);

	// setCIMInstance() JavaDoc
	//
	sb.append(BeanGenerator.getJavaDoc(I18N.loadString(
	    "CIMBEAN_SETCIMINSTANCE_JAVADOC"), new StringBuffer(), 
	    vSetCIMInstance, 1));

	// setCIMInstance()
	//
	sb.append(BeanGenerator.getSourceLine(CIMBEAN_SETCIMINSTANCE, 
	    1)).append(RETURN);

	// update() JavaDoc
	// 
	sb.append(BeanGenerator.getJavaDoc(I18N.loadString(
	    "CIMBEAN_UPDATE_JAVADOC"), new StringBuffer(), (Vector)null,
	    1));

	// update()
	//
	sb.append(BeanGenerator.getSourceLine(CIMBEAN_UPDATE,
	    1)).append(RETURN);

	// update(String, Object) JavaDoc
	//
	sb.append(BeanGenerator.getJavaDoc(I18N.loadString(
	    "CIMBEAN_UPDATE2_JAVADOC"), new StringBuffer(), vUpdateDoc, 1));

	// update(String, Object)
	//
	sb.append(BeanGenerator.getSourceLine(CIMBEAN_UPDATE2,
	    1)).append(RETURN);

	// delete() JavaDoc
	//
	sb.append(BeanGenerator.getJavaDoc(I18N.loadString(
	    "CIMBEAN_DELETE_JAVADOC"), new StringBuffer(), (Vector)null,
	    1));

	// delete()
	//
	sb.append(BeanGenerator.getSourceLine(CIMBEAN_DELETE,
	    1)).append(RETURN);

	// getBeanKeys() JavaDoc
	//
	sb.append(BeanGenerator.getJavaDoc(I18N.loadString(
	    "CIMBEAN_GETBEANKEYS_JAVADOC"), new StringBuffer(), vVersionDoc,
	    1));

	// getBeanKeys()
	//
	sb.append(BeanGenerator.getSourceLine(CIMBEAN_GETBEANKEYS,
	    1)).append(RETURN);

	// getVersion() JavaDoc
	//
	sb.append(BeanGenerator.getJavaDoc(I18N.loadString(
	    "CIMBEAN_GETVERSION_JAVADOC"), new StringBuffer(), vBeanKeysDoc,
	    1));

	// getVersion()
	//
	sb.append(BeanGenerator.getSourceLine(CIMBEAN_GETVERSION,
	    1)).append(RETURN);

	// toString() JavaDoc
	//
	sb.append(BeanGenerator.getJavaDoc(I18N.loadString(
	    "CIMBEAN_TOSTRING_JAVADOC"), new StringBuffer(), vStringDoc, 1));

	// toString()
	//
	sb.append(BeanGenerator.getSourceLine(CIMBEAN_TOSTRING,
	    1)).append(RETURN);

	// Interface close
	//
	sb.append(BeanGenerator.getSourceLine(CLOSE_BRACE + 
	    BeanGenerator.format(INTERFACE_CLOSE, CIM), 0));

	return (sb);

    } // getCIMBeanInterface

    /**
     * This method returns the content of the CIMBeanImpl base Class.
     *
     * @return	StringBuffer	content of the CIMBeanImpl Class
     */
    private StringBuffer getCIMBeanImplClass() {

	// file header
	//
	StringBuffer sb = new StringBuffer(fileHeader.toString());

	// package statement
	//
	sb.append(packageStatement);

	// import statements
	//
	sb.append(CIMBEANIMPL_IMPORTS).append(RETURN);

	// Class JavaDoc
	//
	sb.append(BeanGenerator.getJavaDoc(I18N.loadString(
	    "CIMBEANIMPL_CLASS_JAVADOC"), new StringBuffer(), (Vector)null, 0));

	// Class open
	//
	sb.append(BeanGenerator.getSourceLine(CIMBEANIMPL_OPEN + RETURN, 0));

	// Class variables
	//
	sb.append(CIMBEANIMPL_VARIABLES).append(RETURN);

	// default constructor JavaDoc
	//
	String constructorDesc = I18N.loadString(
	    "CIMBEANIMPL_DEFAULTCONSTRUCTOR_JAVADOC");
	sb.append(BeanGenerator.getJavaDoc(constructorDesc, new StringBuffer(), 
	    (Vector)null, 1));

	// default constructor
	//
	sb.append(CIMBEANIMPL_DEFAULTCONSTRUCTOR).append(RETURN);

	// full constructor JavaDoc
	//
	constructorDesc = I18N.loadString(
	    "CIMBEANIMPL_FULLCONSTRUCTOR_JAVADOC");
	sb.append(BeanGenerator.getJavaDoc(constructorDesc, new StringBuffer(), 
	    vConstructorDoc, 1));

	// full constructor
	//
	sb.append(CIMBEANIMPL_FULLCONSTRUCTOR).append(RETURN);

	// getCIMOMHandle() JavaDoc
	//
	sb.append(BeanGenerator.getJavaDoc(I18N.loadString(
	    "CIMBEAN_GETCIMOMHANDLE_JAVADOC"), new StringBuffer(), 
	    vGetCIMOMHandle, 1));

	// getCIMOMHandle()
	//
	sb.append(CIMBEANIMPL_GETCIMOMHANDLE).append(RETURN);

	// setCIMOMHandle() JavaDoc
	//
	sb.append(BeanGenerator.getJavaDoc(I18N.loadString(
	    "CIMBEAN_SETCIMOMHANDLE_JAVADOC"), new StringBuffer(), 
	    vSetCIMOMHandle, 1));

	// setCIMOMHandle()
	//
	sb.append(CIMBEANIMPL_SETCIMOMHANDLE).append(RETURN);

	// getCIMInstance() JavaDoc
	//
	sb.append(BeanGenerator.getJavaDoc(I18N.loadString(
	    "CIMBEAN_GETCIMINSTANCE_JAVADOC"), new StringBuffer(), 
	    vGetCIMInstance, 1));

	// getCIMInstance()
	//
	sb.append(CIMBEANIMPL_GETCIMINSTANCE).append(RETURN);

	// setCIMInstance() JavaDoc
	//
	sb.append(BeanGenerator.getJavaDoc(I18N.loadString(
	    "CIMBEAN_SETCIMINSTANCE_JAVADOC"), new StringBuffer(), 
	    vSetCIMInstance, 1));

	// setCIMInstance()
	//
	sb.append(CIMBEANIMPL_SETCIMINSTANCE).append(RETURN);

	// update() JavaDoc
	// 
	sb.append(BeanGenerator.getJavaDoc(I18N.loadString(
	    "CIMBEAN_UPDATE_JAVADOC"), new StringBuffer(), (Vector)null,
	    1));

	// update()
	//
	sb.append(CIMBEANIMPL_UPDATE).append(RETURN);

	// update(String, Object) JavaDoc
	//
	sb.append(BeanGenerator.getJavaDoc(I18N.loadString(
	    "CIMBEAN_UPDATE2_JAVADOC"), new StringBuffer(), vUpdateDoc, 1));

	// update(String, Object)
	//
	sb.append(CIMBEANIMPL_UPDATE2).append(RETURN);

	// delete() JavaDoc
	//
	sb.append(BeanGenerator.getJavaDoc(I18N.loadString(
	    "CIMBEAN_DELETE_JAVADOC"), new StringBuffer(), (Vector)null,
	    1));

	// delete()
	//
	sb.append(CIMBEANIMPL_DELETE).append(RETURN);

	// getProperty() JavaDoc
	//
	sb.append(BeanGenerator.getJavaDoc(I18N.loadString(
	    "CIMBEANIMPL_GETPROPERTY_JAVADOC"), new StringBuffer(), 
	    vGetPropertyDoc, 1));

	// getProperty()
	//
	sb.append(CIMBEANIMPL_GETPROPERTY).append(RETURN);

	// getArrayProperty() JavaDoc
	//
	sb.append(BeanGenerator.getJavaDoc(I18N.loadString(
	    "CIMBEANIMPL_GETARRAYPROPERTY_JAVADOC"), new StringBuffer(), 
	    vGetArrayPropertyDoc, 1));

	// getArrayProperty()
	//
	sb.append(CIMBEANIMPL_GETARRAYPROPERTY).append(RETURN);

	// getAssociationProperty() JavaDoc
	//
	sb.append(BeanGenerator.getJavaDoc(I18N.loadString(
	    "CIMBEANIMPL_GETASSOCIATIONPROPERTY_JAVADOC"), new StringBuffer(), 
	    vGetAssociationPropertyDoc, 1));

	// getAssociationProperty()
	//
	sb.append(CIMBEANIMPL_GETASSOCIATIONPROPERTY).append(RETURN);

	// setProperty() JavaDoc
	//
	sb.append(BeanGenerator.getJavaDoc(I18N.loadString(
	    "CIMBEANIMPL_SETPROPERTY_JAVADOC"), new StringBuffer(), 
	    vSetPropertyDoc, 1));

	// setProperty()
	//
	sb.append(CIMBEANIMPL_SETPROPERTY).append(RETURN);

	// setArrayProperty() JavaDoc
	//
	sb.append(BeanGenerator.getJavaDoc(I18N.loadString(
	    "CIMBEANIMPL_SETARRAYPROPERTY_JAVADOC"), new StringBuffer(), 
	    vSetArrayPropertyDoc, 1));

	// setArrayProperty()
	//
	sb.append(CIMBEANIMPL_SETARRAYPROPERTY).append(RETURN);

	// getBeanKeys() JavaDoc
	//
	sb.append(BeanGenerator.getJavaDoc(I18N.loadString(
	    "CIMBEAN_GETBEANKEYS_JAVADOC"), new StringBuffer(), vBeanKeysDoc,
	    1));

	// getBeanKeys()
	//
	sb.append(CIMBEANIMPL_GETBEANKEYS).append(RETURN);

	// getObjectPath() JavaDoc
	//
	sb.append(BeanGenerator.getJavaDoc(I18N.loadString(
	    "CIMBEANIMPL_GETOBJPATH_JAVADOC"), new StringBuffer(), vObjPathDoc,
	    1));

	// getObjectPath()
	//
	sb.append(CIMBEANIMPL_GETOBJECTPATH).append(RETURN);

	// getArrayIndex() JavaDoc
	//
	sb.append(BeanGenerator.getJavaDoc(I18N.loadString(
	    "CIMBEANIMPL_GETARRAYINDEX_JAVADOC"), new StringBuffer(), 
	    vGetArrayIndex, 1));

	// getArrayIndex()
	//
	sb.append(CIMBEANIMPL_GETARRAYINDEX).append(RETURN);

	// getVersion() JavaDoc
	//
	sb.append(BeanGenerator.getJavaDoc(I18N.loadString(
	    "CIMBEAN_GETVERSION_JAVADOC"), new StringBuffer(), vVersionDoc,
	    1));

	// getVersion()
	//
	sb.append(CIMBEANIMPL_GETVERSION).append(RETURN);

	// toString() JavaDoc
	//
	sb.append(BeanGenerator.getJavaDoc(I18N.loadString(
	    "CIMBEAN_TOSTRING_JAVADOC"), new StringBuffer(), vStringDoc, 1));

	// toString()
	//
	sb.append(CIMBEANIMPL_TOSTRING).append(RETURN);

	// Class close
	//
	sb.append(BeanGenerator.getSourceLine(CLOSE_BRACE + 
	    BeanGenerator.format(CLASS_CLOSE, CIM), 0));

	return (sb);

    } // getCIMBeanImplClass

} // BeanBaseWriter
