package examples.controls.integerControl;

import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.core.control.FloatControl;
import net.happybrackets.core.control.IntegerControl;
import net.happybrackets.core.control.TriggerControl;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

/**
 * THis skecth will show you how to change from different integer control displays
 */
public class ChangeControlTypes implements HBAction {
    @Override
    public void action(HB hb) {
        hb.reset(); //Clears any running code on the device
        //Write your sketch below


        IntegerControl integerControl = new IntegerControl(this, "Int Control", 0){
            @Override
            public void valueChanged(int control_val) {/* Write your DynamicControl code below this line ***/
                hb.setStatus("Val " + control_val);
                /* Write your DynamicControl code above this line */
            }
        };


        TriggerControl setBuddy = new TriggerControl(this, "Set Buddy") {
            @Override
            public void triggerEvent() {// Write your DynamicControl code below this line
                integerControl.setDisplayRange(-10, 10, DynamicControl.DISPLAY_TYPE.DISPLAY_ENABLED_BUDDY);
                // Write your DynamicControl code above this line
            }
        };// End DynamicControl setBuddy code


        TriggerControl setSlider = new TriggerControl(this, "Set Slider") {
            @Override
            public void triggerEvent() {// Write your DynamicControl code below this line
                // to have a slider you must have the minimum less than maximum
                integerControl.setDisplayRange(-10, 10, DynamicControl.DISPLAY_TYPE.DISPLAY_DEFAULT);
                // Write your DynamicControl code above this line
            }
        };// End DynamicControl setSlider code


        TriggerControl setText = new TriggerControl(this, "Set Text") {
            @Override
            public void triggerEvent() {// Write your DynamicControl code below this line
                // if you set the min and max to zero, it will not show a slider
                integerControl.setDisplayRange(0, 0, DynamicControl.DISPLAY_TYPE.DISPLAY_DEFAULT);
                // Write your DynamicControl code above this line
            }
        };// End DynamicControl setText code


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
