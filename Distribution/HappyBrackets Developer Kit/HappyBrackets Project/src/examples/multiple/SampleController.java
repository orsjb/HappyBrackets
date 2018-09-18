package examples.multiple;

import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.Function;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

/**
 * This sketch demonstrates how to use different mechanisms to perform different functions
 * The sketch allows manipulation of the sample by
 * 1 - Changing playback rate with a dynamicControl pair
 *
 * 2 - Reading audio position by monitoring the current playback position inside a thread
 * and setting two dynamicControls. One control is a slider, while the other is the display text
 * two class variables have been made for the audio position so we can change the values
 * inside a class function.
 * The audio position can be changed by stopping the sample player and typing new audio position
 * into the audioPositionText and pressing enter OR by moving the slider
 *
 * 3 - Changing the direction of playback by using a boolean dynamicControl.
 * A function called calculatedRate is used to set the samplePlayer playback rate  rate by
 * multiplying the playbackRate and a positive or negative playbackDirection
 *
 * 4 - Functionality to set start and end loop positions with dynamicControl pairs
 * Note these values are in milliseconds
 *
 * 5 - Ability to make sample jump to the start loop position with a trigger DynamicControl
 * displayed as a button
 *
 * 6 - A monitor thread that updates the audio position while playing. The thread is created
 * when we start playing and killed when playing stops
 *
 * 7 - A boolean DynamicControl to start and stop playback with a checkbox
 */
public class SampleController implements HBAction {
    final int NUMBER_AUDIO_CHANNELS = 1; // define how many audio channels our device is using
    

    // we will make two class member control references so we can set them in a class function
    DynamicControl audioSliderPosition = null;
    DynamicControl audioTextPosition = null;

    // create a samplePlayer we can access inside class functions
    SamplePlayer samplePlayer = null;

    // Create a class updateer thread that will display our audio position while we are playing
    Thread updateThread = null;

    @Override
    public void action(HB hb) {

        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");

        final float MAX_PLAYBACK_RATE = 2;
        final float START_PLAY_RATE = 1;


        // define the duration of our sample. We will set this when the sample is loaded
        float sampleDuration = 0;
        /**************************************************************
         * Load a sample and play it
         *
         * simply type samplePLayer-basic to generate this code and press <ENTER> for each parameter
         **************************************************************/

        final float INITIAL_VOLUME = 1; // define how loud we want the sound
        Glide audioVolume = new Glide(INITIAL_VOLUME);

        // Define our sample name
        final String SAMPLE_NAME = "data/audio/Roje/i-write.wav";

        // create our actual sample
        Sample sample = SampleManager.sample(SAMPLE_NAME);

        // test if we opened the sample successfully
        if (sample != null) {
            // Create our sample player
            SamplePlayer samplePlayer = new SamplePlayer(sample);
            // Samples are killed by default at end. We will stop this default actions so our sample will stay alive
            samplePlayer.setKillOnEnd(false);

            // Connect our sample player to audio
            Gain gainAmplifier = new Gain(NUMBER_AUDIO_CHANNELS, audioVolume);
            gainAmplifier.addInput(samplePlayer);
            hb.ac.out.addInput(gainAmplifier);

            /******** Write your code below this line ********/

            // we need to assign this new samplePlayer to our class samplePlayer
            this.samplePlayer = samplePlayer;

            // Create an on Off Control

            /*************************************************************
             * Create a Boolean type Dynamic Control that displays as a check box
             *
             * Simply type booleanControl to generate this code
             *************************************************************/
            DynamicControl stopPlayControl = hb.createDynamicControl(this, ControlType.BOOLEAN, "Play", true)
                    .addControlListener(control -> {
                        boolean control_val = (boolean) control.getValue();

                        /*** Write your DynamicControl code below this line ***/
                        // if true, we will play
                        samplePlayer.pause(!control_val);

                        // if we are going to play, we will create the updateThread, otherwise we will kill it

                        if (control_val){
                            // create a new thread if one does not exists
                            if (updateThread == null) {
                                updateThread = createUpdateThread();
                            }
                        }
                        else
                        {
                            // if we have a thread, kill it
                            if (updateThread != null)
                            {
                                updateThread.interrupt();
                                updateThread = null;
                            }
                        }

                        /*** Write your DynamicControl code above this line ***/
                    });
            /*** End DynamicControl code ***/
            // get our sample duration
            sampleDuration = (float)sample.getLength();

            // make our sampleplay a looping type
            samplePlayer.setLoopType(SamplePlayer.LoopType.LOOP_FORWARDS);

            // Define what our directions are
            final int PLAY_FORWARD = 1;
            final int PLAY_REVERSE = -1;

            // define our playbackRate control
            Glide playbackRate = new Glide(START_PLAY_RATE);
            // define our sample rate
            Glide playbackDirection = new Glide(PLAY_FORWARD);  // We will control this with yaw

            // This function will be used to set the playback rate of the samplePlayer
            // The playbackDirection will have a value of -1 when it is in reverse, and 1 in forward
            // This is multiplied by the playbackRate to get an absolute value
            Function calculatedRate = new Function(playbackRate, playbackDirection) {
                @Override
                public float calculate() {
                    return x[0] * x[1];
                }
            };

            // now set the rate to the samplePlayer
            samplePlayer.setRate(calculatedRate);

            // Use a Buddy control to change the PlaybackRate
            /*************************************************************
             * Create a Float type Dynamic Control pair that displays as a slider and text box
             *
             * Simply type floatBuddyControl to generate this code
             *************************************************************/
            DynamicControl playbackRateControl = hb.createControlBuddyPair(this, ControlType.FLOAT, "Playback Rate", START_PLAY_RATE, 0, MAX_PLAYBACK_RATE)

                    .addControlListener(control -> {
                        float control_val = (float) control.getValue();

                        /*** Write your DynamicControl code below this line ***/
                        playbackRate.setValue(control_val);
                        /*** Write your DynamicControl code above this line ***/
                    });
            /*** End DynamicControl code ***/


            // create a checkbox to make it play forward or reverse
            /*************************************************************
             * Create a Boolean type Dynamic Control pair that displays as a check box
             *
             * Simply type booleanControl to generate this code
             *************************************************************/
            DynamicControl directionControl = hb.createDynamicControl(this, ControlType.BOOLEAN, "Reverse", false)
                    .addControlListener(control -> {
                        boolean control_val = (boolean) control.getValue();

                        /*** Write your DynamicControl code below this line ***/
                        if (!control_val){
                            playbackDirection.setValue(PLAY_FORWARD);
                        }
                        else{
                            playbackDirection.setValue(PLAY_REVERSE);
                        }
                        /*** Write your DynamicControl code above this line ***/
                    });
            /*** End DynamicControl code ***/

            // display a slider for position
            /*************************************************************
             * Create a Float type Dynamic Control that displays as a slider
             *
             * Simply type floatSliderControl to generate this code
             *************************************************************/
            DynamicControl audioPosition = hb.createDynamicControl(this, ControlType.FLOAT, "Audio Position", 0, 0, sampleDuration)
                    .addControlListener(control -> {
                        float control_val = (float) control.getValue();

                        /*** Write your DynamicControl code below this line ***/

                        samplePlayer.setPosition(control_val);
                        setAudioTextPosition(control_val);
                        /*** Write your DynamicControl code above this line ***/
                    });
            /*** End DynamicControl code ***/

            // set our newly created control to the class reference
            audioSliderPosition = audioPosition;

            /*************************************************************
             * Create a string type Dynamic Control that displays as a text box
             *
             * Simply type textControl to generate this code
             *************************************************************/
            DynamicControl audioPositionText = hb.createDynamicControl(this, ControlType.TEXT, "Audio Position", "")
                    .addControlListener(control -> {
                        String control_val = (String) control.getValue();

                        /*** Write your DynamicControl code below this line ***/
                        // we will decode our string value to audio position

                        // we only want to do this if we are stopped
                        if (samplePlayer.isPaused()) {
                            // our string will be hh:mm:ss.m
                            try {
                                String[] units = control_val.split(":"); //will break the string up into an array
                                int hours = Integer.parseInt(units[0]); //first element
                                int minutes = Integer.parseInt(units[1]); //second element
                                float seconds = Float.parseFloat(units[2]); // thirsd element
                                float audio_seconds = 360 * hours + 60 * minutes + seconds; //add up our values

                                float audio_position =  audio_seconds * 1000;
                                setAudioSliderPosition(audio_position);
                            } catch (Exception ex) {
                            }
                        }
                        /*** Write your DynamicControl code above this line ***/
                    });
            /*** End DynamicControl code ***/

            // set newly create DynamicControl to our class variable

            audioTextPosition = audioPositionText;
            // create a thread to update our position while we are playing
            // This is done in a function
            updateThread = createUpdateThread();

            // We will set sample Start and End points
            // define our start and end points
            Glide  loop_start = new Glide(0);
            Glide loop_end = new Glide(sampleDuration);

            samplePlayer.setLoopStart(loop_start);
            samplePlayer.setLoopEnd(loop_end);

            /*************************************************************
             * Create a Float type Dynamic Control pair that displays as a slider and text box
             *
             * Simply type floatBuddyControl to generate this code
             *************************************************************/
            DynamicControl loopStartControl = hb.createControlBuddyPair(this, ControlType.FLOAT, "Loop start", 0, 0, sampleDuration)
                    .addControlListener(control -> {
                        float control_val = (float) control.getValue();

                        /*** Write your DynamicControl code below this line ***/
                        float current_audio_position = (float)samplePlayer.getPosition();

                        if (current_audio_position < control_val){
                            samplePlayer.setPosition(control_val);
                        }
                        loop_start.setValue(control_val);

                        /*** Write your DynamicControl code above this line ***/
                    });
            /*** End DynamicControl code ***/

            /*************************************************************
             * Create a Float type Dynamic Control pair that displays as a slider and text box
             *
             * Simply type floatBuddyControl to generate this code
             *************************************************************/
            DynamicControl loopEndControl = hb.createControlBuddyPair(this, ControlType.FLOAT, "Loop End", sampleDuration, 0, sampleDuration)
                    .addControlListener(control -> {
                        float control_val = (float) control.getValue();

                        /*** Write your DynamicControl code below this line ***/
                        loop_end.setValue(control_val);
                        /*** Write your DynamicControl code above this line ***/
                    });
            /*** End DynamicControl code ***/


            // Add a control to make sample player start at loop start position

            /*************************************************************
             * Create a Trigger type Dynamic Control that displays as a button
             *
             * Simply type triggerControl to generate this code
             *************************************************************/
            DynamicControl startLoop = hb.createDynamicControl(this, ControlType.TRIGGER, "Start Loop")
                    .addControlListener(control -> {

                        /*** Write your DynamicControl code below this line ***/
                        samplePlayer.setPosition(loop_start.getCurrentValue());
                        /*** Write your DynamicControl code above this line ***/
                    });
            /*** End DynamicControl code ***/
            /******** Write your code above this line ********/
        } else {
            hb.setStatus("Failed sample " + SAMPLE_NAME);
        }
        /*** End samplePlayer code ***/

    }

    /**
     * Set the position on the slider control
     * @param audio_position  The position in seconds
     */
    void setAudioSliderPosition(double audio_position){
        audioSliderPosition.setValue(audio_position);
    }

    /**
     * Set the text in the text audio position slider so it is in this format
     * "hh:mm:ss.t"
     * @param audio_position audio position in seconds
     */
    void setAudioTextPosition(double audio_position){
        double seconds =  audio_position / 1000;

        int tenths = ((int) (seconds * 10)) % 10;
        int minutes = (int) seconds / 60;
        int hours = minutes / 60;

        String position_text = String.format("%02d:%02d:%02d.%d", hours, minutes, ((int) seconds) % 60, tenths);
        audioTextPosition.setValue(position_text);
    }


    /**
     * Creates a thread to update our audio position while we are playing
     * @return the thread that will do updating
     */
    Thread createUpdateThread() {

        /***********************************************************
         * Create a runnable thread object
         * simply type threadFunction to generate this code
         ***********************************************************/
        Thread thread = new Thread(() -> {
            int SLEEP_TIME = 100;
            while (true) {
                /*** write your code below this line ***/
                if (!samplePlayer.isPaused()) {
                    double audio_position = samplePlayer.getPosition();

                    // set the position in our slider and text position display
                    setAudioSliderPosition(audio_position);
                    setAudioTextPosition(audio_position);
                }

                /*** write your code above this line ***/

                try {
                    Thread.sleep(SLEEP_TIME);
                } catch (InterruptedException e) {
                    /*** remove the break below to just resume thread or add your own action***/
                    break;
                    /*** remove the break above to just resume thread or add your own action ***/

                }
            }
        });

        /*** write your code you want to execute before you start the thread below this line ***/

        /*** write your code you want to execute before you start the thread above this line ***/

        thread.start();
        /****************** End threadFunction **************************/

        return thread;
    }

    //<editor-fold defaultstate="collapsed" desc="Debug Start">
    /**
     * This function is used when running sketch in IntelliJ IDE for debugging or testing
     *
     * @param args standard args required
     */
    public static void main(String[] args) {

        try {
            HB.runDebug(MethodHandles.lookup().lookupClass());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //</editor-fold>
}
