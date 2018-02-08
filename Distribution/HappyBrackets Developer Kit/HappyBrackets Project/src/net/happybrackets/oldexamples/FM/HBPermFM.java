package net.happybrackets.oldexamples.FM;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.*;
import net.happybrackets.device.HB;

public class HBPermFM {

    private Glide modFreq;
    private Glide modDepth;
    private  Glide baseFreq;
    private Glide gainLevel;
    private boolean isPlayingSound = false;
    private boolean playSound = true;


    /**
     * Sets the gain of the FM Synth
     * @param level the new level
     */
    public void setGainLevel(float level){
        gainLevel.setValue(level);
    }
    /**
     * Set Modulatore Frequency
     * @param freq the frequency to set to
     */
    public void setModFreq(float freq){
        modFreq.setValue(freq);
    }

    /**
     * Sets the depth of modulation.
     * Eg, if depth is 500 and the carrier frequency is 2KHz,
     * The freque will swing from 1.5KHz to 2.5KHz
     * @param depth The depth frequency
     */
    public void setModDepth(float depth){
        modDepth.setValue(depth);
    }

    /**
     * Sets the carrier frequency of the sound
     * @param freq
     */
    public void setBaseFreq(float freq){
        baseFreq.setValue(freq);
    }
    /**
     * create a bouncer class so we can modularise
     * @param hb HappyBracket instance
     * @param initial_freq initial frequency to bounce
     */
    public HBPermFM(HB hb, float initial_freq) {

        modFreq = new Glide(hb.ac, 0);
        modDepth = new Glide(hb.ac, 0);
        baseFreq = new Glide(hb.ac, initial_freq);

        //this is the FM synth
        WavePlayer modulator = new WavePlayer(hb.ac, modFreq, Buffer.SINE);

        //THis function actually determines what the wave player is doing
        Function modFunction = new Function(modulator, modDepth, baseFreq) {
            @Override
            public float calculate() {
                return x[0] * x[1] + x[2];
            }
        };

        WavePlayer wp = new WavePlayer(hb.ac, modFunction, Buffer.SINE);
        //add the gain
        gainLevel = new Glide(hb.ac, 0);
        Gain g = new Gain(hb.ac, 1, gainLevel);

        //connect together
        g.addInput(wp);
        hb.ac.out.addInput(g);

    }
}
