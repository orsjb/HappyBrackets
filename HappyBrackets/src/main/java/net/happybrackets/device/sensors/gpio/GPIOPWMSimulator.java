package net.happybrackets.device.sensors.gpio;

import net.happybrackets.core.control.BooleanControl;
import net.happybrackets.core.control.ControlMap;
import net.happybrackets.core.control.IntegerSliderControl;

public class GPIOPWMSimulator extends GPIOPWMOutput {

    IntegerSliderControl integerSliderControl;
    /**
     * Constructor
     * @param gpio_number the GPIO pin number
     */
    protected GPIOPWMSimulator(int gpio_number) {
        super(gpio_number);
        /*************************************************************
         * Create an integer type Dynamic Control that displays as a slider
         * Simply type intSliderControl to generate this code
         *************************************************************/
        integerSliderControl = new IntegerSliderControl(this, "GPIO PWM " + gpio_number, 0, 0, getPwmRange()) {

            @Override
            public void valueChanged(int control_val) {
                /*** Write your DynamicControl code below this line ***/

                /*** Write your DynamicControl code above this line ***/
            }
        }.setDisabled(true);/*** End DynamicControl integerSliderControl code ***/


    }

    @Override
    public void setValue(int value) {
        integerSliderControl.setValue(value);
    }


    @Override
    void reset() {

    }

    @Override
    void unnasign() {
        ControlMap.getInstance().removeControl(integerSliderControl.getDynamicControl());
    }
}