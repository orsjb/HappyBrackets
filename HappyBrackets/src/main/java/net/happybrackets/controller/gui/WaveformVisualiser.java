package net.happybrackets.controller.gui;

import net.beadsproject.beads.core.AudioContext;
import net.happybrackets.device.DebuggerWaveformVisualizer;

/**
 * Displays the waveform on a JPanel based on a {@link AudioContext}
 *
 * This is a legacy Class for Kadenze course
 */
public class WaveformVisualiser{

    /**
     * Displays the waveform in a Panel
     * This is a lagacy feature for Kadenze course
     * @param ac {@link AudioContext} from Beads
     */
    public static void open(AudioContext ac) {
        DebuggerWaveformVisualizer visualizer = DebuggerWaveformVisualizer.createVisualiser();
        visualizer.setAudioContext(ac);
    }

    /**
     * Legacy function For kendeza course
     * @param ac {@link AudioContext} from Beads
     * @param legacy ignore. Does nothing
     */
    public static void open(AudioContext ac, boolean legacy){
        open(ac);
    }

}
