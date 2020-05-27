package examples.sensor.accelerometer;

import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.core.control.FloatControl;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.AccelerometerListener;

import java.lang.invoke.MethodHandles;
import java.text.DecimalFormat;

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

        // Simply type floatBuddyControl to generate this code
        FloatControl displayX = new FloatControl(this, "Accel X Monitor", 0) {
            @Override
            public void valueChanged(double control_val) {// Write your DynamicControl code below this line

                // Write your DynamicControl code above this line
            }
        }.setDisplayRange(-1, 1, DynamicControl.DISPLAY_TYPE.DISPLAY_ENABLED_BUDDY);// End DynamicControl displayX code


        // Simply type floatBuddyControl to generate this code
        FloatControl displayY = new FloatControl(this, "Accel Y Monitor", 0) {
            @Override
            public void valueChanged(double control_val) {// Write your DynamicControl code below this line

                // Write your DynamicControl code above this line
            }
        }.setDisplayRange(-1, 1, DynamicControl.DISPLAY_TYPE.DISPLAY_ENABLED_BUDDY);// End DynamicControl displayY code


        // Simply type floatBuddyControl to generate this code
        FloatControl displayZ = new FloatControl(this, "Accel Z Monitor", 0) {
            @Override
            public void valueChanged(double control_val) {// Write your DynamicControl code below this line

                // Write your DynamicControl code above this line
            }
        }.setDisplayRange(-1, 1, DynamicControl.DISPLAY_TYPE.DISPLAY_ENABLED_BUDDY);// End DynamicControl displayZ code

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


                // We will display our status as two decimal places
                String status_display = "\tx:"+ String.format("%.1g", x_val)
                        + "\ty:" + String.format("%.1g", y_val)
                        + "\tz:" + String.format("%.1g", z_val);

                hb.setStatus(status_display);
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
