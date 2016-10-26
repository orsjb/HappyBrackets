package net.happybrackets.assignment_tasks.Session8;

import net.happybrackets.extras.assignment_autograding.SimpleCheckable;

/**
 *
 * In this task detect when an event has occurred, based on the threshold.
 *
 * Whenever an event occurs print the index of the event to the StringBuffer.
 *
 * For example, if the input stream was {0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0} then the output would be:
 *
 * 4
 * 10
 *
 */
public class CodeTask8_2 implements SimpleCheckable {

    public static void main(String[] args) {
        StringBuffer buf = new StringBuffer();
        float[] sensorData = new float[]{0.1f, 0.13f, 0.154f, 0.1234f, 0.14523f, 0.12965f, 0.1f};
        float threshold = 0.1f;
        new CodeTask8_2().task(new Object[]{buf, sensorData, threshold});
        System.out.println(buf);
    }

    @Override
    public void task(Object... objects) {
        //********** do your work here ONLY **********
        //your objects...
        StringBuffer buf = (StringBuffer)objects[0];
        float[] sensordata = (float[])objects[1];
        float threshold = (float)objects[2];
        //do stuff here, remove the following line
        buf.append("Hello World!\n");
        //********** do your work here ONLY **********
    }
}
