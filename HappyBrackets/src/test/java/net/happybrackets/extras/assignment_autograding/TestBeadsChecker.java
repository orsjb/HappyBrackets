package net.happybrackets.extras.assignment_autograding;

/**
 * Created by ollie on 8/08/2016.
 */
public class TestBeadsChecker {

//    public static void main(String[] args) throws IOException {
//
//        HelloWorldBeads testCode = new HelloWorldBeads();
//        AudioContext ac = new AudioContext(new NonrealtimeIO());
//
//        //set up recorder
//        RecordToFile rtf = new RecordToFile(ac, 2, new File("result.wav"));
//        rtf.addInput(ac.out);
//        ac.out.addDependent(rtf);
//
//        //here we're testing
//        String result = testCode.task(ac);
//
//        //run non realtime
//        DelayTrigger dt = new DelayTrigger(ac, 10000, new Bead() {
//            @Override
//            protected void messageReceived(Bead bead) {
//                //TODO we should be able to run snapshots of the audio at different times
//                //We can actually do this with a Clock!
//            }
//        });
//
//        System.out.println("completed " + ac.getTime());
//
//        ac.runForNMillisecondsNonRealTime(10000);
//        System.out.println("completed " + ac.getTime());
//
//        //close record stream
//        rtf.kill();
//
//        //ac
//        ac.printCallChain();
//
//    }

}
