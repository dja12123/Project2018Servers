#!/bin/bash

if [ $# -eq 0 ]; then
	echo "need input argument"
	exit 1
fi

bashrc=/etc/bash.bashrc

sed -i '/.*JAVA_HOME.*/d' $bashrc
sed -i '/.*SPARK_HOME.*/d' $bashrc

rm -r $1/spark

echo "Reboot System?(y/n)"
read answer
if [ $answer = "y" ]; then
	reboot
fi