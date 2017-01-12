/*
 * Copyright 2016 Ollie Bown
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.happybrackets.examples;/**
 * Created by ollie on 23/07/2016.
 */

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.*;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class Session2Example extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    boolean doReverb = false;

    @Override
    public void start(Stage primaryStage) throws IOException {
        //seq
        int[] seq = new int[]{0,5,4,3,2,4,5,4};
        //random
        Random rng = new Random();
        //audio context
        AudioContext ac = new AudioContext();
        ac.start();
        //reverb
        Reverb rb = new Reverb(ac, 2);
        ac.out.addInput(rb);
        //delay
        TapIn tin = new TapIn(ac, 10000);
        Glide delayTime = new Glide(ac, 200);
        TapOut tout = new TapOut(ac, tin, delayTime);
        Glide delayFeedbackAmount = new Glide(ac, 0.8f);
        Gain delayFeedback = new Gain(ac, 1, delayFeedbackAmount);
        delayFeedback.addInput(tout);
        tin.addInput(delayFeedback);
        tin.addInput(rb);
        ac.out.addInput(delayFeedback);
        //clock
        Clock clock = new Clock(ac, 500);
        clock.addMessageListener(new Bead() {
            int step = 0;
            @Override
            protected void messageReceived(Bead bead) {
                if(clock.getCount() % 16 == 0) {
                    int note = seq[step % seq.length];
                    note = 48 + Pitch.dorian[note];
                    step++;
                    WavePlayer wp = new WavePlayer(ac, Pitch.mtof(note), Buffer.SQUARE);
                    Envelope e = new Envelope(ac, 0.1f);
                    Gain g = new Gain(ac, 1, e);
                    g.addInput(wp);
                    e.addSegment(0, 100, new KillTrigger(g));
                    ac.out.addInput(g);
                    if(doReverb) {
                        rb.addInput(g);
                    } else {
                        tin.addInput(g);
                    }
                }
            }
        });
        //recorder
        RecordToFile rtf = new RecordToFile(ac, 2, new File("test.wav"));
        rtf.addInput(ac.out);
        ac.out.addDependent(rtf);
        ac.out.addDependent(clock);
        //general interface stuff
        VBox vbox = new VBox();
        Scene s = new Scene(vbox);
        primaryStage.setScene(s);
        //controls
        Button b = new Button("stop record");
        b.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                rtf.kill();
            }
        });
        vbox.getChildren().add(b);
        Button b2 = new Button("reverb");
        b2.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                doReverb = !doReverb;
            }
        });
        vbox.getChildren().add(b2);
        Slider sl = new Slider(0, 1, 0.8);
        sl.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                delayFeedbackAmount.setValue(newValue.floatValue());
            }
        });
        vbox.getChildren().add(sl);
        Slider sl2 = new Slider(1, 1000, 200);
        sl2.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                delayTime.setValue(newValue.floatValue());
            }
        });
        vbox.getChildren().add(sl2);
        primaryStage.show();
    }
}
