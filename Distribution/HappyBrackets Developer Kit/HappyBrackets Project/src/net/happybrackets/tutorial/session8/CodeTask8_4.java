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

/** For this code task we want to look at the accelerometer and use it to 
 * trigger a sound when you turn over the accelerometer.
 * 
 */
public class CodeTask8_1 implements HBAction {

    @Override
    public void action(HB hb) {

		enum orientation = {UP, DOWN}; 
		orientation currentOri  = UP;
		orientation previousOri = UP;
        hb.reset();

        //set up an object that will respond to a minimu sensor.
        hb.sensors.put("mu", new MiniMU());

        //load a set of sounds
        SampleManager.group("Guitar", "data/audio/Nylon_Guitar");

        Glide rate = new Glide(hb.ac, 1);
		
		//play a new random sound
        Sample s = SampleManager.randomFromGroup("Guitar");
        SamplePlayer sp = new SamplePlayer(hb.ac, s);
        sp.setRate(rate);
        hb.sensors.get("LSM9DS1").addListener(new SensorListener() {
            @Override
        	public void sensorUpdated() {
				
				// Get the data from Z.
				double[] floatData = LSM9DS1.getAccelerometerData();
				double   zAxis     = floatData[2]; 
				
				// set previous orientation to the current orientation
				previousOri = currentOri;
				
				// Is it positive or negative.
				if (zAxis > 0) {
					currentOri = UP;
				} else {
					currentOri = DOWN;
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
