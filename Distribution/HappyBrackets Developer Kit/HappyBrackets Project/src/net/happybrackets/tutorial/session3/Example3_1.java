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
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.*;
import net.happybrackets.controller.gui.WaveformVisualiser;

/**
 * Created by ollie on 25/07/2016.
 */
public class Example3_1 extends Application {

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

        WavePlayer lfo = new WavePlayer(ac, 1, Buffer.SINE);

        WavePlayer wp = new WavePlayer(ac, 500, Buffer.SINE);

        ZMap mappedLFOToGain = new ZMap(ac, 1);
        mappedLFOToGain.setRanges(-1, 1, 0, 0.1f);
        mappedLFOToGain.addInput(lfo);
        Gain g = new Gain(ac, 1, mappedLFOToGain);
        g.addInput(wp);
        ac.out.addInput(g);

        //visualiser
        WaveformVisualiser.open(ac);

    }
}
