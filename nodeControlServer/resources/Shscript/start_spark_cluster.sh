#!/bin/bash

workers=( "worker-10" "worker-11" "worker-12" "worker-13" )

for args in ${workers[@]};do
        opt+="$args:7077,"
done
echo '"' >> spark-env.sh


for worki in ${workers[@]}; do
	ssh meshproject@$worki "$SPARK_HOME/sbin/start-master.sh"
	 ssh meshproject@$worki "$SPARK_HOME/sbin/start-slave.sh spark://$opt"
done
