bashrc=/etc/bash.bashrc
if ! grep -q ".*libs/Project2018Servers.*" $bashrc; then
	echo "java -jar /root/Project2018Servers/build/libs/Project2018Servers.jar" >> $bashrc
fi