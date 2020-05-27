package examples.pythonscript;

import net.happybrackets.core.HBAction;
import net.happybrackets.core.ShellExecute;
import net.happybrackets.core.control.*;
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
    @Override
    public void action(HB hb) {
        /***** Type your HBAction code below this line ******/
        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");

        final String PROGRAM_NAME = "python";
        final String SCRIPT_NAME = "data/scripts/hellopython.py";

        // Type textControlSender to generate this code
        TextControl text_display = new TextControl(this, "Program State", "");



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

        // Type triggerControl to generate this code 
        TriggerControl killTrigger = new TriggerControl(this, "Kill Process") {
            @Override
            public void triggerEvent() {// Write your DynamicControl code below this line 
                executor.killProcess();
                // Write your DynamicControl code above this line 
            }
        };// End DynamicControl killTrigger code 


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
