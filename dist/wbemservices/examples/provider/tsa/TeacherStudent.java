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
import javax.wbem.provider.*;
import javax.wbem.client.CIMOMHandle;
import javax.wbem.client.ProviderCIMOMHandle;

import java.util.Vector;

/**
 * This provider provides for teachers, students and their associations.
 * It employs a brute force method of recreating everything in each method.
 * It would be much smaller if we stored the instances within a vector or
 * hash map and obtained them from there.
 *
 * @author	Sun Microsystems, Inc.
 */
public class TeacherStudent implements CIMInstanceProvider, CIMMethodProvider,
				Authorizable, CIMAssociatorProvider {

    private static final String studentClass = "Ex_Student";
    private static final String teacherClass = "Ex_Teacher";
    private static final String teacherStudentClass = "Ex_TeacherStudent";

    private ProviderCIMOMHandle cimom;

	
    public void initialize(CIMOMHandle cimom) 
        throws CIMException {
	    this.cimom = (ProviderCIMOMHandle)cimom;
    }

    public void cleanup() 
        throws CIMException {
    }

    /*
     * Dummy for now. Must be completed to return full instances instead
     * of just the instance names. The localOnly flag decides whether
     * inherited properties are required or not.
     */
    public CIMInstance[] enumerateInstances(CIMObjectPath opi,
                                            boolean localOnly,
                                            boolean includeQualifiers,
                                            boolean includeClassOrigin,
                                            String[] propertyList,
                                            CIMClass cc)
    throws CIMException {

	System.out.println("In enumInstances localonly, where:");
	System.out.println("-->\tCIMObjectPath opi=:  "+opi);

	Vector v = new Vector();
	if (localOnly) {
	    cc = cc.localElements();
	}

	if (opi.getObjectName().equalsIgnoreCase(teacherClass)) {
		// Three teachers
		CIMInstance ci = cc.newInstance();
		ci.setProperty("name", new CIMValue("teacher1"));
		v.addElement(ci);
		ci = cc.newInstance();
		ci.setProperty("name", new CIMValue("teacher2"));
		v.addElement(ci);
		ci = cc.newInstance();
		ci.setProperty("name", new CIMValue("teacher3"));
		v.addElement(ci);
	    	CIMInstance[] ciArray = new CIMInstance[v.size()];
	    	v.toArray(ciArray);
	    	return ciArray;
	}

	if (opi.getObjectName().equalsIgnoreCase("studentClass")) {
		// Seven students
		CIMInstance ci = cc.newInstance();
		ci.setProperty("name", new CIMValue("student1"));
		v.addElement(ci);
		ci = cc.newInstance();
		ci.setProperty("name", new CIMValue("student2"));
		v.addElement(ci);
		ci = cc.newInstance();
		ci.setProperty("name", new CIMValue("student3"));
		v.addElement(ci);
		ci = cc.newInstance();
		ci.setProperty("name", new CIMValue("student4"));
		v.addElement(ci);
		ci = cc.newInstance();
		ci.setProperty("name", new CIMValue("student5"));
		v.addElement(ci);
		ci = cc.newInstance();
		ci.setProperty("name", new CIMValue("student6"));
		v.addElement(ci);
		ci = cc.newInstance();
		ci.setProperty("name", new CIMValue("student7"));
		v.addElement(ci);
	    	CIMInstance[] ciArray = new CIMInstance[v.size()];
	    	v.toArray(ciArray);
	    	return ciArray;
	}

	if (opi.getObjectName().equalsIgnoreCase(teacherStudentClass)) {
	    /*
	     * Associations:
	     * student1 and student2 are taught by teacher1
	     * teacher2, 3, 4, 5 and 6 are taught by teacher2
	     * Teacher3 doesnt teach anybody, and 
	     * Stundent7 isnt taught by anybody.
	     */
		CIMInstance ci = cc.newInstance();
		// teaches
		CIMObjectPath op = new CIMObjectPath(teacherClass);
		op.addKey("name", new CIMValue("teacher1"));
		op.setNameSpace(opi.getNameSpace());
		ci.setProperty("teaches", new CIMValue(op));
		// taught by
		op = new CIMObjectPath(studentClass);
		op.addKey("name", new CIMValue("student1"));
		op.setNameSpace(opi.getNameSpace());
		ci.setProperty("taughtby", new CIMValue(op));
		v.addElement(ci);
		
		ci = cc.newInstance();
		// teaches
		op = new CIMObjectPath(teacherClass);
		op.addKey("name", new CIMValue("teacher1"));
		op.setNameSpace(opi.getNameSpace());
		ci.setProperty("teaches", new CIMValue(op));
		// taught by
		op = new CIMObjectPath(studentClass);
		op.addKey("name", new CIMValue("student2"));
		op.setNameSpace(opi.getNameSpace());
		ci.setProperty("taughtby", new CIMValue(op));
		v.addElement(ci);

		ci = cc.newInstance();
		// teaches
		op = new CIMObjectPath(teacherClass);
		op.addKey("name", new CIMValue("teacher2"));
		op.setNameSpace(opi.getNameSpace());
		ci.setProperty("teaches", new CIMValue(op));
		// taught by
		op = new CIMObjectPath(studentClass);
		op.addKey("name", new CIMValue("student3"));
		op.setNameSpace(opi.getNameSpace());
		ci.setProperty("taughtby", new CIMValue(op));
		v.addElement(ci);

		ci = cc.newInstance();
		// teaches
		op = new CIMObjectPath(teacherClass);
		op.addKey("name", new CIMValue("teacher2"));
		op.setNameSpace(opi.getNameSpace());
		ci.setProperty("teaches", new CIMValue(op));
		// taught by
		op = new CIMObjectPath(studentClass);
		op.addKey("name", new CIMValue("student4"));
		op.setNameSpace(opi.getNameSpace());
		ci.setProperty("taughtby", new CIMValue(op));
		v.addElement(ci);

		ci = cc.newInstance();
		// teaches
		op = new CIMObjectPath(teacherClass);
		op.addKey("name", new CIMValue("teacher2"));
		op.setNameSpace(opi.getNameSpace());
		ci.setProperty("teaches", new CIMValue(op));
		// taught by
		op = new CIMObjectPath(studentClass);
		op.addKey("name", new CIMValue("student5"));
		op.setNameSpace(opi.getNameSpace());
		ci.setProperty("taughtby", new CIMValue(op));
		v.addElement(ci);

		ci = cc.newInstance();
		// teaches
		op = new CIMObjectPath(teacherClass);
		op.addKey("name", new CIMValue("teacher2"));
		op.setNameSpace(opi.getNameSpace());
		ci.setProperty("teaches", new CIMValue(op));
		// taught by
		op = new CIMObjectPath(studentClass);
		op.addKey("name", new CIMValue("student6"));
		op.setNameSpace(opi.getNameSpace());
		ci.setProperty("taughtby", new CIMValue(op));
		v.addElement(ci);

	    	CIMInstance[] ciArray = new CIMInstance[v.size()];
	    	v.toArray(ciArray);
	    	return ciArray;
	}

	    // Some class we do not support.
	    throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED, opi);
    }

    public CIMObjectPath[] enumerateInstanceNames(CIMObjectPath opi, 
					 	  CIMClass cc) 
    throws CIMException {
	System.out.println("In enumInstances, where:");
	System.out.println("-->\tCIMObjectPath opi=:  "+opi);

	Vector v = new Vector();

	if (opi.getObjectName().equalsIgnoreCase(teacherClass)) {
	    // teachers
	    CIMObjectPath op = new CIMObjectPath(teacherClass);
	    op.addKey("name", new CIMValue("teacher1"));
	    op.setNameSpace(opi.getNameSpace());
	    v.addElement(op);

	    op = new CIMObjectPath(teacherClass);
	    op.addKey("name", new CIMValue("teacher2"));
	    op.setNameSpace(opi.getNameSpace());
	    v.addElement(op);

	    op = new CIMObjectPath(teacherClass);
	    op.addKey("name", new CIMValue("teacher3"));
	    op.setNameSpace(opi.getNameSpace());
	    v.addElement(op);

	    CIMObjectPath[] copArray = new CIMObjectPath[v.size()];
	    v.toArray(copArray);
	    return copArray;
	}

	if (opi.getObjectName().equalsIgnoreCase(studentClass)) {
	    // students
	    CIMObjectPath op = new CIMObjectPath(studentClass);
	    op.setNameSpace(opi.getNameSpace());
	    op.addKey("name", new CIMValue("student1"));
	    v.addElement(op);

	    op = new CIMObjectPath(studentClass);
	    op.setNameSpace(opi.getNameSpace());
	    op.addKey("name", new CIMValue("student2"));
	    v.addElement(op);

	    op = new CIMObjectPath(studentClass);
	    op.setNameSpace(opi.getNameSpace());
	    op.addKey("name", new CIMValue("student3"));
	    v.addElement(op);

	    op = new CIMObjectPath(studentClass);
	    op.setNameSpace(opi.getNameSpace());
	    op.addKey("name", new CIMValue("student4"));
	    v.addElement(op);

	    op = new CIMObjectPath(studentClass);
	    op.setNameSpace(opi.getNameSpace());
	    op.addKey("name", new CIMValue("student5"));
	    v.addElement(op);

	    op = new CIMObjectPath(studentClass);
	    op.setNameSpace(opi.getNameSpace());
	    op.addKey("name", new CIMValue("student6"));
	    v.addElement(op);

	    op = new CIMObjectPath(studentClass);
	    op.setNameSpace(opi.getNameSpace());
	    op.addKey("name", new CIMValue("student7"));
	    v.addElement(op);

	    CIMObjectPath[] copArray = new CIMObjectPath[v.size()];
	    v.toArray(copArray);
	    return copArray;
	}

	if (opi.getObjectName().equalsIgnoreCase(teacherStudentClass)) {
	    /*
	     * Associations:
	     * student1 and student2 are taught by teacher1
	     * teacher2, 3, 4, 5 and 6 are taught by teacher2
	     * Teacher3 doesnt teach anybody, and 
	     * Stundent7 isnt taught by anybody.
	     */
		CIMObjectPath op = new CIMObjectPath(teacherStudentClass);
		op.setNameSpace(opi.getNameSpace());
		// teaches
		CIMObjectPath opr = new CIMObjectPath(teacherClass);
		opr.setNameSpace(opi.getNameSpace());
		opr.addKey("name", new CIMValue("teacher1"));
		op.addKey("teaches", new CIMValue(opr));
		// taught by
		opr = new CIMObjectPath(studentClass);
		opr.setNameSpace(opi.getNameSpace());
		opr.addKey("name", new CIMValue("student1"));
		op.addKey("taughtby", new CIMValue(opr));
		v.addElement(op);
		
		op = new CIMObjectPath(teacherStudentClass);
		op.setNameSpace(opi.getNameSpace());
		// teaches
		opr = new CIMObjectPath(teacherClass);
		opr.setNameSpace(opi.getNameSpace());
		opr.addKey("name", new CIMValue("teacher1"));
		op.addKey("teaches", new CIMValue(opr));
		// taught by
		opr = new CIMObjectPath(studentClass);
		opr.setNameSpace(opi.getNameSpace());
		opr.addKey("name", new CIMValue("student2"));
		op.addKey("taughtby", new CIMValue(opr));
		v.addElement(op);
		
		op = new CIMObjectPath(teacherStudentClass);
		op.setNameSpace(opi.getNameSpace());
		// teaches
		opr = new CIMObjectPath(teacherClass);
		opr.setNameSpace(opi.getNameSpace());
		opr.addKey("name", new CIMValue("teacher2"));
		op.addKey("teaches", new CIMValue(opr));
		// taught by
		opr = new CIMObjectPath(studentClass);
		opr.setNameSpace(opi.getNameSpace());
		opr.addKey("name", new CIMValue("student3"));
		op.addKey("taughtby", new CIMValue(opr));
		v.addElement(op);
		
		op = new CIMObjectPath(teacherStudentClass);
		op.setNameSpace(opi.getNameSpace());
		// teaches
		opr = new CIMObjectPath(teacherClass);
		opr.setNameSpace(opi.getNameSpace());
		opr.addKey("name", new CIMValue("teacher2"));
		op.addKey("teaches", new CIMValue(opr));
		// taught by
		opr = new CIMObjectPath(studentClass);
		opr.setNameSpace(opi.getNameSpace());
		opr.addKey("name", new CIMValue("student4"));
		op.addKey("taughtby", new CIMValue(opr));
		v.addElement(op);
		
		op = new CIMObjectPath(teacherStudentClass);
		op.setNameSpace(opi.getNameSpace());
		// teaches
		opr = new CIMObjectPath(teacherClass);
		opr.setNameSpace(opi.getNameSpace());
		opr.addKey("name", new CIMValue("teacher2"));
		op.addKey("teaches", new CIMValue(opr));
		// taught by
		opr = new CIMObjectPath(studentClass);
		opr.setNameSpace(opi.getNameSpace());
		opr.addKey("name", new CIMValue("student5"));
		op.addKey("taughtby", new CIMValue(opr));
		v.addElement(op);
		
		op = new CIMObjectPath(teacherStudentClass);
		op.setNameSpace(opi.getNameSpace());
		// teaches
		opr = new CIMObjectPath(teacherClass);
		opr.setNameSpace(opi.getNameSpace());
		opr.addKey("name", new CIMValue("teacher2"));
		op.addKey("teaches", new CIMValue(opr));
		// taught by
		opr = new CIMObjectPath(studentClass);
		opr.setNameSpace(opi.getNameSpace());
		opr.addKey("name", new CIMValue("student6"));
		op.addKey("taughtby", new CIMValue(opr));
		v.addElement(op);
		
	    	CIMObjectPath[] copArray = new CIMObjectPath[v.size()];
	    	v.toArray(copArray);
	    	return copArray;
	    }

	    // Some class we do not support.
	    throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED, opi);
    }

    public CIMInstance getInstance(CIMObjectPath op, 
                                   boolean localOnly,
                                   boolean includeQualifiers,
                                   boolean includeClassOrigin,
                                   String[] propertyList,
				   CIMClass cc) 
    throws CIMException {
	System.out.println("In getInstance, where:");
	System.out.println("-->\tCIMObjectPath op="+op);
	System.out.println("-->\top.getObjectName()="+op.getObjectName());
	System.out.println("-->\top.getNameSpace()="+op.getNameSpace());
	System.out.println("-->\top.getKeys()="+op.getKeys());
	    
	if (op.getObjectName().equalsIgnoreCase(teacherClass)) {
		// Wont bother to verify if the instance actually is present
		CIMInstance ci = cc.newInstance();
		CIMProperty cp = (CIMProperty)op.getKeys().elementAt(0);
		ci.setProperty("name", cp.getValue());
		if (localOnly) {
		    ci = ci.localElements();
		}
		return ci.filterProperties(propertyList, 
					   includeQualifiers,
					   includeClassOrigin);
	}

	if (op.getObjectName().equalsIgnoreCase(studentClass)) {
		// Wont bother to verify if the instance actually is present
		CIMInstance ci = cc.newInstance();
		CIMProperty cp = (CIMProperty)op.getKeys().elementAt(0);
		ci.setProperty("name", cp.getValue());
		if (localOnly) {
		    ci = ci.localElements();
		}
		return ci.filterProperties(propertyList, 
					   includeQualifiers,
					   includeClassOrigin);
	}

	if (op.getObjectName().equalsIgnoreCase(teacherStudentClass)) {
		// Wont bother to verify if the instance actually is present
		CIMInstance ci = cc.newInstance();
		CIMProperty cp = (CIMProperty)op.getKeys().elementAt(0);
		ci.setProperty(cp.getName(), cp.getValue());
		cp = (CIMProperty)op.getKeys().elementAt(1);
		ci.setProperty(cp.getName(), cp.getValue());
		if (localOnly) {
		    ci = ci.localElements();
		}
		return ci.filterProperties(propertyList, 
					   includeQualifiers,
					   includeClassOrigin);
	}

	throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED, op);
    }


    public CIMValue invokeMethod(CIMObjectPath op, String methodName, 
	CIMArgument[] inParams, CIMArgument[] outParams) throws CIMException {
	System.out.println("In invokeMethod, where:");
	System.out.println("-->\tCIMObjectPath op="+op);
	return null;
    }

    public CIMObjectPath createInstance(CIMObjectPath op, CIMInstance ci) 
                                                      throws CIMException {
	System.out.println("In createInstance, where:");
	System.out.println("-->\tCIMObjectPath op="+op);

	throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED, op);
    }

    public void setInstance(CIMObjectPath op, CIMInstance ci,
	boolean includeQualifiers, String[] propList) 
                                              throws CIMException {
	System.out.println("In setInstance, where:");
	System.out.println("-->\tCIMObjectPath op="+op);

	throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED, op);
    }

    public void deleteInstance(CIMObjectPath op) throws CIMException {
	System.out.println("In deleteInstance, where:");
	System.out.println("-->\tCIMObjectPath op="+op);

	throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED, op);
    }

    public CIMInstance[] execQuery(CIMObjectPath op, 
			    String query, 
			    String ql, 
			    CIMClass cc) 
		   throws CIMException {
	System.out.println("In execQuery, where:");
	System.out.println("-->\tCIMObjectPath op="+op);

	throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED, op);
    }

    public CIMInstance[] associators(CIMObjectPath assocName,
			      CIMObjectPath objectName,
			      String resultClass,
			      String role,
			      String resultRole,
			      boolean includeQualifiers,
			      boolean includeClassOrigin,
			      String propertyList[]) throws CIMException {
	System.out.println("In associators, where:");
	System.out.println("-->\tCIMObjectPath assocName="+assocName);
	System.out.println("-->\tCIMObjectPath objectName="+objectName);
	System.out.println("-->\trole="+role);
	System.out.println("-->\resultRole="+resultRole);

	if (objectName.getObjectName().equalsIgnoreCase(teacherClass)) {
	    Vector v = new Vector();

	    if ((role != null) && (!role.equalsIgnoreCase("teaches"))) {
	        // Teachers only play the teaches role.
	        return (CIMInstance[])v.toArray();
	    }

	    // Ok we need to get associators of a teacher
	    CIMProperty nameProp = 
	              (CIMProperty)objectName.getKeys().elementAt(0);
	    String name = (String)nameProp.getValue().getValue();

	    // Get the student class
	    CIMObjectPath tempOp = new CIMObjectPath(studentClass);
	    tempOp.setNameSpace(assocName.getNameSpace());
	    /*
	     * More efficient if you apply the filter properties here,
	     * and then take care of setting the appropriate properties
	     * further down. Not doing it now for convenience.
	     */
	    CIMClass cc = cimom.getClass(tempOp, false, 
					 includeQualifiers, 
					 includeClassOrigin, 
					 propertyList);

	    if (name.equals("teacher1")) {
	        // We need students for teacher1
	        CIMInstance ci = cc.newInstance();
		ci.setProperty("name", new CIMValue("student1"));
		v.addElement(ci.filterProperties(propertyList,
				   includeQualifiers, includeClassOrigin));
		ci = cc.newInstance();
		ci.setProperty("name", new CIMValue("student2"));
		v.addElement(ci.filterProperties(propertyList,
				   includeQualifiers, includeClassOrigin));
	        return (CIMInstance[])v.toArray();
	    }

	    if (name.equals("teacher2")) {
	        // We need students for teacher2
	        CIMInstance ci = cc.newInstance();
		ci.setProperty("name", new CIMValue("student3"));
		v.addElement(ci.filterProperties(propertyList,
				     includeQualifiers, includeClassOrigin));
		ci = cc.newInstance();
		ci.setProperty("name", new CIMValue("student4"));
		v.addElement(ci.filterProperties(propertyList,
				     includeQualifiers, includeClassOrigin));
		ci = cc.newInstance();
		ci.setProperty("name", new CIMValue("student5"));
		v.addElement(ci.filterProperties(propertyList,
				     includeQualifiers, includeClassOrigin));
		ci = cc.newInstance();
		ci.setProperty("name", new CIMValue("student6"));
		v.addElement(ci.filterProperties(propertyList,
				     includeQualifiers, includeClassOrigin));
	        return (CIMInstance[])v.toArray();
	    }

	    if (name.equals("teacher3")) {
	        // Teacher3 teaches nobody
	        return (CIMInstance[])v.toArray();
	    }

	    // Huh? We dont know this teacher
	    throw new CIMInstanceException(CIMException.CIM_ERR_NOT_FOUND, 
		objectName);
	}

	if (objectName.getObjectName().equalsIgnoreCase(studentClass)) {
	    Vector v = new Vector();

	    if ((role != null)  &&
		    (!role.equalsIgnoreCase("taughtby"))) {
	        // Students only play the taughtby role.
	        return (CIMInstance[])v.toArray();
	    }

	    // Ok we need to get associators of a student
	    CIMProperty nameProp = 
		(CIMProperty)objectName.getKeys().elementAt(0);
	    String name = (String)nameProp.getValue().getValue();

	    // Get the teacher class
	    CIMObjectPath tempOp = new CIMObjectPath(teacherClass);
	    tempOp.setNameSpace(assocName.getNameSpace());
	    /*
	     * More efficient if you apply the filter properties here,
	     * and then take care of setting the appropriate properties
	     * further down. Not doing it now for convenience.
	     */
	    CIMClass cc = cimom.getClass(tempOp, false, 
					 includeQualifiers,
					 includeClassOrigin,
					 propertyList);

	    if (name.equals("student1")) {
	        // We need the teacher for student1
	        CIMInstance ci = cc.newInstance();
		ci.setProperty("name", new CIMValue("teacher1"));
		v.addElement(ci.filterProperties(propertyList,
			includeQualifiers, includeClassOrigin));
	        return (CIMInstance[])v.toArray();
	    }

	    if (name.equals("student2")) {
	        // We need the teacher for student2
	        CIMInstance ci = cc.newInstance();
		ci.setProperty("name", new CIMValue("teacher1"));
		v.addElement(ci.filterProperties(propertyList,
			includeQualifiers, includeClassOrigin));
	        return (CIMInstance[])v.toArray();
	    }

	    if (name.equals("student3")) {
	        // We need the teacher for student1
	        CIMInstance ci = cc.newInstance();
		ci.setProperty("name", new CIMValue("teacher2"));
		v.addElement(ci.filterProperties(propertyList,
			includeQualifiers, includeClassOrigin));
	        return (CIMInstance[])v.toArray();
	    }

	    if (name.equals("student4")) {
	        // We need the teacher for student1
	        CIMInstance ci = cc.newInstance();
		ci.setProperty("name", new CIMValue("teacher2"));
		v.addElement(ci.filterProperties(propertyList,
			includeQualifiers, includeClassOrigin));
	        return (CIMInstance[])v.toArray();
	    }

	    if (name.equals("student5")) {
	        // We need the teacher for student1
	        CIMInstance ci = cc.newInstance();
		ci.setProperty("name", new CIMValue("teacher2"));
		v.addElement(ci.filterProperties(propertyList,
			includeQualifiers, includeClassOrigin));
	        return (CIMInstance[])v.toArray();
	    }

	    if (name.equals("student6")) {
	        // We need the teacher for student1
	        CIMInstance ci = cc.newInstance();
		ci.setProperty("name", new CIMValue("teacher2"));
		v.addElement(ci.filterProperties(propertyList,
			includeQualifiers, includeClassOrigin));
	        return (CIMInstance[])v.toArray();
	    }

	    if (name.equals("student7")) {
	        // stundent7 has no teacher
	        return (CIMInstance[])v.toArray();
	    }

	    // Huh? We dont know this student
	    throw new CIMInstanceException(CIMException.CIM_ERR_NOT_FOUND, 
					   objectName);
	}
	// If object path is well formed, we shouldnt come here
	throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED, 
			       objectName);
    }

    public CIMObjectPath[] associatorNames(CIMObjectPath assocName,
				CIMObjectPath objectName,
			  	String resultClass,
				String role,
				String resultRole) throws CIMException {
	System.out.println("In associatorNames, where:");
	System.out.println("-->\tCIMObjectPath assocName="+assocName);
	System.out.println("-->\tCIMObjectPath objectName="+objectName);
	System.out.println("-->\trole="+role);
	System.out.println("-->\resultRole="+resultRole);

	if (objectName.getObjectName().equalsIgnoreCase(teacherClass)) {
	    Vector v = new Vector();

	    if ((role != null) && (!role.equalsIgnoreCase("teaches"))) {
	        // Teachers only play the teaches role.
	        return null;
	    }

	    // Ok we need to get associators of a teacher
	    CIMProperty nameProp = 
		(CIMProperty)objectName.getKeys().elementAt(0);
	    String name = (String)nameProp.getValue().getValue();

	    if (name.equals("teacher1")) {
	        // We need student names for teacher1
	        CIMObjectPath op = new CIMObjectPath(studentClass);
		op.setNameSpace(assocName.getNameSpace());
		op.addKey("name", new CIMValue("student1"));
		v.addElement(op);
		op = new CIMObjectPath(studentClass);
		op.setNameSpace(assocName.getNameSpace());
		op.addKey("name", new CIMValue("student2"));
		v.addElement(op);

		CIMObjectPath[] copArray = new CIMObjectPath[v.size()];
		v.toArray(copArray);
		return copArray;
	    }

	    if (name.equals("teacher2")) {
	        // We need student names for teacher2
	        CIMObjectPath op = new CIMObjectPath(studentClass);
		op.setNameSpace(assocName.getNameSpace());
		op.addKey("name", new CIMValue("student3"));
		v.addElement(op);
		op = new CIMObjectPath(studentClass);
		op.setNameSpace(assocName.getNameSpace());
		op.addKey("name", new CIMValue("student4"));
		v.addElement(op);
		op = new CIMObjectPath(studentClass);
		op.setNameSpace(assocName.getNameSpace());
		op.addKey("name", new CIMValue("student5"));
		v.addElement(op);
		op = new CIMObjectPath(studentClass);
		op.setNameSpace(assocName.getNameSpace());
		op.addKey("name", new CIMValue("student6"));
		v.addElement(op);

		CIMObjectPath[] copArray = new CIMObjectPath[v.size()];
		v.toArray(copArray);
		return copArray;
	    }

	    if (name.equals("teacher3")) {
	        // We need students for teacher3
	        // Teacher3 teaches nobody
	        return null;
	    }

	    // Huh? We dont know this teacher
	    throw new CIMInstanceException(CIMException.CIM_ERR_NOT_FOUND, 
					   objectName);
	}

	if (objectName.getObjectName().equalsIgnoreCase(studentClass)) {
	    Vector v = new Vector();

	    if ((role != null) && (!role.equalsIgnoreCase("taughtby"))) {
	        // Students only play the taughtby role.
	        return null;
	    }

	    // Ok we need to get associators of a student
	    CIMProperty nameProp = 
		(CIMProperty)objectName.getKeys().elementAt(0);
	    String name = (String)nameProp.getValue().getValue();

	    if (name.equals("student1")) {
	        // We need the teacher name for student1
	        CIMObjectPath op = new CIMObjectPath(teacherClass);
		op.addKey("name", new CIMValue("teacher1"));
		op.setNameSpace(assocName.getNameSpace());
		v.addElement(op);
		CIMObjectPath[] copArray = new CIMObjectPath[v.size()];
		v.toArray(copArray);
		return copArray;
	    }

	    if (name.equals("student2")) {
	        // We need the teacher name for student2
	        CIMObjectPath op = new CIMObjectPath(teacherClass);
		op.addKey("name", new CIMValue("teacher1"));
		op.setNameSpace(assocName.getNameSpace());
		CIMObjectPath[] copArray = new CIMObjectPath[v.size()];
		v.toArray(copArray);
		return copArray;
	    }

	    if (name.equals("student3")) {
	        // We need the teacher name for student3
	        CIMObjectPath op = new CIMObjectPath(teacherClass);
		op.addKey("name", new CIMValue("teacher2"));
		op.setNameSpace(assocName.getNameSpace());
		v.addElement(op);
		CIMObjectPath[] copArray = new CIMObjectPath[v.size()];
		v.toArray(copArray);
		return copArray;
	    }

	    if (name.equals("student4")) {
	        // We need the teacher name for student4
	        CIMObjectPath op = new CIMObjectPath(teacherClass);
		op.addKey("name", new CIMValue("teacher2"));
		op.setNameSpace(assocName.getNameSpace());
		v.addElement(op);
		CIMObjectPath[] copArray = new CIMObjectPath[v.size()];
		v.toArray(copArray);
		return copArray;
	    }

	    if (name.equals("student5")) {
	        // We need the teacher name for student5
	        CIMObjectPath op = new CIMObjectPath(teacherClass);
		op.addKey("name", new CIMValue("teacher2"));
		op.setNameSpace(assocName.getNameSpace());
		v.addElement(op);
		CIMObjectPath[] copArray = new CIMObjectPath[v.size()];
		v.toArray(copArray);
		return copArray;
	    }

	    if (name.equals("student6")) {
	        // We need the teacher name for student6
	        CIMObjectPath op = new CIMObjectPath(teacherClass);
		op.addKey("name", new CIMValue("teacher2"));
		op.setNameSpace(assocName.getNameSpace());
		v.addElement(op);
		CIMObjectPath[] copArray = new CIMObjectPath[v.size()];
		v.toArray(copArray);
		return copArray;
	    }

	    if (name.equals("student7")) {
	        // stundent7 has no teacher
	        return null;
	    }

	    // Huh? We dont know this student
	    throw new CIMInstanceException(CIMException.CIM_ERR_NOT_FOUND, 
					   objectName);
	}
	// If object path is well formed, we shouldnt come here
	throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED, 
			       objectName);
    }

    public CIMInstance[] references(CIMObjectPath assocName,
				CIMObjectPath objectName,
				String role,
				boolean includeQualifiers,
				boolean includeClassOrigin,
				String propertyList[]) throws CIMException {
	System.out.println("In references, where:");
	System.out.println("-->\tCIMObjectPath assocName="+assocName);
	System.out.println("-->\tCIMObjectPath objectName="+objectName);
	System.out.println("-->\trole="+role);

	if (objectName.getObjectName().equalsIgnoreCase(teacherClass)) {
	    Vector v = new Vector();

	    if ((role != null) && (!role.equalsIgnoreCase("teaches"))) {
	        // Teachers only play the teaches role.
	        return (CIMInstance[])v.toArray();
	    }

	    // Ok we need to get associations to a teacher
	    CIMProperty nameProp = 
		(CIMProperty)objectName.getKeys().elementAt(0);
	    String name = (String)nameProp.getValue().getValue();

	    /*
	     * Get the association class
	     * More efficient if you apply the filter properties here,
	     * and then take care of setting the appropriate properties
	     * further down. Not doing it now for convenience.
	     */
	    CIMClass cc = cimom.getClass(assocName, false,
					 includeQualifiers,
					 includeClassOrigin,
					 propertyList);

	    if (name.equals("teacher1")) {
	        // We need associations for teacher1
	        CIMInstance ci = cc.newInstance();
		// teaches
		CIMObjectPath op = new CIMObjectPath(teacherClass);
		op.addKey("name", new CIMValue("teacher1"));
		op.setNameSpace(assocName.getNameSpace());
		ci.setProperty("teaches", new CIMValue(op));
		// taught by
		op = new CIMObjectPath(studentClass);
		op.addKey("name", new CIMValue("student1"));
		op.setNameSpace(assocName.getNameSpace());
		ci.setProperty("taughtby", new CIMValue(op));
		v.addElement(ci.filterProperties(propertyList,
			includeQualifiers, includeClassOrigin));

		ci = cc.newInstance();
		// teaches
		op = new CIMObjectPath(teacherClass);
		op.addKey("name", new CIMValue("teacher1"));
		op.setNameSpace(assocName.getNameSpace());
		ci.setProperty("teaches", new CIMValue(op));
		// taught by
		op = new CIMObjectPath(studentClass);
		op.addKey("name", new CIMValue("student2"));
		op.setNameSpace(assocName.getNameSpace());
		ci.setProperty("taughtby", new CIMValue(op));
		v.addElement(ci.filterProperties(propertyList,
			includeQualifiers, includeClassOrigin));

	        return (CIMInstance[])v.toArray();
	    }

	    if (name.equals("teacher2")) {
	        // We need associations for teacher2
	        CIMInstance ci = cc.newInstance();
		// teaches
		CIMObjectPath op = new CIMObjectPath(teacherClass);
		op.addKey("name", new CIMValue("teacher2"));
		op.setNameSpace(assocName.getNameSpace());
		ci.setProperty("teaches", new CIMValue(op));
		// taught by
		op = new CIMObjectPath(studentClass);
		op.addKey("name", new CIMValue("student3"));
		op.setNameSpace(assocName.getNameSpace());
		ci.setProperty("taughtby", new CIMValue(op));
		v.addElement(ci.filterProperties(propertyList,
			includeQualifiers, includeClassOrigin));

		ci = cc.newInstance();
		// teaches
		op = new CIMObjectPath(teacherClass);
		op.addKey("name", new CIMValue("teacher2"));
		op.setNameSpace(assocName.getNameSpace());
		ci.setProperty("teaches", new CIMValue(op));
		// taught by
		op = new CIMObjectPath(studentClass);
		op.addKey("name", new CIMValue("student4"));
		op.setNameSpace(assocName.getNameSpace());
		ci.setProperty("taughtby", new CIMValue(op));
		v.addElement(ci.filterProperties(propertyList,
			includeQualifiers, includeClassOrigin));

		ci = cc.newInstance();
		// teaches
		op = new CIMObjectPath(teacherClass);
		op.addKey("name", new CIMValue("teacher2"));
		op.setNameSpace(assocName.getNameSpace());
		ci.setProperty("teaches", new CIMValue(op));
		// taught by
		op = new CIMObjectPath(studentClass);
		op.addKey("name", new CIMValue("student5"));
		op.setNameSpace(assocName.getNameSpace());
		ci.setProperty("taughtby", new CIMValue(op));
		v.addElement(ci.filterProperties(propertyList,
			includeQualifiers, includeClassOrigin));

		ci = cc.newInstance();
		// teaches
		op = new CIMObjectPath(teacherClass);
		op.addKey("name", new CIMValue("teacher2"));
		op.setNameSpace(assocName.getNameSpace());
		ci.setProperty("teaches", new CIMValue(op));
		// taught by
		op = new CIMObjectPath(studentClass);
		op.addKey("name", new CIMValue("student6"));
		op.setNameSpace(assocName.getNameSpace());
		ci.setProperty("taughtby", new CIMValue(op));
		v.addElement(ci.filterProperties(propertyList,
			includeQualifiers, includeClassOrigin));

	        return (CIMInstance[])v.toArray();
	    }

	    if (name.equals("teacher3")) {
	        // We need associations for teacher3
	        // Teacher3 teaches nobody
	        return (CIMInstance[])v.toArray();
	    }

	    // Huh? We dont know this teacher
	    throw new CIMInstanceException(CIMException.CIM_ERR_NOT_FOUND, 
					   objectName);
	}

	if (objectName.getObjectName().equalsIgnoreCase(studentClass)) {
	    Vector v = new Vector();

	    if ((role != null) && (!role.equalsIgnoreCase("taughtby"))) {
	        // Students only play the taughtby role.
	        return (CIMInstance[])v.toArray();
	    }

	    // Ok we need to get associators of a student
	    CIMProperty nameProp = 
		(CIMProperty)objectName.getKeys().elementAt(0);
	    String name = (String)nameProp.getValue().getValue();
	    /*
	     * Get the association class
	     * More efficient if you apply the filter properties here,
	     * and then take care of setting the appropriate properties
	     * further down. Not doing it now for convenience.
	     */
	    CIMClass cc = cimom.getClass(assocName, false,
					 includeQualifiers,
					 includeClassOrigin,
					 propertyList);

	    if (name.equals("student1")) {
	        // We need associations for student1
	        CIMInstance ci = cc.newInstance();
		// teaches
		CIMObjectPath op = new CIMObjectPath(teacherClass);
		op.addKey("name", new CIMValue("teacher1"));
		op.setNameSpace(assocName.getNameSpace());
		ci.setProperty("teaches", new CIMValue(op));
		// taught by
		op = new CIMObjectPath(studentClass);
		op.addKey("name", new CIMValue("student1"));
		op.setNameSpace(assocName.getNameSpace());
		ci.setProperty("taughtby", new CIMValue(op));
		v.addElement(ci.filterProperties(propertyList,
			includeQualifiers, includeClassOrigin));

	        return (CIMInstance[])v.toArray();
	    }

	    if (name.equals("student2")) {
	        // We need associations for student2
	        CIMInstance ci = cc.newInstance();
		// teaches
		CIMObjectPath op = new CIMObjectPath(teacherClass);
		op.addKey("name", new CIMValue("teacher1"));
		op.setNameSpace(assocName.getNameSpace());
		ci.setProperty("teaches", new CIMValue(op));
		// taught by
		op = new CIMObjectPath(studentClass);
		op.addKey("name", new CIMValue("student2"));
		op.setNameSpace(assocName.getNameSpace());
		ci.setProperty("taughtby", new CIMValue(op));
		v.addElement(ci.filterProperties(propertyList,
			includeQualifiers, includeClassOrigin));

	        return (CIMInstance[])v.toArray();
	    }

	    if (name.equals("student3")) {
	        // We need associations for student3
	        CIMInstance ci = cc.newInstance();
		// teaches
		CIMObjectPath op = new CIMObjectPath(teacherClass);
		op.addKey("name", new CIMValue("teacher2"));
		op.setNameSpace(assocName.getNameSpace());
		ci.setProperty("teaches", new CIMValue(op));
		// taught by
		op = new CIMObjectPath(studentClass);
		op.addKey("name", new CIMValue("student3"));
		op.setNameSpace(assocName.getNameSpace());
		ci.setProperty("taughtby", new CIMValue(op));
		v.addElement(ci.filterProperties(propertyList,
			includeQualifiers, includeClassOrigin));

	        return (CIMInstance[])v.toArray();
	    }


	    if (name.equals("student4")) {
	        // We need associations for student2
	        CIMInstance ci = cc.newInstance();
		// teaches
		CIMObjectPath op = new CIMObjectPath(teacherClass);
		op.addKey("name", new CIMValue("teacher2"));
		op.setNameSpace(assocName.getNameSpace());
		ci.setProperty("teaches", new CIMValue(op));
		// taught by
		op = new CIMObjectPath(studentClass);
		op.addKey("name", new CIMValue("student4"));
		op.setNameSpace(assocName.getNameSpace());
		ci.setProperty("taughtby", new CIMValue(op));
		v.addElement(ci.filterProperties(propertyList,
			includeQualifiers, includeClassOrigin));

	        return (CIMInstance[])v.toArray();
	    }


	    if (name.equals("student5")) {
	        // We need associations for student2
	        CIMInstance ci = cc.newInstance();
		// teaches
		CIMObjectPath op = new CIMObjectPath(teacherClass);
		op.addKey("name", new CIMValue("teacher2"));
		op.setNameSpace(assocName.getNameSpace());
		ci.setProperty("teaches", new CIMValue(op));
		// taught by
		op = new CIMObjectPath(studentClass);
		op.addKey("name", new CIMValue("student5"));
		op.setNameSpace(assocName.getNameSpace());
		ci.setProperty("taughtby", new CIMValue(op));
		v.addElement(ci.filterProperties(propertyList,
			includeQualifiers, includeClassOrigin));

	        return (CIMInstance[])v.toArray();
	    }


	    if (name.equals("student6")) {
	        // We need associations for student2
	        CIMInstance ci = cc.newInstance();
		// teaches
		CIMObjectPath op = new CIMObjectPath(teacherClass);
		op.addKey("name", new CIMValue("teacher2"));
		op.setNameSpace(assocName.getNameSpace());
		ci.setProperty("teaches", new CIMValue(op));
		// taught by
		op = new CIMObjectPath(studentClass);
		op.addKey("name", new CIMValue("student6"));
		op.setNameSpace(assocName.getNameSpace());
		ci.setProperty("taughtby", new CIMValue(op));
		v.addElement(ci.filterProperties(propertyList,
			includeQualifiers, includeClassOrigin));

	        return (CIMInstance[])v.toArray();
	    }


	    if (name.equals("student7")) {
	        // No associations for student7
	        return (CIMInstance[])v.toArray();
	    }

	    // Huh? We dont know this student
	    throw new CIMInstanceException(CIMException.CIM_ERR_NOT_FOUND, 
					   objectName);
	}
	// If object path is well formed, we shouldnt come here
	throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED, 
			       objectName);
    }

    public CIMObjectPath[] referenceNames(CIMObjectPath assocName,
				CIMObjectPath objectName,
				String role) throws CIMException {
	System.out.println("In referenceNames, where:");
	System.out.println("-->\tCIMObjectPath assocName="+assocName);
	System.out.println("-->\tCIMObjectPath objectName="+objectName);
	System.out.println("-->\trole="+role);

	if (objectName.getObjectName().equalsIgnoreCase(teacherClass)) {
	    Vector v = new Vector();

	    if ((role != null)  &&
		(!role.equalsIgnoreCase("teaches"))) {
	        // Teachers only play the teaches role.
	        return null;
	    }

	    // Ok we need to get association names to a teacher
	    CIMProperty nameProp = 
	                (CIMProperty)objectName.getKeys().elementAt(0);
	    String name = (String)nameProp.getValue().getValue();

	    if (name.equals("teacher1")) {
	        // We need association names for teacher1
	        CIMObjectPath op = new CIMObjectPath(teacherStudentClass);
		op.setNameSpace(assocName.getNameSpace());
		// teaches
		CIMObjectPath opr = new CIMObjectPath(teacherClass);
		opr.setNameSpace(assocName.getNameSpace());
		opr.addKey("name", new CIMValue("teacher1"));
		op.addKey("teaches", new CIMValue(opr));
		// taught by
		opr = new CIMObjectPath(studentClass);
		opr.setNameSpace(assocName.getNameSpace());
		opr.addKey("name", new CIMValue("student1"));
		op.addKey("taughtby", new CIMValue(opr));
		v.addElement(op);

		op = new 
		  CIMObjectPath(teacherStudentClass);
		op.setNameSpace(assocName.getNameSpace());
		// teaches
		opr = new CIMObjectPath(teacherClass);
		opr.setNameSpace(assocName.getNameSpace());
		opr.addKey("name", new CIMValue("teacher1"));
		op.addKey("teaches", new CIMValue(opr));
		// taught by
		opr = new CIMObjectPath(studentClass);
		opr.setNameSpace(assocName.getNameSpace());
		opr.addKey("name", new CIMValue("student2"));
		op.addKey("taughtby", new CIMValue(opr));
		v.addElement(op);

		CIMObjectPath[] copArray = new CIMObjectPath[v.size()];
		v.toArray(copArray);
		return copArray;
	    }

	    if (name.equals("teacher2")) {

	        // We need association names for teacher2
	        CIMObjectPath op = new CIMObjectPath(teacherStudentClass);
		op.setNameSpace(assocName.getNameSpace());
		// teaches
		CIMObjectPath opr = new CIMObjectPath(teacherClass);
		opr.setNameSpace(assocName.getNameSpace());
		opr.addKey("name", new CIMValue("teacher2"));
		op.addKey("teaches", new CIMValue(opr));
		// taught by
		opr = new CIMObjectPath(studentClass);
		opr.setNameSpace(assocName.getNameSpace());
		opr.addKey("name", new CIMValue("student3"));
		op.addKey("taughtby", new CIMValue(opr));
		v.addElement(op);

		op = new 
		  CIMObjectPath(teacherStudentClass);
		op.setNameSpace(assocName.getNameSpace());
		// teaches
		opr = new CIMObjectPath(teacherClass);
		opr.setNameSpace(assocName.getNameSpace());
		opr.addKey("name", new CIMValue("teacher2"));
		op.addKey("teaches", new CIMValue(opr));
		// taught by
		opr = new CIMObjectPath(studentClass);
		opr.setNameSpace(assocName.getNameSpace());
		opr.addKey("name", new CIMValue("student4"));
		op.addKey("taughtby", new CIMValue(opr));
		v.addElement(op);

		op = new CIMObjectPath(teacherStudentClass);
		op.setNameSpace(assocName.getNameSpace());
		// teaches
		opr = new CIMObjectPath(teacherClass);
		opr.setNameSpace(assocName.getNameSpace());
		opr.addKey("name", new CIMValue("teacher2"));
		op.addKey("teaches", new CIMValue(opr));
		// taught by
		opr = new CIMObjectPath(studentClass);
		opr.setNameSpace(assocName.getNameSpace());
		opr.addKey("name", new CIMValue("student5"));
		op.addKey("taughtby", new CIMValue(opr));
		v.addElement(op);

		op = new CIMObjectPath(teacherStudentClass);
		op.setNameSpace(assocName.getNameSpace());
		// teaches
		opr = new CIMObjectPath(teacherClass);
		opr.setNameSpace(assocName.getNameSpace());
		opr.addKey("name", new CIMValue("teacher2"));
		op.addKey("teaches", new CIMValue(opr));
		// taught by
		opr = new CIMObjectPath(studentClass);
		opr.setNameSpace(assocName.getNameSpace());
		opr.addKey("name", new CIMValue("student6"));
		op.addKey("taughtby", new CIMValue(opr));
		v.addElement(op);

		CIMObjectPath[] copArray = new CIMObjectPath[v.size()];
		v.toArray(copArray);
		return copArray;
	    }

	    if (name.equals("teacher3")) {
	        // We need associations for teacher3
	        // Teacher3 teaches nobody
	        return null;
	    }

	    // Huh? We dont know this teacher
	    throw new CIMInstanceException(CIMException.CIM_ERR_NOT_FOUND, 
		objectName);
	}

	if (objectName.getObjectName().equalsIgnoreCase(studentClass)) {
		
	    Vector v = new Vector();

	    if ((role != null)  &&
		(!role.equalsIgnoreCase("taughtby"))) {
	        // Teachers only play the teaches role.
	        return null;
	    }

	    // Ok we need to get association names to a student
	    CIMProperty nameProp = 
	        (CIMProperty)objectName.getKeys().elementAt(0);
	    String name = (String)nameProp.getValue().getValue();

	    if (name.equals("student1")) {
	        // We need association names for student1
	        CIMObjectPath op = new CIMObjectPath(teacherStudentClass);
		op.setNameSpace(assocName.getNameSpace());
		// teaches
		CIMObjectPath opr = new CIMObjectPath(teacherClass);
		opr.setNameSpace(assocName.getNameSpace());
		opr.addKey("name", new CIMValue("teacher1"));
		op.addKey("teaches", new CIMValue(opr));
		// taught by
		opr = new CIMObjectPath(studentClass);
		opr.setNameSpace(assocName.getNameSpace());
		opr.addKey("name", new CIMValue("student1"));
		op.addKey("taughtby", new CIMValue(opr));
		v.addElement(op);

		CIMObjectPath[] copArray = new CIMObjectPath[v.size()];
		v.toArray(copArray);
		return copArray;
	    }

	    if (name.equals("student2")) {
	        // We need association names for student2
	        CIMObjectPath op = new CIMObjectPath(teacherStudentClass);
		op.setNameSpace(assocName.getNameSpace());
		// teaches
		CIMObjectPath opr = new CIMObjectPath(teacherClass);
		opr.setNameSpace(assocName.getNameSpace());
		opr.addKey("name", new CIMValue("teacher1"));
		op.addKey("teaches", new CIMValue(opr));
		// taught by
		opr = new CIMObjectPath(studentClass);
		opr.setNameSpace(assocName.getNameSpace());
		opr.addKey("name", new CIMValue("student2"));
		op.addKey("taughtby", new CIMValue(opr));
		v.addElement(op);

		CIMObjectPath[] copArray = new CIMObjectPath[v.size()];
		v.toArray(copArray);
		return copArray;
	    }

	    if (name.equals("student3")) {
	        // We need association names for student3
	        CIMObjectPath op = new CIMObjectPath(teacherStudentClass);
		op.setNameSpace(assocName.getNameSpace());
		// teaches
		CIMObjectPath opr = new CIMObjectPath(teacherClass);
		opr.setNameSpace(assocName.getNameSpace());
		opr.addKey("name", new CIMValue("teacher2"));
		op.addKey("teaches", new CIMValue(opr));
		// taught by
		opr = new CIMObjectPath(studentClass);
		opr.setNameSpace(assocName.getNameSpace());
		opr.addKey("name", new CIMValue("student3"));
		op.addKey("taughtby", new CIMValue(opr));
		v.addElement(op);

		CIMObjectPath[] copArray = new CIMObjectPath[v.size()];
		v.toArray(copArray);
		return copArray;
	    }

	    if (name.equals("student4")) {
	        // We need association names for student4
	        CIMObjectPath op = new CIMObjectPath(teacherStudentClass);
		op.setNameSpace(assocName.getNameSpace());
		// teaches
		CIMObjectPath opr = new CIMObjectPath(teacherClass);
		opr.setNameSpace(assocName.getNameSpace());
		opr.addKey("name", new CIMValue("teacher2"));
		op.addKey("teaches", new CIMValue(opr));
		// taught by
		opr = new CIMObjectPath(studentClass);
		opr.setNameSpace(assocName.getNameSpace());
		opr.addKey("name", new CIMValue("student4"));
		op.addKey("taughtby", new CIMValue(opr));
		v.addElement(op);

		CIMObjectPath[] copArray = new CIMObjectPath[v.size()];
		v.toArray(copArray);
		return copArray;
	    }

	    if (name.equals("student5")) {
	        // We need association names for student5
	        CIMObjectPath op = new CIMObjectPath(teacherStudentClass);
	        op.setNameSpace(assocName.getNameSpace());
	        // teaches
		CIMObjectPath opr = new CIMObjectPath(teacherClass);
		opr.setNameSpace(assocName.getNameSpace());
		opr.addKey("name", new CIMValue("teacher2"));
		op.addKey("teaches", new CIMValue(opr));
		// taught by
		opr = new CIMObjectPath(studentClass);
		opr.setNameSpace(assocName.getNameSpace());
		opr.addKey("name", new CIMValue("student5"));
		op.addKey("taughtby", new CIMValue(opr));
		v.addElement(op);

		CIMObjectPath[] copArray = new CIMObjectPath[v.size()];
		v.toArray(copArray);
		return copArray;
	    }

	    if (name.equals("student6")) {
	        // We need association names for student6
	        CIMObjectPath op = new CIMObjectPath(teacherStudentClass);
		op.setNameSpace(assocName.getNameSpace());
		// teaches
		CIMObjectPath opr = new CIMObjectPath(teacherClass);
		opr.setNameSpace(assocName.getNameSpace());
		opr.addKey("name", new CIMValue("teacher2"));
		op.addKey("teaches", new CIMValue(opr));
		// taught by
		opr = new CIMObjectPath(studentClass);
		opr.setNameSpace(assocName.getNameSpace());
		opr.addKey("name", new CIMValue("student6"));
		op.addKey("taughtby", new CIMValue(opr));
		v.addElement(op);

		CIMObjectPath[] copArray = new CIMObjectPath[v.size()];
		v.toArray(copArray);
		return copArray;
	    }

	    if (name.equals("student7")) {
	        // No associations for student7
	        return null;
	    }

	    // Huh? We dont know this student
	    throw new CIMInstanceException(CIMException.CIM_ERR_NOT_FOUND, 
		objectName);
	}
	// If object path is well formed, we shouldnt come here
	throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED, 
			       objectName);
    }

}



