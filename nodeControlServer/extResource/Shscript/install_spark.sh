if [ $# -ne 1 ]; then
	exit 1
fi

mkdir "$1/../"
cd "$1/../"


wget http://mirror.apache-kr.org/spark/spark-2.3.1/spark-2.3.1-bin-hadoop2.7.tgz
tar -xvf spark-2.3.1-bin-hadoop2.7.tgz
mv spark-2.3.1-bin-hadoop2.7 spark

echo "export SPARK_HOME=$1" >> /etc/bashrc
echo "export JAVA_HOME=/usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt/jre" >> /etc/bashrc
echo 'export PATH=$PATH:$SPARK_HOME/bin' >> /etc/bashrc

cd $1/conf
cp spark-env.sh.template spark-env.sh
