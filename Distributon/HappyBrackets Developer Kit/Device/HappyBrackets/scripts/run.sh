#!/bin/bash

### Script to autorun HappyBrackets on device

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
ACTION=

echo “Running HappyBrackets”

(/usr/bin/sudo /usr/bin/java -cp data/classes -Xmx512m -jar HB.jar buf=$BUF sr=$SR bits=$BITS ins=$INS outs=$OUTS start=$AUTOSTART $ACTION > stdout &) &

### Finally, run the network-monitor.sh script to keep WiFi connection alive
(scripts/network-monitor.sh > netstatus &) &