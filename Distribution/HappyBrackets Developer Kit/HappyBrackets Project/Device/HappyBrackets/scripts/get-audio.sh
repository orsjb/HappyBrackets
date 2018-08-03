#!/bin/bash

### Script to download audio from PI by automating the scp command
### The device is added as a parameter to the script

### move to the correct dir for running this script (one level above where this script is)

if [ $# -gt 1 ]; then
    DEVICE_NAME=$1
    FILE_PATH=$2
else
   echo "Enter the device Name"
   read DEVICE
   DEVICE_NAME=$DEVICE

    echo "Enter Where you want the audio stored to from your home directory"
    read TARGET
    FILE_PATH=$TARGET
    echo "Get audio from ${DEVICE_NAME} and store in ${TARGET}"
fi


if [ "$DEVICE_NAME" != "" ]; then
# run scp 

    HOST_ADDRESS="pi@${DEVICE_NAME}"
    echo “Downloading audio from ${HOST_ADDRESS}”
    scp -r $HOST_ADDRESS:~/HappyBrackets/data/audio/ ~/$FILE_PATH

else
    echo "You need to enter the device name and the Target Folder as argument to call. eg ${0} hb-001d43801b7a.local Documents/mydownload/"
fi


