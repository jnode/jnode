@echo off
setlocal
"%JAVA_HOME%\bin\java" -Xmx512M -Xms128M -jar core\lib\ant-launcher.jar -lib "%JAVA_HOME%\lib" -lib core\lib -q -f all\build.xml %*
