#!/bin/sh

dir=`dirname $0`
java -Xmx512M -Xms128M -jar $dir/core/lib/ant-launcher.jar -lib $dir/core/lib -f $dir/all/build.xml $*


