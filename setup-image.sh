# Script to setup new pi disc image with HappyBrackets and appropriate details

# run from pi, with an internet connection

# install zeroconf
sudo apt-get install libnss-mdns
sudo apt-get install netatalk

# We will skip the wifi driver for now as the Raspi3 has one built in

# get 'interfaces' file and copy it to /etc/network
wget http://happybrackets.net/image/interfaces
sudo mv interfaces /etc/network/interfaces

# clone the github repos to 
cd 
mkdir git
cd git
git clone https://github.com/orsjb/HappyBrackets.git

# TODO setup audio if necessary.
# set audio output to max volume

# Setup autorun
