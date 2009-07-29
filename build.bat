@echo off

if ("%JAVA_HOME%") == ("") GOTO ERROR1
echo JAVA_HOME is set to "%JAVA_HOME%"

if NOT EXIST "%JAVA_HOME%\bin\java.exe" GOTO ERROR2
"%JAVA_HOME%\bin\java" -Xmx768M -Xms256M -jar core\lib\ant-launcher.jar -lib "%JAVA_HOME%\lib" -lib core\lib -f all\build.xml %*
GOTO :END

:ERROR1
echo ERROR: JAVA_HOME is not set.
GOTO :HELP

:ERROR2
echo ERROR: JAVA_HOME is set to a wrong value, was expecting "%JAVA_HOME%\bin\java.exe" to exist.
GOTO :HELP

:HELP
echo A typical java home is C:\Program Files\Java\jdk1.6.0 (or 'jdk1.6.1' ...)
pause
GOTO :END

:END
