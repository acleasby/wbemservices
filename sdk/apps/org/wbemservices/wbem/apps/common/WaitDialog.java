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

package org.wbemservices.wbem.apps.common;

import javax.swing.*;
import java.awt.*;

/**
 *
 * @author 	Sun Microsystems
 */

public class WaitDialog extends JDialog implements Runnable {
    
    JLabel gear;
    private volatile Thread dialogThread;
    private int gearcount = 1;
    ImageIcon gears1 = Util.loadImageIcon("gears01.gif");
    ImageIcon gears2 = Util.loadImageIcon("gears02.gif");
    ImageIcon gears3 = Util.loadImageIcon("gears03.gif");
    ImageIcon gears4 = Util.loadImageIcon("gears04.gif");
    ImageIcon gears5 = Util.loadImageIcon("gears05.gif");
    ImageIcon gears6 = Util.loadImageIcon("gears06.gif");
    ImageIcon gears7 = Util.loadImageIcon("gears07.gif");
    ImageIcon gears8 = Util.loadImageIcon("gears08.gif");
    ImageIcon gears9 = Util.loadImageIcon("gears09.gif");
    ImageIcon gears10 = Util.loadImageIcon("gears10.gif");
    ImageIcon gears11 = Util.loadImageIcon("gears11.gif");
    ImageIcon gears12 = Util.loadImageIcon("gears12.gif");

    public WaitDialog(Frame parent, String title, String message) {
	super(parent, title);
	JLabel l = new JLabel(message);
	l.setForeground(Color.black);
	l.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
	gear = new JLabel(gears1);
	JComponent contentpane = (JComponent)getContentPane();
	contentpane.setLayout(new BorderLayout());
	contentpane.add("West", gear);
	contentpane.add("Center", l);
	contentpane.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
	pack();
    }

    private void changeGear() {
	gearcount++;
	if (gearcount == 13) {
	gearcount = 1;
	}
	switch (gearcount) {
	    case 1:
		gear.setIcon(gears1);
		break;
	    case 2:
		gear.setIcon(gears2);
		break;
	    case 3:
		gear.setIcon(gears3);
		break;
	    case 4:
		gear.setIcon(gears4);
		break;
	    case 5:
		gear.setIcon(gears5);
		break;
	    case 6:
		gear.setIcon(gears6);
		break;
	    case 7:
		gear.setIcon(gears7);
		break;
	    case 8:
		gear.setIcon(gears8);
		break;
	    case 9:
		gear.setIcon(gears9);
		break;
	    case 10:
		gear.setIcon(gears10);
		break;
	    case 11:
		gear.setIcon(gears11);
		break;
	    case 12:
		gear.setIcon(gears12);
		break;
	}
    }

    public void start() {
	dialogThread = new Thread(this);
	dialogThread.start();
	this.setVisible(true);
	setCursor(new Cursor(Cursor.WAIT_CURSOR));
    }

    public void stop() {
	dialogThread = null;
	this.dispose();
    }


    public void run() {
	Thread thisThread = Thread.currentThread();
	while (dialogThread == thisThread) {
	    changeGear();
	    try {
		Thread.sleep(200);
	    }
	    catch (Exception ex) {
	    }   
	}
    }

}
