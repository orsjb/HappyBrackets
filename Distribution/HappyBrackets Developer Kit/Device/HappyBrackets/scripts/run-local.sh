#!/bin/bash

### Script to autorun HappyBrackets on device
### This script is identical to run.sh but defaults to running HappyBrackets in 'local' access mode.

### move to the correct dir for running this script (one level above where this script is)
DIR=`dirname $0`
cd ${DIR}/..

### run the auto-rename script
scripts/auto-rename.sh

### run HappyBrackets
### args are bufSize (512), sample rate (44100), bits (16), input channels (0), output channels (1), autostart (true)

BUF=1024
SR=44100
BITS=16
INS=0
OUTS=1 
AUTOSTART=true 
# ACCESSMODE may be 'open' (accept code from any controller), 'local' (accept 
# code from controller on local host), or 'closed' (do not accept any code).
ACCESSMODE=local
ACTION=

echo “Running HappyBrackets”

(/usr/bin/sudo /usr/bin/java -cp data/classes:HB.jar -Xmx512m net.happybrackets.device.DeviceMain buf=$BUF sr=$SR bits=$BITS ins=$INS outs=$OUTS start=$AUTOSTART access=$ACCESSMODE $ACTION > stdout &) &

### Finally, run the network-monitor.sh script to keep WiFi connection alive
(scripts/network-monitor.sh > netstatus &) &
