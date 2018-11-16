if [ $# -le 1 ]; then
	exit 1
fi

bashrc=/etc/bash.bashrc

if ! grep -q "export SPARK_HOME=$1/spark" $bashrc; then

		echo "wget"
        wget http://mirror.apache-kr.org/spark/spark-2.4.0/spark-2.4.0-bin-hadoop2.7.tgz -P /root/
        echo "tar"
        tar -xvf /root/spark-2.4.0-bin-hadoop2.7.tgz -C /root/
        echo "mv"
        mv /root/spark-2.4.0-bin-hadoop2.7 $1/spark

		echo "configing"
        echo "export SPARK_HOME=$1/spark" >> $bashrc
        echo "export JAVA_HOME=/usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt/jre" >> $bashrc
        echo 'export PATH=$PATH:$SPARK_HOME/bin' >> $bashrc

        cd $1/spark/conf
        cp spark-env.sh.template spark-env.sh
fi
. $bashrc