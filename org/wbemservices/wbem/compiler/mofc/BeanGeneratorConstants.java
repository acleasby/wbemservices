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

/**
 * This Interface defines constants used by the mof2bean feature of the
 * mofcomp. BeanGenerator, BeanBaseWriter, BeanClassWriter, and 
 * BeanInterfaceWriter implement this Interface.
 */
public interface BeanGeneratorConstants {

    // base Interface and Class constants
    //
    final static String CIMBEAN = "CIMBean";
    final static String CIMBEAN_IMPORTS = 
	"import javax.wbem.cim.CIMException;\n" +
	"import javax.wbem.client.CIMOMHandle;\n" +
	"import javax.wbem.cim.CIMInstance;\n";
    final static String CIMBEAN_GETCIMOMHANDLE =
	"public CIMOMHandle getCIMOMHandle();\n";
    final static String CIMBEAN_SETCIMOMHANDLE =
	"public void setCIMOMHandle(CIMOMHandle handle);\n";
    final static String CIMBEAN_GETCIMINSTANCE =
	"public CIMInstance getCIMInstance();\n";
    final static String CIMBEAN_SETCIMINSTANCE =
	"public void setCIMInstance(CIMInstance instance);\n";
    final static String CIMBEAN_UPDATE = 
	"public void update() throws CIMException;\n";
    final static String CIMBEAN_UPDATE2 = 
	"public void update(String propName, Object value) " +
	"throws CIMException;\n";
    final static String CIMBEAN_DELETE =
	"public void delete() throws CIMException;\n";
    final static String CIMBEAN_GETBEANKEYS = 
	"public String[] getBeanKeys();\n";
    final static String CIMBEAN_GETVERSION = 
	"public String getBeanVersion();\n";
    final static String CIMBEAN_TOSTRING = "public String toString();\n";

    final static String CIMBEANIMPL = "CIMBeanImpl";
    final static String CIMBEANIMPL_IMPORTS = "import java.io.Serializable;\n" +
	"import java.util.*;\nimport javax.wbem.client.CIMOMHandle;\n" +
	"import javax.wbem.cim.CIMException;\n" +
	"import javax.wbem.cim.CIMInstance;\n" +
	"import javax.wbem.cim.CIMObjectPath;\n" +
	"import javax.wbem.cim.CIMValue;\n" +
	"import javax.wbem.client.CIMOMHandle;\n";
    final static String CIMBEANIMPL_OPEN = 
	"public class CIMBeanImpl implements CIMBean, Serializable {\n";
    final static String CIMBEANIMPL_VARIABLES =
	"    private CIMInstance\tcimInstance = null;\n" +
	"    private CIMOMHandle\tcimomHandle = null;\n";
    final static String CIMBEANIMPL_DEFAULTCONSTRUCTOR =
	"    public CIMBeanImpl() {\n\n\tsuper();\n\n    } // constructor\n";
    final static String CIMBEANIMPL_FULLCONSTRUCTOR =
	"    public CIMBeanImpl(CIMOMHandle handle, CIMInstance instance) " +
	"{\n\n\tsuper();\n\tcimomHandle = handle;\n" +
	"\tcimInstance = instance;\n\n    } // constructor\n";
    final static String CIMBEANIMPL_GETCIMOMHANDLE =
	"    public CIMOMHandle getCIMOMHandle() {\n\n" +
	"\treturn (cimomHandle);\n\n    } // getCIMOMHandle\n";
    final static String CIMBEANIMPL_SETCIMOMHANDLE =
	"    public void setCIMOMHandle(CIMOMHandle handle) {\n\n" +
	"\tthis.cimomHandle = handle;\n\n    } // setCIMOMHandle\n";
    final static String CIMBEANIMPL_GETCIMINSTANCE =
	"    public CIMInstance getCIMInstance() {\n\n" +
	"\treturn (cimInstance);\n\n    } // getCIMInstance\n";
    final static String CIMBEANIMPL_SETCIMINSTANCE =
	"    public void setCIMInstance(CIMInstance instance) {\n\n" +
	"\tthis.cimInstance = instance;\n\n    } // setCIMInstance\n";
    final static String CIMBEANIMPL_UPDATE = 
	"    public void update() throws CIMException {\n\n" +
	"\tcimomHandle.setInstance(getObjectPath(), cimInstance);\n\n" +
	"} // update\n";
    final static String CIMBEANIMPL_UPDATE2 = 
	"    public void update(String propName, Object value) " +
	"throws CIMException {\n\n" +
	"\tcimomHandle.setProperty(getObjectPath(), propName, " +
	"new CIMValue(value));\n\n    } // update\n";
    final static String CIMBEANIMPL_DELETE = 
	"    public void delete() throws CIMException {\n\n" +
	"\tcimomHandle.deleteInstance(getObjectPath());\n\n    } // delete\n";
    final static String CIMBEANIMPL_GETPROPERTY = 
	"    protected Object getProperty(String propName) {\n\n" +
	"\ttry {\n\n\t    return " +
	"(cimInstance.getProperty(propName).getValue().getValue());\n\n" +
	"\t} catch (NullPointerException npe) {\n\t}\n" +
	"\treturn ((Object)null);\n\n    } // getProperty\n";
    final static String CIMBEANIMPL_GETARRAYPROPERTY =
	"    protected String[] getArrayProperty(String propName, String[] " +
	"valueArr,\n\tObject[] valueMapArr) {\n\n\tList propList = null;\n\t" +
	"try {\n\n\t    propList =\n\t\t((List)cimInstance.getProperty(" +
	"propName).getValue().getValue());\n\n\t} catch (" +
	"NullPointerException npe) {\n\t}\n\n\tif (propList != null) {\n\n" +
	"\t    String[] returnArr;\n\t    returnArr = new String[" +
	"propList.size()];\n\t    ListIterator listIterator = " +
	"propList.listIterator();\n\t    int counter = 0;\n\t    while (" +
	"listIterator.hasNext()) {\n\n\t\treturnArr[counter] = valueArr[" +
	"getArrayIndex(valueMapArr,\n\t\t    listIterator.next())];\n\t\t" +
	"counter++;\n\n\t    }\n\t    return (returnArr);\n\n\t}\n\t" +
	"return ((String[])null);\n\n    } // getArrayProperty\n";
    final static String CIMBEANIMPL_GETASSOCIATIONPROPERTY =
	"    protected void getAssociationProperty(CIMObjectPath cop, " +
	"CIMBeanImpl\n\tbean) throws CIMException {\n\n" +
	"\tcop.setNameSpace(\"\");\n\tCIMInstance ci = " +
	"cimomHandle.getInstance(cop, false, true, true,\n\t    " +
	"(String[])null);\n\tbean.setCIMInstance(ci);\n\t" +
	"bean.setCIMOMHandle(cimomHandle);\n\n    " +
	"} // getAssociationProperty\n";
    final static String CIMBEANIMPL_SETPROPERTY = 
	"    protected void setProperty(String propName, Object propValue)" +
	"throws\n\tCIMException {\n\n" +
	"\tcimInstance.setProperty(propName, new CIMValue(propValue));\n\n" +
	"    } // setProperty\n";
    final static String CIMBEANIMPL_SETARRAYPROPERTY =
	"    protected void setArrayProperty(String propName, String[] " +
	"valueArr,\n\tObject[] valueMapArr, String[] propValues) throws CIMException {\n\n\t" +
	"Vector vPropValue = new Vector(propValues.length);\n\t" +
	"for (int i = 0; i < propValues.length; i++) {\n\n\t" +
	"    vPropValue.addElement(valueMapArr[getArrayIndex(valueArr," +
	"\n\t\tpropValues[i])]);\n\n\t}\n\tsetProperty(propName, " +
	"vPropValue);\n\n    } // setArrayProperty\n";
    final static String CIMBEANIMPL_GETBEANKEYS = 
	"    public String[] getBeanKeys() {\n\n" +
	"\treturn ((String[])null);\n\n    } // getBeanKeys\n";
    final static String CIMBEANIMPL_GETVERSION = 
	"    public String getBeanVersion() {\n\n" +
	"\treturn (\"-1\");\n\n    } // getBeanVersion\n";
    final static String CIMBEANIMPL_TOSTRING = 
	"    public String toString() {\n\n" +
	"\treturn (cimInstance.toString());\n\n    } // toString\n";
    final static String CIMBEANIMPL_GETOBJECTPATH = 
	"    protected CIMObjectPath getObjectPath() {\n\n" +
	"\tCIMObjectPath cop = new CIMObjectPath(cimInstance.getClassName());" +
	"\n\tVector vKeys = cimInstance.getKeyValuePairs();\n" +
	"\tif ((vKeys != null) && (vKeys.size() > 0)) {\n\n" +
	"\t    cop.setKeys(vKeys);\n\n\t} else {\n\n" +
	"\t    String[] keyArr = getBeanKeys();\n" +
	"\t    if (keyArr != null) {\n\n" +
	"\t\tString keyProperty;\n" +
	"\t\tfor (int i = 0; i < keyArr.length; i++) {\n\n" +
	"\t\t    keyProperty = keyArr[i];\n" +
	"\t\t    cop.addKey(keyProperty,\n" +
	"\t\t\t(cimInstance.getProperty(keyProperty)).getValue());\n\n" +
	"\t\t}\n\n\t    }\n\n\t}\n\treturn (cop);\n\n    } // getObjectPath\n"; 
    final static String CIMBEANIMPL_GETARRAYINDEX = 
	"    protected int getArrayIndex(Object[] objArr, Object obj) {\n\n" +
	"\tList arrList = Arrays.asList(objArr);\n" +
	"\treturn (arrList.indexOf(obj));\n\n    } // getArrayIndex\n";

    // Java Datatype constants
    //
    final static String ARRAY_SUFFIX = "[]";
    final static String INT = "int";

    final static String BOOLEAN = "Boolean";
    final static String BOOLEAN_ARRAY = "Boolean[]";

    final static String CHAR = "Character";
    final static String CHAR_ARRAY = "Character[]";

    final static String DATETIME = "CIMDateTime";

    final static String OBJECT = "Object";
    final static String OBJECT_ARRAY = "Object[]";

    final static String REAL32 = "Float";
    final static String REAL32_ARRAY = "Float[]";

    final static String REAL64 = "Double";
    final static String REAL64_ARRAY = "Double[]";

    final static String SINT8 = "Byte";
    final static String SINT8_ARRAY = "Byte[]";

    final static String SINT16 = "Short";
    final static String SINT16_ARRAY = "Short[]";

    final static String SINT32 = "Integer";
    final static String SINT32_ARRAY = "Integer[]";

    final static String SINT64 = "Long";
    final static String SINT64_ARRAY = "Long[]";

    final static String STRING = "String";
    final static String STRING_ARRAY = "String[]";

    final static String UINT8 = "UnsignedInt8";
    final static String UINT8_ARRAY = "UnsignedInt8[]";

    final static String UINT16 = "UnsignedInt16";
    final static String UINT16_ARRAY = "UnsignedInt16[]";

    final static String UINT32 = "UnsignedInt32";
    final static String UINT32_ARRAY = "UnsignedInt32[]";

    final static String UINT64 = "UnsignedInt64";
    final static String UINT64_ARRAY = "UnsignedInt64[]";

    // CIM Qualifier constants
    //
    final static String DESCRIPTION = "Description";
    final static String UNITS = "Units";
    final static String DISPLAYNAME = "DisplayName";
    final static String VALUEMAP = "ValueMap";
    final static String VALUES = "Values";
    final static String IN = "IN";
    final static String OUT = "OUT";
    final static String ABSTRACT = "Abstract";
    final static String REFERENCE = "REF";
    final static String	OVERRIDE = "Override";
    final static String VERSION = "Version";
    final static String TERMINAL = "Terminal";

    final static String RETURN = "\n";

    final static String JAVADOC_PARAM_TAG = "@param";
    final static String JAVADOC_RETURN_TAG = "@return";
    final static String JAVADOC_EXCEPTION_TAG = "@exception";

    // The following resource's params are as follows:
    // {0} = valid JavaDoc tag (e.g. param, return, exception)
    // {1} = datatype of the JavaDoc element (e.g. String, Boolean)
    // {2} = description of the JavaDoc element
    //
    final static String JAVADOC_ELEMENT = "{0}\t{1}\t{2}";

    final static String JAVADOC_OPEN = "/**\n";
    final static String JAVADOC_BODY = "* ";
    final static String JAVADOC_CLOSE = "*/\n";

    // Generated code constants
    //
    final static String NAMESPACE = "/root/cimv2";
    final static String LOCALHOST = "localhost";
    final static String CIM = "CIM";
    final static String CIMOMHANDLE = "CIMOMHandle";
    final static String CIMINSTANCE = "CIMInstance";
    final static String CIMOBJECTPATH = "CIMObjectPath";
    final static String BEANKEYS = "BeanKeys";
    final static String OUTPUT = "Output";

    // Java source constants
    //
    final static String TAB = "\t";
    final static String SPACES = "    ";
    final static String SPACE = " ";
    final static String QUOTE = "\"";
    final static String COMMA = ", ";
    final static String LINE_COMMENT = "//";
    final static String PLUS = "plus";
    final static String THROWS = " throws ";
    final static String BEAN = "Bean";
    final static String BEANIMPL = "BeanImpl";
    final static String THROWS_CIMEXCEPTION = "throws CIMException";
    final static String CIMEXCEPTION = "CIMException";
    final static String JAVA_FILE = ".java";
    final static String VERSION_FORMAT = "{0}.{1}.{2}";

    final static String PACKAGE_STATEMENT = "package {0};\n";
    final static String IMPORT_STATEMENT = "import {0};\n";
    final static String DEFAULT_IMPORTS = "import javax.wbem.client.*;\n" +
	"import javax.wbem.cim.*;\nimport java.util.*;\n";

    // NOTE: we have to concatenate the open brace on lines of code that
    // use MessageFormat.format() for substitution, as it chokes on the
    // lack of a closing brace.
    //
    final static String OPEN_BRACE = "{\n";
    final static String CLOSE_BRACE = "}";
    final static String INTERFACE_OPEN = "public interface {0}Bean ";
    final static String INTERFACE_CLOSE = " // Interface {0}Bean";
    final static String CHILD_INTERFACE_OPEN = 
	"public interface {0}Bean extends {1}Bean ";
    final static String METHOD_PARAM = "{0} {1}, ";
    final static String OUTPUT_PARAM = "{0} {1}";

    final static String TEST_STRING = "{";
    final static String INTERFACE_ACCESSOR = "public {0} get{1}(){2};";
    final static String INTERFACE_MUTATOR = "public void set{0}({1} {2}){3};";
    final static String INTERFACE_UNITS = "public String get{0}Units(){1};";
    final static String INTERFACE_DISPLAYNAME = 
	"public String get{0}DisplayName(){1};";
    final static String INTERFACE_INVOKEMETHOD = "public {0} {1}({2}){3};";

    final static String CLASS_OPEN = 
	"public class {0}BeanImpl extends {1}BeanImpl implements {0}Bean ";
    final static String CLASS_FINAL_OPEN = 
	"public final class {0}BeanImpl extends {1}BeanImpl implements " +
	"{0}Bean ";
    final static String CLASS_CLOSE = " // Class {0}BeanImpl";

    // Bean class variable code
    //
    final static String VARIABLE_HANDLE = "this.{0} = {0};\n";
    final static String CIMINSTANCE_VARIABLE = 
	"private CIMInstance cimInstance = null;\n";
    final static String CIMOMHANDLE_VARIABLE = 
	"private CIMOMHandle cimomHandle = null;\n";
    final static String KEY_ARRAY_VARIABLE =
	"private final static String[] keysArr = {0};\n";
    final static String VERSION_VARIABLE =
	"private final String versionStr = \"{0}\";\n";
    final static String CLASS_VARIABLE = 
	"private {0} {1} = null;\n";
    final static String VALUEMAP_PREFIX = "VALUEMAP_";
    final static String VALUES_PREFIX = "VALUE_";
    final static String VALUEMAP_CONSTANT = 
	"final static {0} {1} = new {0}(\"{2}\");\n";
    final static String VALUES_CONSTANT = 
	"public final static String {0} = new String(\"{1}\");\n";
    final static String VALUEMAP_CONSTANTS_ARR =
	"private final {0}[] {1} = {2};\n";
    final static String VALUES_CONSTANTS_ARR = 
	"private final String[] {0} = {1};\n";
    final static String VALUEMAP_ARRAY_NAME = "{0}ValueMapArr";
    final static String VALUES_ARRAY_NAME = "{0}ValueArr";
    final static String VALUEMAP_ACCESSOR_BODY =
	"int index = getArrayIndex({0}, ({1})getProperty(\"{2}\"));\n";
    final static String VALUEMAP_ACCESSOR_BODY2 =
	"\tif (index < 0) {\n\n\t    return (null);\n\n\t}\n";
    final static String VALUEMAP_ACCESSOR_RETURN =
	"return ({0}[index]);\n";
    final static String VALUEMAP_ACCESSOR_ARRAYBODY =
	"return (getArrayProperty(\"{0}\", {1}, {2}));\n";
    final static String VALUEMAP_MUTATOR_BODY =
	"int index = getArrayIndex({0}, {1});\n";
    final static String VALUEMAP_MUTATOR_BODY2 = "\tif (index < 0) {\n\n" +
	"\t    throw new IllegalArgumentException();\n\n\t}\n";
    final static String VALUEMAP_MUTATOR_BODY3 =
	"setProperty(\"{0}\", {1}[index]);\n";
    final static String VALUEMAP_MUTATOR_ARRAYBODY =
	"setArrayProperty(\"{0}\", {1}, {2}, {3});\n";

    // Bean constructor code
    //
    final static String CLASS_CONSTRUCTOR = 
	"public {0}BeanImpl(CIMOMHandle handle, CIMInstance instance) ";
    final static String ABSTRACT_CONSTRUCTOR =
	"protected {0}BeanImpl(CIMOMHandle handle, CIMInstance instance) ";
    final static String BEAN_SUPERCLASS = "super(handle, instance);\n";
    final static String SUPERCLASS = "super();\n";
    final static String CIMOM_HANDLE = "this.cimomHandle = handle;\n";
    final static String CIMINSTANCE_HANDLE = "this.cimInstance = instance;\n";
    final static String CONSTRUCTOR_CLOSE = "} // constructor\n";

    // CIMBean.getBeanKeys()
    //
    final static String GETKEYS_METHOD = SPACES +
	"public String[] getBeanKeys() {\n\n\treturn keysArr;\n\n" + SPACES +
	"} // getBeanKeys\n";

    // CIMBean.getBeanVersion()
    //
    final static String GETVERSION_METHOD = SPACES +
	"public String getBeanVersion() {\n\n\treturn versionStr;\n\n" + 
	SPACES + "} // getBeanVersion\n";

    // get<Property/Param>Values() method
    //
    final static String GETVALUES_OPEN = "public String[] get{0}Values()";
    final static String GETVALUES_BODY = "return ({0});\n";
    final static String GETVALUES_CLOSE = " // get{0}Values\n";

    final static String INTERFACE_GETVALUES = "public String[] get{0}Values();";

    // <ClassName>BeanKeys Class
    //
    final static String BEANKEYS_CLASS_OPEN = 
	"public class {0}BeanKeys implements CIMBeanKeys, Serializable ";
    final static String BEANKEYS_CONSTRUCTOR_OPEN = "public {0}BeanKeys({1}) ";
    final static String BEANKEYS_CLASS_CLOSE = " // Class {0}BeanKeys\n";
    final static String BEANKEYS_CIMOBJPATH_OPEN = 
	"public CIMObjectPath getCIMObjectPath() ";
    final static String ACCESSOR_OPEN = "public {0} get{1}() ";
    final static String ACCESSOR_BODY = "return this.{0};\n";

    // Bean property Accessor method code
    //
    final static String CLASS_ACCESSOR_OPEN = "public {0} get{1}(){2} ";
    final static String CLASS_ACCESSOR_BODY = 
	"return ({0})getProperty(\"{1}\");\n";
    final static String CLASS_ACCESSOR_ARRAYBODY =
	"Vector v = (Vector)getProperty(\"{0}\");\n";
    final static String CLASS_ACCESSOR_ARRAYBODY2 = "{0}[] returnArr = null;\n";
    final static String CLASS_ACCESSOR_ARRAYBODY3 = "if (v != null) {\n\n";
    final static String CLASS_ACCESSOR_ARRAYBODY4 = 
	"returnArr = new {0}[v.size()];\n";
    final static String CLASS_ACCESSOR_ARRAYBODY5 = 
	"\t    Enumeration enum = v.elements();\n\t    int counter = 0;\n\t" +
	"    while (enum.hasMoreElements()) {\n\n";
    final static String CLASS_ACCESSOR_ARRAYBODY6 = 
	"returnArr[counter] = ({0})enum.nextElement();\n";
    final static String CLASS_ACCESSOR_ARRAYBODY7 = 
	"\t\tcounter++;\n\n\t    }\n\n\t}\n\treturn (returnArr);\n\n";
    final static String CLASS_ACCESSOR_ASSOCBODY1 =
	"{0}Impl beanImpl = new {0}Impl(null, null);\n";
    final static String CLASS_ACCESSOR_ASSOCBODY2 =
	"try {\n\n";
    final static String CLASS_ACCESSOR_ASSOCBODY3 =
	"getAssociationProperty((CIMObjectPath)getProperty(\"{0}\"), " +
	"beanImpl);\n\n";
    final static String CLASS_ACCESSOR_ASSOCBODY4 =
	"} catch (CIMException cex) {\n\n";
    final static String CLASS_ACCESSOR_ASSOCBODY5 =
	"return (({0})beanImpl);\n\n";
    final static String ACCESSOR_CLOSE = " // get{0}\n";

    // Bean property Mutator method code
    //
    final static String CLASS_MUTATOR_OPEN = "public void set{0}({1} {2}){3} ";
    final static String CLASS_MUTATOR_BODY = 
	"setProperty(\"{0}\", {1});\n";
    final static String CLASS_MUTATOR_ARRAYBODY =
	"Vector v = new Vector({0}.length);\n";
    final static String CLASS_MUTATOR_ARRAYBODY2 =
	"for (int i = 0; i < {0}.length; i++) ";
    final static String CLASS_MUTATOR_ARRAYBODY3 =
	"v.addElement({0}[i]);\n";
    final static String CLASS_MUTATOR_ARRAYBODY4 =
	"setProperty(\"{0}\", v);\n";
    final static String MUTATOR_CLOSE = " // set{0}\n";

    // Bean property Units Accessor method code
    //
    final static String CLASS_UNITS_OPEN = "public String get{0}Units(){1} ";
    final static String CLASS_UNITS_BODY = "return \"{0}\";\n";
    final static String CLASS_UNITS_CLOSE = " // get{0}Units\n";

    // Bean property Display Name Accessor method code
    //
    final static String CLASS_DISPLAYNAME_OPEN = 
	"public String get{0}DisplayName(){1} ";
    final static String CLASS_DISPLAYNAME_BODY = "return \"{0}\";\n";
    final static String CLASS_DISPLAYNAME_CLOSE = " // get{0}DisplayName\n";

    // CIMBeanKeys getCIMObjectPath() implementation
    //
    final static String BEANKEYS_VECTOR_DECL = "Vector vKeys = new Vector()\n;";
    final static String BEANKEYS_VECTOR_ADD = 
	"vKeys.addElement(new CIMProperty(\"{0}\", new CIMValue({1})));\n";
    final static String BEANKEYS_RETURN_DECL = 
	"return (new CIMObjectPath(\"{0}\", vKeys));\n";

    final static String CLASS_INVOKEMETHOD_OPEN = "public {0} {1}({2}){3} ";
    final static String INVOKEMETHOD_CLOSE = " // {0}\n";
    final static String INVOKEMETHOD_INVECTOR_DECL = 
	"CIMArgument[] inParams = new CIMArgument[{0}];\n";
    final static String INVOKEMETHOD_OUTVECTOR_DECL = 
	"CIMArgument[] outParams = new CIMArgument[{0}];\n";
    final static String INVOKEMETHOD_INVECTOR_ADD =
	"inParams[{0}] = new CIMArgument(\"{1}\", new CIMValue({2}));\n";
    final static String INVOKEMETHOD_PARAM_INVECTOR_ADD =
	"{0}[getArrayIndex({1}, {2})]";
    final static String INVOKEMETHOD_INVOKE = 
	"CIMValue cv = cimomHandle.invokeMethod(cimInstance.getObjectPath()," +
	" \"{0}\", inParams, outParams);";
    final static String INVOKEMETHOD_OUTPUT_OPEN =
	"if (outParams != null && outParams.length > 0) ";
    final static String INVOKEMETHOD_OUTPUT_BODY =
	"{0}[0] = ({1})(new {2}({3}));\n";
    final static String INVOKEMETHOD_OUTPUT_PARAM =
	"({0})((CIMArgument)(outParams[{1}])).getValue().getValue(), ";
    final static String INVOKEMETHOD_OUTPUT_ARRAY_PARAM =
	"({0}[])((Vector)(((CIMArgument)(outParams[{1}])).getValue().getValue()))" 
	+ ".toArray(new {0}[0]), ";
    final static String INVOKEMETHOD_RETURN = "return ({0})cv.getValue();\n";

    final static String METHODOUTPUT_CLASS = "{0}_{1}OutputImpl";
    final static String METHODOUTPUT_INTERFACE = "{0}_{1}Output";
    final static String METHODOUTPUT_CLASS_OPEN =  
	"private class {0} implements {1} ";
    final static String METHODOUTPUT_INTERFACE_OPEN =  "public interface {0} ";
    final static String METHODOUTPUT_CONSTRUCTOR_OPEN = 
	"public {0}({1}) ";
    final static String METHODOUTPUT_CLASS_CLOSE = " // Class {0}\n";
    final static String METHODOUTPUT_INTERFACE_CLOSE = " // Interface {0}";
    final static String MUTATOR_OPEN = "public void set{0}({1} {2}) ";

} // BeanGeneratorConstants
