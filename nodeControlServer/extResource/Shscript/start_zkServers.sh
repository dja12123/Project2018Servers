#!/bin/bash

workers=( "worker-10" "worker-11" "worker-12" "worker-13" )

for worki in ${workers[@]}; do
        ssh meshproject@$worki "~/zookeeper/zookeeper/bin/zkServer.sh start"
done

