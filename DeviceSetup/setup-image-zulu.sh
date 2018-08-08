# Script to setup new pi disc image with HappyBrackets and appropriate details

# run from pi, with an internet connection

# we must download and unzip HappyBrackets before we go into our sudo script so files will be unzipped to correct path

cd
# get the happybrackets zipped project folder
# get latest filename
FILENAME=$(curl http://www.happybrackets.net/downloads/happy-brackets-runtime.php?version)

echo $FILENAME
curl -O http://www.happybrackets.net/downloads/$FILENAME
unzip $FILENAME
rm $FILENAME

#everything below this needs to be sudo
sudo -s <<SUDO_EXE

# keep apt-get up to date with mirrors
apt-get -y update


#we need to install dirmngr so we can install zulu
# https://blog.sleeplessbeastie.eu/2017/11/02/how-to-fix-missing-dirmngr/
apt-get install dirmngr --install-recommends

# install zeroconf
apt-get -y --force-yes install libnss-mdns
apt-get -y --force-yes install netatalk

# install i2c tools
apt-get -y --force-yes install i2c-tools

#we need to installk wiringPI becasue it is not in STretch Lite
sudo apt-get -y --force-yes install wiringpi

# install ZULU java 8
apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys 0x219BD9C9
echo 'deb http://repos.azulsystems.com/debian stable main' > /etc/apt/sources.list.d/zulu.list

apt-get update -qq
apt-get install zulu-embedded-8

# Enable I2C on raspi, to connect to sensors. 
# Counter-intuitively 'do_i2c 0' means 'enable'. 
raspi-config nonint do_i2c 0


# TODO setup audio if necessary.
# set audio output to max volume, well not quite max but close
amixer cset numid=1 0
# save audio settings
alsactl store


# set up ssh login
update-rc.d ssh enable
invoke-rc.d ssh start

#end sudo privilege here
SUDO_EXE

# now we need to see if we are going to start HappyBrackets from rc.local or from GUI Startup
#we should be back to PI user

echo "Who am I"
echo $USER

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


