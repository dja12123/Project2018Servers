if [ -z "$1" ]; then
	exit 1
fi

if grep -q "export SPARK_HOME=$1" /etc/bashrc; then
	mkdir "$1/../"
	cd "$1/../"
	
	wget http://apache.tt.co.kr/spark/spark-2.4.0/spark-2.4.0-bin-hadoop2.7.tgz
	tar -xvf spark-2.4.0-bin-hadoop2.7.tgz
	mv spark-2.4.0-bin-hadoop2.7 spark
	
	echo "export SPARK_HOME=$1" >> /etc/bashrc
	echo "export JAVA_HOME=/usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt/jre" >> /etc/bashrc
	echo 'export PATH=$PATH:$SPARK_HOME/bin' >> /etc/bashrc
	
	cd $1/conf
	cp spark-env.sh.template spark-env.sh
fi