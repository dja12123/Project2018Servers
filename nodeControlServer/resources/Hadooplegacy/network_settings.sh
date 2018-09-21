ssh-keygen

for number in 10 11 12 13; do
	if ! grep -q "worker-$number" /etc/hosts; then
		echo "192.168.0.$number	worker-$number" | sudo tee -a /etc/hosts
	fi
	if ! grep -q "worker-$number" $HADOOP_HOME/etc/hadoop/workers; then
		echo "worker-$number" >> $HADOOP_HOME/etc/hadoop/workers
	fi
	yes | ssh-copy-id meshproject@worker-$number
done
