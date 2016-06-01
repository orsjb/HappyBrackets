package net.happybrackets.core;

import net.happybrackets.device.dynamic.HB;

import java.io.Serializable;

/**
 * PI Playback Object.
 * @author ollie
 *
 */
public interface DynamoAction extends Serializable {

	void action(final HB d);

}
