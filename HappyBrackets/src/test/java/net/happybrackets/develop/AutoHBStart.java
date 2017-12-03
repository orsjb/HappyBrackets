package net.happybrackets.develop;


import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;


import java.lang.invoke.MethodHandles;


public class AutoHBStart implements HBAction{

    public static void main(String[] args) {

        try {
            HB.runDebug(MethodHandles.lookup().lookupClass());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void action(HB hb) {

        System.out.println("Hello World! We are running HB Action.");
        //hb.createDynamicControl(ControlType.FLOAT, "Test");
    }


}
