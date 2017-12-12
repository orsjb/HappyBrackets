#!/bin/bash

### Script to upload audio to PI by automating the scp command
### The device is added as a parameter to the script


if [ $# -gt 1 ]; then
    DEVICE_NAME=$1
    FILE_PATH=$2
else
   echo "Enter the device Name"
   read DEVICE
   DEVICE_NAME=$DEVICE

    echo "Enter Where you want the audio sent from. The folder must have a folder called audio inside it"
    echo "eg, if your files are in ~/Documents/audio  just enter Documents"

    read TARGET
    FILE_PATH=$TARGET
    echo "Send audio FROM ${TARGET} to ${DEVICE_NAME} "
fi


if [ "$DEVICE_NAME" != "" ]; then
# run scp 

    HOST_ADDRESS="pi@${DEVICE_NAME}"
    echo “Sending audio to ${HOST_ADDRESS}”
    scp -r ~/$FILE_PATH/audio/* $HOST_ADDRESS:~/HappyBrackets/data/audio/

    echo "We need to flush filles to your device to prevent disk becoming corrupt. The following line will be sent when you enter you password"

    echo "sync"

    ssh $HOST_ADDRESS "sync"



else
    echo "You need to enter the device name and the Source Folder as argument to call. eg ${0} hb-001d43801b7a.local ~/Documents/mydownload/"
fi


