#!/bin/bash

set -e

VERSION=1.0.3
REPOSITORY=releases

mvn deploy:deploy-file -Durl="https://admin:n1c3NC0mpl3x!@nexus.infinio.com/content/repositories/$REPOSITORY" -Dfile=dist/wbemservices/bin/mofcomp.jar -DgroupId=org.sourceforge.wbemservices -DartifactId=mofcomp -Dversion=$VERSION -Dpackaging=jar

mvn deploy:deploy-file -Durl="https://admin:n1c3NC0mpl3x!@nexus.infinio.com/content/repositories/$REPOSITORY" -Dfile=dist/wbemservices/cimom/lib/cimom.jar -DgroupId=org.sourceforge.wbemservices -DartifactId=cimom -Dversion=$VERSION -Dpackaging=jar
mvn deploy:deploy-file -Durl="https://admin:n1c3NC0mpl3x!@nexus.infinio.com/content/repositories/$REPOSITORY" -Dfile=dist/wbemservices/cimom/lib/cimrepository.jar -DgroupId=org.sourceforge.wbemservices -DartifactId=cimrepository -Dversion=$VERSION -Dpackaging=jar
mvn deploy:deploy-file -Durl="https://admin:n1c3NC0mpl3x!@nexus.infinio.com/content/repositories/$REPOSITORY" -Dfile=dist/wbemservices/cimom/lib/wbemstartup.jar -DgroupId=org.sourceforge.wbemservices -DartifactId=wbemstartup -Dversion=$VERSION -Dpackaging=jar

mvn deploy:deploy-file -Durl="https://admin:n1c3NC0mpl3x!@nexus.infinio.com/content/repositories/$REPOSITORY" -Dfile=dist/wbemservices/lib/wbem.jar -DgroupId=org.sourceforge.wbemservices -DartifactId=wbem -Dversion=$VERSION -Dpackaging=jar

mvn deploy:deploy-file -Durl="https://admin:n1c3NC0mpl3x!@nexus.infinio.com/content/repositories/$REPOSITORY" -Dfile=./wbemservices.zip -DgroupId=org.sourceforge.wbemservices -DartifactId=wbemservices -Dversion=$VERSION -Dpackaging=zip
