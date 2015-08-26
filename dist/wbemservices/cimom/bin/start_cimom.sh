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

# If JDK is not set, use a reasonable default
if [ -z $JAVA_HOME ]; then
 export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64/jre
fi

if [ ! -d $JAVA_HOME ]; then
 echo
 echo The JAVA_HOME environment variable must be set to an
 echo existing copy of a JDK.
 echo
 echo Example: export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64/jre
 echo
 exit 2
fi

# If java is not in /usr/bin, edit as needed
JAVA="$JAVA_HOME/bin/java -classpath"

# Define locations for WBEM jar files
WBSERHOME=`cd ../..;pwd`
CIMOMDIR=$WBSERHOME/cimom
CIMOMBIN=$CIMOMDIR/bin
WBSERLIB=$WBSERHOME/lib
CIMOMLIB=$CIMOMDIR/lib
WBLOGDIR=$CIMOMDIR/logr

STARTUP=$CIMOMLIB/wbemstartup.jar

CLASSPATH=$STARTUP

JVM_DEFINES="\
	-Xmx128m \
	-Djava.security.manager \
	-Djava.security.policy=$CIMOMBIN/cimom.policy" 

# Define location of WBEMServices.properties
WBSERFILE=$CIMOMBIN/WBEMServices_Unix.properties

DEBUG="\
	-DDebugLevel=3 \
	-DDebugDevice=stdout"

CIMOM_DEFINES="\
	-DBaseDir=$WBSERHOME \
	-Dpropdir=$CIMOMBIN \
	-Dlogdir=$WBLOGDIR"

CIMOMSERVER=org.wbemservices.wbem.bootstrap.StartWBEMServices
CIMOMPID=$WBLOGDIR/cimomserver.pid

# Must be root 
$ID | $GREP uid=0\( 1>/dev/null 2>&1
if [ $? -ne 0 ]; then
        $ECHO "You must be root to run this command"
        exit 1
fi

$ECHO "*********************************************************"
$ECHO "Starting up the CIMOM"
$ECHO "*********************************************************"

# Start the CIMOM in full debug mode
#$ECHO $JAVA $CLASSPATH $JVM_DEFINES $CIMOM_DEFINES $DEBUG $CIMOMSERVER $WBSERFILE &
#$JAVA $CLASSPATH $JVM_DEFINES $CIMOM_DEFINES $DEBUG $CIMOMSERVER $WBSERFILE &

# Start the CIMOM in non debug mode
$ECHO $JAVA $CLASSPATH $JVM_DEFINES $CIMOM_DEFINES $CIMOMSERVER $WBSERFILE &
$JAVA $CLASSPATH $JVM_DEFINES $CIMOM_DEFINES $CIMOMSERVER $WBSERFILE &

pid=$!

# Save its pid and ps output in file cimomserver.pid 
# for stop_cimom.sh
$RM $CIMOMPID
$ECHO $pid > $CIMOMPID
$PS | $GREP $pid | $GREP java >> $CIMOMPID

$ECHO "*********************************************************"
$ECHO "CIMOM has started up as PID $pid "
$TAIL -1 $CIMOMPID
$ECHO "*********************************************************"


