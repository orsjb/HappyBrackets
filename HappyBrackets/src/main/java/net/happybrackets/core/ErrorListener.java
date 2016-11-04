package net.happybrackets.core;

/**
 * Generic interface for processes that wish to receive notification of errors occuring in other components.
 * Used to communicate errors to the plugin if available.
 */
public interface ErrorListener {
    /**
     * @param clazz The class the error occurred in.
     * @param description The description of the error. May be null.
     * @param ex The exception that was thrown if applicable, otherwise null.
     */
    public void errorOccurred(Class clazz, String description, Exception ex);
}
