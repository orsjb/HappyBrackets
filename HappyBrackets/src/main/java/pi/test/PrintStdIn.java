package pi.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import core.AudioSetup;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;


public class PrintStdIn{
	public static void main (String args[]){

		AudioContext ac = AudioSetup.getAudioContext(args);
		ac.start();

		Glide g = new Glide(ac, 500);
		WavePlayer wp = new WavePlayer(ac, g, Buffer.SINE);

		ac.out.addInput(wp);
		ac.out.setGain(0.1f);

		try{
			BufferedReader br = new BufferedReader(new InputStreamReader (System.in));
			String input;
			while ((input=br.readLine())!=null){
				System.out.println("Java: " + input);
				String inVal1 = input.split("\\s+")[1];
				System.out.println(inVal1);
				float val = Float.parseFloat(inVal1);
				g.setValue(val *  2000f);
			}
		}catch(IOException io){
		io.printStackTrace();
		}
	}
}
