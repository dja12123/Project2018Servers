if [ -z "$1" ]; then
	exit 1
fi

cd $SPARK_HOME/sbin/

./start-slave.sh $1 #example spark://worker-11:7077 -m 512M -c 2