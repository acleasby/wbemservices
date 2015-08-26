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
#are Copyright ¨ 2002 Sun Microsystems, Inc.
#
#All Rights Reserved.
#
#Contributor(s): _______________________________________
#

#
# This Bourne shell script sets up the environment to run
# the example programs
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

CLASSPATH=".:$WBEM"

# Get username and password
if [ "$2" = "" ]; then
	echo "USAGE: sh run_examples.sh user password [protocol]"
	echo "   where protocol is http (the default) or rmi"
	exit 1
fi

USER=$1
PASSWD=$2

PROTOCOL=HTTP
if [ "$3" = "" -o "$3" = "http" -o "$3" = "HTTP" ]; then
	PROTOCOL=HTTP
fi

if [ "$3" = "rmi" -o "$3" = "RMI" ]; then
	PROTOCOL=RMI
fi

# NOTE: The following is a short-term workaround
mkdir -p /var/sadm/wbem/security


# Edit to target another system
HOST=localhost

# Copy provider class files to CIMOM directory
$CP $EXAMPLES/provider/sfl/*.class $CIMOMLIB
$CP $EXAMPLES/provider/tsa/*.class $CIMOMLIB
$CP $EXAMPLES/provider/sip/*.class $CIMOMLIB

MOF=SFL_Provider.mof
CLASS=EX_SFLProvider
$ECHO
$ECHO "*********************************************************"
$ECHO "Creating $CLASS class"
$ECHO "*********************************************************"
$ECHO
cd $WBSERBIN
$CP $EXAMPLES/provider/sfl/$MOF .
sh mofcomp.sh $USER $PASSWD $MOF
$RM $MOF

cd $EXAMPLES/client/enumeration
CLASS=EX_SFLProvider
PROGRAM=ClientEnum
$ECHO
$ECHO "*********************************************************"
$ECHO "Running $PROGRAM on $CLASS over $PROTOCOL"
$ECHO "*********************************************************"
$ECHO

$ECHO $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL

cd $EXAMPLES/client/batching
CLASS=EX_SFLProvider
PROGRAM=TestBatch
$ECHO
$ECHO "*********************************************************"
$ECHO "Running $PROGRAM on $CLASS over $PROTOCOL"
$ECHO "*********************************************************"
$ECHO

$ECHO $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL

cd $EXAMPLES/client/query
CLASS=EX_SFLProvider
PROGRAM=TestQuery
$ECHO
$ECHO "*********************************************************"
$ECHO "Running $PROGRAM on $CLASS over $PROTOCOL"
$ECHO "*********************************************************"
$ECHO

$ECHO $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $PROTOCOL \'select \* from $CLASS\'
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $PROTOCOL 'select * from EX_SFLProvider'

cd $EXAMPLES/client/query
CLASS=EX_SFLProvider
PROGRAM=ExampleQuery
$ECHO
$ECHO "*********************************************************"
$ECHO "Running $PROGRAM on $CLASS over $PROTOCOL"
$ECHO "*********************************************************"
$ECHO

$ECHO $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $PROTOCOL
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $PROTOCOL

cd $EXAMPLES/client/class
CLASS=EX_SFLProvider
PROGRAM=DeleteClass
$ECHO
$ECHO "*********************************************************"
$ECHO "Running $PROGRAM on $CLASS over $PROTOCOL"
$ECHO "*********************************************************"
$ECHO

$ECHO $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL

cd $EXAMPLES/client/namespace
PARENT=root
CHILD=child
PROGRAM=DeleteNameSpace
$ECHO
$ECHO "*********************************************************"
$ECHO "Running $PROGRAM to delete /$PARENT/$CHILD over $PROTOCOL"
$ECHO "Should get CIM_ERR_INVALID_NAMESPACE error"
$ECHO "*********************************************************"
$ECHO

$ECHO $JAVA $CLASSPATH $PROGRAM $HOST $PARENT $CHILD $USER $PASSWD $PROTOCOL
$JAVA $CLASSPATH $PROGRAM $HOST $PARENT $CHILD $USER $PASSWD $PROTOCOL

cd $EXAMPLES/client/namespace
PROGRAM=CreateNameSpace
$ECHO
$ECHO "*********************************************************"
$ECHO "Running $PROGRAM to create /$PARENT/$CHILD over $PROTOCOL"
$ECHO "*********************************************************"
$ECHO

$ECHO $JAVA $CLASSPATH $PROGRAM $HOST $PARENT $CHILD $USER $PASSWD $PROTOCOL
$JAVA $CLASSPATH $PROGRAM $HOST $PARENT $CHILD $USER $PASSWD $PROTOCOL

cd $EXAMPLES/client/namespace
PROGRAM=DeleteNameSpace
$ECHO
$ECHO "*********************************************************"
$ECHO "Running $PROGRAM to delete /$PARENT/$CHILD over $PROTOCOL"
$ECHO "*********************************************************"
$ECHO

$ECHO $JAVA $CLASSPATH $PROGRAM $HOST $PARENT $CHILD $USER $PASSWD $PROTOCOL
$JAVA $CLASSPATH $PROGRAM $HOST $PARENT $CHILD $USER $PASSWD $PROTOCOL

MOF=SimpleInstanceProvider.mof
CLASS=EX_SimpleInstanceProvider
$ECHO
$ECHO "*********************************************************"
$ECHO "Creating $CLASS class"
$ECHO "*********************************************************"
$ECHO
cd $WBSERBIN
$CP $EXAMPLES/provider/sip/$MOF .
sh mofcomp.sh $USER $PASSWD $MOF
$RM $MOF

cd $EXAMPLES/client/instance
CLASS=EX_SimpleInstanceProvider
PROGRAM=DeleteInstances
$ECHO
$ECHO "*********************************************************"
$ECHO "Running $PROGRAM on $CLASS over $PROTOCOL"
$ECHO "Should get CIM_ERR_NOT_SUPPORTED because its provider did not"
$ECHO "implement the deleteInstance method"
$ECHO "*********************************************************"
$ECHO

$ECHO $JAVA $CLASSPATH $PROGRAM $HOST $CLASS $USER $PASSWD $PROTOCOL
$JAVA $CLASSPATH $PROGRAM $HOST $CLASS $USER $PASSWD $PROTOCOL

cd $EXAMPLES/client/class
CLASS=EX_SimpleInstanceProvider
PROGRAM=DeleteClass
$ECHO
$ECHO "*********************************************************"
$ECHO "Running $PROGRAM on $CLASS over $PROTOCOL"
$ECHO "*********************************************************"
$ECHO

$ECHO $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL

MOF=TeacherStudent.mof
$ECHO
$ECHO "*********************************************************"
$ECHO "Creating EX_TeacherStudent, EX_Teacher, EX_Student classes"
$ECHO "*********************************************************"
$ECHO
cd $WBSERBIN
$CP $EXAMPLES/provider/tsa/$MOF .
sh mofcomp.sh $USER $PASSWD $MOF
$RM $MOF

cd $EXAMPLES/client/class
CLASS=EX_Teacher
PROGRAM=DeleteClass
$ECHO
$ECHO "*********************************************************"
$ECHO "Running $PROGRAM on $CLASS over $PROTOCOL"
$ECHO "Should get CIM_ERR_FAILED error because association 
EX_TeacherStudent"
$ECHO "has active references to EX_Teacher"
$ECHO "*********************************************************"
$ECHO

$ECHO $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL

cd $EXAMPLES/client/class
CLASS=EX_Student
PROGRAM=DeleteClass
$ECHO
$ECHO "*********************************************************"
$ECHO "Running $PROGRAM on $CLASS over $PROTOCOL"
$ECHO "Should get CIM_ERR_FAILED error because association 
EX_TeacherStudent"
$ECHO "has active references to EX_Student"
$ECHO "*********************************************************"
$ECHO

$ECHO $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL

cd $EXAMPLES/client/class
CLASS=EX_TeacherStudent
PROGRAM=DeleteClass
$ECHO
$ECHO "*********************************************************"
$ECHO "Running $PROGRAM on $CLASS over $PROTOCOL"
$ECHO "*********************************************************"
$ECHO

$ECHO $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL

cd $EXAMPLES/client/class
CLASS=EX_Teacher
PROGRAM=DeleteClass
$ECHO
$ECHO "*********************************************************"
$ECHO "Running $PROGRAM on $CLASS over $PROTOCOL"
$ECHO "*********************************************************"
$ECHO

$ECHO $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL

cd $EXAMPLES/client/class
CLASS=EX_Student
PROGRAM=DeleteClass
$ECHO
$ECHO "*********************************************************"
$ECHO "Running $PROGRAM on $CLASS over $PROTOCOL"
$ECHO "*********************************************************"
$ECHO

$ECHO $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL

$RM $CIMOMLIB/*.class

