package net.happybrackets.core.control;

/**
 * Define the different types of scope we want for our controls
 * A control may be instatiated per sketch, per class, per device, or globally
 *
 * UNIQUE - a new independent control is created and only sends messages to registered listeners
 * SKETCH - messages are also sent to other controls with the same name and scope belonging to other instances in the same sketch.
 * CLASS - messages are also sent to other controls with the same name and scope belonging to other instances of the same class.
 * DEVICE - messages are also sent to other controls with the same name and scope on the same device
 * GLOBAL - messages are also sent to other controls with the same name and scope on the entire network.
 */
public enum ControlScope {
    /**
     * a new independent control is created and only sends messages to registered listeners
     */
    UNIQUE,
    /**
     * messages are sent to registered listeners and also other controls with the same name and scope belonging to other instances in the same sketch.
     */
    SKETCH,

    /**
     * messages are sent to registered listeners and also other controls with the same name and scope belonging to other instances of the same class.
     */
    CLASS,

    /**
     * messages are sent to registered listeners and also to other controls with the same name and scope on the same device
     */
    DEVICE,

    /**
     * messages are sent to registered listeners and also sent to other controls with the same name and scope on the entire network.
     */
    GLOBAL


}
