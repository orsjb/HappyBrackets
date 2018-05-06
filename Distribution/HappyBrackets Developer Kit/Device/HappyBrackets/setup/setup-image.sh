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

# install java 8
sudo apt-get -y --force-yes install oracle-java8-jdk 

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

# set up autorun
wget --no-check-certificate -N https://raw.githubusercontent.com/orsjb/HappyBrackets/master/DeviceSetup/rc.local
sudo mv rc.local /etc/
chmod +x /etc/rc.local

# set up ssh login
sudo update-rc.d ssh enable
sudo invoke-rc.d ssh start

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
echo "You can (and should) change these details by making changes to the interfaces file:"
echo "sudo nano /etc/network/interfaces"


