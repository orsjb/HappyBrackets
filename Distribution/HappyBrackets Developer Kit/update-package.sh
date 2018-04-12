#this script will read the files from Github and write them into Distribution Kit

#make a temp dir to download files
mkdir temp
cd temp

# get files from Github
svn checkout https://github.com/orsjb/HappyBrackets/trunk/Distribution/HappyBrackets%20Developer%20Kit

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

#now get out and delete our temp dir
cd ../..
rm -r -f temp

