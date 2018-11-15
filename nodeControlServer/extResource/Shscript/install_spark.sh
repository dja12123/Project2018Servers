bashrc=/etc/bash.bashrc

if ! grep -q "export SPARK_HOME=$1/spark" $bashrc; then

        wget http://apache.tt.co.kr/spark/spark-2.4.0/spark-2.4.0-bin-hadoop2.7$
        tar -xvf spark-2.4.0-bin-hadoop2.7.tgz
        mv spark-2.4.0-bin-hadoop2.7 $1/spark

        echo "export SPARK_HOME=$1/spark" >> $bashrc
        echo "export JAVA_HOME=/usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt/jre" >>$
        echo 'export PATH=$PATH:$SPARK_HOME/bin' >> $bashrc

        cd $1/spark/conf
        cp spark-env.sh.template spark-env.sh
fi
. $bashrc