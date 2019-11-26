package net.happybrackets.documentation;

import net.beadsproject.beads.data.Buffer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.*;
import net.happybrackets.core.instruments.SampleModule;
import net.happybrackets.core.instruments.WaveModule;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;
import java.net.InetAddress;

public class ControlScopeExample implements HBAction {
    @Override
    public void action(HB hb) {
        hb.reset(); //Clears any running code on the device


        IntegerControl control1 = new IntegerControlSender(this, "ControlName", 0);
        control1.setControlScope(ControlScope.TARGET);
        control1.addControlTarget("HB-123456", "HB-654321");
        control1.addControlTarget( "192.168.0.2");

        IntegerTextControl control2 = new IntegerTextControl(this, "ControlName", 0) {
            @Override
            public void valueChanged(int control_val) {// Write your DynamicControl code below this line
                System.out.println("Read " + control_val);
                // Write your DynamicControl code above this line
            }
        };// End DynamicControl control2 code
        control2.setControlScope(ControlScope.TARGET);

        control1.setValue(2); // This will not be seen by control2 unless control1 has this device as one of it's targets


        // This is on device "HB-654321"
        IntegerTextControl control3 = new IntegerTextControl(this, "ControlName", 0) {
            @Override
            public void valueChanged(int control_val) {// Write your DynamicControl code below this line
                System.out.println("Read " + control_val);
                // Write your DynamicControl code above this line
            }
        };// End DynamicControl receiver code
        control3.setControlScope(ControlScope.TARGET);


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
