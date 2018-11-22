if [ dpkg -s python-pip ]; then
	echo "already installed"
	exit 1
fi

apt-get install python-pip
pip install tensorflow
pip install tensorflowonspark
pip install jupyter jupyter[notebook]