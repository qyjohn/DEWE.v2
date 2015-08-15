#!/bin/bash
./dewe.sh worker stop
./dewe.sh worker start
./dewe.sh monitor stop
./dewe.sh monitor md0 start

