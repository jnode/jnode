@echo off
setlocal
java -Xmx512M -Xms128M -jar core\lib\ant-launcher.jar -lib core\lib -f all\build.xml %*
