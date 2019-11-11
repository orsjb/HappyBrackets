package net.happybrackets.core.control;

import java.net.InetAddress;

/**
 * Define the different types of scope we want for our controls
 * Although similar to the send and receive objects in Max in that the name and type
 * parameter of the DynamicControl determines message interconnection,
 * the {@link ControlScope} dictates how far (in
 * a topological sense) the object can reach in order to communicate with other
 * {@link DynamicControl} objects.  The different scopes available are:
 * <br>
 * <br>{@link ControlScope#UNIQUE} - a new independent control is created and only sends messages to registered listeners
 * <br>{@link ControlScope#SKETCH} - messages are also sent to other controls with the same name and scope belonging to other instances in the same sketch.
 * <br>{@link ControlScope#CLASS} - messages are also sent to other controls with the same name and scope belonging to other instances of the same class.
 * <br>{@link ControlScope#DEVICE} - messages are also sent to other controls with the same name and scope on the same device
 * <br>{@link ControlScope#GLOBAL} - messages are also sent to other controls with the same name and scope on the entire network.
 * <br>{@link ControlScope#TARGET} - messages are also sent to other controls with the same name and scope on specific or targeted devices on the network.
 */
public enum ControlScope {
    /**
     * An independent control that only sends messages to the GUI and not to other Controls.
     */
    UNIQUE,
    /**
     * Messages are sent to all devices of the same {@link ControlType}, {@link ControlScope} and name to all instances of the same  {@link Object} as the first parameter of the control constructor.
     */
    SKETCH,

    /**
     * Messages are sent to all devices of the same {@link ControlType}, {@link ControlScope} and name to all instances of the same {@link Class} as the first parameter of the control constructor.
     */
    CLASS,

    /**
     * Messages are sent to all devices of the same {@link ControlType}, {@link ControlScope} and name on the same device
     */
    DEVICE,

    /**
     * Messages are sent to all devices of the same {@link ControlType}, {@link ControlScope} and name on the entire network.
     */
    GLOBAL,

    /**
     * Messages are sent to registered listeners with the same scope and specific or targeted devices on the network.
     * See {@link DynamicControlParent#setControlTarget(String...)} and {@link DynamicControlParent#setControlTarget(InetAddress...)} for specific details on
     * sending messages to a specific target address.
     */
    TARGET


}
