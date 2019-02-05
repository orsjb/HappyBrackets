#!/bin/bash

### Script to upload scripts to PI by automating the scp command
### The device is added as a parameter to the script



if [ $# -gt 0 ]; then
    DEVICE_NAME=$1
else
   echo "Enter the device address - eg 192.168.0.3"
   read DEVICE
   DEVICE_NAME=$DEVICE
fi

echo "Scripts will be sent from this folder"

SCRIPT_PATH="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# move into Device Folder

DEVICE_DATA_PATH="../Device/HappyBrackets/data"
cd "$DEVICE_DATA_PATH"
FILE_PATH="$(pwd)"

echo "Current dir" $FILE_PATH

echo "Send scripts FROM ${FILE_PATH} to ${DEVICE_NAME} "


## first see if we can use expect to save our passwords inside the script
if [ -f "/usr/bin/expect" ]
then
    DEF_PASSWORD="raspberry"

    while true
    do
        echo "Enter Password. Just press enter for default "$DEF_PASSWORD
        read -s PASSWORD

        ENTRY_LEN=${#PASSWORD}

        if [ ${#PASSWORD}  -eq 0 ]
        then
            PASSWORD=$DEF_PASSWORD
            echo "Use Default password"
            break

        else
            #we need to check that the same password is used
            echo "Re Enter Password."
            read -s PASSWORD_RETRY
            if [ "$PASSWORD" = "$PASSWORD_RETRY" ]
            then
                break
            else
                echo "Passwords do not match"
            fi
        fi
    done
fi


#define a function where we can use our SSH commands in a shell and pass our password in so we don't ned to interact
function expect_password {
    expect -c "\
    set timeout 90
    set env(TERM)
    spawn $1
    expect \"assword:\"
    send \"$PASSWORD\r\"
    expect eof
  "
}

if [ "$DEVICE_NAME" != "" ]; then
# run scp
    while true
    do
        HOST_ADDRESS="pi@${DEVICE_NAME}"
        echo “Sending jar files to ${HOST_ADDRESS}”

        ## See if we can automate this with our stored password
        if [ -f "/usr/bin/expect" ]
        then

            expect_password "sh -c \"scp -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -r scripts/* $HOST_ADDRESS:~/HappyBrackets/data/scripts/\""


            # now we need to SSH into device so we can do a restart of PI
            echo "We need to reboot our device"

            echo "sudo shutdown -r now"

            #Do a spawn to flush

            expect_password "ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $HOST_ADDRESS \"sync\""


        else
            HOST_ADDRESS="pi@${DEVICE_NAME}"
            echo “Sending scripts to ${HOST_ADDRESS}”
            scp -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -r $FILE_PATH/scripts/* $HOST_ADDRESS:~/HappyBrackets/data/scripts/

            echo "We need to flush files to your device to prevent disk becoming corrupt. The following line will be sent when you enter you password"

            echo "sync"

            ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $HOST_ADDRESS "sync"


        fi

        #see if we want to update another device - see if they entered the device name
        if [ $# -gt 0 ]; then
            echo "No More devices"
            exit 0
        fi

        #see if we want to update another device
        while true
        do
	    #see if we need to send to more devices
            echo "Do you want to update another PI. Type Y or N";
            read RESPONSE

            case $RESPONSE in

                [yY] | [yY][Ee][Ss] )
                    echo "Repeat"
		    echo "Enter the device address - eg 192.168.0.3"
		    read DEVICE
		    DEVICE_NAME=$DEVICE
		    echo "${DEVICE_NAME}"

                    break
                    ;;

                [nN] | [nN][Oo] )
                    echo "Quitting";
                    exit 0
                    ;;
                *) echo "Invalid input"
                    ;;
            esac
        done
    done


else
    echo "You need to enter the device address and the Source Folder as argument to call. eg ${0} 192.168.0.5 ~/Documents/mydownload/"
fi


