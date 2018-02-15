package net.happybrackets.Nime2018Examples;

import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;

public class TestPython implements HBAction {
    @Override
    public void action(HB hb) {



        hb.setStatus("Try to load");

        String output = "Start";
        try {
            output = execCmd(("python /home/pi/hello.py"));
            hb.setStatus(output);

            hb.setStatus("Complete");
        } catch (IOException e) {
            e.printStackTrace();
            hb.setStatus(e.getMessage());
        }



    }

    public static String execCmd(String cmd) throws java.io.IOException {
        Process proc = Runtime.getRuntime().exec(cmd);

        /***********************************************************
         * Create a runnable thread object
         * simply type threadFunction to generate this code
         ***********************************************************/
        Thread thread = new Thread(() -> {
            int SLEEP_TIME = 10000;
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {

            }

            while (true) {
                /*** write your code below this line ***/


                proc.destroy();
                /*** write your code above this line ***/

                try {
                    Thread.sleep(SLEEP_TIME);
                } catch (InterruptedException e) {
                    /*** remove the break below to just resume thread or add your own action***/
                    break;
                    /*** remove the break above to just resume thread or add your own action ***/

                }
            }
        });

        /*** write your code you want to execute before you start the thread below this line ***/

        /*** write your code you want to execute before you start the thread above this line ***/

        thread.start();
        /****************** End threadFunction **************************/
        java.io.InputStream is = proc.getInputStream();
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");

        String val = "";
        if (s.hasNext()) {
            val = s.next();
            System.out.println(val);

        }
        else {
            val = "";
        }
        return val;
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
