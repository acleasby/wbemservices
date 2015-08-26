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

rem If xslt is not in c:\xalan-j_2_3_\bin, edit as needed
set XALAN_HOME=c:\xalan-j_2_3_\bin

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
set WBSERMOF=%WBSERHOME%\mof
set XMLDIR=%WBSERBIN%\xml

cd %WBSERBIN%

rem Path to directory where jar files were created. 
set WBEM=%WBSERLIB%\wbem.jar
set CIMOM=%CIMOMLIB%\cimom.jar
set MOFC=%WBSERBIN%\mofcomp.jar
set REPOSITORY=%CIMOMLIB%\cimrepository.jar
set XALAN=%XALAN_HOME%\xalan.jar

if not "%1" == "" goto RUN
echo USAGE: mof2html mof_file
cd %WBSERBIN%
goto END

:RUN
set MOF=%1

rem set MOFFILE= name of MOF file
dir/b %MOF% > tmp1.bat
copy mofvar.inp+tmp1.bat tmp2.bat
call tmp2.bat
del tmp?.bat

@echo off
rem NT 4.0 DOS doesn't have a deltree command
rem Use the OS environment variable to see
rem if this is an NT/2000 system
if "%OS%"=="" deltree -y %XMLDIR%
if not "%OS%"=="" rmdir /s /q %XMLDIR%
 
set CLASSPATH=.;%MOFC%;%CIMOM%;%WBEM%;%REPOSITORY%
set MOFCOMPILE=org.wbemservices.wbem.compiler.mofc.CIM_Mofc

echo.
echo *********************************************************
echo Creating XML files from %MOF% in %XMLDIR%
echo *********************************************************
echo.

rem Execute in the MOF directory to handle #pragam Include
rem NOTE: Works even if MOF is relative paths, 
rem because WBSERMOF and WBSERBIN are at the same directory level

cd %WBSERMOF%

rem See if MOFFILE must be copied and deleted
set COPYMOF=false
if exist %WBSERMOF%\%MOFFILE% goto NOCOPY
set COPYMOF=true
copy %MOF%

:NOCOPY
@echo on
%JAVA% %CLASSPATH% %MOFCOMPILE% -v -x -b -o %XMLDIR% %MOFFILE%

@echo off
rem See if MOFFILE must be deleted
if "%COPYMOF%" == "false" goto HTML
del %WBSERMOF%\%MOFFILE%

:HTML
move *.xml %XMLDIR%

set CLASSPATH=.;%XALAN%
set XSLT=org.apache.xalan.xslt.Process
set INDEX=%WBSERBIN%\index.xsl
set STYLE=%WBSERBIN%\multimof.xsl

echo.
echo *********************************************************
echo Creating HTML files from XML files in %XMLDIR%
echo *********************************************************
echo.

@echo on
%JAVA% %CLASSPATH% -Xmx128m %XSLT% -in %XMLDIR%\mof.xml -xsl %INDEX% -out %XMLDIR%\mof.html
@echo off

copy %XMLDIR%\mof.xml %WBSERBIN%
@echo on
%JAVA% %CLASSPATH% -Xmx128m %XSLT% -in %XMLDIR%\bigmof.xml -xsl %STYLE% -out %XMLDIR%\mof.html
copy %WBSERBIN%\mof.css %XMLDIR%

@echo off
del %WBSERBIN%\mof.xml
del %XMLDIR%\*.xml
del %XMLDIR%\store
del %XMLDIR%\Version_Number
del %XMLDIR%\Snapshot.*
del %XMLDIR%\Logfile.*

echo.
echo *********************************************************
echo In a browser, open %XMLDIR%\index.html
echo *********************************************************
echo.


:END
cd %WBSERBIN%
rem Batch file terminated by error.
rem Can't use exit because it terminates the DOS window
