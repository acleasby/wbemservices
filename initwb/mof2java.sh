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
#are Copyright © 2002 Sun Microsystems, Inc.
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
JAVA_HOME="/usr/java"

# If java is not in /usr/bin, edit as needed
JAVA="$JAVA_HOME/bin/java -classpath"

WBSERBIN=`pwd`
WBSERHOME=`cd ..;pwd`

JAVALIBS="$JAVA_HOME/jre/lib/rt.jar:$JAVA_HOME/lib/tools.jar"
CLASSPATH=.:$WBSERBIN:$JAVALIBS

MOFCOMPILE=org.wbemservices.wbem.compiler.mofc.CIM_Mofc

if [ $# -lt 3 ] ; then
    echo "Usage: sh mof2java.sh mof_file dest_dir pkg_name"
    exit 
else
	MOF=$1
	SCHEMA_DIR=$2
	PKG=$3
fi

$ECHO "*********************************************************"
$ECHO "Creating the .java file for each class in $MOF"
$ECHO "*********************************************************"


cd $WBSERBIN
echo $JAVA $CLASSPATH mof2java $MOF $SCHEMA_DIR $PKG
$JAVA $CLASSPATH mof2java $MOF $SCHEMA_DIR $PKG
cd $WBSERBIN

