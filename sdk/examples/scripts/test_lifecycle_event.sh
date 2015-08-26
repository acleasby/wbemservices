#!/bin/sh
#
#EXHIBIT A - Sun Industry Standards Source License
#
#"The contents of this file are subject to the Sun Industry
#Standards Source License Version 1.2 (the "License"):
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
# This is a batch file that runs some of the example programs
# 

PATH=/bin:/usr/bin:/usr/sbin:$PATH
ECHO="echo"
PWD="pwd"
CP="cp -f"
RM="rm -f"

# If JDK is not in /usr/java, edit as needed
JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64/jre

# If java is not in /usr/bin, edit as needed
JAVA="$JAVA_HOME/bin/java -classpath"

WBSERHOME=`cd ..;pwd`
WBSERLIB=$WBSERHOME/lib
WBSERBIN=$WBSERHOME/bin
CIMOMDIR=$WBSERHOME/cimom
CIMOMLIB=$CIMOMDIR/lib
EXAMPLES=$WBSERHOME/examples

WBEM=$WBSERLIB/wbem.jar


# Get username and password
if [ "$3" = "" ]; then
	echo "USAGE: sh test_lifecycle_event.sh user password protocol [host]"
	echo "    where protocol is http (default) or rmi"
	exit 1
fi

USER=$1
PASSWD=$2
PROTOCOL=$3

HOST=localhost
if [ "$4" != "" ]; then
	HOST=$4
fi

# NOTE: The following is a short-term workaround
mkdir -p /var/sadm/wbem/security

# Copy provider class files
cp $EXAMPLES/client/event/*_Provider.class $CIMOMLIB

if [ "$HOST" = "localhost" ]; then
	MOF=TestIntrinsicEvents.mof
	cd $WBSERBIN
	sh mofcomp.sh $USER $PASSWD $EXAMPLES/client/event/$MOF
fi

pause() {
echo Press Enter to continue . . .
read key
return 0
}

echo
echo Run sh test_lifecycle_listener.sh $USER $PASSWD $PROTOCOL $HOST in another window
echo "When test_lifecycle_listener.sh shows: Waiting for event CIM_InstCreation"
pause

CLASSPATH=".:$WBEM"
PROGRAM=CreateInstance
CLASS=Class_E
cd $EXAMPLES/client/instance
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyE=E.1 $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyE=E.1 $PROTOCOL 

PROGRAM=EnumerateInstanceNames
CLASS=Class_E
cd $EXAMPLES/client/instance
echo "**********************************************"
echo "Running $PROGRAM on $CLASS  over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 
echo
echo "When test_lifecycle_listener.sh shows: Waiting for event CIM_InstModification"
pause

PROGRAM=SetInstance
CLASS=Class_E
cd $EXAMPLES/client/instance
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyE=E.1 uint32E=1:uint32 objectPath $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyE=E.1 uint32E=1:uint32 objectPath $PROTOCOL 
echo
echo "When test_lifecycle_listener.sh shows: Waiting for event CIM_InstRead"
pause

PROGRAM=GetInstance
CLASS=Class_E
cd $EXAMPLES/client/instance
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyE=E.1 null includeQualifiers $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyE=E.1 null includeQualifiers $PROTOCOL 
echo
echo "When test_lifecycle_listener.sh shows: Waiting for event CIM_InstMethodCall"
pause

PROGRAM=InvokeCIMMethod
CLASS=Class_E
cd $EXAMPLES/client/method
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyE=E.1 Sum $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyE=E.1 Sum $PROTOCOL 
echo
echo "When test_lifecycle_listener.sh shows: Waiting for event CIM_InstDeletion"
pause

PROGRAM=DeleteInstance
CLASS=Class_E
cd $EXAMPLES/client/instance
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyE=E.1 $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyE=E.1 $PROTOCOL 
echo
echo "When test_lifecycle_listener.sh completes"
pause

PROGRAM=DeleteClass
CLASS=Class_E
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 

# Remove Provider class files
$RM $CIMOMLIB/*_Provider.class

cd $EXAMPLES
