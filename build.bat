@echo off
setlocal
java -Xmx512M -Xms128M -jar core\lib\ant-launcher.jar -lib %JAVA_HOME%\lib -lib core\lib -f all\build.xml %*
