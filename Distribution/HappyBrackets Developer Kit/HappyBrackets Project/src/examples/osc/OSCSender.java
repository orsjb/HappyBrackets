package examples.osc;

import net.happybrackets.core.HBAction;
import net.happybrackets.core.HBReset;
import net.happybrackets.core.OSCUDPSender;
import net.happybrackets.core.control.*;
import net.happybrackets.device.HB;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

/**
 * This example will show how to send an OSC Message
 */
public class OSCSender implements HBAction, HBReset {
    // Change to the number of audio Channels on your device
    final int NUMBER_AUDIO_CHANNELS = 1;

    // This variable will become true when the composition is reset
    boolean compositionReset = false;

    OSCUDPSender oscSender = new OSCUDPSender();

    String targetAddress = "127.0.0.1";
    String OSCAddress = "/myAddress";
    int oscPort =  9000;

    Object oscArgument = null;

    // This is the Control that we will display where we are sending
    TextControl target_display;
    TextControl resultDisplay;

    @Override
    public void action(HB hb) {
        /***** Type your HBAction code below this line ******/
        // remove this code if you do not want other compositions to run at the same time as this one
        //hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");

        // We will make a display to see
        target_display = new TextControlSender(this, "Final Target", "");
        resultDisplay = new TextControlSender(this, "Send result", "");



        // Make an Input for our Controls
        new TextControl(this, "Set Address", targetAddress) {
            @Override
            public void valueChanged(String control_val) {/* Write your DynamicControl code below this line */

                targetAddress = control_val;
                updateTargetDisplay();
                /* Write your DynamicControl code above this line */
            }
        };/* End DynamicControl target code */


        // make a place to enter your oscPort
        new IntegerTextControl(this, "Set Port", oscPort) {
            @Override
            public void valueChanged(int control_val) {/* Write your DynamicControl code below this line */
                oscPort = control_val;
                updateTargetDisplay();
                /* Write your DynamicControl code above this line */
            }
        };/* End DynamicControl integerTextControl code */

        new TextControl(this, "OSC Name", OSCAddress) {
            @Override
            public void valueChanged(String control_val) {/* Write your DynamicControl code below this line */
                OSCAddress = control_val;
                /* Write your DynamicControl code above this line */
            }
        };/* End DynamicControl textControl code */


        // Only one of these messages will send
        new IntegerTextControl(this, "Set Int Arg", 0) {
            @Override
            public void valueChanged(int control_val) {/* Write your DynamicControl code below this line */
                oscArgument = control_val;

                if (oscSender.send(HB.createOSCMessage(OSCAddress, oscArgument), targetAddress, oscPort)) {
                    displaySendResult("Sent " + OSCAddress + " " + oscArgument);
                } else {
                    displaySendResult("Failed Send " + oscSender.getLastError());
                }

                /* Write your DynamicControl code above this line */
            }
        };/* End DynamicControl integerTextControl code */

        // Lets send a boolean
        new BooleanControl(this, "Set Bool Arg", false) {
            @Override
            public void valueChanged(Boolean control_val) {/* Write your DynamicControl code below this line */
                oscArgument = control_val;
                if (oscSender.send(HB.createOSCMessage(OSCAddress, oscArgument), targetAddress, oscPort)) {
                    displaySendResult("Sent " + OSCAddress + " " + oscArgument);
                } else {
                    displaySendResult("Failed Send " + oscSender.getLastError());
                }

                /* Write your DynamicControl code above this line */
            }
        };/* End DynamicControl booleanControl code */

        // Lets send a float
        new FloatTextControl(this, "Set float", 0) {
            @Override
            public void valueChanged(double control_val) {/* Write your DynamicControl code below this line */
                oscArgument = control_val;
                if (oscSender.send(HB.createOSCMessage(OSCAddress, oscArgument), targetAddress, oscPort)) {
                    displaySendResult("Sent " + OSCAddress + " " + oscArgument);
                } else {
                    displaySendResult("Failed Send " + oscSender.getLastError());
                }

                /* Write your DynamicControl code above this line */
            }
        };/* End DynamicControl code floatTextControl */

        // lets send a string

        new TextControl(this, "set String Message", "") {
            @Override
            public void valueChanged(String control_val) {/* Write your DynamicControl code below this line */
                oscArgument = control_val;
                if (oscSender.send(HB.createOSCMessage(OSCAddress, oscArgument), targetAddress, oscPort)) {
                    displaySendResult("Sent " + OSCAddress + " " + oscArgument);
                } else {
                    displaySendResult("Failed Send " + oscSender.getLastError());
                }

                /* Write your DynamicControl code above this line */
            }
        };/* End DynamicControl textControl code */


        // resend our last message
        new TriggerControl(this, "Resend Last Message") {
            @Override
            public void triggerEvent() {/* Write your DynamicControl code below this line */
                if (oscSender.send(HB.createOSCMessage(OSCAddress, oscArgument), targetAddress, oscPort)) {
                    displaySendResult("Sent " + OSCAddress + " " + oscArgument);
                } else {
                    displaySendResult("Failed Send " + oscSender.getLastError());
                }

                /* Write your DynamicControl code above this line */
            }
        };/* End DynamicControl triggerControl code */


        updateTargetDisplay();
        /***** Type your HBAction code above this line ******/
    }


    /**
     * Display where we will send our messages to
     */
    void updateTargetDisplay(){
        target_display.setValue("Send to " + targetAddress + " Port " + oscPort);
    }

    /**
     * Display the result to the control
     * @param message the message to display
     */
    void displaySendResult (String message){
        resultDisplay.setValue(message);
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
