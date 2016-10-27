package net.happybrackets.tutorial.session2;

import javafx.application.Application;
import javafx.stage.Stage;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.controller.gui.WaveformVisualiser;

/**
 */
public class Example2_1 extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        //set up the audio context
        AudioContext ac = new AudioContext();
        ac.start();
        //add the waveplayer
        Envelope e = new Envelope(ac, 500);
        e.addSegment(1000, 2000);
        e.addSegment(500, 200);
        WavePlayer wp = new WavePlayer(ac, e, Buffer.SINE);
        Gain g = new Gain(ac, 1, 0.1f);
        //connect together
        g.addInput(wp);
        ac.out.addInput(g);
        //visualiser
        WaveformVisualiser.open(ac);
    }
}
