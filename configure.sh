#!/bin/sh
CMD=`type -p $0`
DIR=`dirname $CMD`

# Belt and braces classpath to get the most recent version
# of the 'configure' classes that we can find.
CP=$DIR/distr/build/classes:\
$DIR/all/build/descriptors/jnode-configure.jar:\
$DIR/distr/lib/jnode-configure-dist.jar:\
$DIR/distr/lib/nanoxml-2.2.3.jar

java -cp $CP org.jnode.configure.Configure "$@"
