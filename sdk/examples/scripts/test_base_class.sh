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
	echo "USAGE: sh test_base_class.sh user password protocol [host]"
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
PROGRAM=EnumerateClasses
CLASS=Class_A1
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS objectPath $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS objectPath $PROTOCOL 
pause

PROGRAM=EnumerateClasses
CLASS=Class_A1
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS deep $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS deep $PROTOCOL 
pause

PROGRAM=EnumerateClasses
CLASS=Class_A1
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS localOnly $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS localOnly $PROTOCOL 
pause

PROGRAM=EnumerateClasses
CLASS=Class_A1
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS includeQualifiers $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS includeQualifiers $PROTOCOL 
pause

PROGRAM=EnumerateClasses
CLASS=Class_A1
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS includeClassOrigin $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS includeClassOrigin $PROTOCOL 
pause

PROGRAM=EnumerateClasses
CLASS=Class_A1
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS all_false $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS all_false $PROTOCOL 
pause

PROGRAM=GetClass
CLASS=Class_A1
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS objectPath $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS objectPath $PROTOCOL 
pause

PROGRAM=GetClass
CLASS=Class_B2
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS localOnly $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS localOnly $PROTOCOL 
pause

PROGRAM=GetClass
CLASS=Class_E3
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS includeQualifiers $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS includeQualifiers $PROTOCOL 
pause

PROGRAM=GetClass
CLASS=Class_B2
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS includeClassOrigin $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS includeClassOrigin $PROTOCOL 
pause

PROGRAM=GetClass
CLASS=Class_C2
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS !propertyList $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS !propertyList $PROTOCOL 
pause

PROGRAM=GetClass
CLASS=Class_D2
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS propertyList $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS propertyList $PROTOCOL 
pause

PROGRAM=GetClass
CLASS=Class_F3
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS objectPath $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS objectPath $PROTOCOL 
pause

PROGRAM=SetClass
CLASS=Class_F3
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 

PROGRAM=GetClass
CLASS=Class_F3
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS objectPath $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS objectPath $PROTOCOL 
pause

PROGRAM=CreateClass
CLASS=Class_G3
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS Class_D2 $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS Class_D2 $PROTOCOL 

PROGRAM=GetClass
CLASS=Class_G3
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS objectPath $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS objectPath $PROTOCOL 
pause

PROGRAM=EnumerateClassNames
CLASS=Class_A1
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS objectPath $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS objectPath $PROTOCOL 
pause

PROGRAM=EnumerateClassNames
CLASS=Class_A1
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS deep $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS deep $PROTOCOL 
pause

PROGRAM=EnumerateClassNames
CLASS=Class_A1
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS !deep $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS !deep $PROTOCOL 
pause

# Must delete children classes before parent classes
PROGRAM=DeleteClass
CLASS=Class_G3
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 

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
CLASS=Class_D2
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
CLASS=Class_B2
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

cd $EXAMPLES
