package examples.basic;

import net.beadsproject.beads.data.Buffer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.core.control.IntegerControl;
import net.happybrackets.core.instruments.WaveModule;
import net.happybrackets.core.scheduling.Clock;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

/**
 * Demonstrates how Polylimited outputs cut earlier sounds off as the
 * number of outputs exceed the maximum number of inputs our PolyLimit has
 */
public class PolyLimitedOutput implements HBAction {
    @Override
    public void action(HB hb) {
        hb.reset(); //Clears any running code on the device
        //Write your sketch below

        // Let us display the size of the PolyLimiter
        int poly_size = HB.getPolyLimitedOutput().getMaxInputs();
        hb.setStatus("Poly " + poly_size);

        // To create this, just type clockTimer
        Clock clock = HB.createClock(500).addClockTickListener((offset, this_clock) -> {// Write your code below this line
            // Create a random frequency and add to poly limited output
            WaveModule waveModule = new WaveModule();
            float freq = hb.rng.nextFloat() * 500 + 500;
            waveModule.setFrequency(freq);

            waveModule.connectTo(HB.getPolyLimitedOutput());
            // Write your code above this line
        });

        clock.start();// End Clock Timer

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
