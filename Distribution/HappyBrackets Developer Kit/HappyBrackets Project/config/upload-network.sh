#!/bin/bash

### Script to upload new network to device by automating the scp command
### The device is added as a parameter to the script

### move to the correct dir for running this script 
DIR=`dirname $0`
cd ${DIR}/device

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
    echo "Enter wireless network name"
    read SID
    echo "Enter wireless network password"   
    read PWD

	
    while true
    do

	#get the beginning part of the settings from file
	interfaces=$(<interfacesprefix)

	#add our network parameters to value
	chr="\""
	nl=$'\n'
	ssid="wpa-ssid $chr$SID$chr$nl"
	psk="wpa-psk $chr$PWD$chr$nl"
	#echo "$ssid"
	#echo "$psk"`
	interfacefile=$interfaces$nl$ssid$psk

	#write new file to disk for sending
	echo "$interfacefile" >interfaces
        HOST_ADDRESS="pi@${DEVICE_NAME}"
        echo “Running Upload to ${HOST_ADDRESS}”
        scp interfaces $HOST_ADDRESS:/etc/network

	# now remove the file we created so it does not remain on the disk
	rm interfaces
        # now we need to SSH into device so we can do a restart of PI
        echo "We need to reboot our device"

        echo "sudo shutdown -r now"

        ssh $HOST_ADDRESS "sudo shutdown -r now"

        while true
        do
	    #see if we need to send to more devices
            echo "Do you want to set theese parameters to another PI. Type Y or N";
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


