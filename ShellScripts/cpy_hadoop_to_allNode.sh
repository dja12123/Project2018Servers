for number in 10 11 12 13
do
	#scp -r /opt/hadoop/* meshproject@worker-$number:/opt/hadoop/
	#scp -r /opt/hadoop_tmp/* meshproject@worker-$number:/opt/hadoop_tmp/
	scp /opt/hadoop/*.sh meshproject@worker-$number:/opt/hadoop/
done
