package net.happybrackets.examples;

import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.ControlScope;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.device.HB;

public class Test implements HBAction {
    @Override
    public void action(HB hb) {

    DynamicControl my_sender = hb.createDynamicControl(this, ControlType.INT, "myGlobal", 0);
    my_sender.setControlScope(ControlScope.GLOBAL);

    //.. somewhere in my Sketch
    my_sender.setValue(100); // this will go across network



    DynamicControl my_receiver = hb.createDynamicControl(this, ControlType.INT, "myGlobal", 0);
    my_sender.setControlScope(ControlScope.GLOBAL);
    my_receiver.addControlListener(new DynamicControl.DynamicControlListener() {
        @Override
        public void update(DynamicControl dynamicControl) {
            int x_val = (int)dynamicControl.getValue();
            System.out.println("I Just Got " +  x_val);
        }
    });
    }

}
