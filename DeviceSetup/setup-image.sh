# Script to setup new pi disc image with HappyBrackets and appropriate details

# run from pi, with an internet connection

cd


# keep apt-get up to date with mirrors
sudo apt-get -y update

# install zeroconf
sudo apt-get -y --force-yes install libnss-mdns
sudo apt-get -y --force-yes install netatalk

# install i2c tools
sudo apt-get -y --force-yes install i2c-tools

while true
do
    #Let user decide which JVMs to install
    echo "Select which JVM to install.";
    echo "Select 'O' for Oracle or 'Z' for Zulu or type 'both' to install both."
    echo "Zulu will be selected fopr you if you do not make a selection within 10 seconds"
    RESPONSE="z" # we are going to make Zulu our default if they do not select in time

    read -t 10 -p "'O', 'Z' or 'both' >" RESPONSE

    case $RESPONSE in

        [oO]|[0] )
            echo "Set Oracle"
            INSTALL_ORACLE=true
            break
            ;;

        [zZ] )
            echo "Set Zulu";
            INSTALL_ZULU=true
            break
            ;;

        [bB] | [bB][oO][tT][hH] )
            echo "Set Both JVM";
            INSTALL_ZULU=true
            INSTALL_ORACLE=true
            break
            ;;
        *) echo "Invalid input"
            # we will set back to our default
            echo "$RESPONSE is not a valid response"
            ;;
    esac
done

# install java 8
if [ $INSTALL_ORACLE ]; then
    echo "Install Oracle"
    sudo apt-get -y --force-yes install oracle-java8-jdk
fi

if [ $INSTALL_ZULU ]; then
    echo "Install Zulu"

    # install ZULU java 8
    apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys 0x219BD9C9
    echo 'deb http://repos.azulsystems.com/debian stable main' > /etc/apt/sources.list.d/zulu.list

    apt-get update -qq
    apt-get install zulu-embedded-8
fi



#we need to installk wiringPI becasue it is not in STretch Lite - Needed for GPIO access
sudo apt-get -y --force-yes install wiringpi

# Enable I2C on raspi, to connect to sensors. 
# Counter-intuitively 'do_i2c 0' means 'enable'. 
sudo raspi-config nonint do_i2c 0

# get the happybrackets zipped project folder
# get latest filename
FILENAME=$(curl http://www.happybrackets.net/downloads/happy-brackets-runtime.php?version)

echo $FILENAME
curl -O http://www.happybrackets.net/downloads/$FILENAME
unzip $FILENAME
rm $FILENAME

# TODO setup audio if necessary.
# set audio output to max volume, well not quite max but close
amixer cset numid=1 0
# save audio settings
sudo alsactl store


# set up ssh login
sudo update-rc.d ssh enable
sudo invoke-rc.d ssh start


# now we need to see if we are going to start HappyBrackets from rc.local or from GUI Startup
#we should be back to PI user

GUI_FILE=~/.config/lxsession/LXDE-pi/autostart

if [ -f "$GUI_FILE" ]; then
    echo "This PI is a GUI Program. We need to append our startup script to it"
    START_TEXT="@/usr/bin/sudo /home/pi/HappyBrackets/scripts/run.sh"

    #we need to add this startup text to GUI init file
    #first see if it exists
    if grep -Fxq "$START_TEXT" "$GUI_FILE"
        then
        # code if found
        echo "Startup text already in GUI file";
    else
        # code if not found
        echo "Append startup text to file"
        echo $START_TEXT >>$GUI_FILE
    fi

else # this is a standard non-gui PI
    echo "This is a non gui PI. Add line to /etc/rc.local"
    START_TEXT="/usr/bin/sudo /home/pi/HappyBrackets/scripts/run.sh"
    RC_FILE=/etc/rc.local

    #we need to add this startup text to GUI init file
    #first see if it exists
    if grep -Fxq "$START_TEXT" $RC_FILE
        then
        # code if found
        echo "Startup text already in RC file";
    else
        # code if not found
        # set up autorun
        sudo wget --no-check-certificate -N https://raw.githubusercontent.com/orsjb/HappyBrackets/master/DeviceSetup/rc.local
        sudo mv rc.local /etc/
        sudo chmod +x /etc/rc.local

    fi
fi


# Network Settings
echo "***********************************"
echo "-----------------------------------"
echo "***********************************"
echo "Do you want to alter your WiFi network settings so your Pi automatically connects to 'PINet'?"
echo "If you are currently connected on a working WiFi network you probably don't want to change the network settings."
echo "If you are connected directly or over ethernet, you can change the network settings to connect to a particular wifi network."
echo "if you do wish to change the network settings so that your Pi is setup to automatically connect then do the following commands:"
echo ""
echo "NOTE: Do not run these commands on a normal laptop or computer, only on a Raspberry Pi."
echo ""
echo "wget --no-check-certificate -N https://raw.githubusercontent.com/orsjb/HappyBrackets/master/DeviceSetup/wpa_supplicant.conf"
echo "sudo cp /etc/wpa_supplicant/wpa_supplicant.conf /etc/wpa_supplicant/wpa_supplicant.conf"
echo "sudo mv wpa_supplicant.conf /etc/wpa_supplicant/wpa_supplicant.conf"
echo ""
echo "If you make this change, the SSID this PI will search for will be 'PINet'"
echo "Password is 'happybrackets'"



