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

exit 0
