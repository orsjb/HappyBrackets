package net.happybrackets.rendererengine;

import java.lang.annotation.*;

/**
 * Annotation that defines a generic method that will be exposed via OSC.
 * OSC Port is defined by the oscPort field at a class that extends {@link Renderer}.
 * It accepts methods with the following parameter types: String, OSCMessage, float, int and boolean.
 *
 * @author Augusto Dias
 */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target({ElementType.METHOD})
public @interface HBCommand { }
