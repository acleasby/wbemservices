@echo off
setlocal
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

rem This batch file sets up the environment to run test programs 
rem to verify that the CIMOM is operating properly.

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

rem The Unix equivalent of the following is WBSERHOME=`cd ..\..;pwd`
@echo off
@cd ..\..
echo @prompt set WBSERHOME=$p$_ > tmp1.bat
command /e:4096 /c tmp1.bat > tmp2.bat
call tmp2.bat
del tmp?.bat

rem Setup the directory variables
set WBSERLIB=%WBSERHOME%\lib
set CIMOMBIN=%WBSERHOME%\cimom\bin
set VERIFY=%WBSERHOME%\examples\client\verify

set WBEM=%WBSERLIB%\wbem.jar

set CLASSPATH=.;%VERIFY%;%WBEM%
set HOST=localhost

if "%2" == "" goto USAGE

if not "%1" == "" set USER=%1
if not "%2" == "" set PASSWORD=%2
if not "%3" == "" set HOST=%3
if not "%4" == "" set CLASS1=%4
if not "%5" == "" set CLASS2=%5
goto DOIT

:USAGE
echo "Usage: verify_cimom.bat <user> <password> [host] [http_class] [rmi_class]"
goto END

:DOIT

rem NOTE: The following is a short-term workaround
mkdir \var
mkdir \var\sadm
mkdir \var\sadm\wbem
mkdir \var\sadm\wbem\security

set CLASS=CIM_Collection
set CLASS=CIM_LogicalElement

cd %CIMOMBIN%
rem Edit to reference another CIM class
set PROTOCOL=HTTP

echo *********************************************************
echo Running program to get class definition for %CLASS% over %PROTOCOL%
echo *********************************************************

@echo on
%JAVA% %CLASSPATH% %DEBUG% SimpleGetClass %HOST% %USER% %PASSWORD% %CLASS% %PROTOCOL%
@echo off

echo *********************************************************
echo Running program to enumerate subclasses for %CLASS% over %PROTOCOL%
echo *********************************************************

@echo on
%JAVA% %CLASSPATH% EnumClasses %HOST% %USER% %PASSWORD% %CLASS% %PROTOCOL%
@echo off

rem Edit to reference another CIM class
set PROTOCOL=RMI

echo *********************************************************
echo Running program to get class definition for %CLASS% over %PROTOCOL%
echo *********************************************************

@echo on
%JAVA% %CLASSPATH% SimpleGetClass %HOST% %USER% %PASSWORD% %CLASS% %PROTOCOL%
@echo off

echo *********************************************************
echo Running program to enumerate subclasses for %CLASS% over %PROTOCOL%
echo *********************************************************

@echo on
%JAVA% %CLASSPATH% EnumClasses %HOST% %USER% %PASSWORD% %CLASS% %PROTOCOL%
@echo off

:END
rem Batch file terminated by error.
rem Can't use exit because it terminates the DOS window.
