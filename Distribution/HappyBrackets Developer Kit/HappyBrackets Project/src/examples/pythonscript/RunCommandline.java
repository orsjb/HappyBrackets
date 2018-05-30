package examples.pythonscript;

import net.happybrackets.core.HBAction;
import net.happybrackets.core.ShellExecute;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.device.HB;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

/**
 * This composition runs a python script using a commandline
 * When the process starts, the text box will display running.
 * When the process completes the exit status will be displayed
 * If the Kill Process trigger control is pressed before the process completes,
 * the error status is displayed
 */
public class RunCommandline implements HBAction {
    // Change to the number of audio Channels on your device
    final int NUMBER_AUDIO_CHANNELS = 1;

    @Override
    public void action(HB hb) {
        /***** Type your HBAction code below this line ******/
        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");

        final String PROGRAM_NAME = "python";
        final String SCRIPT_NAME = "data/scripts/hellopython.py";

        /*************************************************************
         * Create a string type Dynamic Control that displays as a text box
         *
         * Simply type textControl to generate this code
         *************************************************************/
        DynamicControl text_display = hb.createDynamicControl(this, ControlType.TEXT, "Program State", "")
                .addControlListener(control -> {
                    String control_val = (String) control.getValue();

                    /*** Write your DynamicControl code below this line ***/

                    /*** Write your DynamicControl code above this line ***/
                });
        /*** End DynamicControl text_display code ***/

        /**************************************************
         * Create an executor that uses commandline
         **************************************************/
        ShellExecute executor = new ShellExecute().addProcessCompleteListener((shellExecute, exit_value) -> {
            System.out.println("****************************************");
            System.out.println("Commandline");
            System.out.println("****************************************");
            System.out.println("Text: " +  shellExecute.getProcessText());
            System.out.println("Error: " +  shellExecute.getErrorText());
            System.out.println("Exit status: " + exit_value);
            System.out.println("****************************************");
            text_display.setValue("Complete -  status " + exit_value);
        });

        try {
            System.out.println("About to start executing command " + PROGRAM_NAME +" " + SCRIPT_NAME);
            executor.executeCommand(PROGRAM_NAME +" " + SCRIPT_NAME);
            text_display.setValue("Running ");
        } catch (IOException e) {
            e.printStackTrace();
            text_display.setValue("Failed " + e.getMessage());
        }


        /*************************************************************
         * Create a Trigger type Dynamic Control that displays as a button
         *
         * Simply type triggerControl to generate this code
         *************************************************************/
        DynamicControl killTrigger = hb.createDynamicControl(this, ControlType.TRIGGER, "Kill Process")
                .addControlListener(control -> {

                    /*** Write your DynamicControl code below this line ***/
                    executor.killProcess();
                    /*** Write your DynamicControl code above this line ***/
                });
        /*** End DynamicControl code ***/

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
