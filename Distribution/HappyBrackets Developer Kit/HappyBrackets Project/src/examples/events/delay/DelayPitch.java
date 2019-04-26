package examples.events.delay;

import net.beadsproject.beads.data.Buffer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.HBReset;
import net.happybrackets.core.instruments.WaveModule;
import net.happybrackets.core.scheduling.Clock;
import net.happybrackets.core.scheduling.Delay;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;
/*
This sketch plays 400Khz Tone with a clock that changes its pitch to 1Khz for 300ms
We will use a delay Object to triger a return to original pitch
 */
public class DelayPitch implements HBAction, HBReset {
    // Change to the number of audio Channels on your device
    final int NUMBER_AUDIO_CHANNELS = 1;

    int ORIGINAL_PITCH = 400;
    int HIGH_PITCH = 1000;
    double HOLD_TIME = 300;


    // This variable will become true when the composition is reset
    boolean compositionReset = false;

    @Override
    public void action(HB hb) {
        /***** Type your HBAction code below this line ******/
        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");

        WaveModule waveModule = new WaveModule();
        waveModule.setFrequency(ORIGINAL_PITCH);
        waveModule.setGain(0.1f);
        waveModule.setBuffer(Buffer.SINE);
        waveModule.connectTo(hb.ac.out);

        /* To create this, just type clockTimer */
        Clock clock = hb.createClock(500).addClockTickListener((offset, this_clock) -> {/* Write your code below this line */
            waveModule.setFrequency(HIGH_PITCH);

            // now create the delay to switch back to original pitch
            new Delay(HOLD_TIME, waveModule, (v, o) -> {
                // v is how far out we were from our exact delay time in ms and is a double
                // o is the parameter we passed in, which was the waveplayer
                System.out.println("Delay offset by " + v + " ms");
                ((WaveModule)o).setFrequency(ORIGINAL_PITCH);
            });

            /* Write your code above this line */
        });

        clock.start();/* End Clock Timer */
        


        /***** Type your HBAction code above this line ******/
    }


    /**
     * Add any code you need to have occur when a reset occurs
     */
    @Override
    public void doReset() {
        compositionReset = true;
        /***** Type your HBReset code below this line ******/

        /***** Type your HBReset code above this line ******/
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
