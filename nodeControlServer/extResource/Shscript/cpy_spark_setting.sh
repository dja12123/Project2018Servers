#capy configuration to another node
###################################

if [ -z "$1" ]; then
	echo "!!!!!!!!!!plz enter recive node !!!!!!!"
	exit 1
fi

cd $SPARK_HOME/

scp ./conf/spark-env.sh meshproject@$1:$SPARK_HOME/conf/
scp ./sbin/start-slave.sh meshproject@$1:$SPARK_HOME/sbin/
scp ~/scripts/* meshproject@$1:~/scripts/
scp ~/zookeeper/zookeeper/conf/zoo.cfg meshproject@$1:~/zookeeper/zookeeper/conf/
exit 0
