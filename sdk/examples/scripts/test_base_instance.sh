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
	echo "USAGE: sh test_base_instance.sh user password protocol [host]"
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
cp $EXAMPLES/client/instance/*_Provider.class $CIMOMLIB

if [ "$HOST" = "localhost" ]; then
	MOF=TestClass.mof
	cd $WBSERBIN
	sh mofcomp.sh $USER $PASSWD $EXAMPLES/client/class/$MOF
fi

pause() {
echo Press Enter to continue . . .
read key
return 0
}

CLASSPATH=".:$WBEM"
PROGRAM=CreateInstance
CLASS=Class_A1
cd $EXAMPLES/client/instance
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyA1=A1.1 $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyA1=A1.1 $PROTOCOL 

PROGRAM=CreateInstance
CLASS=Class_B2
cd $EXAMPLES/client/instance
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyA1=B2.1 $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyA1=B2.1 $PROTOCOL 

PROGRAM=CreateInstance
CLASS=Class_C2
cd $EXAMPLES/client/instance
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyA1=C2.1 $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyA1=C2.1 $PROTOCOL 

PROGRAM=CreateInstance
CLASS=Class_D2
cd $EXAMPLES/client/instance
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyA1=D2.1 $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyA1=D2.1 $PROTOCOL 

PROGRAM=CreateInstance
CLASS=Class_E3
cd $EXAMPLES/client/instance
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyA1=E3.1 $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyA1=E3.1 $PROTOCOL 

PROGRAM=EnumerateInstanceNames
CLASS=Class_A1
cd $EXAMPLES/client/instance
echo "**********************************************"
echo "Running $PROGRAM on $CLASS  over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 
pause

PROGRAM=EnumerateInstances
CLASS=Class_A1
cd $EXAMPLES/client/instance
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS objectPath null $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS objectPath null $PROTOCOL 
pause

PROGRAM=SetInstance
CLASS=Class_A1
cd $EXAMPLES/client/instance
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyA1=A1.1 stringA1=StringA1.1:string objectPath $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyA1=A1.1 stringA1=StringA1.1:string objectPath $PROTOCOL 

PROGRAM=GetInstance
CLASS=Class_A1
cd $EXAMPLES/client/instance
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyA1=A1.1 null objectPath $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyA1=A1.1 null objectPath $PROTOCOL 
pause

PROGRAM=SetInstance
CLASS=Class_B2
cd $EXAMPLES/client/instance
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyA1=B2.1 stringA1=StringB2.1:string objectPath $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyA1=B2.1 stringA1=StringB2.1:string objectPath $PROTOCOL 

PROGRAM=SetInstance
CLASS=Class_B2
cd $EXAMPLES/client/instance
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyA1=B2.1 uint32B2=32:uint32 propertyList $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyA1=B2.1 uint32B2=32:uint32 propertyList $PROTOCOL 
pause

PROGRAM=GetInstance
CLASS=Class_B2
cd $EXAMPLES/client/instance
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyA1=B2.1 null localOnly $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyA1=B2.1 null localOnly $PROTOCOL 
pause

PROGRAM=SetInstance
CLASS=Class_C2
cd $EXAMPLES/client/instance
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyA1=C2.1 stringA1=StringC2.1:string objectPath $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyA1=C2.1 stringA1=StringC2.1:string objectPath $PROTOCOL 

PROGRAM=SetInstance
CLASS=Class_C2
cd $EXAMPLES/client/instance
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyA1=C2.1 sint32C2=-32:sint32 !propertyList $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyA1=C2.1 sint32C2=-32:sint32 !propertyList $PROTOCOL 

PROGRAM=GetInstance
CLASS=Class_C2
cd $EXAMPLES/client/instance
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyA1=C2.1 null includeQualifiers $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyA1=C2.1 null includeQualifiers $PROTOCOL 
pause

PROGRAM=SetInstance
CLASS=Class_D2
cd $EXAMPLES/client/instance
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyA1=D2.1 stringA1=StringD2.1:string objectPath $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyA1=D2.1 stringA1=StringD2.1:string objectPath $PROTOCOL 

PROGRAM=SetInstance
CLASS=Class_D2
cd $EXAMPLES/client/instance
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyA1=D2.1 booleanD2=false:boolean propertyList $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyA1=D2.1 booleanD2=false:boolean propertyList $PROTOCOL 

PROGRAM=GetInstance
CLASS=Class_D2
cd $EXAMPLES/client/instance
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyA1=D2.1 null !propertyList $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyA1=D2.1 null !propertyList $PROTOCOL 
pause

PROGRAM=SetInstance
CLASS=Class_E3
cd $EXAMPLES/client/instance
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyA1=E3.1 stringA1=StringE3.1:string objectPath $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyA1=E3.1 stringA1=StringE3.1:string objectPath $PROTOCOL 

PROGRAM=SetInstance
CLASS=Class_E3
cd $EXAMPLES/client/instance
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyA1=E3.1 uint32B2=32:uint32 propertyList $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyA1=E3.1 uint32B2=32:uint32 propertyList $PROTOCOL 

PROGRAM=SetInstance
CLASS=Class_E3
cd $EXAMPLES/client/instance
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyA1=E3.1 real32E3=32.5:real32 !propertyList $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyA1=E3.1 real32E3=32.5:real32 !propertyList $PROTOCOL 

PROGRAM=GetInstance
CLASS=Class_E3
cd $EXAMPLES/client/instance
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyA1=E3.1 real32E3 propertyList $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyA1=E3.1 real32E3 propertyList $PROTOCOL 
pause

PROGRAM=EnumerateInstances
CLASS=Class_A1
cd $EXAMPLES/client/instance
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS objectPath null $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS objectPath null $PROTOCOL 
pause

PROGRAM=EnumerateInstances
CLASS=Class_B2
cd $EXAMPLES/client/instance
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS deep null $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS deep null $PROTOCOL 
pause

PROGRAM=EnumerateInstances
CLASS=Class_C2
cd $EXAMPLES/client/instance
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS localOnly null $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS localOnly null $PROTOCOL 
pause

PROGRAM=EnumerateInstances
CLASS=Class_D2
cd $EXAMPLES/client/instance
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS includeQualifiers null $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS includeQualifiers null $PROTOCOL 
pause

PROGRAM=EnumerateInstances
CLASS=Class_E3
cd $EXAMPLES/client/instance
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS includeClassOrigin null $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS includeClassOrigin null $PROTOCOL 
pause

PROGRAM=EnumerateInstances
CLASS=Class_A1
cd $EXAMPLES/client/instance
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS !propertyList null $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS !propertyList null $PROTOCOL 
pause

PROGRAM=EnumerateInstances
CLASS=Class_B2
cd $EXAMPLES/client/instance
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS propertyList uint32B2 $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS propertyList uint32B2 $PROTOCOL 
pause

PROGRAM=DeleteInstance
CLASS=Class_A1
cd $EXAMPLES/client/instance
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyA1=A1.1 $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyA1=A1.1 $PROTOCOL 

PROGRAM=DeleteInstance
CLASS=Class_B2
cd $EXAMPLES/client/instance
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyA1=B2.1 $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyA1=B2.1 $PROTOCOL 

PROGRAM=DeleteInstance
CLASS=Class_C2
cd $EXAMPLES/client/instance
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyA1=C2.1 $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyA1=C2.1 $PROTOCOL 

PROGRAM=DeleteInstance
CLASS=Class_D2
cd $EXAMPLES/client/instance
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyA1=D2.1 $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyA1=D2.1 $PROTOCOL 

PROGRAM=DeleteInstance
CLASS=Class_E3
cd $EXAMPLES/client/instance
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyA1=E3.1 $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS keyA1=E3.1 $PROTOCOL 

PROGRAM=DeleteClass
CLASS=Class_F3
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 

PROGRAM=DeleteClass
CLASS=Class_E3
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 

PROGRAM=DeleteClass
CLASS=Class_B2
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 

PROGRAM=DeleteClass
CLASS=Class_C2
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 

PROGRAM=DeleteClass
CLASS=Class_D2
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 

PROGRAM=DeleteClass
CLASS=Class_A1
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 

# Remove Provider class files
$RM $CIMOMLIB/*_Provider.class

cd $EXAMPLES
