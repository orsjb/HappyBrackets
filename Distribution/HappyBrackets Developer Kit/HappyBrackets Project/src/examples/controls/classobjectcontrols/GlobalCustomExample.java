package examples.controls.classobjectcontrols;

import net.happybrackets.core.Device;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.*;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

/**
 * This example sends a custom class across the network.
 * Clicking the Change Values send button will populate the class with new values and send them as global messages
 * The class that is being sent must be visible and loadable in the receiving class
 * In this example it is done by putting it inside this class
 *
 * It is possible to cretae a standalone custom class as a message type, sending to startup, and then sending startup classes to device
 *
 */
public class GlobalCustomExample implements HBAction {
    // We are going to Send the SampleClassMessage as a message
    // The class must be visible within the device so either add it to the current sketch
    // Alternatively, you can create a separate class, send to startup and send classes to device
    class SampleClassMessage {
        public String deviceName = "";
        public int i_val = 0;
        public double d_val = 0;
    }

    @Override
    public void action(HB hb) {
        hb.reset(); //Clears any running code on the device
        //Write your sketch below

        // Cretae some controls to display the received values
        TextControl s_val = new TextControl(this, "S Val", "");
        IntegerControl i_val = new IntegerControl(this, "I Val", 0);
        FloatControl d_val = new FloatControl(this, "D Val", 0);


        // Create our Dynamic Control to send the class across network
        // Type classObjectControl to generate this code
        ClassObjectControl classSender = new ClassObjectControl(this, "Class Control", SampleClassMessage.class) {
            @Override
            public void valueChanged(Object object_val) {
                SampleClassMessage control_val = (SampleClassMessage) object_val;
                // Write your DynamicControl code below this line
                s_val.setValue(control_val.deviceName);
                i_val.setValue(control_val.i_val);
                d_val.setValue(control_val.d_val);
                // Write your DynamicControl code above this line
            }

        }.setControlScope(ControlScope.GLOBAL);// End DynamicControl classSender code


        TriggerControl changeValues = new TriggerControl(this, "Change Values") {
            @Override
            public void triggerEvent() {// Write your DynamicControl code below this line 
                SampleClassMessage sampleClassMessage = new SampleClassMessage();
                sampleClassMessage.deviceName = Device.getDeviceName();
                sampleClassMessage.i_val = (int) (Math.random() * 1000);
                sampleClassMessage.d_val = Math.random();

                classSender.setValue(sampleClassMessage);

                // Write your DynamicControl code above this line 
            }
        };// End DynamicControl changeValues code 

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
