package examples.controls.classobjectcontrols;

import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.ClassObjectControl;
import net.happybrackets.core.control.ControlScope;
import net.happybrackets.core.control.TripleAxisMessage;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.AccelerometerListener;

import java.lang.invoke.MethodHandles;

/**
 * This sketch will demonstrate sending scaled accelerometers values as global messages
 * accelerometer values will be converted to a tilt in degrees, where accelerometer value of 1 will be 90 degrees and -1 will be -90
 */
public class GlobalSendAccelerometer implements HBAction {

    // define the same name For our sender and receiver
    final String TRIPLE_AXIS_NAME = "Triple Axis Values";

    @Override
    public void action(HB hb) {
        hb.reset(); //Clears any running code on the device
        //Write your sketch below

        // Type classObjectControlSender to generate this code
        ClassObjectControl tripLeAxisSender = new ClassObjectControl(this, TRIPLE_AXIS_NAME, TripleAxisMessage.class).setControlScope(ControlScope.GLOBAL);

        // type accelerometerSensor to create this. Values typically range from -1 to + 1
        new AccelerometerListener(hb) {
            @Override
            public void sensorUpdated(float x_val, float y_val, float z_val) { // Write your code below this line

                TripleAxisMessage sendValueMessage = new TripleAxisMessage(convertAccelerometerTiltToDegrees(x_val),
                convertAccelerometerTiltToDegrees(y_val),
                convertAccelerometerTiltToDegrees(z_val));

                tripLeAxisSender.setValue(sendValueMessage);

                // Write your code above this line
            }
        };//  End accelerometerSensor

        // Type classObjectControl to generate this code
        ClassObjectControl objectControl = new ClassObjectControl(this, "controlName", TripleAxisMessage.class) {
            @Override
            public void valueChanged(Object object_val) {
                TripleAxisMessage control_val = (TripleAxisMessage) object_val;
                // Write your DynamicControl code below this line
                
                // Write your DynamicControl code above this line
            }

        };// End DynamicControl objectControl code


        // write your code above this line
    }

    /**
     * Convert accelerometer value ranging from -1 to 1 to -90 and + 90
     * @param acceleromter_value
     * @return range between -90 and +90 degrees
     */
    float convertAccelerometerTiltToDegrees (float acceleromter_value){
        // values must be between -1 and +1 to not cause an error
        if (acceleromter_value < -1.0f){
            acceleromter_value = -1.0f;
        }
        else if (acceleromter_value > 1.0f){
            acceleromter_value = 1.0f;
        }

        // convert to radians
        double radians = Math.asin(acceleromter_value);

        // now return as degrees
        return (float)Math.toDegrees(radians);

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
