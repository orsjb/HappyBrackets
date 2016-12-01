package net.happybrackets.assignment_tasks.session3;

import javafx.application.Application;
import javafx.stage.Stage;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.Clock;
import net.beadsproject.beads.ugens.GranularSamplePlayer;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.happybrackets.controller.gui.WaveformVisualiser;
import net.happybrackets.extras.assignment_autograding.BeadsChecker;

/**
 * Study the code below.
 *
 * Consider the following:
 * Why doesn't the SamplePlayer need to be destroyed, as was the case with Noise and WavePlayer in previous examples?
 * Why doesn't the sample data get read every time the sound is played?
 *
 * Tasks:
 * 1) Loop the SamplePlayer so that you get an alternating (backwards-forwards) loop over the last 25% of the file.
 * 2) Now that you are looping the SamplePlayer, apply an ADSR envelope, as you did in the code tasks in the previous session, including killing the sound (remember because you are passing the sound through a new UGen object, that object will not be killed automatically).
 * 3) Use the 'getPitch().setValue(x)' method on the SamplePlayer to pitch each note to follow a random pentatonic pattern. Note that the desired frequency is not the same as the playback rate. A playback rate of 1 will play the sound at its original frequency. The sample has a pitch class of "A natural".
 *
 */
public class CodeTask3_1 extends Application implements BeadsChecker.BeadsCheckable {

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
        //********** do your work here ONLY **********
        //clock
        Clock c = new Clock(ac, 500);
        ac.out.addDependent(c);
        c.addMessageListener(new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                if (c.getCount() % 32 == 0) {
                    ac.out.addInput(new SamplePlayer(ac, SampleManager.sample("data/audio/Nylon_Guitar/Clean_A_harm.wav")));
                }
            }
        });
        //********** do your work here ONLY **********
    }
}
