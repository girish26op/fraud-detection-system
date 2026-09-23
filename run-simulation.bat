@echo off
set "JAVA_HOME=C:\Program Files\Microsoft\jdk-21.0.12.101-hotspot"
set "M2_HOME=C:\tools\apache-maven-3.9.9"
set "PATH=%JAVA_HOME%\bin;%M2_HOME%\bin;%PATH%"

echo ======================================================================
echo Running Java 21 Virtual Threads Fraud Detection Simulation
echo ======================================================================
java -cp target\classes com.fraud.sys.engine.TransactionProcessor
pause
