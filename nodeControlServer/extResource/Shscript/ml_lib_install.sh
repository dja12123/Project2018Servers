dpkg -s "python3-pip" > /dev/null 2>&1

bashrc=/etc/bash.bashrc

if ! grep -q ".*PYSPARK_PYTHON.*" $bashrc; then
	echo "export PYSPARK_PYTHON=python3" >> $bashrc
fi

if [ $? -eq 0 ]; then
	echo "already installed"
	exit 1
fi
apt-get -y update

apt -y install libatlas-base-dev

apt-get -y install python3-pip

pip3 install tensorflow
pip3 install tensorflowonspark
pip3 install jupyter jupyter[notebook]
pip3 install pygame