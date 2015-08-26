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
# This Bourne shell script is used to stop the CIMOM
#

PATH=/bin:/usr/bin:/usr/sbin:$PATH
RM="rm -f"
ECHO="echo"
PWD="pwd"
TAIL="tail"
HEAD="head"

# Assumes that all WBEM Services executables are in current directory
WBSERHOME=`cd ../..;pwd`
CIMOMDIR=$WBSERHOME/cimom
CIMOMBIN=$CIMOMDIR/bin
WBSERLIB=$WBSERHOME/lib
CIMOMLIB=$CIMOMDIR/lib
WBLOGDIR=$CIMOMDIR/logr

# start_cimom.sh saved the pid here
CIMOMPID=$WBLOGDIR/cimomserver.pid

pid=`$HEAD -1 $CIMOMPID`

if [ -f $CIMOMPID ]; then
	# cimomserver.pid found
	pid=`$HEAD -1 $CIMOMPID`
	ps=`$TAIL -1 $CIMOMPID`
	$ECHO "*********************************************************"
	$ECHO "CIMOM is PID $pid"
	$ECHO "$ps"
	$ECHO "As root user, run kill -9 $pid"
	$ECHO "*********************************************************"
else
	# cimomserver.pid not found
	$ECHO "*********************************************************"
	$ECHO "Cannot determine PID of CIMOM"
	$ECHO "Try running ps -eaf | grep java to find it"
	$ECHO "Then as root user, run kill -9 PID"
	$ECHO "*********************************************************"
fi

