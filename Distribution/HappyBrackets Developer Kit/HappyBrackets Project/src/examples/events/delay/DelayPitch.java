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
        waveModule.connectTo(HB.getAudioOutput());

        /* To create this, just type clockTimer */
        Clock clock = hb.createClock(500).addClockTickListener((offset, this_clock) -> {/* Write your code below this line */
            waveModule.setFrequency(HIGH_PITCH);

            // now create the delay to switch back to original pitch


            // type delayline to create this code 
            new Delay(HOLD_TIME, waveModule, (delay_offset, param) -> {
                // delay_offset is how far out we were from our exact delay time in ms and is a double
                // param is the parameter we passed in type your code below this line
                System.out.println("Delay offset by " + delay_offset + " ms");
                ((WaveModule)param).setFrequency(ORIGINAL_PITCH);
                // type your code above this line
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
