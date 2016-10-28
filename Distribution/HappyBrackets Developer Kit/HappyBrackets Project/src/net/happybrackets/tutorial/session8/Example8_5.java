package net.happybrackets.tutorial.session8;

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.HTS221;
import net.happybrackets.device.sensors.LPS25H;
import net.happybrackets.device.sensors.LSM9DS1;
import net.happybrackets.device.sensors.SensorUpdateListener;

/**
 */
public class Example8_5 implements HBAction {

    @Override
    public void action(HB hb) {
        hb.reset();
        HTS221 hts = (HTS221)hb.getSensor(HTS221.class);
        hts.addListener(new SensorUpdateListener() {
            @Override
            public void sensorUpdated() {
                // get x
                double humidity = hts.getHumidityData();
                double temperature = hts.getTemperatureData();
                System.out.println("Humidity: " + humidity);
                System.out.println("Temperature: " + temperature);
            }
        });
        LPS25H lps = (LPS25H)hb.getSensor(LPS25H.class);
        lps.addListener(new SensorUpdateListener() {
            @Override
            public void sensorUpdated() {
                // get x
//                double pressure = lps.();
//                System.out.println("Pressure: " + pressure);
            }
        });

    }

}
