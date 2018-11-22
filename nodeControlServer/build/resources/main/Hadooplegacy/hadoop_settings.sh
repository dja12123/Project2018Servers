sudo mkdir -p /opt/hadoop_tmp/
sudo mkdir -p /opt/hadoop/
sudo chown -Rh meshproject /opt/hadoop_tmp/
sudo chown -Rh meshproject /opt/hadoop/

enviovar=/home/meshproject/.bashrc
if ! grep -q "export HADOOP_HOME=/opt/hadoop/hadoop" $enviovar; then
echo "export HADOOP_HOME=/opt/hadoop/hadoop" >> $enviovar
echo 'export PATH=$PATH:$HADOOP_HOME/bin' >> $enviovar
echo 'export PATH=$PATH:$HADOOP_HOME/sbin' >> $enviovar
echo 'export HADOOP_MAPRED_HOME=$HADOOP_HOME' >> $enviovar
echo 'export HADOOP_COMMON_HOME=$HADOOP_HOME' >> $enviovar
echo 'export HADOOP_HDFS_HOME=$HADOOP_HOME' >> $enviovar
echo 'export YARN_HOME=$HADOOP_HOME' >> $enviovar
echo 'export HADOOP_COMMON_LIB_DIR=$HADOOP_HOME/lib/native' >> $enviovar
echo 'export HADOOP_OPTS="-Djava.library.path=$HADOOP_HOME/lib"' >> $enviovar
echo 'export JAVA_HOME=/usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt/jre' >> $enviovar
fi
. ~/.bashrc

while [ true ]; do
	echo ">>>> Are you sure download hadoop3.0.3?(yes/no)"
	read qa
	if [ $qa = "yes" ]; then
		cd /opt/hadoop/
		sudo wget http://mirror.apache-kr.org/hadoop/common/hadoop-3.0.3/hadoop-3.0.3.tar.gz
		sudo tar xvf hadoop-3.0.3.tar.gz
		sudo mv hadoop-3.0.3 hadoop
		echo ">>>> end download and extract hadoop"
		break
	elif [ $qa = "no" ]; then
		echo ">>>> skip download hadoop"
		break
	else
		echo ">>>> enter yes or no"
	fi
done
envsh=$HADOOP_HOME/etc/hadoop/hadoop-env.sh
jenv="export JAVA_HOME=/usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt/jre"
if [ -f $envsh ] & ! grep -q "$jenv" "$envsh"; then
echo "$jenv" >> $envsh
fi
