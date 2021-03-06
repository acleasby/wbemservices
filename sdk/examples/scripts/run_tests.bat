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
rem are Copyright � 2002 Sun Microsystems, Inc.
rem 
rem All Rights Reserved.
rem 
rem Contributor(s): _______________________________________
rem 

rem
rem This is a batch file that runs some of the example programs
rem 

rem JAVA_HOME points to the top directory of the installed
rem version of the JDK.  Normally set in build.bat for
rem top down builds.  Edit the next line as needed.
rem set JAVA_HOME=c:\jdk1.2.2

if "%JAVA_HOME%"=="" set JAVA_HOME=c:\j2sdk1.4.1_02
if exist %JAVA_HOME%\LICENSE goto JAVA_OK
echo.
echo The JAVA_HOME environment variable must be set to an
echo existing copy of the Java SDK.
echo.
echo Example: set JAVA_HOME=c:\j2sdk1.4.1_02
echo.
goto END

:JAVA_OK

rem If java is not here, edit as needed
set JAVA=%JAVA_HOME%\bin\java -classpath

rem The Unix equivalent of the following is WBSERHOME=`cd ..;pwd`
@echo off
@cd ..
echo @prompt set WBSERHOME=$p$_ > tmp1.bat
command /e:4096 /c tmp1.bat > tmp2.bat
call tmp2.bat
del tmp?.bat

rem Path to directory where jar files were created. Edit as needed
set WBSERLIB=%WBSERHOME%\lib
set WBSERBIN=%WBSERHOME%\bin
set CIMOMDIR=%WBSERHOME%\cimom
set CIMOMLIB=%CIMOMDIR%\lib
set EXAMPLES=%WBSERHOME%\examples
cd %EXAMPLES%

set WBEM=%WBSERLIB%\wbem.jar

set CLASSPATH=.;%WBEM%

:HELP
if not "%3" == "" goto RUN
echo USAGE: test_query user passwd protocol [host]
echo    where protocol is http (default) or rmi
goto END

:RUN
rem NOTE: The following is a short-term workaround
mkdir \var
mkdir \var\sadm
mkdir \var\sadm\wbem
mkdir \var\sadm\wbem\security

set USER=%1
set PASSWD=%2
set PROTOCOL=%3

if "%4" == "" set HOST=localhost
if not "%4" == "" set HOST=%4

call test_connect.bat %USER% %PASSWD% %PROTOCOL% %HOST%
pause
call test_namespace.bat %USER% %PASSWD% %PROTOCOL% %HOST%
pause
call test_qualifier.bat %USER% %PASSWD% interop %PROTOCOL% %HOST%
pause
call test_base_class.bat %USER% %PASSWD% %PROTOCOL% %HOST%
pause
call test_base_instance.bat %USER% %PASSWD% %PROTOCOL% %HOST%
pause
call test_assoc_class.bat %USER% %PASSWD% %PROTOCOL% %HOST%
pause
call test_method.bat %USER% %PASSWD% %PROTOCOL% %HOST%
pause
call test_property.bat %USER% %PASSWD% %PROTOCOL% %HOST%
pause
call test_query.bat %USER% %PASSWD% %PROTOCOL% %HOST%
pause
call test_lifecycle_event.bat %USER% %PASSWD% %PROTOCOL% %HOST%

@echo off
:END
rem Batch file terminated by error.
rem Can't use exit because it terminates the DOS window
