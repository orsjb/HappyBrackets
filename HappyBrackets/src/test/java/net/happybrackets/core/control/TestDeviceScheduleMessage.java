package net.happybrackets.core.control;

import de.sciss.net.OSCMessage;
import net.happybrackets.core.Device;
import net.happybrackets.core.scheduling.DeviceSchedulerValue;
import org.junit.Test;

import java.util.Random;

/**
 * Test the DeviceSchedulerValue encoding and decoding
 */
public class TestDeviceScheduleMessage {
    // We will check value inside test
    boolean testSuccess = false;


    // define our Dynamic COntrol Name
    final String CONTROL_NAME =  "Custom";

    @Test
    public void testCustomMessage() {
        //setup a running order for our tests
        // Ignore Device name so we do not ignore Global message from same device name
        DynamicControl.setIgnoreName(true);

        DeviceSchedulerValue customGlobalMessage = new DeviceSchedulerValue( Device.getDeviceName(),  Math.random(), Math.random(), new Random().nextInt());

        DeviceSchedulerValue secondGlobalMessage = new DeviceSchedulerValue( Device.getDeviceName(),  Math.random(), Math.random(), new Random().nextInt());

        assert (!customGlobalMessage.equals(secondGlobalMessage));

        ClassObjectControl objectControl = new ClassObjectControl(this, CONTROL_NAME, DeviceSchedulerValue.class) {
            @Override
            public void valueChanged(Object control_val) {
                if (control_val != null){
                    DeviceSchedulerValue decoded = (DeviceSchedulerValue)control_val;
                    // Check the value of our decode here
                    testSuccess = decoded.equals(secondGlobalMessage);
                }
            }
        }.setControlScope(ControlScope.GLOBAL);


        DynamicControl test_control2 = new DynamicControl(this, ControlType.OBJECT, CONTROL_NAME, secondGlobalMessage);
        // encode our message
        OSCMessage message = test_control2.buildNetworkSendMessage();


        DynamicControl.processOSCControlMessage(message, ControlScope.GLOBAL); // The control Listener will get called
        assert (testSuccess);
    }
}
