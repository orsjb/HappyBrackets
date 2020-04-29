#!/bin/bash

### Script to Create a default config for device HappyBrackets on device


#if we want to set sepcific parameters for a device, place them in $HOSTNAME.config
CONFIG_FILE="../config/"$HOSTNAME.config


#define default values

cat > $CONFIG_FILE<<EOL
BUF=1024
SR=44100
BITS=16
INS=0
OUTS=2
DEVICE=0
USE_BEADS_AUDIO=true 
ACCESSMODE=open
MUTE=27
EOL
