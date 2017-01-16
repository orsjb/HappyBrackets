#!/bin/bash

# Builds the deployment zips and uploads them.
# Before you run this you will need to run ‘gradle deploy’ from within the main HappyBrackets dev project, and also rebuilt the IntelliJ plugin and move it into the distribution folder (or re-run ‘gradle deploy’ which will do that).

# TODO: remove .DS_Store and other unwanted files.

zip -r HappyBracketsDeveloperKit.zip HappyBrackets\ Developer\ Kit/
cd HappyBrackets\ Developer\ Kit/Device/
zip -r ../../HappyBracketsDeviceRuntime.zip HappyBrackets/
cd ../../
scp *.zip happybrackets@happybrackets.net:happybrackets.net/downloads/
rm *.zip
cp -r HappyBrackets\ Developer\ Kit/HappyBrackets\ Project/libs/docs/hb/javadoc ./doc
scp -r doc happybrackets@happybrackets.net:happybrackets.net/
rm -r doc
