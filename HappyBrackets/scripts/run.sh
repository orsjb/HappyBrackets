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

# Run the main app
# args are bufSize (8192), sample rate (22050), bits (16), input channels (0), output channels (1), autostart (true)

BUF=4096
SR=22050
BITS=16
INS=0
OUTS=1 
AUTOSTART=true 

/usr/bin/sudo /usr/bin/java -cp HappyBrackets.jar net.happybrackets.device.DeviceMain $BUF $SR $BITS $INS $OUTS $AUTOSTART  > stdout &

############## BONUS FEATURE #################
## Also run the code app (but wait a bit first)
#sleep 10
#/usr/bin/sudo /usr/bin/java -cp HappyBrackets.jar compositions.pipos_2014.webdirections.fluff_install.FluffyWoolInstallation &
############## ------------- #################

### Various old or test scripts ###
# /usr/bin/sudo /usr/bin/java -cp build/picode.jar test.MiniMUTest $BUF $SR $BITS $INS $OUTS  > stdout &
# /usr/bin/sudo /usr/bin/java -cp build/picode.jar test.PI4JTest > stdout &
# libs/minimulib/minimu9-ahrs -b /dev/i2c-1 | /usr/bin/sudo /usr/bin/java -cp build/picode.jar test.PrintStdIn $BUF $SR $INS $OUTS > stdout &
# echo ~ > stdout &
# libs/minimulib/minimu9-ahrs -b /dev/i2c-1 > stdout &
# /usr/bin/sudo /usr/bin/java -cp build/picode.jar dynamic.DynamoPI $BUF $SR $BITS $INS $OUTS > stdout &
# /usr/bin/sudo /usr/bin/java -cp build/picode.jar synch.Synchronizer $BUF $SR $BITS $INS $OUTS > stdout &
####################################

# Finally, run the network-monitor.sh script to keep WiFi connection alive

/usr/bin/sudo scripts/network-monitor.sh > netstatus &