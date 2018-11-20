cd $SPARK_HOME/conf
cp spark-env.sh.template spark-env.sh
echo "export SPARK_WORKER_INSTANCES=4" >> spark-env.sh
