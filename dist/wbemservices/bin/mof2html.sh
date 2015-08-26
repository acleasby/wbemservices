#!/bin/sh
#
#EXHIBIT A - Sun Industry Standards Source License
#
#"The contents of this file are subject to the Sun Industry
#Standards Source License Version 1.2 (the "License");
#You may not use this file except in compliance with the
#License. You may obtain a copy of the 
#License at http://wbemservices.sourceforge.net/license.html
#
#Software distributed under the License is distributed on
#an "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either
#express or implied. See the License for the specific
#language governing rights and limitations under the License.
#
#The Original Code is WBEM Services.
#
#The Initial Developer of the Original Code is:
#Sun Microsystems, Inc.
#
#Portions created by: Sun Microsystems, Inc.
#are Copyright ¨ 2001 Sun Microsystems, Inc.
#
#All Rights Reserved.
#
#Contributor(s): _______________________________________
#

#
# This is a Bourne shell script that adds new MOF classes
# to the CIM Repository while the CIMOM is running.
# 

PATH=/bin:/usr/bin:/usr/sbin:$PATH
RM="rm -f"
MKDIR="mkdir -p"
ECHO="echo"
PWD="pwd"

# If JDK is not in /usr/java, edit as needed
JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64/jre

# If java is not in /usr/bin, edit as needed
JAVA="$JAVA_HOME/bin/java -classpath"

# If xslt is not in /usr/xalan-j_2_2_D11/bin, edit as needed
XALAN_HOME="/usr/xalan-j_2_3_1/bin"

# Path to directory where jar files were created. Edit as needed
WBSERHOME=`cd ..;pwd`
WBSERBIN=$WBSERHOME/bin
WBSERLIB=$WBSERHOME/lib
WBSERMOF=$WBSERHOME/mof
CIMOMDIR=$WBSERHOME/cimom
CIMOMLIB=$CIMOMDIR/lib
XMLDIR=$WBSERBIN/xml

WBEM=$WBSERLIB/wbem.jar
CIMOM=$CIMOMLIB/cimom.jar
MOFC=$WBSERBIN/mofcomp.jar
REPOSITORY=$CIMOMLIB/cimrepository.jar
XALAN=$XALAN_HOME/xalan.jar


if [ $# -lt 1 ] ; then
    echo "Usage: sh mof2html.sh mof_file"
    exit 
else
	MOF=$1
fi

CLASSPATH=.:$MOFC:$CIMOM:$WBEM:$REPOSITORY
MOFCOMPILE=org.wbemservices.wbem.compiler.mofc.CIM_Mofc
FLAGS="-v -x -b -o $XMLDIR"

$ECHO "*********************************************************"
$ECHO "Creating XML files from $MOF in $XMLDIR"
$ECHO "*********************************************************"

$RM -r $XMLDIR
# Assume the needed MOF files are in WBSERMOF
cd $WBSERMOF

echo $JAVA $CLASSPATH $MOFCOMPILE $FLAGS $MOF
$JAVA $CLASSPATH $MOFCOMPILE $FLAGS $MOF

mv *.xml $XMLDIR

CLASSPATH=.:$XALAN:$XALAN_HOME/xml-apis.jar:$XALAN_HOME/xercesImpl.jar
XSLT=org.apache.xalan.xslt.Process
INDEX=$WBSERBIN/index.xsl
STYLE=$WBSERBIN/multimof.xsl

$ECHO "*********************************************************"
$ECHO "Creating HTML files from XML files in $XMLDIR"
$ECHO "*********************************************************"

echo $JAVA $CLASSPATH -Xmx128m $XSLT -in $XMLDIR/mof.xml -xsl $INDEX -out $XMLDIR/mof.html
$JAVA $CLASSPATH -Xmx128m $XSLT -in $XMLDIR/mof.xml -xsl $INDEX -out $XMLDIR/mof.html

cp $XMLDIR/mof.xml $WBSERBIN
echo $JAVA $CLASSPATH -Xmx128m $XSLT -in $XMLDIR/bigmof.xml -xsl $STYLE -out $XMLDIR/mof.html
$JAVA $CLASSPATH -Xmx128m $XSLT -in $XMLDIR/bigmof.xml -xsl $STYLE -out $XMLDIR/mof.html
cp $WBSERBIN/mof.css $XMLDIR

# Remove unneeded files
rm -f $WBSERBIN/mof.xml
rm -f $XMLDIR/*.xml
rm -f $XMLDIR/store
rm -f $XMLDIR/Version_Number
rm -f $XMLDIR/Snapshot.*
rm -f $XMLDIR/Logfile.*

$ECHO "*********************************************************"
$ECHO "In a browser, open $XMLDIR/index.html"
$ECHO "*********************************************************"

cd $WBSERBIN
