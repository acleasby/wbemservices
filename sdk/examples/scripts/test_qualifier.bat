@echo off
setlocal
rem 
rem EXHIBIT A - Sun Industry Standards Source License
rem 
rem "The contents of this file are subject to the Sun Industry
rem Standards Source License Version 1.2 (the "License");
rem You may not use this file except in compliance with the
rem License. You may obtain a cd of the 
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
set USER=%1
set PASSWD=%2
set PARENT=%3
set PROTOCOL=HTTP
if "%4" == "HTTP" set PROTOCOL=HTTP
if "%4" == "http" set PROTOCOL=HTTP
if "%4" == "RMI" set PROTOCOL=RMI
if "%4" == "rmi" set PROTOCOL=RMI


rem NOTE: The following is a short-term workaround
mkdir \var
mkdir \var\sadm
mkdir \var\sadm\wbem
mkdir \var\sadm\wbem\security

if "%5" == "" set HOST=localhost
if not "%5" == "" set HOST=%5

:CREATE
set PROGRAM=CreateQualifierType
cd %EXAMPLES%\client\qualifier
echo **********************************************
echo Running %PROGRAM% on %HOST%:\%PARENT% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %PARENT% q_boolean boolean %PROTOCOL% 
@echo off

set PROGRAM=GetQualifierType
cd %EXAMPLES%\client\qualifier
echo **********************************************
echo Running %PROGRAM% on %HOST%:\%PARENT% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %PARENT% q_boolean %PROTOCOL% 
@echo off
pause

set PROGRAM=CreateQualifierType
cd %EXAMPLES%\client\qualifier
echo **********************************************
echo Running %PROGRAM% on %HOST%:\%PARENT% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %PARENT% q_uint32 uint32 %PROTOCOL% 
@echo off

set PROGRAM=GetQualifierType
cd %EXAMPLES%\client\qualifier
echo **********************************************
echo Running %PROGRAM% on %HOST%:\%PARENT% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %PARENT% q_uint32 %PROTOCOL% 
@echo off
pause

set PROGRAM=CreateQualifierType
cd %EXAMPLES%\client\qualifier
echo **********************************************
echo Running %PROGRAM% on %HOST%:\%PARENT% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %PARENT% q_uint64 uint64 %PROTOCOL% 
@echo off

set PROGRAM=GetQualifierType
cd %EXAMPLES%\client\qualifier
echo **********************************************
echo Running %PROGRAM% on %HOST%:\%PARENT% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %PARENT% q_uint64 %PROTOCOL% 
@echo off
pause

set PROGRAM=SetQualifierType
cd %EXAMPLES%\client\qualifier
echo **********************************************
echo Running %PROGRAM% on %HOST%:\%PARENT% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %PARENT% q_sint sint32 %PROTOCOL% 
@echo off

set PROGRAM=GetQualifierType
cd %EXAMPLES%\client\qualifier
echo **********************************************
echo Running %PROGRAM% on %HOST%:\%PARENT% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %PARENT% q_sint %PROTOCOL% 
@echo off
pause

set PROGRAM=SetQualifierType
cd %EXAMPLES%\client\qualifier
echo **********************************************
echo Running %PROGRAM% on %HOST%:\%PARENT% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %PARENT% q_sint sint64 %PROTOCOL% 
@echo off

set PROGRAM=GetQualifierType
cd %EXAMPLES%\client\qualifier
echo **********************************************
echo Running %PROGRAM% on %HOST%:\%PARENT% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %PARENT% q_sint %PROTOCOL% 
@echo off
pause

set PROGRAM=SetQualifierType
cd %EXAMPLES%\client\qualifier
echo **********************************************
echo Running %PROGRAM% on %HOST%:\%PARENT% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %PARENT% q_real real32 %PROTOCOL% 
@echo off

set PROGRAM=GetQualifierType
cd %EXAMPLES%\client\qualifier
echo **********************************************
echo Running %PROGRAM% on %HOST%:\%PARENT% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %PARENT% q_real %PROTOCOL% 
@echo off
pause

set PROGRAM=SetQualifierType
cd %EXAMPLES%\client\qualifier
echo **********************************************
echo Running %PROGRAM% on %HOST%:\%PARENT% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %PARENT% q_real real64 %PROTOCOL% 
@echo off

set PROGRAM=GetQualifierType
cd %EXAMPLES%\client\qualifier
echo **********************************************
echo Running %PROGRAM% on %HOST%:\%PARENT% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %PARENT% q_real %PROTOCOL% 
@echo off
pause

set PROGRAM=SetQualifierType
cd %EXAMPLES%\client\qualifier
echo **********************************************
echo Running %PROGRAM% on %HOST%:\%PARENT% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %PARENT% q_string string %PROTOCOL% 
@echo off

set PROGRAM=GetQualifierType
cd %EXAMPLES%\client\qualifier
echo **********************************************
echo Running %PROGRAM% on %HOST%:\%PARENT% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %PARENT% q_string %PROTOCOL% 
@echo off
pause

set PROGRAM=EnumQualifierType
cd %EXAMPLES%\client\qualifier
echo **********************************************
echo Running %PROGRAM% on %HOST%:\%PARENT% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %PARENT% %PROTOCOL% 
@echo off
pause

set PROGRAM=DeleteQualifierType
cd %EXAMPLES%\client\qualifier
echo **********************************************
echo Running %PROGRAM% on %HOST%:\%PARENT% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %PARENT% q_boolean %PROTOCOL% 
@echo off

set PROGRAM=DeleteQualifierType
cd %EXAMPLES%\client\qualifier
echo **********************************************
echo Running %PROGRAM% on %HOST%:\%PARENT% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %PARENT% q_uint32 %PROTOCOL% 
@echo off

set PROGRAM=DeleteQualifierType
cd %EXAMPLES%\client\qualifier
echo **********************************************
echo Running %PROGRAM% on %HOST%:\%PARENT% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %PARENT% q_uint64 %PROTOCOL% 
@echo off

set PROGRAM=DeleteQualifierType
cd %EXAMPLES%\client\qualifier
echo **********************************************
echo Running %PROGRAM% on %HOST%:\%PARENT% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %PARENT% q_sint %PROTOCOL% 
@echo off

set PROGRAM=DeleteQualifierType
cd %EXAMPLES%\client\qualifier
echo **********************************************
echo Running %PROGRAM% on %HOST%:\%PARENT% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %PARENT% q_real %PROTOCOL% 
@echo off

set PROGRAM=DeleteQualifierType
cd %EXAMPLES%\client\qualifier
echo **********************************************
echo Running %PROGRAM% on %HOST%:\%PARENT% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %PARENT% q_string %PROTOCOL% 
@echo off

@echo off
:END
cd %EXAMPLES%
rem Batch file terminated by error.
rem Can't use exit because it terminates the DOS window
