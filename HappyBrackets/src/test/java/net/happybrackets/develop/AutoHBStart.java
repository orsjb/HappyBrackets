package net.happybrackets.develop;


import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;


import java.lang.invoke.MethodHandles;

/**
 * Class demonstrates how to start HB in the debugger
 * Either run AutoHBStart.main() or debug AutoHBStart.main() from context menu
 */
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
        WavePlayer wp = new WavePlayer(hb.ac, 1000f, Buffer.SINE);
        Gain g = new Gain(hb.ac, 1);
        g.addInput(wp);
        hb.ac.out.addInput(g);
        int num_outputs = hb.ac.out.getOuts();
        System.out.println(num_outputs + " outputs");

    }


}
