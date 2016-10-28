package net.happybrackets.tutorial.session3;

import javafx.application.Application;
import javafx.stage.Stage;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.*;
import net.happybrackets.controller.gui.WaveformVisualiser;

/**
 * In this example we demonstrate a simple use of ring modulation, affecting a sample.
 */
public class Example3_3 extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        //set up the audio context
        AudioContext ac = new AudioContext();
        ac.start();
        //load sample
        Sample s = SampleManager.sample("data/audio/Nylon_Guitar/Clean_A_harm.wav");
        SamplePlayer sp = new SamplePlayer(ac, s);
        //set loop loop parameters
        sp.setLoopType(SamplePlayer.LoopType.LOOP_ALTERNATING);
        Envelope loopEndEnv = new Envelope(ac, 500);
        sp.setLoopEnd(loopEndEnv);
        loopEndEnv.addSegment(1, 5000);
        //Envelope controlling the frequency of the ringmod modulation signal
        Envelope modFreq = new Envelope(ac, 500);
        modFreq.addSegment(1000, 3000);
        //the ringmod oscillator
        WavePlayer ringmodulator = new WavePlayer(ac, modFreq, Buffer.SINE);
        Gain g = new Gain(ac, 1, 0.1f);
        g.addInput(ringmodulator);
        //all way do to get the ringmod effect is multiply the source signal by our osciallator's signal
        Mult m = new Mult(ac, sp, g);
        ac.out.addInput(m);
        //visualiser
        WaveformVisualiser.open(ac);

    }
}
