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
rem are Copyright © 2002 Sun Microsystems, Inc.
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

rem Copy provider class files
copy %EXAMPLES%\client\instance\*_Provider.class %CIMOMLIB%

set USER=%1
set PASSWD=%2
set PROTOCOL=%3

if "%4" == "" set HOST=localhost
if not "%4" == "" set HOST=%4

if NOT "%HOST%" == "localhost" goto CREATE
set MOF=TestClass.mof
cd %WBSERBIN%
call mofcomp -v -u %USER% -p %PASSWD% %EXAMPLES%\client\class\%MOF%
set CLASSPATH=.;%WBEM%;%XML%
cd %EXAMPLES%

:CREATE
set PROGRAM=CreateInstance
set CLASS=Class_A1
cd %EXAMPLES%\client\instance
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% keyA1=A1.1 %PROTOCOL% 
@echo off

set PROGRAM=CreateInstance
set CLASS=Class_B2
cd %EXAMPLES%\client\instance
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% keyA1=B2.1 %PROTOCOL% 
@echo off

set PROGRAM=CreateInstance
set CLASS=Class_C2
cd %EXAMPLES%\client\instance
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% keyA1=C2.1 %PROTOCOL% 
@echo off

set PROGRAM=CreateInstance
set CLASS=Class_D2
cd %EXAMPLES%\client\instance
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% keyA1=D2.1 %PROTOCOL% 
@echo off

set PROGRAM=CreateInstance
set CLASS=Class_E3
cd %EXAMPLES%\client\instance
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% keyA1=E3.1 %PROTOCOL% 
@echo off

set PROGRAM=EnumerateInstanceNames
set CLASS=Class_A1
cd %EXAMPLES%\client\instance
echo **********************************************
echo Running %PROGRAM% on %CLASS%  over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% %PROTOCOL% 
@echo off
pause

set PROGRAM=EnumerateInstances
set CLASS=Class_A1
cd %EXAMPLES%\client\instance
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% objectPath null %PROTOCOL% 
@echo off
pause

:SET
set PROGRAM=SetInstance
set CLASS=Class_A1
cd %EXAMPLES%\client\instance
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% keyA1=A1.1 stringA1=StringA1.1:string objectPath %PROTOCOL% 
@echo off

set PROGRAM=GetInstance
set CLASS=Class_A1
cd %EXAMPLES%\client\instance
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% keyA1=A1.1 null objectPath %PROTOCOL% 
@echo off
pause

set PROGRAM=SetInstance
set CLASS=Class_B2
cd %EXAMPLES%\client\instance
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% keyA1=B2.1 stringA1=StringB2.1:string objectPath %PROTOCOL% 
@echo off

set PROGRAM=SetInstance
set CLASS=Class_B2
cd %EXAMPLES%\client\instance
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% keyA1=B2.1 uint32B2=32:uint32 propertyList %PROTOCOL% 
@echo off
pause

set PROGRAM=GetInstance
set CLASS=Class_B2
cd %EXAMPLES%\client\instance
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% keyA1=B2.1 null localOnly %PROTOCOL% 
@echo off
pause

set PROGRAM=SetInstance
set CLASS=Class_C2
cd %EXAMPLES%\client\instance
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% keyA1=C2.1 stringA1=StringC2.1:string objectPath %PROTOCOL% 
@echo off

set PROGRAM=SetInstance
set CLASS=Class_C2
cd %EXAMPLES%\client\instance
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% keyA1=C2.1 sint32C2=-32:sint32 !propertyList %PROTOCOL% 
@echo off

set PROGRAM=GetInstance
set CLASS=Class_C2
cd %EXAMPLES%\client\instance
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% keyA1=C2.1 null includeQualifiers %PROTOCOL% 
@echo off
pause

set PROGRAM=SetInstance
set CLASS=Class_D2
cd %EXAMPLES%\client\instance
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% keyA1=D2.1 stringA1=StringD2.1:string objectPath %PROTOCOL% 
@echo off

set PROGRAM=SetInstance
set CLASS=Class_D2
cd %EXAMPLES%\client\instance
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% keyA1=D2.1 booleanD2=false:boolean propertyList %PROTOCOL% 
@echo off

set PROGRAM=GetInstance
set CLASS=Class_D2
cd %EXAMPLES%\client\instance
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% keyA1=D2.1 null !propertyList %PROTOCOL% 
@echo off
pause

set PROGRAM=SetInstance
set CLASS=Class_E3
cd %EXAMPLES%\client\instance
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% keyA1=E3.1 stringA1=StringE3.1:string objectPath %PROTOCOL% 
@echo off

set PROGRAM=SetInstance
set CLASS=Class_E3
cd %EXAMPLES%\client\instance
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% keyA1=E3.1 uint32B2=32:uint32 propertyList %PROTOCOL% 
@echo off

set PROGRAM=SetInstance
set CLASS=Class_E3
cd %EXAMPLES%\client\instance
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% keyA1=E3.1 real32E3=32.5:real32 !propertyList %PROTOCOL% 
@echo off

set PROGRAM=GetInstance
set CLASS=Class_E3
cd %EXAMPLES%\client\instance
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% keyA1=E3.1 real32E3 propertyList %PROTOCOL% 
@echo off
pause

:ENUM
set PROGRAM=EnumerateInstances
set CLASS=Class_A1
cd %EXAMPLES%\client\instance
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% objectPath null %PROTOCOL% 
@echo off
pause

set PROGRAM=EnumerateInstances
set CLASS=Class_B2
cd %EXAMPLES%\client\instance
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% deep null %PROTOCOL% 
@echo off
pause

set PROGRAM=EnumerateInstances
set CLASS=Class_C2
cd %EXAMPLES%\client\instance
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% localOnly null %PROTOCOL% 
@echo off
pause

set PROGRAM=EnumerateInstances
set CLASS=Class_D2
cd %EXAMPLES%\client\instance
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% includeQualifiers null %PROTOCOL% 
@echo off
pause

set PROGRAM=EnumerateInstances
set CLASS=Class_E3
cd %EXAMPLES%\client\instance
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% includeClassOrigin null %PROTOCOL% 
@echo off
pause

set PROGRAM=EnumerateInstances
set CLASS=Class_A1
cd %EXAMPLES%\client\instance
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% !propertyList null %PROTOCOL% 
@echo off
pause

set PROGRAM=EnumerateInstances
set CLASS=Class_B2
cd %EXAMPLES%\client\instance
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% propertyList uint32B2 %PROTOCOL% 
@echo off
pause

:DELETE
set PROGRAM=DeleteInstance
set CLASS=Class_A1
cd %EXAMPLES%\client\instance
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% keyA1=A1.1 %PROTOCOL% 
@echo off

set PROGRAM=DeleteInstance
set CLASS=Class_B2
cd %EXAMPLES%\client\instance
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% keyA1=B2.1 %PROTOCOL% 
@echo off

set PROGRAM=DeleteInstance
set CLASS=Class_C2
cd %EXAMPLES%\client\instance
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% keyA1=C2.1 %PROTOCOL% 
@echo off

set PROGRAM=DeleteInstance
set CLASS=Class_D2
cd %EXAMPLES%\client\instance
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% keyA1=D2.1 %PROTOCOL% 
@echo off

set PROGRAM=DeleteInstance
set CLASS=Class_E3
cd %EXAMPLES%\client\instance
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% keyA1=E3.1 %PROTOCOL% 
@echo off

set PROGRAM=DeleteClass
set CLASS=Class_F3
cd %EXAMPLES%\client\class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% %PROTOCOL% 
@echo off

set PROGRAM=DeleteClass
set CLASS=Class_E3
cd %EXAMPLES%\client\class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% %PROTOCOL% 
@echo off

set PROGRAM=DeleteClass
set CLASS=Class_B2
cd %EXAMPLES%\client\class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% %PROTOCOL% 
@echo off

set PROGRAM=DeleteClass
set CLASS=Class_C2
cd %EXAMPLES%\client\class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% %PROTOCOL% 
@echo off

set PROGRAM=DeleteClass
set CLASS=Class_D2
cd %EXAMPLES%\client\class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% %PROTOCOL% 
@echo off

set PROGRAM=DeleteClass
set CLASS=Class_A1
cd %EXAMPLES%\client\class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% %PROTOCOL% 
@echo off

rem Remove Provider class files
del %CIMOMLIB%\*_Provider.class

@echo off
:END
cd %EXAMPLES%
rem Batch file terminated by error.
rem Can't use exit because it terminates the DOS window
