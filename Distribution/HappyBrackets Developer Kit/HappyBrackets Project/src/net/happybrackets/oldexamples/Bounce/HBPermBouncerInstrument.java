package net.happybrackets.oldexamples.Bounce;

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.*;
import net.happybrackets.device.HB;

/**
 * This class encapsulates the bouncer into a separate class
 * We are able to make various instances of the class, and this, facilitate
 * polyphony
 * The Modulator frequency and depth are based on the base frequency, thus creating
 * a predictable timbre for the sound
 */
public class HBPermBouncerInstrument {

    private Clock clock;
    private Glide modFreq;
    private Glide modDepth;
    private  Glide baseFreq;
    private boolean isPlayingSound = false;
    private boolean playSound = true;
    private float modFreqMultiplier = 0;
    private float modDepthMultiplier = 0;


    public void setSpeed(int rate){
        clock.setTicksPerBeat( rate);
    }
    /**
     * Commence playing
     */
    public void play() {playSound = true;}

    /**
     * Stop Playing
     */
    public void stop(){playSound = false;}

    /**
     * Set Modulator Frequency factor.
     * @param multiplier value to multiply with the carrier frequency
     */
    public void setModFreqFactor(float multiplier){
        modFreqMultiplier = multiplier;
        setModFreqency();

    }

    /**
     * Set the actual Modulator frequency
     */
    private void setModFreqency() {
        modFreq.setValue(modFreqMultiplier * baseFreq.getValue());

    }
    /**
     * Sets the depth of modulation multiplied by the  base frequency.
     * Eg, if depth_factor is 0.5 and the carrier frequency is 2KHz,
     * The frequency will swing from 1KHz to 3KHz
     * @param depth_multiplier The depth frequency factor
     */
    public void setModDepthMultiplier(float depth_multiplier){
        modDepthMultiplier = depth_multiplier;
        setModDepth();
    }

    /**
     * Set the actual Modulator Depth Frequency
     */
    private void setModDepth(){
        modDepth.setValue(modDepthMultiplier * baseFreq.getValue());

    }
    /**
     * Sets the carrier frequency of the sound
     * @param freq
     */
    public void setBaseFreq(float freq){

        baseFreq.setValue(freq);
        // we need to update the actual mdoulators based on these values
        setModDepth();
        setModFreqency();
    }
    /**
     * create a bouncer class so we can modularise
     * @param hb HappyBracket instance
     * @param initial_freq initial frequency to bounce
     */
    public HBPermBouncerInstrument(HB hb, float initial_freq) {
        clock = new Clock(hb.ac, 500);

        modFreq = new Glide(hb.ac, 0);
        modDepth = new Glide(hb.ac, 0);
        baseFreq = new Glide(hb.ac, initial_freq);
        hb.ac.out.addDependent(clock);

        //this is the FM synth
        WavePlayer modulator = new WavePlayer(hb.ac, modFreq, Buffer.SINE);
        Function modFunction = new Function(modulator, modDepth, baseFreq) {
            @Override
            public float calculate() {
                return x[0] * x[1] + x[2];
            }
        };

        WavePlayer wp = new WavePlayer(hb.ac, modFunction, Buffer.SINE);
        //add the gain
        Glide gainLevel = new Glide(hb.ac, 0);
        Gain g = new Gain(hb.ac, 1, gainLevel);

        //connect together
        g.addInput(wp);
        hb.ac.out.addInput(g);


        clock.addMessageListener(new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                long clock_count = clock.getCount();
                if (clock_count % 16 == 0) {
                    //add the waveplayer
                    if (isPlayingSound) {
                        gainLevel.setGlideTime(100);
                        gainLevel.setValue(0);
                        isPlayingSound = false;
                    }
                    else {
                        if (playSound) {
                            gainLevel.setGlideTime(10);
                            gainLevel.setValue(0.1f);
                            isPlayingSound = true;
                        }

                    }

                }
            }
        });
    }
}
