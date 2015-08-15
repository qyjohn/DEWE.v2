#!/bin/bash
cd /FoxData
cp ~/TestData/DEWE.v2_Montage_6.0_Example.tar.gz .
tar zxvf DEWE.v2_Montage_6.0_Example.tar.gz
for i in $(seq 1 $1)
do
    cp -r DEWE.v2_Montage_6.0_Example test-$i
done
