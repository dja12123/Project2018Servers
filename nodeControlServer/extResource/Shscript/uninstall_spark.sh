#!/bin/bash

if [ $# -eq 0 ]; then
	echo "need input argument"
	exit 1
fi

bashrc=/etc/bash.bashrc

sed -i "/export SPARK_HOME=$1/spark/d" $bashrc
sed -i '/export PATH=$PATH:$SPARK_HOME/bin/d' $bashrc

rm -r $1/spark

echo "Reboot System?"
read answer
if [ $answer = "y" | $answer = "yes" ]; then
	shutdown -r
fi