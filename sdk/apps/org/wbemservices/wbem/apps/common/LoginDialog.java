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

package org.wbemservices.wbem.apps.common;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * 
 *
 * @version 	1.21, 04/03/01
 * @author 	Sun Microsystems
 */

public class LoginDialog extends AdminDialog implements DocumentListener {
 
    protected JButton btnOK, btnCancel;    
    protected JTextField uname;
    protected JTextField hname;
    protected JPasswordField pass;
    protected JTextField nspace;
    protected JTextField interop;
    protected GenInfoPanel infoPanel;
    
    protected boolean loggedIn = false;

    protected int reqMask;
    protected int inputMask;

    public final static int HOSTNAME = 1;
    public final static int NAMESPACE = 2;
    public final static int USERNAME = 4;
    public final static int PASSWORD = 8;
    public final static int INTEROP = 16;
    public final static int NONE = 0;
    public final static int ALL = HOSTNAME | NAMESPACE | USERNAME | 
	                          PASSWORD | INTEROP;
    
    public LoginDialog(Frame parent, int iMask, int rMask) {
	super(parent, I18N.loadString("TTL_LOGIN", 
	      "org.wbemservices.wbem.apps.common.common"), false);

	inputMask = iMask;
	// reqMask can only include fields in the input mask
	reqMask = rMask & iMask;
	infoPanel = this.getInfoPanel();

	btnOK = this.getOKBtn();
	btnOK.addActionListener(new OKCancelButtonListener());

	btnCancel = this.getCancelBtn();
	btnCancel.addActionListener(new OKCancelButtonListener());
	JPanel mainPanel = getRightPanel();	
	mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));


	mainPanel.setLayout(new ColumnLayout(LAYOUT_ALIGNMENT.EXPAND));

	JPanel inputs = new JPanel();
	inputs.setLayout(new ColumnLayout(LAYOUT_ALIGNMENT.RIGHT));

	TextFieldFocusListener tfListener = new TextFieldFocusListener();

	// if showing hostname textfield, add it and set up listeners
	if ((inputMask & HOSTNAME) > 0) { 
	    JPanel hnamep = new JPanel();
	    ActionString asHost = new ActionString("LBL_HOSTURL",
	        "org.wbemservices.wbem.apps.common.common");
	    JLabel hnamel = new JLabel(asHost.getString() + ":", 
				       SwingConstants.RIGHT);
	    hnamel.setDisplayedMnemonic(asHost.getMnemonic());
	    hnamep.add(hnamel);
	    
	    hname = new JTextField(12);
	    hnamel.setLabelFor(hname);
	    hname.addFocusListener(tfListener);
	    hname.getDocument().addDocumentListener(this);
	    hnamep.add(hname);
	    inputs.add(hnamep);
	}

	// if showing namespace textfield, add it and set up listeners
	if ((inputMask & NAMESPACE) > 0) {
	    JPanel nspacep = new JPanel();
	    ActionString asNSpace = new ActionString("LBL_NAMESPACE");
	    JLabel nspacel = new JLabel(asNSpace.getString() + ":",
				       SwingConstants.RIGHT);
	    nspacel.setDisplayedMnemonic(asNSpace.getMnemonic());
	    nspacep.add(nspacel);

	    nspace = new JTextField(12);
	    nspacel.setLabelFor(nspace);
	    nspace.addFocusListener(tfListener);
	    nspace.getDocument().addDocumentListener(this);
	    nspacep.add(nspace);
	    inputs.add(nspacep);
	}

	// if showing username textfield, add it and set up listeners
	if ((inputMask & USERNAME) > 0) {
	    JPanel login = new JPanel();
	    ActionString asUser = new ActionString("LBL_USERNAME",
	        "org.wbemservices.wbem.apps.common.common");
	    JLabel userl = new JLabel(asUser.getString() + ":", 
				       SwingConstants.RIGHT);
	    userl.setDisplayedMnemonic(asUser.getMnemonic());
	    login.add(userl);

	    uname = new JTextField(12);
	    userl.setLabelFor(uname);
	    uname.addFocusListener(tfListener);
	    uname.getDocument().addDocumentListener(this);
	    login.add(uname);
	    inputs.add(login);
	}

	// if showing password textfield, add it and set up listeners
	if ((inputMask & PASSWORD) > 0) {
	    JPanel passwd = new JPanel();
	    ActionString asPasswd = new ActionString("LBL_PASSWORD",
	        "org.wbemservices.wbem.apps.common.common");
	    JLabel passwdl = new JLabel(asPasswd.getString() + ":",
				       SwingConstants.RIGHT);
	    passwdl.setDisplayedMnemonic(asPasswd.getMnemonic());
	    passwd.add(passwdl);

	    pass = new JPasswordField(12);
	    passwdl.setLabelFor(pass);
	    pass.addFocusListener(tfListener);
	    pass.getDocument().addDocumentListener(this);
	    passwd.add(pass);
	    inputs.add(passwd);
	}

	// if showing protocol buttons, add them and set up listeners
	if ((inputMask & INTEROP) > 0) {
	    JPanel interopp = new JPanel();
	    ActionString asInterop = new ActionString("LBL_INTEROP_NS");
	    JLabel lInterop = new JLabel(asInterop.getString() + ":",
				       SwingConstants.RIGHT);
	    lInterop.setDisplayedMnemonic(asInterop.getMnemonic());
	    interopp.add(lInterop);
	    
	    interop = new JTextField(12);
	    lInterop.setLabelFor(interop);
	    interop.addFocusListener(tfListener);
	    interop.getDocument().addDocumentListener(this);
	    interopp.add(interop);
	    inputs.add(interopp);

	}


	mainPanel.add(inputs);

	setSize(DefaultProperties.loginWindowSize);	

	setOKEnabled();
	Util.positionWindow(this, parent);
    }


    public boolean getLoggedIn() {
	return loggedIn;
	}

    public void setLoggedIn(boolean b) {
	loggedIn = b; 
	}
    
    public String getUserName()
    {
	String ret = "";
	if (uname != null) {
	    ret = uname.getText().trim();
	}
	return ret;
    }

    public String getInteropNameSpace()
    {
	String ret = "";
	if (interop != null) {
	    ret = interop.getText().trim();
	}
	return ret;
    }

    public String getPassword() {
	String ret = "";
	if (pass != null) {
	    ret = new String(pass.getPassword());
	}
	return ret.trim();
    }

    public String getHostName()
    {
	String ret = "";
	if (hname != null) {
	    ret = hname.getText().trim();
	}
	return ret;
    }

    public String getNameSpace() {
	String ret = "";
	if (nspace != null) {
	    ret = nspace.getText().trim();
	}
	return ret;
    }

    public void setHostName(String s) {
	if (hname != null) {
	    hname.setText(s);
	}
    }

    public void setInteropNameSpace(String s) {
	if (interop != null) {
	    interop.setText(s);
	}
    }

    public void setNameSpace(String s) {
	if (nspace != null) {
	    nspace.setText(s);
	}
    }

    public void setUserName(String s) {
	if (uname != null) {
	    uname.setText(s);
	}
    }

    public void setPassword(String s) {
	if (pass != null) {
	    pass.setText(s);
	}
    }

    public void changedUpdate(DocumentEvent evt) {
	documentEvent(evt);
    }

    public void insertUpdate(DocumentEvent evt) {
	documentEvent(evt);
    }

    public void removeUpdate(DocumentEvent evt) {
	documentEvent(evt);
    }

    public void documentEvent(DocumentEvent evt) {
	if (btnOK != null) {
	    setOKEnabled();
	}
    }
    
    protected void setOKEnabled() {        
	boolean b = true;
	if ((reqMask & HOSTNAME) > 0) {
	    b = b && (hname.getText().trim().length() != 0);
	}
	if ((reqMask & NAMESPACE) > 0) {
	    b = b && (nspace.getText().trim().length() != 0);
	}

	if ((reqMask & USERNAME) > 0) {
	    b = b && (uname.getText().trim().length() != 0);
	}
	if ((reqMask & PASSWORD) > 0) {
	    b = b && (getPassword().trim().length() != 0);
	}	    

	if ((reqMask & INTEROP) > 0) {
	    b = b && (interop.getText().trim().length() != 0);
	}	    

	btnOK.setEnabled(b);
    }

    // override this function for when OK button clicked
    public void okClicked() {
    }
    
    // override this function for when cancel button clicked
    public void cancelClicked() {
    }

    class OKCancelButtonListener implements ActionListener {
	public void actionPerformed(ActionEvent e) {
	    if (e.getSource() == btnCancel) { 
		cancelClicked();
	    } else if (e.getSource() == btnOK) { 
		okClicked();
	    }
	}
    }
  
    
}
