package net.happybrackets.device.sensors.gpio;

import net.happybrackets.core.control.ControlMap;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.core.control.IntegerControl;

public class GPIOPWMSimulator extends GPIOPWMOutput {

    IntegerControl integerSliderControl;
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
        integerSliderControl = new IntegerControl(this, "GPIO PWM " + gpio_number, 0, 0, getPwmRange(), DynamicControl.DISPLAY_TYPE.DISPLAY_DEFAULT) {

            @Override
            public void valueChanged(int control_val) {
                /*** Write your DynamicControl code below this line ***/

                /*** Write your DynamicControl code above this line ***/
            }
        }.setDisplayType(DynamicControl.DISPLAY_TYPE.DISPLAY_DISABLED);/*** End DynamicControl integerSliderControl code ***/


    }

    @Override
    public void setValue(int value) {
        integerSliderControl.setValue(value);
    }

    @Override
    public int getValue() {
        return integerSliderControl.getValue();
    }


    @Override
    void reset() {

    }

    @Override
    void unnasign() {
        ControlMap.getInstance().removeControl(integerSliderControl.getDynamicControl());
    }
}
