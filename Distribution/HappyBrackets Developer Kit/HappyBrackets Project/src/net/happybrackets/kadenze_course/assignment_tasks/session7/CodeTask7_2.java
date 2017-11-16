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

package net.happybrackets.kadenze_course.assignment_tasks.session7;

import de.sciss.net.OSCMessage;
import net.beadsproject.beads.core.AudioContext;
import net.happybrackets.extras.assignment_autograding.BeadsChecker;

/**
 * In this task, you are basically reversing the process of the previous task. You should create an OSC message with
 * exactly the same format and content as the previous task. The data for the OSC message is given to you as a single
 * String which you have to parse.
 *
 * A big clue to how to do this is to use the split() method in String to split the input data up into smaller Strings.
 */
public class CodeTask7_2 implements BeadsChecker.BeadsCheckable {

    public static void main(String[] args) {
        StringBuffer buf = new StringBuffer();
        String inputData = "Waveform=SINE\n" +
                "Note1: MIDI=60, gain=0.1\n" +
                "Note2: MIDI=63, gain=0.2\n" +
                "Note3: MIDI=67, gain=0.1\n" +
                "filtfreq=500\n" +
                "filtq=0.2";
        OSCMessage[] messages = new OSCMessage[1];
        new CodeTask7_2().task(null, buf, new Object[]{inputData, messages});
        System.out.println(buf);
    }

    @Override
    public void task(AudioContext ac, StringBuffer buf, Object... objects) {
        //********** do your work here ONLY **********
        //your objects...
        String inputData = (String) objects[0];
        OSCMessage[] messages = (OSCMessage[]) objects[1];
        //do stuff here, remove the following line, create the OSCMessage 'm' and fill it up as required. This is returned.
        buf.append("Hello World!\n");
        OSCMessage m = null;
        //********** do your work here ONLY **********
        messages[0] = m;
    }
}
