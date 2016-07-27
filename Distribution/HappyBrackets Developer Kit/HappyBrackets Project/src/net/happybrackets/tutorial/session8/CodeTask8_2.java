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

		double xAngle = 0;
		double yAngle = 0;
		double zAngle = 0;

		// Reset
        hb.reset();

        // Set up an object that will respond to a minimu sensor.
        hb.sensors.put(LSM9DS1.class, new LSM9DS1());

		// Envelope env
		Envelope env = new Envelope(ac, 440.0f);

		
		// Play a Sine Tone
		
		WavePlayer wp = new WavePlayer(ac, env, Buffer.SINE);

		Gain g = new Gain(ac, 0, 0.1f);
		g.addInput(wp);
		hb.ac.addInput(g);
		hb.ac.start();
		
        hb.sensors.get("LSM9DS1").addListener(new SensorListener() {
            @Override
        	public void sensorUpdated() {
				
				// Get the data from the accelerometer.
				double[] floatData = LSM9DS1.getAccelerometerData();
				double 	 xAxis	   = floatData[0];
				double 	 yAxis	   = floatData[1];
				double   zAxis     = floatData[2]; 
				
				// do polar to cartesian 
			    xAngle = Math.atan(xAxis / (Math.sqrt(Math.pow(yAxis, 2) + Math.pow(zAxis, 2))));
			    yAngle = Math.atan(yAxis / (Math.sqrt(Math.pow(xAxis, 2) + Math.pow(zAxis, 2))));
			    zAngle = Math.atan(Math.sqrt(Math.pow(xAxis, 2) + Math.pow(yAxis, 2)) / zAxis);

				// convert degrees to radians
			    xAngle = xAngle * 180.00 / Math.PI;  
				yAngle = yAngle * 180.00 / Math.PI;   
				zAngle = zAngle * 180.00 / Math.PI; 
				
				// set the pitch of the sine wave. (What will this be?)
				env.setValue(xAngle*10);
			
            }
        });
    }
}
