package net.happybrackets.device.sensors.gpio;

/**
 * Interface for retrieving state of GPIO pin
 */
public interface GPIOInputListener {
    void stateChanged (GPIOInput sensor, boolean new_state);
}
