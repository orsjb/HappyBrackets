package net.happybrackets.tutorial.session2;

import javafx.application.Application;
import javafx.stage.Stage;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Clock;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.controller.gui.WaveformVisualiser;

/**
 * Created by ollie on 5/06/2016.
 *
 * Here is some basic code that shows how to set up a Clock. If you run it you won't hear anything, but you will see the clock ticks outputting to the console.
 *
 * Complete the following tasks:
 *
 * 1) Add a WavePlayer object that plays continually.
 * 2) Create a Glide object that controls the frequency of the WavePlayer, with a glide time of 500.
 * 3) Use the Clock to update the Glide every 4 beats. Each time choose a new frequency at random, using pitches from a pentatonic scale in the octave above middle-C (MIDI note 60). Use the Pitch class to calculate frequencies from the MIDI note numbers.
 * 4) Add a new one-hit bass note that plays every 8 beats. The note should be a square wave, also chosen randomly from the same pentatonic scale but two octaves lower than the portamento line above. The note should play through an ADSR envelope and be removed once played, using a KillTrigger.
 *
 */
public class CodeTask2_2 extends Application {

    public static void main(String[] args) {launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        //Audio stuff
        AudioContext ac = new AudioContext();
        ac.start();
        WaveformVisualiser.open(ac);
        //create a Clock
        Clock c = new Clock(ac, 500);
        //important! Make sure your clock is running by adding it as a 'dependent' to some other UGen.
        ac.out.addDependent(c);
        //add some behaviour that responds to the clock
        c.addMessageListener(new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                if(c.isBeat()) {
                    System.out.println("-----BEAT------");
                }
                System.out.println("tick " + c.getCount() + " (beat " + c.getBeatCount() + ")");
            }
        });
    }
}
