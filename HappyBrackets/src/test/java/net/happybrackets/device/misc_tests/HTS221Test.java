package net.happybrackets.device.misc_tests;

import net.beadsproject.beads.data.DataBead;
import net.happybrackets.device.sensors.HTS221;

public class HTS221Test {


    public static void main(String[] args) throws Exception {

        // Test instantiation of the sensors

        HTS221 sense = new HTS221();
        DataBead senseDB;
        senseDB = sense.getData();
        System.out.println(senseDB.values());

    }

}
