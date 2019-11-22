package net.happybrackets.core.instruments;

import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.SamplePlayer;

/**
 * Encapsulates a basic Sample player into a single class object. It is possible to define the sample name through
 * {@link #setSample(String)}, define looping through {@link #setLoopStart(UGen)}, {@link #setLoopStart(double)}, {@link #setLoopEnd(UGen)}, {@link #setLoopEnd(UGen)}, {@link #setLoopStart(UGen)} and {@link #setLoopType(SamplePlayer.LoopType)}.
 *  <br> <br>
 *  The playback rate of the sample can be controlled through {@link #setRate(UGen)} and {@link #setRate(double)}.
 *  <br> <br>
 *  The position of the player can be set through {@link #setPosition(double)} and {@link #setToEnd()}.
 *  <br>Sample playback can paused through {@link #pause(boolean)}
 *
 *  <br> The gain can be set to a {@link UGen} object via {@link #setGainObject(UGen)} or to a specific value
 *  * via {@link #setGainValue(double)}.
 *  <br><br>An example of using the {@link SampleModule} might be as follows
 *  <pre>
 // define our start and end loop points
 final float LOOP_START = 0;
 final  float LOOP_END = 2000;

 final String sample_name = "data/audio/Roje/i-write.wav";
 SampleModule samplePlayer = new SampleModule();
 if (samplePlayer.setSample(sample_name)) {
    samplePlayer.connectTo(HB.getAudioOutput());

    // define our loop type. we will loop forwards
    samplePlayer.setLoopType(SamplePlayer.LoopType.LOOP_FORWARDS);

     // now set the loop start and end in the actual sample player
    samplePlayer.setLoopStart(LOOP_START);
    samplePlayer.setLoopEnd(LOOP_END);
 } else {
    HB.sendStatus("Failed sample " + sample_name);
 }


 *  </pre>
 */
public class SampleModule extends BasicInstrument{

    final static float DEFAULT_GAIN = 1;
    final static float DEFAULT_PLAYBACK_RATE = 1;

    public final String EXAMPLE_SAMPLE_NAME = "data/audio/Roje/i-write.wav";

    UGen playbackRate;
    UGen loopStart = null;
    UGen loopEnd = null;


    SamplePlayer samplePlayer = null;

    boolean restartSample = true;


    SamplePlayer.LoopType loopType = SamplePlayer.LoopType.NO_LOOP_FORWARDS;

    /**
     * Creates basic sample player for playing wave files
     */
    public SampleModule(){
        super(new Glide(DEFAULT_GAIN));

        playbackRate = new Glide(DEFAULT_PLAYBACK_RATE);
    }

    /**
     * Set the sample. Note that the path is relative to Device/HappyBrackets.
     * Generally, the audio would be in "data/audio/"
     * The example filename is located "data/audio/Roje/i-write.wav"
     * @param filename the filename of the sample
     * @return true if the sample was loaded
     */
    public boolean setSample(String filename){

        boolean ret = false;

        Sample sample = SampleManager.sample(filename);

        // test if we opened the sample successfully
        if (sample != null) {
            // Create our sample player if it does not yet exist
            if (samplePlayer == null) {
                samplePlayer = new SamplePlayer(sample);
                // Samples are killed by default at end. We will stop this default actions so our sample will stay alive
                samplePlayer.setKillOnEnd(false);

                samplePlayer.setRate(playbackRate);

                if (playbackRate.getValue() < 0) {
                    samplePlayer.setToEnd();
                }



                gainAmplifier.addInput(samplePlayer);
                ret = true;
            }
            else {
                samplePlayer.setSample(sample);

                if (restartSample) {
                    if (playbackRate.getValue() < 0) {
                        double length = sample.getLength();
                        samplePlayer.setToEnd();
                    }
                    else {
                        samplePlayer.setPosition(0);
                    }
                }
            }

        }

        return ret;
    }

    /**
     * Pause the Sample PLayback
     * @param pause_playing true if we want to pause. Set to false to start playing
     * @return this
     */
    public SampleModule pause(boolean pause_playing){
        if (samplePlayer != null){
            samplePlayer.pause(pause_playing);
        }

        return this;
    }

    /**
     * Set SamplePlayer to new position
     * @param new_position new position in milliseconds
     * @return this
     */
    public SampleModule setPosition(double new_position){
        if (samplePlayer != null){
            samplePlayer.setPosition(new_position);
        }

        return this;
    }

    /**
     * Set SamplePlayer to end
     * @return this
     */
    public SampleModule setToEnd(){
        if (samplePlayer != null){
            samplePlayer.setToEnd();
        }

        return this;
    }

    /**
     * set an object to control the playback rate of the samplePlayer. For example,
     * to set the playback rate using an envelop, we might use the following code:
     * <br>
     * <pre>
     Envelope sampleRateEnvelope =  new Envelope(0);

     final String s = "data/audio/Roje/i-write.wav";
     SampleModule sampleModule = new SampleModule();
     if (sampleModule.setSample(s)) {// Write your code below this line

     sampleModule.setRate(sampleRateEnvelope);
     sampleModule.connectTo(HB.getAudioOutput());

     sampleRateEnvelope.addSegment(2, 3000); // Set Rate to 2X over 3 seconds
     sampleRateEnvelope.addSegment(1, 2000); // Set Rate to 1X over 2 seconds

     // Write your code above this line
     } else {
     HB.HBInstance.setStatus("Failed sample " + s);
     }// End samplePlayer code
     * </pre>
     * @param new_rate_control the new object that will control the playback rate
     * @return this
     */
    public SampleModule setRate(UGen new_rate_control){
        if (samplePlayer != null){
            samplePlayer.setRate(new_rate_control);
        }

        // now assign new one
        playbackRate = new_rate_control;

        return this;
    }

    /**
     * set the playback rate to this value. A negative value makes the sample play backwards
     * @param new_rate the new frequency
     * @return this
     */
    public SampleModule setRate(double new_rate){
        playbackRate.setValue((float)new_rate);

        return this;
    }

    /**
     * FLag indicating we will restart when a new sample name is added
     * @return the value of restartSample
     */
    public boolean isRestartSample() {
        return restartSample;
    }

    /**
     * Cause the sample player to start playing new samples at the start
     * @param restart_on_new_sample true if we want to play new samples at the start
     * @return this
     */
    public SampleModule setRestartSample(boolean restart_on_new_sample){
        restartSample = restart_on_new_sample;
        return this;
    }

    /**
     * Set the startLoop position
     * @param loop_start loopPoint
     * @return this
     */
    public SampleModule setLoopStart (double loop_start){
        if (loopStart != null){
            loopStart.setValue((float)loop_start);
        }
        else
        {
            setLoopStart(new Glide((float)loop_start));
        }

        return this;
    }

    /**
     * Set the start Loop Control Object
     * @param loop_start the Object that will control loopStart
     * @return this
     */
    public SampleModule setLoopStart (UGen loop_start){
        loopStart = loop_start;
        if (samplePlayer != null){
            samplePlayer.setLoopStart(loopStart);
        }

        return this;
    }

    /**
     * Set the End Loop position
     * @param loop_end loopPoint
     * @return this
     */
    public SampleModule setLoopEnd (double loop_end){
        if (loopEnd != null){
            loopEnd.setValue((float)loop_end);
        }
        else
        {
            setLoopEnd(new Glide((float)loop_end));
        }

        return this;
    }

    /**
     * Set the End Loop Control Object
     * @param loop_end the Object that will control loopEnd
     * @return this
     */
    public SampleModule setLoopEnd (UGen loop_end){
        loopEnd = loop_end;
        if (samplePlayer != null){
            samplePlayer.setLoopEnd(loopEnd);
        }

        return this;
    }

    /**
     * Connect the output of this instrument to the input of another device
     * @param input_device the device we want to connect it to
     * @return this
     */
    public SampleModule connectTo(UGen input_device){
        connectToDevice(input_device);
        return this;
    }


    /**
     * Sets the Loop Type for this Sampler
     * @param loop_type the type of loop
     * @return this
     */
    public SampleModule setLoopType(SamplePlayer.LoopType loop_type) {
        loopType = loop_type;

        if (samplePlayer != null){
            samplePlayer.setLoopType(loop_type);
        }

        return this;
    }

    /**
     * Get the Beads SamplePlayer Object
     * @return the SamplePlayer
     */
    public SamplePlayer getSamplePlayer() {
        return samplePlayer;
    }

}
