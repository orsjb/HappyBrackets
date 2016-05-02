package core;
import java.io.Serializable;

import pi.dynamic.DynamoPI;

/**
 * PI Playback Object.
 * @author ollie
 *
 */
public interface PIPO extends Serializable {

	void action(final DynamoPI d);

}
