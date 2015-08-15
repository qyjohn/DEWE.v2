#!/bin/bash
cd /FoxData
for i in $(seq 1 $1)
do
    mfssettrashtime -r 0 test-$i
    rm -Rf test-$i
done
