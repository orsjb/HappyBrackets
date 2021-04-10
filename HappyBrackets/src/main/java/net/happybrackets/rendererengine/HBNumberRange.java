package net.happybrackets.rendererengine;

import java.lang.annotation.*;


/**
 * Annotation that defines a numeric field that will be exposed via OSC.
 * OSC Port is defined by the oscPort field at a class that extends {@link Renderer}.
 * It accepts fields with the following types: float and int.
 * The min/max is mandatory. If you do not want to specify a min/max, use {@link HBParam}.
 *
 * @author Augusto Dias
 */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target({ElementType.FIELD})
public @interface HBNumberRange {
    /**
     * The minimum value allowed for the field.
     */
    double min();

    /**
     * The maximum value allowed for the field.
     */
    double max();
}
