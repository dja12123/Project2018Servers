#!/bin/bash

cd ~/Project2018Servers/
git pull
gradle clean build
gradle moveres
sh ./nodeControlServer/extResource/Shscript/all_change_unix.sh