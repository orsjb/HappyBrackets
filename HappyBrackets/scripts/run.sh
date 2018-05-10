#!/bin/bash

# Script to autorun on pi
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

# Run the main app
# args are bufSize (512), sample rate (44100), bits (16), input channels (0), output channels (1), autostart (true)

BUF=1024
SR=44100
BITS=16
INS=0
OUTS=1
DEVICE=0
AUTOSTART=true
ACTION=

(/usr/bin/sudo /usr/bin/java -cp data/classes -Xmx512m -jar HB.jar buf=$BUF sr=$SR bits=$BITS ins=$INS outs=$OUTS device=$DEVICE start=$AUTOSTART $ACTION > stdout &) &

# Finally, run the network-monitor.sh script to keep WiFi connection alive

(/usr/bin/sudo scripts/network-monitor.sh > netstatus &) &
