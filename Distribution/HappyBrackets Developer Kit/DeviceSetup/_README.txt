What’s in this folder

This folder contains files that you need to copy to your SDCard if you choose NOT to install the default image.
Copy the contents of the folder to your SDCard from your computer. When you connect to your Pi, access them through console or SSH in the /boot directory

ssh
____
This file will enable SSH on your PI

wpa_supplicant.conf
____________________
This will set up the WIFI configuration on your device. Change the settings to match your WIFI network

setup-device.sh
______________
Run this script to install HappyBrackets and dependencies. EG
cd /boot; ./setup-image.sh

