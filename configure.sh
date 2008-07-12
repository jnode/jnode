#!/bin/sh
cmd=`type -p $0`
dir=`dirname $cmd`
cp=$dir/all/build/descriptors/jnode-configure.jar:$dir/distr/lib/nanoxml-2.2.3.jar

java -cp $cp org.jnode.configure.Configure "$@"
