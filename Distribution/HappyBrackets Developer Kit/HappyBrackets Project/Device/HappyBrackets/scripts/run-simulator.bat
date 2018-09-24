cd ..\

echo "Running HappyBrackets"

java -cp  "data/classes;HB.jar;data/jars/*"  net.happybrackets.device.DeviceMain buf=1024 sr=44100 bits=16 ins=0 outs=1 start=true access=open simulate=true > ramfs/stdout 2>&1

exit
