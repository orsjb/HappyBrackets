package examples.sensor.accelerometer;

import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.AccelerometerListener;

import java.lang.invoke.MethodHandles;

/**
 * This sketch displays the values returning from accelerometer
 */
public class MonitorAccelerometer implements HBAction {
    @Override
    public void action(HB hb) {
        /***** Type your HBAction code below this line ******/

        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");

        /*************************************************************
         * Create a Float type Dynamic Control pair that displays as a slider and text box
         *
         * Simply type floatBuddyControl to generate this code
         *************************************************************/
        DynamicControl displayX = hb.createControlBuddyPair(this, ControlType.FLOAT, "Accel X", 0, -1, 1);
        // Listener removed as it is unnecessary
        /*** End DynamicControl code ***/

        /*************************************************************
         * Create a Float type Dynamic Control pair that displays as a slider and text box
         *
         * Simply type floatBuddyControl to generate this code
         *************************************************************/
        DynamicControl displayY = hb.createControlBuddyPair(this, ControlType.FLOAT, "Accel Y", 0, -1, 1);
        // Listener removed as it is unnecessary
        /*** End DynamicControl code ***/

        /*************************************************************
         * Create a Float type Dynamic Control pair that displays as a slider and text box
         *
         * Simply type floatBuddyControl to generate this code
         *************************************************************/
        DynamicControl displayZ = hb.createControlBuddyPair(this, ControlType.FLOAT, "Accel Z", 0, -1, 1);
        // Listener removed as it is unnecessary
        /*** End DynamicControl code ***/

        /*****************************************************
         * Find an accelerometer sensor. If no sensor is found
         * you will receive a status message
         * accelerometer values typically range from -1 to + 1
         * to create this code, simply type accelerometerSensor
         *****************************************************/
        new AccelerometerListener(hb) {
            @Override
            public void sensorUpdated(float x_val, float y_val, float z_val) {
                /******** Write your code below this line ********/
                displayX.setValue(x_val);
                displayY.setValue(y_val);
                displayZ.setValue(z_val);
                /******** Write your code above this line ********/

            }
        };
        /*** End accelerometerSensor code ***/

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
