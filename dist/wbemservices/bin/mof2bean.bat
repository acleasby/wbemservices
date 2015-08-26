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
rem are Copyright © 2002 Sun Microsystems, Inc.
rem 
rem All Rights Reserved.
rem 
rem Contributor(s): _______________________________________
rem 

rem
rem This is a batch file that adds the new MOF classes
rem to the CIM Repository while the CIMOM is running.
rem NOTE: Due to a bug, the MOF file must be in the current
rem directory; i.e. only a MOF filename argument can be used,
rem not a pathname.
rem 

rem JAVA_HOME points to the top directory of the installed
rem version of the JDK.  Normally set in build.bat for
rem top down builds.  Edit the next line as needed.
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

rem If java is not here, edit as needed
set JAVA=%JAVA_HOME%\bin\java -classpath


rem The Unix equivalent of the following is CURDIR=`pwd`
echo @prompt set CURDIR=$p$_ > tmp1.bat
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

rem Setup the directory variables
set WBSERBIN=%WBSERHOME%\bin
set WBSERLIB=%WBSERHOME%\lib
set CIMOMDIR=%WBSERHOME%\cimom
set CIMOMLIB=%CIMOMDIR%\lib
set WBSERMOF=%WBSERHOME%\mof\dmtf\2.8
set BEANDIR=%WBSERBIN%\bean

cd %WBSERBIN%

rem Path to directory where jar files were created. 
set WBEM=%WBSERLIB%\wbem.jar
set CIMOM=%CIMOMLIB%\cimom.jar
set MOFC=%WBSERBIN%\mofcomp.jar
set REPOSITORY=%CIMOMLIB%\cimrepository.jar

set CLASSPATH=.;%MOFC%;%CIMOM%;%WBEM%;%REPOSITORY%

set MOFCOMPILE=org.wbemservices.wbem.compiler.mofc.CIM_Mofc

if not "%4" == "" goto RUN
echo USAGE: mof2bean user passwd bean_config mof_file
cd %WBSERBIN%

goto END

:RUN
set USER=%1
set PASSWD=%2
set BEAN=%3
set MOF=%4

rem Set MOFFILE to the name of MOF file without the path
echo set MOFFILE= > tmp1.bat
dir/b %MOF% > tmp2.bat
copy tmp1.bat+tmp2.bat tmp3.bat
call tmp3.bat
del tmp?.bat

rem Remove any old repository in  the bean output directory
rem But do not delete any previously generated JavaBean files

mkdir %BEANDIR%
del %BEANDIR%\Snapshot.*
del %BEANDIR%\Logfile.*
del %BEANDIR%\Store
del %BEANDIR%\Version_Number

rem -o, output directory 
rem -u, user 
rem -p, password
rem -j, javabeans 

echo.
echo *********************************************************
echo Generating JavaBeans from %MOF% in %BEANDIR%
echo *********************************************************
echo.

rem Execute in the MOF directory to handle #pragam Include
rem NOTE: Works even if BEAN and MOF are relative paths, 
rem because WBSERMOF and WBSERBIN are at the same directory level

cd %WBSERMOF%

rem See if MOFFILE must be copied and deleted
set COPYMOF=false
if exist %WBSERMOF%\%MOFFILE% goto NOCOPY
set COPYMOF=true
copy %MOF%

:NOCOPY
@echo on
%JAVA% %CLASSPATH% %MOFCOMPILE% -u %USER% -p %PASSWD% -o %BEANDIR% -j %CURDIR%\%BEAN% %MOFFILE%

@echo off
rem See if MOFFILE must be deleted
if "%COPYMOF%" == "false" goto END
del %WBSERMOF%\%MOFFILE%

:END
cd %WBSERBIN%
rem Batch file terminated by error.
rem Can't use exit because it terminates the DOS window
