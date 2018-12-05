sed -i 's/http:\/\/raspbian.raspberrypi.org\/raspbian\//http:\/\/ftp.neowiz.com\/raspbian\/raspbian\//g' /etc/apt/sources.list

apt-get -y update
apt-get -y upgrade
apt-get -y install openjdk-8-jre
apt-get -y install openjdk-8-jdk
apt-get -y install gradle
apt-get -y install wiringpi
