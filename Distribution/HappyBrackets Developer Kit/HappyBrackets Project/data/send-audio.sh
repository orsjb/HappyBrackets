#!/bin/bash

### Script to upload audio to PI by automating the scp command
### The device is added as a parameter to the script



if [ $# -gt 0 ]; then
    DEVICE_NAME=$1
else
   echo "Enter the device Name"
   read DEVICE
   DEVICE_NAME=$DEVICE
fi

    echo "Audio willbe sent from this folder"


    FILE_PATH=$(dirname $0)
    echo "Send audio FROM ${FILE_PATH} to ${DEVICE_NAME} "



if [ "$DEVICE_NAME" != "" ]; then
# run scp 

    HOST_ADDRESS="pi@${DEVICE_NAME}"
    echo “Sending audio to ${HOST_ADDRESS}”
    scp -r $FILE_PATH/audio/* $HOST_ADDRESS:~/HappyBrackets/data/audio/

    echo "We need to flush files to your device to prevent disk becoming corrupt. The following line will be sent when you enter you password"

    echo "sync"

    ssh $HOST_ADDRESS "sync"



else
    echo "You need to enter the device name and the Source Folder as argument to call. eg ${0} hb-001d43801b7a.local ~/Documents/mydownload/"
fi


