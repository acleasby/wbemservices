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
	echo "USAGE: sh test_query.sh user password protocol [host]"
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
cp $EXAMPLES/client/query/*_Provider.class $CIMOMLIB

if [ "$HOST" = "localhost" ]; then
	MOF=TestExecQuery.mof
	cd $WBSERBIN
	sh mofcomp.sh $USER $PASSWD $EXAMPLES/client/query/$MOF
fi

pause() {
echo Press Enter to continue . . .
read key
return 0
}

CLASSPATH=".:$WBEM"
PROGRAM=CreateInstance
CLASS=Class_Q
cd $EXAMPLES/client/instance
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyQ=Q.1 $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyQ=Q.1 $PROTOCOL 

PROGRAM=SetProperty
CLASS=Class_Q
cd $EXAMPLES/client/property
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyQ=Q.1 sint32Q=3:sint32 objectPath $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyQ=Q.1 sint32Q=3:sint32 objectPath $PROTOCOL 
pause

PROGRAM=CreateInstance
CLASS=Class_Q
cd $EXAMPLES/client/instance
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyQ=Q.2 $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyQ=Q.2 $PROTOCOL 

PROGRAM=SetProperty
CLASS=Class_Q
cd $EXAMPLES/client/property
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyQ=Q.2 sint32Q=-3:sint32 objectPath $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyQ=Q.2 sint32Q=-3:sint32 objectPath $PROTOCOL 
pause

PROGRAM=CreateInstance
CLASS=Class_R
cd $EXAMPLES/client/instance
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyQ=R.1 $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyQ=R.1 $PROTOCOL 

PROGRAM=SetProperty
CLASS=Class_R
cd $EXAMPLES/client/property
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyQ=R.1 sint32R=-27:sint32 objectPath $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyQ=R.1 sint32R=-27:sint32 objectPath $PROTOCOL 

PROGRAM=SetProperty
CLASS=Class_R
cd $EXAMPLES/client/property
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyQ=R.1 sint32Q=7:sint32 objectPath $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyQ=R.1 sint32Q=7:sint32 objectPath $PROTOCOL 
pause

PROGRAM=CreateInstance
CLASS=Class_R
cd $EXAMPLES/client/instance
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyQ=R.2 $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyQ=R.2 $PROTOCOL 

PROGRAM=SetProperty
CLASS=Class_R
cd $EXAMPLES/client/property
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyQ=R.2 sint32Q=-7:sint32 objectPath $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyQ=R.2 sint32Q=-7:sint32 objectPath $PROTOCOL 

PROGRAM=SetProperty
CLASS=Class_R
cd $EXAMPLES/client/property
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyQ=R.2 sint32R=27:sint32 objectPath $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyQ=R.2 sint32R=27:sint32 objectPath $PROTOCOL 
pause

PROGRAM=EnumerateInstanceNames
CLASS=Class_Q
cd $EXAMPLES/client/instance
echo "**********************************************"
echo "Running $PROGRAM on $CLASS  over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 
pause

PROGRAM=EnumerateInstances
CLASS=Class_Q
cd $EXAMPLES/client/instance
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS objectPath null $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS objectPath null $PROTOCOL 
pause

PROGRAM=TestExecQuery
CLASS=Class_Q
cd $EXAMPLES/client/query
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD query1.wql $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD query1.wql $PROTOCOL 
pause

PROGRAM=TestExecQuery
CLASS=Class_Q
cd $EXAMPLES/client/query
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD query2.wql $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD query2.wql $PROTOCOL 
pause

PROGRAM=TestExecQuery
CLASS=Class_Q
cd $EXAMPLES/client/query
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD query3.wql $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD query3.wql $PROTOCOL 
pause

PROGRAM=TestExecQuery
CLASS=Class_R
cd $EXAMPLES/client/query
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD query4.wql $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD query4.wql $PROTOCOL 
pause

 set PROGRAM=TestExecQuery
CLASS=Class_R
cd $EXAMPLES/client/query
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD query5.wql $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD query5.wql $PROTOCOL 
pause

PROGRAM=DeleteInstance
CLASS=Class_Q
cd $EXAMPLES/client/instance
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyQ=Q.1 $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyQ=Q.1 $PROTOCOL 

PROGRAM=DeleteInstance
CLASS=Class_Q
cd $EXAMPLES/client/instance
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyQ=Q.2 $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyQ=Q.2 $PROTOCOL 

PROGRAM=DeleteInstance
CLASS=Class_R
cd $EXAMPLES/client/instance
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyQ=R.1 $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyQ=R.1 $PROTOCOL 

PROGRAM=DeleteInstance
CLASS=Class_R
cd $EXAMPLES/client/instance
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyQ=R.2 $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyQ=R.2 $PROTOCOL 

PROGRAM=DeleteClass
CLASS=Class_R
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 

PROGRAM=DeleteClass
CLASS=Class_Q
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 

# Remove Provider class files
$RM $CIMOMLIB/*_Provider.class

cd $EXAMPLES
