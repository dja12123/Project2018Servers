mkdir ~/spark
cd ~/spark


wget http://mirror.apache-kr.org/spark/spark-2.3.1/spark-2.3.1-bin-hadoop2.7.tgz
tar -xvf spark-2.3.1-bin-hadoop2.7.tgz
mv spark-2.3.1-bin-hadoop2.7 spark

echo 'export SPARK_HOME=/home/meshproject/spark/spark' >> /home/meshproject/.bashrc
echo 'export JAVA_HOME=/usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt/jre' >> /home/meshproject/.bashrc
echo 'export PATH=$SPARK_HOME/bin:$PATH' >> /home/meshproject/.bashrc

cd ~/spark/spark/conf
cp spark-env.sh.template spark-env.sh
