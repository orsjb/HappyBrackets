package net.happybrackets.assignment_tasks.session1;

import javafx.application.Application;
import javafx.stage.Stage;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.controller.gui.WaveformVisualiser;
import net.happybrackets.extras.assignment_autograding.BeadsChecker;

import java.util.Random;

/**
 *
 * This task involves for-loops. It is assumed you have enough coding experience to know how to make one. If you're not sure, take a look at the supporting material to see where you can learn about basic principles of Java programming.
 *
 * Remember to do all of your work in the task() function only. This will be tested.
 *
 * Using the example code, add an integer variable N that specifies the number of oscillators, and then write a for-loop that creates N oscillators.
 *
 * Be sure to also regulate the volume to account for the fact that many oscillators will be louder than 1!
 *
 * Try this with 10 or 100 oscillators.
 *
 * The number 50 below dictates the random spread of the oscillator detune. See what happens when you vary this.
 *
 */
public class CodeTask1_3 extends Application implements BeadsChecker.BeadsCheckable {

    public static void main(String[] args) {launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        //the AudioContext
        AudioContext ac = new AudioContext();
        ac.start();
        //a StringBuffer used to record anything you want to print out
        StringBuffer buf = new StringBuffer();
        //do your work here, using the function below
        task(ac, buf);
        //say something to the console output
        System.out.println(buf.toString());
        //finally, this creates a window to visualise the waveform
        WaveformVisualiser.open(ac);
    }

    @Override
    public void task(AudioContext ac, StringBuffer stringBuffer, Object... objects) {
        //create a random number generator
        Random rng = new Random();
        //create a WavePlayer with a slight random detune on the pitch
        WavePlayer wp = new WavePlayer(ac, 500 + rng.nextFloat() * 50, Buffer.SINE);
        ac.out.addInput(wp);
        //ac.out is actually a Gain object, so we can simply turn down the level here... no need for another Gain object.
        ac.out.setGain(0.1f);
    }
}
