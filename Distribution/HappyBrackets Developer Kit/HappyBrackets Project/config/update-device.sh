#!/bin/bash

### Script to upload new network to device by automating the scp command
### The device is added as a parameter to the script

### move to the correct dir for running this script 
cd ../libs


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
    echo "Update device"

    while true
    do

        HOST_ADDRESS="pi@${DEVICE_NAME}"
        echo “Running Upload to ${HOST_ADDRESS}”
        scp HB.jar $HOST_ADDRESS:~/HappyBrackets

        # now we need to SSH into device so we can do a restart of PI
        echo "We need to reboot our device"

        echo "sudo shutdown -r now"

        ssh $HOST_ADDRESS "sudo shutdown -r now"

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
    echo "You need to enter the device name as argument to call. eg ${0} hb-001d43801b7a.local"
fi


