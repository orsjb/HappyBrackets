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

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.ugens.*;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.LSM9DS1;
import net.happybrackets.device.sensors.SensorUpdateListener;

public class ResonantBooms implements HBAction {

    float filterFreqGlideTime = 800;

    float filterQVal = 2f;
    float filterGainVal = 2f;

    float hpfFreqVal = 100f;

    float reverbDampVal = 0.7f;
    float reverbSizeVal = 0.9f;
    float reverbGainVal = 0.7f;

    double xAxisUp = 0.7f;
    double xAxisDown = 0.0001f;
    double yAxisUp = 0.7f;
    double yAxisDown = 0.0001f;
    double zAxisUp = 0.7f;
    double zAxisDown = 0.0001f;
    double magUp = 0.7f;
    double magDown = 0.00001f;

    @Override
    public void action(HB hb) {
        //setup
        hb.reset();
        hb.testBleep();

        /*
        Path is oscillator > gain > lowPass > {reverbGain > reverb, } > hpf > hb.ac.out
         */

        //modulated oscillator
        Glide modFreq = new Glide(hb.ac, 20);
        WavePlayer modulator = new WavePlayer(hb.ac, modFreq, Buffer.SINE);
        Glide baseFreq = new Glide(hb.ac, 200);
        Glide modAmount = new Glide(hb.ac, 30);
        Function f = new Function(baseFreq, modAmount, modulator) {
            @Override
            public float calculate() {
                return x[0] + x[1] * x[2];
            }
        };
        WavePlayer oscillator = new WavePlayer(hb.ac, f, Buffer.SAW);
        Glide gainEnvelope = new Glide(hb.ac, 0);
        Gain gain = new Gain(hb.ac, 1, gainEnvelope);
        //modulating lowPass
        BiquadFilter lowPass = new BiquadFilter(hb.ac, 1, BiquadFilter.Type.LP);
        Glide filterFreq = new Glide(hb.ac, filterFreqGlideTime);
        lowPass.setFrequency(filterFreq);
        lowPass.setQ(filterQVal);
        lowPass.setGain(filterGainVal);
        //reverb
        Gain reverbGain = new Gain(hb.ac, 1, reverbGainVal);
        Reverb rb = new Reverb(hb.ac, 2);
        rb.setDamping(reverbDampVal);
        rb.setSize(reverbSizeVal);
        //hpf for removing some of the bad bass
        BiquadFilter hpf = new BiquadFilter(hb.ac, 2, BiquadFilter.HP);
        hpf.setFrequency(hpfFreqVal);
        //connection
        gain.addInput(oscillator);
        lowPass.addInput(gain);
        reverbGain.addInput(lowPass);
        rb.addInput(reverbGain);
        hpf.addInput(rb);
        hpf.addInput(lowPass);
        hb.ac.out.addInput(hpf);
        //sensor listener
        LSM9DS1 mySensor = (LSM9DS1) hb.getSensor(LSM9DS1.class);
        mySensor.addListener(new SensorUpdateListener() {

            double prevXAxis = 0;
            double xAxisRunningVal = 0;
            double prevYAxis = 0;
            double yAxisRunningVal = 0;
            double prevZAxis = 0;
            double zAxisRunningVal = 0;
            double prevMag = 0;
            double magRunningVal = 0;

            @Override
            public void sensorUpdated() {
                //get the data
                double xAxis = mySensor.getGyroscopeData()[0];
                double yAxis = mySensor.getGyroscopeData()[1];
                double zAxis = mySensor.getGyroscopeData()[2];
                double mag = Math.sqrt(xAxis*xAxis + yAxis*yAxis + zAxis*zAxis);
                //make some magic control values. Magic = these have weird decay ramps
                xAxisRunningVal += (xAxis - xAxisRunningVal) * ((prevXAxis < xAxis) ? xAxisUp : xAxisDown);
                prevXAxis = xAxis;
                yAxisRunningVal += (yAxis - yAxisRunningVal) * ((prevYAxis < yAxis) ? yAxisUp : yAxisDown);
                prevYAxis = yAxis;
                zAxisRunningVal += (zAxis - zAxisRunningVal) * ((prevZAxis < zAxis) ? zAxisUp : zAxisDown);
                prevZAxis = zAxis;
                magRunningVal += (mag - magRunningVal) * ((prevMag < mag) ? magUp : magDown);
                prevMag = mag;
                //now do stuff with magRunningVal, xAxisRunningVal, etc.
                business();
                //print outs
                System.out.println("------SENSOR UPDATE------");
                System.out.print(" x   : ");
                for(int i = 0; i < xAxisRunningVal; i++) {
                    System.out.print(" ");
                }
                System.out.println("|" + xAxisRunningVal);
                System.out.print(" y   : ");
                for(int i = 0; i < yAxisRunningVal; i++) {
                    System.out.print(" ");
                }
                System.out.println("|" + yAxisRunningVal);
                System.out.print(" z   : ");
                for(int i = 0; i < zAxisRunningVal; i++) {
                    System.out.print(" ");
                }
                System.out.println("|" + zAxisRunningVal);
                System.out.print(" mag : ");
                for(int i = 0; i < magRunningVal; i++) {
                    System.out.print(" ");
                }
                System.out.println("|" + magRunningVal);
            }

            public void business() {

                //updates to the modulation
                modFreq.setValue((float)(zAxisRunningVal*zAxisRunningVal) * 3f);
                modAmount.setValue(15);

                //updates to the synth
                gainEnvelope.setValue((float)Math.abs(magRunningVal) * 0.5f);
//                int xmapped = (int)(xAxisRunningVal*xAxisRunningVal * 20 + 30);
//                float freqFromMidi = Pitch.mtof(Pitch.forceToScale(xmapped, Pitch.dorian));
                baseFreq.setValue((float)xAxisRunningVal * 30 + 200);

                //updates to the lowPass
                filterFreq.setValue((float)Math.abs(magRunningVal) * 500f + 50f);

            }
        });
    }
}
