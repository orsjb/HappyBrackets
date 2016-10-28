package net.happybrackets.tutorial.session2;

import javafx.application.Application;
import javafx.stage.Stage;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.Clock;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.controller.gui.WaveformVisualiser;

/**
 * In this example, we show simple use of the Clock class.
 * It is really important to notice the line ac.out.addDependent(clock), without which the Clock does not run.
 *
 * Notice below, if you are looking at this file in IntelliJ that the code for creating the Bead is rewritten to simplify the dynamic creation of this class. Look for the line clock.addMessageListener() and exand it by clicking on the + button on the left hand side of the code.
 *
 * Also notice here that at each clock event we are creating a new sound, added to the signal chain. We use an Envelope and a KillTrigger to ensure that the sound does not remain forever, but fades out to zero and is then destroyed.
 */
public class Example2_3 extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        //set up the audio context
        AudioContext ac = new AudioContext();
        ac.start();

        Clock clock = new Clock(ac, 500);
        ac.out.addDependent(clock);

        clock.addMessageListener(new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                if(clock.getCount() % 16 == 0) {
                    //add the waveplayer
                    WavePlayer wp = new WavePlayer(ac, 500, Buffer.SINE);
                    //add the gain
                    Envelope e = new Envelope(ac, 0.1f);
                    Gain g = new Gain(ac, 1, e);
                    e.addSegment(0, 200, new KillTrigger(g));
                    //connect together
                    g.addInput(wp);
                    ac.out.addInput(g);
                }
            }
        });

        //visualiser
        WaveformVisualiser.open(ac);
    }

}
