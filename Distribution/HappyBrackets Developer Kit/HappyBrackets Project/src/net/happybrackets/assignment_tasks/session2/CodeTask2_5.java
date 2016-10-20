package net.happybrackets.assignment_tasks.session2;

import javafx.application.Application;
import javafx.stage.Stage;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.*;
import net.happybrackets.controller.gui.WaveformVisualiser;
import net.happybrackets.extras.assignment_autograding.BeadsChecker;

/**
 *
 * The following code is exactly the same as CodeTask2_4.
 *
 * Now transform this delay into a ping-pong delay, in which the sound 'ping-pongs' from the left channel to the right channel and back again, with feedback as above. The time it takes to get from left to right is controlled separately from the time from right to left. Set your ping-pong delay so that the left channel echo comes 125ms after the original sound, and then the right channel echo comes 250ms after that, followed again by an attenuated delay in the left channel 125ms later, and so on.
 *
 */
public class CodeTask2_5 extends Application implements BeadsChecker.BeadsCheckable {

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
        //create a Clock
        Clock c = new Clock(ac, 500);
        ac.out.addDependent(c);
        //create a delay line with 10s of max audio storage
        TapIn tin = new TapIn(ac, 10000);
        Envelope delayTime = new Envelope(ac, 333);
        TapOut tout = new TapOut(ac, tin, delayTime);
        Gain delayGain = new Gain(ac, 1, 0.5f);
        delayGain.addInput(tout);
        ac.out.addInput(delayGain);
        //add some behaviour that responds to the clock
        c.addMessageListener(new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                if(c.isBeat()) {
                    Noise n = new Noise(ac);
                    Envelope e = new Envelope(ac, 0.1f);
                    Gain g = new Gain(ac, 1, e);
                    e.addSegment(0, 100, new KillTrigger(g));
                    g.addInput(n);
                    ac.out.addInput(g);
                    tin.addInput(g);
                }
            }
        });
        //********** do your work here ONLY **********
    }
}
