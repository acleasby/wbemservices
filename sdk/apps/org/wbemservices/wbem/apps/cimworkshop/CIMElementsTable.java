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

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.DefaultTableModel;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.util.EventObject;
import java.util.Vector;

import javax.wbem.cim.*;
import javax.wbem.client.CIMClient;

import org.wbemservices.wbem.apps.common.ActionString;
import org.wbemservices.wbem.apps.common.CancelObject;
import org.wbemservices.wbem.apps.common.CIMClientObject;
import org.wbemservices.wbem.apps.common.I18N;
import org.wbemservices.wbem.apps.common.Util;


/**
 *
 * @author 	Sun Microsystems
 */


public class CIMElementsTable extends JScrollPane implements
    ListSelectionListener, TableModelListener, MouseListener,
    ActionListener {

    public final static int PROPERTY_TABLE = 0;
    public final static int QUALIFIER_TABLE = 1;
    public final static int NON_EDITABLE = 0;
    public final static int EDITABLE = 1;
    public final static int ONLY_VALUE_EDITABLE = 2;

    protected JTable table;
    protected Vector tableHeading;
    protected Vector cimElements =  null;
    protected CIMElement parentElement = null;
    protected ElementsDataModel elementsDataModel;
    protected String emptyString;
    protected boolean dataChanged = false;
    protected boolean isClass = false;
    protected boolean newElement = false;
    protected int tableType;
    protected CIMValue currentCIMValue;
    protected int accessState;
    protected JPopupMenu popupMenu;
    protected CIMClient cimClient;

    protected ImageIcon keyIcon;
    protected ImageIcon inheritedIcon;
    protected ImageIcon blankIcon;
    protected CIMTypes cimTypes;

    private final int INHERITED_COLUMN = 0;
    private final int KEY_COLUMN = 1;
    private int nameColumn;
    private int typeColumn;
    private int valueColumn;

    protected Vector uneditableRows;

    public CIMElementsTable() {
	this(1, 0, false);
    }

    public CIMElementsTable(int tType) {
	this(NON_EDITABLE, tType);
    }

    public CIMElementsTable(int tType, boolean nElement) {
	this(NON_EDITABLE, tType, nElement);
	if (nElement) {
	    accessState = ONLY_VALUE_EDITABLE;
	}
    }

    public CIMElementsTable(int aState, int tType) {
	this(aState, tType, false);
    }

    public CIMElementsTable(int aState, int tType, boolean nElement) {
	super();
	accessState = aState;
	tableType = tType;
	newElement = nElement;

	if (tableType == PROPERTY_TABLE) {
	    nameColumn = 2;
	} else {
	    nameColumn = 0;
	}
	typeColumn = nameColumn + 1;
	valueColumn = nameColumn + 2;



	uneditableRows = new Vector();
	cimTypes = new CIMTypes();
	emptyString = I18N.loadString("LBL_EMPTY");
	cimElements = new Vector();
	keyIcon = Util.loadImageIcon("key.gif");
	blankIcon = Util.loadImageIcon("blank.gif");
	inheritedIcon = Util.loadImageIcon("inherited.gif");


	// create popup menu and add listener;
	popupMenu = new JPopupMenu();


	elementsDataModel = new ElementsDataModel();
	tableHeading = new Vector();
	if (tableType == PROPERTY_TABLE) {
    	    tableHeading.addElement("");
    	    tableHeading.addElement("");
	}
	ActionString asName = new ActionString("LBL_NAME",
	    "org.wbemservices.wbem.apps.common.common");
	ActionString asType = new ActionString("LBL_TYPE");
	ActionString asValue = new ActionString("LBL_VALUE");

	tableHeading.addElement(asName.getString());
	tableHeading.addElement(asType.getString());
	tableHeading.addElement(asValue.getString());

	table = new JTable(elementsDataModel) {
	    public String getToolTipText(MouseEvent e) {
		String s = "";
		Point p = e.getPoint();
		int col = columnAtPoint(p);
		int row = rowAtPoint(p);
		if ((row < 0) || (row >= elementsDataModel.getRowCount())) {
		    return "";
		}
		Object obj = elementsDataModel.getValueAt(row, col);
		String name = elementsDataModel.getValueAt(
				row, nameColumn).toString();
		if ((col >= nameColumn) && (col <= valueColumn)) {
		    s = obj.toString();
		} else if (col == INHERITED_COLUMN) {
		    if (obj == inheritedIcon) {
			s = I18N.loadStringFormat("MSG_PROP_INHERITED", name);
		    } else {
			s = I18N.loadStringFormat("MSG_PROP_NOT_INHERITED",
						  name);
		    }
		} else if (col == KEY_COLUMN) {
		    if (obj == keyIcon) {
			s = I18N.loadStringFormat("MSG_PROP_KEY", name);
		    } else {
			s = I18N.loadStringFormat("MSG_PROP_NOT_KEY", name);
		    }
		}
		return s;
	    }
	};
	table.setBackground(this.getBackground());
	table.getSelectionModel().addListSelectionListener(this);

	table.getSelectionModel().setSelectionMode(
	    ListSelectionModel.SINGLE_SELECTION);
	table.getTableHeader().setReorderingAllowed(false);
	table.setCellSelectionEnabled(true);

	elementsDataModel.addTableModelListener(this);

	JTextField keyField = new JTextField();
	JTextField inheritedField = new JTextField();
	JTextField nameField = new JTextField();
	JTextField typeField = new JTextField();
	JTextField valueField = new JTextField();



	if (tableType == PROPERTY_TABLE) {

	    TableColumn col0 = table.getColumnModel().getColumn(0);
	    TableColumn col1 = table.getColumnModel().getColumn(1);

	    // set key and inherited columns to set size
	    col0.setMinWidth(20);
	    col0.setMaxWidth(20);
	    col1.setMinWidth(20);
	    col1.setMaxWidth(20);

	    // sizeColumnsToFit must be called due to a JTable bug
	    table.sizeColumnsToFit(0);

	    col0.setCellEditor(
		new DefaultCellEditor(inheritedField) {
		    public boolean isCellEditable(EventObject evt) {
			return false;
		    }
		});

	    col1.setCellEditor(
		new DefaultCellEditor(keyField) {
		    public boolean isCellEditable(EventObject evt) {
			return false;
		    }
		});
	}

	table.getColumnModel().getColumn(nameColumn).setCellEditor(
	    new DefaultCellEditor(nameField) {
		public boolean isCellEditable(EventObject evt) {
		    return false;
		}
	    });


	table.getColumnModel().getColumn(typeColumn).setCellEditor(
	    new CIMTypeEditor(typeField));

	TableColumn col3 = table.getColumnModel().getColumn(valueColumn);
	col3.setCellEditor(new CIMTextFieldCellEditor(valueField));

	table.setPreferredScrollableViewportSize(new Dimension(300, 200));
	setViewportView(table);

    }

// BUGFIX. Accessibility fixes
    public JTable getTable() {
	return table;
    }
//
    public void hideIconColumns() {
	TableColumnModel tcm = table.getColumnModel();
	TableColumn col0 = table.getColumnModel().getColumn(0);
	TableColumn col1 = table.getColumnModel().getColumn(1);
	tcm.removeColumn(col0);
	tcm.removeColumn(col1);
	// sizeColumnsToFit must be called due to a JTable bug
	table.sizeColumnsToFit(0);
    }

    public void actionPerformed(ActionEvent evt) {
	String actionCmd = evt.getActionCommand();
	if (actionCmd.equals("SHOW_VALUE")) {
// BUGFIX. Accessibility fixes
	    showValue();
	}
    }

    public void showValue() {
	String name = null;
	CIMDataType dataType = null;
	Object value = null;

	CIMElement currentElement = (CIMElement)cimElements.elementAt(
				    getSelectedRow());
	name = currentElement.getName();
	if (tableType == PROPERTY_TABLE) {
	    dataType = ((CIMProperty)currentElement).getType();
	    CIMValue cv = ((CIMProperty)currentElement).getValue();
	    if (cv != null) {
		value =  cv.getValue();
	    } else {
		value = null;
	    }
	} else if (tableType == QUALIFIER_TABLE) {
	    CIMValue cv = ((CIMQualifier)currentElement).getValue();
	    if (cv != null) {
		dataType = cv.getType();
		value =  cv.getValue();
	    } else {
		value = null;
	    }
	}
	CIMValueDialog.showDialog(Util.getFrame(table), value,
				      name, dataType, false);
    }
// BUGFIX END. Accessibility fixes

    // can specifically specify row numbers that cannot be edited
    public void setUneditableRows(Vector v) {
	uneditableRows = v;
    }

    public void addTableListener(TableModelListener tml) {
	elementsDataModel.addTableModelListener(tml);
    }

    public void addListSelectionListener(ListSelectionListener lsl) {
	table.getSelectionModel().addListSelectionListener(lsl);
    }

    protected void setParentElement(CIMElement parent) {
	parentElement = parent;
    }

    protected void deleteElement(int row) {
	if (tableType == PROPERTY_TABLE) {
	    CIMProperty prop = (CIMProperty)cimElements.elementAt(row);
	} else if (tableType == QUALIFIER_TABLE) {
	    CIMQualifier prop = (CIMQualifier)cimElements.elementAt(row);

	}
    }

    public int getSelectedRow() {
	return table.getSelectedRow();
    }

    public int getSelectedColumn() {
	return table.getSelectedColumn();
    }

    public boolean isNameDefined(String name) {
	if (name != null) {
	    for (int i = 0; i < cimElements.size(); i++) {
		String nameVal = (String)
				 elementsDataModel.getValueAt(i, nameColumn);
		if (name.equalsIgnoreCase(nameVal)) {
		    return true;
		}
	    }
	}
	return false;
    }

    class ElementsDataModel extends DefaultTableModel {

	public int getColumnCount() {
	    if (tableHeading != null) {
		return tableHeading.size();
	    } else {
		return 0;
	    }
	}

	public Object getValueAt(int row, int col) {
	    if (tableType == PROPERTY_TABLE) {
		CIMProperty prop = (CIMProperty)cimElements.elementAt(row);
		if (prop != null) {
		    if (col == nameColumn) {
			return prop.getName();
		    } else if (col == typeColumn) {
			return Util.getDataTypeString(prop.getType());
		    } else if (col == valueColumn) {
			CIMValue cv = prop.getValue();
			String val =  emptyString;
			if (cv != null) {
			    if (cv.getValue() != null) {
				val = cv.getValue().toString();
				if (val.equals("")) {
				    val =  emptyString;
				}
			    }
			}
			return val;
		    } else if (col == INHERITED_COLUMN) {
			String origClass = prop.getOriginClass();
			if ((origClass == null) || (parentElement == null)) {
			    return blankIcon;
			} else if (parentElement.getName().equals(origClass)) {
			    return blankIcon;
			} else {
			    return inheritedIcon;
			}

		    } else if (col == KEY_COLUMN) {
			if (prop.isKey()) {
			    return keyIcon;
			} else {
			    return blankIcon;
			}
		    }
		}
	    } else if (tableType == QUALIFIER_TABLE) {
		CIMQualifier prop = (CIMQualifier)cimElements.elementAt(row);
		if (prop != null) {
		    if (col == nameColumn) {
			return prop.getName();
		    } else if (col == typeColumn) {
			try {
			    CIMQualifierType cqt = cimClient.getQualifierType(
			        new CIMObjectPath(prop.getName()));
			    return Util.getDataTypeString(cqt.getType());
			} catch (Exception e) {
			    return "NULL";
			}
		    } else if (col == valueColumn) {
			CIMValue cv = prop.getValue();
			String val =  emptyString;
			if (cv != null) {
			    if (cv.getValue() != null) {
				val = cv.getValue().toString();
				if (val.equals("")) {
				    val =  emptyString;
				}
			    }
			}
			return val;
		    } else {
			return blankIcon;
		    }
		}
	    }
	    return emptyString;
	}

	public void setValueAt(Object value, int row, int col) {
	    if (tableType == PROPERTY_TABLE) {
		CIMProperty prop = (CIMProperty)cimElements.elementAt(row);
		if (prop == null) {
		    prop = new CIMProperty();
		}

		if (col == nameColumn) {
		    prop.setName((String)value);
		} else if (col == typeColumn) {
		    CIMDataType cdt;
		    if (((String)value).indexOf('_') > 0) {
			cdt = new CIMDataType((String)value);
		    } else {
			cdt = new CIMDataType(cimTypes.getCIMType(
					      (String)value));
		    }
		    prop.setType(cdt);
		} else if (col == valueColumn) {
		    prop.setValue(new CIMValue(value));
		}
		cimElements.setElementAt(prop, row);
	    } else if (tableType == QUALIFIER_TABLE) {
		CIMQualifier prop = (CIMQualifier)cimElements.elementAt(row);
		if (prop == null) {
		    prop = new CIMQualifier();
		}
		if (col == valueColumn) {
		    prop.setValue(new CIMValue(value));
		}
		cimElements.setElementAt(prop, row);
	    }
	    fireTableCellUpdated(row, col);
	}


	public String getColumnName(int col) {
	    return (String)tableHeading.elementAt(col);
	}

	public Class getColumnClass(int col) {
	    return getValueAt(0, col).getClass();
	}

	public boolean isCellEditable(int row, int col) {
	    boolean b = false;
	    if ((col != valueColumn) ||
		(!CIMClientObject.userHasWritePermission()) ||
		(uneditableRows.contains(new Integer(row))) ||
		(accessState == NON_EDITABLE)) {
		b = false;
	    } else if (newElement) {
		b = true;
	    } else if (parentElement instanceof CIMInstance) {
		CIMElement currentElement = (CIMElement)
		    cimElements.elementAt(row);
		if ((tableType == PROPERTY_TABLE) &&
		    (((CIMProperty)currentElement).isKey())) {
		    b = false;
		} else {
		    b = true;
		}
	    }
	    return b;
	}

    }

    public void valueChanged(ListSelectionEvent e) {
    }

// BUGFIX. Accessibility fixes
    public boolean isSelectionEmpty() {
	return ((table == null) || (table.getSelectedRow() < 0));
    }

    protected void createPopupMenu(Point point) {

	int currentRow = table.rowAtPoint(point);
	table.setRowSelectionInterval(currentRow, currentRow);
	Point vpLocation = getViewport().getViewPosition();
	popupMenu.show(this, (point.x - vpLocation.x + 10),
			     (point.y - vpLocation.y));
    }


    public void mousePressed(MouseEvent evt) {
	if (evt.isPopupTrigger()) {
	    createPopupMenu(evt.getPoint());
	}
    }

    public void mouseReleased(MouseEvent evt) {
	if (evt.isPopupTrigger()) {
	    createPopupMenu(evt.getPoint());
	}
    }

    public void mouseClicked(MouseEvent evt) {
	if (evt.isPopupTrigger()) {
	    createPopupMenu(evt.getPoint());
	}
    }

    public void mouseEntered(MouseEvent evt) {
    }


    public void mouseExited(MouseEvent evt) {
    }


    public void tableChanged(TableModelEvent e) {
    }



    public void setAccessState(int as) {
	accessState = as;
    }


    public void windowClosing(WindowEvent evt) {
    }

    public void windowOpened(WindowEvent evt) {
    }

    public void windowClosed(WindowEvent evt) {
    }

    public void windowDeiconified(WindowEvent evt) {
    }

    public void windowActivated(WindowEvent evt) {
    }

    public void windowIconified(WindowEvent evt) {
    }

    public void windowDeactivated(WindowEvent evt) {
    }


// **********************************************************************
    public class CIMTextFieldCellEditor extends DefaultCellEditor {

	String currentName;
	CIMDataType currentDataType = null;
	Object currentValue;
	CIMElement currentElement;
	int currentRow;
	boolean isKey;

	public CIMTextFieldCellEditor(JTextField textField) {
	    super(textField);
	}

	public Component getTableCellEditorComponent(JTable tbl,
	    Object value, boolean isSelected, int row, int column) {

	    currentRow = row;

	    ((JTextField)editorComponent).setEditable(isTextFieldEditable());

	    currentElement = (CIMElement)cimElements.elementAt(row);
	    currentName = currentElement.getName();
	    if (tableType == PROPERTY_TABLE) {
		currentDataType = ((CIMProperty)currentElement).getType();
		CIMValue cv = ((CIMProperty)currentElement).getValue();
		if (cv != null) {
		    currentValue =  cv.getValue();
		} else {
		    currentValue = null;
		}
		isKey = ((CIMProperty)currentElement).isKey();
	    } else if (tableType == QUALIFIER_TABLE) {
		CIMValue cv = ((CIMQualifier)currentElement).getValue();
		if (cv != null) {
		    currentDataType = cv.getType();
		    currentValue =  cv.getValue();
		} else {

		    // try to find the qualifier type
		    try {
			CIMQualifierType cqt = cimClient.getQualifierType(
			    new CIMObjectPath(currentName));
			if (cqt != null) {
			    currentDataType = cqt.getType();
			}
		    } catch (Exception e) {
			// ignore, we'll fix it later
		    }
		    currentValue = null;
		    // as a last resort, use string data type
		    if (currentDataType == null) {
			currentDataType = new CIMDataType(CIMDataType.STRING);
		    }
		}
		isKey = false;
	    }
	    return editorComponent;
	}

	public Object getCellEditorValue() {
	    return currentValue;
	}

	public boolean isCellEditable(EventObject evt) {
    	    return true;
	}

	public boolean stopCellEditing() {
	    return true;
	}

	public void cancelCellEditing() {
	}

	public boolean shouldSelectCell(EventObject evt) {
	    if (table.getModel().isCellEditable(currentRow, valueColumn)) {
		currentValue = CIMValueDialog.showDialog(Util.getFrame(table),
							 currentValue,
							 currentName,
							 currentDataType, true);
		// if user clicked Cancel button, currentValue will
		// be of type CancelObject
		if (currentValue instanceof CancelObject) {
		    fireEditingCanceled();
		} else {
		    fireEditingStopped();
		}
	    }
	    return false;
	}

	public boolean isTextFieldEditable() {
	    boolean ret = false;
	    return ret;
	}

	public void showDialog() {

	}



    }




// **********************************************************************
    public class CIMTypeEditor extends DefaultCellEditor {

	String currentName;
	String currentType;
	CIMElement currentElement;

	int currentRow;

	public CIMTypeEditor(JTextField textField) {
	    super(textField);
	}

	public Component getTableCellEditorComponent(JTable tbl,
	    Object value, boolean isSelected, int row, int column) {

	    currentRow = row;

	    ((JTextField)editorComponent).setEditable(false);

	    currentElement = (CIMElement)cimElements.elementAt(row);
	    currentName = currentElement.getName();
	    if (tableType == PROPERTY_TABLE) {
		CIMDataType cdt = ((CIMProperty)currentElement).getType();
		currentType = Util.getDataTypeString(cdt);
	    } else if (tableType == QUALIFIER_TABLE) {
		CIMValue cv = ((CIMQualifier)currentElement).getValue();
		if (cv != null) {
		    currentType = Util.getDataTypeString(cv.getType());
		} else {
		    currentType = "Null";
		}
	    }
	    return editorComponent;
	}

	public Object getCellEditorValue() {
	    return currentType;
	}

	public boolean isCellEditable(EventObject evt) {
	    if (!CIMClientObject.userHasWritePermission()) {
		return false;
	    }
	    if (accessState == EDITABLE) {
		if (isClass && (currentElement instanceof CIMProperty)) {
		    if (((CIMProperty)currentElement).getOriginClass() !=
							null) {
			return false;
		    }
		}
		return true;
	    } else {
		table.setRowSelectionInterval(currentRow, currentRow);
    		return false;
	    }
	}

	public boolean stopCellEditing() {
	    return true;
	}

	public void cancelCellEditing() {
	}

	public boolean shouldSelectCell(EventObject evt) {
	    if (isCellEditable(evt)) {
		showDialog();
	    }
	    return false;
	}

	public void showDialog() {
	    CIMTypeDialog dlg = new CIMTypeDialog(Util.getFrame(table),
						  currentType);

	    String newType = dlg.getSelectedType();
	    if ((newType == null) || newType.equals(currentType)) {
		fireEditingCanceled();
	    } else {
		currentType = newType;
		// XXX need to set value to empty
		fireEditingStopped();
	    }
	}


    }

}
