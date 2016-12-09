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

package net.happybrackets.assignment_tasks.session6;

import net.beadsproject.beads.core.AudioContext;
import net.happybrackets.extras.assignment_autograding.BeadsChecker;
import net.happybrackets.extras.assignment_autograding.SimpleCheckable;

import java.util.Hashtable;

/**
 * In this task, you are being passed a Hashtable with three elements, a MIDI note, a base-freq and a set of octave frequencies. The data in the Hashtable may be wrong. The MIDI value is always correct. The base-freq should correspond to the MIDI value, and the octaves value should contain for floats representing the frequencies of the four octaves above the base-freq.
 *
 * You will be passed one Hashtable, as in the example below. You should do two things:
 * 1) check the data in the Hashtable and report which information is wrong. e.g., if the base-freq is wrong then output a line saying base-freq. If the first element in the octaves array is wrong then output a line saying octaves0, then octaves1 for the second element and so on.
 * 2) correct the data in the Hashtable. After your task is completed, the Hashtable will be checked for correctness, including making sure there are no extra elements.
 *
 */
public class CodeTask6_2 implements BeadsChecker.BeadsCheckable {

    public static void main(String[] args) {
        StringBuffer buf = new StringBuffer();
        Hashtable<String, Object> testShare = new Hashtable<>();
        testShare.put("midi", 60);
        testShare.put("base-freq", 180.0f);
        testShare.put("octaves", new float[] {320.0f, 640.0f, 1280.0f, 2560.0f} );
        new CodeTask6_2().task(null, buf, new Object[]{testShare});
        System.out.println(buf);
    }

    @Override
    public void task(AudioContext ac, StringBuffer buf, Object... objects) {
        //********** do your work here ONLY **********
        //your objects...
        Hashtable<String, Object> share = (Hashtable<String, Object>)objects[0];
        //do stuff here, remove the following line
        buf.append("Hello World!\n");
        //********** do your work here ONLY **********
    }
}
