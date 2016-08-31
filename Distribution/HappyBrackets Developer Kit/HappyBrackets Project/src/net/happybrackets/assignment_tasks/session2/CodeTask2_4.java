package net.happybrackets.assignment_tasks.session2;

import de.sciss.net.OSCServer;
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
 * The following code plays a regular noise burst through a simple delay.
 *
 * 1) Identify what number below indicates the delay time of the delay, and use it to speed up the delay so that it is a very tight slapback of just a few milliseconds, overlapping the original sound.
 * 2) Make the delay feedback on itself by connecting two UGens together in a single line of code, and identify which number is responsible for the delay feedback level. Set that number so that the delay lasts at least as long as the interval between sound events.
 *
 */
public class CodeTask2_4 extends Application implements BeadsChecker.BeadsCheckable {

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
    }
}
