#!/bin/bash

# Script to autorun on pi
# get the MAC address to use as hostname

NEWHOST=`cat /sys/class/net/wlan0/address | sed s/://g`
OLDHOST=`cat /etc/hostname`

# correct format of hostname (pisound-<MAC>)
 
NEWHOST=pisound-${NEWHOST}

# reboot with correct hostname if required

if [ "$NEWHOST" != "$OLDHOST" ] 
then
	echo "Changing hostname to format pisound-<MAC>. This will require a reboot."
	echo $NEWHOST > hostname
	sudo mv hostname /etc/
	sudo reboot 
fi

# move to the correct dir for running java (one level above where this script is)

DIR=`dirname $0`
cd ${DIR}/..

echo “Running HappyBrackets”

# Run the main app
# args are bufSize (512), sample rate (44100), bits (16), input channels (0), output channels (1), autostart (true)

BUF=512
SR=44100
BITS=16
INS=0
OUTS=1 
AUTOSTART=true 

/usr/bin/sudo /usr/bin/java -cp HB.jar net.happybrackets.device.DeviceMain $BUF $SR $BITS $INS $OUTS $AUTOSTART  > stdout &

################ OPTIONAL ####################
## Edit and uncomment the following two lines if you want to run a specific class on startup. You will need to have compiled the class and updated HB.jar on the device so that it contains this class.
#sleep 10
#/usr/bin/sudo /usr/bin/java -cp HB.jar compositions.pipos_2014.webdirections.fluff_install.FluffyWoolInstallation &
############## ------------- #################

# Finally, run the network-monitor.sh script to keep WiFi connection alive

/usr/bin/sudo scripts/network-monitor.sh > netstatus &