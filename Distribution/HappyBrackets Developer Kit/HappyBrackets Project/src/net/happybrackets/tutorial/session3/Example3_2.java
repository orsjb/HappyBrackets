package net.happybrackets.tutorial.session3;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.*;
import net.happybrackets.controller.gui.WaveformVisualiser;

import java.util.Random;

/**
 * Created by ollie on 25/07/2016.
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
        //load sample
        Sample s = SampleManager.sample("data/audio/Nylon_Guitar/Clean_A_harm.wav");
        GranularSamplePlayer sp = new GranularSamplePlayer(ac, s);
        //set rate and pitch parameters
        Envelope e = new Envelope(ac, 2);
        e.addSegment(1, 3000);
        sp.setRate(e);
        sp.getPitchUGen().setValue(4);
        //set loop loop parameters
        sp.setLoopType(SamplePlayer.LoopType.LOOP_ALTERNATING);
        Envelope loopEndEnv = new Envelope(ac, 500);
        sp.setLoopEnd(loopEndEnv);
        loopEndEnv.addSegment(1, 5000);
        //set grain parameters
        sp.getGrainIntervalUGen().setValue(10);
        sp.getGrainSizeUGen().setValue(20);
        sp.getRandomnessUGen().setValue(0.01f);
        //ac.out.addInput(sp);

        Random random = new Random();

        Envelope baseFreq = new Envelope(ac, 500);
        baseFreq.addSegment(1000, 2000);

        float modAmount = 500;
        WavePlayer modulator = new WavePlayer(ac, new Function(baseFreq) {
            @Override
            public float calculate() {
                return x[0] * 1.1f;
            }
        }, Buffer.SINE);

        Function modSignal = new Function(modulator, baseFreq) {
            @Override
            public float calculate() {
                return x[0] * modAmount + x[1];
            }
        };
        WavePlayer carrier = new WavePlayer(ac, modSignal, Buffer.SINE);


        Gain g = new Gain(ac, 1, 0.1f);
        g.addInput(carrier);
        ac.out.addInput(g);

        //visualiser
        WaveformVisualiser.open(ac);

    }
}
