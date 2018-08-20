What’s in this folder

This folder contains files that you need to copy to your SDCard if you choose NOT to install the default image.
Copy the contents of the folder to your SDCard from your computer. When you connect to your Pi, access them through console or SSH in the /boot directory

ssh
____
This file will enable SSH on your PI

wpa_supplicant.conf
____________________
This will set up the WIFI configuration on your device. Change the settings to match your WIFI network

setup-image.sh
______________
Run this script to install HappyBrackets and dependencies using Zulu Java Virtual Machine (recommended). EG
cd /boot; ./setup-image.sh

Setup-image-oracle.sh
______________
Run this script to install HappyBrackets and dependencies using Oracle Java Virtual Machine. EG
cd /boot; ./setup-image-oracle.sh
