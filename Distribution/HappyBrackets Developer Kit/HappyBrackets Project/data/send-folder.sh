#!/bin/bash

### Script to upload folder to PI by automating the scp command
### The device and folder can be added as a parameter to the script

DEVICE_DATA_PATH="../Device/HappyBrackets/data"
cd "$DEVICE_DATA_PATH"
FILE_PATH="$(pwd)"

echo "Current dir" $FILE_PATH
echo "Folders available to send are as follows:"

find . -type d


if [ $# -gt 0 ]; then
    FOLDER_NAME=$1
else
   echo "You need to enter the foldername that you want to send. Available folders are listed above"
     read FOLDER
     FOLDER_NAME=$FOLDER
fi

if [ $# -gt 1 ]; then
    FOLDER_NAME=$2
else
   echo "Enter the device address - eg 192.168.0.3"
   read DEVICE
   DEVICE_NAME=$DEVICE
fi

echo "Files will be sent from this folder"
SCRIPT_PATH="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# move into Device Folder




echo "Send folder FROM ${FILE_PATH} to ${DEVICE_NAME} "


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

            // first make folder
            expect_password "ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $HOST_ADDRESS \"mkdir -p ~/HappyBrackets/data/$FOLDER_NAME\""

            expect_password "sh -c \"scp -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -r $FOLDER_NAME/* $HOST_ADDRESS:~/HappyBrackets/data/$FOLDER_NAME/\""


            # now we need to SSH into device so we can do a restart of PI
            echo "We need to reboot our device"

            echo "sync"

            #Do a spawn to sync

            expect_password "ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $HOST_ADDRESS \"sync\""


        else
            ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $HOST_ADDRESS "mkdir -p ~/HappyBrackets/data/$FOLDER_NAME"
            scp -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -r $FILE_PATH/$FOLDER_NAME/* $HOST_ADDRESS:~/HappyBrackets/data/$FOLDER_NAME/

            # now we need to SSH into device so we can do a sync of PI filsystem
            echo "We need to sync our device"

            echo "sync"

            ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $HOST_ADDRESS "sync"

        fi


        #see if we want to update another device - see if they entered the device name-
        # note this is different to other scripts
        if [ $# -gt 1 ]; then
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
    echo "You need to enter the device name and the Source Folder as argument to call. eg ${0} 192.168.0.5 ~/Documents/mydownload/"
fi


