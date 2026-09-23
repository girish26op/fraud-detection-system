@echo off
set "JAVA_HOME=C:\Program Files\Microsoft\jdk-21.0.12.101-hotspot"
set "M2_HOME=C:\tools\apache-maven-3.9.9"
set "PATH=%JAVA_HOME%\bin;%M2_HOME%\bin;%PATH%"

echo ======================================================================
echo Starting Fraud Detection System (Spring Boot REST API on Port 8080)
echo ======================================================================
mvn spring-boot:run
pause
