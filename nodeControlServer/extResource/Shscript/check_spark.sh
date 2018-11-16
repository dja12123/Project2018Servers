#!/bin/bash

if [ -e $1/spark ]; then
	echo "true"
else
	echo "false"
fi
