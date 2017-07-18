package net.happybrackets.examples;

import net.beadsproject.beads.analysis.SegmentListener;
import net.beadsproject.beads.analysis.featureextractors.FFT;
import net.beadsproject.beads.analysis.featureextractors.PowerSpectrum;
import net.beadsproject.beads.analysis.segmenters.ShortFrameSegmenter;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.TimeStamp;

/**
 * Created by ollie on 30/1/17.
 */
public class AnalysisExample {

    public static void main(String[] args) {
        AudioContext ac = new AudioContext();
        ShortFrameSegmenter sfs = new ShortFrameSegmenter(ac);
        ac.out.addDependent(sfs);

        FFT fft = new FFT();
        sfs.addListener(fft);
        PowerSpectrum ps = new PowerSpectrum();
        fft.addListener(ps);

        sfs.addSegmentListener(new SegmentListener() {
            @Override
            public void newSegment(TimeStamp timeStamp, TimeStamp timeStamp1) {
                float[] psData = ps.getFeatures();
                //do your stuff here
            }
        });
    }
}
