package net.happybrackets.device.sensors.gpio;

import net.happybrackets.core.control.BooleanControl;

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
}
