#!/bin/bash

### Script to Create a default config for device HappyBrackets on device


#if we want to set sepcific parameters for a device, place them in $HOSTNAME.config
CONFIG_FILE=$HOSTNAME.config


#define default values

cat > $CONFIG_FILE<<EOL
BUF=1024
SR=44100
BITS=16
INS=0
OUTS=1 
DEVICE=0
AUTOSTART=true 
ACCESSMODE=open
CONFIG=device-config.json
MUTE=27
EOL
