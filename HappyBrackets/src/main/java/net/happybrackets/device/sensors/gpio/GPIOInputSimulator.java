package net.happybrackets.device.sensors.gpio;

import com.pi4j.io.gpio.PinPullResistance;
import net.happybrackets.core.control.BooleanControl;

/**
 * Simulates GPIO input and output so we can run
 */
public class GPIOInputSimulator extends GPIOInput  {



    BooleanControl booleanControl;

    /**
     * Constructor
     *
     * @param gpio_number the GPIO pin number
     * @param pinPullResistance
     */
    GPIOInputSimulator(int gpio_number, PinPullResistance pinPullResistance) {
        super(gpio_number);
        System.out.println("Created GPIOInputSimulator");
        booleanControl = new BooleanControl(this, "GPIO In " + gpio_number, false) {
            @Override
            public void valueChanged(Boolean control_val) {
                GPIOInput input = (GPIOInput)this.getDynamicControl().getParentSketch();
                stateChanged(input, control_val);
            }
        };
    }

    @Override
    public boolean getState() {
        return booleanControl.getValue();
    }

    @Override
    void reset() {
        clearAllStateListeners();
    }
}
