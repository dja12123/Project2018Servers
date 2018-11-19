$HADOOP_HOME/sbin/stop-all.sh
for number in 10 11 12 13
do
	ssh worker-$number "rm -rf /opt/hadoop_tmp/hdfs/datanode/*"
done
$HADOOP_HOME/bin/hdfs namenode -format
