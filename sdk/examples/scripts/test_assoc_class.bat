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
set USER=%1
set PASSWD=%2
set PROTOCOL=HTTP
if "%3" == "HTTP" set PROTOCOL=HTTP
if "%3" == "http" set PROTOCOL=HTTP
if "%3" == "RMI" set PROTOCOL=RMI
if "%3" == "rmi" set PROTOCOL=RMI

rem NOTE: The following is a short-term workaround
mkdir \var
mkdir \var\sadm
mkdir \var\sadm\wbem
mkdir \var\sadm\wbem\security

if "%4" == "" set HOST=localhost
if not "%4" == "" set HOST=%4

if NOT "%HOST%" == "localhost" goto ENUM
set MOF=TestAssoc.mof
cd %WBSERBIN%
call mofcomp -v -u %USER% -p %PASSWD% %EXAMPLES%\client\class\%MOF%
cd %EXAMPLES%

:ENUM
set CLASSPATH=.;%WBEM%;%XML%
set PROGRAM=EnumerateClassNames
set CLASS=Root_Class
cd %EXAMPLES%\client\class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% deep %PROTOCOL% 
@echo off
pause

set PROGRAM=EnumerateClasses
set CLASS=Antecedent_Class
cd %EXAMPLES%\client\class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% deep %PROTOCOL% 
@echo off
pause

set PROGRAM=EnumerateClasses
set CLASS=Dependent_Class
cd %EXAMPLES%\client\class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% deep %PROTOCOL% 
@echo off
pause

set PROGRAM=EnumerateClasses
set CLASS=Other_Class
cd %EXAMPLES%\client\class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% deep %PROTOCOL% 
@echo off
pause

set PROGRAM=EnumerateClassNames
set CLASS=Other_Assoc
cd %EXAMPLES%\client\class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% deep %PROTOCOL% 
@echo off
pause

set PROGRAM=EnumerateClasses
set CLASS=Other_Assoc
cd %EXAMPLES%\client\class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% includeClassOrigin %PROTOCOL% 
@echo off
pause

set PROGRAM=EnumerateClassNames
set CLASS=Dependency_Assoc
cd %EXAMPLES%\client\class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% deep %PROTOCOL% 
@echo off
pause

set PROGRAM=EnumerateClasses
set CLASS=Dependency_Assoc
cd %EXAMPLES%\client\class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% includeClassOrigin %PROTOCOL% 
@echo off
pause

:ASSOCNAME
set PROGRAM=AssociatorNames
set CLASS=Class_A1
cd %EXAMPLES%\client\class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% all_null null %PROTOCOL% 
@echo off
pause

set PROGRAM=AssociatorNames
set CLASS=Class_A1
cd %EXAMPLES%\client\class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% assocClass Assoc_A1D1 %PROTOCOL% 
@echo off
pause

set PROGRAM=AssociatorNames
set CLASS=Class_A1
cd %EXAMPLES%\client\class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% resultClass Class_D2 %PROTOCOL% 
@echo off
pause

set PROGRAM=AssociatorNames
set CLASS=Class_A1
cd %EXAMPLES%\client\class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% role Antecedent %PROTOCOL% 
@echo off
pause

set PROGRAM=AssociatorNames
set CLASS=Class_A1
cd %EXAMPLES%\client\class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% resultRole Dependent %PROTOCOL% 
@echo off
pause

:ASSOC
set PROGRAM=Associators
set CLASS=Class_A1
cd %EXAMPLES%\client\class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% all_null null %PROTOCOL% 
@echo off
pause

set PROGRAM=Associators
set CLASS=Class_A1
cd %EXAMPLES%\client\class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% assocClass Assoc_A1D1 %PROTOCOL% 
@echo off
pause

set PROGRAM=Associators
set CLASS=Class_A1
cd %EXAMPLES%\client\class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% resultClass Class_D2 %PROTOCOL% 
@echo off
pause

set PROGRAM=Associators
set CLASS=Class_A1
cd %EXAMPLES%\client\class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% role Antecedent %PROTOCOL% 
@echo off
pause

set PROGRAM=Associators
set CLASS=Class_A1
cd %EXAMPLES%\client\class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% resultRole Dependent %PROTOCOL% 
@echo off
pause

set PROGRAM=Associators
set CLASS=Class_A1
cd %EXAMPLES%\client\class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% includeQualifier null %PROTOCOL% 
@echo off
pause

set PROGRAM=Associators
set CLASS=Class_A1
cd %EXAMPLES%\client\class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% includeClassOrigin null %PROTOCOL% 
@echo off
pause

set PROGRAM=Associators
set CLASS=Class_A1
cd %EXAMPLES%\client\class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% propertyList class_D1,dependentProperty %PROTOCOL% 
@echo off
pause

:REFNAME
set PROGRAM=ReferenceNames
set CLASS=Class_A1
cd %EXAMPLES%\client\class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% all_null null %PROTOCOL% 
@echo off
pause

set PROGRAM=ReferenceNames
set CLASS=Class_A1
cd %EXAMPLES%\client\class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% resultClass Assoc_A1D1 %PROTOCOL% 
@echo off
pause

set PROGRAM=ReferenceNames
set CLASS=Class_A1
cd %EXAMPLES%\client\class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% role Antecedent %PROTOCOL% 
@echo off
pause

:REF
set PROGRAM=References
set CLASS=Class_A1
cd %EXAMPLES%\client\class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% all_nullfalse null %PROTOCOL% 
@echo off
pause

set PROGRAM=References
set CLASS=Class_A1
cd %EXAMPLES%\client\class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% resultClass Assoc_A1D1 %PROTOCOL% 
@echo off
pause

set PROGRAM=References
set CLASS=Class_A1
cd %EXAMPLES%\client\class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% role Antecedent %PROTOCOL% 
@echo off
pause

set PROGRAM=References
set CLASS=Class_A1
cd %EXAMPLES%\client\class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% includeQualifier null %PROTOCOL% 
@echo off
pause

set PROGRAM=References
set CLASS=Class_A1
cd %EXAMPLES%\client\class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% includeClassOrigin null %PROTOCOL% 
@echo off
pause

set PROGRAM=References
set CLASS=Class_A1
cd %EXAMPLES%\client\class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% propertyList Assoc_A1D1,assocProperty %PROTOCOL% 
@echo off
pause

:DELETE
rem Must delete associate classes before base classes
rem Must delete children classes before parent classes
set PROGRAM=DeleteClass
set CLASS=Assoc_A1O1
cd %EXAMPLES%\client\class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% %PROTOCOL% 
@echo off

set PROGRAM=DeleteClass
set CLASS=Assoc_A1O2
cd %EXAMPLES%\client\class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% %PROTOCOL% 
@echo off

set PROGRAM=DeleteClass
set CLASS=Assoc_A1O3
cd %EXAMPLES%\client\class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% %PROTOCOL% 
@echo off

set PROGRAM=DeleteClass
set CLASS=Other_Assoc
cd %EXAMPLES%\client\class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% %PROTOCOL% 
@echo off

set PROGRAM=DeleteClass
set CLASS=Assoc_A1D1
cd %EXAMPLES%\client\class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% %PROTOCOL% 
@echo off

set PROGRAM=DeleteClass
set CLASS=Assoc_A1D2
cd %EXAMPLES%\client\class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% %PROTOCOL% 
@echo off

set PROGRAM=DeleteClass
set CLASS=Assoc_A1D3
cd %EXAMPLES%\client\class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% %PROTOCOL% 
@echo off

set PROGRAM=DeleteClass
set CLASS=Dependency_Assoc
cd %EXAMPLES%\client\class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% %PROTOCOL% 
@echo off

rem Now delete base classes
rem Must delete children classes before parent classes
set PROGRAM=DeleteClass
set CLASS=Class_D1
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
set CLASS=Class_D3
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

set PROGRAM=DeleteClass
set CLASS=Class_O1
cd %EXAMPLES%\client\class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% %PROTOCOL% 
@echo off

set PROGRAM=DeleteClass
set CLASS=Class_O2
cd %EXAMPLES%\client\class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% %PROTOCOL% 
@echo off

set PROGRAM=DeleteClass
set CLASS=Class_O3
cd %EXAMPLES%\client\class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% %PROTOCOL% 
@echo off

set PROGRAM=DeleteClass
set CLASS=Antecedent_Class
cd %EXAMPLES%\client\class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% %PROTOCOL% 
@echo off

set PROGRAM=DeleteClass
set CLASS=Dependent_Class
cd %EXAMPLES%\client\class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% %PROTOCOL% 
@echo off

set PROGRAM=DeleteClass
set CLASS=Other_Class
cd %EXAMPLES%\client\class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% %PROTOCOL% 
@echo off

set PROGRAM=DeleteClass
set CLASS=Root_Class
cd %EXAMPLES%\client\class
echo **********************************************
echo Running %PROGRAM% on %CLASS% over %PROTOCOL%
echo **********************************************
@echo on
%JAVA% %CLASSPATH% %PROGRAM% %HOST% %USER% %PASSWD% %CLASS% %PROTOCOL% 
@echo off

@echo off
:END
cd %EXAMPLES%
rem Batch file terminated by error.
rem Can't use exit because it terminates the DOS window
