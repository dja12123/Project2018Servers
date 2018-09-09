#!/bin/bash

cd $SPARK_HOME
./sbin/stop-master.sh

workers=( "worker-10" "worker-11" "worker-12" "worker-13" )

for worki in ${workers[@]}; do
         ssh meshproject@$worki "$SPARK_HOME/sbin/stop-slave.sh"
done


