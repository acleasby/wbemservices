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

import java.io.*;
import java.util.*;

/**
 * A tool to create java beans from mof files
 */
public class mof2java {
    private static void usage() {
        System.err.println("Usage: mof2java -out dest_dir [-package package_name] mof_files... ");
        System.exit(1);
    }

    /**
     * The main method
     * 
     * @param args the arguments
     */
    public static void main(String args[]) {
        if(args.length == 0) {
            usage();
        }

        String destDir     = null;
        String packageName = null;

        int filestart = 0;
        for(int i = 0; i < args.length; i++){
            if(args[i].charAt(0) == '-') {
                if(args[i].equals("-out")) {
                    i++;
                    destDir = args[i];
                } else if(args[i].equals("-package")) {
                    i++;
                    packageName = args[i];
                } else {
                    System.err.println("Unknown option: " + args[i]);
                    usage();
                }
            } else {
                filestart = i;
                break;
            }
        }

        if(destDir == null) {
            System.err.println("dest_dir not specified.");
            usage();
        }

        File dir = new File(destDir);
        if(!dir.exists()) {
            System.out.println("Creating directory: " + dir.getAbsolutePath());
            dir.mkdirs();
        }

        for(int i = filestart; i < args.length; i++) {
            String mof = args[i];
            try {
                String line = null;
                String newline = null;
                BufferedReader bin = new BufferedReader(new FileReader(mof));
                while( (line = bin.readLine()) != null ) {
                    // Only look at lines beginning with 'class'
                    if( line.startsWith("class") ) {
                        // If no { found, read one more line
                        if( line.indexOf("{") == -1 )
                            line = line + bin.readLine();
                        // Might not be any white space before/after : or before {
                        // Replace with space, else StringTokenize does not parse properly
                        newline = line;
                        if( line.indexOf(":") != -1 )
                            newline = line.replace(':', ' ');
                        if( line.indexOf("{") != -1 )
                            newline = newline.replace('{', ' ');
                        // Assume format is 'class classname : superclass {'
                        StringTokenizer stLine = new StringTokenizer(newline);
                        // Eat the class keyword
                        stLine.nextToken();
                        String className = stLine.nextToken();
                        FileOutputStream fout = new FileOutputStream(destDir + File.separator + className + ".java");
                        PrintStream psout = new PrintStream(fout);
                        if( packageName != null )
                            psout.println("package " + packageName + ";");
                        if( line.indexOf(":") != -1 ) {
                            newline = line.replace('{', ' ');
                            String superName = stLine.nextToken();
                            psout.println("public class " + className + " extends " + superName + " {");
                        } else {
                            psout.println("public class " + className + " {");
                        }
                        psout.println("}");
                        fout.close();
                        psout.close();
                    }
                }
            } catch(Exception e) {
                System.out.println("Current directory: " + new File(".").getAbsolutePath());
                System.out.println("Exception processing " + mof + ": " + e);
                System.exit(2);
            }
        }
    }
}
