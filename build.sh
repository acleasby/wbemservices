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
#are Copyright ¿ 2002 Sun Microsystems, Inc.
#
#All Rights Reserved.
#
#Contributor(s): _______________________________________
#

#
# This is a Bourne shell script creates and packages the
# WBEM Services executables from scratch using ANT and
# build.xml.
#

# If JDK is not set, use a reasonable default
if [ -z "$JAVA_HOME" ]; then
 JAVA_HOME="/usr/java"
 export JAVA_HOME
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

# If ant is not set, use a reasonable default
if [ -z "$ANT_HOME" ]; then
 ANT_HOME="/usr/jakarta-ant-1.4"
 export ANT_HOME
fi

ANT="$ANT_HOME/bin/ant"

if [ ! -x $ANT ]; then
 echo
 echo The ANT_HOME environment variable must be set to an
 echo existing copy of Ant.
 echo
 echo Example: export ANT_HOME=$ANT_HOME
 echo
 exit 2
fi

# Start the build using ANT
$ANT -Dant.build.javac.source=1.6 -Dant.build.javac.target=1.6 $*
