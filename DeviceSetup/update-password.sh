#!/bin/bash

# Run this script from your controller computer to automatically change your password from the default on any remote devices. You will be prompted to give the device name and a new password.

echo "Enter the name of the device (e.g., hb-abcd12345678)…"

read DEVICE_NAME

echo "Enter the current password…"

read -s CURRENT_PWD

echo "Enter the new password..."

read -s NEW_PWD1

echo "Confirm the new password..."

read -s NEW_PWD2

if(( NEW_PWD1 != NEW_PWD2 )); then
	echo "Error: passwords do not match. Try again!"
	exit 1
fi

# At this point log into the device via SSH and change the master password, as well as changing the entry in ~/HappyBrackets/config/device-config.json. How do we determine success?

ssh pi:${CURRENT_PWD}@${DEVICE_NAME}.local

echo "Password has been changed. You will need to set this password in your controller config in the IntelliJ Plugin in order to be able to speak to the device."


