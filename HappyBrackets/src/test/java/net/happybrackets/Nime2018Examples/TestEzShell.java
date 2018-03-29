package net.happybrackets.Nime2018Examples;

import net.happybrackets.core.EZShell;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

public class TestEzShell implements HBAction {
    @Override
    public void action(HB hb) {

        EZShell.call("python", "data/hello.py");

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
