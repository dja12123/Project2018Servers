#!/bin/bash

if [ $# -eq 0 ]; then
	echo "need input argument"
	exit 1
fi

javac="readlink -f /usr/bin/javac"
javahome="${javac/javac/}"

sed -i "/export SPARK_HOME=$1/spark/d" /etc/bash.bashrc
#sed -i '/export JAVA_HOME=/usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt/jre/d' /etc/bash.bashrc
sed -i '/export PATH=$PATH:$SPARK_HOME/bin/d' /etc/bash.bashrc

rm -r $1/spark

echo "Reboot System?"
read an
if [ an = "y" | and = "yes" ]; then
	shutdown -r
fi