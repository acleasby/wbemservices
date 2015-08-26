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

rem JAVA_HOME points to the top directory of the installed
rem version of the JDK.  Edit the next line as needed.
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

rem The Unix equivalent of the following is WBSERHOME=`cd ..\..;pwd`
@echo off
@cd ..\..
echo @prompt set WBSERHOME=$p$_ > tmp1.bat
command /e:4096 /c tmp1.bat > tmp2.bat
call tmp2.bat
del tmp?.bat

rem Setup the directory variables
set CIMOMDIR=%WBSERHOME%\cimom
set CIMOMBIN=%CIMOMDIR%\bin
set WBSERLIB=%WBSERHOME%\lib
set CIMOMLIB=%CIMOMDIR%\lib
set WBLOGDIR=%CIMOMDIR%\logr

set STARTUP=%CIMOMLIB%\wbemstartup.jar
set CLASSPATH=%STARTUP%
set WBSERFILE=%CIMOMBIN%\WBEMServices_Win32.properties

set CIMOMSERVER=org.wbemservices.wbem.bootstrap.StartWBEMServices
set CIMOMPID=%WBLOGDIR%\cimomserver.pid

cd %CIMOMBIN%
@echo off
echo *********************************************************
echo Starting up the CIMOM
echo *********************************************************

rem Start the CIMOM in debug mode
rem @echo on
rem %JAVA% %CLASSPATH% -Xmx128m -Djava.security.manager -Djava.security.policy=%CIMOMBIN%\cimom.policy -DBaseDir=%WBSERHOME% -Dpropdir=%CIMOMBIN% -Dlogdir=%WBLOGDIR% -DDebugLevel=3 -DDebugDevice=stdout %CIMOMSERVER% %WBSERFILE%
rem @echo off

rem Start the CIMOM in non debug mode
@echo on
%JAVA% %CLASSPATH% -Xmx128m -Djava.security.manager -Djava.security.policy=%CIMOMBIN%\cimom.policy -DBaseDir=%WBSERHOME% -Dpropdir=%CIMOMBIN% -Dlogdir=%WBLOGDIR% %CIMOMSERVER% %WBSERFILE%

@echo off
:END
rem Batch file terminated by error.
rem Can't use exit because it terminates the DOS window.
