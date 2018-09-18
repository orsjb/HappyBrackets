package examples.events.clocks;

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.instruments.WaveModule;
import net.happybrackets.core.scheduling.Clock;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.AccelerometerListener;

import java.lang.invoke.MethodHandles;

/**
 * This sketch will create a WaveModule will play a whole tone scale through a clock
 * The frequency of the WaveModule is calculated by using the MIDI to Frequency function
 * The note number is incremented by two each clock beat, resulting in a whole tone scale
 * When the note number has reached our defined END_NOTE, we will move back to start note
 *
 * The x value of an accelerometer is used to change the clock interval
 */
public class SensorControlledBeadsClock implements HBAction {

    final int NUMBER_AUDIO_CHANNELS = 1; // define how many audio channels our device is using
    
    // These parameters need to be class variables so they can be accessed within the clock
    final int START_NOTE = 40; // this is the MIDI number of first note
    final int END_NOTE = 110;  // this is the last note we will play
    // define a variable to calulate and store next frequency
    int currentNote = START_NOTE;

    @Override
    public void action(HB hb) {
        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");

        WaveModule player = new WaveModule();
        player.setBuffer(Buffer.SQUARE);
        player.setMidiFequency(currentNote);
        player.connectTo(hb.ac.out);

        final double CLOCK_INTERVAL = 500;

        /************************************************************
         * Create a clock with a interval based on the clock duration
         *
         * To create this, just type beadsClockTimer
         ************************************************************/
        // Create a clock with beat interval of CLOCK_INTERVAL ms
        net.beadsproject.beads.ugens.Clock clock = new net.beadsproject.beads.ugens.Clock(500);


        // let us handle triggers
        clock.addMessageListener(new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                // see if we are at the start of a beat
                boolean start_of_beat = clock.getCount() % clock.getTicksPerBeat() == 0;
                if (start_of_beat) {
                    /*** Write your code to perform functions on the beat below this line ****/

                    if (currentNote < END_NOTE) {
                        // add 2 to the current note
                        currentNote += 2;
                    }
                    else {
                        currentNote = START_NOTE;
                    }

                    player.setMidiFequency(currentNote);

                    /*** Write your code to perform functions on the beat above this line ****/
                } else {
                    /*** Write your code to perform functions off the beat below this line ****/

                    /*** Write your code to perform functions off the beat above this line ****/
                }
            }
        });
        /*********************** end beadsClockTimer **********************/

        // let us make the clock speed dependant upon the X value of the accelerometer

        // we will scale the value between 1 and 1 /3 the initial clock interval
        clock.setIntervalEnvelope(hb.getAccelerometer_X(CLOCK_INTERVAL, CLOCK_INTERVAL / 3));


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
