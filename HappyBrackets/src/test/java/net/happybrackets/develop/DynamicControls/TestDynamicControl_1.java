package net.happybrackets.develop.DynamicControls;

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Noise;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.ControlScope;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

public class TestDynamicControl_1 implements HBAction {

    float floatVal = 2000; // must be a class variable to access inside inner classes
    String controlHashCode = ""; // we are just using this now as our real control will have a hash code

    @Override
    public void action(HB hb) {

        try {
            DynamicControl slider_control = hb.createDynamicControl(this, ControlType.INT, "Slider-class", 2000, 100, 4000);

            DynamicControl text_control = hb.createDynamicControl(this, ControlType.TEXT, "Text", "Hello Text This is going to be much longer");
            DynamicControl float_control = hb.createDynamicControl(this, ControlType.FLOAT, "Float-device", 0, -1, 1);
            DynamicControl button_control = hb.createDynamicControl(this, ControlType.TRIGGER, "Button", 0);
            DynamicControl checkbox_control = hb.createDynamicControl(this, ControlType.BOOLEAN, "Checkbox-global", false);

            DynamicControl text_mirror = hb.createDynamicControl(this, ControlType.TEXT, "Text", "Text Mirror");

            DynamicControl int_text = hb.createDynamicControl(this, ControlType.INT, "Int", 2000);
            DynamicControl float_text = hb.createDynamicControl(this, ControlType.FLOAT, "Float", 100.0);

            slider_control.setControlScope(ControlScope.CLASS);
            float_control.setControlScope(ControlScope.DEVICE);
            checkbox_control.setControlScope(ControlScope.GLOBAL);

            text_mirror.setDisplayType(DynamicControl.DISPLAY_TYPE.DISPLAY_DISABLED);
            slider_control.setDisplayType(DynamicControl.DISPLAY_TYPE.DISPLAY_DISABLED);
            //hb.setPresetValue("Name", 1);
            //hb.setPresetValue("Name", 1);

            int_text.addControlListener(new DynamicControl.DynamicControlListener() {
                @Override
                public void update(DynamicControl control) {
                    int i = (int) control.getValue();
                    System.out.println("Int val " + i);

                }
            });

            float_text.addControlListener(new DynamicControl.DynamicControlListener() {
                @Override
                public void update(DynamicControl control) {
                    double f = ((double)control.getValue());
                    System.out.println("Float val " + f);

                }
            });

            slider_control.addControlListener(new DynamicControl.DynamicControlListener() {
                @Override
                public void update(DynamicControl control) {
                    int i = (int) control.getValue();
                    floatVal = (float) i;
                    System.out.println("Slider val " + i);
                }
            });

            float_control.addControlListener(new DynamicControl.DynamicControlListener() {
                @Override
                public void update(DynamicControl control) {
                    float f = (float) ((double)control.getValue());
                    System.out.println("Float Val " + f);
                }
            });
            text_control.addControlListener(new DynamicControl.DynamicControlListener() {
                @Override
                public void update(DynamicControl control) {
                    String s = (String) control.getValue();
                    System.out.println("Control Listener received :" + s);
                    text_mirror.setValue("Miiror: " + s);
                }
            });

            checkbox_control.addControlListener(new DynamicControl.DynamicControlListener() {
                @Override
                public void update(DynamicControl control) {
                    boolean b = (Boolean) control.getValue();
                    System.out.println("Checkbox val " + b);

                }
            });
            button_control.addControlListener(new DynamicControl.DynamicControlListener() {
                @Override
                public void update(DynamicControl control) {
                    System.out.println("Button Value Reset Sliders to zero and erase text");
                    checkbox_control.setValue(0);
                    text_control.setValue("Reset Text");
                    float_control.setValue(float_control.getMinimumDisplayValue());
                    slider_control.setValue(slider_control.getMinimumDisplayValue());
                }
            });

            controlHashCode = slider_control.getControlMapKey();


            hb.masterGainEnv.setValue(0.2f);
            hb.clock.getIntervalUGen().setValue(2000);
            hb.clock.clearMessageListeners();
            hb.pattern(new Bead() {
                @Override
                protected void messageReceived(Bead bead) {
                    if (hb.clock.getCount() % 8 == 0) {
                        float freq = Pitch.forceFrequencyToScale(hb.rng.nextFloat() * floatVal + 100, Pitch.dorian);
                        WavePlayer wp = new WavePlayer(hb.ac, freq, Buffer.SINE);
                        Envelope e = new Envelope(hb.ac, hb.rng.nextFloat() * 0.03f + 0.1f);
                        Gain g = new Gain(hb.ac, 1, e);
                        e.addSegment(0, 5000, new KillTrigger(g));
                        g.addInput(wp);
                        hb.sound(g);
                    }
                    if (hb.clock.getCount() % 6 == 5) {
                        float freq = Pitch.forceFrequencyToScale(hb.rng.nextFloat() * 1000 + 100, Pitch.dorian);
                        WavePlayer wp = new WavePlayer(hb.ac, freq, Buffer.SINE);
                        Envelope e = new Envelope(hb.ac, 0);
                        Gain g = new Gain(hb.ac, 1, e);
                        e.addSegment(hb.rng.nextFloat() * 0.02f + 0.04f, 1000);
                        e.addSegment(0, 100, new KillTrigger(g));
                        g.addInput(wp);
                        hb.sound(g);
                    }
                    if (hb.clock.getCount() % 4 == 0) {
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
        catch (Exception ex){
            ex.printStackTrace();
        }

    }

    public static void main(String[] args) {

        try {
            HB.runDebug(MethodHandles.lookup().lookupClass());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
