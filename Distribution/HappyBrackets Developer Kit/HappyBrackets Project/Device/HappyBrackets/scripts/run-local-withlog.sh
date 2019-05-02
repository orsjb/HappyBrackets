#!/bin/bash

### Script to autorun HappyBrackets on a local computer (i.e., typically your mac or PC where you are running IntelliJ rather than your remote device such as a Raspberry Pi).
### This script is similar to run.sh but does run the auto-rename nor the network monitor, and defaults to running HappyBrackets in 'local' access mode.

### move to the correct dir for running this script (one level above where this script is)
DIR=`dirname $0`
cd ${DIR}/..

### run HappyBrackets
### args to HB.jar are: buf (buffer size, default=1024), sr (sample rate, default=44100), bits (sample bit size, default=16), ins (input channels, default=0), outs (output channels, default=1), start (autostart audio, default=true), access (live code access mode, either ‘open’, ‘local’ or ‘closed’, default=open), followed by the full class path to any HBAction you wish to auto run. All args except the last one can be entered in any order.

BUF=1024
SR=44100
BITS=16
INS=0
OUTS=2
AUTOSTART=true 
ACCESSMODE=local
ACTION=
SIMULATE=true

echo “Running HappyBrackets and logging to stdout”

java -cp data/classes:HB.jar -Xmx512m net.happybrackets.device.DeviceMain buf=$BUF sr=$SR bits=$BITS ins=$INS outs=$OUTS start=$AUTOSTART access=$ACCESSMODE $ACTION simulate=$SIMULATE > ramfs/stdout 2>&1
