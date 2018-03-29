#!/bin/bash

### Script to restore the network settings to their default
### The device is added as a parameter to the script

### move to the correct dir for running this script (one level above where this script is)
cd device

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
	
    while true
    do

	#read the default network settingd
	interfaces=$(<interfacesback)

	echo "$interfaces" >interfaces
        HOST_ADDRESS="pi@${DEVICE_NAME}"
        echo “Running Upload to ${HOST_ADDRESS}”
        scp interfaces $HOST_ADDRESS:/etc/network

	# now remove the file we created so it does not site on the drive
	rm interfaces

	echo "We need to flush files to your device to prevent disk becoming corrupt. The following line will be sent when you enter you password"

	echo "sync"

	ssh $HOST_ADDRESS "sync"


	#see if we want to set another device
        while true
        do
	    #see if we need to reset more devices
            echo "Do you want to run again. Type Y or N";
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


