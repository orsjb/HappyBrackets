package net.happybrackets.tutorial.session6;

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Noise;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;

/**
 */
public class LiveCoder implements HBAction {
    @Override
    public void action(HB hb) {
        hb.masterGainEnv.setValue(0.2f);
        hb.clock.getIntervalUGen().setValue(8000);
        hb.clock.clearMessageListeners();
        hb.pattern(new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                if(hb.clock.getCount() % 2 == 0) {
                    float freq = Pitch.forceFrequencyToScale(hb.rng.nextFloat() * 2000 + 100, Pitch.dorian) * 1f;
                    if(hb.rng.nextFloat() < 0.2f) freq *= 0.5f;
                    WavePlayer wp = new WavePlayer(hb.ac, freq, Buffer.SINE);
                    Envelope e = new Envelope(hb.ac, 0);
                    Gain g = new Gain(hb.ac, 1, e);
                    e.addSegment(hb.rng.nextFloat() * 0.05f + 0.1f, 500 * hb.rng.nextFloat());
                    e.addSegment(0, 2000, new KillTrigger(g));
                    g.addInput(wp);
                    hb.sound(g);
                }
                if(hb.clock.getCount() % 12 == 5) {
                    float freq = Pitch.forceFrequencyToScale(hb.rng.nextFloat() * 1000 + 100, Pitch.dorian);
                    WavePlayer wp = new WavePlayer(hb.ac, freq, Buffer.SAW);
                    Envelope e = new Envelope(hb.ac, 0);
                    Gain g = new Gain(hb.ac, 1, e);
                    e.addSegment(hb.rng.nextFloat() * 0.02f + 0.04f, 1000);
                    e.addSegment(0, 100, new KillTrigger(g));
                    g.addInput(wp);
                    hb.sound(g);
                }
                if(hb.clock.getCount() % 2 == 0 || hb.rng.nextFloat() < 0.02f) {
                    Noise n = new Noise(hb.ac);
                    Envelope e = new Envelope(hb.ac, hb.rng.nextFloat() * hb.rng.nextFloat() * 0.2f + 0.02f);
                    Gain g = new Gain(hb.ac, 1, e);
                    e.addSegment(0, 2 + hb.rng.nextInt(20), new KillTrigger(g));
                    g.addInput(n);
                    hb.sound(g);
                }
            }
        });
    }
}
