#!/bin/bash

cd ~/Project2018Servers/
git pull
sh ./nodeControlServer/extResource/Shscript/all_change_unix.sh
gradle clean build
gradle moveres