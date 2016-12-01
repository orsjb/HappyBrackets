package net.happybrackets.examples;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.*;

import java.util.Random;

/**
 * Created by ollie on 23/07/2016.
 */
public class RingModExample {

    public static void main(String[] args) {
        //random number generator
        Random rng = new Random();
        //set up audio
        AudioContext ac = new AudioContext();
        ac.start();
        //load samples
        SampleManager.group("guitar", "data/audio/Nylon_Guitar");
        //set up ring modulator
        Glide modFreq = new Glide(ac, 500, 100);
        WavePlayer modulator = new WavePlayer(ac, modFreq, Buffer.SINE);
        Clock clock = new Clock(ac, 500);
        ac.out.addDependent(clock);
        clock.addMessageListener(new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                if(clock.getCount() % 4 == 0) {
                    //load sample into sample player
                    SamplePlayer sp = new SamplePlayer(ac, SampleManager.randomFromGroup("guitar"));
                    //optional reverse
                    if(rng.nextFloat() < 0.3f) {
                        sp.getRateUGen().setValue(-1);
                        sp.setPosition(sp.getSample().getLength());
                    }
                    //mix in the ring mod
                    Mult m = new Mult(ac, sp, modulator);
                    Gain g = new Gain(ac, 1, 0.5f);
                    g.addInput(m);
                    //always remember to kill
                    sp.setKillListener(new KillTrigger(g));
                    ac.out.addInput(g);
                    //play with the ring mod value
                    if(rng.nextFloat() < 0.1f) {
                        modFreq.setValue(rng.nextFloat() * 500 + 100);
                    }
                    System.out.println(ac.out.getNumberOfConnectedUGens(0));
                }
            }
        });
    }
}
