apt-get -y update

apt -y install libatlas-base-dev

apt-get -y install python3.6
apt-get -y install python3-pip

pip3 install tensorflow
pip3 install tensorflowonspark
pip3 install jupyter jupyter[notebook]
pip3 install flask-socketio

echo "Reboot System?(y/n)"
read answer
if [ $answer = "y" ]; then
	reboot
fi