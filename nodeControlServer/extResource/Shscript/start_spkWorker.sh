if [ "$#" -le 1 ]; then
	exit 1
fi

$SPARK_HOME/sbin/start-slave.sh "$@" #example spark://worker-11:7077 -m 512M -c 2