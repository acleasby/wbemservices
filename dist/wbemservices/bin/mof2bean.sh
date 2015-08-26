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
#are Copyright © 2001 Sun Microsystems, Inc.
#
#All Rights Reserved.
#
#Contributor(s): _______________________________________
#

#
# This Bourne shell script generates JavaBeans from the
# specified MOF definition. It must be run while the CIMOM
# is running.
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

# Path to directory where jar files were created. Edit as needed
WBSERHOME=`cd ..;pwd`
WBSERBIN=$WBSERHOME/bin
WBSERLIB=$WBSERHOME/lib
WBSERMOF$WBSERHOME/mof
CIMOMDIR=$WBSERHOME/cimom
CIMOMLIB=$CIMOMDIR/lib
BEANDIR=$WBSERBIN/bean

WBEM=$WBSERLIB/wbem.jar
CIMOM=$CIMOMLIB/cimom.jar
MOFC=$WBSERBIN/mofcomp.jar
REPOSITORY=$CIMOMLIB/cimrepository.jar

CLASSPATH=.:$MOFC:$CIMOM:$WBEM:$REPOSITORY

MOFCOMPILE=org.wbemservices.wbem.compiler.mofc.CIM_Mofc

if [ $# -lt 4 ] ; then
    echo "Usage: sh mofcomp.sh user passwd bean_config mof_file"
    exit 
else
	USER=$1
	PASSWD=$2
	BEAN=$3
	MOF=$4
fi

FLAGS="-u $USER -p $PASSWD -o $BEANDIR"

# Remove any old repository in  the bean output directory
# But do not delete any previously generated JavaBean files
$MKDIR $BEANDIR
$RM $BEANDIR/Snapshot.*
$RM $BEANDIR/Logfile.*
$RM $BEANDIR/Store
$RM $BEANDIR/Version_Number

$ECHO "*********************************************************"
$ECHO "Generating JavaBeans from $MOF in $BEANDIR"
$ECHO "*********************************************************"

# Execute in the MOF directory to handle #pragam Include
# NOTE: Works even if BEAN and MOF are relative paths, 
# because WBSERMOF and WBSERBIN are at the same directory level
cd $WBSERMOF
echo $JAVA $CLASSPATH $MOFCOMPILE $FLAGS -j $BEAN $MOF
$JAVA $CLASSPATH $MOFCOMPILE $FLAGS -j $BEAN $MOF

cd $WBSERBIN
