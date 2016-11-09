package net.happybrackets.tutorial.session8;

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.LSM9DS1;
import net.happybrackets.device.sensors.SensorUpdateListener;

/**
 * In this example we use the x-axis of the accelerometer to manipulate the pitch of the playback of  a sample that is played off the clock.
 */
public class Example8_4 implements HBAction {

    @Override
    public void action(HB hb) {
        hb.reset();
        //load a set of sounds
        SampleManager.group("Guitar", "data/audio/Nylon_Guitar");
        Glide rate = new Glide(hb.ac, 1);
        //create a pattern that plays notes
        hb.pattern(new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                if(hb.clock.getCount() % 32 == 0) {
                    //play a new random sound
                    Sample s = SampleManager.fromGroup("Guitar", 1);
                    SamplePlayer sp = new SamplePlayer(hb.ac, s);
                    sp.setRate(rate);
                    hb.sound(sp);
                }
            }
        });
        LSM9DS1 lsm = (LSM9DS1)hb.getSensor(LSM9DS1.class);
        lsm.addListener(new SensorUpdateListener() {
            @Override
            public void sensorUpdated() {
                // get x
                double x = lsm.getAccelerometerData()[0];
                rate.setValue((float)x / 1000f);
            }
        });
    }

}
