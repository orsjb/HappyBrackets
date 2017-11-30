package net.happybrackets.examples;

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.*;
import net.happybrackets.device.HB;

public class HBPermBouncer {

    private Clock clock;
    private Glide modFreq;
    private Glide modDepth;
    private  Glide baseFreq;
    private boolean isPlayingSound = false;
    private boolean playSound = true;
    final String CONTROL_PREFIX = "Accel-";

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
    public HBPermBouncer(HB hb, float initial_freq) {
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
        Glide glide = new Glide(hb.ac, 0);
        Gain g = new Gain(hb.ac, 1, glide);

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
                        glide.setGlideTime(100);
                        glide.setValue(0);
                        isPlayingSound = false;
                    }
                    else {
                        if (playSound) {
                            glide.setGlideTime(10);
                            glide.setValue(0.1f);
                            isPlayingSound = true;
                        }

                    }

                }
            }
        });
    }
}
