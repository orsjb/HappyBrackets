package net.happybrackets.device;

import net.happybrackets.device.sensors.gpio.GPIODigitalOutput;

import java.io.IOException;

/**
 * This class will control hard muting the audio output when there is no composition loaded into Happy Brackets
 *
 */
public class MuteControl {
    // THis is our default GPIO for Muting Audio
    public static final int DEFAULT_MUTE_OUTPUT = 27;
    private final int muteIO;
    GPIODigitalOutput outputPin = null;

    /**
     * Creates a Mute control defined by GPIO Number
     * @param gpio the GPIO we are using. These are Pi4J numbers
     * @throws IOException if unable to create output pin
     */
    public MuteControl(int gpio) throws Exception
    {
        outputPin = GPIODigitalOutput.getOutputPin(gpio);
        if (outputPin == null){
            throw new Exception("Unable to create GPIO Mute output on " + gpio);
        }
        // We will Set unprovisioning protection
        outputPin.protectUnprovision(true);

        // we will not be muted to start
        outputPin.setState(true);
        muteIO = gpio;
    }

    /**
     * Creates a MuteControl using Default GPIO Number
     * @throws Exception An exception thrown if unable to create based on GPIO
     */
    public MuteControl() throws Exception{
        this(DEFAULT_MUTE_OUTPUT);
    }

    /**
     * Gets the GPIO that we will use for Muting
     * @return the GPIO for Muting
     */
    public int getMuteIO() {
        return muteIO;
    }

    /**
     * Get the state of Mute
     * @return whether we are muted
     */
    public boolean getMuteState(){
        return !outputPin.getState();
    }

    /**
     * Mute or unmute current output. Muting will set output to low
     * @param mute true if we want to mute
     * @return previous state of mute
     */
    synchronized public boolean muteOutput(boolean mute){
        boolean current_state = getMuteState();
        outputPin.setState(!mute);
        return current_state;
    }
}
