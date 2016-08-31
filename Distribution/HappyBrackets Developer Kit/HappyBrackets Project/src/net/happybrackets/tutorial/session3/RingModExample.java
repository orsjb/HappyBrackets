package net.happybrackets.tutorial.session3;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.*;

import java.io.File;
import java.io.IOException;
import java.util.Random;

/**
 * Created by ollie on 23/07/2016.
 */
public class RingModExample {

    public static void main(String[] args) throws IOException {
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
                    float pan = rng.nextFloat();
                    UGen panner = new UGen(ac, 1, 2) {
                        @Override
                        public void calculateBuffer() {
                            for(int i = 0; i < bufferSize; i++) {
                                float val = bufIn[0][i] * 0.5f;
                                bufOut[0][i] = val * pan;
                                bufOut[1][i] = val * (1 - pan);
                            }
                        }
                    };
                    panner.addInput(m);
                    //always remember to kill
                    sp.setKillListener(new KillTrigger(panner));
                    ac.out.addInput(panner);
                    //play with the ring mod value
                    if(rng.nextFloat() < 0.1f) {
                        modFreq.setValue(rng.nextFloat() * 500 + 100);
                    }
                    System.out.println(ac.out.getNumberOfConnectedUGens(0));
                }
            }
        });

        //recording code
        RecordToFile recorder = new RecordToFile(ac, 2, new File("test.wav"));
        ac.out.addDependent(recorder);
        recorder.addInput(ac.out);
        DelayTrigger dt = new DelayTrigger(ac, 20000, new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                recorder.kill();
                System.out.println("RECORDING HAS ENDED");
            }
        });
        ac.out.addDependent(dt);

    }
}
