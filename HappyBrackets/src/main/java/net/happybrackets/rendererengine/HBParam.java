package net.happybrackets.rendererengine;

import java.lang.annotation.*;

/**
 * Annotation that defines a generic field (number or string) that will be exposed via OSC.
 * OSC Port is defined by the oscPort field at a class that extends {@link Renderer}.
 * It accepts fields with the following types: String, OSCMessage, float, int and boolean.
 * The min/max is mandatory. If you do not want to specify a min/max, use {@link HBParam}.
 *
 * @author Augusto Dias
 */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target({ElementType.FIELD})
public @interface HBParam {
}

