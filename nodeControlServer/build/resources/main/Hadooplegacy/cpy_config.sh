cd $HADOOP_HOME/etc/hadoop/
for receiver in 10 11 12 13
do
	scp *.xml meshproject@worker-$receiver:$HADOOP_HOME/etc/hadoop/
	scp workers meshproject@worker-$receiver:$HADOOP_HOME/etc/hadoop/
done
