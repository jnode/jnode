@echo off
setlocal
set CLASSPATH=core\lib\ant.jar;core\lib\optional.jar;core\lib\junit.jar;%JAVA_HOME%\lib\tools.jar;%ANT_HOME%\lib\NetComponents.jar
java -Xmx512M -Xms128M org.apache.tools.ant.Main -f all\build.xml %*
