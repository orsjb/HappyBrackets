cd ..\

echo "Running HappyBrackets"

java -cp  "data/classes;HB.jar;data/jars/*"  net.happybrackets.device.DeviceMain buf=1024 sr=44100 bits=16 ins=0 outs=2 start=true access=open simulate=true

pause
