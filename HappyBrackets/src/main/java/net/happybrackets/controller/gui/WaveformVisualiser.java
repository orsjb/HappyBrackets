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

package net.happybrackets.controller.gui;

import com.sun.glass.ui.Application;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import net.beadsproject.beads.core.AudioContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ollie on 7/07/2016.
 */
public abstract class WaveformVisualiser {

    final static Logger logger = LoggerFactory.getLogger(WaveformVisualiser.class);

    public static void open(AudioContext ac) {
        open(ac, true);
    }

    public static void open(AudioContext ac, boolean killAudioOnClose) {
        Stage stage = new Stage();
        stage.setResizable(false);
        Canvas c = new Canvas();
        c.setWidth(500);
        c.setHeight(300);
        Label l = new Label("Waveform");
        l.setAlignment(Pos.CENTER);
        VBox vb = new VBox(4, l, c);
        vb.setAlignment(Pos.CENTER);
        stage.setScene(new Scene(vb));
        stage.show();
        if(killAudioOnClose) {
            //we have to make sure stops when the window closes, else the program never exits
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    ac.stop();
                    System.exit(0);
                }       //not always desired behaviour
            });
        }
        new AnimationTimer() {
            public void handle(long currentNanoTime) {
                    GraphicsContext g2d = c.getGraphicsContext2D();
                    g2d.clearRect(0, 0, c.getWidth(), c.getHeight());
                    g2d.setStroke(Color.BLUE);
                    g2d.setLineWidth(0.2);
                    for(int chan = 0; chan < ac.out.getOuts(); chan++) {
                        float[] buf = ac.out.getOutBuffer(chan);
                        int lastY = (int) (((buf[0] * 0.5f + 0.5) + chan) * c.getHeight()/ac.out.getOuts());
                        int lastX = 0;
                        g2d.moveTo(0, lastY);
                        for (int i = 0; i < buf.length; i++) {
                            float f = buf[i];
                            if(f < -1) f = -1; if(f > 1)  f = 1; if(Float.isNaN(f)) f = 0;
                            int x = (int) ((float) i / buf.length * c.getWidth());
                            int y = (int) (((f * 0.5f + 0.5) + chan) * c.getHeight()/ac.out.getOuts());
                            g2d.strokeLine(lastX, lastY, x, y);
                            lastX = x;
                            lastY = y;
                        }
                    }
            }
        }.start();
    }

}
