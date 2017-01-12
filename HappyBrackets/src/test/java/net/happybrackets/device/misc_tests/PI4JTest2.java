/*
 * Copyright 2016 Ollie Bown
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.happybrackets.device.misc_tests;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public class PI4JTest2 {
	
	public static class MyInputListener implements GpioPinListenerDigital {
	    @Override
	    public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
	        // display pin state on console
	        System.out.println(" --> GPIO PIN STATE CHANGE: " + event.getPin() + " = "
	                + event.getState());
	    }
	}

	public static void main(String[] args) {
		final GpioController gpio = GpioFactory.getInstance();
		System.out.println("Got GPIO instance.");
		//the pins
		GpioPinDigitalInput myInPin2 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_02); 
		System.out.println("Got GPIO pin 2.");

		GpioPinDigitalInput myInPin3 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_03); 
		System.out.println("Got GPIO pin 3.");

		GpioPinDigitalInput myInPin4 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_04); 
		System.out.println("Got GPIO pin 4.");

		GpioPinDigitalInput myInPin5 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_05); 
		System.out.println("Got GPIO pin 5.");

		GpioPinDigitalInput myInPin8 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_08); 
		System.out.println("Got GPIO pin 8.");

		GpioPinDigitalInput myInPin9 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_09); 
		System.out.println("Got GPIO pin 9.");
		
//		myInPin.addListener(new MyInputListener());
//		System.out.println("Set up listener for GPIO pin 3.");
		
		while(true) {
			System.out.println(myInPin2.getState() + " " + myInPin3.getState() + " " + myInPin4.getState() + " " + myInPin5.getState() + " " + myInPin8.getState() + " " + myInPin9.getState());
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
