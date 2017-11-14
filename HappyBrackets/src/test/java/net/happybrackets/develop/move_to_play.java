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

package mappings;

import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Function;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.LSM9DS1;
import net.happybrackets.device.sensors.SensorUpdateListener;

/**
 * For this example we want to look at the accelerometer and use it to trigger a sound when you turn over the
 * accelerometer.
 */
public class move_to_play implements HBAction {

    public enum Orientation {UP, DOWN}

    Orientation currentOri = Orientation.UP;
    Orientation previousOri = Orientation.DOWN;



    @Override
    public void action(HB hb) {
        hb.reset();
        //load a set of sounds
        Sample s = SampleManager.sample("data/audio/harry_potter/hp-demo-2.wav");
        SamplePlayer sp = new SamplePlayer(hb.ac, s);
        Envelope e = new Envelope(hb.ac, 0.01f);

        SampleManager.group("Guitar", "data/audio/Nylon_Guitar");
        //play a new random sound
        Sample s1 = SampleManager.fromGroup("Guitar", 3);

        Function spinMapping = new Function(e) {
            public float calculate() {

                float y = (float) (1/(1 + Math.exp((double) (-25 * (x[0] - 0.5)))));
                return y;
            }
        };

        Gain g = new Gain(hb.ac, 1, spinMapping);
        g.addInput(sp);


        SamplePlayer sp1 = new SamplePlayer(hb.ac, s1);

        //Play test sound
        hb.sound(sp1);
        hb.sound(g);

        LSM9DS1 mySensor = (LSM9DS1) hb.getSensor(LSM9DS1.class);
        mySensor.addListener(new SensorUpdateListener() {
            @Override
            public void sensorUpdated() {
                // Get the data from Z.
                double zAxis = mySensor.getGyroscopeData()[2];
                double yAxis = mySensor.getGyroscopeData()[1];
                double xAxis = mySensor.getGyroscopeData()[0];

                double spin = Math.sqrt(Math.sqrt(xAxis+yAxis)+zAxis);

                System.out.println("Spin Mapping: ");
                e.addSegment((float)spin,10);
            }
        });
    }


}
