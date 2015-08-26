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

package org.wbemservices.wbem.compiler.mofc;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Date;

/**
 * 
 * 
 * 
 * 
 *
 * @author 	Sun Microsystems, Inc.
 * @version 	1.11, 02/26/01
 * @since       WBEM 1.0
 */
public class LogFile {

    public  final static int DEVELOPMENT    =  4;
    public  final static int DEBUG	    =  3;
    public  final static int INFORMATIONAL  =  2;
    public  final static int WARNING	    =  1;
    public  final static int CRITICAL	    =  0;
    private final static String newLine	= "\n";
    private static boolean started = false;
    private static int logLevel = WARNING;   
    private static DataOutputStream fStream;
    private static String fileName = "logfile.log";
    private static boolean verbose = false;        
    
    /**
     *	add
     *  adds data to an installation log
     * @param level defines the log that will data
     * @param resourceID the resource id
     */
    public static void add(int level, String resourceID) {
	if (logLevel >= level) {
	    addInfo(I18N.loadString(resourceID));
	}
    }

    /**
     * adds arbitrary number of data items to a log file
     *
     * @param level defines the log that will data
     * @param resourceID is used to load a string from a resource bundle
     * @param obj Array of data items to be logged
     */
    public static void add(int level, String resourceID, Object[] obj){
	if (logLevel >= level){
	    MessageFormat mf = new MessageFormat(I18N.loadString(resourceID));
	    mf.setLocale(I18N.locale);	
	    for (int i = 0; i < obj.length; i++) {
		obj[i] = obj[i].toString();
	    }
	    addInfo(mf.format(obj));
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
				Object info2, Object info3) {
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
				Object info2) {
	add(level, resourceID, new Object[] { info1, info2 });
    }

    /**
     * adds data to a log file
     *
     * @param level defines the log that will data
     * @param resourceID is used to load a string from a resource bundle
     * @param info the data to be logged
     */
    public static void add(int level, String resourceID, Object info) {
	add(level, resourceID, new Object[] { info });
			
    }

    /**
     * add
     * adds data to a log file
     * @param level defines the log that will data
     * @param in the a stream that provides data to be logged
     */
    public static void add(int level, InputStream in){
	BufferedReader fileReader = new BufferedReader
	    (new InputStreamReader(new DataInputStream(in))); 
	String line;

	try {
	    while ((line = fileReader.readLine()) != null ) {
       		if (logLevel >= level){
		    addInfo(line);
    		}
	    }
	} catch (IOException e) {
	}
    }

    /**
     * Output DEVELOPMENT log indicating method entry
     * @param methodName method being entered
     */
    public static void methodEntry(String methodName) {
	add(DEVELOPMENT, "METHOD_ENTRY", methodName);
    }

    /**
     * Output DEVELOPMENT log indicating method return
     * @param methodName method being returned from
     */
    public static void methodReturn(String methodName) {
	add(DEVELOPMENT, "METHOD_RETURN", methodName);
    }

    /**
     * Add a string to the log file
     */
    private static void addInfo(String info){
	if (started) {
	    try{
		if (verbose) {
		    System.out.println(info);
		    System.out.println(newLine);
		}
		fStream.writeBytes(info);
		fStream.writeBytes(newLine);
	    }catch(IOException e){
	    }
	}
    }

    public static String getFileName(){
	return fileName;
    }

    /**
     * Gets the current loglevel
     */
    public static int getLevel(){
	return logLevel;
    }

    public static boolean getVerbose() {
	return verbose;
    }

    public static void setVerbose(boolean b) {
	verbose = b;
    }
    
    public static void setFileName(String name){
	fileName = name;
    }

    /**
     * Sets the level of information to log to the logfile
     * @param level the level
     */
    public static void setLevel(int level){
	logLevel = level;
    }

    public static void start(){
	start(fileName);
    }

    public static void start(String fName) {
	//fileName must be the complete path and filename to the file
	//If fileName exists - rename to back and start new log, 
	//if back exists delete it
	fileName = fName;
	//make usre that the directory exists and that the file is rw
	try{
	    File f = new File(fileName);
	    if (f.exists()){
		File fBak = new File(fileName + ".bak");
		if (fBak.exists()){
		    if (fBak.delete()){
			f.renameTo(fBak);		
		    }else{
			f.delete();
		    }
		}else{
		    f.renameTo(fBak);
		}
	    }
	    fStream = new DataOutputStream(new FileOutputStream(f));
	}catch(IOException e){
	    System.out.println("LogFile:"+e);
	    System.exit(1);
	}
	started = true;
	writeHeader();
    }

    public static void stop() {
	writeFooter();
	try{
	    fStream.close();
	}catch(IOException e){
	}
    }

    private static void writeFooter() {
	try{
	    fStream.writeBytes("Completed: " +
			       DateFormat.getDateTimeInstance(DateFormat.FULL,
			       DateFormat.FULL,I18N.locale).format(new Date())
			       + newLine);
	    fStream.writeBytes(newLine);
	}catch(IOException e){
	}
    }

    private static void writeHeader(){
	String[] logNames = {
	    "CRITICAL",
	    "WARNING",
	    "INFORMATION",
	    "DEBUG",
	    "DEVELOPMENT"
	};
	    
	try{
	    fStream.writeBytes("Started:" +
			       DateFormat.getDateTimeInstance(DateFormat.FULL,
			       DateFormat.FULL,I18N.locale).format(new Date())
			       + newLine);
	    fStream.writeBytes("Log Level: " + logNames[logLevel] + newLine);
	    fStream.writeBytes(newLine);
	}catch(IOException e){
	}
    }

}
