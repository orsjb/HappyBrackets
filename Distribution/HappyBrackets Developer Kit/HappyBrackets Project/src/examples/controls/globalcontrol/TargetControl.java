package examples.controls.globalcontrol;

import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.*;
import net.happybrackets.device.HB;
import net.happybrackets.device.network.DeviceConnectedEventListener;

import java.lang.invoke.MethodHandles;
import java.net.InetAddress;
import java.util.Collection;

/**
 * This sketch sends a Target dynamic control message
 *
 * The known devices are loaded and a set of controls that display the device name, add the device as a target, and remove the device as a target
 *
 * Two target controls are created - one for sending and one for receiving.  The sending control has targets added and removed from it
 *
 * Adding a target will cause messages to be sent to that device, which will receive the value through the receivingControl.
 *
 * Each time a new device connects to the network, we will receive a DeviceConnectedEventListener event which will give us details about it
 * We will then add controls for it
 * This will also set the status on that device
 *
 * Run this on two or more different devices
 */
public class TargetControl implements HBAction, DeviceConnectedEventListener {
    /**********************************************
     We need to make our counter a class variable so
     it can be accessed within the message handler
    ***********************************************/
    // Now create an index counter to select a frequency
    int counter = 0;

    // This is the control we will use to send messages to targets
    IntegerControl sendingControl = null;

    @Override
    public void action(HB hb) {

        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");



        // Create a control for displaying the name of the device that sent us the message
        TextControl sendingDevice = new TextControl(this, "Sending Device", "");


        // Type intBuddyControl to generate this code. This is set to Target Scope
        sendingControl = new IntegerControl(this, "Target Control", 0) {
            @Override
            public void valueChanged(int control_val) {// Write your DynamicControl code below this line
                // Write your DynamicControl code above this line 
            }
        }.setDisplayRange(-100, 100, DynamicControl.DISPLAY_TYPE.DISPLAY_ENABLED_BUDDY).setControlScope(ControlScope.TARGET);// End DynamicControl targetControl code


        // THis is the control that will receive messages targetted to this device
        IntegerControl receivingControl = new IntegerControl(this, "Target Control", 0) {
            @Override
            public void valueChanged(int control_val) {// Write your DynamicControl code below this line 
                sendingDevice.setValue(getSendingDevice() + " sent " + control_val);
                hb.setStatus("Rx " + control_val);
                // Write your DynamicControl code above this line 
            }
        }.setControlScope(ControlScope.TARGET);// End DynamicControl receivingControl code

        // get the names of all the devices we know about
        Collection<String> knownDevices = hb.getKnownDeviceNames();

        // iterate through each device name and create controls for it
        for (String device_name:
             knownDevices) {

            InetAddress device_address = hb.getDeviceAddress(device_name);
            if (device_address != null) {
                addDeviceControls(device_name, device_address);

            }
        }

        // Now add ourselves as a listener for new devices
        hb.addDeviceConnectedEventListener(this);

    }

    /**
     * Add device controls for a device given it's name and {@link InetAddress}
     * @param device_name the device name
     * @param device_address the Address of the device
     */
    void addDeviceControls(String device_name, InetAddress device_address){
        String display_name = device_name + " - " + device_address.getHostAddress();
        TextControl deviceNameDisplay = new TextControl(this, "Device Name", display_name);

        // create controls for adding and removing devices
        TriggerControl addControl = new TriggerControl(this, "Add Target " + device_name) {
            @Override
            public void triggerEvent() {// Write your DynamicControl code below this line
                // add the device as a target
                System.out.println("Add device target " + device_name);
                sendingControl.addControlTarget(device_name);
                // Write your DynamicControl code above this line
            }
        };// End DynamicControl addControl code

        // Type triggerControl to generate this code
        TriggerControl removeControl = new TriggerControl(this, "Remove target " + device_name) {
            @Override
            public void triggerEvent() {// Write your DynamicControl code below this line
                sendingControl.removeControlTarget(device_name);
                // Write your DynamicControl code above this line
            }
        };// End DynamicControl removeControl code

    }

    @Override
    public void deviceConnected(String s, InetAddress inetAddress) {
        System.out.println("Detected " + s + " at " + inetAddress.getHostAddress());
        addDeviceControls(s, inetAddress);
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
