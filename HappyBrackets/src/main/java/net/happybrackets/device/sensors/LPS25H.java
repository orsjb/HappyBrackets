package net.happybrackets.device.sensors;
/**
 * Licensed to the Rhiot under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

/***************
 * Adapted for the Happy brackets project by Sam Ferguson (2016).
 *
 * We will return configuration information and scaling information so
 * this sensor can be compared to others.
 *
 ***************/

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import net.beadsproject.beads.data.DataBead;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class LPS25H  extends Sensor{

    public static final byte LPS25H_ADDRESS = 0x5c;

    public static final byte REF_P_XL = 0x08;
    public static final byte REF_P_L = 0x09;
    public static final byte REF_P_H = 0x0A;
    public static final byte WHO_AM_I = 0x0F;
    public static final byte RES_CONF = 0x10;
    // Reserved (Do not modify) 11-1F Reserved
    public static final byte CTRL_REG1 = 0x20;
    public static final byte CTRL_REG2 = 0x21;
    public static final byte CTRL_REG3 = 0x22;
    public static final byte CTRL_REG4 = 0x23;
    public static final byte INT_CFG = 0x24;
    public static final byte INT_SOURCE = 0x25;
    // Reserved (Do not modify) 26 Reserved
    public static final byte STATUS_REG = 0x27;
    public static final byte PRESS_POUT_XL = 0x28;
    public static final byte PRESS_OUT_L = 0x29;
    public static final byte PRESS_OUT_H = 0x2A;
    public static final byte TEMP_OUT_L = 0x2B;
    public static final byte TEMP_OUT_H = 0x2C;
    // Reserved (Do not modify) 2D Reserved
    public static final byte FIFO_CTRL = 0x2E;
    public static final byte FIFO_STATUS = 0x2F;
    public static final byte THS_P_L = 0x30;
    public static final byte THS_P_H = 0x31;
    // public static final byte Reserved 32-38
    public static final byte RPDS_L = 0x39;
    public static final byte RPDS_H = 0x3A;

    // ST suggested Configuration
    private static final byte RES_CONF_SC = 0x05;
    private static final byte CTRL_REG2_SC = 0x40;
    private static final byte FIFO_CTRL_SC = (byte) 0xc0;

    // Static constant for conversion
    private static final double DOUBLE_42_5 = 42.5;
    private static final double DOUBLE_480_0 = 480.0;
    private static final double DOUBLE_4096_0 = 4096.0;

    private ByteBuffer buffer = ByteBuffer.allocate(4);

    public static byte TEMP_DATA_AVAILABLE_MASK = 0x01;
    public static byte PRESS_DATA_AVAILABLE_MASK = 0x02;

    public double tempData;
    public double barometricPressureData;

    public enum LPS25HControlRegistry1 {

        ODR_ONE_SHOT(0b000), ODR_1_HZ(0b001), ODR_7_HZ(0b010), ODR_12DOT5_HZ(0b011), ODR_25_HZ(0b100), ODR_RESERVED(
                0b101), DIFFEN_DISABLED(0b0), DIFFEN_ENABLE(0b1), RESETAZ_DISABLE(0b0), RESETAZ_RESET(0b1), BDU_CONTINUOUS(
                0b0), BDU_UPDATE_AFTER_READING(0b1), PD_ACTIVE(0b1), PD_POWER_DOWN(0b0), SIM_4WIRE(0b0), SIM_3WIRE(
                0b1);

        final byte value;

        LPS25HControlRegistry1(int value) {
            this.value = (byte) value;
        }

    }

    private LPS25HControlRegistry1 pd = LPS25HControlRegistry1.PD_ACTIVE;
    private LPS25HControlRegistry1 odr = LPS25HControlRegistry1.ODR_25_HZ;
    private LPS25HControlRegistry1 diffEn = LPS25HControlRegistry1.DIFFEN_DISABLED;
    private LPS25HControlRegistry1 bdu = LPS25HControlRegistry1.BDU_UPDATE_AFTER_READING;
    private LPS25HControlRegistry1 resetAz = LPS25HControlRegistry1.RESETAZ_DISABLE;
    private LPS25HControlRegistry1 sim = LPS25HControlRegistry1.SIM_4WIRE;

    boolean debug = true;
    I2CDevice device;
    I2CBus bus;
    DataBead db2 = new DataBead();

    public static void main(String[] args) throws Exception {
        LPS25H sense = new LPS25H();

        while (true) {

            double tempVal = sense.getTemperatureData();
            double pressureVal = sense.getBarometricPressureData();
            try {
                Thread.sleep(1000);                 //1000 milliseconds is one second.
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public LPS25H() throws Exception {


        bus = I2CFactory.getInstance(1);
        if (debug){ System.out.println("bus: " + bus.toString());}
        device = bus.getDevice(LPS25H_ADDRESS);
        doStart();
        if (debug){ System.out.println("device: " + device.toString());}
        buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        start();
    }



    public String getSensorName(){
        return "LPS25H";
    }

    public void update() throws IOException {

        for (SensorUpdateListener sListener: listeners){
            SensorUpdateListener sl = (SensorUpdateListener) sListener;

        }
    }

    protected void doStart() throws Exception {
        //super.doStart();
        byte registry1 = (byte) (pd.value << 7 | odr.value << 4 | diffEn.value << 3 | bdu.value << 2
                | resetAz.value << 1 | sim.value);
        device.write(CTRL_REG1, registry1);

        // ST suggested configuration
        device.write(RES_CONF, RES_CONF_SC);
        device.write(FIFO_CTRL, FIFO_CTRL_SC);
        device.write(CTRL_REG2, CTRL_REG2_SC);
    }

    public void doStop() throws Exception {
        //super.doStop();
        byte crtl1 = (byte) device.read(CTRL_REG1);
        byte maskToPowerDown = (byte) (0xff ^ (~LPS25HControlRegistry1.PD_POWER_DOWN.value << 7));
        crtl1 &= maskToPowerDown;

        device.write(CTRL_REG1, crtl1);

    }

    private double getBarometricPressureData() throws IOException {
        //device.read(PRESS_POUT_XL | I2CConstants.MULTI_BYTE_READ_MASK, buffer.array(), 0, 3);
        device.read(PRESS_POUT_XL | I2CConstants.MULTI_BYTE_READ_MASK, buffer.array(), 0, 3);

        buffer.put(3, (byte) 0);
        int PRESS_OUT = buffer.getInt(0);
        System.out.println("DEBUG: PRESSURE_OUT  : " + PRESS_OUT);
        double press = (PRESS_OUT / DOUBLE_4096_0);
        System.out.println("DEBUG: PRESSURE_OUT (hPa) : " + press);
        return press;

    }

    private double getTemperatureData() throws IOException {
        //device.read(TEMP_OUT_L | I2CConstants.MULTI_BYTE_READ_MASK, buffer.array(), 0, 2);
        device.read(TEMP_OUT_L | I2CConstants.MULTI_BYTE_READ_MASK, buffer.array(), 0, 2);
        short TEMP_OUT = buffer.getShort(0);
        System.out.println("DEBUG: TEMP_OUT  : " + TEMP_OUT);
        double temp = (DOUBLE_42_5 + (TEMP_OUT / DOUBLE_480_0));
        System.out.println("DEBUG: TEMP_OUT (C) : " + temp);
        return temp;
    }

    public final class I2CConstants {

        public static final int MULTI_BYTE_READ_MASK = 0x80;

    }


    private void start() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        // get data
                        tempData = getTemperatureData();
                        barometricPressureData = getBarometricPressureData();
                        //pass data on to listeners
                    } catch(IOException e){
                        e.printStackTrace();
                    }

                    for(SensorUpdateListener listener : listeners) {
                        listener.sensorUpdated();
                    }

                    try {
                        Thread.sleep(10);		//TODO this should not be hardwired.
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        new Thread(task).start();
    }



}
