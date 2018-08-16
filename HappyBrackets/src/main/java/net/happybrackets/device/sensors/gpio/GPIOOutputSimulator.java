package net.happybrackets.device.sensors.gpio;

import net.happybrackets.core.control.BooleanControl;
import net.happybrackets.core.control.ControlMap;

public class GPIOOutputSimulator extends GPIODigitalOutput {

    BooleanControl booleanControl;
    /**
     * Constructor
     *
     * @param gpio_number the GPIO pin number
     */
    protected GPIOOutputSimulator(int gpio_number) {
        super(gpio_number);
        booleanControl = new BooleanControl(this, "GPIO Out " + gpio_number, false) {
            @Override
            public void valueChanged(Boolean control_val) {
            }
        }.setDisabled(true);
    }

    @Override
    public void setState(boolean state) {
        booleanControl.setValue(state);
    }

    @Override
    public boolean getState() {
        return booleanControl.getValue();
    }

    @Override
    void reset() {

    }

    @Override
    void unnasign() {
        ControlMap.getInstance().removeControl(booleanControl.getDynamicControl());

    }
}
