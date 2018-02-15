package net.happybrackets.Nime2018Examples;

import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.device.HB;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;

public class TestPython implements HBAction {
    Process shellProcess = null;

    @Override
    public void action(HB hb) {



        hb.setStatus("Try to load");

        String output = "Start";
        try {
            output = execCmd(("python /Users/angelo/hello.py"));
            hb.setStatus(output);

            hb.setStatus("Complete");
        } catch (IOException e) {
            e.printStackTrace();
            hb.setStatus(e.getMessage());
        }


        /*************************************************************
         * Create a Trigger type Dynamic Control that displays as a button
         *
         * Simply type triggerControl to generate this code
         *************************************************************/
        DynamicControl triggerControl = hb.createDynamicControl(this, ControlType.TRIGGER, "killprocess")
                .addControlListener(control -> {

                    /*** Write your DynamicControl code below this line ***/

                    if (shellProcess != null)
                    {
                        shellProcess.destroy();
                        shellProcess = null;
                    }
                    /*** Write your DynamicControl code above this line ***/
                });
        /*** End DynamicControl code ***/

    }

    String execCmd(String cmd) throws java.io.IOException {
        shellProcess = Runtime.getRuntime().exec(cmd);

        /***********************************************************
         * Create a runnable thread object
         * simply type threadFunction to generate this code
         ***********************************************************/
        Thread thread = new Thread(() -> {

            java.io.InputStream is = shellProcess.getInputStream();
            java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");

            String val = "";
            if (s.hasNext()) {
                val = s.next();
                System.out.println(val);

            }
            else {
                val = "";
                System.out.println();
            }

            System.out.println("End Read");
        });

        /*** write your code you want to execute before you start the thread below this line ***/

        /*** write your code you want to execute before you start the thread above this line ***/

        thread.start();
        /****************** End threadFunction **************************/

        return "";
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
