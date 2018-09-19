#!/bin/bash

### Script to autorun HappyBrackets on a local computer (i.e., typically your mac or PC where you are running IntelliJ rather than your remote device such as a Raspberry Pi).
### This script is similar to run.sh but does run the auto-rename nor the network monitor, and defaults to running HappyBrackets in 'local' access mode.

### run HappyBrackets
### args to HB.jar are: buf (buffer size, default=1024), sr (sample rate, default=44100), bits (sample bit size, default=16), ins (input channels, default=0), outs (output channels, default=1), start (autostart audio, default=true), access (live code access mode, either ‘open’, ‘local’ or ‘closed’, default=open), followed by the full class path to any HBAction you wish to auto run. All args except the last one can be entered in any order.


#define the file that has custom settings
CONFIG_FILE=$HOSTNAME.config

BUF=1024
SR=44100
BITS=16
INS=0
OUTS=1 
AUTOSTART=true 
ACCESSMODE=open
ACTION=
SIMULATE=true
CONFIG=device-config.json

#let us see if we have any specific values we want to use
while IFS="=" read line val
do
    if [ "$line" = "BUF"   ]
    then
        BUF=$val
        echo "Set SR to "$BUF
    fi

    if [ "$line" = "SR"   ]
    then
        SR=$val
        echo "Set SR to "$SR
    fi

    if [ "$line" = "BITS"   ]
    then
        BITS=$val
        echo "Set Bits to "$BITS
    fi

    if [ "$line" = "INS"   ]
    then
        INS=$val
        echo "Set INS to "$INS
    fi


    if [ "$line" = "OUTS"   ]
    then
        OUTS=$val
        echo "Set OUTS to "$OUTS
    fi

    if [ "$line" = "DEVICE"   ]
    then
        DEVICE=$val
        echo "Set DEVICE to "$DEVICE
    fi

    if [ "$line" = "AUTOSTART"   ]
    then
        AUTOSTART=$val
        echo "Set AUTOSTART to "$AUTOSTART
    fi

    if [ "$line" = "ACCESSMODE"   ]
    then
        ACCESSMODE=$val
        echo "Set ACCESSMODE to "$ACCESSMODE
    fi

    if [ "$line" = "CONFIG"   ]
    then
        CONFIG=$val
        echo "Set CONFIG to "$CONFIG
    fi

done <$CONFIG_FILE

### move to the correct dir for running this script (one level above where this script is)
DIR=`dirname $0`
cd ${DIR}/..

java -cp "data/classes:HB.jar:data/jars/*" -Xmx512m net.happybrackets.device.DeviceMain buf=$BUF sr=$SR bits=$BITS ins=$INS outs=$OUTS start=$AUTOSTART access=$ACCESSMODE $ACTION simulate=$SIMULATE config=$CONFIG  
