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

LOCAL_MACHINE="127.0.1.1"
MACHINE_TEXT="$LOCAL_MACHINE"$'\t'"$NEWHOST"

#we need to add this hostname to hosts file
if grep -Fxq "$MACHINE_TEXT" /etc/hosts
then
    # code if found
    echo "Hostname already in hosts file";
else
    # code if not found
    echo "Append hostname to hosts file"
    echo -n "$MACHINE_TEXT"$'\n' >> /etc/hosts
fi

# reboot with correct hostname if required

if [ "$NEWHOST" != "$OLDHOST" ] 
then
	echo "Changing hostname to format hb-<MAC>. This will require a reboot."
	echo $NEWHOST > hostname
	sudo mv hostname /etc/
	sudo reboot 
fi



