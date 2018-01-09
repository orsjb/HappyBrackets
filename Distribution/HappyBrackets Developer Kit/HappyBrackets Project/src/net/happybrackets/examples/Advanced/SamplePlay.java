package net.happybrackets.examples.Advanced;

import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.Function;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.ControlScope;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

public class SamplePlay implements HBAction {

    // define the sample we are playing
    final String SAMPLE_NAME = "data/audio/hiphop.wav";

    SamplePlayer sp = null;

    // define variables that will modify the way sample is played
    Glide rateEnv, rateMod, gainEnv, loopStart, loopEnd;

    // Make some controls to Display Values
    DynamicControl positionControl, playingControl;

    // Define what the max we can move our sample position to
    float maxSamplePosition = 0;

    // flag to indicate our direction
    boolean doRewind = false;

    @Override
    public void action(HB hb) {
        //audio stuff
        gainEnv = new Glide(hb.ac, 1f, 500);
        Gain g = new Gain(hb.ac, 1, gainEnv);
        rateEnv = new Glide(hb.ac, 0, 0);
        rateMod = new Glide(hb.ac, 1, 200);

        Function rate = new Function(rateEnv, rateMod) {
            @Override
            public float calculate() {
                return x[0] * x[1];
            }
        };

        SampleManager.setVerbose(true);
        Sample sample = SampleManager.sample(SAMPLE_NAME);
        if (sample != null)
        {
            sp = new SamplePlayer(hb.ac, sample);
            sp.setRate(rate);
            sp.setLoopType(SamplePlayer.LoopType.LOOP_FORWARDS);

            // Define the initial loop start and and points
            loopStart = new Glide(hb.ac, 0, 500);

            maxSamplePosition = (float)sp.getSample().getLength();
            loopEnd = new Glide(hb.ac, maxSamplePosition, 500);

            sp.setLoopStart(loopStart);
            sp.setLoopEnd(loopEnd);

            // Connect our sample player to audio
            g.addInput(sp);

            hb.sound(g);

            play(doRewind);

            //Create the controls we will use to display the status
            playingControl = hb.createDynamicControl(ControlType.BOOLEAN, "PLaying", true)
                    .addControlListener(new DynamicControl.DynamicControlListener() {
                        @Override
                        public void update(DynamicControl dynamicControl) {
                            // We will Change state based on control state
                            boolean do_play = (Boolean)dynamicControl.getValue();
                            if (do_play){
                                play(doRewind);
                            }
                            else {
                                stop();
                            }
                        }
                    });


            // Create a control for position
            positionControl = hb.createDynamicControl(this, ControlType.FLOAT, "Position", 0).setControlScope(ControlScope.SKETCH)
                    .addControlListener(new DynamicControl.DynamicControlListener() {
                        @Override
                        public void update(DynamicControl dynamicControl) {
                            // we will move to the audio position
                            float new_position = (float)dynamicControl.getValue();
                            setSamplePosition(new_position);
                        }
                    });

            positionControl = hb.createDynamicControl(this, ControlType.FLOAT, "Position", 0, 0, maxSamplePosition).setControlScope(ControlScope.SKETCH);

            // Create a rewind checkbox
            hb.createDynamicControl(ControlType.BOOLEAN, "Rewind", false).addControlListener(new DynamicControl.DynamicControlListener() {
                @Override
                public void update(DynamicControl dynamicControl) {
                    doRewind = (Boolean)dynamicControl.getValue();
                    if (rateEnv.getCurrentValue() != 0) {
                        stop();
                        play(doRewind);
                    }
                }
            });

            // add a speed Control
            hb.createDynamicControl(this, ControlType.FLOAT, "Speed", 1).setControlScope(ControlScope.SKETCH)
                    .addControlListener(new DynamicControl.DynamicControlListener() {
                        @Override
                        public void update(DynamicControl dynamicControl) {
                            rateMod.setValue((float)dynamicControl.getValue());
                        }
                    });

            hb.createDynamicControl(this, ControlType.FLOAT, "Speed", 1, 0, 4).setControlScope(ControlScope.SKETCH);


            // Let us Display the audio State every 100ms
            Runnable audio_status_task = new Runnable() {
                @Override
                public void run() {
                    while(true) {
                        float position = getAudioPosition();
                        positionControl.setValue(position);


                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            new Thread(audio_status_task).start();
        }
        else
        {
            System.out.println("Unable to load " + SAMPLE_NAME);
        }
    }

    /**
     * Set the position of the sample player
     * @param new_position the psoition to set to
     */
    void setSamplePosition(float new_position){
        try
        {
            sp.setPosition(new_position);
        }
        catch (Exception ex){
            System.out.println(ex.getMessage());
        }
    }

    /**
     * play audio
     * @param rewind set to tru for rewind
     */
    void play(boolean rewind){
        rateEnv.setValue(rewind? -1f: 1f);
        gainEnv.setValue(1f);
    }

    /**
     * stop Audio playback
     */
    void stop(){
        rateEnv.setValue(0);
    }

    /**
     * Get the current audio position
     * @return the current audio position
     */
    float getAudioPosition(){
        return (float)sp.getPosition();
    }

    public static void main(String[] args) {

        try {
            HB.runDebug(MethodHandles.lookup().lookupClass());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
