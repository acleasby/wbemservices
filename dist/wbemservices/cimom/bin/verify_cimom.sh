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
# This Bourne shell script sets up the environment to run
# test programs to verify that the CIMOM is operating properly.
#

PATH=/bin:/usr/bin:/usr/sbin:$PATH
ECHO="echo"

# If JDK is not set, use a reasonable default
if [ -z $JAVA_HOME ]; then
 export JAVA_HOME="/usr/java"
fi

if [ ! -d $JAVA_HOME ]; then
 echo
 echo The JAVA_HOME environment variable must be set to an
 echo existing copy of a JDK.
 echo
 echo Example: export JAVA_HOME=$JAVA_HOME
 echo
 exit 2
fi

# If java is not in /usr/bin, edit as needed
JAVA="$JAVA_HOME/bin/java -classpath"

WBSERHOME=`cd ../..;pwd`
WBSERLIB=$WBSERHOME/lib
VERIFY=$WBSERHOME/examples/client/verify

WBEM=$WBSERLIB/wbem.jar

CLASSPATH=".:$VERIFY:$WBEM"

# NOTE: The following is a short-term workaround
mkdir -p /var/sadm/wbem/security

CLASS1=CIM_Collection
CLASS2=CIM_LogicalElement
HOST=localhost

if [ $# -lt 2 ] ; then
	echo "Usage: verify_cimom.sh <user> <password> [host] [http_class] [rmi_class]"
	exit 1
fi

if [ $# -gt 0 ] ; then
	USER=$1
fi

if [ $# -gt 1 ] ; then
	PASSWORD=$2
fi

if [ $# -gt 2 ] ; then
	HOST=$3
fi

if [ $# -gt 3 ] ; then
	CLASS1=$4
fi

if [ $# -gt 4 ] ; then
	CLASS2=$5
fi

PROTOCOL=HTTP
$ECHO
$ECHO "*********************************************************"
$ECHO "Running program to get class definition for $CLASS1 over $PROTOCOL"
$ECHO "*********************************************************"
$ECHO

CMD="$JAVA $CLASSPATH SimpleGetClass $HOST $USER $PASSWORD $CLASS1 $PROTOCOL"
$ECHO $CMD
$CMD

$ECHO
$ECHO "*********************************************************"
$ECHO "Running program to enumerate subclasses for $CLASS1 over $PROTOCOL"
$ECHO "*********************************************************"
$ECHO

CMD="$JAVA $CLASSPATH EnumClasses $HOST $USER $PASSWORD $CLASS1 $PROTOCOL"
$ECHO $CMD
$CMD

PROTOCOL=RMI
$ECHO
$ECHO "*********************************************************"
$ECHO "Running program to get class definition for $CLASS2 over $PROTOCOL"
$ECHO "*********************************************************"
$ECHO

CMD="$JAVA $CLASSPATH SimpleGetClass $HOST $USER $PASSWORD $CLASS2 $PROTOCOL"
$ECHO $CMD
$CMD

$ECHO
$ECHO "*********************************************************"
$ECHO "Running program to enumerate subclasses for $CLASS2 over $PROTOCOL"
$ECHO "*********************************************************"
$ECHO

CMD="$JAVA $CLASSPATH EnumClasses $HOST $USER $PASSWORD $CLASS2 $PROTOCOL"
$ECHO $CMD
$CMD


