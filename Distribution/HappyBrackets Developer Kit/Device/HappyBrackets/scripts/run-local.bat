cd ..\

echo "Running HappyBrackets"

java -jar HB.jar net.happybrackets.device.DeviceMain buf=1024 sr=44100 bits=16 ins=0 outs=1 start=true access=local simulate=true

pause