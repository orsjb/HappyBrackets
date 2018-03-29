package net.happybrackets.develop.DynamicControls;

import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.ControlScope;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.device.HB;

public class TestDynamicControl_3 implements HBAction {

    float floatVal = 2000; // must be a class variable to access inside inner classes
    String controlHashCode = ""; // we are just using this now as our real control will have a hash code

    @Override
    public void action(HB hb) {
        hb.createDynamicControl(ControlType.INT, "test no parent").addControlListener(new DynamicControl.DynamicControlListener() {
            @Override
            public void update(DynamicControl control) {
                System.out.println((int)control.getValue());
            }
        });

        hb.createDynamicControl(ControlType.INT, "test no parent init", 10).addControlListener(new DynamicControl.DynamicControlListener() {
            @Override
            public void update(DynamicControl control) {
                System.out.println((int)control.getValue());
            }
        });

        hb.createDynamicControl(ControlType.INT, "test no parent max min", 10, 1, 20).addControlListener(new DynamicControl.DynamicControlListener() {
            @Override
            public void update(DynamicControl control) {
                System.out.println((int)control.getValue());
            }
        });


        hb.createDynamicControl(ControlType.FLOAT, "test no parent ").addControlListener(new DynamicControl.DynamicControlListener() {
            @Override
            public void update(DynamicControl control) {
                System.out.println((float)control.getValue());
            }
        });


        hb.createDynamicControl(ControlType.FLOAT, "test no parent with init", 10).addControlListener(new DynamicControl.DynamicControlListener() {
            @Override
            public void update(DynamicControl control) {
                System.out.println((float)control.getValue());
            }
        });

        hb.createDynamicControl(ControlType.FLOAT, "test no parent with max min", 10, 0, 1).addControlListener(new DynamicControl.DynamicControlListener() {
            @Override
            public void update(DynamicControl control) {
                System.out.println((float)control.getValue());
            }
        });


        hb.createDynamicControl(ControlType.TEXT, "Test no parent").addControlListener(new DynamicControl.DynamicControlListener() {
            @Override
            public void update(DynamicControl control) {
                System.out.println((String) control.getValue());
            }
        });

        hb.createDynamicControl(ControlType.TEXT, "Test no parent with init", "Test Init Value").addControlListener(new DynamicControl.DynamicControlListener() {
            @Override
            public void update(DynamicControl control) {
                System.out.println((String) control.getValue());
            }
        });

        hb.createDynamicControl(ControlType.BOOLEAN, "Test No Parent").addControlListener(new DynamicControl.DynamicControlListener() {
            @Override
            public void update(DynamicControl control) {
                System.out.println((Boolean) control.getValue());
            }
        });

        hb.createDynamicControl(ControlType.BOOLEAN, "Test No Parent init", true).addControlListener(new DynamicControl.DynamicControlListener() {
            @Override
            public void update(DynamicControl control) {
                System.out.println((boolean)control.getValue());
            }
        });

        DynamicControl test_sender = new DynamicControl(ControlType.TRIGGER, "Remote Trigger").addControlListener(new DynamicControl.DynamicControlListener() {
            @Override
            public void update(DynamicControl control) {
                System.out.println("Remote Trigger");
            }
        });

        hb.createDynamicControl(ControlType.TRIGGER, "Trigger").addControlListener(new DynamicControl.DynamicControlListener() {
            @Override
            public void update(DynamicControl control) {

                System.out.println("Trigger");
                test_sender.setValue(null);
            }
        });


        // Now we will make a couple of buddies
        hb.createDynamicControl(ControlType.INT, "Buddy", 0, 1, 100).setControlScope(ControlScope.CLASS);
        hb.createDynamicControl(ControlType.INT, "Buddy").setControlScope(ControlScope.CLASS).addControlListener(new DynamicControl.DynamicControlListener() {
            @Override
            public void update(DynamicControl control) {
                System.out.println("Buddy val " + (int)control.getValue());
            }
        });


    }
}
