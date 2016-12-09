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
import javafx.stage.Stage;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.*;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class ReverbExample extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        Random rng = new Random();
        AudioContext ac = new AudioContext();
        ac.start();
        Reverb rb = new Reverb(ac, 2);
        ac.out.addInput(rb);
        Clock clock = new Clock(ac, 500);
        clock.addMessageListener(new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                if(clock.getCount() % 16 == 0) {
                    Noise n = new Noise(ac);
                    Envelope e = new Envelope(ac, 0.1f);
                    Gain g = new Gain(ac, 1, e);
                    e.addSegment(0.1f, 100);
                    e.addSegment(0, 100, new KillTrigger(g));
                    g.addInput(n);
                    if(rng.nextFloat() < 1) {
                        rb.addInput(g);
                    }
                    ac.out.addInput(g);
                }
            }
        });
        RecordToFile rtf = new RecordToFile(ac, 2, new File("test.wav"));
        rtf.addInput(ac.out);
        ac.out.addDependent(rtf);
        ac.out.addDependent(clock);
        Button b = new Button("X");
        Group g = new Group();
        g.getChildren().add(b);
        Scene s = new Scene(g);
        primaryStage.setScene(s);
        b.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
//                boolean paused = rb.isPaused();
//                rb.pause(!paused);

                rtf.kill();
            }
        });
        Slider sl = new Slider(0, 1, 0);
        sl.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                System.out.println(newValue);
            }
        });
        g.getChildren().add(sl);
        primaryStage.show();
    }
}
