package net.happybrackets.core;

import net.happybrackets.device.HB;

import java.io.Serializable;

/**
 * PI Playback Object.
 * @author ollie
 *
 */
public interface HBAction extends Serializable {

	void action(final HB d);

}
