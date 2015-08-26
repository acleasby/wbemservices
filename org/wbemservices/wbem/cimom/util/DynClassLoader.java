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
 *are Copyright © 2002 Sun Microsystems, Inc.
 *
 *All Rights Reserved.
 *
 *Contributor(s): _______________________________________
*/

package org.wbemservices.wbem.cimom.util;

import java.net.*;

import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;

/** A dynamic classloader.
 *  This class can be used to create a dynamic class loader. Its main use
 *  is by the CIMOM's adpater factory as each protocol gets its own classloader
 *  This allows adapters to be added dynamically
 *
 * @author  Jim Marshall (jmars@east.sun.com)
 * @version 1.0
 */
public class DynClassLoader extends URLClassLoader {
    /** The base directory for WBEM Services
     *  This variable specifies where the base install directory for
     *  WBEM Services. <I>For Example:</I><BR>
     *  <UL>
     *  <LI>On Solaris this would be set to the BASEDIR set when the WBEM Services
     *  package is installed (<CODE>pkgparam SUNWwbcou BASEDIR</CODE>)<LI>
     *  <LI>On Windows this would be set to the drive WBEM Services was installed
     *  on (<CODE>C:\</CODE>)</LI>
     *  </UL>
     *  This can be used by putting a <CODE>{0}</CODE> in the string passed into
     *  the addClassToURL(String) function or any of the constructors which take
     *  a <CODE>String</CODE>
     */
    public static final String BASEDIR = System.getProperty("BaseDir", "");
    
    /** The file URL specifier */
    static final String FILEURL = "file:";

    /** Create a new instance with the passed in path as a URL
     *  @param pPath The path to use, see addToClassPath(String) for format of
     *  string
     *  @param pParent The parent class loader - pass null for primordial
     *  @see #addToClassPath(String)
     */
    public DynClassLoader(String pPath, ClassLoader pParent) {
        super(new URL[0], pParent);
        addToClassPath(pPath);
    }
    
    /** Create a new instance using the passed in paths
     *  Creates a classloader with the passed in paths as the classpath
     *  @param pPaths The paths to add, see addToClassPath(String) for format of
     *  individual strings
     *  @param pParent The parent class loader - pass null for primordial
     *  @see #addToClassPath(String)
     */
    public DynClassLoader(String[] pPaths, ClassLoader pParent) {
        super(new URL[0], pParent);
        for (int count = 0; count < pPaths.length; count++) {
		addToClassPath(pPaths[count]);
        }
    }
    
    /** Creates a new instance with the classpath set to the passed in URLs
     *  Creates an instance with the passed in URLs
     *  @param urls the URLs used in the classpath, this URL is not modified in
     *  anyway.
     *  @param parent The parent class loader - pass null for primordial
     */
    public DynClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    /** Creates new DynClassLoader
     *  @param pParent The parent class loader - pass null for primordial
     */
    public DynClassLoader(ClassLoader pParent) {
        super(new URL[0], pParent);
    }
    
    /** Adds a URL to the classloaders classpath
     *  This function adds the passed in path to the classloader classpath
     *  @param url The URL to add, the URL is not modified in anyway
     */
    public void addToClassPath(URL url) {
        try {
            addURL(url);
        }
        catch(Exception e) {
            System.out.println("Error adding URL: " + url);
            e.printStackTrace();
        }
    }

    /** Adds a path to the classloaders classpath
     *  This function adds the path to the classloaders classpath, teh string
     *  should be in the format of a URL.<BR>
     *  <B>NOTE</B> - If the passed in path is <CODE>file:</CODE> and also
     *  contains <CODE>{0}</CODE> after the protocol specifier the contents of
     *  the <I>BASEDIR</I> variable will be placed between the
     *  <CODE>file:</CODE> and the rest of the URL.
     *  @param path The path to add, should be in the form of a URL, e.g.
     *  <CODE>String path = "file:/usr/home/foo.jar";</CODE>
     *  @see #BASEDIR
     */
    public void addToClassPath(String path) {
        if (path == null) {
            return;
        }
        try {
            String spec = path;
            // if it's a file url, see if we need to insert BASEDIR
            if (path.regionMatches(true, 0, FILEURL, 0, FILEURL.length())){
                Object argument[] = {DynClassLoader.BASEDIR};
                spec = java.text.MessageFormat.format(path, argument);
            }
            addToClassPath(new URL(spec));
        }
        catch(MalformedURLException e) {
            System.out.println("Malformed URL: " + path);
            e.printStackTrace();
        }
    }

    /** Returns the permissions for the given codesource object.
     * Returns the permissions for the given codesource object. The
     * implementation of this method grants all permissions as classes loaded
     * with this classloader as all classes will be loaded by the super user
     * @param cs the codesource
     * @return the permissions granted to the codesource
     */
    protected PermissionCollection getPermissions(CodeSource cs) {
        PermissionCollection pc =
        (new AllPermission()).newPermissionCollection();
        pc.add(new AllPermission());
        return pc;
    }
}
