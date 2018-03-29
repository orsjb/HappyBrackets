#!/bin/bash

### Script to autorun HappyBrackets on device

### move to the correct dir for running this script (one level above where this script is)
DIR=`dirname $0`
cd ${DIR}/..

### run the auto-rename script
scripts/auto-rename.sh

### run HappyBrackets
### args to HB.jar are: buf (buffer size, default=1024), sr (sample rate, default=44100), bits (sample bit size, default=16), ins (input channels, default=0), outs (output channels, default=1), start (autostart audio, default=true), access (live code access mode, either ‘open’, ‘local’ or ‘closed’, default=open), followed by the full class path to any HBAction you wish to auto run. All args except the last one can be entered in any order.

BUF=1024
SR=44100
BITS=16
INS=0
OUTS=1 
AUTOSTART=true 
ACCESSMODE=open
ACTION=

echo “Running HappyBrackets”

(/usr/bin/sudo /usr/bin/java -cp data/classes:HB.jar -Xmx512m net.happybrackets.device.DeviceMain buf=$BUF sr=$SR bits=$BITS ins=$INS outs=$OUTS start=$AUTOSTART access=$ACCESSMODE $ACTION > ramfs/stdout 2>&1) &

### Finally, run the network-monitor.sh script to keep WiFi connection alive
# Ignore this for stretch (scripts/network-monitor.sh > netstatus &) &
