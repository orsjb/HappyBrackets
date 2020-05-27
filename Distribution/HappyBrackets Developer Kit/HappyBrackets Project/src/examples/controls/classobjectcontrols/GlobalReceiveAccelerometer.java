package examples.controls.classobjectcontrols;

import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.*;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

/**
 * This class will display the values received from GlobalSendAccelerometer example
 */
public class GlobalReceiveAccelerometer implements HBAction {
    final String TRIPLE_AXIS_NAME = "Triple Axis Values";

    @Override
    public void action(HB hb) {
        hb.reset(); //Clears any running code on the device
        //Write your sketch below

        FloatControl x_value = new FloatControl(this, "X value", 0);

        FloatControl y_value = new FloatControl(this, "Y value", 0);


        FloatControl z_value = new FloatControl(this, "Z Value", 0);

        // Type classObjectControl to generate this code
        ClassObjectControl objectControlReceiver = new ClassObjectControl(this, TRIPLE_AXIS_NAME, TripleAxisMessage.class) {
            @Override
            public void valueChanged(Object object_val) {
                TripleAxisMessage control_val = (TripleAxisMessage) object_val;
                // Write your DynamicControl code below this line

                x_value.setValue(control_val.getX());
                y_value.setValue(control_val.getY());
                z_value.setValue(control_val.getZ());
                // Write your DynamicControl code above this line
            }

        }.setControlScope(ControlScope.GLOBAL);// End DynamicControl objectControlReceiver code

        // write your code above this line
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
