#!/bin/sh

dir=`dirname $0`

unset CP
for f in $dir/core/lib/*.jar; do
	CP=$CP:$f
done

java -Xmx512M -Xms128M -classpath $CP:$CLASSPATH org.apache.tools.ant.Main -f all/build.xml $*


