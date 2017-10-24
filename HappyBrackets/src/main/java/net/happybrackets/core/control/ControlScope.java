package net.happybrackets.core.control;

/**
 * Define the different types of scope we want for our controls
 * A control may be instatiated per sketch, per class, per device, or globally
 */
public enum ControlScope {
    SKETCH,
    CLASS,
    DEVICE,
    GLOBAL
}
