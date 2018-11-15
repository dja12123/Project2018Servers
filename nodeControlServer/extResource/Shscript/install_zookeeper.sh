if [ $# -ne 1 ]; then
	exit 1
fi

mkdir $1/../
cd $1/../

wget http://mirror.apache-kr.org/zookeeper/zookeeper-3.4.13/zookeeper-3.4.13.tar.gz
tar -xvf zookeeper-3.4.13.tar.gz
mv zookeeper-3.4.13 zookeeper

echo "export ZK_HOME=$1" >> /etc/bashrc