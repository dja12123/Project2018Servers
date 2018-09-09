#!/bin/bash
workersArr=('worker-10' 'worker-11' 'worker-12' 'worker-13')
for i in ${workersArr[@]}; do
        echo "@@@@@ Current Host: $i"
	ssh meshproject@$i "mkdir ~/scripts"
	scp ~/scripts/* meshproject@$i:~/scripts/
	#ssh meshproject@$i "sh ~/scripts/install_spark.sh"
	sh cpy_spark_setting.sh $i
done
