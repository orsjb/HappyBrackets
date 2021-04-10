package net.happybrackets.rendererengine;

import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.ugens.*;

/**
 * This render allows you to:
 * load up a database of sounds
 * choose a sound to play
 * choose whether that sound is granular or straight
 */

public class GranularRenderer extends Renderer {

    //the renderer controller
    RendererController rc = RendererController.getInstance();

    //audio objects
    public GranularSamplePlayer gsp;
    public SamplePlayer sp;
    public float grainInterval;
    public Gain g;
    public WavePlayer fmModulator;
    public WavePlayer fm;
    public BiquadFilter bf;
    public UGen theActualOut;
    public float fmDepth = 0;

    //lfo
    public WavePlayer lfo;
    public float lfoDepth;

    //audio controls
    public Glide gain;
    public Glide pitch;

    //other timing params
    public int clockIntervalLock = 0;
    public double clockLockPosition = 0;
    public int clockDelayTicks = 0;

    //light data
    private double[] sparkleD = new double[]{0, 0, 0};
    public double[] rgbD = new double[]{255, 255, 255};
    public double masterBrightness = 0;
    public double pulseBrightness = 1;
    public double decay = 0.7f;
    public double sparkle = 0;

    //id for tracking event objects
    public int timeoutThresh = -1;
    public int timeoutCount = 0;
    boolean audioIsSetup = false;
    long timeOfLastTriggerMS = 0;

    @Override
    public void setupLight() {
        rc.addClockTickListener((offset, this_clock) -> {       //assumes clock is running at 20ms intervals for now
            int beatCount = (int)this_clock.getNumberTicks();
            if (clockIntervalLock > 0 && beatCount % clockIntervalLock == (clockDelayTicks % clockIntervalLock)) {
                triggerBeat(beatCount);
            }
            triggerTick();
        });
    }

    @Override
    public void setupAudio() {
        audioIsSetup = true;
        //construct audio elements
        pitch = new Glide(1);
        gsp = new GranularSamplePlayer(1);
        sp = new SamplePlayer(1);
        gsp.setKillOnEnd(false);
        gsp.setInterpolationType(SamplePlayer.InterpolationType.LINEAR);
        gsp.setLoopType(SamplePlayer.LoopType.NO_LOOP_FORWARDS);
        gsp.getGrainIntervalUGen().setValue(60);
        grainInterval = 60;
        gsp.getGrainSizeUGen().setValue(80);
        gsp.getRandomnessUGen().setValue(0.01f);
        gsp.setPitch(pitch);
        sp.setKillOnEnd(false);
        sp.setLoopCrossFade(2);
        sp.setInterpolationType(SamplePlayer.InterpolationType.LINEAR);
        sp.setLoopType(SamplePlayer.LoopType.NO_LOOP_FORWARDS);
        sp.setPitch(pitch);
        bf = new BiquadFilter(1, BiquadFilter.Type.NOTCH);
        bf.addInput(sp);
        fmModulator = new WavePlayer(0, Buffer.SINE);
        Function f = new Function(fmModulator, pitch) {
            @Override
            public float calculate() {
                return x[0] * fmDepth * x[1] + x[1];
            }
        };
        fm = new WavePlayer(f, Buffer.SINE);
        //set up a clock
        rc.addClockTickListener((offset, this_clock) -> {       //assumes clock is running at 20ms intervals for now
            int beatCount = (int)this_clock.getNumberTicks();
            if (clockIntervalLock > 0 && beatCount % clockIntervalLock == (clockDelayTicks % clockIntervalLock)) {
                triggerBeat(beatCount);
            }
            triggerTick();
        });
        gain = new Glide(0);
        g = new Gain(1, gain);              //TODO add a Function that can listen to the audio power? Would that work?
        useGranularSamplePlayer();
        theActualOut = out;
        theActualOut.addInput(g);
        //lfo stuff
        lfoDepth = 0;
        lfo = new WavePlayer(50, Buffer.SINE);
    }

    public void triggerBeat(int beatCount) {
        lightLoopTrigger();
        if(audioIsSetup) {
            triggerSampleWithOffset(0);
        }
    }

    public void triggerTick() {
        lightUpdate();
        timeoutCount++;
        if(timeoutThresh >= 0 && timeoutCount > timeoutThresh) {
            if(audioIsSetup) {
                gain.setValue(0);
            }
            masterBrightness = 0;
        }
    }

    //light behaviours
    public void lightLoopTrigger() {
        masterBrightness = pulseBrightness;
    }

    public void lightUpdate() {
        //decay
        masterBrightness *= decay;
        //sparkle
        sparkleD[0] = Math.random() * 512 - 256;
        sparkleD[1] = Math.random() * 512 - 256;
        sparkleD[2] = Math.random() * 512 - 256;
        rc.displayColor(this,
                (int)clip(((rgbD[0] + sparkle * sparkleD[0]) * masterBrightness), 0, 255),
                (int)clip(((rgbD[1] + sparkle * sparkleD[1]) * masterBrightness), 0, 255),
                (int)clip(((rgbD[2] + sparkle * sparkleD[2]) * masterBrightness), 0, 255)
        );
    }

    public void setRGB(int r, int g, int b) {
        rgbD[0] = r;
        rgbD[1] = g;
        rgbD[2] = b;
    }

    //audio controls

    public void useGranularSamplePlayer() {
        if(audioIsSetup) {
            g.clearInputConnections();
            g.addInput(gsp);
        }
    }

    public void useRegularSamplePlayer() {
        if(audioIsSetup) {
            g.clearInputConnections();
            g.addInput(bf);
        }
    }

    public void useFMSynth() {
        if(audioIsSetup) {
            g.clearInputConnections();
            g.addInput(fm);
        }
    }

    public void useNoAudioSource() {
        g.clearInputConnections();
    }

    public void setSample(Sample s) {
        if(gsp != null) {
            gsp.setSample(s);
            sp.setSample(s);
        }
    }

    public Sample getCurrentSample() {
        if(gsp != null) {
            return gsp.getSample();
        } else {
            return null;
        }
    }

    public void rate(float rate) {
        if(audioIsSetup) {
            gsp.getRateUGen().setValue(rate);
            sp.getRateUGen().setValue(rate);
        }
    }

    public void grainOverlap(float overlap) {
        if(audioIsSetup) {
            gsp.getGrainSizeUGen().setValue(grainInterval * overlap);
        }
    }

    public void grainInterval(float interval) {
        if(audioIsSetup) {
            gsp.getGrainIntervalUGen().setValue(interval);
            grainInterval = interval;
        }
    }

    public void gain(float gain) {
        if(audioIsSetup) {
            this.gain.setValue(gain);
        }
    }

    public void random(float random) {
        if(audioIsSetup) {
            gsp.getRandomnessUGen().setValue(random);
        }
    }

    public void pitch(float pitchd) {
        if(audioIsSetup) {
            this.pitch.setValue(pitchd);
        }
    }

    public void loopType(SamplePlayer.LoopType type) {
        if(gsp != null) {
            gsp.setLoopType(type);
            sp.setLoopType(type);
        }
    }

    public void loopStart(float start) {
        if(audioIsSetup) {
            gsp.getLoopStartUGen().setValue(start);
            sp.getLoopStartUGen().setValue(start);
        }
    }

    public void loopEnd(float end) {
        if(audioIsSetup) {
            gsp.getLoopEndUGen().setValue(end);
            sp.getLoopEndUGen().setValue(end);
        }
    }

    public void clockInterval(int interval) {
        clockIntervalLock = interval;
    }

    public void clockDelay(int delayTicks) {
        clockDelayTicks = delayTicks;
    }

    public void clockLockPosition(float positionMS) {
        clockLockPosition = positionMS;
    }

    public void lfoFreq(float freq) {
        if(audioIsSetup) {
            lfo.setFrequency(freq);
        }
    }

    public void lfoDepth(float depth) {
        lfoDepth = depth;
    }

    public void lfoWave(Buffer wave) {
        if(audioIsSetup) {
            lfo.setBuffer(wave);
        }
    }

    public void position(double ms) {
        if(audioIsSetup) {
            gsp.setPosition(ms);
            sp.setPosition(ms);
        }
    }

    public void filterFreq(float freq) {
        bf.setFrequency(freq);
    }

    public void filterQ(float q) {
        bf.setQ(q);
    }

    public void filterGain(float gain) {
        bf.setGain(gain);
    }

    public void filterType(BiquadFilter.Type type) {
        bf.setType(type);
    }

    public void fmModDepth(float modDepth) {
        this.fmDepth = modDepth;
    }

    public void fmModFreq(float freq) {
        fmModulator.setFrequency(freq);
    }

    public void fmCarrierWave(Buffer wave) {
        fm.setBuffer(wave);
    }

    public void fmModWave(Buffer wave) {
        fmModulator.setBuffer(wave);
    }

    public void brightness(float brightness) {
        this.masterBrightness = brightness;
    }

    public void pulseBrightness(float pulseBrightness) {
        this.pulseBrightness = pulseBrightness;
    }

    public void decay(float decay) {
        this.decay = decay;
    }

    public void sparkle(float sparkle) {
        this.sparkle = sparkle;
    }

    public void quiet() {
        brightness(0);
        gain(0);
    }

    public void triggerSampleWithOffset(double offset) {
        position(clockLockPosition + offset);
        timeOfLastTriggerMS = System.currentTimeMillis() - (int)offset;
    }

    //LFO controls

    public void setLFORingMod() {
        if(audioIsSetup) {
            //the LFO is multiplied to the combined signal of the GSP and SP.
            clearLFO();
            theActualOut.clearInputConnections();
            Function f = new Function(lfo, g, gain) {
                @Override
                public float calculate() {
                    float lfo = x[0];
                    float input = x[1];
                    float theGain = x[2];
                    float output = 0;
                    if(lfoDepth > 1) lfoDepth = 1; else if(lfoDepth < 0) lfoDepth = 0;
                    if(lfoDepth < 0.5f) {
                        float ldepthTemp = lfoDepth * 2;
                        output = (1 - ldepthTemp + (lfo * ldepthTemp * theGain)) * input;
                    } else {
                        float ldepthTemp = 1f - (lfoDepth - 0.5f) * 2;
                        output = (1 - ldepthTemp + (input * ldepthTemp * theGain)) * lfo;
                    }
                    return Math.max(-1, Math.min(1, output));
                }
            };
            theActualOut.addInput(f);
        }
    }

    public void clearLFO() {
        if(audioIsSetup) {
            theActualOut.clearInputConnections();
            theActualOut.addInput(g);
        }
    }

    public void setTimeoutThresh(int thresh) {
        this.timeoutThresh = thresh;
    }

    public void resetTimeout() {
        this.timeoutCount = 0;
    }

    public long timeSinceLastTriggerMS() {
        return System.currentTimeMillis() - timeOfLastTriggerMS;
    }
    public double clip(double val, double min, double max) {
        return Math.min(Math.max(val, min), max);
    }
}
