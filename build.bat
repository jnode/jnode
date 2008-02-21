@echo off
setlocal
"%JAVA_HOME%\bin\java" -Xmx768M -Xms256M -jar core\lib\ant-launcher.jar -lib "%JAVA_HOME%\lib" -lib core\lib -f all\build.xml %*
