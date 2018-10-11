package net.happybrackets.develop.DynamicControls;

import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.BooleanControl;
import net.happybrackets.core.control.ControlScope;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.core.scheduling.HBScheduler;
import net.happybrackets.core.scheduling.Scheduling;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

public class ScheduledControl implements HBAction {
    // Change to the number of audio Channels on your device
    final int NUMBER_AUDIO_CHANNELS = 1;

    @Override
    public void action(HB hb) {
        /***** Type your HBAction code below this line ******/
        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");


        /*************************************************************
         * Create a Boolean type Dynamic Control that displays as a check box
         * Simply type booleanControl to generate this code
         *************************************************************/
        BooleanControl readBooleanControl = new BooleanControl(this, "Read", false) {
            @Override
            public void valueChanged(Boolean new_value) {
                /*** Write your DynamicControl code below this line ***/
                System.out.println("Read " + new_value);
                /*** Write your DynamicControl code above this line ***/
            }
        };/*** End DynamicControl readBooleanControl code ***/


        DynamicControl button_control = hb.createDynamicControl(this, ControlType.TRIGGER, "Scheduled", 0);
        button_control.addControlListener(control -> {
            System.out.println("Scheduled Trigger");
        });

        DynamicControl check_control =  hb.createDynamicControl(this, ControlType.BOOLEAN, "Delayed", false);
        check_control.addControlListener(control -> {
            System.out.println("delayed Check " +  control.getValue());
        });

        /*************************************************************
         * Create a Boolean type Dynamic Control that displays as a check box
         * Simply type booleanControl to generate this code
         *************************************************************/
        BooleanControl setBooleanControl = new BooleanControl(this, "Set", false) {
            @Override
            public void valueChanged(Boolean new_value) {
                /*** Write your DynamicControl code below this line ***/
                System.out.println("Set " + new_value);
                // add 10 seconds
                double scheduled_time = HBScheduler.getGlobalScheduler().getSchedulerTime() + 10000;
                button_control.setValue(null, (long)scheduled_time);
                check_control.setValue(new_value, (long)scheduled_time);

                readBooleanControl.setValue(new_value);
                /*** Write your DynamicControl code above this line ***/
            }
        };/*** End DynamicControl setBooleanControl code ***/



        /*************************************************************
         * Create a Boolean type Dynamic Control pair that displays as a check box
         * Simply type globalBooleanControl to generate this code
         *************************************************************/
        BooleanControl booleanControl = new BooleanControl(this, "control name", false) {
            @Override
            public void valueChanged(Boolean new_value) {
                /*** Write your DynamicControl code below this line ***/

                /*** Write your DynamicControl code above this line ***/
            }
        };
        booleanControl.setControlScope(ControlScope.GLOBAL);
        /*** End DynamicControl booleanControl code ***/


        /*************************************************************
         * Create a Boolean type Dynamic Control pair that displays as a check box
         * Simply type globalBooleanControl to generate this code
         *************************************************************/
        BooleanControl global1 = new BooleanControl(this, "control name", false) {
            @Override
            public void valueChanged(Boolean new_value) {
                /*** Write your DynamicControl code below this line ***/

                /*** Write your DynamicControl code above this line ***/
            }
        };
        global1.setControlScope(ControlScope.GLOBAL);
        /*** End DynamicControl global1 code ***/

        /*************************************************************
         * Create a Boolean type Dynamic Control pair that displays as a check box
         * Simply type globalBooleanControl to generate this code
         *************************************************************/
        BooleanControl global2 = new BooleanControl(this, "control name", false) {
            @Override
            public void valueChanged(Boolean new_value) {
                /*** Write your DynamicControl code below this line ***/

                /*** Write your DynamicControl code above this line ***/
            }
        };
        global2.setControlScope(ControlScope.GLOBAL);
        /*** End DynamicControl global2 code ***/

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
