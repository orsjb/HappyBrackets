package net.happybrackets.examples.Samples;

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
import net.happybrackets.device.sensors.Accelerometer;
import net.happybrackets.device.sensors.SensorUpdateListener;

import java.lang.invoke.MethodHandles;

/**
 * Loop Play sample based on X and Z value of accelerometer
 * The X value determines the speed, while Z value is whether forward or reverse
 */
public class SamplePlay implements HBAction {

    // define the sample we are playing
    final String SAMPLE_NAME = "data/audio/hiphop.wav";

    SamplePlayer sp = null;

    // define variables that will modify the way sample is played
    Glide rateEnv, rateMod, gainEnv, loopStart, loopEnd;

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

            hb.ac.out.addInput(g);

            play(doRewind);

            Accelerometer sensor = (Accelerometer)hb.getSensor(Accelerometer.class);
            if (sensor != null)
            {
                // add a listener to our sensor
                sensor.addListener(new SensorUpdateListener() {
                    @Override
                    public void sensorUpdated() {

                        double x_val = sensor.getAccelerometerX();
                        double y_val = sensor.getAccelerometerY();
                        double z_val = sensor.getAccelerometerZ();

                        // first let us do the rate from 0 to 2
                        double rate = x_val + 1;
                        rateMod.setValue((float)rate);


                        //see if we are going forward or backward based on Z
                        rateEnv.setValue(z_val < 0 ? -1f: 1f);
                    }
                });
            }
        }
        else
        {
            System.out.println("Unable to load " + SAMPLE_NAME);
        }
    }

    /**
     * Set the position of the sample player
     * @param new_position the position to set to
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
