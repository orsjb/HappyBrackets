/*
 * Copyright 2016 Ollie Bown
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.happybrackets.assignment_tasks.session8;

import net.beadsproject.beads.core.AudioContext;
import net.happybrackets.extras.assignment_autograding.BeadsChecker;

/**
 * In this task you should populate the array smoothSensorData by running a sliding window average over the array
 * sensorData, using the window length specified by windowLength.
 *
 * You should use a window with 5 samples, and you should calculate the arithmetic mean.
 *
 * You could just start the window with its left most extent lined up to the beginning of the values, but that would
 * mean that the output would be shorter than the input.
 *
 * Instead, to make sure your smoothed output is the same length as the input you should:
 * Add copies of the first sample so that the window's middle is centred on the 1st sample for the 1st calculation, and
 * add copies of the last sample so that the window middle is centred on the last sample for the final calculation.
 *
 */


public class CodeTask8_1 implements BeadsChecker.BeadsCheckable {

    public static void main(String[] args) {
        StringBuffer buf = new StringBuffer();
        float[] sensorData = new float[]{0.1f, 0.13f, 0.154f, 0.1234f, 0.14523f, 0.12965f, 0.1f};
        float[] smoothSensorData = new float[sensorData.length];
        int windowLength = 3;
        new CodeTask8_1().task(null, buf, new Object[]{sensorData, smoothSensorData, windowLength});
        System.out.println(buf);
    }

    @Override
    public void task(AudioContext ac, StringBuffer buf, Object... objects) {
        //********** do your work here ONLY **********
        //your objects...
        float[] sensorData = (float[]) objects[0];
        float[] smoothSensorData = (float[]) objects[1];
        int windowLength = (int) objects[2];


        // do your task here, and remove the following line
        buf.append("Hello World!\n");

        //********** do your work here ONLY **********
    }
}
