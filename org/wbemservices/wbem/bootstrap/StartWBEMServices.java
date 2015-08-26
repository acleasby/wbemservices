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
 *are Copyright © 2003 Sun Microsystems, Inc.
 *
 *All Rights Reserved.
 *
 *Contributor(s): WBEM Solutions, Inc.
 */

package org.wbemservices.wbem.bootstrap;

import org.wbemservices.wbem.cimom.util.DynClassLoader;

import java.util.*;

/** Class which starts the WBEM Services CIMOM
 *  This class starts the WBEM Services CIMOM using a Dynamic classload
 * @author  Jim Marshall (jmars@east.sun.com)
 * @version 1.0
 */
public class StartWBEMServices {
    // no one can create an instance of this class
    private StartWBEMServices() {}
    
    /** The name of the CIMOM class to start */
    private static final String CIMOM_CLASS = "org.wbemservices.wbem.cimom.WBEMServices";
    /** indicates that this class has already executed or not */
    private static boolean running = false;
    /** The classpaths to add to the dynamic class loader */
    private static String[] classpaths = null;
    
    /** Property which specifies the location of WBEMServices.properties */
    public static final String WBEM_SERVICE = "WBEMServices";
    /** Property in WBEMServices.properties for debug level */
    public static final String DEBUG_LEVEL = "DebugLevel";
    /** Property in WBEMServices.properties for debug device */
    public static final String DEBUG_DEVICE = "DebugDevice";
    /** Property which specifies the base dir for WBEM Services */
    public static final String BASEDIR = "BaseDir";
    /** Property which specifies the 'propdir' for WBEM Services */
    public static final String PROPDIR = "propdir";
    /** Property which specifies the install directory */
    public static final String INSTALLDIR = "InstallDir";
    /** Property which specifies where the repository is located */
    public static final String LOGDIR = "logdir";
    /** Property which specifies the location of the log */
    public static final String LOGPARENT = "logparent";
    /** Property which specifies the wbem lib directory */
    public static final String LIBWBEMDIR = "libwbemdir";
    
    /** The system property which tells the CIMOM to debug */
    public static final String SYSTEM_DEBUG_LEVEL = "wbem.debug.level";
    /** The system property which specifies the device to output to */
    public static final String SYSTEM_DEBUG_DEVICE = "wbem.debug.device";

    /** sets the various system directory properties if specified in
     * the WBEMServices.properties file */
    private static void setDirs(Properties props) {
        // see if basedir was set via a -D option
        String basedir = System.getProperty(StartWBEMServices.BASEDIR);
        if (basedir == null) {
            // nope see if it was in the properties dir
            basedir = props.getProperty(StartWBEMServices.BASEDIR, "");
            // set it
            System.setProperty(StartWBEMServices.BASEDIR, basedir);
        }
	// see if propdir was set via a -D option
	String propdir = System.getProperty(StartWBEMServices.PROPDIR);
	if (propdir == null) {
	    // nope, see if it is in the properties file
	    propdir = props.getProperty(StartWBEMServices.PROPDIR);
	    if (propdir != null) {
		// set it
		System.setProperty(StartWBEMServices.PROPDIR, propdir);
	    }
	}
        // see if logdir was set via a -D option
        String logdir = System.getProperty(StartWBEMServices.LOGDIR);
        if (logdir == null) {
            // nope see if it was in the properties file
            logdir = props.getProperty(StartWBEMServices.LOGDIR);
	    if (logdir != null) {
		// set it
		System.setProperty(StartWBEMServices.LOGDIR, logdir);
	    }
        }
        // see if logparent was set via a -D option
        String logparent = System.getProperty(StartWBEMServices.LOGPARENT);
        if (logparent == null) {
            // nope see if it was in the properties file
            logparent = props.getProperty(StartWBEMServices.LOGPARENT);
	    if (logparent != null) {
		// set it
		System.setProperty(StartWBEMServices.LOGPARENT, logparent);
	    }
        }
        // see if libwbemdir was set via a -D option
        String libwbemdir = System.getProperty(StartWBEMServices.LIBWBEMDIR);
        if (libwbemdir == null) {
            // nope see if it was in the properties file
            libwbemdir = props.getProperty(StartWBEMServices.LIBWBEMDIR);
	    if (libwbemdir != null) {
		// set it
		System.setProperty(StartWBEMServices.LIBWBEMDIR, libwbemdir);
	    }
        }
        // see if InstallDir was set via a -D option
        String installdir = System.getProperty(StartWBEMServices.INSTALLDIR);
        if (installdir == null) {
            // nope see if it was in the properties file
            installdir = props.getProperty(StartWBEMServices.INSTALLDIR);
	    if (installdir != null) {
		// set it
		System.setProperty(StartWBEMServices.INSTALLDIR, installdir);
	    }
        }
    }
    
    /** gets the requires classpath info from a properties file */
    private static String[] getClassPaths(Properties props) {
        String[] ret = new String[0];
        String val = props.getProperty("classpath");
        if (val != null) {
            int count = 0;
            StringTokenizer tok = new StringTokenizer(val,
                java.io.File.pathSeparator, false);
            ret = new String[tok.countTokens()];
            while (tok.hasMoreTokens()) {
                String path = tok.nextToken();
                ret[count] = "file:" + path;
                count++;
            }
        }
        return ret;
    }
    
    /** Checks if the debug options should be set */
    private static void setDebug(Properties props) {
        // see if level is set
        String level = props.getProperty(StartWBEMServices.DEBUG_LEVEL);
        if (level != null) {
            System.setProperty(StartWBEMServices.SYSTEM_DEBUG_LEVEL, level);
            // get device
            String device = props.getProperty(StartWBEMServices.DEBUG_DEVICE);
            // default to stdout
            if (device == null) {
                device = "stdout";
            }
            System.setProperty(StartWBEMServices.SYSTEM_DEBUG_DEVICE, device);
        }
    }

    /** Loads the WBEMServices.properties file */
    private static void loadPropertyFile(String pName) throws Exception {
        Properties props = new Properties();
        props.load(new java.io.FileInputStream(pName));
        classpaths = getClassPaths(props);
        setDebug(props);
        setDirs(props);
    }

    /** Starts WBEM Services CIMOM
    * Create a Dynamic Classloader, then starts the CIMOM
    * @param args the command line arguments
    */
    public static synchronized Object startWBEM(String args[]) throws Exception {
        if (running == true) {
            throw new Exception("WBEMServices has already been executed");
        }
        // find where the WBEMProperties is
        String wbemProps = System.getProperty(StartWBEMServices.WBEM_SERVICE);
        // Was it specified via a -D?
        if (wbemProps == null) {
            // Nope, see if its on the command line
            if (args.length < 1 || args[0].endsWith(".properties") == false) {
                System.out.println("Invalid command line.");
                System.out.println("Need location of properties file.\n");
                System.out.print("java -DWBEMService=/usr/sadm/lib/wbem/");
                System.out.println("WBEMServices.properties " +
			StartWBEMServices.class.getName());
                System.out.println("\nor\n");
                System.out.print("java " + StartWBEMServices.class.getName());
                System.out.println("/usr/sadm/lib/wbem/WBEMServices.properties");
                System.exit(1);
            }
            wbemProps = args[0];
        }
        StartWBEMServices.loadPropertyFile(wbemProps);
        // Create the dynamic class loader, parent is the primordial classloader
        DynClassLoader dcl =
            new DynClassLoader(StartWBEMServices.classpaths,
		ClassLoader.getSystemClassLoader());
        // load the CIMOM class
        Class c = dcl.loadClass(CIMOM_CLASS);
        // what parameters the constructor takes
        Class Args[] = {String[].class};
        // find the constructor which takes this
        java.lang.reflect.Method method = c.getMethod("StartCIMOM", Args);
        // Start the CIMOM using the above ctor
        Object ret = method.invoke(null, new Object[] {args});
        if (ret != null) {
            running = true;
        }
        return ret;
    }

    /** The main method
     *  @param args command line arguments
     */
    public static void main (String args[]) {
        try {
            StartWBEMServices.startWBEM(args);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
