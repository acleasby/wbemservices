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

rem This is a batch file that runs the mof compiler

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
set CIMOMLIB=%WBSERHOME%\cimom\lib

rem JAVA_HOME points to the top directory of the installed
rem version of the JDK.  Normally set in build.bat for
rem top down builds.  Edit the next line as needed.

if exist %JAVA_HOME%\LICENSE goto JAVA_OK
echo.
echo The JAVA_HOME environment variable must be set to an
echo existing copy of the Java SDK.
echo.
echo Example: set JAVA_HOME=c:\j2sdk1.4.2
echo.
goto END

:JAVA_OK

rem If java is not here, edit as needed
set JAVA=%JAVA_HOME%\bin\java -classpath

rem Path to directory where jar files were created. 
set WBEM=%WBSERLIB%\wbem.jar
set CIMOM=%CIMOMLIB%\cimom.jar
set MOFC=%WBSERBIN%\mofcomp.jar
set REPOSITORY=%CIMOMLIB%\cimrepository.jar

set CLASSPATH=.;%MOFC%;%CIMOM%;%WBEM%;%REPOSITORY%

set MOFCOMPILE=org.wbemservices.wbem.compiler.mofc.CIM_Mofc

cd %WBSERBIN%
%JAVA% %CLASSPATH% %MOFCOMPILE% %*

:END
cd %WBSERBIN%
