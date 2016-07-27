package net.happybrackets.tutorial.session8;

import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;

/**
 * Created by ollie on 6/06/2016.
 *
 * The following code creates noise, just like the Noise object, except it does so using a custom Function class.
 *
 * (Note this is much less efficient than using the Noise object because it actually uses a random number generator, whereas the Noise class plays back a readymade buffer of generated noise. Also, the Function object is a bit less efficient because it makes an individual method call each time it calculates a new sample).
 *
 * Change the code so that the Function object generates a saw tooth wave, oscillating between -0.1 and +0.1, with a frequency defined by a float variable 'freq'. To do this you will need to know the sample rate, using ac.getSampleRate().
 *
 *
 */
public class CodeTask8_3 implements HBAction {


    @Override
    public void action(HB hb) {


    }
}
