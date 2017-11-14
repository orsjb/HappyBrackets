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

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.*;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.LSM9DS1;
import net.happybrackets.device.sensors.SensorUpdateListener;

/**
 * An example with a more extensive musical structure. You can play with this while it is running.
 */
public class LiveCoder implements HBAction {

    float baseFreq = 0;

    @Override
    public void action(HB hb) {
        hb.resetLeaveSounding();
        hb.masterGainEnv.setValue(0.2f);
        hb.clock.getIntervalUGen().setValue(2000);

        Glide freq = new Glide(hb.ac, 5000);
        BiquadFilter bf = new BiquadFilter(hb.ac, 1, BiquadFilter.Type.LP);
        PolyLimit pl = new PolyLimit(hb.ac, 1, 4);
        bf.setFrequency(freq);
        bf.addInput(pl);
        hb.sound(bf);

        hb.pattern(new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                if (hb.clock.getCount() % 2 == 0) {
                    float freq = Pitch.forceFrequencyToScale(hb.rng.nextFloat() * 2000 + baseFreq, Pitch.dorian) * 1f;
                    if (hb.rng.nextFloat() < 0.2f) freq *= 0.5f;
                    WavePlayer wp = new WavePlayer(hb.ac, freq, Buffer.SINE);
                    Envelope e = new Envelope(hb.ac, 0);
                    Gain g = new Gain(hb.ac, 1, e);
                    e.addSegment(hb.rng.nextFloat() * 0.05f + 0.1f, 50 * hb.rng.nextFloat());
                    e.addSegment(0, 2000, new KillTrigger(g));
                    g.addInput(wp);
//                    hb.sound(g);
                    pl.addInput(g);
                }
                if (hb.clock.getCount() % 6 == 5) {
                    float freq = Pitch.forceFrequencyToScale(hb.rng.nextFloat() * 1000 + baseFreq, Pitch.dorian);
                    WavePlayer wp = new WavePlayer(hb.ac, freq, Buffer.SAW);
                    Envelope e = new Envelope(hb.ac, 0);
                    Gain g = new Gain(hb.ac, 1, e);
                    e.addSegment(hb.rng.nextFloat() * 0.02f + 0.04f, 10);
                    e.addSegment(0, 100, new KillTrigger(g));
                    g.addInput(wp);
//                    hb.sound(g);
                    pl.addInput(g);
                }
                if (hb.clock.getCount() % 4 == 0 || hb.rng.nextFloat() < 0.02f) {
//                    Noise n = new Noise(hb.ac);
//                    Envelope e = new Envelope(hb.ac, hb.rng.nextFloat() * hb.rng.nextFloat() * 0.2f + 0.02f);
//                    Gain g = new Gain(hb.ac, 1, e);
//                    e.addSegment(0, 2 + hb.rng.nextInt(20), new KillTrigger(g));
//                    g.addInput(n);
//                    hb.sound(g);
                }
            }
        });
//        LSM9DS1 mySensor = (LSM9DS1) hb.getSensor(LSM9DS1.class);
//        mySensor.addListener(new SensorUpdateListener() {
//
//
//            double prevMag = 0;
//            double val = 0;
//
//            @Override
//            public void sensorUpdated() {
//                // Get the data from Z.
//                double zAxis = mySensor.getGyroscopeData()[2];
//                double yAxis = mySensor.getGyroscopeData()[1];
//                double xAxis = mySensor.getGyroscopeData()[0];
//                //
//                hb.clock.getIntervalUGen().setValue((float)Math.abs(zAxis) * 60000 + 300);
//                baseFreq = (float)Math.abs(xAxis) * 500 + 500;
//                freq.setValue((float)Math.abs(yAxis) * 50000 + 50);
//            }
//        });
    }
}
