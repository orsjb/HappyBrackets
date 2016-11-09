package net.happybrackets.compositions;

import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.*;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.device.network.NetworkCommunication;
import net.happybrackets.device.sensors.MiniMU;
import net.happybrackets.device.sensors.SensorUpdateListener;
import net.happybrackets.device.sensors.sensor_types.AccelerometerSensor;

import java.net.SocketAddress;

/**
 * Created by ollie on 24/06/2016.
 */
public class NIME2016DriveByTransitVanComposition implements HBAction {

    //different sample sets
    final String TALK = "talk";
    final String DNB = "dnb";
    final String AMEN = "amen";
    final String RAGGA = "ragga";

    String sampleGroup = AMEN;

    enum RepeatMode {ONE_HIT, REPEAT, NONE}
    RepeatMode repeatMode = RepeatMode.REPEAT;
    enum SampleSelect {SPECIFIC, ID, RANDOM}
    SampleSelect sampleSelect = SampleSelect.SPECIFIC;

    int specificSampleSelect;
    boolean newSample = false;

    float mashupLevel = 0f;

    final float SOURCE_BPM = 170;
    final float SOURCE_INTERVAL = 60000 / 170;

    //strength of user input effects to different elements
    float rateModEffect = 0f;
    float pitchBendEffect = 0f;
    float radioNoiseEffect = 0f;
    float flangeDelayEffect = 0f;
    float joltSnubEffect = 0f;

    //audio stuff

    //sample
    UGen grainCrossFade, sampleGain;
    UGen baseRate, rateMod, rateModAmount;
    UGen basePitch, pitchMod, pitchModAmount;

    //FM synth
    UGen fmBaseFreq, fmModFreqMult, fmModAmount;

    //noise FM mix
    UGen filtFreq, filtQ, filtGain, noiseGain, fmGain;

    //delay
    UGen delayFeedback, delayInputGain, delayRateMult;

    //tremolo
    UGen tremoloAmount, tremoloRateMult;

    public void action(HB hb) {
        //reset
        hb.reset();
        hb.masterGainEnv.setValue(0.1f);
        //load samples
        SampleManager.group(TALK, "data/audio/NIME2016/talk");
        SampleManager.group(DNB, "data/audio/NIME2016/dnb");
        SampleManager.group(AMEN, "data/audio/NIME2016/amen");
        SampleManager.group(RAGGA, "data/audio/NIME2016/ragga");
        //other setups
        setupAudio(hb);
        setupNetworkListener(hb);
//        setupSensorListener(hb);

        //test sound
        Gain g = new Gain(hb.ac, 1, 0.1f);
        g.addInput(new WavePlayer(hb.ac, 500, Buffer.SINE));
//        hb.sound(g);
    }

    private void setupAudio(HB hb) {
        //sample controls - initiate all the controls we will use to control the sample
        grainCrossFade = new Glide(hb.ac, 0);  //1 = fully granular, 0 = fully non-granular
        sampleGain = new Glide(hb.ac, 1);
        baseRate = new Glide(hb.ac, 1);
        rateMod = new Glide(hb.ac);
        rateModAmount = new Glide(hb.ac);
        basePitch = new Glide(hb.ac, 1);
        pitchMod = new Glide(hb.ac);
        pitchModAmount = new Glide(hb.ac);
        //sample stuff - set up the sample players
        sp = new SamplePlayer(hb.ac, SampleManager.fromGroup(sampleGroup, 0));
        gsp = new GranularSamplePlayer(hb.ac, SampleManager.fromGroup(sampleGroup, 0));
        sp.setKillOnEnd(false);
        gsp.setKillOnEnd(false);
        //connect up sample system
        Function sampleMix = new Function(gsp, sp, grainCrossFade) {
            public float calculate() {
                return x[0] * x[2] + x[1] * (1f - x[2]);
            }
        };
        Function rate = new Function(baseRate, rateMod, rateModAmount) {
            public float calculate() {
                return x[0] + x[1] * x[2];
            }
        };
        sp.setRate(rate);
        gsp.setRate(rate);
        Function pitch = new Function(basePitch, pitchMod, pitchModAmount) {
            public float calculate() {
                return x[0] + x[1] * x[2];
            }
        };
        gsp.setPitch(pitch);
        gsp.setGrainSize(grainSize);
        gsp.setGrainInterval(grainInterval);
        gsp.setRandomness(grainRandomness);
        //noise controls
        filtFreq = new Glide(hb.ac, 1000);
        filtQ = new Glide(hb.ac, 1);
        filtGain = new Glide(hb.ac, 1);
        noiseGain = new Glide(hb.ac);
        fmGain = new Glide(hb.ac);
        //FM synth + noise
        WavePlayer fmMod = new WavePlayer(hb.ac, 0, Buffer.SINE);
        WavePlayer fmCarrier = new WavePlayer(hb.ac, 0, Buffer.SINE);
        Noise n = new Noise(hb.ac);
        //create FM synth and connect up FM + noise system
        fmMod.setFrequency(new Function(fmBaseFreq, fmModFreqMult) {
            public float calculate() {
                return (x[0] * x[1]);
            }
        });
        fmCarrier.setFrequency(new Function(fmBaseFreq, fmMod, fmModAmount) {
            public float calculate() {
                return x[0] + (x[1] * x[2]);
            }
        });
        Gain fmMix = new Gain(hb.ac, 1, fmGain);
        fmMix.addInput(fmCarrier);
        Gain noiseMix = new Gain(hb.ac, 1, noiseGain);
        noiseMix.addInput(n);
        //create the filter
        BiquadFilter f = new BiquadFilter(hb.ac, 1);
        f.setFrequency(filtFreq);
        f.setQ(filtQ);
        f.setGain(filtGain);
//        f.addInput(fmMix);            //FM a bit too hard to handle!
        f.addInput(noiseMix);
        //delay controls
        delayFeedback = new Glide(hb.ac, 0);
        delayInputGain = new Glide(hb.ac, 1);
        delayRateMult = new Glide(hb.ac, 1);
        //delay
        TapIn tin = new TapIn(hb.ac, 10000);
        TapOut tout = new TapOut(hb.ac, tin, new Function(rate, delayRateMult) {
            public float calculate() {
                return x[1] * SOURCE_INTERVAL / x[0];
            }
        });
        Gain delayInput = new Gain(hb.ac, 1, delayInputGain);
        Gain delayReturn = new Gain(hb.ac, 1, delayFeedback);
        tin.addInput(delayInput);
        tin.addInput(delayReturn);
        delayReturn.addInput(tout);
        delayInput.addInput(f);
        delayInput.addInput(sampleMix);
        //tremolo controls
        tremoloAmount = new Glide(hb.ac);
        tremoloRateMult = new Glide(hb.ac);
        //tremolo
        WavePlayer lfo = new WavePlayer(hb.ac, 0, Buffer.SINE);
        lfo.setFrequency(new Function(rate, tremoloRateMult) {
            public float calculate() {
                return x[1] * SOURCE_INTERVAL / x[0];
            }
        });
        Gain tremolo = new Gain(hb.ac, 1, new Function(lfo, tremoloAmount) {
            public float calculate() {
                return (x[0] * 0.5f + 1f) * x[1] + (1f - x[1]);
            }
        });
        tremolo.addInput(delayReturn);
        tremolo.addInput(f);
        tremolo.addInput(sampleMix);
        //final plumbing - connect it all to ouput
        hb.ac.out.addInput(tremolo);
//        hb.ac.out.addInput(sp);
        //set up clock to trigger samples
        hb.clockInterval.setValue(SOURCE_INTERVAL);
        hb.pattern(new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                if(hb.clock.getCount() % 64 == 0) {
                    //trigger a new break
                    Sample s = null;
                    switch (sampleSelect) {
                        case SPECIFIC:
                            if(newSample) {
                                s = SampleManager.fromGroup(sampleGroup, specificSampleSelect);
                            }
                            break;
                        case ID_ONCE:
                            if(newSample) {
                                s = SampleManager.fromGroup(sampleGroup, hb.myIndex() + specificSampleSelect);
                            }
                            break;
                        case RANDOM_ONCE:
                            if(newSample) {
                                s = SampleManager.randomFromGroup(sampleGroup);
                            }
                            break;
                        case RANDOM_REPEAT:
                            s = SampleManager.randomFromGroup(sampleGroup);
                            break;
                    }
                    if(s != null) {
                        sp.setSample(s);
                        gsp.setSample(s);
                        newSample = false;
                    }
                    sp.reset();
                    gsp.reset();

                    //TODO choose playback position?
                    //TODO set up any mash actions (depends on mashupLevel)




                }
            }
        });
    }

    private void setupNetworkListener(HB hb) {
        hb.controller.addListener(new OSCListener() {
            @Override
            public void messageReceived(OSCMessage msg, SocketAddress sender, long time) {
                System.out.println("Received message " + msg.getName());
                if (msg.getName().equals("/pitchBendEffect")) {
                    pitchBendEffect = (float) msg.getArg(0);
                    pitchModAmount.setValue(pitchBendEffect);
                } else if (msg.getName().equals("/rateModEffect")) {
                    rateModEffect = (float) msg.getArg(0);
                    rateModAmount.setValue(rateModEffect);
                } else if (msg.getName().equals("/radioNoseEffect")) {
                    radioNoiseEffect = (float) msg.getArg(0);
                } else if (msg.getName().equals("/flangeDelayEffect")) {
                    flangeDelayEffect = (float) msg.getArg(0);
                    delayFeedback.setValue(flangeDelayEffect);
                } else if (msg.getName().equals("/joltSnubEffect")) {
                    joltSnubEffect = (float) msg.getArg(0);
                } else if (msg.getName().equals("/repeatMode")) {
                    repeatMode = RepeatMode.values()[(int) msg.getArg(0)];
                } else if (msg.getName().equals("/sampleSelect")) {
                    sampleSelect = SampleSelect.values()[(int) msg.getArg(0)];
                } else if (msg.getName().equals("/sampleGroup")) {
                    sampleGroup = ((String) msg.getArg(0)).trim().toUpperCase();
                } else if (msg.getName().equals("/mashupLevel")) {
                    mashupLevel = (float) msg.getArg(0);
                } else if (msg.getName().equals("/synchBreaks")) {
                    hb.clock.reset();                                       //TODO test synch
                }
            }
        });
    }

    private void setupSensorListener(HB hb) {
//        hb.getSensor("MiniMu");
//        if(!hb.sensors.containsKey("MiniMu")) {
//            try {
//                hb.sensors.put("MiniMu", new MiniMU());
//            } catch (Exception e) {
//                System.out.println("Cannot create MiniMu sensor");
//                hb.setStatus("No MinMu available.");
//                return;
//            }
//        }
        hb.getSensor(MiniMU.class).addListener(new SensorUpdateListener() {
            @Override
            public void sensorUpdated() {
                double[] accel = ((AccelerometerSensor)hb.getSensor(MiniMU.class)).getAccelerometerData();
                sensor((float)accel[0] / 12000f,(float)accel[1] / 12000f,(float)accel[2] / 12000f);
            }
        });
//        @Override
//            public void accelData(double x, double y, double z) {
//               sensor((float)x / 12000f,(float)y / 12000f,(float)z / 12000f);
//            }
//        });
    }

    private void mashItUp() {
        //reset all the envelopes (sampleGain, baseRate, basePitch, grainSize, grainInterval, grainRandomness, noiseGain
//        baseRate.clear();
//        basePitch.clear();
        grainSize.clear();
        grainInterval.clear();
        grainRandomness.clear();
        delayFeedback.clear();
        delayRateMult.clear();
        delayInputGain.clear();
        //Now sample is selected (or might be dead).
        //Time to mash.
        //use mashup level
        if(mashupLevel > 0) {
            //regular mash
            mashA();
        } else {
            //no mash - reset
            unmash();
        }
    }

    //Mash variables
    // -- tremoloRateMult
    // -- tremoloAmount
    // -- baseRate
    // -- basePitch
    // -- grainSize
    // -- grainInterval
    // -- grainRandomness
    // -- delayRateMult
    // -- delayFeedback

    private void mashA() {  //regular
        int extremity = hb.rng.nextInt((int)(mashupLevel * 5) + 1);
        ////////////set base rate
        if(hb.rng.nextFloat() < mashupLevel) {
            float dir = 1;
            if(hb.rng.nextFloat() < 0.2f) {
                dir = -1;
                sp.setPosition(sp.getSample().getLength());
                gsp.setPosition(gsp.getSample().getLength());
            }
            float rate = dir * rand(LOW_MULTS);
            baseRate.addSegment(rate, 0);
            //possible snub
            if(hb.rng.nextFloat() < 0.2f) {
                baseRate.addSegment(rate, 1000);
                baseRate.addSegment(-0.1f, hb.rng.nextFloat() * 2000);
            }
        } else {
            baseRate.setValue(defaultBaseRate);
        }
        //set base pitch
        if(hb.rng.nextFloat() < mashupLevel) {
            float dir = 1;
            if(hb.rng.nextFloat() < 0.5f) {
                dir = -1;
                sp.setPosition(sp.getSample().getLength());
            }
            float rate = dir * rand(MULTS);
            basePitch.addSegment(rate, 0);
            //possible snub
            if(hb.rng.nextFloat() < 0.2f) {
                basePitch.addSegment(rate, 1000);
                basePitch.addSegment(-0.1f, rand(0, 2000));
            }
        } else {
            basePitch.setValue(defaultBasePitch);
        }
        //grain
        if(hb.rng.nextFloat() < mashupLevel) {
            float grainInt = rand(10,100);
            float grainSizeMult = rand(0.5f, 2);
            grainSize.addSegment(grainInt * grainSizeMult, rand(10,2000));
            grainInterval.addSegment(grainInt, rand(10,2000));
            grainRandomness.setValue(prob(0.4f) ? rand(0.01f, 0.5f) : 0);
        } else {
            grainInterval.setValue(40);
            grainSize.setValue(60);
            grainRandomness.setValue(0.001f);
        }
        //tremolo
        tremoloRateMult.setValue(rand(MULTS));
        if(hb.rng.nextFloat() < mashupLevel) {
            tremoloAmount.setValue(prob(0.3f) ? rand(0.5f, 1) : 0);
        } else {
            tremoloAmount.setValue(0);
        }
        //delay
        delayRateMult.addSegment(prob(0.5f) ? rand(TINY_DIVS): rand(MULTS), rand(0, 2000));
        if(hb.rng.nextFloat() < mashupLevel) {
            delayInputGain.addSegment(prob(0.1f) ? rand(0.5f, 1) : 0, rand(0, 2000));
            delayInputGain.addSegment(0, rand(0, 2000));

        } else {
            delayInputGain.setValue(0);
        }
    }

    private float rand(float[] x) {
        return x[hb.rng.nextInt(x.length)];
    }

    private boolean prob(float prob) {
        return hb.rng.nextFloat() < prob;
    }

    private void unmash() {
        tremoloAmount.setValue(0);
        baseRate.setValue(defaultBaseRate);
        basePitch.setValue(defaultBasePitch);
        grainSize.setValue(60);
        grainInterval.setValue(40);
        grainRandomness.setValue(0.001f);
        delayInputGain.setValue(0);
    }

    private void sensor(float x, float y, float z) {
//                System.out.println("Sensor data: " + x + " " + y + " " + z);
        float rms = (float) Math.sqrt(x * x + y * y + z * z);
        //the following ones always happen...
        //pitchBendEffect
        pitchMod.setValue(x); //<--- TODO calibrate to 0.9-1.1
        //rateModEffect
        rateMod.setValue(y);  //<--- TODO calibrate to 0.5-2
        //radioNoiseEffect
        filtFreq.setValue(Math.abs(x) * 20000 + 30); //<--- TODO calibrate to 500-15,000
        filtQ.setValue(Math.min(2, Math.abs(y) * 100));    //<--- TODO calibrate to ??
        filtGain.setValue(Math.min(1, Math.abs(z) * 100)); //<--- TODO calibrate to ??
        //the following noise effect sometimes happens, depending on noiseEffect
        float sampleProx = Math.min(1, Math.abs(x) * 100f);        //<--- TODO calibrate to 0-1
        float sampleGainVal = 1f - (noiseEffect * sampleProx);
        sampleGain.setValue((float) Math.sqrt(sampleGainVal));
        noiseGain.setValue(Math.min(0.8f, (float) Math.sqrt(1f - sampleGainVal)));
        //check for a big event
        float thresh = 0.5f;  //<--- TODO calibrate
        if (rms > thresh && timeSinceLastTrigger > 100) {
            trigger();
            timeSinceLastTrigger = 0;
        }
        timeSinceLastTrigger++;
    }

    private float rand(float low, float high) {
        return hb.rng.nextFloat() * (high-low) + low;
    }

    private void trigger() {
        System.out.println("Trigger");
        //flangeDelayEffect
        float realFlangeDelayEffect = flangeDelayEffect * 1f; //<--- TODO calibrate
        if(realFlangeDelayEffect > 0) {
            //clear all envelopes
            grainSize.clear();
            grainInterval.clear();
            grainRandomness.clear();
            delayFeedback.clear();
            delayRateMult.clear();
            delayInputGain.clear();
            //envelopes - do stuff to delayRateMult and delayFeedback (takeover and run envelopes)
            //delay spike, dependent on flange
            delayInputGain.setValue(realFlangeDelayEffect);
            delayInputGain.addSegment(realFlangeDelayEffect, 1000);
            delayInputGain.addSegment(0, 50);
            int elements = hb.rng.nextInt(3);
            for (int i = 0; i < elements; i++) {
                delayRateMult.addSegment(rand(TINY_DIVS), rand(10,2000));
            }
            delayFeedback.setValue(0.85f);  //fix this?
        }
        //joltSnubEffect
        float realJoltSnubEffect = joltSnubEffect * 1f; //<--- TODO calibrate
        //do stuff to baseRate and basePitch, together (takeover and run envelopes)
        if(realJoltSnubEffect > 0) {
            baseRate.clear();
            basePitch.clear();
            baseRate.addSegment(-0.1f, 1000);
            basePitch.addSegment(-0.1f, 1501);
        }
    }

}
