package examples.osc;

import de.sciss.net.OSCMessage;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.HBReset;
import net.happybrackets.core.OSCUDPListener;
import net.happybrackets.core.OSCUDPReceiver;
import net.happybrackets.core.control.TextControl;
import net.happybrackets.core.control.TextControlSender;
import net.happybrackets.device.HB;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.SocketAddress;

/**
 * This composition will receive OSC on Port 9000
 */
public class OSCReceiveValues implements HBAction, HBReset {
    // Change to the number of audio Channels on your device
    final int NUMBER_AUDIO_CHANNELS = 1;

    // This variable will become true when the composition is reset
    boolean compositionReset = false;

    @Override
    public void action(HB hb) {
        /***** Type your HBAction code below this line ******/
        // remove this code if you do not want other compositions to run at the same time as this one
        //hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");

        // This is where we will display out Message
        TextControl receivedMessageControl = new TextControlSender(this, "Received Message", "");

        /* type osclistener to create this code */
        OSCUDPListener oscudpListener = new OSCUDPListener(9000) {
            @Override
            public void OSCReceived(OSCMessage oscMessage, SocketAddress socketAddress, long l) {
                /* type your code below this line */
                // first display the source of message and message name
                String display_val = socketAddress.toString() + ": " + oscMessage.getName();

                for (int i = 0; i < oscMessage.getArgCount(); i++){
                    // add each arg to display message
                    display_val = display_val + " " + oscMessage.getArg(i);
                }

                ;
                receivedMessageControl.setValue(display_val);
                /* type your code above this line */
            }
        };
        if (oscudpListener.getPort() < 0){ //port less than zero is an error
            String error_message =  oscudpListener.getLastError();
            System.out.println("Error opening port " + 9000 + " " + error_message);
        } /** end oscListener code */

        /***** Type your HBAction code above this line ******/
    }


    /**
     * Add any code you need to have occur when a reset occurs
     */
    @Override
    public void doReset() {
        compositionReset = true;
        /***** Type your HBReset code below this line ******/

        /***** Type your HBReset code above this line ******/
    }

    //<editor-fold defaultstate="collapsed" desc="Debug Start">

    /**
     * This function is used when running sketch in IntelliJ IDE for debugging or testing
     *
     * @param args standard args required
     */
    public static void main(String[] args) {

        try {
            HB.runDebug(MethodHandles.lookup().lookupClass());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //</editor-fold>
}
