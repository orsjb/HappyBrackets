package net.happybrackets.core;

import java.io.Serializable;

/**
 * Interface to send classes notification that a rest has occurred so it can cancel any threads
 * or resources it has allocated
 */
public interface HBReset extends Serializable {
    void doReset();
}
