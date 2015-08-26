@echo off
rem 
rem EXHIBIT A - Sun Industry Standards Source License
rem 
rem "The contents of this file are subject to the Sun Industry
rem Standards Source License Version 1.2 (the "License");
rem You may not use this file except in compliance with the
rem License. You may obtain a copy of the 
rem License at http://wbemservices.sourceforge.net/license.html
rem 
rem Software distributed under the License is distributed on
rem an "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either
rem express or implied. See the License for the specific
rem language governing rights and limitations under the License.
rem 
rem The Original Code is WBEM Services.
rem 
rem The Initial Developer of the Original Code is:
rem Sun Microsystems, Inc.
rem 
rem Portions created by: Sun Microsystems, Inc.
rem are Copyright (c) 2002 Sun Microsystems, Inc.
rem 
rem All Rights Reserved.
rem 
rem Contributor(s): _______________________________________
rem 

rem 
rem This batch file creates a document describing the
rem classes in the MOF file in JavaDoc form.
rem

rem JAVA_HOME points to JDK. Edit next line as needed.
if "%JAVA_HOME%"=="" set JAVA_HOME=c:\j2sdk1.4.1
if exist %JAVA_HOME%\LICENSE goto JAVA_OK
echo.
echo The JAVA_HOME environment variable must be set to an
echo existing copy of the Java SDK.
echo.
echo Example: set JAVA_HOME=c:\j2sdk1.4.1
echo.
goto END

:JAVA_OK

rem If javadoc is not here, edit as needed
set JAVA=%JAVA_HOME%\bin\java

rem If javadoc is not here, edit as needed
set JAVADOC=%JAVA_HOME%\bin\javadoc

rem WBSERBIN points to the top directory of the source tree.
rem The Unix equivalent of the following is WBSERBIN=`pwd`
@echo off
echo @prompt set WBSERBIN=$p$_ > tmp1.bat
command /e:4096 /c tmp1.bat > tmp2.bat
call tmp2.bat
del tmp?.bat

rem The Unix equivalent of the following is WBSERHOME=`cd ..;pwd`
@echo off
@cd ..
echo @prompt set WBSERHOME=$p$_ > tmp1.bat
command /e:4096 /c tmp1.bat > tmp2.bat
call tmp2.bat
del tmp?.bat

set JAVALIBS=%JAVA_HOME%\jre\lib\rt.jar;%JAVA_HOME%\lib\tools.jar
set CLASSPATH=.;%WBSERBIN%;%JAVALIBS%

if not "%1" == "" goto RUN
echo USAGE: mof2java mof_file dest_dir pkg_name
cd %WBSERBIN%
goto END

:RUN
set MOF=%1
set SCHEMA_DIR=%2
set PKG=%3

cd %WBSERBIN%
echo *********************************************************
echo Creating the .java file for each class in %MOF%
echo *********************************************************

@echo on
%JAVA% -classpath %CLASSPATH% mof2java %MOF% %SCHEMA_DIR% %PKG%
@echo off
:END
cd %WBSERBIN%
