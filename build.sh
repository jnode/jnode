#!/bin/sh

dir=`dirname $0`
java -Xmx768M -Xms256M -jar $dir/core/lib/ant-launcher.jar -lib $JAVA_HOME/lib -lib $dir/core/lib -f $dir/all/build.xml $*
