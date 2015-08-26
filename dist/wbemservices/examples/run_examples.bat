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

rem Setup the directory variables
set WBSERLIB=%WBSERHOME%\lib
set WBSERBIN=%WBSERHOME%\bin
set CIMOMDIR=%WBSERHOME%\cimom
set CIMOMLIB=%CIMOMDIR%\lib
set EXAMPLES=%WBSERHOME%\examples

set WBEM=%WBSERLIB%\wbem.jar

set CLASSPATH=.;%WBEM%

if not "%2" == "" goto PROTOCOL
:HELP
echo USAGE: run_examples user passwd [protocol]
echo * protocol is http (the default) or rmi
cd %EXAMPLES%
goto END

:PROTOCOL
set USER=%1
set PASSWD=%2

set PROTOCOL=HTTP
if "%3" == "" goto RUN
if "%3" == "HTTP" goto RUN
if "%3" == "http" goto RUN
if "%3" == "RMI" set PROTOCOL=RMI
if "%3" == "rmi" set PROTOCOL=RMI

:RUN
rem NOTE: The following is a short-term workaround
mkdir \var
mkdir \var\sadm
mkdir \var\sadm\wbem
mkdir \var\sadm\wbem\security

rem Edit to target another system
set HOST=localhost

cd %EXAMPLES%
rem Copy provider class files to CIMOM directory
copy %EXAMPLES%\provider\sfl\*.class %CIMOMLIB%
copy %EXAMPLES%\provider\tsa\*.class %CIMOMLIB%
copy %EXAMPLES%\provider\sip\*.class %CIMOMLIB%

set MOF=SFL_Provider.mof
set CLASS=EX_SFLProvider
echo **********************************************
echo Creating %CLASS% class
echo **********************************************
cd %WBSERBIN%
call mofcomp -v -u %USER% -p %PASSWD% %EXAMPLES%\provider\sfl\%MOF%
set CLASSPATH=.;%WBEM%;%XML%
cd %EXAMPLES%

set QUALIFIER=ExampleQualifier

set CLASS=EX_SFLProvider
set PROGRAM=ClientEnum
copy %EXAMPLES%\client\enumeration\%PROGRAM%.class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% %PROTOCOL%
@echo off
del %PROGRAM%.class

set CLASS=EX_SFLProvider
set PROGRAM=TestBatch
copy %EXAMPLES%\client\batching\%PROGRAM%.class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% %PROTOCOL%
@echo off
del %PROGRAM%.class

set CLASS=EX_SFLProvider
set PROGRAM=ExampleQuery
copy %EXAMPLES%\client\query\%PROGRAM%.class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% %PROTOCOL% 
@echo off
del %PROGRAM%.class

set CLASS=EX_SFLProvider
set PROGRAM=DeleteClass
copy %EXAMPLES%\client\class\%PROGRAM%.class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% %PROTOCOL% 
@echo off
del %PROGRAM%.class

set PARENT=root
set CHILD=child
set PROGRAM=DeleteNameSpace
copy %EXAMPLES%\client\namespace\%PROGRAM%.class
echo **********************************************
echo Running %PROGRAM% to delete /%PARENT%/%CHILD% over %PROTOCOL%
echo Should get CIM_ERR_INVALID_NAMESPACE
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %PARENT% %CHILD% %USER% %PASSWD% %CLASS% %PROTOCOL% 
@echo off
del %PROGRAM%.class

set PROGRAM=CreateNameSpace
copy %EXAMPLES%\client\namespace\%PROGRAM%.class
echo **********************************************
echo Running %PROGRAM% to create /%PARENT%/%CHILD% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %PARENT% %CHILD% %USER% %PASSWD% %PROTOCOL% 
@echo off

echo **********************************************
echo Running %PROGRAM% to create /%PARENT%/%CHILD% over %PROTOCOL%
echo Should get CIM_ERR_ALREADY_EXISTS error
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %PARENT% %CHILD% %USER% %PASSWD% %PROTOCOL% 
@echo off
del %PROGRAM%.class

set PROGRAM=DeleteNameSpace
copy %EXAMPLES%\client\namespace\%PROGRAM%.class
echo **********************************************
echo Running %PROGRAM% on /%PARENT%/%CHILD% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %PARENT% %CHILD% %USER% %PASSWD% %PROTOCOL% 
@echo off
del %PROGRAM%.class

set MOF=SimpleInstanceProvider.mof
set CLASS=EX_SimpleInstanceProvider
echo **********************************************
echo Creating %CLASS% class
echo **********************************************
cd %WBSERBIN%
call mofcomp -v -u %USER% -p %PASSWD% %EXAMPLES%\provider\sip\%MOF%
set CLASSPATH=.;%WBEM%;%XML%
cd %EXAMPLES%

set CLASS=EX_SimpleInstanceProvider
set PROGRAM=DeleteInstances
copy %EXAMPLES%\client\instance\%PROGRAM%.class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo Should get CIM_ERR_NOT_SUPPORTED because the provider did not
echo implement the deleteInstance method
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %CLASS% %USER% %PASSWD% %PROTOCOL%
@echo off
del %PROGRAM%.class

set CLASS=EX_SimpleInstanceProvider
set PROGRAM=DeleteClass
copy %EXAMPLES%\client\class\%PROGRAM%.class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% %PROTOCOL% 
@echo off
del %PROGRAM%.class

set MOF=TeacherStudent.mof
echo **********************************************
echo Creating Creating EX_TeacherStudent, EX_Teacher, EX_Student classes
echo **********************************************
cd %WBSERBIN%
call mofcomp -v -u %USER% -p %PASSWD% %EXAMPLES%\provider\tsa\%MOF%
set CLASSPATH=.;%WBEM%;%XML%
cd %EXAMPLES%

set CLASS=EX_Teacher
set PROGRAM=DeleteClass
 copy %EXAMPLES%\client\class\%PROGRAM%.class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo Should get CIM_ERR_FAILED error because association EX_TeacherStudent
echo has references to EX_Teacher
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% %PROTOCOL%
@echo off
del %PROGRAM%.class

set CLASS=EX_Student
set PROGRAM=DeleteClass
copy %EXAMPLES%\client\class\%PROGRAM%.class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo Should get CIM_ERR_FAILED error because association EX_TeacherStudent
echo has references to EX_Student
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% %PROTOCOL%
@echo off
del %PROGRAM%.class

set CLASS=EX_TeacherStudent
set PROGRAM=DeleteClass
copy %EXAMPLES%\client\class\%PROGRAM%.class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% %PROTOCOL%
@echo off
del %PROGRAM%.class

set CLASS=EX_Teacher
set PROGRAM=DeleteClass
copy %EXAMPLES%\client\class\%PROGRAM%.class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% %PROTOCOL%
@echo off
del %PROGRAM%.class

set CLASS=EX_Student
set PROGRAM=DeleteClass
copy %EXAMPLES%\client\class\%PROGRAM%.class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% %PROTOCOL%
@echo off
del %PROGRAM%.class

del %CIMOMLIB%\*.class

@echo off
:END
rem Batch file terminated by error.
rem Can't use exit because it terminates the DOS window
