dpkg -s "python3-pip" > /dev/null 2>&1

if [ $? -eq 0 ]; then
	echo "already installed"
	exit 1
fi
yes | apt-get update

yes | apt install libatlas-base-dev

yes | apt-get install python3-picamera
yes | apt-get install python3-pip

pip3 install tensorflow
pip3 install tensorflowonspark
pip3 install jupyter jupyter[notebook]
pip3 install pygame