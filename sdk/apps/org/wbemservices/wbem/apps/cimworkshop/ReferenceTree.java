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

package org.wbemservices.wbem.apps.cimworkshop;

import javax.swing.JScrollPane;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import java.awt.Component;
import java.awt.Cursor;
import java.util.Enumeration;

import org.wbemservices.wbem.apps.common.Util;

import javax.wbem.cim.CIMObjectPath;
import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMInstance;
import javax.wbem.client.CIMClient;

/**
 * 
 *
 * @author 	Sun Microsystems
 */


/**
 * This class created a tree which allows a user to traverse the associations
 * of a class or instance.  The root node of the tree will always contain a 
 * class or instance name. If that class has any references to it, it's child
 * nodes will be those references.  In turn the child nodes of those references
 * will contain class or instance names of the associators to those references.
 *
 */
public class ReferenceTree extends JScrollPane implements 
    TreeExpansionListener {
   
    protected JTree tree;
    protected JScrollPane thisPane;
    protected CIMClient cimClient;
    protected ClassNode root;
    private final int CLASS_TREE = 0;
    private final int INSTANCE_TREE = 1;
    private int treeType = CLASS_TREE;

/**
 * ReferenceTree constructor
 *
 * @param op     The opject path of a class or reference.
 * @param cc     The CIMClient object used to connect to the CIM Object
 *               Manager
 */
    public ReferenceTree(CIMObjectPath op, CIMClient cc) {
	super();
	cimClient = cc;
	thisPane = this;
	
	// check if the CIMObject path is for an instance
	if ((op.getKeys() != null) && (op.getKeys().size() > 0)) {
	    treeType = INSTANCE_TREE;
	}

	// create the root node
	ClassNode rootNode = new ClassNode(op);
	DefaultTreeModel model = new DefaultTreeModel(rootNode);
	tree = new JTree(model);

	// explore node will add it's children
	exploreNode(rootNode);

	TreeCellRenderer cellRenderer = tree.getCellRenderer();
	if (cellRenderer instanceof DefaultTreeCellRenderer) {
	    CustomBasicRenderer cbr = new CustomBasicRenderer();
	    tree.setCellRenderer(cbr);
	}

	tree.addTreeExpansionListener(this);
	tree.getSelectionModel().setSelectionMode( 
		TreeSelectionModel.SINGLE_TREE_SELECTION);
	tree.setRootVisible(true);
	setViewportView(tree);
    }

// BUGFIX. Accessibility changes
    /**
     * Returns the JTree
     *
     */
    public JTree getTree() {
	return tree;
    }

    /**
     * When the node of a tree is expanded, we check if we have explored
     * this node before.  If not, we explore it (check if it needs child
     * nodes and added them if it does)
     *
     */
      public void treeExpanded(TreeExpansionEvent e) {	
	TreePath path = e.getPath();
	
	CIMElementNode node = (CIMElementNode)
	    path.getLastPathComponent();

	if (!node.isExplored()) {
	    Cursor oldCursor = getCursor();
	    Util.setWaitCursor(this);
	    exploreNode(node);
	    Util.setCursor(this, oldCursor);
	}
    }

    public void treeCollapsed(TreeExpansionEvent e) {
    }

    // Return the currently selected node.
    public DefaultMutableTreeNode getSelectedNode() {
	if (tree.getSelectionPath() == null) {
	    return null;
	}
	return (DefaultMutableTreeNode)tree.getSelectionPath().
				    getLastPathComponent();
    }

    public TreePath getSelectedPath() {
	return tree.getSelectionPath();
    }

    /**
     *
     * When we explore a node, we check if it has been explored before.  If
     * it hasn't,  we add, depending on what type of node it is, reference or 
     * class nodes as it's child nodes.  We then mark the node as explored.
     */
    private void exploreNode(CIMElementNode node) {
	Enumeration childList = node.getChildList();
	// check if it has been explored or has children that need to be added
	if (!node.isExplored() && (childList != null)) {
	    int i = 0;
	    DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
	    while (childList.hasMoreElements()) {
		CIMObjectPath op = (CIMObjectPath)childList.nextElement();
		// if this node is a class node, children will be reference
		// nodes
		if (node instanceof ClassNode) {
		    ReferenceNode refNode = new ReferenceNode(op,
		        node.getObjectPath());
		    model.insertNodeInto(refNode, node, i++);
		// if this node is a referenc node, children will be class
		// nodes
		} else if (node instanceof ReferenceNode) {
		    ClassNode classNode = new ClassNode(op);
		    model.insertNodeInto(classNode, node, i++);
		}
	    }
	    // set node as been explored
	    node.setExplored();
	}
    }

    // depending on whether the node is a class node or a reference node,
    // display a different icon so it is easier to follow the tree GUI
    protected class CustomBasicRenderer extends DefaultTreeCellRenderer {
	public Component getTreeCellRendererComponent(JTree theTree,
	    Object value, boolean isSelected, boolean expanded,
	    boolean leaf, int row, boolean focus) {

	    CIMElementNode node = (CIMElementNode)value;

	    Component c = super.getTreeCellRendererComponent(theTree,
		value, isSelected, expanded, leaf, row, focus);

	    String iconName = "class.gif";
	    
	    if (node instanceof ReferenceNode) {
		iconName = "reference.gif";
	    }

	    if (c instanceof JLabel) {
		JLabel l = (JLabel) c;

		ImageIcon icon = Util.loadImageIcon(iconName);
		l.setIcon(icon);

	    }
	    return c;
	}
    }


    abstract class CIMElementNode extends DefaultMutableTreeNode {
	protected boolean explored = false;
	protected boolean leaf = false;
	Enumeration childList = null; 
	
	public CIMElementNode(CIMObjectPath op) {
	    setUserObject(op);
	}


	public CIMObjectPath getObjectPath() {
	    return (CIMObjectPath)getUserObject();
	}

	public String toString() {
	    CIMObjectPath op = (CIMObjectPath)getUserObject();
	    return op.toString();
	}

	public boolean isLeaf() {
	    return leaf;
	}

	public boolean isExplored() {
	    return explored;
	}
	
	public void setExplored() {
	    explored = true;
	}

	public void setExplored(boolean b) {
	    explored = b;
	}

	public Enumeration getChildList() {
	    return childList;
	}
	
    }

    class ReferenceNode extends CIMElementNode {
	
	public ReferenceNode(CIMObjectPath op, CIMObjectPath opParent) {
	    super(op);
	    setChildList(opParent);
	}
	
	private void setChildList(CIMObjectPath opParent) {
	    CIMObjectPath op = getObjectPath();
	    leaf = true;
	    try {
		Enumeration e  = cimClient.associatorNames(opParent, 
							   op.getObjectName(),
							   null, null, null);
		childList = Util.sortEnumeration(e);
		leaf = !childList.hasMoreElements();
	    } catch (CIMException exc) {
		// ignore error, leaf will be true
	    }
		
	}

	
	public String getReferenceName() {
	    return getObjectPath().getObjectName();
	}

	public String toString() {
	    return getReferenceName();
	}

    }

    class ClassNode extends CIMElementNode {

	public ClassNode(CIMObjectPath op) {
	    super(op);
	    setChildList();
	}

	private void setChildList() {
	    CIMObjectPath op = null;
	    if (treeType == INSTANCE_TREE) {
		try {
		    CIMInstance ci = cimClient.getInstance(getObjectPath(), 
							   true, true, 
							   true, null);
		    op = new CIMObjectPath(ci.getClassName());
		} catch (CIMException exc) {
		    return;
		}
	    } else {
		op = getObjectPath();
	    } 
				
	    leaf = true;
	    try {
		
		Enumeration e = cimClient.referenceNames(op, "", "");
		childList = Util.sortEnumeration(e);
		leaf = !childList.hasMoreElements();
	    } catch (CIMException exc) {
		if (exc.getID().equals("NOT_ASSOCIATOR_PROVIDER")) {
		    leaf = false;
		}
		return;
	    }
	}

	// only show the first 40 characters of name so the tree isn't too wide
	public String toString() {
	    String ret = getObjectPath().toString();
	    if (ret.trim().length() < 40) {
		return ret;
	    } else {
		return ret.substring(0, 39);
	    }
	}

    }


}
