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
rem are Copyright © 2001 Sun Microsystems, Inc.
rem 
rem All Rights Reserved.
rem 
rem Contributor(s): _______________________________________
rem 

rem 
rem This batch file sets up environment variables and runs
rem CimWorkShop.
rem 

rem Set JAVA_HOME to the top directory of the installed
rem version of the jdk.  Edit the next line as needed.
rem set JAVA_HOME=c:\jdk1.2.2

if "%JAVA_HOME%"=="" set JAVA_HOME=c:\jdk1.2.2
if exist %JAVA_HOME%\LICENSE goto JAVA_OK
echo.
echo The JAVA_HOME environment variable must be set to an
echo existing copy of the Java SDK.
echo.
echo Example: set JAVA_HOME=c:\jdk1.2.2
echo.
goto END

:JAVA_OK


rem If java is not in /usr/bin, edit as needed
set JAVA=%JAVA_HOME%\bin\java -classpath

rem The Unix equivalent of the following is WBSERHOME=`cd ..;pwd`
@echo off
@cd ..
echo @prompt set WBSERHOME=$p$_ > tmp1.bat
command /e:4096 /c tmp1.bat > tmp2.bat
call tmp2.bat
del tmp?.bat

rem Defines directories where WBEM jar files are located
set WBSERBIN=%WBSERHOME%\bin
set WBSERLIB=%WBSERHOME%\lib

set CIMWORKSHOP=%WBSERBIN%\cimworkshop.jar
set WBEM=%WBSERLIB%\wbem.jar

set CLASSPATH=%WBSERLIB%;%CIMWORKSHOP%;%WBEM%%

set CIMWSSERVER=org.wbemservices.wbem.apps.cimworkshop.CIMWorkshop

echo.
echo *********************************************************
echo Starting up cimworkshop
echo *********************************************************
echo.
@echo on
cd %WBSERBIN%
%JAVA% %CLASSPATH% -Dinstall.dir=%WBSERBIN% %CIMWSSERVER%

@echo off
:END
rem Batch file terminated by error.
rem Can't use exit because it terminates the DOS window.
