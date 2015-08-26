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

package org.wbemservices.wbem.apps.cimworkshop;

import java.awt.Frame;
import java.awt.event.WindowEvent;

import javax.swing.JOptionPane;
import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMNameSpace;
import javax.wbem.cim.CIMObjectPath;
import javax.wbem.client.CIMClient;

import org.wbemservices.wbem.apps.common.CIMClientObject;
import org.wbemservices.wbem.apps.common.CIMErrorDialog;
import org.wbemservices.wbem.apps.common.ContextHelpListener;
import org.wbemservices.wbem.apps.common.DefaultProperties;
import org.wbemservices.wbem.apps.common.I18N;
import org.wbemservices.wbem.apps.common.LoginDialog;
import org.wbemservices.wbem.apps.common.Util;

/**
 * 
 *
 * @version 	%I%, %G%
 * @author 	Sun Microsystems
 */

public class CWSLoginDialog extends LoginDialog {

    private static String hostname;

    public CWSLoginDialog(Frame parent) {
	this(parent, null, null);
    }

    public CWSLoginDialog(Frame parent, String servername, String interopns) {
	super(parent, HOSTNAME | USERNAME | PASSWORD | INTEROP, 
	      HOSTNAME | INTEROP);

	if (interop != null) {
	    if ((interopns == null) || (interopns.trim().length() == 0)) {
		interopns = CIMClientObject.getInteropNameSpace();
	    }
	    interop.setText(interopns);
	}

	CIMNameSpace ci = new CIMNameSpace();
	String sName = "";
	if (servername != null) {
	    sName = servername;
	} else {
	    sName = ci.getHost();
	}

	hname.setText("http://" + sName + "/" + interopns);

	// setup URLS for help
	this.setDefaultFocusListener(new ContextHelpListener(
		infoPanel, "cimworkshop", "login_000.htm"), true);

	try {
	    infoPanel.setUrl(DefaultProperties.getHelpUrl("login_000.htm"));
	} catch (Exception e) {
	    // ignore exception, just preloading a help file
	}

	hname.addFocusListener(new ContextHelpListener(
		infoPanel, "cimworkshop", "login_010.htm"));
	interop.addFocusListener(new ContextHelpListener(
		infoPanel, "cimworkshop", "login_060.htm"));

	uname.addFocusListener(new ContextHelpListener(
		infoPanel, "cimworkshop", "login_030.htm"));

	pass.addFocusListener(new ContextHelpListener(
		infoPanel, "cimworkshop", "login_040.htm"));

	if (CIMClientObject.getUserName() != null) {
	    setUserName(CIMClientObject.getUserName());
	}
	show();
    }

    public void okClicked() {
	setCursor(Util.waitCursor);
	if (CIMClientObject.initialize(getHostName(),
	    			       "",
	    			       getUserName(),
	    			       getPassword(),
				       "")) {



	    CIMClient cc = CIMClientObject.getClient();
	    String namespace = CIMClientObject.getNameSpace();
	    // try to enumerate qualifier types to make sure we 
	    // have a valid client and namespace
	    try {
		cc.enumQualifierTypes(new CIMObjectPath("", namespace));
	    } catch (CIMException e) {
		if (e.getID().equals("XMLERROR")) {
		    JOptionPane.showMessageDialog(this, 
		        I18N.loadStringFormat("ERR_HTTP_CONNECT", getHostName()),
			I18N.loadString("TTL_CIM_ERROR"), 
			JOptionPane.ERROR_MESSAGE);
		    setCursor(Util.defaultCursor);
		    return;
		} else if (e.getID().equals("CIM_ERR_ACCESS_DENIED")) {
		    // if access denied, we have and invalid credential

			CIMErrorDialog.display(this, new CIMException(
					       "INVALID_CREDENTIAL"));
			setCursor(Util.defaultCursor);
			return;
		}
		CIMErrorDialog.display(this, new CIMException(
				       "CIM_ERR_INVALID_NAMESPACE", 
				       namespace));
		setCursor(Util.defaultCursor);
		return;
	    }
	    
	    setLoggedIn(true);
	    dispose();
	}
	setCursor(Util.defaultCursor);
    }
    
    public void cancelClicked() {
	setLoggedIn(false);
	dispose();
    }    
    
    public void windowOpened(WindowEvent evt) {
	if (getUserName().length() < 1) {
	    uname.requestFocus();
	} else {
	    pass.requestFocus();
	}
    }


}
