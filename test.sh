#!/bin/sh

dir=`dirname $0`

if [ $# -eq 0 ] ; then
    echo "usage: ./test.sh <project> [ <test> ... ]"
    echo " where <project> is \"all\" or a JNode component project, and"
    echo " the <test>s are targets in the respective \"build-tests.xml\" files"
    exit
fi

if [ $1 = all ] ; then 
    PROJECTS=`find . -maxdepth 2 -name build-tests.xml -exec dirname {} \;` 
else
    PROJECTS=$1
fi
shift

for PROJECT in $PROJECTS ; do
    java -Xmx768M -Xms256M -jar $dir/core/lib/ant-launcher.jar \
	-lib $JAVA_HOME/lib -lib $dir/core/lib \
        -f $dir/$PROJECT/build-tests.xml $*
done

