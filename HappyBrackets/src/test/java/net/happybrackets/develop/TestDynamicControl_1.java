package net.happybrackets.develop;

import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Noise;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.OSCVocabulary;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.device.HB;

import java.net.SocketAddress;

public class TestDynamicControl_1 implements HBAction {

    float floatVal = 2000; // must be a class variable to access inside inner classes
    int controlHashCode = 0; // we are just using this now as our real control will have a hash code

    @Override
    public void action(HB hb) {


        DynamicControl new_control = new DynamicControl(this, ControlType.SLIDER, "Slider", 2000, 100, 4000);

        new DynamicControl(this, ControlType.TEXT, "Text", "Hello Text");
        new DynamicControl(this, ControlType.FLOAT, "Float", 200.0, 100.0, 300.0);
        new DynamicControl(this, ControlType.BUTTON, "Button", 0);
        new DynamicControl(this, ControlType.CHECKBOX, "Checkbox", 1);


        new_control.addControlListener(new DynamicControl.DynamicControlListener() {
            @Override
            public void update(DynamicControl control) {
                floatVal = (float)control.getValue();
            }
        });

        controlHashCode = new_control.hashCode();


        hb.controller.addListener(new OSCListener() {
            @Override
            public void messageReceived(OSCMessage msg, SocketAddress sender, long time) {
                if (OSCVocabulary.match(msg, "/dev/control")) {

                    //float arg_val = (float) msg.getArg(0);
                    DynamicControl control  = DynamicControl.getControl(controlHashCode);

                    // this should also send the event
                    control.setValue(msg.getArg(0));

                }
            }
        });

        hb.masterGainEnv.setValue(0.2f);
        hb.clock.getIntervalUGen().setValue(2000);
        hb.clock.clearMessageListeners();
        hb.pattern(new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                if(hb.clock.getCount() % 8 == 0) {
                    float freq = Pitch.forceFrequencyToScale(hb.rng.nextFloat() * floatVal + 100, Pitch.dorian);
                    WavePlayer wp = new WavePlayer(hb.ac, freq, Buffer.SINE);
                    Envelope e = new Envelope(hb.ac, hb.rng.nextFloat() * 0.03f + 0.1f);
                    Gain g = new Gain(hb.ac, 1, e);
                    e.addSegment(0, 5000, new KillTrigger(g));
                    g.addInput(wp);
                    hb.sound(g);
                }
                if(hb.clock.getCount() % 6 == 5) {
                    float freq = Pitch.forceFrequencyToScale(hb.rng.nextFloat() * 1000 + 100, Pitch.dorian);
                    WavePlayer wp = new WavePlayer(hb.ac, freq, Buffer.SINE);
                    Envelope e = new Envelope(hb.ac, 0);
                    Gain g = new Gain(hb.ac, 1, e);
                    e.addSegment(hb.rng.nextFloat() * 0.02f + 0.04f, 1000);
                    e.addSegment(0, 100, new KillTrigger(g));
                    g.addInput(wp);
                    hb.sound(g);
                }
                if(hb.clock.getCount() % 4 == 0) {
                    Noise n = new Noise(hb.ac);
                    Envelope e = new Envelope(hb.ac, hb.rng.nextFloat() * 0.01f + 0.02f);
                    Gain g = new Gain(hb.ac, 1, e);
                    e.addSegment(0, 5, new KillTrigger(g));
                    g.addInput(n);
                    hb.sound(g);
                }
            }
        });
    }

}
