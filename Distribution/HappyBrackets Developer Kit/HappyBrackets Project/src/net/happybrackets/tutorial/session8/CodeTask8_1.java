package net.happybrackets.tutorial.session8;

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.MiniMU;
import net.happybrackets.device.sensors.SensorListener;

/**
 * Created by ollie on 24/06/2016.
 */
public class CodeTask8_1 implements HBAction {

    @Override
    public void action(HB hb) {


        hb.reset();

        //set up an object that will respond to a minimu sensor.
        hb.sensors.put("mu", new MiniMU());

        //load a set of sounds
        SampleManager.group("Guitar", "data/audio/Nylon_Guitar");

        Glide rate = new Glide(hb.ac, 1);

        //create a pattern that plays notes
        hb.pattern(new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                if(hb.clock.getCount() % 32 == 0) {
                    //play a new random sound
                    Sample s = SampleManager.randomFromGroup("Guitar");
                    SamplePlayer sp = new SamplePlayer(hb.ac, s);
                    sp.setRate(rate);
                    hb.sound(sp);
                }
            }
        });

        hb.sensors.get("mu").addListener(new MiniMU.MiniMUListener() {
            @Override
            public void accelData(double x, double y, double z) {
                rate.setValue((float)x / 1000f);
            }
        });
    }

}
