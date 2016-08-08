package net.happybrackets.extras.assignment_autograding;

import com.google.gson.Gson;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.core.io.NonrealtimeIO;
import net.beadsproject.beads.ugens.DelayTrigger;
import net.beadsproject.beads.ugens.RecordToFile;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * Created by ollie on 27/07/2016.
 */
public class BeadsChecker {

    public interface BeadsCheckable {
        public String task(AudioContext ac, Object... args);
    }

    public BeadsChecker(BeadsCheckable testCode) {
        AudioContext ac = new AudioContext(new NonrealtimeIO());

        //set up recorder
        RecordToFile rtf = null;
        try {
            rtf = new RecordToFile(ac, 2, new File("result.wav"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        rtf.addInput(ac.out);
        ac.out.addDependent(rtf);

        //here we're testing
        String result = testCode.task(ac);

        //run non realtime
        DelayTrigger dt = new DelayTrigger(ac, 10000, new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                //TODO we should be able to run snapshots of the audio at different times
                //We can actually do this with a Clock!
            }
        });

        System.out.println("completed " + ac.getTime());

        ac.runForNMillisecondsNonRealTime(10000);
        System.out.println("completed " + ac.getTime());

        //close record stream
        rtf.kill();

        //ac
        ac.printCallChain();

    }

}
