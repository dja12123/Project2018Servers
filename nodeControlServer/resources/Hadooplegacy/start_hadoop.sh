$HADOOP_HOME/bin/hdfs namenode -format
cd $HADOOP_HOME/sbin/
./start-yarn.sh
./start-dfs.sh
