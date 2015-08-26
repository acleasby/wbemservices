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

package org.wbemservices.wbem.cimom;

/**
 * This class is used by the MOF registry handler to log the actions that are
 * being performed. Once all the actions are completed, the UnregHandler
 * generates appropriate information which can be used to undo (i.e. perform
 * unregistry) the actions that took place. Presently this class creates
 * unreg files using a pseudo-MOF format. We may want to make this an interface 
 * in the future, so that we can have multiple implementations - for e.g. we
 * can generate XML documents which represent the appropriate undo actions.
 *
 * @author      Sun Microsystems, Inc.
 * @version     1.3, 04/04/01
 * @since       WBEM 2.5
 */
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import javax.wbem.cim.*;
import javax.wbem.client.Debug;

class UnregHandler {

    OutputStream out;
    // This list contains the operations needed for unreg. Additions to this
    // list are done so that the undo for the last operation appears first -
    // that way undos are done in the reverse order.
    List opList = new ArrayList();
    // Each registry operation extends the UndoOperation base class which
    // can be invoked to output undo actions into the pseudo-MOF format.
    private static abstract class UndoOperation {
	// Each operation needs a particular mode to be set in the output MOF
	// using the pragma namespace directive. The mode determines what
	// directive is used.
	static final int DELETEMODE = 1;
	static final int SETMODE = 2;
	static final int CREATEMODE = 3;

	// mode of this operation
	protected int mode = 0;
	// namespace of this operation. Like mode, the MOF file requires us
	// to set the namespace for this operation using the pragma directive.
	protected CIMObjectPath nameSpace = null;

	// mode getter
	int getMode() {
	    return mode;
	}

	// for the present mode, returns the directive that must be ouput.
	private String getModeString() {
	    switch(mode) {
		// XXX we need to put these in a common constants file
		case DELETEMODE: return "__delete";
		case CREATEMODE: return "__create";
		case SETMODE: return "__modify";
	    }
	    throw new IllegalArgumentException(mode+"");
	}

	// mode setter
	void setMode(int mode) {
	    this.mode = mode;
	}

	// namespace getter
	CIMObjectPath getNameSpace() {
	    return nameSpace;
	}

	// namespace setter
	void setNameSpace(CIMObjectPath nameSpace) {
	    this.nameSpace = nameSpace;
	}

	// Given a previous input operation, this method determines what
	// directives, if any are required to be output to the MOF. Directives
	// are needed if the mode or namespace is different for the current
	// operation, compared to the previous operation (or if there is no
	// previous operation, i.e. previousOp == null). Can be used by
	// the subclasses to generate directives.
	String getDirectives(UndoOperation previousOp) {
	    StringBuffer directive = new StringBuffer("");
	    String nsc = getNameSpace().getNameSpace();
	    if (previousOp == null) {
		// No previous operation, output directive is set to that
		// required by the current operation.
		nsc = nsc.replace('\\','/');
		directive.append("\n#pragma namespace(\""+nsc+"\")");
		directive.append("\n#pragma namespace(\""+
		getModeString()+"\")");
		return directive.toString();
	    }
	    String nsp = previousOp.getNameSpace().getNameSpace();
	    if (!nsp.equals(nsc)) {
		// Namespaces differ - Need to output namespace directive
		nsc = nsc.replace('\\','/');
		directive.append("\n#pragma namespace(\""+nsc+"\")");
	    }
	    if (!(getMode() == previousOp.getMode())) {
		// Mode differs - Need to output mode directive
		directive.append("\n#pragma namespace(\""+
		getModeString()+"\")");
	    }
	    return directive.toString();
	}

	// The undo operation. This must be implemented by the subclass 
	// operations.
	abstract String undo(UndoOperation previousOp);

	public UndoOperation(CIMObjectPath nameSpace, int mode) {
	    setNameSpace(nameSpace);
	    setMode(mode);
	}
    }

    // This class undoes a modify class operation. It requires the definition
    // of the class before it was modified.
    private static class SetClassOp extends UndoOperation {
	CIMClass oldcc;

	// The undo operation returns the namespace directives and the
	// the old definition of the CIM class.
	public String undo(UndoOperation previousOp) {
	    StringBuffer sb = new StringBuffer("");
	    // Get the namespace and mode directives from the superclass
	    sb.append(getDirectives(previousOp));
	    sb.append("\n"+oldcc);
	    return sb.toString();
	}

	public SetClassOp(CIMObjectPath namespace, CIMClass oldcc) {
	    super(namespace, SETMODE);
	    Debug.trace3("Set class op created");
	    this.oldcc = oldcc;
	}
    }

    // This class undoes a create class op. It needs the namespace and name
    // of the class that was created.
    private static class CreateClassOp extends UndoOperation {
	String cName;

	public String undo(UndoOperation previousOp) {
	    StringBuffer sb = new StringBuffer("");
	    // Get the namespace and mode directives from the superclass
	    sb.append(getDirectives(previousOp));
	    sb.append("\n"+new CIMClass(cName));
	    return sb.toString();
	}

	// The undo operation returns the namespace directives and the
	// name of the class to delete on undo.
	public CreateClassOp(CIMObjectPath namespace, CIMClass newClass) {
	    // In order to undo a create, we must do the opposite, i.e.
	    // specify a delete mode.
	    super(namespace, DELETEMODE);
	    Debug.trace3("Create class op created");
	    // We just store the class name, that is all the info we need
	    // to delete it.
	    this.cName = newClass.getName();
	}
    }

    // This class undoes a create instance op. It needs the namespace and the
    // instance that was created. 
    private static class CreateInstanceOp extends UndoOperation {
	CIMInstance ci;

	// The undo operation returns the namespace directives and the
	// instance to delete on undo.
	public String undo(UndoOperation previousOp) {
	    StringBuffer sb = new StringBuffer("");
	    // Get the namespace and mode directives from the superclass
	    sb.append(getDirectives(previousOp));
	    sb.append("\n"+ci);
	    return sb.toString();
	}

	public CreateInstanceOp(CIMObjectPath namespace, 
	CIMInstance newInstance) {
	    // In order to undo a create, we must do the opposite, i.e.
	    // specify a delete mode.
	    super(namespace, DELETEMODE);
	    Debug.trace3("Create instance op created");
	    // We just store the class name, that is all the info we need
	    // to delete it.
	    this.ci = newInstance;
	}
    }

    // This class undoes a delete instance op. It needs the namespace and the
    // instance that was deleted. 
    private static class DeleteInstanceOp extends UndoOperation {
	CIMInstance ci;

	// The undo operation returns the namespace directives and the
	// instance to delete on undo.
	public String undo(UndoOperation previousOp) {
	    StringBuffer sb = new StringBuffer("");
	    // Get the namespace and mode directives from the superclass
	    sb.append(getDirectives(previousOp));
	    sb.append("\n"+ci);
	    return sb.toString();
	}

	public DeleteInstanceOp(CIMObjectPath namespace, 
	CIMInstance newInstance) {
	    // In order to undo a create, we must do the opposite, i.e.
	    // specify a delete mode.
	    super(namespace, CREATEMODE);
	    Debug.trace3("Delete instance op created");
	    // We just store the class name, that is all the info we need
	    // to delete it.
	    this.ci = newInstance;
	}
    }

    // This class undoes a modify instance operation. It requires the definition
    // of the instance before it was modified.
    private static class SetInstanceOp extends UndoOperation {
	CIMInstance oldci;

	// The undo operation returns the namespace directives and the
	// the old definition of the CIM instance.
	public String undo(UndoOperation previousOp) {
	    StringBuffer sb = new StringBuffer("");
	    // Get the namespace and mode directives from the superclass
	    sb.append(getDirectives(previousOp));
	    sb.append("\n"+oldci);
	    return sb.toString();
	}

	public SetInstanceOp(CIMObjectPath namespace, CIMInstance oldci) {
	    super(namespace, SETMODE);
	    Debug.trace3("Set instance op created");
	    this.oldci = oldci;
	}
    }
    // Constructor takes an output stream to where the unreg actions are written
    // It is up to the outputstream to decide if/how to persist the information
    UnregHandler(OutputStream out) {
	this.out = out;
    }

    public void createNameSpace(CIMNameSpace ns)
    throws CIMException {
	// XXX must log this action??
    }

    public void createQualifierType(CIMObjectPath name, CIMQualifierType qt)
    throws CIMException {
	// XXX must log this action??
    }

    // This method takes in the old class definition. This is used to revert
    // back during an unreg. We also pass in newcc just in case future 
    // implementations choose to do optimizations based on the class diffs.
    public void setClass(CIMObjectPath name, CIMClass oldcc, CIMClass newcc)
    throws CIMException {
	// We ignore newcc
	opList.add(0, new SetClassOp(name, oldcc));
	Debug.trace3("Added SetClassOp to list");
    }

    public void createClass(CIMObjectPath name, CIMClass cc)
    throws CIMException {
	opList.add(0, new CreateClassOp(name, cc));
    }

    // This method takes in the old instance definition. This is used to revert
    // back during an unreg. We also pass in newci just in case future 
    // implementations choose to do optimizations based on the instance diffs.
    public void setInstance(CIMObjectPath name, CIMInstance oldci, 
    CIMInstance newci)
    throws CIMException {
	// We ignore the newci for now.
	opList.add(0, new SetInstanceOp(name, oldci));
    }

    public void createInstance(CIMObjectPath name, CIMInstance ci)
    throws CIMException {
	opList.add(0, new CreateInstanceOp(name, ci));
	Debug.trace3("Added CreateInstanceOp to list");
    }

    public void deleteInstance(CIMObjectPath path, CIMInstance oldci)
    throws CIMException {
	opList.add(0, new DeleteInstanceOp(path, oldci));
	Debug.trace3("Added DeleteInstanceOp to list");
    }

    // This method is invoked when registration is complete. All actions
    // pertaining to unreg must be output at this time.
    public void outputUnreg() {
	Iterator i = opList.iterator();
	UndoOperation prevOp = null;
	PrintStream pstream = new PrintStream(out);
	while (i.hasNext()) {
	    UndoOperation uop = (UndoOperation)i.next();
	    String outString = uop.undo(prevOp);
	    pstream.print(outString);
	    prevOp = uop;
	}
    }
}
