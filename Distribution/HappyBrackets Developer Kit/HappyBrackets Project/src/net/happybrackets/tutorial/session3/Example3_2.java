package net.happybrackets.tutorial.session3;

import javafx.application.Application;
import javafx.stage.Stage;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Function;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.controller.gui.WaveformVisualiser;

/**
 * This example demonstrates a simple Frequency Modulation signal chain.
 *
 * Note in this case the base frequency is dynamically controlled by an Envelope, whereas the modulation ratio and the modulation amount are both fixed values. It would be simple to make all of these parameters dynamic and controlled by any other factors you wished.
 *
 */
public class Example3_2 extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        //set up the audio context
        AudioContext ac = new AudioContext();
        ac.start();
        //base freq - as envelope, with a line segment
        Envelope baseFreq = new Envelope(ac, 500);
        baseFreq.addSegment(1000, 2000);
        //fixed modulation ratio (the ratio of the modulator frequency to the carrier frequency
        float modRatio = 1.1f;
        //create the modulator wave player, with a Function object mapping the baseFreq.
        WavePlayer modulator = new WavePlayer(ac, new Function(baseFreq) {
            @Override
            public float calculate() {
                return x[0] * modRatio;
            }
        }, Buffer.SINE);
        //fixed modulation amount
        float modAmount = 500;
        //create the modulation signal, which combines the baseFreq with the output from the modulator
        Function modSignal = new Function(modulator, baseFreq) {
            @Override
            public float calculate() {
                return x[0] * modAmount + x[1];
            }
        };
        //create the carrier signal
        WavePlayer carrier = new WavePlayer(ac, modSignal, Buffer.SINE);
        //connect to output
        Gain g = new Gain(ac, 1, 0.1f);
        g.addInput(carrier);
        ac.out.addInput(g);
        //visualiser
        WaveformVisualiser.open(ac);
    }
}
