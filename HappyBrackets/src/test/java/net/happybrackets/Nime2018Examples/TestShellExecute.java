package net.happybrackets.Nime2018Examples;

import net.happybrackets.core.HBAction;
import net.happybrackets.core.ShellExecute;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.device.HB;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;

public class TestShellExecute implements HBAction {
    @Override
    public void action(HB hb) {

        ShellExecute executer = new ShellExecute();
        executer.addProcessCompleteListener((shellExecute, exit_value) -> {
            System.out.println("Text: " +  shellExecute.getProcessText());
            System.out.println("Error: " +  shellExecute.getErrorText());
            System.out.println("Exit status: " + exit_value);

        });


        try {
            executer.executeCommand("python /Users/angelo/hello.py");
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*************************************************************
         * Create a Trigger type Dynamic Control that displays as a button
         *
         * Simply type triggerControl to generate this code
         *************************************************************/
        DynamicControl killTrigger = hb.createDynamicControl(this, ControlType.TRIGGER, "KillProcess")
                .addControlListener(control -> {

                    /*** Write your DynamicControl code below this line ***/

                    executer.killProcess();
                    /*** Write your DynamicControl code above this line ***/
                });
        /*** End DynamicControl code ***/
    }

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
}
