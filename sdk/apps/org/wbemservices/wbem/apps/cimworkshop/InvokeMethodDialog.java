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
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import java.awt.Frame;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.EventObject;
import java.util.Enumeration;

import org.wbemservices.wbem.apps.common.*;

import javax.wbem.cim.*;
import javax.wbem.client.CIMClient;

/**
 * 
 *
 * @version 	1.23, 04/10/01
 * @author 	Sun Microsystems
 */

/**
 * This class allows a user to invoke a CIM method.  It displays a table
 * that has 1 row for each parameter in the method.  Each row has five columns;
 * these are for parameter name, CIMType, parameter type (input, output or
 * both), input value and output value.  When the user supplys values for all
 * input parameters, they can invoke the method.  The return values for the
 * ouput parameters are displayed in the table.  The return value for the method
 * is displayed after the "Return value" label.
 *
 */
public class InvokeMethodDialog extends AdminDialog implements 
    TableModelListener {

    protected JButton invokeBtn, closeBtn;    
    protected ParametersDataModel paramDataModel;
    protected JTable paramTable;
    protected String[] tableHeading;

    protected ArrayList methodParams;
    protected Frame parentFrame;
    protected JLabel returnValue;

    protected CIMMethod cimMethod;
    protected CIMObjectPath cimObjectPath;
    protected CIMTypes cimTypes;

    private final int INPUT_PARAM	    = 1;
    private final int OUTPUT_PARAM	    = 2;
    private final int INPUT_OUTPUT_PARAM    = 3;

    public InvokeMethodDialog(Frame parent, CIMMethod cm, 
			      CIMObjectPath op) {
	super(parent, I18N.loadStringFormat("TTL_INVOKE_METHOD", 
					    cm.getName()), false);
	// info panel is used to display online help.
	GenInfoPanel infoPanel = this.getInfoPanel();
	ActionString asInvoke = new ActionString("MNU_INVOKE_METHOD");
	invokeBtn = this.getOKBtn();
	invokeBtn.addActionListener(new ButtonListener());
	invokeBtn.setText(asInvoke.getString());
	invokeBtn.setMnemonic(asInvoke.getMnemonic());
	invokeBtn.setActionCommand("INVOKE_METHOD");
	closeBtn = this.getCancelBtn();
	closeBtn.addActionListener(new ButtonListener());
	closeBtn.setText(I18N.loadString("LBL_CLOSE",
			  "org.wbemservices.wbem.apps.common.common"));	
	JPanel mainPanel = getRightPanel();	
	
	cimMethod = cm;
	cimObjectPath = op;
	parentFrame = parent;

	// cimTypes is used to convert CIMDataType between int and string values
	cimTypes = new CIMTypes();
	mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 15, 15, 15));
	mainPanel.setLayout(new ColumnLayout(LAYOUT_ALIGNMENT.EXPAND));

	JPanel retValPanel = new JPanel(new RowLayout());
	retValPanel.add(new JLabel(I18N.loadString("LBL_RETURN_VALUE") + ": "));
	returnValue = new JLabel();
	retValPanel.add(returnValue);
	mainPanel.add(retValPanel);
	

	ActionString asName = new ActionString("LBL_NAME",
			       "org.wbemservices.wbem.apps.common.common");
	ActionString asCIMType = new ActionString("LBL_CIM_TYPE");
	ActionString asParamType = new ActionString("LBL_PARAM_TYPE");
	ActionString asInputValue = new ActionString("LBL_INPUT_VALUE");
	ActionString asOutputValue  = new ActionString("LBL_OUTPUT_VALUE");
	tableHeading = new String[5];
	tableHeading[0] = asName.getString();
	tableHeading[1] = asCIMType.getString();
	tableHeading[2] = asParamType.getString();
	tableHeading[3] = asInputValue.getString();
	tableHeading[4] = asOutputValue.getString();

	paramDataModel = new ParametersDataModel();
	paramDataModel.addTableModelListener(this);
	paramTable = new JTable(paramDataModel) {
	    public String getToolTipText(MouseEvent e) {
		Point p = e.getPoint();
		int col = columnAtPoint(p);
		int row = rowAtPoint(p);
		if ((row < 0) || (row >= paramDataModel.getRowCount())) {
		    return "";
		}
		String s = paramDataModel.getValueAt(row, col).toString();
		if (s.trim().length() == 0) {
		    return null;
		} else {
		    return s;
		}
	    }
	};

	paramTable.setBackground(this.getBackground());

	paramTable.getSelectionModel().setSelectionMode(
	    ListSelectionModel.SINGLE_SELECTION);
	paramTable.getTableHeader().setReorderingAllowed(false);
	paramTable.setCellSelectionEnabled(true);

	ActionString asParams = new ActionString("LBL_PARAMETERS");
	JLabel lbParams = new JLabel(asParams.getString() + ":");
	// lbParams.setDisplayedMnemonic(asParams.getMnemonic());
	// lbParams.setLabelFor(paramTable);
	JScrollPane scrollPane = new JScrollPane(paramTable);
	mainPanel.add(Box.createVerticalStrut(20));
	mainPanel.add(lbParams);
	mainPanel.add(scrollPane);

	// make all columns non-editable
	for (int i = 0; i < 5; i++) {
	    TableColumn col = paramTable.getColumnModel().getColumn(i);
	    col.setCellEditor(
		new DefaultCellEditor(new JTextField()) {
		    public boolean isCellEditable(EventObject evt) {
			return false;
		    }
		});
	}

	TableColumn inputColumn = paramTable.getColumnModel().getColumn(3);
	inputColumn.setCellEditor(new InputParamCellEditor(new JTextField()));
	// setup URLS for help
	this.setDefaultFocusListener(new ContextHelpListener(
		infoPanel, "cimworkshop", "Methods_000.htm"), true);

	paramTable.addFocusListener(new ContextHelpListener(
		infoPanel, "cimworkshop", "Methods_010.htm"));		

	initialize(cimMethod);
	invokeBtn.setEnabled(isInputComplete());

	pack();
	setLocation(Util.getCenterPoint(parent, this));
	show();

    }


    /**
     * Checks to make sure all input parameters have values
     *
     * @return boolean  True if all input parameters have input values,
     *                  otherwise False
     */
    public boolean isInputComplete() {
	// get all parameters
	Iterator iter = methodParams.iterator();
	while (iter.hasNext()) {
	    // for each parameter, get input type
	    Object[] params = (Object[])iter.next();
	    int inputType = ((Integer)params[2]).intValue();
	    // if any of the input parameters are null, return false
	    if ((inputType & INPUT_PARAM) != 0) {
		if (params[3] == null) {
		    return false;
		}
	    }

	}
	// all input parameters have values
	return true;
    }

    /**
     * Automatically called when table is changed.  When table is changed,
     * we need to enable or disable invoke button depending on whether or not 
     * all input parameters have input values.
     */
    public void tableChanged(TableModelEvent e) {
	invokeBtn.setEnabled(isInputComplete());
    }


    /**
     * This sets up the table with all the parameter information from the 
     * CIMMethod.
     *
     * @param method   CIMMethod to be invoked.
     */
    private void initialize(CIMMethod method) {
	// methodParams is an ArrayList that will contains a list of Object 
	// arrays.  This is used to keep track of the parameter values
	// The first object array contains the values for the first parameter,
	// the second for the second parameter, etc.

	methodParams = new ArrayList();

	// get a Vector of method parameters from the CIMMethod object
	Vector params = method.getParameters();
	for (Enumeration e = params.elements(); e.hasMoreElements(); ) {
	    // paramInfo will contain information about each parameter.  This
	    // includes the param name, CIMtype, paramtype (input, output or
	    // both), input value an output value.
	    Object[] paramInfo = new Object[5];
	    CIMParameter cimParam = (CIMParameter)e.nextElement();
	    paramInfo[0] = cimParam.getName();
	    paramInfo[1] = cimParam.getType();

	    // check if parameter is input, output or both
	    int inOutType = 0;
	    if (isQualifierTrue(cimParam, "IN")) {
		inOutType = inOutType | INPUT_PARAM;
	    }
	    if (isQualifierTrue(cimParam, "OUT")) {
		inOutType = inOutType | OUTPUT_PARAM;
	    }
	    
	    paramInfo[2] = new Integer(inOutType);
	    // set input value to null
	    paramInfo[3] = null;
	    // set output value to null
	    paramInfo[4] = null;
	    methodParams.add(paramInfo);
	}

	// tell dataModel how many rows to draw
	paramDataModel.setNumRows(methodParams.size());
	paramTable.repaint();

    }

    // 
    // 
    /**
     * Checks a CIMParameter to see if it contains a certain qualifier
     * with a true value
     *
     * @param param      The CIMParameters to check qualifier for
     * @param name       The name of the qualifier we are checking
     * @return boolean   True if the qualifier exists and has a True value,
     *                   otherwise False
     */
    private boolean isQualifierTrue(CIMParameter param, String name) {
	boolean ret = false;
	// check for qualifier
	CIMQualifier qual = param.getQualifier(name);
	if (qual != null) {
	    // if qualifier exists, check if it has a TRUE value
	    CIMValue cv = qual.getValue();
	    if (cv != null) {
		if (cv.equals(CIMValue.TRUE)) {
		    ret = true;
		}
	    }
	}
	return ret;
    }

    /**
     * This method gets invokes the CIMMethod.
     *
     */
    private void invokeMethod()  {
	// get a CIMArgument array  of the input parameters
	CIMArgument[] input = getInputParams();
	// create a CIMArgument array to hold output parameters 
	CIMArgument[] output = new CIMArgument[methodParams.size()];
	CIMClient client = CIMClientObject.getClient();
	try {
	    // call invoke method
	    CIMValue retVal = client.invokeMethod(cimObjectPath, 
		    cimMethod.getName(), input, output);
	    if (retVal != null) {
		returnValue.setText(retVal.toString());
	    } else {
		returnValue.setText(I18N.loadString("LBL_NULL",
		    "org.wbemservices.wbem.apps.common.common"));
	    }
	    // put ouput values in table
	    displayOutputValues(output);
	} catch (CIMException exc) {
	    CIMErrorDialog.display(this, exc);
	}
    }

    /**
     * Checks all parameters for those that are input parameters and returns
     * an array of CIMArguments that contain the input values
     *
     * @return array of CIMArguments
     */
    private CIMArgument[] getInputParams() {
	ArrayList argList = new ArrayList();
	// get all paramters
	Iterator iter = methodParams.iterator();
	while (iter.hasNext()) {
	    // get each parameter
	    Object[] param = (Object[])iter.next();
	    int inputType = ((Integer)param[2]).intValue();
	    // if it's an input parameter, add it's value to the return array
	    if ((inputType & INPUT_PARAM) != 0) {
		CIMArgument arg = new CIMArgument((String)param[0]);
		arg.setType((CIMDataType)param[1]);
		CIMValue value = (CIMValue)param[3];
		if (value != null) {
		    arg.setType(value.getType());
		    arg.setValue(value);
		}
		argList.add(arg);
	    }
	}
	return (CIMArgument[])argList.toArray(new CIMArgument[argList.size()]);
    }

    /**
     * updates the table with the output values from the method invocation
     *
     * @param args   Vector containing output values
     */
    private void displayOutputValues(CIMArgument args[]) {
	int argIndex = 0;
	// step through all parameters
	for (int i = 0; i < methodParams.size(); i++) {
	    Object[] param = (Object[])methodParams.get(i);
	    int outputType = ((Integer)param[2]).intValue();
	    // check if current parameter is an output parameter.  If it is,
	    // set it's value to the value of the next available CIMArgument 
	    // in the output values array
	    if ((outputType & OUTPUT_PARAM) != 0) {
		CIMArgument arg = args[argIndex++];
		if (arg != null) {
		    param[4] = arg.getValue();
		}
	    }
	}
	// repainting table will update the new values
	paramTable.repaint();
    }
	


    /**
     *  DataModel for the parameters table
     *
     */
    class ParametersDataModel extends DefaultTableModel {

	public int getColumnCount() {
	    return 5;
	}

	public Object getValueAt(int row, int col) {
	    // row will equal array list index
	    Object[] param = (Object[])methodParams.get(row);
	    Object ret = null;
	    if (param != null) {
		// value will be index into paramter array
		ret = param[col];
		if (ret != null) {
		    // if it's second column, display CIMDataType string
		    if (col == 1) {
			int type = CIMDataType.NULL;
			CIMDataType cdt = (CIMDataType)ret;
			if (cdt != null) {
			    type = cdt.getType();
			}
			ret = cimTypes.getCIMType(type);
		    }
		    // if it's third column, display correct input/ouput string
		    if (col == 2) {
			switch (((Integer)ret).intValue()) {
			case 1:
			    ret = I18N.loadString("LBL_INPUT");
			    break;
			case 2:
			    ret = I18N.loadString("LBL_OUTPUT");
			    break;
			case 3:
			    ret = I18N.loadString("LBL_INPUT_OUTPUT");
			    break;
			default:
			    ret = "";

			}
		    } else if ((col == 3) || (col == 4)) {
			String val =  "";
			CIMValue cv = (CIMValue)ret;
			if (cv != null) {
			    val = cv.getValue().toString();
			}
			ret = val;
		    }
		}

	    }
	    if (ret == null) {
		ret = "";
	    }
	    return ret;
	}

	public void setValueAt(Object value, int row, int col) {
	    Object[] param = (Object[])methodParams.get(row);
	    param[col] = value;
	    fireTableCellUpdated(row, col);
	}


	public String getColumnName(int col) {
	    return tableHeading[col];
	}

	public Class getColumnClass(int col) {
	    return getValueAt(0, col).getClass();
	}

    }


    public class InputParamCellEditor extends DefaultCellEditor {
	Object currentValue;
	String currentName;
	CIMDataType currentDataType = null;
	boolean isInputType = true;

	public InputParamCellEditor(JTextField textField) {
	    super(textField);
	}

	public Component getTableCellEditorComponent(JTable table, 
	    Object value, boolean isSelected, int row, int column) {



	    ((JTextField)editorComponent).setEditable(false);

	    // get values ar correct row
	    Object[] param = (Object[])methodParams.get(row);
	    currentName = (String)param[0];
	    currentDataType = (CIMDataType)param[1];
	    currentValue = param[3];
	    // decide if we have input type, this value will default used to
	    // decide if we need to show value dialog
	    isInputType = (((Integer)param[2]).intValue() & INPUT_PARAM) != 0;
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

	    // if input type, show value dialog
	    if (isInputType) {
		Object value = null;
		if (currentValue != null) {
		    value = ((CIMValue)currentValue).getValue();
		}
		value = CIMValueDialog.showDialog(
                    Util.getFrame(paramTable),
		    value, currentName,
		    currentDataType, true);
		// if user clicked cancel
		if (value instanceof CancelObject) {
		    fireEditingCanceled();
		} else {
		    // set return value from value dialog
		    if (value != null) {
			currentValue = new CIMValue(value);
		    }
		    fireEditingStopped();
		}
	    }
	    return false;
	}
    }
 
    class ButtonListener implements ActionListener {
	public void actionPerformed(ActionEvent e) {
	    if (e.getSource() == invokeBtn) { // invoke button
		invokeMethod();
	    } else if (e.getSource() == closeBtn) { // close button
		dispose();
	    }
	}
    }

}
