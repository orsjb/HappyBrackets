# Script to setup new pi disc image with HappyBrackets and appropriate details

# run from pi, with an internet connection
#download image from Github

curl https://raw.githubusercontent.com/orsjb/HappyBrackets/master/DeviceSetup/setup-pi-image.sh | bash

# now see if they want to enable auto renaming of device
        while true
        do
	    #see if we need to send to more devices
            echo "Do you want to automatically rename your PI. Type Y or N";
            read RESPONSE

            case $RESPONSE in

                [yY] | [yY][Ee][Ss] )
                    echo "Installing auto-rename script"
                    sudo wget --no-check-certificate -N https://raw.githubusercontent.com/orsjb/HappyBrackets/master/DeviceSetup/auto-rename.sh
                    sudo mv auto-rename.sh /home/pi/HappyBrackets/scripts/
                    sudo chmod +x /home/pi/HappyBrackets/scripts/auto-rename.sh

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