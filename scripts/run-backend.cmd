@echo off
setlocal

set "PROJECT_ROOT=%~dp0.."
set "JDK_HOME=D:\SoftWare\IntelliJ IDEA 2026.1.3\jbr"

if not exist "%JDK_HOME%" (
  echo Stable JDK not found: %JDK_HOME%
  exit /b 1
)

set "JAVA_HOME=%JDK_HOME%"
set "PATH=%JDK_HOME%\bin;%PATH%"

cd /d "%PROJECT_ROOT%"
echo Using JAVA_HOME=%JAVA_HOME%
echo Starting backend with stable local JDK...

mvn -s .mvn/settings.xml spring-boot:run
