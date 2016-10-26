package net.happybrackets.assignment_tasks.session7;

import de.sciss.net.OSCMessage;
import net.happybrackets.extras.assignment_autograding.SimpleCheckable;

import java.util.Hashtable;

/**
 *
 * In this task you have to parse incoming MIDI note messages containing a MIDI note number and note velocity.
 * Following the MIDI protocol, any event with a velocity of zero means that the note has ended.
 * Your task is to maintain the hashtable so that it only contains currently active notes (those notes that have started but not ended).
 * Any MIDI note number must either be active or not active (you can't play two of the same note at the same time).
 *
 * In the code below, after the first call of the task function, the Hashtable should contain exactly one entry, with key 60 and value 110.
 * After the second call, there should be two entries. After the third call there should be one entry, and so on.
 *
 * Your code should also respond to an OSC message with address "/panic", which should clear the Hashtable of any active notes.
 *
 */
public class CodeTask7_3 implements SimpleCheckable {

    public static void main(String[] args) {
        CodeTask7_3 task = new CodeTask7_3();
        StringBuffer buf = new StringBuffer();
        Hashtable<Integer, Integer> noteStore = new Hashtable();
        OSCMessage message = null;
        //this task involves several note events coming in...

        message = new OSCMessage("/note", new Object[]{60, 110});
        task.task(new Object[]{buf, noteStore, message});

        message = new OSCMessage("/note", new Object[]{62, 55});
        task.task(new Object[]{buf, noteStore, message});

        message = new OSCMessage("/note", new Object[]{60, 0});
        task.task(new Object[]{buf, noteStore, message});

        message = new OSCMessage("/note", new Object[]{62, 0});
        task.task(new Object[]{buf, noteStore, message});

        System.out.println(buf);
    }

    @Override
    public void task(Object... objects) {
        //********** do your work here ONLY **********
        //your objects...
        StringBuffer buf = (StringBuffer)objects[0];
        Hashtable<Integer, Integer> noteStore = (Hashtable<Integer, Integer>)objects[1];
        OSCMessage message = (OSCMessage)objects[2];
        //do stuff here, remove the following line
        buf.append("Hello World!\n");
        //********** do your work here ONLY **********
    }
}
