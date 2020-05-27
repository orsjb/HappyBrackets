package examples.events.scheduled;

import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.*;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

/**
 * This example will make all other devices running the sketch to follow the buddy value by one second
 */
public class FollowInts implements HBAction {

    final int DELAY_TIME = 1000; // The amount we will make other devices delay

    @Override
    public void action(HB hb) {
        hb.reset(); //Clears any running code on the device
        //Write your sketch below
        TriggerControl setClocks = new TriggerControl(this, "Reset Scheduler") {
            @Override
            public void triggerEvent() {// Write your DynamicControl code below this line
                // this will set all clocks to zero
                HB.sendScheduleSetTime(0, null);
                // Write your DynamicControl code above this line
            }
        };// End DynamicControl setClocks code


        // Type intBuddyControl to generate this code 
        IntegerControl globalBuddy = new IntegerControl(this, "Global Buddy", 0) {
            @Override
            public void valueChanged(int control_val) {// Write your DynamicControl code below this line 
                hb.setStatus("" + control_val);
                // Write your DynamicControl code above this line 
            }
        }.setDisplayRange(0, 100, DynamicControl.DISPLAY_TYPE.DISPLAY_ENABLED_BUDDY).setControlScope(ControlScope.GLOBAL);// End DynamicControl globalBuddy code

        IntegerControl leadControl = new IntegerControl(this, "Lead Control", 0) {
            @Override
            public void valueChanged(int control_val) {// Write your DynamicControl code below this line

                double scheduled_time = HB.getSchedulerTime() + DELAY_TIME;

                // tell the target control to follow this value
                globalBuddy.setValue(control_val, scheduled_time);
                // Write your DynamicControl code above this line
            }
        }.setDisplayRange(0, 100, DynamicControl.DISPLAY_TYPE.DISPLAY_ENABLED_BUDDY);// End DynamicControl leadControl code
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
