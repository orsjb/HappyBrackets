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

import java.util.Hashtable;

/**
 * In this task you have to parse incoming MIDI note messages containing a MIDI note number and note velocity.
 * Following the MIDI protocol, any event with a velocity of zero means that the note has ended.
 * Your task is to maintain the hashtable so that it only contains currently active notes (those notes that have started
 * but not ended).
 * Any MIDI note number must either be active or not active (you can't play two of the same note at the same time).
 *
 * In the code below, after the first call of the task function, the Hashtable should contain exactly one entry, with
 * key 60 and value 110.
 * After the second call, there should be two entries. After the third call there should be one entry, and so on.
 *
 * Your code should also respond to an OSC message with address "/panic", which should clear the Hashtable of any active
 * notes.
 */
public class CodeTask7_3 implements BeadsChecker.BeadsCheckable {

    public static void main(String[] args) {
        CodeTask7_3 task = new CodeTask7_3();
        StringBuffer buf = new StringBuffer();
        Hashtable<Integer, Integer> noteStore = new Hashtable();
        OSCMessage message = null;
        //this task involves several note events coming in...

        message = new OSCMessage("/note", new Object[]{60, 110});
        task.task(null, buf, new Object[]{noteStore, message});

        message = new OSCMessage("/note", new Object[]{62, 55});
        task.task(null, buf, new Object[]{noteStore, message});

        message = new OSCMessage("/note", new Object[]{60, 0});
        task.task(null, buf, new Object[]{noteStore, message});

        message = new OSCMessage("/note", new Object[]{62, 0});
        task.task(null, buf, new Object[]{noteStore, message});

        System.out.println(buf);
    }

    @Override
    public void task(AudioContext ac, StringBuffer buf, Object... objects) {
        //********** do your work here ONLY **********
        //your objects...
        Hashtable<Integer, Integer> noteStore = (Hashtable<Integer, Integer>) objects[0];
        OSCMessage message = (OSCMessage) objects[1];
        //do stuff here, remove the following line
        buf.append("Hello World!\n");
        //********** do your work here ONLY **********
    }
}
