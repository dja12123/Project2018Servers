#!/bin/bash

cd $SPARK_HOME

./sbin/start-master.sh

workers=( "worker-10" "worker-11" "worker-12" "worker-13" )

for worki in ${workers[@]}; do
	 ssh meshproject@$worki "$SPARK_HOME/sbin/start-slave.sh spark://worker-11:7077"
done
