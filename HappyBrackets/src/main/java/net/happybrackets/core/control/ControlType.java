package net.happybrackets.core.control;

/**
 * Define the types of control message we want to send in {@link DynamicControl} objects.
 * <br>{@link ControlType#INT} will send an integer value
 * <br>{@link ControlType#TEXT}  will send a string
 * <br>{@link ControlType#FLOAT}  will send a floating point value
 * <br>{@link ControlType#BOOLEAN}  will send a true or false
 * <br>{@link ControlType#TRIGGER}  will send an event that contains no arguments and is used for triggering some event
 * <br>{@link ControlType#OBJECT} will send whole classes
 *
 */
public enum ControlType {
    /**
     * Integer messages enable sending integer value
     * See {@link IntegerControl} for examples
     */
    INT,

    /**
     * Text messages enable sending string values
     * See {@link TextControl} for examples
     */
    TEXT,

    /**
     * Float messages enable sending double floating point messages
     * See {@link FloatControl} for examples
     */
    FLOAT,

    /**
     * Boolean messages enable sending messages with a true or false value
     * See {@link BooleanControl}  for examples
     */
    BOOLEAN,

    /**
     * Trigger messages enable sending void messages, or messages without a value, similar to a bang in Max
     * See {@link TriggerControl} for examples
     */
    TRIGGER,

    /**
     * Object messages enable complete predefined classes as messages, such as {@link TripleAxisMessage}, or classes you define yourself in your code
     * See {@link ClassObjectControl} for examples
     */
    OBJECT
}
