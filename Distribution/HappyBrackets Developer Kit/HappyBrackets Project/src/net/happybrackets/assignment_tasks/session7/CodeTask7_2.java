package net.happybrackets.assignment_tasks.session7;

import de.sciss.net.OSCMessage;
import net.happybrackets.extras.assignment_autograding.SimpleCheckable;

/**
 *
 * In this task, you are basically reversing the process of the previous task. You should create an OSC message with exactly the same format and content as the previous task. The data for the OSC message is given to you as a single String which you have to parse.
 *
 */
public class CodeTask7_2 implements SimpleCheckable {

    public static void main(String[] args) {
        StringBuffer buf = new StringBuffer();
        String inputData =  "Waveform=SINE\n" +
                            "Note1: MIDI=60, gain=0.1\n" +
                            "Note2: MIDI=63, gain=0.2\n" +
                            "Note3: MIDI=67, gain=0.1\n" +
                            "filtfreq=500\n" +
                            "filtq=0.2";
        OSCMessage[] messages = new OSCMessage[1];
        new CodeTask7_2().task(new Object[]{buf, inputData, messages});
        System.out.println(buf);
    }

    @Override
    public void task(Object... objects) {
        //********** do your work here ONLY **********
        //your objects...
        StringBuffer buf = (StringBuffer)objects[0];
        String inputData = (String)objects[1];
        OSCMessage[] messages = (OSCMessage[])objects[2];
        //do stuff here, remove the following line
        buf.append("Hello World!\n");
        OSCMessage m = null;
        //********** do your work here ONLY **********
        messages[0] = m;
    }
}
