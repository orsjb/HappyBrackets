#!/bin/bash

### Script to upload jar files to PI by automating the scp command
### The device is added as a parameter to the script



if [ $# -gt 0 ]; then
    DEVICE_NAME=$1
else
   echo "Enter the device Name"
   read DEVICE
   DEVICE_NAME=$DEVICE
fi

    echo "Jar files will be sent from this folder"


    FILE_PATH=$(dirname $0)
    echo "Send jars FROM ${FILE_PATH} to ${DEVICE_NAME} "



if [ "$DEVICE_NAME" != "" ]; then
# run scp 
    while true
    do
        HOST_ADDRESS="pi@${DEVICE_NAME}"
        echo “Sending jar files to ${HOST_ADDRESS}”
        scp -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -r $FILE_PATH/jars/* $HOST_ADDRESS:~/HappyBrackets/data/jars/

        # now we need to SSH into device so we can do a restart of PI
        echo "We need to reboot our device"

        echo "sudo shutdown -r now"

        ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $HOST_ADDRESS "sudo shutdown -r now"



#see if we want to update another device
        while true
        do
	    #see if we need to send to more devices
            echo "Do you want to update another PI. Type Y or N";
            read RESPONSE

            case $RESPONSE in

                [yY] | [yY][Ee][Ss] )
                    echo "Repeat"
		    echo "Enter the device Name"
		    read DEVICE
		    DEVICE_NAME=$DEVICE
		    echo "${DEVICE_NAME}"

                    break
                    ;;

                [nN] | [n|N][O|o] )
                    echo "Quitting";
                    exit 0
                    ;;
                *) echo "Invalid input"
                    ;;
            esac
        done
    done


else
    echo "You need to enter the device name and the Source Folder as argument to call. eg ${0} hb-001d43801b7a.local ~/Documents/mydownload/"
fi


