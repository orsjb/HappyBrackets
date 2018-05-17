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

package net.happybrackets.device.sensors;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import net.beadsproject.beads.data.DataBead;
import net.happybrackets.device.sensors.sensor_types.AccelerometerSensor;
import net.happybrackets.device.sensors.sensor_types.GyroscopeSensor;
import net.happybrackets.device.sensors.sensor_types.MagnetometerSensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 *This device exists on the MiniMu v5 board
 */
public class LSM6DS33 extends Sensor implements AccelerometerSensor, GyroscopeSensor {

	final static Logger logger = LoggerFactory.getLogger(LSM6DS33.class);

	// This appears to be the value that gives us +/-1 accelerometer values for standard gravity
	final int ACCELEROMETER_MULTIPLIER = 0x8000 / 2;

	final int GYROSCOPE_MULTIPLIER = 0x8000 / 2;

	// values taken from https://github.com/pololu/lsm6-arduino/blob/master/LSM6.h
	static final int FUNC_CFG_ACCESS   = 0x01;

	static final byte FIFO_CTRL1        = 0x06;
	static final byte FIFO_CTRL2        = 0x07;
	static final byte FIFO_CTRL3        = 0x08;
	static final byte FIFO_CTRL4        = 0x09;
	static final byte FIFO_CTRL5        = 0x0A;
	static final byte ORIENT_CFG_G      = 0x0B;

	static final byte INT1_CTRL         = 0x0D;
	static final byte INT2_CTRL         = 0x0E;
	static final byte WHO_AM_I          = 0x0F;
	static final byte CTRL1_XL          = 0x10;
	static final byte CTRL2_G           = 0x11;
	static final byte CTRL3_C           = 0x12;
	static final byte CTRL4_C           = 0x13;
	static final byte CTRL5_C           = 0x14;
	static final byte CTRL6_C           = 0x15;
	static final byte CTRL7_G           = 0x16;
	static final byte CTRL8_XL          = 0x17;
	static final byte CTRL9_XL          = 0x18;
	static final byte CTRL10_C          = 0x19;

	static final byte WAKE_UP_SRC       = 0x1B;
	static final byte TAP_SRC           = 0x1C;
	static final byte D6D_SRC           = 0x1D;
	static final byte STATUS_REG        = 0x1E;

	static final byte OUT_TEMP_L        = 0x20;
	static final byte OUT_TEMP_H        = 0x21;
	static final byte OUTX_L_G          = 0x22;
	static final byte OUTX_H_G          = 0x23;
	static final byte OUTY_L_G          = 0x24;
	static final byte OUTY_H_G          = 0x25;
	static final byte OUTZ_L_G          = 0x26;
	static final byte OUTZ_H_G          = 0x27;
	static final byte OUTX_L_XL         = 0x28;
	static final byte OUTX_H_XL         = 0x29;
	static final byte OUTY_L_XL         = 0x2A;
	static final byte OUTY_H_XL         = 0x2B;
	static final byte OUTZ_L_XL         = 0x2C;
	static final byte OUTZ_H_XL         = 0x2D;

	static final byte FIFO_STATUS1      = 0x3A;
	static final byte FIFO_STATUS2      = 0x3B;
	static final byte FIFO_STATUS3      = 0x3C;
	static final byte FIFO_STATUS4      = 0x3D;
	static final byte FIFO_DATA_OUT_L   = 0x3E;
	static final byte FIFO_DATA_OUT_H   = 0x3F;
	static final byte TIMESTAMP0_REG    = 0x40;
	static final byte TIMESTAMP1_REG    = 0x41;
	static final byte TIMESTAMP2_REG    = 0x42;

	static final byte STEP_TIMESTAMP_L  = 0x49;
	static final byte STEP_TIMESTAMP_H  = 0x4A;
	static final byte STEP_COUNTER_L    = 0x4B;
	static final byte STEP_COUNTER_H    = 0x4C;

	static final byte FUNC_SRC          = 0x53;

	static final byte TAP_CFG           = 0x58;
	static final byte TAP_THS_6D        = 0x59;
	static final byte INT_DUR2          = 0x5A;
	static final byte WAKE_UP_THS       = 0x5B;
	static final byte WAKE_UP_DUR       = 0x5C;
	static final byte FREE_FALL         = 0x5D;
	static final byte MD1_CFG           = 0x5E;
	static final byte MD2_CFG           = 0x5F;


	final int DS33_SA0_HIGH_ADDRESS = 0b1101011;
	final int DS33_SA0_LOW_ADDRESS  = 0b1101010;

	@Override
	public String getSensorName() {
		return "LSM6DS33";
	}

	//TODO need to adjust for different versions

	private byte ACC_ADDRESS;
	private byte GYR_ADDRESS;

	private I2CBus bus;
	private I2CDevice gyrodevice, acceldevice;

	private DataBead db2 = new DataBead();

	double[] gyroData = new double[3];
	double[] accelData = new double[3];

	private boolean validLoad = true;

	public static LSM6DS33 getLoadedInstance() {
		return loadedInstance;
	}

	static private LSM6DS33 loadedInstance = null;
	/**
	 * See if we have a valid load
	 * @return true if loaded correctly
	 */
	public boolean isValidLoad(){
		return validLoad;
	}

	public LSM6DS33() {
		db2.put("Name","LSM6DS33 (MiniMU-v5)");
		db2.put("Manufacturer","Pololu");
		// Work out which one we are
		// use MINIMUAHRS code to work out different versions.
		// use WHO_AM_I register to getInstance

		try {
			System.out.println("Getting I2C Bus 1:");
			bus = I2CFactory.getInstance(I2CBus.BUS_1);
			if (bus != null) {
				System.out.println("Connected to bus OK!");
			} else {
				logger.warn("Could not connect to bus!");
			}

		} catch (Exception e) {
			System.out.println("Could not connect to bus!");
		}

		if (bus != null) {
			// let us do an auto detect
			try {
				gyrodevice = bus.getDevice(DS33_SA0_HIGH_ADDRESS);
				acceldevice = bus.getDevice(DS33_SA0_HIGH_ADDRESS);
				validLoad = true;
				System.out.println("Found LSM6DS33 at high address");
			} catch (Exception ex) {
				System.out.println("Did not find LSM6DS33 at High address. Try Low");
				try {
					gyrodevice = bus.getDevice(DS33_SA0_LOW_ADDRESS);
					acceldevice = bus.getDevice(DS33_SA0_LOW_ADDRESS);
					System.out.println("Found LSM6DS33 at low address");
					validLoad = true;
				} catch (Exception ex2) {
					System.out.println("Did not find either");
				}
			}

		}

        if (acceldevice != null && gyrodevice != null) {
            try {
                acceldevice.write(CTRL1_XL, (byte) 0x80);

                gyrodevice.write(CTRL2_G, (byte) 0x80);

                acceldevice.write(CTRL2_G, (byte) 0x80);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

		if (validLoad && bus != null & acceldevice != null) {
            System.out.println("LSM6DS33 valid Load");
			start();
			storeSensor(this);
			loadedInstance = this;
			setValidLoad (true);
		}

        System.out.println("LSM6DS33 end Load");

	}


	private void start() {
		Runnable task = new Runnable() {
			@Override
			public void run() {
                System.out.println("Start LSM6DS33 monitor");

				while(true) {

					try {
						gyroData = readSensorsGyro();
						accelData = readSensorsAccel();

						//pass data on to listeners

						notifyListeners();

					} catch (IOException e) {
							// System.out.println("MiniMU not receiving data.");
							// Assuming we might like this in dev?
							logger.debug("LSM6DS33 not receiving data.");
						System.out.println("LSM6DS33 task " + e.getMessage());
					}
					try {
						Thread.sleep(10);		//TODO this should not be hardwired.
					} catch (InterruptedException e) {
						logger.error("Poll interval interupted while listening for LSM6DS33!", e);
					}
				}
			}


		};
		new Thread(task).start();
	}

	private double[] readSensorsGyro() throws IOException {
		int numElements = 3; //
		double[] result = {0, 0, 0};
		int bytesPerElement = 2; // assuming short?
		int numBytes = numElements * bytesPerElement; //
		byte[] bytes = new byte[numBytes]; //
		DataInputStream accelIn;

		acceldevice.write(OUTX_L_G);

		acceldevice.read(bytes, 0, bytes.length);

		accelIn = new DataInputStream(new ByteArrayInputStream(bytes));
		for (int i = 0; i < numElements; i++) {
			byte a = accelIn.readByte(); //least sig
			byte b = accelIn.readByte(); //most sig
			boolean[] abits = getBits(a);
			boolean[] bbits = getBits(b);
			boolean[] shortybits = new boolean[16];
			for(int j = 0; j < 8; j++) {
				shortybits[j] = bbits[j];
			}
			for (int j = 0; j < 8; j++) {
				shortybits[j + 8] = abits[j];
			}
			int theInt = bits2Int(shortybits);
			result[i] = theInt;
		}
		return result;
	}

	private double[] readSensorsAccel() throws IOException {
		int numElements = 3; //
		double[] result = {0, 0, 0};

		int bytesPerElement = 2; // assuming short?
		int numBytes = numElements * bytesPerElement; //
		byte[] bytes = new byte[numBytes]; //
		DataInputStream accelIn;
		acceldevice.write(OUTX_L_XL);

		acceldevice.read(bytes, 0, bytes.length);

		accelIn = new DataInputStream(new ByteArrayInputStream(bytes));
		for (int i = 0; i < numElements; i++) {
			byte a = accelIn.readByte(); //least sig
			byte b = accelIn.readByte(); //most sig
			boolean[] abits = getBits(a);
			boolean[] bbits = getBits(b);
			boolean[] shortybits = new boolean[16];
			for(int j = 0; j < 8; j++) {
				shortybits[j] = bbits[j];
			}
			for(int j = 0; j < 8; j++) {
				shortybits[j + 8] = abits[j];
			}
			int theInt = bits2Int(shortybits);
			result[i] = theInt;
		}
		return result;
	}


	private static boolean[] getBits(byte inByte) {
		boolean[] bits = new boolean[8];
		for (int j = 0; j < 8; j++) {
			// Shift each bit by 1 starting at zero shift
			byte tmp = (byte) (inByte >> j);
			// Check byte with mask 00000001 for LSB
			bits[7-j] = (tmp & 0x01) == 1;
		}
		return bits;
	}

	private static String bits2String(boolean[] bbits) {
		StringBuffer b = new StringBuffer();
		for(boolean v : bbits) {
			b.append(v?1:0);
		}
		return b.toString();
	}

	private static int bits2Int(boolean[] bbits) {
		int result = 0;
		int length = bbits.length - 1;
		if (bbits[0]) { // if the most significant bit is true
			for(int i = 0; i < length; i++) { //
				result -= bbits[length - i] ? 0 : Math.pow(2, i) ; // use the negative complement version
			}
		} else {
			for(int i = 0; i < length; i++) {
				result += bbits[length - i]? Math.pow(2, i) : 0; // use the positive version
			}
		}
		return result;
	}

	private static String byte2Str(byte inByte) {
		boolean[] bbits = getBits(inByte);
		return bits2String(bbits);
	}

	@Override
	public double[] getGyroscopeData() {
		return gyroData;
	}

	@Override
	public float getGyroscopeX() {
		return (float)gyroData[0] / GYROSCOPE_MULTIPLIER;
	}

	@Override
	public float getGyroscopeY() {
		return (float)gyroData[1] / GYROSCOPE_MULTIPLIER;
	}

	@Override
	public float getGyroscopeZ() {
		return (float)gyroData[2]/ GYROSCOPE_MULTIPLIER;
	}

	@Override
	public double[] getAccelerometerData() {
		return accelData;
	}

	@Override
	public float getAccelerometerX() {
		// we need to convert down to half value of 16 bits (we are using signed 16 bits
        float ret = (float)(accelData [0] / ACCELEROMETER_MULTIPLIER);

        //System.out.println(ret);
        return ret;
	}

	@Override
	public float getAccelerometerY() {
		// we need to convert down to half value of 16 bits (we are using signed 16 bits
        float ret = (float)(accelData [1] / ACCELEROMETER_MULTIPLIER);
        return ret;
	}

	@Override
	public float getAccelerometerZ() {
		// we need to convert down to half value of 16 bits (we are using signed 16 bits
		float ret = (float)(accelData [2] / ACCELEROMETER_MULTIPLIER);

		return ret;
	}


	@Override
	public float getPitch() {
		return getGyroscopeY();
	}

	@Override
	public float getRoll() {
		return getGyroscopeX();
	}

	@Override
	public float getYaw() {
		return getGyroscopeZ();
	}

}
