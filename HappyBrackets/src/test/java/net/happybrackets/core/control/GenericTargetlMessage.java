package net.happybrackets.core.control;

import de.sciss.net.OSCMessage;
import net.happybrackets.core.Device;
import org.junit.Test;

public class GenericTargetlMessage {

    // We will check value inside test
    boolean testSuccess1 = false;
    boolean testSuccess2 = false;


    // define our Dynamic COntrol Name
    final String CONTROL_NAME =  "Generic";

    @Test
    public void testsGenericMessage() {
        //setup a running order for our tests


        // Ignore Device name so we do not ignore GLobal message from same device name
        DynamicControl.setIgnoreName(true);


        GenericTestMessageObject genericGlobalMessage = new GenericTestMessageObject("Hello");
        GenericTestMessageObject secondGlobalMessage = new GenericTestMessageObject("Goodbye");

        DynamicControl test_control = new DynamicControl(this, ControlType.OBJECT, CONTROL_NAME, genericGlobalMessage);
        DynamicControl test_control2 = new DynamicControl(this, ControlType.OBJECT, CONTROL_NAME, genericGlobalMessage);

        test_control.setControlScope(ControlScope.TARGET);
        test_control2.setControlScope(ControlScope.TARGET);

        // we will test if our message decoded as an OSC message inside listener
        test_control.addControlListener(control -> {
            Object value =  control.getValue();

            if (value != null){
                GenericTestMessageObject decoded = GenericTestMessageObject.decode(value);
                if (decoded != null) {
                    //System.out.println(decoded);

                    String sending_device = control.getSendingDevice();
                    System.out.println("Sending device " + sending_device);
                    // Check the value of our decode here
                    testSuccess1 = genericGlobalMessage.equals(decoded);
                }
            }
        });

        // we will test if our message decoded as an OSC message inside listener
        test_control2.addControlListener(control -> {
            Object value =  control.getValue();

            if (value != null){

                GenericTestMessageObject decoded = GenericTestMessageObject.decode(value);
                if (decoded != null) {
                    System.out.println(decoded);

                    String sending_device = control.getSendingDevice();
                    System.out.println("Sending device " + sending_device);
                    // Check the value of our decode here
                    testSuccess2 = decoded.equals(secondGlobalMessage);
                }
            }
        });

        // encode our message
        OSCMessage message = test_control.buildNetworkSendMessage();

        System.out.println(message);

        DynamicControl.processOSCControlMessage(message, ControlScope.TARGET); // The control Listener will get called

        assert (testSuccess1);

        // check our target
        test_control.setValue(secondGlobalMessage);
        // make sure it did not send with no target
        assert (!testSuccess2);

        test_control.setValue(genericGlobalMessage);

        // now add local as a target
        test_control.setTargetDevice(Device.getDeviceName());
        // check our target
        test_control.setValue(secondGlobalMessage);
        // make sure it did not send with no target
        assert (testSuccess2);

    }
}
