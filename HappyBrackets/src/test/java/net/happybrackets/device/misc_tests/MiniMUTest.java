package net.happybrackets.device.misc_tests;

import net.happybrackets.device.sensors.MiniMU;
import net.happybrackets.device.sensors.MiniMU.MiniMUListener;
import net.happybrackets.core.AudioSetup;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;

public class MiniMUTest {

	public static void main(String[] args) {
		//audio
		AudioContext ac = AudioSetup.getAudioContext(args);
		//ac.start();
		//controllers
		final Glide freqCtrl = new Glide(ac, 500);
		final Glide gainCtrl = new Glide(ac, 0.1f);
		//set up signal chain
		WavePlayer wp = new WavePlayer(ac, freqCtrl, Buffer.SINE);
		Gain g = new Gain(ac, 1, gainCtrl);
		g.addInput(wp);
		ac.out.addInput(g);
		//getInstance listening to data
		MiniMUListener myListener = new MiniMUListener() {
			
			public void accelData(double x, double y, double z) {
				String AccString = String.format("MiniMu Acc X/Y/Z = %05.2f %05.2f %05.2f", x,y,z);

				System.out.println(AccString);
				freqCtrl.setValue(((float)Math.abs(x) * 10f) % 10000f + 600f);
				gainCtrl.setValue(((float)Math.abs(y) * 10f) % 400f / 1600f + 0.1f);
			}
			
			public void gyroData(double x, double y, double z) {
				String GyrString = String.format("MiniMu Gyr X/Y/Z = %05.2f %05.2f %05.2f", x,y,z);
				System.out.println(GyrString);
			}
			
			public void magData(double x, double y, double z) {
				String MagString = String.format("MiniMu Mag X/Y/Z = %05.2f %05.2f %05.2f", x,y,z);
				System.out.println(MagString);
			}
			
		};
		MiniMU mm = new MiniMU(myListener);
		mm.start();
	}

}
