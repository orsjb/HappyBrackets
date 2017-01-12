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

import net.beadsproject.beads.data.DataBead;
import net.happybrackets.device.sensors.*;

public class SensorTest {

    static DataBead HTS221dataDB, LPS25HdataDB, LSM9DS1dataDB, MinimuDataDB;
    static DataBead HTS221nameDB, LPS25HnameDB, LSM9DS1nameDB, MinimuNameDB;

    public static void main(String[] args) throws Exception {

//        HTS221 HTS221sensor = new HTS221();
//        HTS221sensor.addListener(new SensorUpdateListener() {
//            @Override
//            public void getData(DataBead db){
//                HTS221dataDB = db;
//            }
//            @Override
//            public void getSensor(DataBead db){
//                HTS221nameDB = db;
//            }
//        });
//        HTS221sensor.update();
//
//        LPS25H LPS25Hsensor = new LPS25H();
//        LPS25Hsensor.addListener(new SensorUpdateListener() {
//            @Override
//            public void getData(DataBead db){
//                LPS25HdataDB = db;
//            }
//            @Override
//            public void getSensor(DataBead db){
//                LPS25HnameDB = db;
//            }
//        });
//        LPS25Hsensor.update();
//
//
        LSM9DS1 LSM9DS1sensor = new LSM9DS1();
        LSM9DS1sensor.addListener(new SensorUpdateListener() {

            @Override
            public void sensorUpdated() {
                //TODO this test is broken until we move the below methods up into this space and make calls to the sensor for the data values.
            }

            public void getData(DataBead db){
                LSM9DS1dataDB = db;
            }

            public void getSensor(DataBead db){
                LSM9DS1nameDB = db;
            }
        });

//        MiniMU MiniMU = new MiniMU();
//        MiniMU.addListener(new SensorUpdateListener() {
//            @Override
//            public void getData(DataBead db){
//                MinimuDataDB = db;
//            }
//            @Override
//            public void getSensor(DataBead db){
//                MinimuNameDB = db;
//            }
//        });
//        MiniMU.update();

//
//        System.out.println(HTS221dataDB);
//        System.out.println(HTS221nameDB);
//        System.out.println(LPS25HdataDB);
//        System.out.println(LPS25HnameDB);
        System.out.println(LSM9DS1dataDB);
        System.out.println(LSM9DS1nameDB);
//        System.out.println(MinimuDataDB);
//        System.out.println(MinimuNameDB);

    }

}
