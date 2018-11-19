#!/bin/bash

if [ $# -eq 0 ]; then
	echo "need input argument"
	exit 1
fi

bashrc=/etc/bash.bashrc
javac=$(readlink -f /usr/bin/javac)
javahome="${javac:0: -9}"

if ! grep -q ".*JAVA_HOME.*" $bashrc; then
	echo "add java global variable"
	echo "export JAVA_HOME=$javahome" >> $bashrc
fi

if ! grep -q ".*SPARK_HOME.*" $bashrc; then

		echo "wget"
		if [ ! -f "/root/spark-2.4.0-bin-hadoop2.7.tgz" ]; then
        	wget http://mirror.apache-kr.org/spark/spark-2.4.0/spark-2.4.0-bin-hadoop2.7.tgz -P /root/
        fi
        echo "tar"
        tar -xvf /root/spark-2.4.0-bin-hadoop2.7.tgz -C /root/
        echo "mv"
        mv /root/spark-2.4.0-bin-hadoop2.7 $1/spark

		echo "configing"
        echo "export SPARK_HOME=$1/spark" >> $bashrc
        echo 'export PATH=$PATH:$SPARK_HOME/bin' >> $bashrc

        cd $1/spark/conf
        cp spark-env.sh.template spark-env.sh
fi
. $bashrc

echo "Reboot System?(y/n)"
read answer
if [ $answer = "y" ]; then
	reboot
fi