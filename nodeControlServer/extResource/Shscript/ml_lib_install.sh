bashrc=/etc/bash.bashrc

if ! grep -q ".*PYSPARK_PYTHON.*" $bashrc; then
	echo "export PYSPARK_PYTHON=python3" >> $bashrc
fi

. $bashrc

apt-get -y update

apt -y install libatlas-base-dev

apt-get -y install python3.6
apt-get -y install python3-pip

pip3 install tensorflow
pip3 install tensorflowonspark

#if ! grep -q ".*PYSPARK_DRIVER_PYTHON.*" $bashrc; then
#	echo "export PYSPARK_DRIVER_PYTHON=jupyter" >> $bashrc
#	echo "export PYSPARK_DRIVER_PYTHON_OPTS='notebook'" >> $bashrc
#fi
pip3 install jupyter jupyter[notebook]
pip3 install flask-socketio
pip3 install pygame

echo "Reboot System?(y/n)"
read answer
if [ $answer = "y" ]; then
	reboot
fi