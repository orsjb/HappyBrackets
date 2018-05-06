#!/bin/sh
#this script will read the zip file from latest version and write them into Distribution Kit

cd "$(dirname "$0")"

#make a temp dir to download files
mkdir temp
cd temp

# get latest filename
FILENAME=$(curl http://www.happybrackets.net/downloads/happy-brackets-sdk.php?version)

echo $FILENAME

curl -O http://www.happybrackets.net/downloads/$FILENAME

unzip $FILENAME



#move into the downloaded repository
cd "HappyBrackets Developer Kit"

ls

#copy device files
cp -R Device ../../

#make our examples read/write so we can overwrite them
chmod -R a+w ../../HappyBrackets\ Project/src/examples

#copy Developer Kit
cp -R HappyBrackets\ Project ../../

#make our examples read only again
chmod -R a-w ../../HappyBrackets\ Project/src/examples

#copy our Plugin
cp HappyBrackets_IntelliJ_Plugin.zip ../../HappyBrackets_IntelliJ_Plugin.zip

cp *.sh ../../

#now get out and delete our temp dir
chmod -R a+w HappyBrackets\ Project/src/examples
cd ../..
rm -r -f temp

NOTICE="You need to update your HappyBrackets_IntelliJ_Plugin.zip to the one in "
echo $NOTICE $(pwd)
