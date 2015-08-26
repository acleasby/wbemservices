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
 *are Copyright (c) 2001 Sun Microsystems, Inc.
 *
 *All Rights Reserved.
 *
 *Contributor(s): AppIQ, Inc., WBEM Solutions, Inc.
 */
package org.wbemservices.wbem.cimom.adapters.client.http;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

import javax.wbem.cim.CIMArgument;
import javax.wbem.cim.CIMClass;
import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMInstance;
import javax.wbem.cim.CIMNameSpace;
import javax.wbem.cim.CIMObjectPath;
import javax.wbem.cim.CIMProperty;
import javax.wbem.cim.CIMQualifierType;
import javax.wbem.cim.CIMValue;
import javax.wbem.cimxml.CIMXml;
import javax.wbem.cimxml.CIMXmlFactory;
import javax.wbem.cimxml.NodeTreeWalker;
import javax.wbem.cimxml.XmlResponse;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wbemservices.wbem.cimom.CIMOMServer;
import org.xml.sax.SAXException;


class XmlResponder implements XmlResponse {
    private CIMOMXMLImpl comp;
    private CIMXml xmlInst;

    XmlResponder(CIMOMServer comp) {
	try {
	    this.comp = new CIMOMXMLImpl(comp);
	    this.xmlInst = CIMXmlFactory.getCIMXmlImpl();
	} catch (Exception e) {

	}
    }

    Document getResponse(Document request,
				PasswordAuthentication auth)
	throws IOException, SAXException, ParserConfigurationException {
	return xmlInst.getXMLResponse(this, request, auth);
    }

    void getResponse(Document request,
			PasswordAuthentication auth,
			DataOutputStream out)
    throws IOException, SAXException {
    xmlInst.getXMLResponse(this, request, auth, out);
    }

    public String getSimpleXMLResponse(String methodtype,
				String callname,
				Node node,
				PasswordAuthentication auth,
				String ver)
	throws IOException {
	XmlResponseImpl resp = null;
	try {
	    if (methodtype.equalsIgnoreCase("METHOD")) {
		resp = new InvokeMethod(callname);
	    } else {
		resp = getResponesClass(node, callname);
	    }
	} catch (IOException e) {
	    throw e;
	} catch (Exception e) {
	    //e.printStackTrace();
	}
	return resp.response(node, auth, ver, comp, xmlInst);
    }


    XmlResponseImpl getResponesClass(Node node, String call)
    throws ClassNotFoundException,
	   InstantiationException,
	   IllegalAccessException,
	   MalformedURLException {
	String classname = "";
	NodeTreeWalker walker = new NodeTreeWalker(node);
	for (node = walker.getCurrentNode();
	    node != null;
	    node = walker.nextNode()) {
	    String nodename = node.getNodeName();
	    if (nodename.equals(CIMXml.IPARAMVALUE) ||
		nodename.equals(CIMXml.PARAMVALUE)) {
		if ("ClassName".equals(
				((Element)node).getAttribute("NAME"))) {
                    Node n = getFirstChildElement(node);                
                    if (n != null) {
                        classname = ((Element)n).getAttribute("NAME");
                    }
		}
	    } else if (nodename.equals(CIMXml.INSTANCE) ||
		       nodename.equals(CIMXml.INSTANCENAME)) {
		    classname = ((Element)node).getAttribute("CLASSNAME");
	    }
	}

	if ("__Namespace".equalsIgnoreCase(classname)) {
	    if ("EnumerateInstanceNames".equalsIgnoreCase(call)) {
		call = "EnumerateNamespaceNames";
	    } else if ("CreateInstance".equalsIgnoreCase(call)) {
		call = "CreateNamespace";
	    } else if ("DeleteInstance".equalsIgnoreCase(call)) {
		call = "DeleteNamespace";
	    }
	}
	Class c = Class.forName(
	    "org.wbemservices.wbem.cimom.adapters.client.http.XmlResponder$" + call);
	return (XmlResponseImpl)c.newInstance();
    }

    private static Node getFirstChildElement(Node node) {
	if (node.hasChildNodes()) {
	    NodeList nl = node.getChildNodes();
	    for (int i = 0; i < nl.getLength(); i++) {
		Node item = nl.item(i);
		if (item.getNodeType() == Node.ELEMENT_NODE) {
		    return item;
		}
	    }
	}
	return null;
    }

    abstract static class XmlResponseImpl {
	protected String localNameSpacePath = null;
	protected String role = null;
	protected String resultRole = null;
	protected CIMObjectPath assocClass = null;
	protected CIMObjectPath className = null;
	protected boolean deepInheritance = false;
	protected CIMObjectPath instanceName;
	protected boolean localOnly = true;
	protected CIMClass modifiedClass;
	protected CIMInstance modifiedInstance;
	protected CIMClass newClass;
	protected CIMInstance newInstance;
	protected CIMObjectPath objectName;
	protected CIMValue newValue = null;
	protected String propertyName = null;
	protected CIMObjectPath qualifierName;
	protected CIMQualifierType newQualifierType;
	protected String query = null;
	protected String queryLanguage = null;
	protected CIMObjectPath resultClass = null;


	protected void setUpCall(Node node)
	    throws IOException, CIMException {
	    String nodename = null;
	    NodeTreeWalker walker = new NodeTreeWalker(node);
	    for (node = walker.getCurrentNode();
		node != null;
		node = walker.nextNode()) {
		nodename = node.getNodeName();
		if (nodename.equals(CIMXml.LOCALNAMESPACEPATH)) {
		    setLocalNameSpacePath(node);
		}
		if (nodename.equals(CIMXml.IPARAMVALUE)) {
		    setIParamValue(node);
		}
	    }
	}

	protected void setLocalNameSpacePath(Node node) {
	    NodeTreeWalker walker = new NodeTreeWalker(node);	    
	    String ns = "";
	    for (node = walker.getCurrentNode();
		 node != null;
		 node = walker.getNextElement(CIMXml.NAMESPACE)) {
		String s = ((Element)node).getAttribute("NAME");
		ns = (ns == "" ? s : ns + "/" + s);
	    }

	    // Ok we've consumed the namespace.
	    if (localNameSpacePath == null) {
		// We've not already set the namespace. This is the first one.
		localNameSpacePath = ns;
	    }
	}

	void setIParamValue(Node node) throws CIMException {
	    String paramname = ((Element)node).getAttribute("NAME");
	    Node newValueNode = null;
	    if (paramname.equalsIgnoreCase("AssocClass")) {
                Node subnode = getFirstChildElement(node);
                if (subnode != null) {            
		  this.assocClass = xi.getCIMObjectPath(subnode);
                }
	    } else if (paramname.equalsIgnoreCase("ClassName")) {                
                Node subnode = getFirstChildElement(node);
                if (subnode != null) { 
                    this.className = xi.getCIMObjectPath(subnode);
                }
	    } else if (paramname.equalsIgnoreCase("DeepInheritance")) {
		this.deepInheritance = xi.getBooleanValue(node);
	    } else if (paramname.equalsIgnoreCase("IncludeClassOrigin")) {
		this.includeClassOrigin = xi.getBooleanValue(node);
	    } else if (paramname.equalsIgnoreCase("IncludeQualifiers")) {
		this.includeQualifiers = xi.getBooleanValue(node);
	    } else if (paramname.equalsIgnoreCase("InstanceName")) {
		this.instanceName = xi.getCIMObjectPath(
				getFirstChildElement(node));
	    } else if (paramname.equalsIgnoreCase("LocalOnly")) {
		this.localOnly = xi.getBooleanValue(node);
	    } else if (paramname.equalsIgnoreCase("ModifiedClass")) {
		this.modifiedClass = xi.getCIMClass(
				getFirstChildElement(node));
		this.className = new CIMObjectPath(modifiedClass.getName());
	    } else if (paramname.equalsIgnoreCase("ModifiedInstance")) {
		NodeTreeWalker walker = new NodeTreeWalker(node);
		for (node = walker.getCurrentNode();
		     node != null;
		     node = walker.nextNode()) {
		    String nodename = node.getNodeName();
		    if (nodename.equals(CIMXml.INSTANCENAME)) {
			this.instanceName = xi.getCIMObjectPath(node);
		    }
		    if (nodename.equals(CIMXml.INSTANCE)) {
			this.modifiedInstance = xi.getCIMInstance(node);
		    }
		}
	    } else if (paramname.equalsIgnoreCase("NewClass")) {
		this.newClass = xi.getCIMClass(
                                 getFirstChildElement(node));
		this.className = new CIMObjectPath(newClass.getName());
	    } else if (paramname.equalsIgnoreCase("NewInstance")) {
		this.newInstance = xi.getCIMInstance(
                                 getFirstChildElement(node));
		this.instanceName = newInstance.getObjectPath();
	    } else if (paramname.equalsIgnoreCase("NewValue")) {
		newValueNode = node;
	    } else if (paramname.equalsIgnoreCase("ObjectName")) {
		this.objectName = xi.getCIMObjectPath(
                               getFirstChildElement(node));
	    } else if (paramname.equalsIgnoreCase("PropertyList")) {
		Node this_node = node;
		int i = 0;
		NodeTreeWalker walker = new NodeTreeWalker(node);
		for (node = walker.getNextElement(CIMXml.VALUE);
		     node != null;
		     node = walker.getNextElement(CIMXml.VALUE)) {
		    i++;
		}
		node = this_node;
		this.propertyList = new String[i];
		i = 0;
		walker = new NodeTreeWalker(node);
		for (node = walker.getNextElement(CIMXml.VALUE);
		     node != null;
		     node = walker.getNextElement(CIMXml.VALUE)) {
		    this.propertyList[i++] = xi.getStringValue(node);
		}
	    } else if (paramname.equalsIgnoreCase("PropertyName")) {
		this.propertyName = xi.getStringValue(node);
	    } else if (paramname.equalsIgnoreCase("QualifierName")) {
		this.qualifierName = new CIMObjectPath(
                               xi.getStringValue(node));
	    } else if (paramname.equalsIgnoreCase("QualifierDeclaration")) {
		this.newQualifierType = xi.getCIMQualifierType(
                               getFirstChildElement(node));
	    } else if (paramname.equalsIgnoreCase("Query")) {
		this.query = xi.getStringValue(node);
	    } else if (paramname.equalsIgnoreCase("QueryLanguage")) {
		queryLanguage = xi.getStringValue(node);
		if (queryLanguage == null) {
		    queryLanguage = "";
		}
	    } else if (paramname.equalsIgnoreCase("ResultClass")) {
                Node subnode = getFirstChildElement(node);
                if (subnode != null) { 
                    this.resultClass = xi.getCIMObjectPath(subnode);
                }            
	    } else if (paramname.equalsIgnoreCase("ResultRole")) {
		this.resultRole = xi.getStringValue(node);
	    } else if (paramname.equalsIgnoreCase("Role")) {
		this.role = xi.getStringValue(node);
	    } else {
		throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER,
							paramname);
	    }

	    if (newValueNode != null) {
	        CIMNameSpace ns = new CIMNameSpace(".", localNameSpacePath);
                CIMClass cs = compXml.getClass(ver, ns, instanceName, false,
                    true, false, null, auth);
                CIMProperty cp = cs.getProperty(propertyName);
                String type = cp.getType().toString();
		this.newValue = xi.getCIMValue(newValueNode, type);
	    }

	}



	protected StringBuffer Xml = new StringBuffer();
	protected CIMOMXMLImpl compXml;
	protected CIMXml xi;
	protected String ver;
	protected CIMNameSpace nameSpace;
	protected PasswordAuthentication auth;
	protected boolean showImplied = false;
	protected boolean includeQualifiers = true;
	protected boolean includeClassOrigin = false;
	protected String [] propertyList = null;
	protected boolean showDefault = false;
	protected boolean showHost = false;
	protected boolean showNamespace = false;

	public String response(Node node, PasswordAuthentication auth,
				String ver, CIMOMXMLImpl comp,
				CIMXml xmlInst)
	throws IOException {
 	    this.ver = ver;
	    this.compXml = comp;
	    this.xi = xmlInst;
	    this.auth = auth;
	    this.showImplied = true;
	    try {
		setupDefaultParam();
		setUpCall(node);
		this.nameSpace = new CIMNameSpace(".", localNameSpacePath);
		if (className == null && instanceName != null) {
		    className = new CIMObjectPath(instanceName.getObjectName(),
						instanceName.getNameSpace());
		    className.setHost(instanceName.getHost());
		}

		compXml.hello(ver);
		responseDo();
	    } catch (CIMException e) {
		Xml.append(xi.getError(e, showImplied));
	    }
	    return Xml.toString();
	}
	protected abstract void responseDo() throws CIMException;
	protected void setupDefaultParam() {}
    }

    static class GetClass extends XmlResponseImpl {
	protected void responseDo() throws CIMException {
	    CIMClass c = compXml.getClass(ver, nameSpace, className,
					localOnly,
					includeQualifiers,
					includeClassOrigin,
					propertyList,
					auth);
	    Xml.append(xi.getClassResult(c, includeQualifiers,
			includeClassOrigin, propertyList, showImplied));
	}
    }

    static class EnumerateClasses extends XmlResponseImpl {
	protected void responseDo() throws CIMException {
	    Enumeration ecop = compXml.enumerateClasses(ver, nameSpace,
				 	 className,
				         deepInheritance,
					 localOnly,
					 includeQualifiers,
					 includeClassOrigin,
					 auth);
	    Xml.append(xi.enumerateClassesResult(ecop, includeQualifiers,
			includeClassOrigin, propertyList, showImplied));
	}
    }

    static class EnumerateClassNames extends XmlResponseImpl {
	protected void responseDo() throws CIMException {
	    Enumeration ecop = compXml.enumerateClassNames(ver, nameSpace,
	    	className, deepInheritance, auth);
	    Xml.append(xi.enumerateClassNamesResult(ecop, includeQualifiers,
	    	includeClassOrigin, propertyList, showImplied));
	}
    }

    static class GetInstance extends XmlResponseImpl {
	protected void responseDo() throws CIMException {
	    CIMInstance ci = compXml.getInstance(ver, nameSpace,
						 instanceName,
						 localOnly,
						 includeQualifiers,
						 includeClassOrigin,
						 propertyList,
						 auth);

	    Xml.append(xi.getInstanceResult(ci, includeQualifiers,
			includeClassOrigin, propertyList, showImplied));
	}

	protected void setupDefaultParam() {
	    this.includeQualifiers = false;
	}
    }

    static class EnumerateInstances extends XmlResponseImpl {
	protected void responseDo() throws CIMException {
	    Enumeration ecop = compXml.enumerateInstances(ver, nameSpace,
						className,
						deepInheritance,
						localOnly,
						includeQualifiers,
						includeClassOrigin,
						propertyList,
						auth);
	    Xml.append(xi.enumerateInstancesResult(ecop, includeQualifiers,
			includeClassOrigin, propertyList, showImplied));
	}

	protected void setupDefaultParam() {
	    deepInheritance = true;
	    this.includeQualifiers = false;
	}
    }

    static class EnumerateInstanceNames extends XmlResponseImpl {
	protected void responseDo() throws CIMException {
	    Enumeration ecop = compXml.enumerateInstanceNames(ver, nameSpace, className,
						     auth);
	    Xml.append(xi.enumerateInstanceNamesResult(ecop, includeQualifiers,
			includeClassOrigin, propertyList, showImplied));
	}
    }

    static class GetProperty extends XmlResponseImpl {
	protected void responseDo() throws CIMException {
	    CIMValue cv = compXml.getProperty(ver, nameSpace, instanceName,
					      propertyName, auth);
	    Xml.append(xi.getPropertyResult(cv, includeQualifiers,
			includeClassOrigin, propertyList, showImplied));
	}
    }

    static class SetProperty extends XmlResponseImpl {
	protected void responseDo() throws CIMException {
	    compXml.setProperty(ver, nameSpace, instanceName, propertyName,
				newValue, auth);
	}
    }

    static class CreateClass extends XmlResponseImpl {
	protected void responseDo() throws CIMException {
	    compXml.addCIMElement(ver, nameSpace,
		new CIMObjectPath(""), newClass, auth);
	}
    }

    static class ModifyClass extends XmlResponseImpl {
	protected void responseDo() throws CIMException {
	    compXml.setCIMElement(ver, nameSpace, className,
					modifiedClass, auth);
	}
    }

    static class DeleteClass extends XmlResponseImpl {
	protected void responseDo() throws CIMException {
	    compXml.deleteClass(ver, nameSpace, className, auth);
	}
    }

    static class Associators extends XmlResponseImpl {
	protected void responseDo() throws CIMException {
	    Enumeration e = compXml.associators(ver, nameSpace,
				 objectName,
				 (assocClass != null ?
				  assocClass.getObjectName() :
				  null),
				 (resultClass != null ?
				  resultClass.getObjectName() :
				  null),
				  role,
				  resultRole,
				  includeQualifiers,
				  includeClassOrigin,
				  propertyList,
				  auth);
	    Xml.append(xi.associatorsResult(e, nameSpace, includeQualifiers,
			includeClassOrigin, propertyList, showImplied));
	}
	protected void setupDefaultParam() {
	    this.includeQualifiers = false;
	}
    }

    static class AssociatorNames extends XmlResponseImpl {
	protected void responseDo() throws CIMException {
	    Enumeration e = compXml.associatorNames(ver, nameSpace,
				 objectName,
				 (assocClass != null ?
				  assocClass.getObjectName() :
				  null),
			         (resultClass != null ?
				  resultClass.getObjectName() :
				  null),
				  role,
				  resultRole, auth);
	    Xml.append(xi.associatorNamesResult(e, nameSpace, includeQualifiers,
			includeClassOrigin, propertyList, showImplied));
	}
    }

    static class References extends XmlResponseImpl {
	protected void responseDo() throws CIMException {
	    Enumeration e = compXml.references(ver, nameSpace, objectName,
			         (resultClass != null ?
				  resultClass.getObjectName() :
				  null),
				  role,
				  includeQualifiers,
				  includeClassOrigin,
				  propertyList,
				  auth);
	    Xml.append(xi.referencesResult(e, nameSpace, includeQualifiers,
			includeClassOrigin, propertyList, showImplied));
	}

	protected void setupDefaultParam() {
	   this.includeQualifiers = false;
	}
    }

    static class ReferenceNames extends XmlResponseImpl {
	protected void responseDo() throws CIMException {
  	    Enumeration e = compXml.referenceNames(ver, nameSpace,
				 objectName,
				 (resultClass != null ?
				  resultClass.getObjectName() :
				  null),
  				  role, auth);
	    Xml.append(xi.referenceNamesResult(e, nameSpace, includeQualifiers, 
                includeClassOrigin, propertyList, showImplied));
	}
    }

    static class CreateNamespace extends XmlResponseImpl {
	protected void responseDo() throws CIMException {
	    CIMNameSpace ns = new CIMNameSpace();
	    CIMNameSpace baseNS = new CIMNameSpace("", "");
	    
	    try {
	    ns.setNameSpace(
		(String)
		    newInstance.getProperty("NameSpace").getValue().getValue());
	    } catch (Exception e) {
		throw new CIMException(CIMException.CIM_ERR_FAILED, e);
	    }
	    compXml.createNameSpace(ver, baseNS, ns, auth);
	}
    }

    static class CreateInstance extends XmlResponseImpl {
	protected void responseDo() throws CIMException {
	    CIMObjectPath cop = compXml.addCIMElement(ver, nameSpace,
					instanceName, newInstance, auth);
	    Xml.append(xi.createInstanceResult((cop == null)?instanceName:cop,
			includeQualifiers, includeClassOrigin, propertyList,
			showImplied));
	}
    }

    static class ModifyInstance extends XmlResponseImpl {
	protected void responseDo() throws CIMException {
	    compXml.setCIMElement(ver, nameSpace, instanceName,
			modifiedInstance, includeQualifiers,
			propertyList, auth);
	}
    }

    static class DeleteNamespace extends XmlResponseImpl {
	protected void responseDo() throws CIMException {
	    CIMNameSpace ns = new CIMNameSpace();
	    CIMNameSpace baseNS = new CIMNameSpace("", "");
	    ns.setNameSpace(instanceName.getNameSpace());
	    compXml.deleteNameSpace(ver, baseNS, ns, auth);
	}
    }

    static class DeleteInstance extends XmlResponseImpl {
	protected void responseDo() throws CIMException {
	    compXml.deleteInstance(ver, nameSpace, instanceName, auth);
	}
    }

    static class EnumerateNamespaceNames extends XmlResponseImpl {
	protected void responseDo() throws CIMException {
	    CIMNameSpace baseNS = new CIMNameSpace("", "");
	    Enumeration ecop = compXml.enumNameSpace(ver, baseNS,
					className, true, auth);
	    Xml.append(xi.enumerateNamespaceResult(ecop,
			includeQualifiers, includeClassOrigin,
			propertyList, showImplied));
	}
    }

    static class CreateQualifier extends XmlResponseImpl {
	protected void responseDo() throws CIMException {
	    compXml.addCIMElement(ver, nameSpace,
		new CIMObjectPath(""), newQualifierType, auth);
	}
    }

    static class DeleteQualifier extends XmlResponseImpl {
	protected void responseDo() throws CIMException {
	    compXml.deleteQualifierType(ver, nameSpace, qualifierName, auth);
	}
    }

    static class GetQualifier extends XmlResponseImpl {
	protected void responseDo() throws CIMException {
	    CIMQualifierType qt = compXml.getQualifierType(
				ver, nameSpace, qualifierName, auth);
	    Xml.append(xi.getQualifierResult(qt, includeQualifiers,
			includeClassOrigin, propertyList, showImplied));
	}
    }

    static class EnumerateQualifiers extends XmlResponseImpl {
	protected void responseDo() throws CIMException {
	    Enumeration e = compXml.enumQualifierTypes(ver, nameSpace,
					new CIMObjectPath(""), auth);
	    Xml.append(xi.enumerateQualifiersResult(e, includeQualifiers,
			includeClassOrigin, propertyList, showImplied));
	}
    }

    static class SetQualifier extends XmlResponseImpl {
	protected void responseDo() throws CIMException {
	    compXml.setCIMElement(ver, nameSpace,
		new CIMObjectPath(""), newQualifierType, auth);
	}
    }

    static class ExecQuery extends XmlResponseImpl {
	protected void responseDo() throws CIMException {
	    Enumeration e = compXml.execQuery(ver, nameSpace,
			new CIMObjectPath(), query, queryLanguage, auth);
	    Xml.append(xi.execQueryResult(e, includeQualifiers,
			includeClassOrigin, propertyList, showImplied));
	}
    }

    static class InvokeMethod extends XmlResponseImpl {
	public InvokeMethod(String methodName) {
	    this.methodName = methodName;
	}

	private Vector vIn = new Vector();
	private ArrayList aIn = new ArrayList();
	private String methodName;

	private void setLocalClassPath(Node node) {
	    // do nothing for now
	}

	private void setlInstanceName(Node node) throws CIMException {
	    instanceName = xi.getCIMObjectPath(node);
	}

	private void setClassName(Node node) throws CIMException {
	    className = xi.getCIMObjectPath(node);
	}

	private void setParamValue(Node node) throws CIMException {
            
	    String type = ((Element)node).getAttribute("PARAMTYPE");
	    String name = ((Element)node).getAttribute("NAME");
	    if (name == null ||
		name.equals("string") ||
		name.equals("char16") ||
		name.equals("datetime") ||
		name.equals("boolean") ||
		name.equals("uint8") ||
		name.equals("uint16") ||
		name.equals("sint16") ||
		name.equals("uint32") ||
		name.equals("sint32") ||
		name.equals("uint64") ||
		name.equals("sint64") ||
		name.equals("real32") ||
		name.equals("real64") ||
		name.equals("reference")) {
		// This was a hack: store the param type in name attr
		vIn.add(xi.getCIMValue(node, name));
	    } else {
		// dmtf cr 710.001
		aIn.add(new CIMArgument(name, xi.getCIMValue(node, type)));
	    }
	}

 	protected void setUpCall(Node node) throws IOException, CIMException {
	    String nodename = null;
	    vIn.clear();
	    NodeTreeWalker walker = new NodeTreeWalker(node);
	    for (node = walker.getCurrentNode();
		 node != null;
		 node = walker.nextNode()) {
		nodename = node.getNodeName();
		// BUGFIX. 03/28/02
		// Discard VALUE_REFERENCE node because it was already processed
		// as PARAMVALUE. NOTE: Must call walker.removeCurrent() against
		// INSTANCEPATH element to truly discard the node
		if (nodename.equals(CIMXml.VALUE_REFERENCE)) {
		    node = walker.nextNode();
		    node = walker.nextNode();
		    walker.removeCurrent();
		} else if (nodename.equals(CIMXml.CLASSNAME)) {
		    setClassName(node);
		} else if (nodename.equals(CIMXml.INSTANCENAME)) {
		    setlInstanceName(node);
		} else if (nodename.equals(CIMXml.LOCALCLASSPATH)) {
		    setLocalClassPath(node);
		} else if (nodename.equals(CIMXml.PARAMVALUE)) {
		    setParamValue(node);
		} else if (nodename.equals(CIMXml.LOCALNAMESPACEPATH)) {
		    setLocalNameSpacePath(node);
		}
	    }
	}

	protected void responseDo() throws CIMException {
	    Vector vOut;
	    CIMObjectPath op =
		(instanceName == null) ? className : instanceName;

            CIMArgument[] args = new CIMArgument[aIn.size()];
            aIn.toArray(args);
            vOut  = compXml.invokeMethod(ver, nameSpace, op,
					methodName, args, auth);
	    Xml.append(xi.invokeArgsMethodResult(vOut, includeQualifiers,
			includeClassOrigin, propertyList, showImplied));
	}
    }
}
