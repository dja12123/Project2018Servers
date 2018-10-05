#!/bin/bash

workers=("worker-10" "worker-11" "worker-12" "worker-13")
cd $SPARK_HOME/conf/
echo "export SPARK_WORKER_INSTANCES=1" >> spark-env.sh
echo "export SPARK_WORKER_CORES=2" >> spark-env.sh
echo "export SPARK_WORKER_MEMORY=512m" >> spark-env.sh
echo -n 'export SPARK_DAEMON_JAVA_OPTS="-Dspark.deploy.recoveryMode=ZOOKEEPER -Dspark.deploy.zookeeper.url=' >> spark-env.sh
for args in ${workers[@]};do
	echo -n "$args:2181," >> spark-env.sh
done
echo '"' >> spark-env.sh
