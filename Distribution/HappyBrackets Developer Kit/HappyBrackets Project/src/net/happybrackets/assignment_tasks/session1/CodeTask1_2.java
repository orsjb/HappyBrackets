package net.happybrackets.assignment_tasks.session1;

import javafx.application.Application;
import javafx.stage.Stage;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Noise;
import net.happybrackets.controller.gui.WaveformVisualiser;
import net.happybrackets.extras.assignment_autograding.BeadsChecker;

/**
 *
 * This code is exactly the same as in CodeTask1_1.
 *
 * Modify the code so that you have two WavePlayers, one connected to the left channel playing a sine tone at 500hz, and one connected to the right channel, playing a square wave tone at 750hz. They should be passing through a stereo Gain object with a gain of 0.2.
 *
 */
public class CodeTask1_2 extends Application implements BeadsChecker.BeadsCheckable {

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
    public void task(AudioContext ac, StringBuffer buf, Object... args) {
        //do your work here
        Noise n = new Noise(ac);
        Gain g = new Gain(ac, 1, 0.1f);
        g.addInput(n);
        ac.out.addInput(g);
    }
}
