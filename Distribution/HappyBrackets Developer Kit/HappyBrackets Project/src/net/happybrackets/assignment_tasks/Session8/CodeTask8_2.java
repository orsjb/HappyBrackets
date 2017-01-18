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
 * In this task detect when a threshold event has occurred.
 * This technique can be used to play a sound if a threshold is exceeded.
 *
 * Whenever a threshold is exceeded print the index of the event to the StringBuffer.
 * Don't print the index all the time it is exceeded, just when it goes from below the threshold to above the threshold.
 *
 * For example, if the input stream was {0.1, 0.05, 0.06, 0.03, 0.21, 0.25, 0.24, 0.07, 0.05, 0.03, 0.29, 0.13}
 * and the threshold was 0.1, then the contents of the string buffer would be:
 * 4
 * 10
 *
 *
 * In practice if you play sounds every time the threshold is exceeded you'll often have 'false starts', where the sound
 * stutters to begin with as the threshold is exceeded a few times to begin with. To avoid this, you can stop listening
 * to the 'threshold exceeded' message as the trigger for playback until the sound has completed playing back.
 *
 */
public class CodeTask8_2 implements BeadsChecker.BeadsCheckable {

    public static void main(String[] args) {
        StringBuffer buf = new StringBuffer();
        float[] sensorData = new float[]{0.01f, 0.13f, 0.154f, 0.1234f, 0.14523f, 0.12965f, 0.1f,
        0.005f,0.003f};
        float threshold = 0.1f;
        new CodeTask8_2().task(null, buf, new Object[]{sensorData, threshold});
        System.out.println(buf);
    }

    @Override
    public void task(AudioContext ac, StringBuffer buf, Object... objects) {
        //********** do your work here ONLY **********
        //your objects...
        float[] sensordata = (float[]) objects[0]; // imports the sensorData from above
        float threshold = (float) objects[1]; // imports the threshold

        //do stuff here, remove the following line
        buf.append("Hello World!\n");

        //********** do your work here ONLY **********
    }
}
