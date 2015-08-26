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
	echo "USAGE: sh test_assoc_class.sh user password protocol [host]"
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
	MOF=TestAssoc.mof
	cd $WBSERBIN
	sh mofcomp.sh $USER $PASSWD $EXAMPLES/client/class/$MOF
	cd $EXAMPLES
fi

pause() {
echo Press Enter to continue . . .
read key
return 0
}

CLASSPATH=".:$WBEM"
PROGRAM=EnumerateClassNames
CLASS=Root_Class
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS deep $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS deep $PROTOCOL 
pause

PROGRAM=EnumerateClasses
CLASS=Antecedent_Class
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS deep $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS deep $PROTOCOL 
pause

PROGRAM=EnumerateClasses
CLASS=Dependent_Class
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS deep $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS deep $PROTOCOL 
pause

PROGRAM=EnumerateClasses
CLASS=Other_Class
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS deep $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS deep $PROTOCOL 
pause

PROGRAM=EnumerateClassNames
CLASS=Other_Assoc
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS deep $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS deep $PROTOCOL 
pause

PROGRAM=EnumerateClasses
CLASS=Other_Assoc
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS includeClassOrigin $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS includeClassOrigin $PROTOCOL 
pause

PROGRAM=EnumerateClassNames
CLASS=Dependency_Assoc
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS deep $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS deep $PROTOCOL 
pause

PROGRAM=EnumerateClasses
CLASS=Dependency_Assoc
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS includeClassOrigin $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS includeClassOrigin $PROTOCOL 
pause

PROGRAM=AssociatorNames
CLASS=Class_A1
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS all_null null $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS all_null null $PROTOCOL 
pause

PROGRAM=AssociatorNames
CLASS=Class_A1
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS assocClass Assoc_A1D1 $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS assocClass Assoc_A1D1 $PROTOCOL 
pause

PROGRAM=AssociatorNames
CLASS=Class_A1
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS resultClass Class_D2 $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS resultClass Class_D2 $PROTOCOL 
pause

PROGRAM=AssociatorNames
CLASS=Class_A1
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS role Antecedent $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS role Antecedent $PROTOCOL 
pause

PROGRAM=AssociatorNames
CLASS=Class_A1
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS resultRole Dependent $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS resultRole Dependent $PROTOCOL 
pause

PROGRAM=Associators
CLASS=Class_A1
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS all_null null $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS all_null null $PROTOCOL 
pause

PROGRAM=Associators
CLASS=Class_A1
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS assocClass Assoc_A1D1 $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS assocClass Assoc_A1D1 $PROTOCOL 
pause

PROGRAM=Associators
CLASS=Class_A1
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS resultClass Class_D2 $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS resultClass Class_D2 $PROTOCOL 
pause

PROGRAM=Associators
CLASS=Class_A1
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS role Antecedent $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS role Antecedent $PROTOCOL 
pause

PROGRAM=Associators
CLASS=Class_A1
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS resultRole Dependent $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS resultRole Dependent $PROTOCOL 
pause

PROGRAM=Associators
CLASS=Class_A1
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS includeQualifier null $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS includeQualifier null $PROTOCOL 
pause

PROGRAM=Associators
CLASS=Class_A1
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS includeClassOrigin null $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS includeClassOrigin null $PROTOCOL 
pause

PROGRAM=Associators
CLASS=Class_A1
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS propertyList class_D1,dependentProperty $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS propertyList class_D1,dependentProperty $PROTOCOL 
pause

PROGRAM=ReferenceNames
CLASS=Class_A1
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS all_null null $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS all_null null $PROTOCOL 
pause

PROGRAM=ReferenceNames
CLASS=Class_A1
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS resultClass Assoc_A1D1 $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS resultClass Assoc_A1D1 $PROTOCOL 
pause

PROGRAM=ReferenceNames
CLASS=Class_A1
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS role Antecedent $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS role Antecedent $PROTOCOL 
pause

PROGRAM=References
CLASS=Class_A1
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS all_nullfalse null $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS all_nullfalse null $PROTOCOL 
pause

PROGRAM=References
CLASS=Class_A1
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS resultClass Assoc_A1D1 $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS resultClass Assoc_A1D1 $PROTOCOL 
pause

PROGRAM=References
CLASS=Class_A1
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS role Antecedent $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS role Antecedent $PROTOCOL 
pause

PROGRAM=References
CLASS=Class_A1
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS includeQualifier null $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS includeQualifier null $PROTOCOL 
pause

PROGRAM=References
CLASS=Class_A1
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS includeClassOrigin null $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS includeClassOrigin null $PROTOCOL 
pause

PROGRAM=References
CLASS=Class_A1
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS propertyList Assoc_A1D1,assocProperty $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS propertyList Assoc_A1D1,assocProperty $PROTOCOL 
pause

# Must delete associate classes before base classes
# Must delete children classes before parent classes
PROGRAM=DeleteClass
CLASS=Assoc_A1O1
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 

PROGRAM=DeleteClass
CLASS=Assoc_A1O2
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 

PROGRAM=DeleteClass
CLASS=Assoc_A1O3
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 

PROGRAM=DeleteClass
CLASS=Other_Assoc
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 

PROGRAM=DeleteClass
CLASS=Assoc_A1D1
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 

PROGRAM=DeleteClass
CLASS=Assoc_A1D2
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 

PROGRAM=DeleteClass
CLASS=Assoc_A1D3
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 

PROGRAM=DeleteClass
CLASS=Dependency_Assoc
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 

# Now delete base classes
# Must delete children classes before parent classes
PROGRAM=DeleteClass
CLASS=Class_D1
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
CLASS=Class_D3
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

PROGRAM=DeleteClass
CLASS=Class_O1
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 

PROGRAM=DeleteClass
CLASS=Class_O2
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 

PROGRAM=DeleteClass
CLASS=Class_O3
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 

PROGRAM=DeleteClass
CLASS=Antecedent_Class
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 

PROGRAM=DeleteClass
CLASS=Dependent_Class
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 

PROGRAM=DeleteClass
CLASS=Other_Class
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 

PROGRAM=DeleteClass
CLASS=Root_Class
cd $EXAMPLES/client/class
echo "**********************************************"
echo "Running $PROGRAM on $CLASS over $PROTOCOL"
echo "**********************************************"
echo $JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 
$JAVA $CLASSPATH $PROGRAM $HOST $USER $PASSWD $CLASS $PROTOCOL 

cd $EXAMPLES
