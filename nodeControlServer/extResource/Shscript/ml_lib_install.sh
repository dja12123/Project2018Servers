dpkg -s "python-pip" &> /dev/null 2>&1

if [ $? -eq 0 ]; then
	echo "already installed"
	exit 1
fi
yes | apt-get install python3
yes | apt-get install python-pip
pip install tensorflow
pip install tensorflowonspark
pip install jupyter jupyter[notebook]
pip install pygame