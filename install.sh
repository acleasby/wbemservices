#!/bin/bash

set -e

VERSION=`cat mvn-version.txt`
REPOSITORY=snapshots

mvn install:install-file -Dfile=dist/wbemservices/bin/mofcomp.jar -DgroupId=org.sourceforge.wbemservices -DartifactId=mofcomp -Dversion=$VERSION -Dpackaging=jar

mvn install:install-file -Dfile=dist/wbemservices/cimom/lib/cimom.jar -DgroupId=org.sourceforge.wbemservices -DartifactId=cimom -Dversion=$VERSION -Dpackaging=jar
mvn install:install-file -Dfile=dist/wbemservices/cimom/lib/cimrepository.jar -DgroupId=org.sourceforge.wbemservices -DartifactId=cimrepository -Dversion=$VERSION -Dpackaging=jar
mvn install:install-file -Dfile=dist/wbemservices/cimom/lib/wbemstartup.jar -DgroupId=org.sourceforge.wbemservices -DartifactId=wbemstartup -Dversion=$VERSION -Dpackaging=jar

mvn install:install-file -Dfile=dist/wbemservices/lib/wbem.jar -DgroupId=org.sourceforge.wbemservices -DartifactId=wbem -Dversion=$VERSION -Dpackaging=jar

mvn install:install-file -Dfile=./wbemservices.zip -DgroupId=org.sourceforge.wbemservices -DartifactId=wbemservices -Dversion=$VERSION -Dpackaging=zip
