#!/bin/bash

# Script to autorun HappyBrackets

# get the MAC address to use as hostname

NEWHOST=`cat /sys/class/net/wlan0/address | sed s/://g`
OLDHOST=`cat /etc/hostname`

if [ "$NEWHOST" == "" ]
then
	NEWHOST=`cat /sys/class/net/eth0/address | sed s/://g`
fi

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

# Run the main app. $ACTION variable below can be used to specify an HBAction to run on startup. Make sure the compiled HBAction class, and any dependencies (including anonymous classes) are placed into data/classes.

BUF=1024
SR=44100
BITS=16
INS=0
OUTS=1 
AUTOSTART=true 
ACTION=

/usr/bin/sudo /usr/bin/java —cp data/classes -Xmx512m -jar HB.jar buf=$BUF sr=$SR bits=$BITS ins=$INS outs=$OUTS start=$AUTOSTART $ACTION > stdout &

# Finally, run the network-monitor.sh script to keep WiFi connection alive

/usr/bin/sudo scripts/network-monitor.sh > netstatus &