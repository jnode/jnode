#!/bin/sh
DIR=`dirname $0`
DIR=`cd $DIR; pwd;`

# Belt and braces classpath to get the most recent version
# of the 'configure' classes that we can find.
CP=$DIR/builder/build/classes:\
$DIR/all/build/descriptors/jnode-configure.jar:\
$DIR/builder/lib/jnode-configure-dist.jar:\
$DIR/shell/lib/nanoxml-2.2.3.jar

if [ $# = 0 ] ; then
    java -cp $CP org.jnode.configure.Configure $DIR/all/conf-source/script.xml
else
    java -cp $CP org.jnode.configure.Configure "$@"
fi

