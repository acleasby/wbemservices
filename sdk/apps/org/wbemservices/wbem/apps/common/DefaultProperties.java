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

import java.awt.Dimension;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;

/**
 * 
 * @author 	Sun Microsystems
 */

public class DefaultProperties {

    private static final String defaultHelpLocale = "C";
    private static String helpLocale = "C";
    public static String loginFont = "sans-14";
    public static String textPanelFont = "serif-12";
    public static String dialogfont = "dialog-bold-12";
    public static Dimension loginWindowSize = new Dimension(550, 330);
    public static Dimension addClassDlgSize = new Dimension(770, 400);
    public static Dimension qualifierDlgSize = new Dimension(770, 400);
    public static String helpLoc = " ";
    public static String installLoc = "";


    public static void setInstallLoc(String loc, String appName) {
	String sep = File.separator;
	if (loc != null) {
	    installLoc = loc;
	}
	if (!installLoc.endsWith(sep) && installLoc.length() != 0) {
	    installLoc = installLoc + sep;
	}
	setHelpLoc(installLoc, appName);
//	setLogHelpLoc(installLoc);
    }

    public static void setHelpLoc(String loc, String appName) {
	String sep = File.separator;
	String helpPath = "help" + sep + appName + sep + "locale";
	String baseDir = loc + helpPath;
	helpLocale = getHelpLocaleDirectory(baseDir, "notfound.html", sep);
	// if locale is default locale, read files out of jar file
	// NOTE: in jar file, separator is always "/"
	if (helpLocale.equals(defaultHelpLocale)) {
	    sep = "/";
	    baseDir = "org/wbemservices/wbem/apps/help/" + appName + "/locale";
	}
	helpLoc = baseDir + sep + helpLocale + sep;
    }
    
    private static String getHelpLocaleDirectory(String pathname, 
	String filename, String sep) {

	Locale loc;
	File   fd;
	String locPath;
	String locValue;
	int    i;
	boolean sw;

	// Use the system default locale.  Get it in string format and try
	// it as the local sub-directory.  If the specified help file is found,
	// thats our directory.  If not found, back off the variant, country
	// code, and language code successfully until we find the file.
	// If still not found, use the "default" help sub-directory.
	loc = Locale.getDefault();
	locPath = "";
	locValue = "";
	fd = null;
	if (loc != null) {
	    locValue = loc.toString();
	    sw = true;
	    while (sw) {
		locPath = pathname + sep + locValue + sep + filename;
		try {
		    fd = new File(locPath);
		    if (fd.exists()) {
			    break;
		    }
		} catch (NullPointerException ex) {
			// Ignore exceptions.  Just keep trying.
		}
		fd = null;
		i = locValue.lastIndexOf('_');
		if (i > 0) {
		    locValue = locValue.substring(0, i);
		} else {
		    sw = false;
		}
	    }					// End of while
	}					// End loc not equal null

	// If the file descriptor is null, we did not find the help file.
	// Just use the default help file directory (the file may still
	// not exist, but this is a bug in the application!).
	if (fd == null) {
	    locValue = defaultHelpLocale;
	}

	return (locValue);

    }

    public static URL getHelpUrl(String helpName) throws MalformedURLException {
	URL url = null;
	// if help locale = default help locale, get help file from jar file
	if (helpLocale.equals(defaultHelpLocale)) {
	    URLClassLoader urlCL = Util.getClassLoader(); 
	    if (urlCL != null) {
		url = urlCL.findResource(helpLoc + helpName);
	    } else {
		try {
		    url = new URL("file:" + helpLoc + helpName);
		} catch (MalformedURLException exc) {
		    throw exc;
		}
	    }
	} else {
	    try {
		url = new URL("file:" + helpLoc + helpName);
	    } catch (MalformedURLException exc) {
		throw exc;
	    }
	}
	return url;
    }
}

