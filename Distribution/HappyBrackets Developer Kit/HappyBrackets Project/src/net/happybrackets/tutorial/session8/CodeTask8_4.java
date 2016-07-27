package net.happybrackets.tutorial.session8;

import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.LSM9DS1;
import net.happybrackets.device.sensors.SensorUpdateListener;

/** For this code task we want to look at the accelerometer and use it to 
 * trigger a sound when you turn over the accelerometer.
 * 
 */
public class CodeTask8_4 implements HBAction {

	public enum Orientation {UP, DOWN};
	Orientation currentOri  = Orientation.UP;
	Orientation previousOri  = Orientation.DOWN;

	@Override
    public void action(HB hb) {


		hb.reset();

        //load a set of sounds
        SampleManager.group("Guitar", "data/audio/Nylon_Guitar");

        Glide rate = new Glide(hb.ac, 1);
		
		//play a new random sound
        Sample s = SampleManager.randomFromGroup("Guitar");
        SamplePlayer sp = new SamplePlayer(hb.ac, s);
        sp.setRate(rate);

		LSM9DS1 mySensor = (LSM9DS1)hb.getSensor(LSM9DS1.class);

		mySensor.addListener(new SensorUpdateListener() {
            @Override
        	public void sensorUpdated() {
				
				// Get the data from Z.
				double zAxis = mySensor.getAccelerometerData()[2];

				// set previous orientation to the current orientation
				previousOri = currentOri;
				
				// Is it positive or negative.
				if (zAxis > 0) {
					currentOri = Orientation.UP;
				} else {
					currentOri = Orientation.DOWN;
				}
				
				// Is it different to the current value (has it changed)
				if (currentOri != previousOri){
					// if so play the sound 
					hb.sound(sp);
				}
            }
        });
    }

}
