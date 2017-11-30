package net.happybrackets.develop;

import net.happybrackets.core.AudioSetup;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

/*
BUF=1024
SR=44100
BITS=16
INS=0
OUTS=1
AUTOSTART=true
ACCESSMODE=local
ACTION=

echo “Running HappyBrackets”

java -cp data/classes:HB.jar -Xmx512m net.happybrackets.device.DeviceMain

buf=1024
sr=44100
bits=16
ins=0
outs=1
start=true access=local

 */

public class AutoHBStart implements HBAction{

    public static void main(String[] args) {
        //set up the AudioContext and start it
        AutoHBStart autoClass = new AutoHBStart();

        HB hb = null;
        try {

            hb = HB.runDebug(MethodHandles.lookup().lookupClass());

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public void action(HB hb) {

        System.out.println("Hello World! We are running HB Action.");
    }
}
