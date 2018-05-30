package examples.sensor.gyroscope;

import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
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
        /*************************************************************
         * Create a Float type Dynamic Control pair that displays as a slider and text box
         *
         * Simply type floatBuddyControl to generate this code
         *************************************************************/
        DynamicControl displayYaw = hb.createControlBuddyPair(this, ControlType.FLOAT, "Yaw", 0, -1, 1);
        // Listener removed as it is unnecessary
        /*** End DynamicControl code ***/

        /*************************************************************
         * Create a Float type Dynamic Control pair that displays as a slider and text box
         *
         * Simply type floatBuddyControl to generate this code
         *************************************************************/
        DynamicControl displayPitch = hb.createControlBuddyPair(this, ControlType.FLOAT, "Pitch", 0, -1, 1);
        // Listener removed as it is unnecessary
        /*** End DynamicControl code ***/

        /*************************************************************
         * Create a Float type Dynamic Control pair that displays as a slider and text box
         *
         * Simply type floatBuddyControl to generate this code
         *************************************************************/
        DynamicControl displayRoll = hb.createControlBuddyPair(this, ControlType.FLOAT, "Roll", 0, -1, 1);
        // Listener removed as it is unnecessary
        /*** End DynamicControl code ***/


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
