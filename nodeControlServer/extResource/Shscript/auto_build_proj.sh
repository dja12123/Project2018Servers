#!/bin/bash

cd ~/Project2018Servers/
git pull
gradle moveres
sh ./nodeControlServer/extResource/Shscript/all_change_unix.sh
gradle clean build