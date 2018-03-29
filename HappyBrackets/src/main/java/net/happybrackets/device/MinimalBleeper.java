package net.happybrackets.device;

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.ugens.*;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.Accelerometer;
import net.happybrackets.device.sensors.Gyroscope;
import net.happybrackets.device.sensors.LSM9DS1;
import net.happybrackets.device.sensors.SensorUpdateListener;

public class MinimalBleeper implements HBAction {

    int[] pitches = Pitch.dorian;
    int[] arpeggio = new int[]{0,4,0,2};
    int[] rates = new int[]{16, 20, 24, 28, 32};
    int nextStep = 0;
    int rate = 8;
    int step;
    float level = 0;
    float coreFreq;

    @Override
    public void action(HB hb) {

        hb.reset();

        hb.clock.getIntervalUGen().setValue(4000);

        Envelope modFreqMount = new Envelope(hb.ac, 0);
        WavePlayer sawMod = new WavePlayer(hb.ac, 1, Buffer.SINE);

        Glide gEnv = new Glide(hb.ac, 0);

        Glide baseFreq = new Glide(hb.ac, 1000);
        Function f = new Function(baseFreq, sawMod, modFreqMount) {
            @Override
            public float calculate() {
                return x[0] + x[0] * x[1] * x[2];
            }
        };
        WavePlayer saw = new WavePlayer(hb.ac, f, Buffer.SINE);
        Gain gain = new Gain(hb.ac, 1, gEnv);
        gain.addInput(saw);
        Reverb rb = new Reverb(hb.ac, 1);
        rb.addInput(gain);
        hb.sound(rb);

        BiquadFilter hpf = new BiquadFilter(hb.ac, 1, BiquadFilter.HP);
        hpf.setFrequency(200);
        hpf.setQ(1f);
        hpf.setGain(1f);
        hpf.addInput(gain);

        hb.sound(hpf);



        hb.pattern(new Bead() {
            @Override
            protected void messageReceived(Bead bead) {

                if(hb.clock.getCount() > nextStep) {
//                    gEnv.clear();
//                    gEnv.setValue(0.f);
//                    gEnv.addSegment(0.2f, 500);
//                    gEnv.addSegment(0.1f, 30);
//                    gEnv.addSegment(0.02f, 500);
//                    gEnv.addSegment(0.0f, 500);

                    level += 0.2f;

                    modFreqMount.clear();
                    modFreqMount.setValue(0);
                    modFreqMount.addSegment(0.5f, 200);
                    modFreqMount.addSegment(2f, 800);

                    //set the frequency
//                    step++;
                    int arpVal = arpeggio[step % arpeggio.length];
                    int pitch = pitches[arpVal % pitches.length] + 48;
                    coreFreq =Pitch.mtof(pitch);

                    nextStep = (int)hb.clock.getCount() + rates[hb.rng.nextInt(rates.length)];

                }

            }
        });

        Accelerometer mySensor = (Accelerometer)hb.getSensor(Accelerometer.class);
        if (mySensor != null){
            // add the listener
            mySensor.addListener(new SensorUpdateListener() {

                @Override
                public void sensorUpdated() {
                    double xAxis = mySensor.getAccelerometerX();
                    double yAxis = mySensor.getAccelerometerY();
                    double zAxis = mySensor.getAccelerometerZ();
                    hb.setStatus("Zaxis: " + zAxis);
                    arpeggio[2] = (int)(zAxis * 10) % pitches.length;
//                    System.out.println("Zaxis:  " + zAxis);
                    sawMod.setFrequency((float)(xAxis * xAxis) * 100 + 5);
                    baseFreq.setValue(coreFreq + 100*(float)yAxis);
                }

            });
        } else {
            System.out.println("No sensor");
        }

        Gyroscope mySensor2 = (Gyroscope) hb.getSensor(Gyroscope.class);
        if(mySensor2 != null) {
            mySensor2.addListener(new SensorUpdateListener() {

                @Override
                public void sensorUpdated() {
                    if (level != 0) {
                        double xAxis = Math.abs(mySensor2.getGyroscopeX());
                        level += xAxis * 0.01f;
                        level -= 0.01f;
                        if (level < 0) level = 0;
                        gEnv.setValue(level);
                    }
                }

            });

        }
    }
}
