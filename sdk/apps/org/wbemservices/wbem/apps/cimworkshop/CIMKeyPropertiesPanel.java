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

import java.util.Vector;

import javax.swing.JMenuItem;
import javax.wbem.cim.CIMInstance;
import javax.wbem.client.CIMClient;

import org.wbemservices.wbem.apps.common.ActionString;

/**
 * 
 *
 * @author 	Sun Microsystems
 */
public class CIMKeyPropertiesPanel extends CIMElementsTable {

    public CIMKeyPropertiesPanel(CIMClient cc, CIMInstance cimInstance) {
	this(cc, cimInstance, true);
    }
				 
    public CIMKeyPropertiesPanel(CIMClient cc, CIMInstance cimInstance, 
				 boolean editable) {
	super(ONLY_VALUE_EDITABLE, PROPERTY_TABLE, editable);

	//hideIconColumns();
	if (!editable) {
	    setAccessState(NON_EDITABLE);
	    ActionString asShow = new ActionString("MNU_SHOW_VALUE");
	    JMenuItem menuItem = popupMenu.add(new JMenuItem(
	        asShow.getString()));
	    menuItem.addActionListener(this);
	    table.addMouseListener(this);
	}
	cimClient = cc;
	setInstance(cimInstance);
    }

    public Vector getProperties() {
	return cimElements;
    }

    public void setInstance(CIMInstance cimInstance) {
	int rows = 0;
	parentElement = cimInstance;
	if (cimInstance == null) {
	    cimElements = null;
	} else {
	    cimElements = cimInstance.getProperties();
	}
	if (elementsDataModel != null && cimElements != null) {
	    rows = cimElements.size();
	}
	elementsDataModel.setNumRows(rows);
	table.repaint();
    }
}
