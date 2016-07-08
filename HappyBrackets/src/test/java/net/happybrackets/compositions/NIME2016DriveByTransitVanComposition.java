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

    final float SOURCE_BPM = 170;
    final float SOURCE_INTERVAL = 60000 / 170;

    //strength of user input effects to different elements
    float flangeDelayEffect = 0f;
    float joltSnubEffect = 0f;

    HB hb;

    //audio stuff
    SamplePlayer sp;
    GranularSamplePlayer gsp;

    //sample
    UGen grainCrossFade;
    Envelope sampleGain;
    Envelope baseRate, basePitch;
    Envelope grainSize, grainInterval, grainRandomness;
    UGen rateMod, rateModAmount;
    UGen pitchMod, pitchModAmount;

    //noise mix
    UGen filtFreq, filtQ, filtGain;
    Envelope noiseGain;

    //delay
    UGen delayFeedback, delayInputGain, delayRateMult;

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
        sampleGain = new Envelope(hb.ac, 1);
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
        filtFreq = new Glide(hb.ac, 500);
        filtQ = new Glide(hb.ac, 1);
        filtGain = new Glide(hb.ac, 1);
        noiseGain = new Envelope(hb.ac);
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
        //set up clock to trigger samples
        hb.clockInterval.setValue(SOURCE_INTERVAL);
        hb.pattern(new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                if(hb.clock.getCount() % 64 == 0) {
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
                    pitchModAmount.setValue((float) msg.getArg(0));
                } else if (msg.getName().equals("/rateModEffect")) {        //amount sensors influence rate
                    rateModAmount.setValue((float) msg.getArg(0));
                } else if (msg.getName().equals("/flangeDelayEffect")) {       //amount sensors influence jolty snubs
                    flangeDelayEffect = (float) msg.getArg(0);
                } else if (msg.getName().equals("/joltSnubEffect")) {       //amount sensors influence jolty snubs
                    joltSnubEffect = (float) msg.getArg(0);
                } else if (msg.getName().equals("/repeatMode")) {           //what type of playback, see RepeatMode options
                    repeatMode = RepeatMode.values()[(int) msg.getArg(0)];
                    newSample = true;
                } else if (msg.getName().equals("/sampleSelect")) {         //what sample is chosen
                    sampleSelect = SampleSelect.values()[(int) msg.getArg(0)];
                    newSample = true;
                } else if (msg.getName().equals("/sampleGroup")) {          //what sample group
                    sampleGroup = ((String) msg.getArg(0)).trim().toUpperCase();
                    newSample = true;
                } else if (msg.getName().equals("/mashupLevel")) {          //amount of mashup from 0-1
                    mashupLevel = (float) msg.getArg(0);
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
        sampleGain.clear();
        baseRate.clear();
        basePitch.clear();
        grainSize.clear();
        grainInterval.clear();
        grainRandomness.clear();

        //Now sample is selected (or might be dead).
        //TODO Time to mash.
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

        //use mashup level

        if(mashupLevel > 0.25) {

        }

    }

    private void sensor(float x, float y, float z) {
        float rms = (float) Math.sqrt(x * x + y * y + z * z);
        //the following ones always happen...
        //pitchBendEffect
        pitchMod.setValue(x); //<--- TODO calibrate
        //rateModEffect
        rateMod.setValue(y);  //<--- TODO calibrate
        //radioNoiseEffect
        filtFreq.setValue(x); //<--- TODO calibrate
        filtQ.setValue(y);    //<--- TODO calibrate
        filtGain.setValue(z); //<--- TODO calibrate
        //the following ones sometimes happen
        //TODO sensors controlling noise / sample mix

        //check for a big event
        float thresh = 10;  //<--- TODO calibrate
        if (rms > thresh) {
            trigger();
        }
    }

    private void trigger() {

        //TODO clear these?...
//        sampleGain.clear();
//        baseRate.clear();
//        basePitch.clear();
//        grainSize.clear();
//        grainInterval.clear();
//        grainRandomness.clear();

        //flangeDelayEffect
        float realFlangeDelayEffect = flangeDelayEffect * 1f; //<--- TODO calibrate
        //TODO - envelopes - do stuff to delayRateMult and delayFeedback (takeover and run envelopes)

        //joltSnubEffect
        float realJoltSnubEffect = joltSnubEffect * 1f; //<--- TODO calibrate
        //TODO - do stuff to baseRate and basePitch (takeover and run envelopes)

    }

}
