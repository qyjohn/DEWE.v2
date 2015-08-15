#!/bin/bash
if [ $# -eq 0 ]
  then
    echo "No arguments supplied"
    echo "Example: "
    echo "  submit.sh 100 10"
fi
if [ $# -eq 1 ]
  then
    echo "Sleep argument no supplied"
    echo "Example: "
    echo "  submit.sh 100 10"
fi

if [ $# -eq 2 ]
  then
    for i in $(seq 1 $1)
    do 
      ./dewe.sh submit test-$i /FoxData/test-$i
      sleep $2
   done
fi
