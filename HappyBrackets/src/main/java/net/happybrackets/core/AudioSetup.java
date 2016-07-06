package net.happybrackets.core;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.IOAudioFormat;
import net.beadsproject.beads.core.io.JavaSoundAudioIO;

public abstract class AudioSetup { 

	public static AudioContext getAudioContext(String[] args) {			//args are bufSize (512), sample rate (11000), input channels (0), output channels (1)

		int bufSize = 2048;
		int sampleRate = 44100;
		int bits = 16;
		int inchans = 0;
		int outchans = 1;
		try {
			//parse args
			for(String arg : args) {
				String[] elements = arg.split("[=]");
				if(elements.length > 1) {
					if(elements[0].equals("buf")) bufSize = Integer.parseInt(elements[1]);
					else if(elements[0].equals("sr")) sampleRate = Integer.parseInt(elements[1]);
					else if(elements[0].equals("bits")) bits = Integer.parseInt(elements[1]);
					else if(elements[0].equals("ins")) inchans = Integer.parseInt(elements[1]);
					else if(elements[0].equals("outs")) outchans = Integer.parseInt(elements[1]);
				}
			}
		} catch(Exception e) {
			System.out.println("Warning, correct args have not been supplied to AudioSetup.getAudioContext()");
		}
		System.out.println("Creating AudioContext with args: bufSize=" + bufSize + ", sampleRate=" + sampleRate + ", bits=" + bits + ", ins=" + inchans + ", outs=" + outchans);
		JavaSoundAudioIO jsaio = new JavaSoundAudioIO(bufSize);
		AudioContext ac = new AudioContext(jsaio, bufSize, new IOAudioFormat(sampleRate, bits, inchans, outchans));
		return ac;

	}
}
