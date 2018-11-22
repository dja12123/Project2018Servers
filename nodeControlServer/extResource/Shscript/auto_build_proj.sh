#!/bin/bash

cd ~/Project2018Servers/
git pull
gradle moveres
gradle clean build
sh ./nodeControlServer/extResource/Shscript/all_change_unix.sh