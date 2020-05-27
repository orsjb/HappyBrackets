package net.happybrackets.core.control;

import org.junit.Test;

/**
 * Test that functions are available for classes
 */
public class ControlFunctions {

    @Test
    public void testFunctions() {

        // Type globalFloatControl to generate this code
        FloatControl floatBuddyControl = new FloatControl(this, "global control name", 0, -1, 1, DynamicControl.DISPLAY_TYPE.DISPLAY_ENABLED_BUDDY) {
            @Override
            public void valueChanged(double control_val) {// Write your DynamicControl code below this line
                String sender =  getSendingDevice();

                // Write your DynamicControl code above this line
            }

        }.setControlScope(ControlScope.GLOBAL);// End DynamicControl floatBuddyControl code

        // Type globalBooleanControl to generate this code
        BooleanControl booleanControl = new BooleanControl(this, "global control name", false) {
            @Override
            public void valueChanged(Boolean control_val) {// Write your DynamicControl code below this line
                String sender =  getSendingDevice();

                // Write your DynamicControl code above this line
            }
        }.setControlScope(ControlScope.GLOBAL);// End DynamicControl booleanControl code

        // Type globalTriggerControl to generate this code
        TriggerControl triggerControl = new TriggerControl(this, "global control name") {
            @Override
            public void triggerEvent() {// Write your DynamicControl code below this line
                String sender =  getSendingDevice();
                // Write your DynamicControl code above this line
            }
        }.setControlScope(ControlScope.GLOBAL);// End DynamicControl triggerControl code

        // Type globalTextControl to generate this code
        TextControl textControl = new TextControl(this, "global control name", "") {
            @Override
            public void valueChanged(String control_val) {// Write your DynamicControl code below this line
                String sender =  getSendingDevice();
                // Write your DynamicControl code above this line
            }
        }.setControlScope(ControlScope.GLOBAL);// End DynamicControl textControl code

        // Type globalIntControl to generate this code
        IntegerControl integerBuddyControl = new IntegerControl(this, "global control name", 0) {
            @Override
            public void valueChanged(int control_val) {// Write your DynamicControl code below this line
                String sender =  getSendingDevice();
                // Write your DynamicControl code above this line
            }
        }.setDisplayRange( -1, 1, DynamicControl.DISPLAY_TYPE.DISPLAY_ENABLED_BUDDY).setControlScope(ControlScope.GLOBAL);// End DynamicControl integerBuddyControl code


    }
}
