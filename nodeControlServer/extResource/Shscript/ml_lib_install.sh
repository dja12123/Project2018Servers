if [ dpkg -s python3 ]; then
	echo "already installed"
	exit 1
fi
yes | apt-get install python 3
yes | apt-get install python-pip
pip install tensorflow
pip install tensorflowonspark
pip install jupyter jupyter[notebook]