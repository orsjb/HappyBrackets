package examples.events.scheduled;

import net.beadsproject.beads.data.Buffer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.instruments.WaveModule;
import net.happybrackets.core.scheduling.Delay;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

/**
 * This example will show two usages of Sending at a time with no parameter
 * as well as sending a parameter
 *
 */
public class RunAtTime implements HBAction {
    @Override
    public void action(HB hb) {
        hb.reset(); //Clears any running code on the device
        //Write your sketch below


        WaveModule waveModule = new WaveModule();
        waveModule.setFrequency(1000);
        waveModule.setGain(0.1f);
        waveModule.setBuffer(Buffer.SINE);
        waveModule.connectTo(HB.getAudioOutput());


        // Use no parameter
        hb.doAtTime(hb.getSynchTime() + 1000, (v, o) -> {
            double variation =  v; // This is how far off the scheduled time we are

            hb.setStatus("Variation of " + variation);
            waveModule.setFrequency(500);
        });


        // Second will pass The WaveModule as a value to the do at time
        hb.doAtTime(hb.getSynchTime() + 2000, waveModule, (v, o) ->
        {
            double variation =  v; // This is how far off the scheduled time we are
            hb.setStatus("Variation of " + variation);
            ((WaveModule) o).setFrequency(1000);
        });

        hb.setStatus("Wait");
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
