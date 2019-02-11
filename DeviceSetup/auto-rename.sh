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

#we need to strip out all 127.0.1.1 from hosts unless it is this hostname

OUT_TEXT=""
while IFS='' read -r line || [[ -n "$line" ]]; do

    if  [[ $line == $LOCAL_MACHINE ]] || [[ $line == $LOCAL_MACHINE* ]] ;
    then
      # See if  we are going to override this by checking if it our one
      if [[ $line ==  $MACHINE_TEXT ]] ;
      then # this is our valid  host. Keep It
        FOUND=true
      else # this is old and written before we re-wrrote the name. We need to remov$
        REWRITE_REQUIRED=true
      fi
    else
        OUT_TEXT=$OUT_TEXT$'\n'$line
    fi

done < "/etc/hosts"

OUT_TEXT=$OUT_TEXT$'\n'$MACHINE_TEXT


if [ ! -z "$FOUND" ] ; # If our host name is not in there, we will need to do a write
then
    REWRITE_REQUIRED=true
fi

if [ -z "$REWRITE_REQUIRED" ] ;
then
        echo "Rewrite required"
        echo "$OUT_TEXT" > /etc/hosts
fi


# reboot with correct hostname if required

if [ "$NEWHOST" != "$OLDHOST" ] 
then
	echo "Changing hostname to format hb-<MAC>. This will require a reboot."
	echo $NEWHOST > hostname
	sudo mv hostname /etc/
	sudo reboot 
fi



