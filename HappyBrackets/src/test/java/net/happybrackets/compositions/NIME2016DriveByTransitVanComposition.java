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
import net.happybrackets.device.sensors.MiniMU;

import java.net.SocketAddress;

/**
 * Created by ollie on 24/06/2016.
 */
public class NIME2016DriveByTransitVanComposition implements HBAction {

    final boolean DUMMY_SENSORS = true; //for debugging when no sensors are available

    final float[] TINY_DIVS = {1/8f,1/16f,1/32f,1/64f,1/128f};
    final float[] MULTS = {0.25f, 0.5f, 0.5f, 1, 1, 1, 1.5f, 1.5f, 2f, 3f};
    final float[] LOW_MULTS = {0.125f, 0.25f, 0.5f, 0.5f, 1, 1, 1.5f};

    //different sample sets
    final String TALK = "talk";
    final String DNB = "dnb";
    final String AMEN = "amen";
    final String RAGGA = "ragga";

    String sampleGroup = TALK;

    enum RepeatMode {ONE_HIT, REPEAT, NONE}
    RepeatMode repeatMode = RepeatMode.REPEAT;
    enum SampleSelect {SPECIFIC, ID, RANDOM}
    SampleSelect sampleSelect = SampleSelect.SPECIFIC;

    int specificSampleSelect;
    boolean newSample = false;

    float mashupLevel = 0f;
    float noiseEffect = 0f;
    float defaultBaseRate = 1;
    float defaultBasePitch = 1;

    final float SOURCE_BPM = 170;
    final float SOURCE_INTERVAL = 60000 / SOURCE_BPM;

    //strength of user input effects to different elements
    float flangeDelayEffect = 0f;
    float joltSnubEffect = 0f;

    HB hb;

    int timeSinceLastTrigger = 0;

    //audio stuff
    SamplePlayer sp;
    GranularSamplePlayer gsp;

    //sample
    UGen grainCrossFade;
    Glide sampleGain;
    Envelope baseRate, basePitch;
    Envelope grainSize, grainInterval, grainRandomness;
    UGen rateMod, rateModAmount;
    UGen pitchMod, pitchModAmount;

    //noise mix
    UGen filtFreq, filtQ, filtGain;
    Glide noiseGain;

    //delay
    Envelope delayFeedback, delayInputGain, delayRateMult;

    //tremolo
    UGen tremoloAmount, tremoloRateMult;

    public void action(HB hb) {
        //reset
        this.hb = hb;
        hb.reset();
        //load samples
        SampleManager.group(TALK, "data/audio/NIME2016/talk");
        SampleManager.group(DNB, "data/audio/NIME2016/dnb");
        SampleManager.group(AMEN, "data/audio/NIME2016/amen");
        SampleManager.group(RAGGA, "data/audio/NIME2016/ragga");
        //other setups
        setupAudio(hb);
        setupNetworkListener(hb);

        if(DUMMY_SENSORS) {
            dummySensor();
        } else {
            setupSensorListener(hb);
        }

        //test sound
        Gain g = new Gain(hb.ac, 1, 0.1f);
        g.addInput(new WavePlayer(hb.ac, 500, Buffer.SINE));
//        hb.sound(g);
    }

    private void setupAudio(HB hb) {
        //sample controls - initiate all the controls we will use to control the sample
        grainCrossFade = new Glide(hb.ac, 0);  //1 = fully granular, 0 = fully non-granular
        sampleGain = new Glide(hb.ac, 1);
        baseRate = new Envelope(hb.ac, 1);
        rateMod = new Glide(hb.ac);
        rateModAmount = new Glide(hb.ac);
        basePitch = new Envelope(hb.ac, 1);
        pitchMod = new Glide(hb.ac);
        pitchModAmount = new Glide(hb.ac);
        grainSize = new Envelope(hb.ac, 60);
        grainInterval = new Envelope(hb.ac, 40);
        grainRandomness = new Envelope(hb.ac, 0);
        //sample stuff - set up the sample players
        sp = new SamplePlayer(hb.ac, SampleManager.fromGroup(TALK, 0));
        gsp = new GranularSamplePlayer(hb.ac, SampleManager.fromGroup(TALK, 0));
        sp.setKillOnEnd(false);
        gsp.setKillOnEnd(false);
        //connect up sample system
        Function sampleMix = new Function(gsp, sp, grainCrossFade, sampleGain) {
            public float calculate() {
                return (x[0] * x[2] + x[1] * (1f - x[2])) * x[3];
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
        filtFreq = new Glide(hb.ac, 500);
        filtQ = new Glide(hb.ac, 1);
        filtGain = new Glide(hb.ac, 1);
        noiseGain = new Glide(hb.ac, 0);
        //noise
        Noise n = new Noise(hb.ac);
        Gain noiseMix = new Gain(hb.ac, 1, noiseGain);
        noiseMix.addInput(n);
        //create the filter
        BiquadFilter f = new BiquadFilter(hb.ac, 1);
        f.setFrequency(filtFreq);
        f.setQ(filtQ);
        f.setGain(filtGain);
        f.addInput(noiseMix);
        //delay controls
        delayFeedback = new Envelope(hb.ac, 0);
        delayInputGain = new Envelope(hb.ac, 1);
        delayRateMult = new Envelope(hb.ac, 1);
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
        //set up clock to trigger samples
        hb.clockInterval.setValue(SOURCE_INTERVAL);
        hb.pattern(new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                if(hb.clock.getCount() % 256 == 0) {
                    switch (repeatMode) {
                        case ONE_HIT:           //play the break once then stop
                            loadNextSample();
                            resetSamplePlayers();
                            repeatMode = RepeatMode.NONE;
                            break;
                        case REPEAT:            //keep playing
                            loadNextSample();
                            resetSamplePlayers();
                            break;
                        case NONE:              //do nothing
                            break;
                    }
                    mashItUp();
                }
            }
        });
    }

    private void loadNextSample() {
        Sample s = null;
        switch (sampleSelect) {
            case SPECIFIC:
                if(newSample) {
                    s = SampleManager.fromGroup(sampleGroup, specificSampleSelect);
                }
                break;
            case ID:
                if(newSample) {
                    s = SampleManager.fromGroup(sampleGroup, hb.myIndex() + specificSampleSelect);
                }
                break;
            case RANDOM:
                s = SampleManager.randomFromGroup(sampleGroup);
                break;
            default:
                break;
        }
        if(s != null) {
            sp.setSample(s);
            gsp.setSample(s);
            newSample = false;
        }
    }

    private void resetSamplePlayers() {
        sp.reset();
        gsp.reset();
    }

    private void setupNetworkListener(HB hb) {
        hb.controller.addListener(new OSCListener() {
            @Override
            public void messageReceived(OSCMessage msg, SocketAddress sender, long time) {
                System.out.println("Received message " + msg.getName());
                if (msg.getName().equals("/pitchBendEffect")) {             //amount sensors influence pitch bend
                    pitchModAmount.setValue(hb.getFloatArg(msg, 0));
                } else if (msg.getName().equals("/rateModEffect")) {        //amount sensors influence rate
                    rateModAmount.setValue(hb.getFloatArg(msg, 0));
                } else if (msg.getName().equals("/noiseEffect")) {          //amount sensors influence noise
                    noiseEffect = hb.getFloatArg(msg, 0);
                } else if (msg.getName().equals("/flangeDelayEffect")) {    //amount jolts influence delay
                    flangeDelayEffect = hb.getFloatArg(msg, 0);
                } else if (msg.getName().equals("/joltSnubEffect")) {       //amount jolts influence snubs
                    joltSnubEffect = hb.getFloatArg(msg, 0);
                } else if (msg.getName().equals("/repeatMode")) {           //what type of playback, see RepeatMode options
                    repeatMode = RepeatMode.values()[(int) msg.getArg(0)];
                    newSample = true;
                } else if (msg.getName().equals("/sampleSelect")) {         //how sample is chosen
                    sampleSelect = SampleSelect.values()[(int) msg.getArg(0)];
                    newSample = true;
                } else if (msg.getName().equals("/sampleChoice")) {         //what sample is chosen
                    specificSampleSelect = (int)msg.getArg(0);
                    newSample = true;
                } else if (msg.getName().equals("/sampleGroup")) {          //what sample group
                    sampleGroup = ((String) msg.getArg(0)).trim().toLowerCase();
                    newSample = true;
                } else if (msg.getName().equals("/mashupLevel")) {          //amount of mashup from 0-1
                    mashupLevel = hb.getFloatArg(msg, 0);
                } else if (msg.getName().equals("/baseRate")) {             //the base rate
                    defaultBaseRate = hb.getFloatArg(msg, 0);
                } else if (msg.getName().equals("/basePitch")) {             //the base pitch
                    defaultBasePitch = hb.getFloatArg(msg, 0);
                } else if (msg.getName().equals("/synchBreaks")) {          //message to synch clocks
                    hb.clock.reset();
                } else if (msg.getName().equals("/trigger")) {              //dummy to simulate trigger
                    trigger();
                } else if(msg.getName().equals("/nograin")) {               //set to no grain
                    grainCrossFade.setValue(0);
                } else if(msg.getName().equals("/grain")) {                 //set to grain
                    grainCrossFade.setValue(1);
                }
            }
        });
    }

    private void dummySensor() {
        new Thread() {
            float x,y,z;
            int count = 0;
            public void run() {
                while(true) {
                    x = (float)Math.sin(count * 1.0);
                    y = (float)Math.sin(count * 1.1);
                    z = (float)Math.sin(count * 1.2);
                    sensor(x,y,z);
                    count++;
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    private void setupSensorListener(HB hb) {
        if(!hb.sensors.containsKey("MiniMu")) {
            try {
                hb.sensors.put("MiniMu", new MiniMU());
            } catch (Exception e) {
                System.out.println("Cannot create MiniMu sensor");
                hb.setStatus("No MinMu available.");
                return;
            }
        }
        hb.sensors.get("MiniMu").addListener(new MiniMU.MiniMUListener() {
            @Override
            public void accelData(double x, double y, double z) {
               sensor((float)x,(float)y,(float)z);
            }
        });
    }

    private void mashItUp() {
        //reset all the envelopes (sampleGain, baseRate, basePitch, grainSize, grainInterval, grainRandomness, noiseGain
        baseRate.clear();
        basePitch.clear();
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
        int extremity = hb.rng.nextInt((int)(mashupLevel * 5));
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
            baseRate.setValue(1);
        }
        //set base pitch
        if(hb.rng.nextFloat() < mashupLevel) {
            float dir = 1;
            if(hb.rng.nextFloat() < 0.2f) {
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
            basePitch.setValue(1);
        }
        //grain
        if(hb.rng.nextFloat() < mashupLevel) {
            float grainInt = rand(10,100);
            float grainSizeMult = rand(0.5f, 4);
            grainSize.addSegment(grainInt * grainSizeMult, rand(10,2000));
            grainInterval.addSegment(grainInt, rand(10,2000));
            grainRandomness.setValue(prob(0.2f) ? rand(0.01f, 0.1f) : 0);
        } else {
            grainInterval.setValue(40);
            grainSize.setValue(60);
            grainRandomness.setValue(0.001f);
        }
        //tremolo
        tremoloRateMult.setValue(rand(MULTS));
        if(hb.rng.nextFloat() < mashupLevel) {
            tremoloAmount.setValue(prob(0.1f) ? rand(0.5f, 1) : 0);
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
        float rms = (float) Math.sqrt(x * x + y * y + z * z);
        //the following ones always happen...
        //pitchBendEffect
        pitchMod.setValue(x); //<--- TODO calibrate to 0.9-1.1
        //rateModEffect
        rateMod.setValue(y);  //<--- TODO calibrate to 0.5-2
        //radioNoiseEffect
        filtFreq.setValue(x); //<--- TODO calibrate to 500-15,000
        filtQ.setValue(y);    //<--- TODO calibrate to ??
        filtGain.setValue(z); //<--- TODO calibrate to ??
        //the following noise effect sometimes happens, depending on noiseEffect
        float sampleProx = x;        //<--- TODO calibrate to 0-1
        float sampleGainVal = 1f - (noiseEffect * sampleProx);
        sampleGain.setValue((float) Math.sqrt(sampleGainVal));
        noiseGain.setValue((float) Math.sqrt(1f - sampleGainVal));
        //check for a big event
        float thresh = 10;  //<--- TODO calibrate
        if (rms > thresh && timeSinceLastTrigger > 10) {
            trigger();
            timeSinceLastTrigger = 0;
        }
        timeSinceLastTrigger++;
    }

    private float rand(float low, float high) {
        return hb.rng.nextFloat() * (low-high) + low;
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
            delayInputGain.addSegment(realFlangeDelayEffect, 500);
            delayInputGain.addSegment(0, 50);
            int elements = hb.rng.nextInt(3);
            for (int i = 0; i < elements; i++) {
                delayRateMult.addSegment(rand(TINY_DIVS), rand(10,2000));
            }
            delayFeedback.setValue(0.86f);  //fix this?
        }
        //joltSnubEffect
        float realJoltSnubEffect = joltSnubEffect * 1f; //<--- TODO calibrate
        //do stuff to baseRate and basePitch, together (takeover and run envelopes)
        if(realJoltSnubEffect > 0) {
            baseRate.clear();
            basePitch.clear();
            baseRate.addSegment(0, 1000);
            basePitch.addSegment(0, 1000);
        }
    }

}
