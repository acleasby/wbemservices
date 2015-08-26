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

package org.wbemservices.wbem.cimom;


import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.text.MessageFormat;

import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMInstance;
import javax.wbem.cim.CIMObjectPath;
import javax.wbem.cim.CIMValue;
import javax.wbem.client.Debug;
import javax.wbem.client.ProviderCIMOMHandle;
import javax.wbem.provider.CIMInstanceProvider;

/**
 * 
 *
 * @author 	Sun Microsystems, Inc.
 * @since       WBEM 1.0
 */
public class LogFile {

    // XXX factory
    //private static Hashtable serviceClass;
    //private static Hashtable serviceProvider;
    private static ProviderCIMOMHandle mCimom = null;
    private static CIMInstanceProvider logProvider = null;
    public  final static int DEVELOPMENT    =  4;
    public  final static int DEBUG	    =  3;
    public  final static int INFORMATIONAL  =  2;
    public  final static int WARNING	    =  1;
    public  final static int CRITICAL	    =  0;
    private final static String newLine	= "\n";
    private static boolean started = true;
    private static int logLevel = WARNING;   
    private static DataOutputStream fStream;
    private static String fileName = "cimom.log";
    private static File f = null;
    private static boolean verbose = false;        
    private final static String LOGDIRPARENT = "/var/sadm/wbem";

    private static CIMInstance getLogInstance() {

	return null;
	// XXX The following wont work! This will introduce infinit recursion
	// becuase mCimom.getClass will in turn want to log!
	/*
	if (mLogClass == null) {
	    // Log service resides in root/cimv2
	    CIMObjectPath obj = new CIMObjectPath(LOGCLASS, "/root/cimv2");
	    try {
		    mLogClass = mCimom.getClass(obj, false, false,
			false, null);
	    }
	    catch (CIMException e) {
		// XXX log error - where?
		mLogClass = null;
	    }
	    if (mLogClass == null) {
		// XXX remove
		System.out.println("Unable to find " + LOGCLASS);
		return null;
	    }
	}

	if (logProvider == null) {
	    logProvider =
		CIMOMImpl.getProviderFactory().getInstanceProvider(
				"", mLogClass);
	    if (logProvider == null) {
		// XXX remove
		System.out.println("Unable to get provider");
		return null;
	    }
	}

	// We have found the log service class and its provider.
	CIMInstance ci = mLogClass.newInstance();
	return ci;
	*/
    }

    private static void setProperties(CIMInstance ci, String resourceID, 
					int level) {
        try {
            ci.setProperty("SummaryMessage", new CIMValue(resourceID));
            ci.setProperty("AppName", new CIMValue("CIMOM"));
            ci.setProperty("category", new CIMValue(new Integer(0)));

            switch (level) {
            case DEVELOPMENT:
            case DEBUG:
            case INFORMATIONAL: level = 0;
                        break;
            case WARNING:       level = 1;  
                        break;
            case CRITICAL:      level = 2;  
                        break;
            }
            ci.setProperty("severity", new CIMValue(new Integer(level)));
            ci.setProperty("UserName", new CIMValue("CIMOM"));
            ci.setProperty("ClientMachineName", new CIMValue("<none>"));
            String hostName = ".";
            try {
                hostName = InetAddress.getLocalHost().getHostName();
            } catch (Exception e) {
                // Do nothing
            }
            ci.setProperty("ServerMachineName", new CIMValue(hostName));
            ci.setProperty("data", new CIMValue(""));
            ci.setProperty("syslogflag", new CIMValue(Boolean.FALSE));
        } catch(CIMException ex) {
            Debug.trace1("Exception trying to setProperties", ex);
        }
    }

    /**
     * Adds data to an installation log
     * @param level defines the log that will data
     * @param resourceID the resource id
     */
    public static void add(int level, String resourceID) throws CIMException {
	if (logLevel >= level) {
	    CIMInstance ci = getLogInstance();
	    if (ci == null) {
		return;
	    }
	    ci.setProperty("DetailedMessage", 
		new CIMValue(I18N.loadString(resourceID)));
	    setProperties(ci, resourceID, level);

	    if (logLevel < DEBUG) {
		try {
		    logProvider.createInstance(
		    new CIMObjectPath(ci.getClassName(), "/root/cimv2"), ci);
		} catch (CIMException e) {
		    System.out.println(e);
		    throw e;
		}
	    }
	    addInfo(ci.toString());
	}
    }

    /**
     * adds arbitrary number of data items to a log file
     *
     * @param level defines the log that will data
     * @param resourceID is used to load a string from a resource bundle
     * @param obj Array of data items to be logged
     */
    public static void add(int level, String resourceID, Object obj[])
    throws CIMException {
	if (logLevel >= level) {

	    MessageFormat mf = new MessageFormat(I18N.loadString(resourceID));
	    mf.setLocale(I18N.locale);	

	    CIMInstance ci = getLogInstance();
	    if (ci == null) {
		return;
	    }
	    ci.setProperty("DetailedMessage", new CIMValue(mf.format(obj)));
	    setProperties(ci, resourceID, level);

	    if (logLevel < DEBUG) {
		try {
		    logProvider.createInstance(
		    new CIMObjectPath(ci.getClassName(), "/root/cimv2"), ci);
		} catch (CIMException e) {
		    System.out.println(e);
		    throw e;
		}
	    }
	    addInfo(ci.toString());
	}
    }

    /**
     * adds three data items to a log file.
     *
     * @param level defines the log that will data
     * @param resourceID is used to load a string from a resource bundle
     * @param info1 the data to be logged
     * @param info2 the data to be logged
     * @param info3 the data to be logged
     */
    public static void add(int level, String resourceID, Object info1,
				Object info2, Object info3) 
				throws CIMException {
	add(level, resourceID, new Object[] { info1, info2, info3 });
    }
    /**
     * adds two data items to a log file.
     *
     * @param level defines the log that will data
     * @param resourceID is used to load a string from a resource bundle
     * @param info1 the data to be logged
     * @param info2 the data to be logged
     */
    public static void add(int level, String resourceID, Object info1,
				Object info2) 
				throws CIMException {
	add(level, resourceID, new Object[] { info1, info2 });
    }

    /**
     * adds data to a log file
     *
     * @param level defines the log that will data
     * @param resourceID is used to load a string from a resource bundle
     * @param info the data to be logged
     */
    public static void add(int level, String resourceID, Object info) 
    throws CIMException {
	add(level, resourceID, new Object[] { info });
			
    }
    /**
     * Output DEVELOPMENT log indicating method entry
     * @param methodName method being entered
     */
    public static void methodEntry(String methodName) throws CIMException {
	add(DEVELOPMENT, "METHOD_ENTRY", methodName);
    }

    /**
     * Output DEVELOPMENT log indicating method return
     * @param methodName method being returned from
     */
    public static void methodReturn(String methodName) throws CIMException {
	add(DEVELOPMENT, "METHOD_RETURN", methodName);
    }
    public static String getFileName() {
	return fileName;
    }

    /**
     * Gets the current loglevel
     */
    public static int getLevel() {
	return logLevel;
    }

    public static boolean getVerbose() {
	return verbose;
    }

    public static void setVerbose(boolean b) {
	verbose = b;
    }
    
    public static void initialize(ProviderCIMOMHandle pCimom)
throws CIMException {
	// XXX factory
	//serviceClass = sc;
	//serviceProvider = sp;
	mCimom = pCimom;
	try {
	    String logpath = System.getProperty("logdir",
				System.getProperty("logparent",LOGDIRPARENT)
				+File.separator+"logr");
	    f = new File(logpath+File.separator+fileName);
	    f.createNewFile();
	    fStream = 
	    new DataOutputStream(
			new FileOutputStream(logpath+File.separator+fileName));
	} catch (Exception e) {
	    System.out.println(e);
	}
    }

    /**
     * Sets the level of information to log to the logfile
     * @param level the level to set
     */
    public static void setLevel(int level) {
	logLevel = level;
    }

    public static void start() {
	started = true;
    }

    private static void addInfo(String info) {
	if (f.length() > 5000000) {
	    try {
		f.delete();
		f.createNewFile();
		fStream = 
		new DataOutputStream(new FileOutputStream(f.getAbsolutePath()));
	    } catch (Exception e) {
		System.out.println(e);
	    }
	}
	try {
	    /*
	    if (verbose) {
		System.out.println(info);
		System.out.println(newLine);
	    }
	    */
	    fStream.writeBytes(info);
	    fStream.writeBytes(newLine);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

}
