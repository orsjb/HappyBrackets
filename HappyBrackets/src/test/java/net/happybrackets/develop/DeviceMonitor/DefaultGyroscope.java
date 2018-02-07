package net.happybrackets.develop.DeviceMonitor;

import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.Gyroscope;
import net.happybrackets.device.sensors.SensorNotFoundException;

import java.lang.invoke.MethodHandles;

public class DefaultGyroscope implements HBAction{

    public static void main(String[] args) {

        try {
            HB.runDebug(MethodHandles.lookup().lookupClass());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void action(HB hb) {

        /*************************************************************
         * Create a Float type Dynamic Control pair that displays as a slider and text box
         *
         * Simply type floatBuddyControl to generate this code
         *************************************************************/
        DynamicControl displayYaw = hb.createControlBuddyPair(this, ControlType.FLOAT, "Yaw", 0, -1, 1);
        // Listener removed as it is uneccesary
        /*** End DynamicControl code ***/

        /*************************************************************
         * Create a Float type Dynamic Control pair that displays as a slider and text box
         *
         * Simply type floatBuddyControl to generate this code
         *************************************************************/
        DynamicControl displayPitch = hb.createControlBuddyPair(this, ControlType.FLOAT, "Pitch", 0, -1, 1);
        // Listener removed as it is uneccesary
        /*** End DynamicControl code ***/

        /*************************************************************
         * Create a Float type Dynamic Control pair that displays as a slider and text box
         *
         * Simply type floatBuddyControl to generate this code
         *************************************************************/
        DynamicControl displayRoll = hb.createControlBuddyPair(this, ControlType.FLOAT, "Roll", 0, -1, 1);
        // Listener removed as it is uneccesary
        /*** End DynamicControl code ***/

        try {
            Gyroscope sensor = (Gyroscope)hb.findSensor(Gyroscope.class);
            sensor.setRounding(3);
            sensor.addListener(() -> {
                System.out.println(sensor.getPitch());
                System.out.println(sensor.getRoll());
                System.out.println(sensor.getYaw());

                displayYaw.setValue(sensor.getYaw());
                displayRoll.setValue(sensor.getRoll());
                displayPitch.setValue(sensor.getPitch());
            });
        } catch (SensorNotFoundException e) {
            e.printStackTrace();
        }
    }
}
