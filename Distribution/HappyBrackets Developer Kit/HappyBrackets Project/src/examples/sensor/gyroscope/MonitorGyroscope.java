package examples.sensor.gyroscope;

import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.core.control.FloatBuddyControl;
import net.happybrackets.core.control.FloatControl;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.GyroscopeListener;


import java.lang.invoke.MethodHandles;

/**
 * * This sketch displays the values returning from gyroscope
 */
public class MonitorGyroscope implements HBAction {
    @Override
    public void action(HB hb) {

        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");

        /***** Type your HBAction code below this line ******/


        // Simply type floatBuddyControl to generate this code
        FloatControl displayYaw = new FloatBuddyControl(this, "Yaw Monitor", 0, -1, 1) {
            @Override
            public void valueChanged(double control_val) {// Write your DynamicControl code below this line

                // Write your DynamicControl code above this line
            }
        };// End DynamicControl displayX code


        // Simply type floatBuddyControl to generate this code
        FloatControl displayPitch = new FloatBuddyControl(this, "Pitch Monitor", 0, -1, 1) {
            @Override
            public void valueChanged(double control_val) {// Write your DynamicControl code below this line

                // Write your DynamicControl code above this line
            }
        };// End DynamicControl displayY code


        // Simply type floatBuddyControl to generate this code
        FloatControl displayRoll = new FloatBuddyControl(this, "Roll Monitor", 0, -1, 1) {
            @Override
            public void valueChanged(double control_val) {// Write your DynamicControl code below this line

                // Write your DynamicControl code above this line
            }
        };// End DynamicControl displayZ code




        /*****************************************************
         * Add a gyroscope sensor listener. *
         * to create this code, simply type gyroscopeSensor
         *****************************************************/
        new GyroscopeListener(hb) {
            @Override
            public void sensorUpdated(float pitch, float roll, float yaw) {
                /******** Write your code below this line ********/
                displayPitch.setValue(pitch);

                displayRoll.setValue(roll);
                displayYaw.setValue(yaw);
                /******** Write your code above this line ********/
            }
        };
        /*** End gyroscopeSensor code ***/

        /***** Type your HBAction code above this line ******/
    }

    //<editor-fold defaultstate="collapsed" desc="Debug Start">
    /**
     * This function is used when running sketch in IntelliJ IDE for debugging or testing
     *
     * @param args standard args required
     */
    public static void main(String[] args) {

        try {
            HB.runDebug(MethodHandles.lookup().lookupClass());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //</editor-fold>
}
