#!/bin/bash
# author: Steph Meslin-Weber
# license: BSD
# fontmap generator
INFILE=$1
HEX=( 0 1 2 3 4 5 6 7 8 9 A B C D E F )

DATA=`cat $1|grep STARTCHAR|sed -e 's/STARTCHAR //g'`
UNITS=0
TENS=0

for LINE in ${DATA}; do

  echo ${LINE}=\\u00${HEX[TENS]}${HEX[UNITS]}

  let "UNITS += 1"
  if [[ ${UNITS} -gt 15 ]]; then
    UNITS=0;
    let "TENS += 1"
  fi
done
