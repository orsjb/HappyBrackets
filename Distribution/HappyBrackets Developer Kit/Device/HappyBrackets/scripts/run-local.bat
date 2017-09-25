cd ..\

BUF=1024
SR=44100
BITS=16
INS=0
OUTS=1 
AUTOSTART=true 
ACCESSMODE=local
ACTION=

echo "Running HappyBrackets"

java -jar HB.jar net.happybrackets.device.DeviceMain buf=1024 sr=44100 bits=16 ins=0 outs=1 start=true access=local

pause