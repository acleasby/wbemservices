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
 *Contributor(s): 
 *                WBEM Solutions, Inc.
 */

package org.wbemservices.wbem.compiler.mofc;



import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.wbem.cimxml.CIMXml;
import javax.wbem.cimxml.CIMXmlFactory;
import javax.wbem.cim.*;

/**
 * Converts the CIM classes defined in a MOF file to XML files,
 * and creates an xml index file. The index file lists the CIM classes
 * that are processed and includes the superclass of each class
 * and whether the class is an association class.  This index file
 * can be used with an XSL style sheet and XSL processor to Generate 
 * HTML pages from the XML files.
 *
 * The XmlWriter class is called from CIM_Mofc.jj to open the 
 * XML index file before parsing a MOF file. The CIM_Mofc.jj 
 * file then calls mofcBackend.java to generate Java classes 
 * for the CIM elements parsed in the MOF file.
 *
 * If the mofcomp command was executed with the -x argument, 
 * the addClass method in the mofcBackend.java class calls the 
 * writeClass method in the XmlWriter class to write an XML file. 
 * The addClass method also calls the WriteAllClasses methods in
 * the XmlWriter class to add an index entry for
 * the class to the XML index file.
 *
 * @author      Sun Microsystems, Inc.
 * @version     1.76, 08/22/00
 * @since       WBEM 2.4
 */
class XmlWriter {

    // Bufferered writer attached to XML index file.
    private BufferedWriter xmlOut;
    private BufferedWriter bigxmlOut;
    CIMXml xi = CIMXmlFactory.getCIMXmlImpl();

    // XML index file
    private String fileName = "mof.xml";
    private String bigfileName = "bigmof.xml";
    private String className = "";

    // Required XML start file tag
    private String startTag = "<\u003Fxml version=\"1.0\"\u003F>"; 

  
    public XmlWriter() {
	// set I18N Bundle
	I18N.setResourceName("org.wbemservices.wbem.compiler.mofc.Compiler");

    }

    BufferedWriter getFile() {
        return xmlOut;
    }

    BufferedWriter getBigFile() {
        return bigxmlOut;
    }

    BufferedWriter openXmlFile() { 

        try {
            File xmlFile = new File(fileName);
            if (xmlFile.exists()) {
	        xmlFile.delete();
	    }
            BufferedWriter out = new BufferedWriter(new FileWriter
                    (fileName, true));
            xmlOut = out;
            writeStartTags(out);
        } catch (IOException e) {
	    System.err.println(I18N.loadStringFormat("CANNOT_CREATE_FILE", fileName));
	    System.exit(1);
        }
        return xmlOut;
    }

   BufferedWriter openBigXmlFile() { 

        try {
            File bigxmlFile = new File(bigfileName);
            if (bigxmlFile.exists()) {
	        bigxmlFile.delete();
	    }
            BufferedWriter out = new BufferedWriter(new FileWriter
                    (bigfileName, true));
            bigxmlOut = out;
            writeBigStartTags(out);
        } catch (IOException e) {
	    System.err.println(I18N.loadStringFormat("CANNOT_CREATE_FILE", fileName));
	    System.exit(1);
        }
        return bigxmlOut;
    }
                  
    void writeBigStartTags(BufferedWriter out) {
        try {
	    out.write(startTag);
            out.newLine();
            out.write("<CIM CIMVERSION=\"2.0\" DTDVERSION=\"2.0\">");
	    out.newLine();
	    out.write("<DECLARATION>");
	    out.newLine();
	    out.write("<DECLGROUP>");
	    out.newLine();
            //out.write("<VALUE.OBJECT>");
            //out.newLine();
        } catch (IOException e) {
	    System.err.println(I18N.loadStringFormat("NO_SUCH_FILE", fileName));
	    System.exit(1);
	    return;
        }
    }

    void writeStartTags(BufferedWriter out) {
        try {
            out.write(startTag);
            out.newLine();
	    out.write("<CLASSES>");
	    out.newLine();
        } catch (IOException e) {
	    System.err.println(I18N.loadStringFormat("NO_SUCH_FILE", fileName));
	    System.exit(1);
	    return;
        }
    }

    void writeEndTags(BufferedWriter out) {
        try {
            out.write("</CLASSES>");
	    out.newLine();
	    out.close();
        } catch (IOException e) {
	    System.err.println(I18N.loadStringFormat("NO_SUCH_FILE", fileName));
	    System.exit(1);
	    return;
        }
    }

    void writeBigEndTags(BufferedWriter out) {
        try {
            //out.write("</VALUE.OBJECT>");
	    //out.newLine();
            out.write("</DECLGROUP>");
	    out.newLine();
            out.write("</DECLARATION>");
	    out.newLine();
            out.write("</CIM>");
	    out.newLine();
	    out.close();
        } catch (IOException e) {
	    System.err.println(I18N.loadStringFormat("NO_SUCH_FILE", fileName));
	    System.exit(1);
	    return;
        }
    }

    void writeAllClasses(CIMClass curClassEl, BufferedWriter out) {
             
        try {
	    out.write("<CLASS NAME=\"");
	    out.write(curClassEl.getName());
	    if (curClassEl.getSuperClass().length() != 0) {
                out.write("\" SUPERCLASS=\"");
       	        out.write(curClassEl.getSuperClass());
            }
            if (curClassEl.isAssociation()) {
	        out.write("\" ASSOCIATION=\"true");
            }
	        out.write("\" />");
	        out.newLine();
        } catch (IOException e) {
	    System.err.println(I18N.loadStringFormat("NO_SUCH_FILE", fileName));
	    System.exit(1);
	    return;
        }
    }


    void writeBigXml(CIMQualifierType curQualEl, BufferedWriter out) {
         try {
	     out.write(xi.CIMQualifierTypeToXml(curQualEl));
             out.newLine();
        } catch (IOException e) {
	    System.err.println(I18N.loadStringFormat("NO_SUCH_FILE", fileName));
	    System.exit(1);
	    return;
        }
    }

    void writeBigXml(CIMClass curClassEl, BufferedWriter out) {
         try {
	     out.write("         <VALUE.OBJECT>");
	     out.write(xi.CIMClassToXml(curClassEl, true, true, null, true));
	     out.write("         </VALUE.OBJECT>");
	     out.newLine();
        } catch (IOException e) {
	    System.err.println(I18N.loadStringFormat("NO_SUCH_FILE", fileName));
	    System.exit(1);
	    return;
        }
    }

    void writeClass(CIMClass curClassEl) {
	try {
  	    className = curClassEl.getName()+".xml";
            BufferedWriter out = new BufferedWriter
                (new FileWriter(curClassEl.getName() 
                 +".xml", false));
            out.write(startTag);
            out.newLine();
            out.write("<CIM CIMVERSION=\"2.0\" DTDVERSION=\"2.0\">");
            out.newLine();
            out.write("   <DECLARATION>");
            out.newLine();
            out.write("      <DECLGROUP>");
	    out.newLine();
	    out.write("         <VALUE.OBJECT>");
	    out.newLine();
	    out.write(xi.CIMClassToXml(curClassEl, true, true, null, true));
	    out.write("         </VALUE.OBJECT>");
	    out.newLine();
	    out.write("      </DECLGROUP>");
	    out.newLine();
	    out.write("   </DECLARATION>");
	    out.newLine();
	    out.write("</CIM>");
	    out.newLine();
	    out.close();
	} catch (IOException e) {
	    System.err.println(I18N.loadStringFormat("CANNOT_CREATE_FILE", className));
	    System.exit(1);
        }
    } 


}
