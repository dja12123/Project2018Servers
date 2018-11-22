#!/bin/bash

cd ~/Project2018Servers/
rm -r ./nodeControlServer/extResource/Shscript/*
git pull
gradle clean build
gradle moveres
sh ./nodeControlServer/extResource/Shscript/all_change_unix.sh