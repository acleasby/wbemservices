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
 *Contributor(s): WBEM Solutions, Inc.
 */

import javax.wbem.cim.*;
import javax.wbem.client.*;
import javax.wbem.provider.*;
import javax.wbem.query.*;

import java.util.*;
import java.io.*;
import java.lang.reflect.Array;

/**
 * @author	Sun Microsystems, Inc.
 * @since      WBEM 1.0
 */
public class SFLProvider implements CIMInstanceProvider, CIMMethodProvider {
    Vector teams;
    private ProviderCIMOMHandle cimomhandle = null;

    /**
     * Called by the CIMOM when the provider is initialized.
     */
    public void initialize(CIMOMHandle cimom) throws CIMException {
	this.cimomhandle = (ProviderCIMOMHandle) cimom;
	teams = new Vector();
	teams.addElement(new SFLTeam("lions", 10, 2));
	teams.addElement(new SFLTeam("tigers", 8, 4));
	teams.addElement(new SFLTeam("bears", 4, 8));
    }

    /**
     * Called by the CIMOM when the provider is removed. Currently the CIMOM
     * does not remove providers, but this method is provided for future
     * versions.
     */
    public void cleanup() 
        throws CIMException {
    }

    /**
     * This method is implemented to retrieve the
     * value of the property specified by the parameters.
     * op Contains the path to the instance whose property must be
     *             retrieved.
     * originClass Contains the name of the class
     *             where this property was originally defined in the hierarchy.
     * propertyName The name of the property.
     * return CIMValue The value of the property.
     */
    public CIMValue getPropertyValue(CIMObjectPath op,
				     String originClass,
				     String propertyName) {
        System.out.println("In getPropertyValue()");
	System.out.println("op = "+op);
	System.out.println("originClass="+originClass);
	System.out.println("propertyName="+propertyName);

	CIMProperty cp = (CIMProperty)(op.getKeys().elementAt(0));
	String name = (String)(cp.getValue().getValue());
	System.out.println("name="+name);

	return new CIMValue("0");
    }

    /**
     * This method is implemented to set the
     * value of the property specified by the parameters.
     * op Contains the path to the instance whose property must be
     *             retrieved.
     * originClass Contains the name of the class
     *             where this property was originally defined in the hierarchy.
     * propertyName The name of the property.
     * cv The value to set the property to.
     */
    public void setPropertyValue(
				 CIMObjectPath op,
				 java.lang.String originClass,
				 java.lang.String propertyName, 
				 CIMValue cv) {
        System.out.println("In setPropertyValue()");
	System.out.println("op = "+op);
	System.out.println("originClass="+originClass);
	System.out.println("propertyName="+propertyName);
	System.out.println("CIMValue="+cv);
	try {
	    CIMProperty cp = (CIMProperty)(op.getKeys().elementAt(0));
	    String name = (String)(cp.getValue().getValue());
	    System.out.println("name="+name);
	} catch (Exception e) {
		e.printStackTrace();
	}

    }

    /**
     * The invokeMethod method implements all instance methods for this
     * MOF class.  The instance is identified by the CIM object path
     * parameter.  The name of the method to execute is specified in the
     * methodName parameter.  Each method implementation must know its
     * input parameters, and pull them off the input parameter Vector.
     * Output parameters are added to the output parameter Vector before
     * returning.
     *
     * op    The CIM object path of the CIM instance for the method
     * methodName  The name of the instance method to execute
     * inParams    Vector of input parameter
     * outParams   Vector of output parameter
     * cc          The CIM class reference
     * return  A vector of OUT parameters in the outParams parameter
     */
    public CIMValue invokeMethod
	(CIMObjectPath op, 
	String methodName,
        CIMArgument[] inParams, 
	CIMArgument[] outParams) throws CIMException {

	System.out.println("-->=======================================");
	System.out.println("In invokeMethod, where:");
	System.out.println("-->\tCIMObjectPath op="+op);
	
	String name = null;

	try {
	    CIMProperty cp = (CIMProperty)(op.getKeys().elementAt(0));
	    name = (String)(cp.getValue().getValue());
	} catch (Exception e) {
	    throw new CIMProviderException
	              (CIMException.CIM_ERR_FAILED, e.toString());
	}
	System.out.println("-->name="+name);

        if (op.getObjectName().equalsIgnoreCase("EX_SFLProvider")) {
            if (methodName.equalsIgnoreCase("setScore")) {
		System.out.println("In setScore SFLProvider.invokemethod()");
		return setScore(inParams, name);
            }
            if (methodName.equalsIgnoreCase("getWins")) {
		System.out.println("In getWins SFLProvider.invokemethod()");
		return getWins(name);
            }
            if (methodName.equalsIgnoreCase("getLosses")) {
		System.out.println("In getLosses SFLProvider.invokemethod()");
		return getLosses(name);
            }
        }
        return null;
    }

    private CIMValue getWins(String name) {
	SFLTeam team;
	for (int i = 0; i < teams.size(); i++) {
 	    team = (SFLTeam)teams.elementAt(i);
	    if (name.equalsIgnoreCase(team.getName()))
		return new CIMValue(new UnsignedInt32(team.getWin()));
	    }
        // Get the property from the persistent instance
	try {
	    CIMInstanceProvider internalProv = cimomhandle.getInternalCIMInstanceProvider();
	    CIMObjectPath cop = new CIMObjectPath(
					      "EX_SFLProvider", "/root/cimv2");
	    cop.addKey("name", new CIMValue(name));
	    CIMClass cClass = cimomhandle.getClass(cop, false, true, true, null);
	    // now get the instance
	    CIMInstance pICI = internalProv.getInstance(cop, false,
							false, false, null,
							cClass);
	    return new CIMValue(
		          pICI.getProperty("win").getValue().getValue());
        } catch (Exception ex) {
        // throw cim error
        // log error
	    // throw new CIMException("CIM_ERR_FAILED");  
        }
	return new CIMValue(new UnsignedInt32("0"));
    }

    private CIMValue getLosses(String name) {
	SFLTeam team;
	for (int i = 0; i < teams.size(); i++) {
 	    team = (SFLTeam)teams.elementAt(i);
	    if (name.equalsIgnoreCase(team.getName()))
		return new CIMValue(new UnsignedInt32(team.getLost()));
	    }
        // Get the property from the persistent instance
	try {
	    CIMInstanceProvider internalProv = cimomhandle.getInternalCIMInstanceProvider();
	    CIMObjectPath cop = new CIMObjectPath(
					    "EX_SFLProvider", "/root/cimv2");
	    cop.addKey("name", new CIMValue(name));
	    CIMClass cClass = cimomhandle.getClass(cop, false, true, true, null);
	    // now get the instance
	    CIMInstance pICI = internalProv.getInstance(cop, false, 
							false, false, null,
							cClass);
	    return new CIMValue(
			  pICI.getProperty("lost").getValue().getValue());
        } catch (Exception ex) {
        // throw cim error
        // log error
	    // throw new CIMException("CIM_ERR_FAILED");  
        }
	return new CIMValue(new UnsignedInt32("0"));
    }

    private void deleteTeam(String name) {
	SFLTeam team;
	for (int i = 0; i < teams.size(); i++) {
 	    team = (SFLTeam)teams.elementAt(i);
	    if (name.equalsIgnoreCase(team.getName())) {
	        teams.removeElementAt(i);
	    }
	}
    }

    private CIMValue setScore(CIMArgument[] in, String name)
                              throws CIMException {
	Vector inParams = new Vector();
	inParams.copyInto(in);
        // System.out.println
        // (">>name="+name+" size()="+inParams.size+" "+inParams);
	SFLTeam team;
	for (int i = 0; i < teams.size(); i++) {
	    team = (SFLTeam)teams.elementAt(i);
	    if (name.equalsIgnoreCase(team.getName())) {
		team.setWin((CIMValue)inParams.elementAt(0));
		team.setLost((CIMValue)inParams.elementAt(1));
		return new CIMValue(new UnsignedInt32(team.getWin()));
	    }
	}
	// get and modify persistent instance
	CIMObjectPath op = new CIMObjectPath("EX_SFLProvider", "/root/cimv2");
	CIMClass cc = cimomhandle.getClass(op, false, true, true, null);
	try {
	    CIMInstanceProvider internalProv = cimomhandle.getInternalCIMInstanceProvider();
	    CIMObjectPath[] copArray = 
			internalProv.enumerateInstanceNames(op, cc);
	    for (int i = 0; i < Array.getLength(copArray); i++) {
	
	        CIMObjectPath xop = copArray[i];
		CIMProperty cProp = (CIMProperty)(xop.getKeys().elementAt(0));
		CIMClass cCl = cimomhandle.getClass(xop, false, true, true, null);
		CIMInstance pICI = cCl.newInstance();
		if (name.equalsIgnoreCase((String)cProp.getValue().getValue())) {
		    pICI.setProperty("name", new CIMValue(name));
		    pICI.setProperty("win", (CIMValue)inParams.elementAt(0));
		    pICI.setProperty("lost", (CIMValue)inParams.elementAt(1));
		    internalProv.setInstance(xop, pICI, true, null);
                    return new CIMValue(pICI.getProperty
					("win").getValue().getValue());
		}
	    }
        } catch (Exception e) {  
	    throw new CIMProviderException(
           			CIMProviderException.GENERAL_EXCEPTION,
				e.getMessage());
        }
	return new CIMValue(new UnsignedInt32("0"));
    }

    /*
    * This enumInstances method returns a vector of data objects
    * of all entries in the teams.
    * op      The CIM class of the class extant to be enumerated
    * deep    If true, all sublcass instances are also enumerated
    * cc      The CIM class reference
    * return   An array of CIMInstance
    */
    public CIMInstance[] enumerateInstances(CIMObjectPath op, 
					    boolean localOnly, 
					    boolean includeQualifiers, 
					    boolean includeClassOrigin, 
					    String[] propList, 
					    CIMClass cc) 
    throws CIMException {
	System.out.println("In enumInstances local, where:");
	System.out.println("-->\tCIMObjectPath op=:  "+op);
        if (op.getObjectName().equalsIgnoreCase("EX_SFLProvider")) {
            Vector instances = new Vector();
	    SFLTeam team;
	    CIMInstance cimI = null;
	    for (int i = 0; i < teams.size(); i++) {
	        team = (SFLTeam)teams.elementAt(i);
	        cimI = cc.newInstance();
		cimI.setProperty("name", new CIMValue
				  (team.getName()));
		cimI.setProperty("win", new CIMValue
				  (new UnsignedInt32(team.getWin())));
		cimI.setProperty("lost", new CIMValue
				  (new UnsignedInt32(team.getLost())));
                instances.addElement(cimI);
	    }
	    CIMInstance[] ciArray  = new CIMInstance[instances.size()];
	    instances.toArray(ciArray);
            return ciArray;
	}
	
        return null;
    }

    /*
     * enumInstances:
     * The entire instances and not just the names are returned.
     * Deep or shallow enumeration is possible, however
     * currently the CIMOM only asks for shallow enumeration.
     */
    public CIMObjectPath[] enumerateInstanceNames(CIMObjectPath op, 
						  CIMClass cc)
    throws CIMException {
        int i;
	System.out.println("In enumInstances, where CIMObjectPath op=:  "+op);
        if (op.getObjectName().equalsIgnoreCase("EX_SFLProvider")) {
            Vector instances = new Vector();
            CIMObjectPath cop;
	    // enumarate teams
	    for (i = 0; i < teams.size(); i++) {
	        cop = new CIMObjectPath(op.getObjectName(), op.getNameSpace());
		System.out.println("-->\tcop="+cop+
		           "\tname="+((SFLTeam)teams.elementAt(i)).getName());
                cop.addKey("name", new CIMValue
			           (((SFLTeam)teams.elementAt(i)).getName()));
                instances.addElement(cop);
	    }
	    // enumerate presistent instances
	    CIMInstanceProvider internalProv = cimomhandle.getInternalCIMInstanceProvider();
       	    CIMObjectPath[] copArray = 
		internalProv.enumerateInstanceNames(op, cc);
	    for (int j = 0; j < Array.getLength(copArray); j++) {
	        CIMObjectPath xop = copArray[j];
	        cop = new CIMObjectPath(
				    xop.getObjectName(), xop.getNameSpace());
		CIMProperty cProp = (CIMProperty)(xop.getKeys().elementAt(0));
                cop.addKey("name", new CIMValue(cProp.getValue().getValue()));
		System.out.println("-->\tcop="+cop);
                instances.addElement(cop);
	    }
	    CIMObjectPath[] retCOPArray = new CIMObjectPath[instances.size()];
	    instances.toArray(retCOPArray);
            return retCOPArray;
	}
        return null;
    }

    /*
    * The getInstance method returns a CIM object instance specified
    * by the key values in the CIM object path.
    * op          The CIM object path of the CIM instance to be returned
    * cc          The CIM class reference
    * localOnly   If true, only non-inherited properties are returned
    * return   A vector of CIM object instances
    */
    public CIMInstance getInstance(CIMObjectPath op, 
				   boolean localOnly, 
				   boolean includeQualifiers, 
				   boolean includeClassOrigin, 
				   String[] propList, 
				   CIMClass cc) 
    throws CIMException {
	String tName = null;
	CIMInstance cimI = null;
	System.out.println("In getInstance, where:");
	System.out.println("-->\tCIMObjectPath op="+op);
	System.out.println("-->\top.getObjectName()="+op.getObjectName());
	System.out.println("-->\top.getNameSpace()="+op.getNameSpace());
	System.out.println("-->\top.getKeys()="+op.getKeys());
	// Get the name of the team
	for (Enumeration e = op.getKeys().elements(); e.hasMoreElements(); ) {
	    CIMProperty cp = (CIMProperty)e.nextElement();
	    if (cp.getName().equalsIgnoreCase("name")) {
		tName = (String)cp.getValue().getValue();
	    }
	}
	cimI = cc.newInstance();
	cimI.setProperty("name", new CIMValue(tName));
	cimI.setProperty("win", (getWins(tName)));
	cimI.setProperty("lost", (getLosses(tName)));
	
	if (localOnly) {
		cimI = cimI.localElements();
	}

        return cimI.filterProperties(propList, 
				     includeQualifiers,
				     includeClassOrigin);
    }

    /**
     * This method must be implemented by instance providers to set
     * the instance specified in the object path. If the instance does
     * not exist, CIMInstanceException with ID CIM_ERR_NOT_FOUND
     * must be thrown. The parameter should be the instance name.
     *   
     * op  The path of the instance to be set. The important part
     *              in this parameter is the namespace component.
     * ci  The instance to be set.
     */
    public void setInstance(CIMObjectPath op, CIMInstance ci, boolean iQ, 
	String[] propList)
                            throws CIMException {
	System.out.println("In setInstance, where ci="+ci);
	System.out.println("In setInstance, where ci.getObjectPath()="+ci.getObjectPath());
	System.out.println
	("In setInstance, where ci.getKeys()="+ci.getKeys());

	System.out.println
	("-->ci.getProperty(name).getValue()=" 
                                           +ci.getProperty("name").getValue());
	System.out.println
	("-->ci.getProperty(name).getValue()=" 
                                +ci.getProperty("name").getValue().getValue());

	String name = (String) ci.getProperty("name").getValue().getValue();

	for (int i = 0; i < teams.size(); i++) {
	    if (name.equalsIgnoreCase(
				((SFLTeam)teams.elementAt(i)).getName())) {
		((SFLTeam)teams.elementAt(i)).setWin((UnsignedInt32)
				ci.getProperty("win").getValue().getValue());
		((SFLTeam)teams.elementAt(i)).setLost((UnsignedInt32) 
				ci.getProperty("lost").getValue().getValue());
		return;
	    }
	}
	// modify persistent instance
        try {
	    CIMInstanceProvider internalProv = cimomhandle.getInternalCIMInstanceProvider();
	    internalProv.setInstance(op, ci, iQ, null);
	    /**
	     * or setup the CIM instance:
	     * CIMObjectPath xop = new CIMObjectPath(
	     * 	                    "EX_SFLProvider", "/root/cimv2");
	     * CIMClass cc = cimomhandle.getClass(xop, false, true, true, null);
	     * CIMInstance pICI = cc.newInstance();
	     * pICI.setProperty("name", new CIMValue(name));
	     * pICI.setProperty("win", new CIMValue(
	     * 	                ci.getProperty("win").getValue().getValue()));
	     * pICI.setProperty("lost", new CIMValue(
	     * 	                ci.getProperty("lost").getValue().getValue()));
	     * internalProv.setInstance(xop, pICI);    
	     */
        } catch (Exception e) {  
	    throw new CIMProviderException(
           			CIMProviderException.GENERAL_EXCEPTION,
				e.getMessage());
        }
    }


    /**
     * This method must be implemented by instance providers to create
     * the instance specified in the object path. If the instance does
     * exist, CIMInstanceException with ID CIM_ERR_ALREADY_EXISTS
     * must be thrown. The parameter should be the instance name.
     *   
     * op  The path of the instance to be set. The important part
     *              in this parameter is the namespace component.
     * ci  The instance to be set.
     * return  CIMObjectPath of the instance that was created.
     */
    public CIMObjectPath createInstance(CIMObjectPath op, CIMInstance ci)
                                        throws CIMException {
	System.out.println("In createInstance, where:");
	System.out.println("-->\tCIMObjectPath op="+op);
	String tName = (String)ci.getProperty("name").getValue().getValue();
	SFLTeam team = new SFLTeam(tName, 0, 0);
	// Use internalprovider to save the new team as persistent object
	try {
	    CIMInstanceProvider internalProv = cimomhandle.getInternalCIMInstanceProvider();
	    CIMObjectPath cop = new CIMObjectPath("EX_SFLProvider",
						  "/root/cimv2");
	    CIMClass cc = cimomhandle.getClass(cop, false, true, true, null);
	    CIMInstance pICI = cc.newInstance();
	    pICI.setProperty("name", new CIMValue(team.getName()));
            pICI.setProperty("win", new CIMValue(
			       new UnsignedInt32(team.getWin())));
	    pICI.setProperty("lost", new CIMValue(
			       new UnsignedInt32(team.getLost())));
	    System.out.println("-->\tCreate persistent object pICI="+pICI);
	    internalProv.createInstance(cop, pICI);    
	} catch (Exception ex) {  
	    throw new CIMProviderException(
                         CIMProviderException.GENERAL_EXCEPTION,
			 ex.getMessage());
	}	  
	return null;
    }

 
    public void createInstance(CIMInstance ci) throws CIMException {
	System.out.println("In createInstance, where ci="+ci);
    }

    /**
     * This method must be implemented by instance providers to delete
     * the instance specified in the object path.
     * ci  The instance to be deleted.
     */
    public void deleteInstance(CIMObjectPath cp)
                               throws CIMException {
	System.out.println("In deleteInstance, where cp="+cp);
	// delete in teams
	CIMProperty cProp = (CIMProperty)(cp.getKeys().elementAt(0));
	String tName = (String)(cProp.getValue().getValue());
	System.out.println("--->\tdelete the team: "+tName);
	deleteTeam(tName);
	// delete in persistent object
	CIMInstanceProvider internalProv = cimomhandle.getInternalCIMInstanceProvider();
	try {
  	    internalProv.deleteInstance(cp);
	} catch (Exception ex) {
	  // throw new CIMException("INTERNALPROV_DELETEINSTANCE_FAILED");
	}  
    }

    /*
     * The execQuery method will support only limited queries based upon
     * partial key matching.  That is, all instances within a management
     * scope can be queried (by specifying only the mgmtDomain key value),
     * and all instances matching an authorization name prefix within a
     * management scope can be queried (by specifying the mgmtDomain key
     * value for the scope and specifying the authorization prefix in the
     * ProfileName key value).  An empty Vector is returned if no entries
     * are selected by the query.
     */
    public CIMInstance[] execQuery(CIMObjectPath op,  
			    	   String query, /* CIM query expression */
                                   String ql, /* CIM query language indicator */
                                   CIMClass cc)  /* CIM class reference */
    throws CIMException {
	System.out.println("In execQuery, where:");
	System.out.println("-->\tCIMObjectPath op="+op);
	System.out.println("-->\tCIM query expression="+query);
	// Initialize the WQL parser
	ByteArrayInputStream in = new ByteArrayInputStream(query.getBytes());
	WQLParser parser = new WQLParser(in);
	// Create a vector to store the result of the query
	Vector result = new Vector();
	try {
	    // Get the select expression from the query
	    // WQL SELECT statement is used to retrieve instances of
	    // a single class.
	    SelectExp q = (SelectExp)parser.querySpecification();
	    // Get the select list from the select expression
	    SelectList attrs = q.getSelectList();
	    // Get the From clause.
	    // The FROM clause identifies the class in which to search for
	    // instances that match the query string.
	    NonJoinExp from = (NonJoinExp)q.getFromClause();
	    // The WHERE clause contains a conditional expression.
	    QueryExp where = q.getWhereClause();
	    // possible to get the attribute values in the query
            String fileName = null;
            try {
                fileName = (String)getAttributeValueInQuery(where, "FileName");
            } catch (Exception e) {
                // this means the fileName is in an NOT clause
                // just let the query mechanism figure it out
                fileName = null;
            }
	    // Use the enumInstances to return a deep enumeration of the class
//	    CIMInstance[] ciArray = enumerateInstances(op, true,
	    CIMInstance[] ciArray = enumerateInstances(op, false,
						       false, false, null, cc);
	    // filtering the instances, matching the query expression
	    // and select list to each instance
	    for (int i = 0; i < Array.getLength(ciArray); i++) {
		if ((where == null) || 
		    (where.apply(ciArray[i]) == true)) {
		    result.addElement(attrs.apply(ciArray[i]));
		} 
	    }
	} catch (Exception e) {
	    throw new CIMException(CIMException.CIM_ERR_FAILED, e.toString());
	}
	// Return the query result
	CIMInstance[] ciArray = new CIMInstance[result.size()];
	result.toArray(ciArray);
        return ciArray;
    }

    private Object getAttributeValueInQuery(QueryExp exp,
                                            String colName) throws Exception {
         
        if (exp instanceof AndQueryExp) {
            QueryExp leftExp = ((AndQueryExp)exp).getLeftExp();
            QueryExp rightExp = ((AndQueryExp)exp).getRightExp();
            Object retStr = getAttributeValueInQuery(leftExp, colName);
            if (retStr != null)
                return retStr;
            retStr = getAttributeValueInQuery(rightExp, colName);
            return retStr;
        }
        
        if (exp instanceof OrQueryExp) {
            QueryExp leftExp = ((OrQueryExp)exp).getLeftExp();
            QueryExp rightExp = ((OrQueryExp)exp).getRightExp();
            Object retStr = getAttributeValueInQuery(leftExp, colName);
            if (retStr != null)
                return retStr;
            retStr = getAttributeValueInQuery(rightExp, colName);
            return retStr;
        }

        if (exp instanceof NotQueryExp) {

            // to handle Negated Expressions, here's the code
 
            QueryExp onlyExp = ((NotQueryExp)exp).getNegatedExp();
            Object retStr = getAttributeValueInQuery(onlyExp, colName);
            if (retStr != null)
                return retStr;
            throw new Exception((String)retStr);
 
            // For now, lets just throw an exception and let the
            // default query handler handle this.
            // throw new Exception();
        }
        if (exp instanceof BinaryRelQueryExp) {
            ValueExp leftVal = ((BinaryRelQueryExp)exp).getLeftValue();
            ValueExp rightVal = ((BinaryRelQueryExp)exp).getRightValue();
            // the attribute value could occur either on the LHS
            // or the RHS of the binarty expression.
            if (leftVal instanceof AttributeExp) {
                String attr = ((AttributeExp)leftVal).getAttributeName();
                if (attr.equalsIgnoreCase(colName)) {
                    if (rightVal instanceof StringValueExp) {
                        return ((StringValueExp)rightVal).getValue();
                    }
                    if (rightVal instanceof DateTimeExp) {
                        return ((DateTimeExp)rightVal).getValue();
                    }
                    return null;
                }
                return null;
            } else if (rightVal instanceof AttributeExp) {
                String attr = ((AttributeExp)rightVal).getAttributeName();
                if (attr.equalsIgnoreCase(colName)) {
                    if (leftVal instanceof StringValueExp) {
                        return ((StringValueExp)leftVal).getValue();
                    }
                    if (leftVal instanceof DateTimeExp) {
                        return ((DateTimeExp)leftVal).getValue();
                    }
                    return null;
                }
                return null;
            }
            return null;
        }
        return null;
    }
 
}

/*
 * 
 */
class SFLTeam {
    String 	name;
    int 	win;
    int    	lost;

    public SFLTeam(String name, int win, int lost) {
        this.name = name;
        this.win = win;
        this.lost = lost;
    }

    public String getName() {
        return this.name;
    }
    public int getWin() {
        return this.win;
    }
    public int getLost() {
        return this.lost;
    }


    public void setWin() {
        this.win++;
    }
    public void setWin(UnsignedInt32 x) {
        this.win = x.intValue();
    }
    public void setWin(CIMValue x) {
        this.win = Integer.parseInt((String)x.getValue());
    }
    public void setWin(int x) {
        this.win = x;
    }
    
    public void setLost() {
        this.lost++;
    }
    public void setLost(UnsignedInt32 x) {
        this.lost = x.intValue();
    }
    public void setLost(CIMValue x) {
        this.lost = Integer.parseInt((String)x.getValue());
    }
    public void setLost(int x) {
        this.lost = x;
    }

}

