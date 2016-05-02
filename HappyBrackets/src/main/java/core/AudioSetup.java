package core;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.IOAudioFormat;
import net.beadsproject.beads.core.io.JavaSoundAudioIO;

public abstract class AudioSetup { 

	public static AudioContext getAudioContext(String[] args) {			//args are bufSize (512), sample rate (11000), input channels (0), output channels (1)
		try {
			int bufSize = Integer.parseInt(args[0]);
			int sampleRate = Integer.parseInt(args[1]);
			int bits = Integer.parseInt(args[2]);
			int inchans = Integer.parseInt(args[3]);
			int outchans = Integer.parseInt(args[4]);
			System.out.println("Creating AudioContext with args: bufSize=" + bufSize + ", sampleRate=" + sampleRate + ", bits=" + bits + ", ins=" + inchans + ", outs=" + outchans);
			JavaSoundAudioIO jsaio = new JavaSoundAudioIO(bufSize);
			AudioContext ac = new AudioContext(jsaio, bufSize, new IOAudioFormat(sampleRate, bits, inchans, outchans));
			return ac;
		} catch(Exception e) {
			System.out.println("Warning, correct args have not been supplied to AudioSetup.getAudioContext()");
			return getAudioContext();
		}
	}
	
	public static AudioContext getAudioContext() {			
		return getAudioContext(new String[] {"2048", "11000", "16", "0", "1"});
	}
}
