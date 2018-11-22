sed -i 's/http:\/\/raspbian.raspberrypi.org\/raspbian\//http:\/\/ftp.kaist.ac.kr\/raspbian\/raspbian\//g' /etc/apt/sources.list

apt-get -y update
apt-get -y upgrade
apt-get -y install gradle
apt-get -y install default-jdk
apt-get -y install wiringpi