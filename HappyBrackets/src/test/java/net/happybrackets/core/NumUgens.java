package net.happybrackets.core;

import net.beadsproject.beads.data.Buffer;
import net.happybrackets.core.control.TriggerControl;
import net.happybrackets.core.instruments.SampleModule;
import net.happybrackets.core.instruments.WaveModule;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

public class NumUgens implements HBAction {
    @Override
    public void action(HB hb) {
        hb.reset(); //Clears any running code on the device

         new TriggerControl(this, "Send Num Connected Ugens") {
            @Override
            public void triggerEvent() {// Write your DynamicControl code below this line
                int num_ugens =  HB.getAudioOutput().getNumberOfConnectedUGens(0);

                hb.setStatus("Num Ugens " + num_ugens);
                // Write your DynamicControl code above this line 
            }
        };// End DynamicControl triggerControl code 

        // type basicWavePlayer to generate this code
        WaveModule waveModule = new WaveModule();
        waveModule.setFrequency(1000);
        waveModule.setGain(0.1f);
        waveModule.setBuffer(Buffer.SINE);
        waveModule.connectTo(HB.getAudioOutput());


        // type basicSamplePLayer to generate this code
        // define our sample name
        final String s = "data/audio/Roje/i-write.wav";
        SampleModule sampleModule = new SampleModule();
        if (sampleModule.setSample(s)) {// Write your code below this line
            sampleModule.connectTo(HB.getAudioOutput());

            // Write your code above this line
        } else {
            HB.HBInstance.setStatus("Failed sample " + s);
        }// End samplePlayer code
        TriggerControl kilWave = new TriggerControl(this, "Kill Wave") {
            @Override
            public void triggerEvent() {// Write your DynamicControl code below this line 
                if (waveModule != null){
                    waveModule.kill();
                    sampleModule.kill();

                }
                // Write your DynamicControl code above this line 
            }
        };// End DynamicControl kilWave code 

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
