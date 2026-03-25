@REM Maven Wrapper startup script for Windows
@REM Required ENV vars:
@REM   JAVA_HOME - location of a JDK home dir

@echo off
setlocal

set MAVEN_PROJECTBASEDIR=%~dp0
set MAVEN_CMD_LINE_ARGS=%*

for %%i in ("%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar") do set WRAPPER_JAR=%%~fi
for %%i in ("%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties") do set WRAPPER_PROPERTIES=%%~fi

if exist "%WRAPPER_JAR%" (
    "%JAVA_HOME%\bin\java.exe" -jar "%WRAPPER_JAR%" %MAVEN_CMD_LINE_ARGS%
) else (
    echo Maven wrapper jar not found. Downloading...
    powershell -Command "& { Invoke-WebRequest -Uri 'https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar' -OutFile '%WRAPPER_JAR%' }"
    "%JAVA_HOME%\bin\java.exe" -jar "%WRAPPER_JAR%" %MAVEN_CMD_LINE_ARGS%
)
