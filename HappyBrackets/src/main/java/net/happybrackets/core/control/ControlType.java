package net.happybrackets.core.control;

/**
 * Define the types of control message we want to send
 * INT will send an integer value
 * TEXT will send a string
 * FLOAT will send a floating point value
 * BOOLEAN will send a 1 or 0 value
 * TRIGGER will send an event that contains no arguments and is used for triggering some event
 *
 */
public enum ControlType {
    INT,
    TEXT,
    FLOAT,
    BOOLEAN,
    TRIGGER/*,
    Add In Version 3.0
    OBJECT*/
}
