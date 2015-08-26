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
# This Bourne shell script is used to start the CIMOM.
#

PATH=/bin:/usr/bin:/usr/sbin:$PATH
RM="rm -f"
PWD="pwd"
ECHO="echo"
ID="id"
GREP="grep"
PS="ps -eaf"
TAIL="tail"

# If JDK is not in /usr/java, edit as needed
if [[ -z $JAVA_HOME ]]; then
    JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64/jre
fi 

# If java is not in /usr/bin, edit as needed
JAVA="$JAVA_HOME/bin/java -classpath"

# Defines directories where WBEM jar files are located
WBSERHOME=`cd ..;pwd`
WBSERBIN=$WBSERHOME/bin
WBSERLIB=$WBSERHOME/lib

CIMWORKSHOP=$WBSERBIN/cimworkshop.jar
WBEM=$WBSERLIB/wbem.jar

CLASSPATH="$WBSERLIB:$CIMWORKSHOP:$WBEM"

DEFINES="\
	-Dinstall.dir=$WBSERBIN"

CIMWSSERVER=org.wbemservices.wbem.apps.cimworkshop.CIMWorkshop

$ECHO "*********************************************************"
$ECHO "Starting up cimworkshop"
$ECHO "*********************************************************"

# Start cimworkshop
$ECHO $JAVA $CLASSPATH $DEFINES $CIMWSSERVER
$JAVA $CLASSPATH $DEFINES $CIMWSSERVER &

