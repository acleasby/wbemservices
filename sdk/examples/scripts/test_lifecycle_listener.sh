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
	echo "USAGE: sh test_lifecycle_listener.sh user password protocol [host]"
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

pause() {
echo Press Enter to continue . . .
read key
return 0
}

CLASSPATH=".:$WBEM"
PROGRAM=AddCIMListener
CLASS=Class_E
cd $EXAMPLES/client/event
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS create $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS create $PROTOCOL 

PROGRAM=AddCIMListener
CLASS=Class_E
cd $EXAMPLES/client/event
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS modify $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS modify $PROTOCOL 

PROGRAM=AddCIMListener
CLASS=Class_E
cd $EXAMPLES/client/event
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS read $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS read $PROTOCOL 

PROGRAM=AddCIMListener
CLASS=Class_E
cd $EXAMPLES/client/event
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS method $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS method $PROTOCOL 

PROGRAM=AddCIMListener
CLASS=Class_E
cd $EXAMPLES/client/event
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS delete $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS delete $PROTOCOL 

cd $EXAMPLES
