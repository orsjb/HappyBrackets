package net.happybrackets.compositions;

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

/**
 * Created by ollie on 24/06/2016.
 */
public class NIME2016DriveByTransitVanComposition implements HBAction {

    //different sample sets
    final String TALK = "talk";
    final String DNB = "dnb";
    final String AMEN = "amen";
    final String RAGGA = "ragga";

    String sampleGroup = TALK;

    enum RepeatMode {ONE_HIT, REPEAT_SAME, REPEAT_CHANGE}
    RepeatMode repeatMode = RepeatMode.ONE_HIT;
    enum SampleSelect {SPECIFIC, ID_ONCE, RANDOM_ONCE, RANDOM_REPEAT}
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
        //load samples
        SampleManager.group(TALK, "data/audio/NIME2016/talk");
        SampleManager.group(DNB, "data/audio/NIME2016/dnb");
        SampleManager.group(AMEN, "data/audio/NIME2016/amen");
        SampleManager.group(RAGGA, "data/audio/NIME2016/ragga");
        //other setups
        setupAudio(hb);
        setupNetworkListener(hb);
        setupSensorListener(hb);
    }

    private void setupAudio(HB hb) {
        //sample controls - initiate all the controls we will use to control the sample
        grainCrossFade = new Glide(hb.ac);  //1 = fully granular, 0 = fully non-granular
        sampleGain = new Glide(hb.ac);
        baseRate = new Glide(hb.ac);
        rateMod = new Glide(hb.ac);
        rateModAmount = new Glide(hb.ac);
        basePitch = new Glide(hb.ac);
        pitchMod = new Glide(hb.ac);
        pitchModAmount = new Glide(hb.ac);
        //sample stuff - set up the sample players
        SamplePlayer sp = new SamplePlayer(hb.ac, SampleManager.fromGroup(TALK, 0));
        GranularSamplePlayer gsp = new GranularSamplePlayer(hb.ac, SampleManager.fromGroup(TALK, 0));
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
        //FM + noise controls
        fmBaseFreq = new Glide(hb.ac);
        fmModFreqMult = new Glide(hb.ac);
        fmModAmount = new Glide(hb.ac);
        filtFreq = new Glide(hb.ac);
        filtQ = new Glide(hb.ac);
        filtGain = new Glide(hb.ac);
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
        f.addInput(fmMix);
        f.addInput(noiseMix);
        //delay controls
        delayFeedback = new Glide(hb.ac);
        delayInputGain = new Glide(hb.ac);
        delayRateMult = new Glide(hb.ac);
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
        tremolo.addInput(tout);
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
        hb.controller.addListener(msg -> {
            if(msg.getName().equals("/pitchBendEffect")) {
                pitchBendEffect = (float) msg.getArg(0);
                pitchModAmount.setValue(pitchBendEffect);
            } else if(msg.getName().equals("/rateModEffect")) {
                rateModEffect = (float) msg.getArg(0);
                rateModAmount.setValue(rateModEffect);
            } else if(msg.getName().equals("/radioNoseEffect")) {
                radioNoiseEffect = (float) msg.getArg(0);
            } else if(msg.getName().equals("/flangeDelayEffect")) {
                flangeDelayEffect = (float) msg.getArg(0);
            } else if(msg.getName().equals("/joltSnubEffect")) {
                joltSnubEffect = (float) msg.getArg(0);
            } else if(msg.getName().equals("/repeatMode")) {
                repeatMode = RepeatMode.values()[(int) msg.getArg(0)];
            } else if(msg.getName().equals("/sampleSelect")) {
                sampleSelect = SampleSelect.values()[(int) msg.getArg(0)];
            } else if(msg.getName().equals("/sampleGroup")) {
                sampleGroup = ((String) msg.getArg(0)).trim().toUpperCase();
            } else if(msg.getName().equals("/mashupLevel")) {
                mashupLevel = (float) msg.getArg(0);
            } else if(msg.getName().equals("/synchBreaks")) {
                hb.clock.reset();                                       //TODO test synch
            }
        });
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
                float rms = (float) Math.sqrt(x * x + y * y + z * z);
                //pitchBendEffect
                pitchMod.setValue((float) x);  //<--- TODO calibrate
                //rateModEffect
                rateMod.setValue((float) x);  //<--- TODO calibrate
                //radioNoiseEffect
                //TODO


                //check for a big event
                float thresh = 10;  //<--- TODO calibrate
                if (rms > thresh) {
                    //flangeDelayEffect
                    float realFlangeDelayEffect = (rms - thresh) * flangeDelayEffect * 1f; //<--- TODO calibrate
                    //TODO - envelopes
                    //joltSnubEffect
                    float realJoltSnubEffect = (rms - thresh) * joltSnubEffect * 1f; //<--- TODO calibrate
                    //TODO
                }
            }
        });
    }

}
