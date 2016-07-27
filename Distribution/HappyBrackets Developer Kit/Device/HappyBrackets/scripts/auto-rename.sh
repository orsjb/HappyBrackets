#!/bin/bash

### get the MAC address to use as hostname

NEWHOST=`cat /sys/class/net/wlan0/address | sed s/://g`
OLDHOST=`cat /etc/hostname`

if [ "$NEWHOST" == "" ]
then
	NEWHOST=`cat /sys/class/net/eth0/address | sed s/://g`
fi

### correct format of hostname (hb-<MAC>)
 
NEWHOST=hb-${NEWHOST}

# reboot with correct hostname if required

if [ "$NEWHOST" != "$OLDHOST" ] 
then
	echo "Changing hostname to format hb-<MAC>. This will require a reboot."
	echo $NEWHOST > hostname
	sudo mv hostname /etc/
	sudo reboot 
fi
