package net.happybrackets.Nime2018Paper;

import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.ControlScope;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.device.HB;

public class ControleSketchExample implements HBAction {

    @Override
    public void action(HB hb) {

        Glide sampleRate = new Glide(hb.ac);
        HBPermFmSynth voice1 = new HBPermFmSynth();
        HBPermFmSynth voice2 = new HBPermFmSynth();

        /* Any sketch can send this value an all others will receive */
        DynamicControl playback_rate = hb.createDynamicControl(voice1, ControlType.FLOAT, "DevicePaybackRate", 1)
                .setControlScope(ControlScope.DEVICE);

        playback_rate.setValue(-1); // make samples play backwards

        /* Inside other Sketch */
        hb.createDynamicControl(voice1, ControlType.FLOAT, "DevicePaybackRate", 1).
                setControlScope(ControlScope.DEVICE).addControlListener(dynamicControl -> {
            // make any sample player on device listening change to this rate
            sampleRate.setValue ((float)dynamicControl.getValue());
        });
    }
}
