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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.CardLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.GregorianCalendar;
import org.wbemservices.wbem.apps.common.*;

/**
 * 
 *
 * @author 	Sun Microsystems
 */

class DateTimeDialog extends CIMEditDialog implements DocumentListener {

    JIntSpinBox sbYear;
    JIntSpinBox sbMonth;
    JIntSpinBox sbDay;
    JIntSpinBox sbDays;
    JIntSpinBox sbHour;
    JIntSpinBox sbMin;
    JIntSpinBox sbSec;
    JIntSpinBox sbMicroSec;
    JIntSpinBox sbUTC;

    JComboBox cbUTC;
    JRadioButton rbDate;
    JRadioButton rbInterval;
    JRadioButton rbNoValue;
    JRadioButton rbTmp;

    JPanel pDate;
    JPanel pDays;
    JPanel pDateDays;
    
    private CardLayout card;

    public DateTimeDialog(Frame parent, String name, String type, 
			  String value) {
	this(parent, name, type, value, true);
    }

    public DateTimeDialog(Frame parent, String name, String type, 
			  String value, boolean enabled) {
	super(parent, name, type, enabled);
	
	card = new CardLayout();
	
	String tmpValue = value;
	if (tmpValue == null) {
	    tmpValue = "";
	}
	
	JPanel frontPane = getFrontPane(tmpValue, enabled);	
	
	String defaultHelp = "ShowValue_000.htm";	
	if (isEditable) {
	    defaultHelp = "DateTime_000.htm";
	}

	// set up help files
	setDefaultHelp(defaultHelp);
	setSpinBoxHelp(sbYear, "DateTime_010.htm");
	setSpinBoxHelp(sbMonth, "DateTime_020.htm");
	setSpinBoxHelp(sbDay, "DateTime_030.htm");
	setSpinBoxHelp(sbHour, "DateTime_040.htm");
	setSpinBoxHelp(sbMin, "DateTime_050.htm");
	setSpinBoxHelp(sbSec, "DateTime_060.htm");
	setSpinBoxHelp(sbMicroSec, "DateTime_070.htm");
	setSpinBoxHelp(sbUTC, "DateTime_080.htm");
	setSpinBoxHelp(sbDays, "DateTime_090.htm");

	
	JPanel mainPanel = this.getMainPanel();
	mainPanel.add(frontPane);

	setOKEnabled();
	pack();
	setLocation(Util.getCenterPoint(parent, this));
	setVisible(true);
    }


    protected JPanel getFrontPane(String value, boolean enabled) {
	
	JPanel pane  = new JPanel();

	ActionString asDate = new ActionString("LBL_DATE");
	ActionString asInterval = new ActionString("LBL_INTERVAL");
	ActionString asNoValue = new ActionString("LBL_NO_VALUE",
				     "org.wbemservices.wbem.apps.common.common");
	ActionString asYear = new ActionString("LBL_YEAR");
	ActionString asMonth = new ActionString("LBL_MONTH");
	ActionString asDay = new ActionString("LBL_DAY");
	ActionString asDays = new ActionString("LBL_DAYS");
	ActionString asHour = new ActionString("LBL_HOUR");
	ActionString asMin = new ActionString("LBL_MINUTES");
	ActionString asSec = new ActionString("LBL_SECONDS");
	ActionString asMicroSec = new ActionString("LBL_MICRO_SECONDS");
	ActionString asUTC = new ActionString("LBL_UTC");


	ButtonGroup group = new ButtonGroup();
	rbDate = new JRadioButton(asDate.getString());
	rbDate.setMnemonic(asDate.getMnemonic());
	rbDate.addActionListener(new RadioButtonListener());
	rbInterval = new JRadioButton(asInterval.getString());
	rbInterval.setMnemonic(asInterval.getMnemonic());
	rbInterval.addActionListener(new RadioButtonListener());
	rbNoValue = new JRadioButton(asNoValue.getString());
	rbNoValue.setMnemonic(asNoValue.getMnemonic());
	rbNoValue.addActionListener(new RadioButtonListener());
	group.add(rbNoValue);
	group.add(rbDate);
	group.add(rbInterval);

	JPanel rbPanel = new JPanel(new ColumnLayout());
	rbPanel.add(rbNoValue);
	rbPanel.add(rbDate);
	rbPanel.add(rbInterval);

	pane.add(rbPanel);	
	pane.setLayout(new ColumnLayout(LAYOUT_ALIGNMENT.LEFT));

	sbYear = new JIntSpinBox(4, 0, 9999);
	sbYear.setIntValue(2001);
	sbYear.addDocumentListener(this);
	JLabel lbYear = new JLabel(asYear.getString(), SwingConstants.RIGHT);
	lbYear.setDisplayedMnemonic(asYear.getMnemonic());
	lbYear.setLabelFor(sbYear.getTextField()); 
	JPanel pYear = new JPanel(new ColumnLayout(
				  LAYOUT_ALIGNMENT.LEFT, 0, 0));
	pYear.add(lbYear);
	pYear.add(sbYear);

	sbMonth = new JIntSpinBox(2, 1, 12);
	sbMonth.setIntValue(1);
	sbMonth.addDocumentListener(this);
	JLabel lbMonth = new JLabel(asMonth.getString(), 
				    SwingConstants.RIGHT);
	lbMonth.setDisplayedMnemonic(asMonth.getMnemonic()); 
	lbMonth.setLabelFor(sbMonth.getTextField()); 
	JPanel pMonth = new JPanel(new ColumnLayout(
				   LAYOUT_ALIGNMENT.LEFT, 0, 0));
	pMonth.add(lbMonth);
	pMonth.add(sbMonth);

	sbDay = new JIntSpinBox(2, 1, 31);
	sbDay.setIntValue(1);
	sbDay.addDocumentListener(this);
	JLabel lbDay = new JLabel(asDay.getString(), SwingConstants.RIGHT);
	lbDay.setDisplayedMnemonic(asDay.getMnemonic());
	lbDay.setLabelFor(sbDay.getTextField()); 
	JPanel pDay = new JPanel(new ColumnLayout(
				 LAYOUT_ALIGNMENT.LEFT, 0, 0));
	pDay.add(lbDay);
	pDay.add(sbDay);

	pDate = new JPanel(new RowLayout(LAYOUT_ALIGNMENT.LEFT));
	pDate.add(pYear);
	pDate.add(Box.createVerticalStrut(20));
	pDate.add(pMonth);
	pDate.add(Box.createVerticalStrut(20));
	pDate.add(pDay);

	pDateDays = new JPanel();
	pDateDays.setLayout(card);

	sbDays = new JIntSpinBox(8, 0, 99999999);
	sbDays.setIntValue(0);
	sbDays.addDocumentListener(this);
	JLabel lbDays = new JLabel(asDays.getString(), SwingConstants.RIGHT);
	lbDays.setDisplayedMnemonic(asDays.getMnemonic());
	lbDays.setLabelFor(sbDays.getTextField()); 
	pDays = new JPanel(new ColumnLayout(LAYOUT_ALIGNMENT.LEFT));
	pDays.add(lbDays);
	pDays.add(sbDays);

	pDateDays.add("date", pDate);
	pDateDays.add("days", pDays);

	sbHour = new JIntSpinBox(2, 0, 24);
	sbHour.setIntValue(0);
	sbHour.addDocumentListener(this);
	JLabel lbHour = new JLabel(asHour.getString(), SwingConstants.RIGHT);
	lbHour.setDisplayedMnemonic(asHour.getMnemonic());
	lbHour.setLabelFor(sbHour.getTextField()); 
	JPanel pHour = new JPanel(new ColumnLayout(
				  LAYOUT_ALIGNMENT.LEFT, 0, 0));
	pHour.add(lbHour);
	pHour.add(sbHour);

	sbMin = new JIntSpinBox(2, 0, 60);
	sbMin.setIntValue(0);
	sbMin.addDocumentListener(this);
	JLabel lbMin = new JLabel(asMin.getString(), SwingConstants.RIGHT);
	lbMin.setDisplayedMnemonic(asMin.getMnemonic());
	lbMin.setLabelFor(sbMin.getTextField()); 
	JPanel pMin = new JPanel(new ColumnLayout(
				 LAYOUT_ALIGNMENT.LEFT, 0, 0));
	pMin.add(lbMin);
	pMin.add(sbMin);

	sbSec = new JIntSpinBox(2, 0, 60);
	sbSec.setIntValue(0);
	sbSec.addDocumentListener(this);
	JLabel lbSec = new JLabel(asSec.getString(), SwingConstants.RIGHT);
	lbSec.setDisplayedMnemonic(asSec.getMnemonic());
	lbSec.setLabelFor(sbSec.getTextField()); 
	JPanel pSec = new JPanel(new ColumnLayout(
				 LAYOUT_ALIGNMENT.LEFT, 0, 0));
	pSec.add(lbSec);
	pSec.add(sbSec);
	
	JPanel pTime = new JPanel(new RowLayout(LAYOUT_ALIGNMENT.LEFT));
	pTime.add(pHour);
	pTime.add(Box.createVerticalStrut(20));
	pTime.add(pMin);
	pTime.add(Box.createVerticalStrut(20));
	pTime.add(pSec);

	sbMicroSec = new JIntSpinBox(6, 0, 999999);
	sbMicroSec.setIntValue(0);
	sbMicroSec.addDocumentListener(this);
	JLabel lbMicroSec = new JLabel(asMicroSec.getString(), 
				       SwingConstants.RIGHT);
	lbMicroSec.setDisplayedMnemonic(asMicroSec.getMnemonic()); 
	lbMicroSec.setLabelFor(sbMicroSec.getTextField()); 
	JPanel pMicroSec = new JPanel(new ColumnLayout(
				      LAYOUT_ALIGNMENT.LEFT, 0, 0));
	pMicroSec.add(lbMicroSec);
	pMicroSec.add(sbMicroSec);

	sbUTC = new JIntSpinBox(3, 0, 720);
	sbUTC.setIntValue(0);
	sbUTC.addDocumentListener(this);
	JLabel lbUTC = new JLabel(asUTC.getString(), SwingConstants.RIGHT);
	lbUTC.setDisplayedMnemonic(asUTC.getMnemonic()); 
	lbUTC.setLabelFor(sbUTC.getTextField()); 
	JPanel pUTC = new JPanel(new ColumnLayout(
				 LAYOUT_ALIGNMENT.LEFT, 0, 0));
	pUTC.add(lbUTC);

	JPanel pUTC1 = new JPanel(new ColumnLayout(
				  LAYOUT_ALIGNMENT.LEFT, 0, 0));
	pUTC1.setLayout(new RowLayout());
	cbUTC = new JComboBox(new String[] {
		"+", "-"});

	pUTC1.add(cbUTC);
	pUTC1.add(sbUTC);

	pUTC.add(pUTC1);

	pane.add(pDateDays);
	pane.add(pTime);

	pane.add(pMicroSec);
	pane.add(pUTC);

	if (value.length() == 25) {
	    try {
		if (value.substring(21, 22).equals(":")) {
		    sbDay.setIntValue(Integer.parseInt(
				      value.substring(0, 8)));
		    rbInterval.doClick();
		} else {
		    sbYear.setIntValue(Integer.parseInt(
				       value.substring(0, 4)));
		    sbMonth.setIntValue(Integer.parseInt(
					value.substring(4, 6)));
		    sbDay.setIntValue(Integer.parseInt(
				      value.substring(6, 8)));
		    rbDate.doClick();
		    cbUTC.setSelectedItem(value.substring(21, 22));
		}
		sbHour.setIntValue(Integer.parseInt(value.substring(8, 10)));
		sbMin.setIntValue(Integer.parseInt(value.substring(10, 12)));
		sbSec.setIntValue(Integer.parseInt(value.substring(12, 14)));
		sbMicroSec.setIntValue(Integer.parseInt(
				       value.substring(15, 21)));
		sbUTC.setIntValue(Integer.parseInt(value.substring(22, 25)));
	    } catch (NumberFormatException e) {
		JOptionPane.showMessageDialog(this, 
		    I18N.loadStringFormat("ERR_INVALID_DATETIME_STRING", 
		    sbDay.getStringValue(), sbMonth.getStringValue()), 
		    I18N.loadString("TTL_CIM_ERROR"),
		    JOptionPane.ERROR_MESSAGE);
		return pane;
	    }
	} else if (value.trim().length() == 0) {
	    Calendar cal = new GregorianCalendar();
	    sbYear.setIntValue(cal.get(Calendar.YEAR));
	    sbMonth.setIntValue(cal.get(Calendar.MONTH) + 1);
	    sbDay.setIntValue(cal.get(Calendar.DAY_OF_MONTH));
	    sbHour.setIntValue(cal.get(Calendar.HOUR_OF_DAY));
	    sbMin.setIntValue(cal.get(Calendar.MINUTE));
	    sbSec.setIntValue(cal.get(Calendar.SECOND));
	    sbMicroSec.setIntValue(cal.get(Calendar.MILLISECOND));
	    int offset = cal.get(Calendar.ZONE_OFFSET);
	    if (offset != 0) {
		if (offset > 0) {
		    cbUTC.setSelectedItem("+");
		} else {
		    cbUTC.setSelectedItem("-");
		}
		offset = Math.abs(offset/60000);
		
	    }
	    sbUTC.setIntValue(offset);
	    rbNoValue.doClick();
	} else {
	    rbDate.doClick();
	}
	if (!enabled) {
	    rbDate.setEnabled(false);
	    rbInterval.setEnabled(false);
	    rbNoValue.setEnabled(false);
	    enableEditFields(false);
	}
	return pane;
	
    }

    private void setSpinBoxHelp(JIntSpinBox sb, String helpFile) {
	sb.getTextField().addFocusListener(new ContextHelpListener(
		getInfoPanel(), "cimworkshop", helpFile));
	sb.getUpButton().addFocusListener(new ContextHelpListener(
		getInfoPanel(), "cimworkshop", helpFile));
	sb.getDownButton().addFocusListener(new ContextHelpListener(
		getInfoPanel(), "cimworkshop", helpFile));
    }

    public void okClicked() {
	StringBuffer buf = new StringBuffer("");
	if (!validDate()) {
	    return;
	}
	if (!rbNoValue.isSelected()) {
	    if (rbDate.isSelected()) {
		buf.append(padNumber(sbYear.getStringValue(), 4));
		buf.append(padNumber(sbMonth.getStringValue(), 2));
		buf.append(padNumber(sbDay.getStringValue(), 2));
	    } else {
		buf.append(padNumber(sbDays.getStringValue(), 8));
	    }
	    buf.append(padNumber(sbHour.getStringValue(), 2));	
	    buf.append(padNumber(sbMin.getStringValue(), 2));
	    buf.append(padNumber(sbSec.getStringValue(), 2));
	    buf.append(".");
	    buf.append(padNumber(sbMicroSec.getStringValue(), 6));

	    if (rbDate.isSelected()) {
		buf.append((String)cbUTC.getSelectedItem());
		buf.append(padNumber(sbUTC.getStringValue(), 3));	
	    } else {
		buf.append(":000");
	    }
	}
	returnString = buf.toString();

	if (setReturnObject()) {
	    dispose();
	} 

    }

    private boolean validDate() {
	boolean retVal = true;
	if (rbDate.isSelected()) {
	    try { 
		int day = sbDay.getIntValue();
		int month = sbMonth.getIntValue();
		
		if (((month == 2) && (day > 28)) ||
		    (((month == 4) || (month == 6) || 
		      (month == 9) || (month == 11)) & (day > 30)) ||
		    (day > 31)) {
		    JOptionPane.showMessageDialog(this, 
		        I18N.loadStringFormat("ERR_INVALID_DAYMONTH", 
		        sbDay.getStringValue(), sbMonth.getStringValue()), 
			I18N.loadString("TTL_CIM_ERROR"),
			JOptionPane.ERROR_MESSAGE);
		    retVal = false;
		}
	    } catch (NumberFormatException e) {
		JOptionPane.showMessageDialog(this, 
		    I18N.loadStringFormat("ERR_INVALID_DATETIME_STRING", 
		    sbDay.getStringValue(), sbMonth.getStringValue()), 
		    I18N.loadString("TTL_CIM_ERROR"),
		    JOptionPane.ERROR_MESSAGE);
		retVal = false;
	    }
	}
	return retVal;
    }

    private String padNumber(String number, int numSize) {
	StringBuffer buf = new StringBuffer(number);
	for (int i = number.length(); i < numSize; i++) {
	    buf.insert(0, 0);
	}
	return buf.toString();
    }


    public void setOKEnabled() {
	if ((rbDate == null) || (!isEditable)) {
	    return;
	}
	boolean b = true;

	if (!rbNoValue.isSelected()) {
	    if (rbDate.isSelected()) {	    
		b = b & (sbYear.getStringValue().trim().length() != 0);
		b = b & (sbMonth.getStringValue().trim().length() != 0);
		b = b & (sbDay.getStringValue().trim().length() != 0);	
		b = b & (sbUTC.getStringValue().trim().length() != 0);
	    } else {
		b = b & (sbDays.getStringValue().trim().length() != 0);	
	    }
	    b = b & (sbHour.getStringValue().trim().length() != 0);
	    b = b & (sbMin.getStringValue().trim().length() != 0);
	    b = b & (sbSec.getStringValue().trim().length() != 0);
	    b = b & (sbMicroSec.getStringValue().trim().length() != 0);
	}
	setOKEnabled(b);	
    }

    public void changedUpdate(DocumentEvent evt) {
	setOKEnabled();
    }

    public void insertUpdate(DocumentEvent evt) {
	setOKEnabled();
    }
    public void removeUpdate(DocumentEvent evt) {
	setOKEnabled();
    }
	  
    class RadioButtonListener implements ActionListener {
	public void actionPerformed(ActionEvent e) {
	    Object source = e.getSource();
	    if (source == rbNoValue) { 
		enableEditFields(false);
	    } else {
		enableEditFields(true);
		if (e.getSource() == rbInterval) { // interval button
		    sbUTC.setEnabled(false);
		    cbUTC.setEnabled(false);
		    card.show(pDateDays, "days");
		} else { // date button
		    card.show(pDateDays, "date");
		}
	    }
	    setOKEnabled();
	}
    }	

    private void enableEditFields(boolean b) {
	sbYear.setEnabled(b);
	sbMonth.setEnabled(b);
	sbDay.setEnabled(b);
	sbDays.setEnabled(b);
	sbHour.setEnabled(b);
	sbMin.setEnabled(b);
	sbSec.setEnabled(b);
	sbMicroSec.setEnabled(b);
	sbUTC.setEnabled(b);
	cbUTC.setEnabled(b);
    }

}
