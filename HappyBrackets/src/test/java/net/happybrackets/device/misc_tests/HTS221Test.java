package net.happybrackets.device.misc_tests;

import net.beadsproject.beads.data.DataBead;
import net.happybrackets.device.sensors.HTS221;
import net.happybrackets.device.sensors.SensorListener;

public class HTS221Test {

    static DataBead senseDB;
    static DataBead nameDB;

    public static void main(String[] args) throws Exception {

        HTS221 sense = new HTS221();
        sense.addListener(new SensorListener() {

            @Override
            public void getData(DataBead db){
                senseDB = db;
            }
            @Override
            public void getSensor(DataBead db){
                nameDB = db;
            }
        });

        sense.update();

        System.out.println(senseDB);
        System.out.println(nameDB);

    }

}
