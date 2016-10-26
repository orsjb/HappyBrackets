package net.happybrackets.assignment_tasks.Session8;

import net.happybrackets.extras.assignment_autograding.SimpleCheckable;

/**
 *
 * TODO
 *
 * Detect mangitude events.
 * Integrate gyro and accelerometer.
 *
 */
public class CodeTask8_2 implements SimpleCheckable {

    public static void main(String[] args) {
        StringBuffer buf = new StringBuffer();
        new CodeTask8_2().task(new Object[]{buf});
        System.out.println(buf);
    }

    @Override
    public void task(Object... objects) {
        //********** do your work here ONLY **********
        //your objects...
        StringBuffer buf = (StringBuffer)objects[0];
        //do stuff here, remove the following line
        buf.append("Hello World!\n");
        //********** do your work here ONLY **********
    }
}
