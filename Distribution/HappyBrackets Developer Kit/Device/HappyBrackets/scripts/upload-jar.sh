#!/bin/bash

### Script to upload new Jar to device by automating the scp command
### The device is added as a parameter to the script

### move to the correct dir for running this script (one level above where this script is)
DIR=`dirname $0`
cd ${DIR}/..

if [ $# -gt 0 ]; then
    DEVICE_NAME=$1
else
    echo "Enter the device Name"
   read DEVICE
   DEVICE_NAME=$DEVICE
   echo "${DEVICE_NAME}"
fi


if [ "$DEVICE_NAME" != "" ]; then
# run scp 

    echo “Running Upload to ${DEVICE_NAME}”

    scp HB.jar pi@$DEVICE_NAME:~/HappyBrackets

else
    echo "You need to enter the device name as argument to call. eg ${0} hb-001d43801b7a.local"
fi



