package net.happybrackets.develop.instruments.sample;

import net.beadsproject.beads.ugens.*;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.HBReset;
import net.happybrackets.core.instruments.SampleModule;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.GyroscopeListener;

import java.lang.invoke.MethodHandles;

/**
 * This example just sees if I can connect up to a signal chain
 */
public class SampleEffects implements HBAction, HBReset {
    // Change to the number of audio Channels on your device


    // This variable will become true when the composition is reset
    boolean compositionReset = false;

    float filterFreqGlideTime = 800;

    float filterQVal = 2f;
    float filterGainVal = 2f;

    float hpfFreqVal = 100f;

    float reverbDampVal = 0.7f;
    float reverbSizeVal = 0.9f;
    float reverbGainVal = 0.7f;

    double xAxisUp = 0.7f;
    double xAxisDown = 0.0001f;
    double yAxisUp = 0.7f;
    double yAxisDown = 0.0001f;
    double zAxisUp = 0.7f;
    double zAxisDown = 0.0001f;
    double magUp = 0.7f;
    double magDown = 0.00001f;

    double prevXAxis = 0;
    double xAxisRunningVal = 0;
    double prevYAxis = 0;
    double yAxisRunningVal = 0;
    double prevZAxis = 0;
    double zAxisRunningVal = 0;
    double prevMag = 0;
    double magRunningVal = 0;

    @Override
    public void action(HB hb) {
        /***** Type your HBAction code below this line ******/
        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");


        final int NUMBER_AUDIO_CHANNELS = hb.ac.out.getOuts();

        SampleModule player = new SampleModule();

        player.setSample(player.EXAMPLE_SAMPLE_NAME);

        player.setLoopType(SamplePlayer.LoopType.LOOP_FORWARDS).setLoopStart(0).setLoopEnd(2000);


        Glide gainEnvelope = new Glide(1);
        Gain gain = new Gain(NUMBER_AUDIO_CHANNELS, gainEnvelope);
        //modulating lowPass
        BiquadFilter lowPass = new BiquadFilter(NUMBER_AUDIO_CHANNELS, BiquadFilter.Type.LP);
        Glide filterFreq = new Glide(filterFreqGlideTime);
        lowPass.setFrequency(filterFreq);
        lowPass.setQ(filterQVal);
        lowPass.setGain(filterGainVal);
        //reverb
        Gain reverbGain = new Gain(NUMBER_AUDIO_CHANNELS, reverbGainVal);
        Reverb rb = new Reverb(NUMBER_AUDIO_CHANNELS);
        rb.setDamping(reverbDampVal);
        rb.setSize(reverbSizeVal);
        //hpf for removing some of the bad bass
        BiquadFilter hpf = new BiquadFilter(NUMBER_AUDIO_CHANNELS, BiquadFilter.HP);
        hpf.setFrequency(hpfFreqVal);
        //connection

        player.connectTo(gain);

        lowPass.addInput(gain);
        reverbGain.addInput(lowPass);
        rb.addInput(reverbGain);
        hpf.addInput(rb);
        hpf.addInput(lowPass);
        hb.ac.out.addInput(hpf);


        /*****************************************************
         * Add a gyroscope sensor listener. *
         * to create this code, simply type gyroscopeSensor
         *****************************************************/
        new GyroscopeListener(hb) {
            @Override
            public void sensorUpdated(float pitch, float roll, float yaw) {
                /******** Write your code below this line ********/
                double xAxis = roll;
                double yAxis = yaw;
                double zAxis = pitch;
                double mag = Math.sqrt(xAxis*xAxis + yAxis*yAxis + zAxis*zAxis);
                //make some magic control values. Magic = these have weird decay ramps
                xAxisRunningVal += (xAxis - xAxisRunningVal) * ((prevXAxis < xAxis) ? xAxisUp : xAxisDown);
                prevXAxis = xAxis;
                yAxisRunningVal += (yAxis - yAxisRunningVal) * ((prevYAxis < yAxis) ? yAxisUp : yAxisDown);
                prevYAxis = yAxis;
                zAxisRunningVal += (zAxis - zAxisRunningVal) * ((prevZAxis < zAxis) ? zAxisUp : zAxisDown);
                prevZAxis = zAxis;
                magRunningVal += (mag - magRunningVal) * ((prevMag < mag) ? magUp : magDown);
                prevMag = mag;

                filterFreq.setValue((float)Math.abs(magRunningVal) * 500f + 50f);

                /******** Write your code above this line ********/
            }
        };
        /*** End gyroscopeSensor code ***/


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
