@echo off
setlocal
rem
rem EXHIBIT A - Sun Industry Standards Source License
rem
rem "The contents of this file are subject to the Sun Industry
rem Standards Source License Version 1.2 (the License");
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
rem The Initial Developer of the Original Code is;
rem Sun Microsystems, Inc.
rem
rem Portions created by; Sun Microsystems, Inc.
rem are Copyright (c) 2001 Sun Microsystems, Inc.
rem
rem All Rights Reserved.
rem
rem Contributor(s); _______________________________________
rem

rem
rem The user must set the SRC and JAVA_HOME environment variables
rem or edit this file to set them before running build.bat

rem Edit the JAVA_HOME variable to point to the top
rem directory of the installed version of the JDK
if "%JAVA_HOME%"=="" set JAVA_HOME=c:\jdk1.4.1
if exist %JAVA_HOME%\LICENSE goto JAVA_OK
echo.
echo The JAVA_HOME environment variable must be set to an
echo existing copy of the Java SDK.
echo.
echo Example: set JAVA_HOME=%JAVA_HOME%
echo.
goto END

:JAVA_OK

rem If ANT is not set, use a reasonable default
if "%ANT_HOME%"=="" set ANT_HOME=c:\jakarta-ant-1.4.1

if exist %ANT_HOME%\bin\ant.bat goto ANT_OK
echo.
echo The ANT_HOME environment variable must be set to an
echo existing copy of Ant.
echo.
echo Example: set ANT_HOME=%ANT_HOME%
echo.
goto END

:ANT_OK

%ANT_HOME%\bin\ant.bat %*

:END
