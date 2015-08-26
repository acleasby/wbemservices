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

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.wbem.cim.*;
import javax.wbem.client.*;

import org.wbemservices.wbem.apps.common.*;

/**
 * 
 *
 * @author 	Sun Microsystems
 */

public class ClassTreePane extends JScrollPane implements ActionListener, 
    TreeSelectionListener, TreeExpansionListener, MouseListener {
   
    protected JTree tree;
    protected DefaultTreeSelectionModel selectionModel;
    protected DefaultTreeModel treeModel;
    protected JComboBox objectBox;
    protected JScrollPane thisPane;

    protected CIMClient cimClient;

    protected RootTreeNode root;

    protected JPopupMenu popupMenu;
    protected JPopupMenu rootPopupMenu;

    protected ImageIcon classIcon;


    public ClassTreePane() {
	super();
	cimClient = null;
	thisPane = this;

	// create popup menu and menu items;
	popupMenu = new JPopupMenu();
	rootPopupMenu = new JPopupMenu();

	// create root node with default "root" label
	root = new RootTreeNode(new CIMObjectPath());
	treeModel = new DefaultTreeModel(root);

	// create tree
	tree = new JTree(treeModel);
	tree.addTreeSelectionListener(this);
	tree.addTreeExpansionListener(this);
	tree.addMouseListener(this);

	tree.getSelectionModel().setSelectionMode( 
		TreeSelectionModel.SINGLE_TREE_SELECTION);
	tree.setRootVisible(true);
	tree.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
	selectionModel = (DefaultTreeSelectionModel)tree.getSelectionModel();

	// class icon used to render class nodes on tree
	classIcon = Util.loadImageIcon("class.gif");

	// set up tree cell renderer to show class icons in tree
	TreeCellRenderer cellRenderer = tree.getCellRenderer();
	if (cellRenderer instanceof DefaultTreeCellRenderer) {
	    CustomBasicRenderer cbr = new CustomBasicRenderer();
	    tree.setCellRenderer(cbr);
	}

	// set up "Scheme" label
	ActionString asSchema = new ActionString("LBL_CLASS_SCHEMA");
	JLabel lSchema = new JLabel(asSchema.getString() + ":");
	lSchema.setDisplayedMnemonic(asSchema.getMnemonic());
	setTreeLabel(lSchema);

	setViewportView(tree);		
    }

    public void valueChanged(TreeSelectionEvent e) {
    }

    public void addTreeSelectionListener(TreeSelectionListener listener) {
	tree.addTreeSelectionListener(listener);
    }

    /**
     * When the node of a tree is expanded, we check if we have explored
     * this node before.  If not, we explore it (check if it needs child
     * nodes and added them if it does)
     *
     */
    public void treeExpanded(TreeExpansionEvent e) {	
	TreePath path = e.getPath();
	ClassTreeNode node = (ClassTreeNode)
	    path.getLastPathComponent();

	// is tree is not already explored then explore it
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
    public ClassTreeNode getSelectedNode() {
	if (tree.getSelectionPath() == null) {
	    return null;
	}
	return (ClassTreeNode)tree.getSelectionPath().
				    getLastPathComponent();
    }

    public String getSelectedNodeString() {
	ClassTreeNode node = getSelectedNode();
	if (node == null) {
	    return null;
	} else {
	    return node.toString();
	}
    }

    public void actionPerformed(ActionEvent e) {
	Util.setWaitCursor(this);
	String action = e.getActionCommand();

	if (action.equals("FIND_CLASS")) {
	    String className = JOptionPane.showInputDialog(this, 
		I18N.loadString("ASK_FIND_CLASS"));
	    if (className != null && !className.equals("")) {
		Stack s = new Stack();
		Util.setWaitCursor(this);
		if (findClass(new CIMObjectPath(""), className, s)) {
		    Stack s1 = (Stack)s.clone();
		    if (!selectNode(s1)) {
			// if cannot select node, need to refresh whole tree and
			// try again (class was added to CIMOM after last 
			// tree refresh)
			refreshTree(root);
			if (!selectNode(s)) {
				// if cannot find class second time, show error
			}
		    }
		} else {
		    JOptionPane.showMessageDialog(this, I18N.loadStringFormat(
			"ERR_FIND_CLASS", className), 
			I18N.loadString("TTL_CIM_ERROR"), 
			JOptionPane.INFORMATION_MESSAGE);
		}
		Util.setDefaultCursor(this);
	    }
	} else if (action.equals("REFRESH")) { 
	    refreshTree(getSelectedNode());
	} 
	Util.setDefaultCursor(this);
    }


    /**
     *
     * When we explore a node, we check if it has been explored before.  If
     * it hasn't,  we add it's child nodes.  We then mark the node as explored.
     */
    private void exploreNode(ClassTreeNode node) {
	Enumeration childList = node.getChildList();
	// if childList is null, try to set childList
	if (childList == null) {
	    node.setChildList();
	    childList = node.getChildList();
	}
	// check if it has been explored or has children that need to be added
	if (!node.isExplored() && (childList != null)) {
	    int i = 0;
	    DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
	    while (childList.hasMoreElements()) {
		CIMObjectPath op = (CIMObjectPath)childList.nextElement();
		ClassTreeNode childNode = new ClassTreeNode(op);
		model.insertNodeInto(childNode, node, i++);
	    }
	    // set node as been explored
	    node.setExplored();
	}
    }

    // to generate a sub tree, clean that subtree then explore the node
    protected void generateTree(ClassTreeNode node) {
	cleanTree(node);
	exploreNode(node);
    }

    protected void cleanTree(ClassTreeNode node) {
	DefaultTreeModel model = 
	    (DefaultTreeModel)tree.getModel();
	// select root node of subtree
        TreePath path = new TreePath(node.getPath());
	tree.setSelectionPath(path);
	while (model.getChildCount(node) > 0) {
	    ClassTreeNode child = (ClassTreeNode)
		model.getChild(node, 0);
	    model.removeNodeFromParent(child);
	}
	// set node to have no children and not explored
	node.clear();
    }

    public void mousePressed(MouseEvent evt) {
	if (evt.isPopupTrigger()) {
	    showPopupMenu(evt.getPoint());
	}
    }

    public void mouseReleased(MouseEvent evt) {
	if (evt.isPopupTrigger()) {
	    showPopupMenu(evt.getPoint());
	}
    }

    public void mouseClicked(MouseEvent evt) {
	if (evt.isPopupTrigger()) {
	    showPopupMenu(evt.getPoint());
	}
    }

    public void mouseEntered(MouseEvent evt) {
    }


    public void mouseExited(MouseEvent evt) {
    }


    protected void showPopupMenu(Point point) {
	TreePath path = tree.getPathForLocation(point.x, point.y);
	// if whitespace, don't show popup
	if (path == null) {
	    return;
	}
	tree.setSelectionPath(path);
	JPopupMenu popup;
	if (getSelectedNode() == root) {
	    popup = rootPopupMenu;
	} else {
	    popup = popupMenu;
	}
	Point vpLocation = getViewport().getViewPosition();
	popup.show(this, (point.x - vpLocation.x + 10), 
			 (point.y - vpLocation.y));
    }


    protected void deleteSelectedNode() {
	ClassTreeNode selectedNode = getSelectedNode();

	DefaultTreeModel model = 
	    (DefaultTreeModel)tree.getModel();
	ClassTreeNode parent = 
	    (ClassTreeNode)selectedNode.getParent();

	// find new node to select depending on whether there are any other 
	// siblings.  First look for previous sibling, then next sibling.  If
	// no siblings selection node will be parent
	ClassTreeNode newSelectionNode = (ClassTreeNode)parent.getChildBefore(
					     selectedNode);
	if (newSelectionNode == null) {
	    newSelectionNode = (ClassTreeNode)parent.getChildAfter(selectedNode);
	}
	if (newSelectionNode == null) {
	    newSelectionNode = parent;
	    // if no siblings, parent node will be leaf node when currently
	    // selected node is deleted
	    parent.setLeaf(true);
	}

	// find new selection path and select that path
	TreePath path = new TreePath(newSelectionNode.getPath());
	tree.setSelectionPath(path);	
	// remove child visually from tree
	model.removeNodeFromParent(selectedNode);
	model.reload(parent);

	// expand selection patgh and make is visible on the scroll pane
	tree.expandPath(path);
	tree.scrollPathToVisible(path);
	tree.setSelectionPath(path);	
    }

    /**
     * Determines is the root node of the tree is selected.
     *
     * @return   True is root node is selected, otherwise false
     */
    public boolean isRootSelected() {
	if (getSelectedNode() instanceof RootTreeNode) {
	    return true;
	} else {
	    return false;
	}
    }

    public void setTreeLabel(JLabel label) {
	setColumnHeaderView(label);
	label.setLabelFor(tree);
    }
   

    protected boolean findClass(CIMObjectPath objectPath, String className, 
	Stack searchStack) {
	searchStack.push(objectPath);
	if (objectPath.getObjectName().equalsIgnoreCase(className)) {
	    return true;
	} else {
	    try {
		Enumeration e = cimClient.enumerateClassNames(objectPath, 
							      false);
		while (e.hasMoreElements()) {
		    CIMObjectPath op = (CIMObjectPath)e.nextElement();
		    if (findClass(op, className, searchStack)) {
			return true;
		    }
		}
	    } catch (CIMException exc) {
		// if exception thrown, ignore 
	    }
	}
	searchStack.pop();
	return false;
    }		

    // refresh whole tree.  Sets new cimClient and root label
    public void refreshTree() {
	cimClient = CIMClientObject.getClient();
	root.setLabel(CIMClientObject.getNameSpace());
	tree.setSelectionRow(0);
	refreshTree(root);
    }

    // refresh sub tree and expand it's path
    public void refreshTree(ClassTreeNode node) {	
	generateTree(node);
	tree.expandPath(new TreePath(node));
	tree.repaint();

    }

    // refresh the sub tree of the selected node
    public void refreshSelectedNode() {
	refreshTree(getSelectedNode());
    }

    // walks down stack of paths to select a node
    protected boolean selectNode(Stack s) {
	// get ride of root node
	s.removeElementAt(0);
	boolean foundNode = false;
	ClassTreeNode node = root;
	for (int i = 0; i < s.size(); i++) {
	    foundNode = false;
	    String opName = ((CIMObjectPath)s.elementAt(i)).getObjectName();
	    for (Enumeration e = node.children(); e.hasMoreElements(); ) {
		node = (ClassTreeNode)e.nextElement();
		if (opName.equals(node.toString())) {
		    foundNode = true;
		    break;
		}
	    }
	    exploreNode(node);
	}
	if (foundNode) {
	    TreePath path = new TreePath(node.getPath());
	    tree.setSelectionPath(path);
	    tree.expandPath(path);
	    tree.scrollPathToVisible(path);
	}
	return foundNode;
    }

    // creates a new node and adds it to the currently selected node
    public ClassTreeNode addNodeToSelected(String nodeName) {
	ClassTreeNode newNode = new ClassTreeNode(new CIMObjectPath(nodeName));
	return addNodeToSelected(newNode);
    }

    // adds a node to the currently selected node
    public ClassTreeNode addNodeToSelected(ClassTreeNode newNode) {
	ClassTreeNode selectedNode = getSelectedNode();
	// insert node into tree
	DefaultTreeModel model = 
	    (DefaultTreeModel)tree.getModel();
	Enumeration e = selectedNode.children();
	int insertIndex = 0;
	// insert into sorted child list
	while (e.hasMoreElements()) {
	    if (newNode.toString().compareToIgnoreCase(
	        e.nextElement().toString()) < 0) {
		break;
	    }
	    insertIndex++;
	}
	model.insertNodeInto(newNode, selectedNode, insertIndex);
	// selected node cannot be leaf if it has a child
	selectedNode.setLeaf(false);
	return newNode;
    }

    protected class CustomBasicRenderer extends DefaultTreeCellRenderer {
	public Component getTreeCellRendererComponent(JTree theTree,
	    Object value, boolean selected, boolean expanded,
	    boolean leaf, int row, boolean hasFocus) {

	    Component c = super.getTreeCellRendererComponent(theTree,
		value, selected, expanded, leaf, row, hasFocus);

	    // is tree node is a leaf and not the root node,
	    // set it's icon to the class icon
	    if (c instanceof JLabel) {
		JLabel l = (JLabel) c;
		if (leaf && (row != 0)) {
		    l.setIcon(classIcon);
		}
	    }
	    return c;
	}
    }

    // represent tree node as a class.  When we add the class nodes to the
    // tree, we only show the top level. we do however need to know if there 
    // will be any sub node.  To do this we enumerate all sub class of the 
    // class represented by the node when the node is created.  If it has 
    // subclasses, we put those in the nodes child list and set the class as 
    // not explored and not a leaf node.  When the node is expanded, we add 
    // create node for it's children, add them to the tree, and set the node
    // as explored.
    class ClassTreeNode extends DefaultMutableTreeNode {
	protected boolean explored = false;
	protected boolean leaf = false;
	protected Enumeration childList = null; 

	public ClassTreeNode() {
	}

	// set the user object to the CIMObjectPath of the class then
	// set the list of subclasses
	public ClassTreeNode(CIMObjectPath op) {
	    setUserObject(op);
	    setChildList();
	}

	// to clear node, null out it's child list and set it as not explore
	public void clear() {
	    explored = false;
	    childList = null;
	}

	// returns the CIMObjectPath of this class
	public CIMObjectPath getObjectPath() {
	    return (CIMObjectPath)getUserObject();
	}

	/// returns the class name
	public String toString() {
	    CIMObjectPath op = (CIMObjectPath)getUserObject();
	    return op.getObjectName().toString();
	}

	// if the class has sublasses, it is not a leaf
	public boolean isLeaf() {
	    return leaf;
	}
	
	// sets whether the node is a leaf node or not
	public void setLeaf(boolean b) {
	    leaf = b;
	}

	// determine whether the node has been explored or not
	public boolean isExplored() {
	    return explored;
	}
	
	// sets the node as being explored
	public void setExplored() {
	    explored = true;
	}

	// sets whether node has been explored
	public void setExplored(boolean b) {
	    explored = b;
	}

	// gets list of subclasses.  Will be null if child list has never
	// been set or empty if children already were added to tree
	public Enumeration getChildList() {
	    return childList;
	}

	public void setChildList() {
	    try {
		// check for valid cimClient
		if (cimClient != null) {
		    // enumerate class names to get child list
		    Enumeration e;
		     e = cimClient.enumerateClassNames(getObjectPath(), 
						       false);
		    childList = Util.sortEnumeration(e);
		    // if has children, node not a leaf
		    leaf = !childList.hasMoreElements();
		} else {
		    leaf = true;
		}
	    } catch (CIMException exc) {
		CIMErrorDialog.display(thisPane, exc);
		return;
	    }
	}
    }

    // root node of the tree, label will be set instead of using CIMObjectPath 
    class RootTreeNode extends ClassTreeNode {
	private String label = "root";

	public RootTreeNode(CIMObjectPath op) {
	    super(op);
	}
	
	// set label for node (will be a namespace)
	public void setLabel(String l) {
	    label = l;
	}

	// show label 
	public String toString() {
	    return label;
	}

    }	
	    
}



