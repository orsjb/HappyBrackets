#!/bin/bash

zip -r HappyBrackets\ Developer\ Kit.zip HappyBrackets\ Developer\ Kit/
cd HappyBrackets\ Developer\ Kit/Device/
zip -r ../../HappyBracketsDeviceRuntime.zip HappyBrackets/
cd ../../
scp *.zip happybrackets@wayne.dreamhost.com:happybrackets.net/downloads/
rm *.zip
