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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MiniMU extends Sensor implements AccelerometerSensor, GyroscopeSensor, MagnetometerSensor {

	final static Logger logger = LoggerFactory.getLogger(MiniMU.class);

	@Override
	public String getSensorName() {
		return "MiniMU";
	}

	//TODO need to adjust for different versions

	private byte MAG_ADDRESS;
	private byte ACC_ADDRESS;
	private byte GYR_ADDRESS;

	private final static int MAG_DATA_ADDR = 0xa8;
	private final static int GYRO_DATA_ADDR = 0xa8;
	private final static int ACC_DATA_ADDR = 0xa8;

	private I2CBus bus;
	private I2CDevice gyrodevice, acceldevice, magdevice;

	private DataBead db2 = new DataBead();

	double[] gyroData = new double[3];
	double[] accelData = new double[3];
	double[] magData = new double[3];

	private boolean validLoad = true;

	public static MiniMU getLoadedInstance() {
		return loadedInstance;
	}

	static private MiniMU loadedInstance = null;
	/**
	 * See if we have a valid load
	 * @return true if loaded correctly
	 */
	public boolean isValidLoad(){
		return validLoad;
	}

	public MiniMU () {
		db2.put("Name","MiniMU-9");
		db2.put("Manufacturer","Pololu");
		// Work out which one we are
		// use MINIMUAHRS code to work out different versions.
		// use WHO_AM_I register to getInstance

		try {
			logger.info("Getting I2C Bus 1:");
			bus = I2CFactory.getInstance(I2CBus.BUS_1);
			if(bus != null) {
				logger.info("Connected to bus OK!");
			} else {
				logger.warn("Could not connect to bus!");
			}

		} catch(Exception e) {
			logger.error("Could not connect to bus!");
		}

		if (bus != null) {
			try {
				//  v2 info
				MAG_ADDRESS = 0x1e;
				ACC_ADDRESS = 0x19;
				GYR_ADDRESS = 0x6b;
				gyrodevice = bus.getDevice(GYR_ADDRESS);
				acceldevice = bus.getDevice(ACC_ADDRESS);
				magdevice = bus.getDevice(MAG_ADDRESS);

			} catch (Exception e) {
				logger.info("OK - not a v2, so I'll try to set up a v3.");
			}
			try {
				//  v3 info
				MAG_ADDRESS = 0x1d;
				ACC_ADDRESS = 0x1d;
				GYR_ADDRESS = 0x6b;
				gyrodevice = bus.getDevice(GYR_ADDRESS);
				acceldevice = bus.getDevice(ACC_ADDRESS);
				magdevice = bus.getDevice(MAG_ADDRESS);

				logger.info("OK - v3 set up.");

			} catch (Exception e2) {
				logger.error("OK - v3 IOException as well. Not sure we have a Minimu v2 or v3 attached.");
			}
		}
		try {

			byte CNTRL1_gyr = 0x20;
			byte CNTRL4_gyr = 0x23;
			byte CNTRL1_acc = 0x20;
			byte CNTRL4_acc = 0x23;
			byte CNTRL1_mag = 0x00;
			byte CNTRL2_mag = 0x01;
			byte CNTRL3_mag = 0x02;
			byte gyroSettings1 = 0b00001111;
			byte gyroSettings4 = 0b00110000;
			byte accSettings1 = 0b01000111;
			byte accSettings4 = 0b00101000;
			byte magSettings1 = 0b00001100;
			byte magSettings2 = 0b00100000;
			byte magSettings3 = 0b00000000;

			// GYRO
			gyrodevice.write(CNTRL1_gyr, gyroSettings1);
			gyrodevice.write(CNTRL4_gyr, gyroSettings4);

			// ACCEL
			acceldevice.write(CNTRL1_acc, accSettings1);
			acceldevice.write(CNTRL4_acc, accSettings4);

			// COMPASS enable
			magdevice.write(CNTRL1_mag, magSettings1);// DO = 011 (7.5 Hz ODR)
			magdevice.write(CNTRL2_mag, magSettings2);// GN = 001 (+/- 1.3 gauss full scale)
			magdevice.write(CNTRL3_mag, magSettings3);// MD = 00 (continuous-conversion mode)
//	        //// LSM303DLHC Magnetometer (from the c code)
//
//	        // DO = 011 (7.5 Hz ODR)
//	        writeMagReg(LSM303_CRA_REG_M, 0b00001100);
//			    #define LSM303_CRA_REG_M         0x00 // LSM303DLH, LSM303DLM, LSM303DLHC
//	        // GN = 001 (+/- 1.3 gauss full scale)
//	        writeMagReg(LSM303_CRB_REG_M, 0b00100000);
//              #define LSM303_CRB_REG_M         0x01 // LSM303DLH, LSM303DLM, LSM303DLHC

			//	        // MD = 00 (continuous-conversion mode)
//	        writeMagReg(LSM303_MR_REG_M, 0b00000000);
//    			#define LSM303_MR_REG_M  0x02 // LSM303DLH, LSM303DLM, LSM303DLHC

		} catch(IOException e) {
			logger.error("Unable to communicate with the MiniMU, we're not going to be getting any sensor data :-(", e);
			validLoad = false;
		}

		if (validLoad && bus != null & acceldevice != null) {
			start();
		}

		storeSensor(this);
		loadedInstance = this;
	}

//	public void update() throws IOException {
//
//		for (SensorUpdateListener sListener: listeners){
//			SensorUpdateListener sl = (SensorUpdateListener) sListener;
//
//			DataBead db = new DataBead();
//			db.put("Accelerator",this.readSensorsAccel());
//			db.put("Gyrometer", this.readSensorsGyro());
//			db.put("Magnetometer", this.readSensorsMag());
//
//			sl.getData(db);
//			sl.getSensor(db2);
//
//		}
//	}

	private void start() {
		Runnable task = new Runnable() {
			@Override
			public void run() {
				while(true) {
					try {
						gyroData = readSensorsGyro();
						accelData = readSensorsAccel();
						magData = readSensorsMag();
						//pass data on to listeners

						notifyListeners();

					} catch (IOException e) {
							// System.out.println("MiniMU not receiving data.");
							// Assuming we might like this in dev?
							logger.debug("MiniMU not receiving data.");
					}
					try {
						Thread.sleep(10);		//TODO this should not be hardwired.
					} catch (InterruptedException e) {
						logger.error("Poll interval interupted while listening for MiniMu!", e);
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
		gyrodevice.read(0xa8, bytes, 0, bytes.length);
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
		acceldevice.read(0xa8, bytes, 0, bytes.length);
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

	private double[] readSensorsMag() throws IOException {
		int numElements = 3; //
		double[] result = {0, 0, 0};
		int bytesPerElement = 2; // assuming short?
		int numBytes = numElements * bytesPerElement; //
		byte[] bytes = new byte[numBytes]; //
		DataInputStream magIn;
		magdevice.read(0x83, bytes, 0, bytes.length);
		magIn = new DataInputStream(new ByteArrayInputStream(bytes));
		for (int i = 0; i < numElements; i++) {
			byte a = magIn.readByte(); //least sig
			byte b = magIn.readByte(); //most sig
			boolean[] abits = getBits(a);
			boolean[] bbits = getBits(b);
			boolean[] shortybits = new boolean[16];
			// The mag sensor is BIG ENDIAN on the lsm303dlhc
			// so lets flip b and a compared to Acc
			for(int j = 0; j < 8; j++) {
				shortybits[j] = abits[j];
			}
			for(int j = 0; j < 8; j++) {
				shortybits[j + 8] = bbits[j];
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
		return accelData;
	}

	@Override
	public double getGyroscopeX() {
		return 0;
	}

	@Override
	public double getGyroscopeY() {
		return 0;
	}

	@Override
	public double getGyroscopeZ() {
		return 0;
	}

	@Override
	public double[] getAccelerometerData() {
		return gyroData;
	}

	@Override
	public double getAccelerometerX() {
		return 0;
	}

	@Override
	public double getAccelerometerY() {
		return 0;
	}

	@Override
	public double getAccelerometerZ() {
		return 0;
	}

	@Override
	public double[] getMagnetometerData() {
		return magData;
	}

	@Override
	public double getMagnetometerX() {
		return 0;
	}

	@Override
	public double getMagnetometerY() {
		return 0;
	}

	@Override
	public double getMagnetometerZ() {
		return 0;
	}

	@Override
	public double getPitch() {
		return getGyroscopeX();
	}

	@Override
	public double getRoll() {
		return getGyroscopeY();
	}

	@Override
	public double getYaw() {
		return getGyroscopeZ();
	}

}
