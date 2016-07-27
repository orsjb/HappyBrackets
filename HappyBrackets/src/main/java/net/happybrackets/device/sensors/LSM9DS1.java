/*******************************************************************************
 * Copyright (c) 2011, 2016 Eurotech and/or its affiliates
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech
 *******************************************************************************/

/***************
 * Adapted for the Happy brackets project by Sam Ferguson (2016).
 *
 * Uses Pi4J instead of dio for accessing the pins
 *
 * We will return configuration information and scaling information so
 * this sensor can be compared to others.
 *
 ***************/

package net.happybrackets.device.sensors;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import net.happybrackets.device.sensors.sensor_types.AccelerometerSensor;
import net.happybrackets.device.sensors.sensor_types.GyroscopeSensor;
import net.happybrackets.device.sensors.sensor_types.MagnetometerSensor;

import java.io.IOException;
import java.nio.ByteBuffer;

//import com.company.sensehat.sensors.KuraException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;


public class LSM9DS1 extends Sensor implements GyroscopeSensor, AccelerometerSensor, MagnetometerSensor{
	
	//private static final Logger //s_logger.error = LoggerFactory.getLogger(LSM9DS1.class);

	public static final int LSM9DS1_ACCADDRESS = 0x6a;
	public static final int LSM9DS1_MAGADDRESS = 0x1c;


	// Accelerometer and gyroscope register address map
	public static final int ACT_THS				= 0x04;
	public static final int ACT_DUR				= 0x05;
	public static final int INT_GEN_CFG_XL		= 0x06;
	public static final int INT_GEN_THS_X_XL	= 0x07;
	public static final int INT_GEN_THS_Y_XL	= 0x08;
	public static final int INT_GEN_THS_Z_XL	= 0x09;
	public static final int INT_GEN_DUR_XL		= 0x0A;
	public static final int REFERENCE_G			= 0x0B;
	public static final int INT1_CTRL			= 0x0C;
	public static final int INT2_CTRL			= 0x0D;
	public static final int WHO_AM_I_XG			= 0x0F;
	public static final int CTRL_REG1_G			= 0x10;
	public static final int CTRL_REG2_G			= 0x11;
	public static final int CTRL_REG3_G			= 0x12;
	public static final int ORIENT_CFG_G		= 0x13;
	public static final int INT_GEN_SRC_G		= 0x14;
	public static final int OUT_TEMP_L			= 0x15;
	public static final int OUT_TEMP_H			= 0x16;
	public static final int STATUS_REG_0		= 0x17;
	public static final int OUT_X_L_G			= 0x18;
	public static final int OUT_X_H_G			= 0x19;
	public static final int OUT_Y_L_G			= 0x1A;
	public static final int OUT_Y_H_G			= 0x1B;
	public static final int OUT_Z_L_G			= 0x1C;
	public static final int OUT_Z_H_G			= 0x1D;
	public static final int CTRL_REG4			= 0x1E;
	public static final int CTRL_REG5_XL		= 0x1F;
	public static final int CTRL_REG6_XL		= 0x20;
	public static final int CTRL_REG7_XL		= 0x21;
	public static final int CTRL_REG8			= 0x22;
	public static final int CTRL_REG9			= 0x23;
	public static final int CTRL_REG10			= 0x24;
	public static final int INT_GEN_SRC_XL		= 0x26;
	public static final int STATUS_REG_1		= 0x27;
	public static final int OUT_X_L_XL			= 0x28;
	public static final int OUT_X_H_XL			= 0x29;
	public static final int OUT_Y_L_XL			= 0x2A;
	public static final int OUT_Y_H_XL			= 0x2B;
	public static final int OUT_Z_L_XL			= 0x2C;
	public static final int OUT_Z_H_XL			= 0x2D;
	public static final int FIFO_CTRL			= 0x2E;
	public static final int FIFO_SRC			= 0x2F;
	public static final int INT_GEN_CFG_G		= 0x30;
	public static final int INT_GEN_THS_XH_G	= 0x31;
	public static final int INT_GEN_THS_XL_G	= 0x32;
	public static final int INT_GEN_THS_YH_G	= 0x33;
	public static final int INT_GEN_THS_YL_G	= 0x34;
	public static final int INT_GEN_THS_ZH_G	= 0x35;
	public static final int INT_GEN_THS_ZL_G	= 0x36;
	public static final int INT_GEN_DUR_G		= 0x37;

	// Magnetic sensor register address map
	public static final int OFFSET_X_REG_L_M	= 0x05;
	public static final int OFFSET_X_REG_H_M	= 0x06;
	public static final int OFFSET_Y_REG_L_M	= 0x07;
	public static final int OFFSET_Y_REG_H_M	= 0x08;
	public static final int OFFSET_Z_REG_L_M	= 0x09;
	public static final int OFFSET_Z_REG_H_M	= 0x0A;
	public static final int WHO_AM_I_M			= 0x0F;
	public static final int CTRL_REG1_M			= 0x20;
	public static final int CTRL_REG2_M			= 0x21;
	public static final int CTRL_REG3_M			= 0x22;
	public static final int CTRL_REG4_M			= 0x23;
	public static final int CTRL_REG5_M			= 0x24;
	public static final int STATUS_REG_M		= 0x27;
	public static final int OUT_X_L_M			= 0x28;
	public static final int OUT_X_H_M			= 0x29;
	public static final int OUT_Y_L_M			= 0x2A;
	public static final int OUT_Y_H_M			= 0x2B;
	public static final int OUT_Z_L_M			= 0x2C;
	public static final int OUT_Z_H_M			= 0x2D;
	public static final int INT_CFG_M			= 0x30;
	public static final int INT_SRC_M			= 0x31;
	public static final int INT_THS_L_M			= 0x32;
	public static final int INT_THS_H_M			= 0x33;

	public static final int WHO_AM_I_AG_ID		= 0x68;
	public static final int WHO_AM_I_M_ID		= 0x3D;

	public static final int ACC_DEVICE          = 0;
	public static final int MAG_DEVICE          = 1;


	public static final float ACC_SCALE_2G        = 0.000061F;
	public static final float ACC_SCALE_4G        = 0.000122F;
	public static final float ACC_SCALE_8G        = 0.000244F;
	public static final float ACC_SCALE_16G       = 0.000732F;
	
	public static final float ACCEL_CAL_MIN_X     = -0.988512F;
	public static final float ACCEL_CAL_MIN_Y     = -1.011500F;
	public static final float ACCEL_CAL_MIN_Z     = -1.012328F;
	public static final float ACCEL_CAL_MAX_X     = 1.006410F;
	public static final float ACCEL_CAL_MAX_Y     = 1.004973F;
	public static final float ACCEL_CAL_MAX_Z     = 1.001244F;
	
	public static final float GYRO_SCALE_250      = (float) (Math.PI / 180.0) * 0.00875F;
	public static final float GYRO_SCALE_500      = (float) (Math.PI / 180.0) * 0.0175F;
	public static final float GYRO_SCALE_2000     = (float) (Math.PI / 180.0) * 0.07F;
	
	public static final float GYRO_BIAS_X_INIT    = 0.024642F;
	public static final float GYRO_BIAS_Y_INIT    = 0.020255F;
	public static final float GYRO_BIAS_Z_INIT    = -0.011905F;
	
	public static final float GYRO_LEARNING_ALPHA   = 2.0F;
	public static final float GYRO_CONTINIOUS_ALPHA = 0.01F;
	
	public static final float ACC_ZERO            = 0.05F;
	public static final float GYRO_ZERO           = 0.2F;
	
	public static final float MAG_SCALE_4        = 0.014F;
	public static final float MAG_SCALE_8        = 0.029F;
	public static final float MAG_SCALE_12       = 0.043F;
	public static final float MAG_SCALE_16       = 0.058F;
	
	public static final float COMPASS_ALPHA               = 0.2F;
	public static final float COMPASS_MIN_X               = -26.074535F;
	public static final float COMPASS_MIN_Y               = -2.034567F;
	public static final float COMPASS_MIN_Z               = -14.253133F;
	public static final float COMPASS_MAX_X               = 49.599648F;
	public static final float COMPASS_MAX_Y               = 70.567223F;
	public static final float COMPASS_MAX_Z               = 55.166424F;
	public static final float COMPASS_ELLIPSOID_OFFSET_X  = 0.268940F;
	public static final float COMPASS_ELLIPSOID_OFFSET_Y  = 0.530345F;
	public static final float COMPASS_ELLIPSOID_OFFSET_Z  = -0.120908F;
	public static final float COMPASS_ELLIPSOID_CORR_11   = 0.973294F;
	public static final float COMPASS_ELLIPSOID_CORR_12   = -0.014069F;
	public static final float COMPASS_ELLIPSOID_CORR_13   = -0.021423F;
	public static final float COMPASS_ELLIPSOID_CORR_21   = -0.014069F;
	public static final float COMPASS_ELLIPSOID_CORR_22   = 0.965692F;
	public static final float COMPASS_ELLIPSOID_CORR_23   = -0.002746F;
	public static final float COMPASS_ELLIPSOID_CORR_31   = -0.021423F;
	public static final float COMPASS_ELLIPSOID_CORR_32   = -0.002746F;
	public static final float COMPASS_ELLIPSOID_CORR_33   = 0.980103F;

	private static I2CBus bus;
	private static I2CDevice accI2CDevice;
	private static I2CDevice magI2CDevice;
    private static boolean debug = false;
    private static boolean debugS = true;

    private double[] previousAcceleration = {0F, 0F, 0F};
	private double gyroBiasX;
	private double gyroBiasY;
	private double gyroBiasZ;
	private int   gyroSampleCount = 0;
	private static int gyroSampleRate = 0;

	private float compassScaleX;
	private float compassScaleY;
	private float compassScaleZ;
	private float compassOffsetX;
	private float compassOffsetY;
	private float compassOffsetZ;

	private double[] accelData = {0, 0, 0};
	private double[] gyroData  = {0, 0, 0};
	private double[] magData   = {0, 0, 0};

	private double[] CompassAverage = {0.0, 0.0, 0.0};

	public static void main(String[] args) throws Exception {

		// this address found from
		LSM9DS1 imu = new LSM9DS1();
		while (true) {

			double[] accVal = imu.getAccelerometerRaw();
            double[] gyrVal = imu.getGyroscopeRaw();
            double[] magVal = imu.getCompassRaw();
            if (debugS) {
                //System.out.println(String.format("Acc: %04.6f %04.6f %04.6f ", accVal[0], accVal[1], accVal[2]));
                System.out.println(String.format("Gyr: %04.6f %04.6f %04.6f ", gyrVal[0], gyrVal[1], gyrVal[2]));
                //System.out.println(String.format("Mag: %04.6f %04.6f %04.6f ", magVal[0], magVal[1], magVal[2]));
            }
            try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}
	}


	public LSM9DS1() throws IOException {

        bus = I2CFactory.getInstance(I2CBus.BUS_1);
		accI2CDevice = bus.getDevice(LSM9DS1_ACCADDRESS);
		magI2CDevice = bus.getDevice(LSM9DS1_MAGADDRESS);
		enableAccelerometer();
		enableGyroscope();
		enableMagnetometer();
		setCalibrationData();
        start();
	}


    public String getSensorName(){
        return "LSM9DS1";
    }




//	public static void closeDevice() {
//		try {
//			if (accI2CDevice != null && magI2CDevice != null) {
//// Power off the device : PD = 0 (power-down mode)
////				disableAccelerometer();
////				disableGyroscope();
////				disableMagnetometer();
////				accI2CDevice.close();
////				accI2CDevice = null;
////				magI2CDevice.close();
////				magI2CDevice = null;
//			}
//			if (imuSensor != null)
//				imuSensor = null;
//		} catch (Exception e) {
//			//////s_logger.error.error("Error in closing device", e);
//		}
//	}

	public static int read(int device, int register)  {
		int result = 0;
		try {
			if (device == 0) {
				accI2CDevice.write((byte) register);
				Thread.sleep(2);
				result = accI2CDevice.read();
			} else if (device == 1) {
				magI2CDevice.write((byte) register);
				Thread.sleep(2);
				result = magI2CDevice.read();
			} else {
				 System.out.println("Device not supported.");
			}
		} catch (IOException e) {
			//////s_logger.error.error("Unable to read to I2C device", e);
		} catch (InterruptedException e1) {
			//////s_logger.error.error(e1.toString());
		}

		return result;
	}

	public static void write(int device, int register, byte value)  {
		try {
			if (device == ACC_DEVICE) {
              if(debugS){  System.out.println("ACCTest " + device + " " + ACC_DEVICE + " reg " + register + " value " + value);}
                accI2CDevice.write(register,  value);
			} else if (device == MAG_DEVICE) {
                if(debugS){System.out.println("MagTest " + device + " " + MAG_DEVICE + " reg " + register + " value " + value);}
                magI2CDevice.write(register,  value);

               // System.out.println("Cant get here");
			} else {
				System.out.println("Device not supported.");
			}		
		} catch (IOException e) {
			////s_logger.error.error("Unable to write to I2C device", e);
		}
	}

    public static void write(int device, int register, byte[] buffer, int offset, int size)  {
        try {
            if (device == ACC_DEVICE) {
                //  System.out.println("ACCTest " + device + " " + ACC_DEVICE + " reg " + register + " value " + value);
                //accI2CDevice.write(register,  value);
                accI2CDevice.write(register, buffer, offset, size);
            } else if (device == MAG_DEVICE) {
                //System.out.println("MagTest " + device + " " + MAG_DEVICE + " reg " + register + " value " + value);
                magI2CDevice.write(register,  buffer, offset, size);

                // System.out.println("Cant get here");
            } else {
                System.out.println("Device not supported.");
            }
        } catch (IOException e) {
            ////s_logger.error.error("Unable to write to I2C device", e);
        }
    }


    public void getOrientationRadians() {
		// Returns the current orientation in radians using the aircraft principal axes of pitch, roll and yaw
		//s_logger.error.info("Method not yet implemented");
	}

	public void getOrientationDegrees() {
		// Returns the current orientation in degrees using the aircraft principal axes of pitch, roll and yaw
		//s_logger.error.info("Method not yet implemented");
	}

	public void getCompass() {
		// Gets the direction of North from the magnetometer in degrees
		//s_logger.error.info("Method not yet implemented");
	}

	public double[] getCompassRaw() {
		
		// Magnetometer x y z raw data in uT (micro teslas)
		double[] mag = new double[3];

		int magFS = 0;
		
			magFS = read(MAG_DEVICE, CTRL_REG2_M) & 0x00000060;
			if (magFS == 0x00000000) { // +/-4 Gauss
				mag[0] = ((read(MAG_DEVICE, OUT_X_H_M) << 8) | (read(MAG_DEVICE, OUT_X_L_M) & 0x000000FF)) * MAG_SCALE_4;
				mag[1] = ((read(MAG_DEVICE, OUT_Y_H_M) << 8) | (read(MAG_DEVICE, OUT_Y_L_M) & 0x000000FF)) * MAG_SCALE_4;
				mag[2] = ((read(MAG_DEVICE, OUT_Z_H_M) << 8) | (read(MAG_DEVICE, OUT_Z_L_M) & 0x000000FF)) * MAG_SCALE_4;
			}
			else if (magFS == 0x00000020) { // +/-8 Gauss
				mag[0] = ((read(MAG_DEVICE, OUT_X_H_M) << 8) | (read(MAG_DEVICE, OUT_X_L_M) & 0x000000FF)) * MAG_SCALE_8;
				mag[1] = ((read(MAG_DEVICE, OUT_Y_H_M) << 8) | (read(MAG_DEVICE, OUT_Y_L_M) & 0x000000FF)) * MAG_SCALE_8;
				mag[2] = ((read(MAG_DEVICE, OUT_Z_H_M) << 8) | (read(MAG_DEVICE, OUT_Z_L_M) & 0x000000FF)) * MAG_SCALE_8;				
			}
			else if (magFS == 0x00000040) { // +/-12 Gauss
				mag[0] = ((read(MAG_DEVICE, OUT_X_H_M) << 8) | (read(MAG_DEVICE, OUT_X_L_M) & 0x000000FF)) * MAG_SCALE_12;
				mag[1] = ((read(MAG_DEVICE, OUT_Y_H_M) << 8) | (read(MAG_DEVICE, OUT_Y_L_M) & 0x000000FF)) * MAG_SCALE_12;
				mag[2] = ((read(MAG_DEVICE, OUT_Z_H_M) << 8) | (read(MAG_DEVICE, OUT_Z_L_M) & 0x000000FF)) * MAG_SCALE_12;				
			}
			else if (magFS == 0x00000060) { // +/-16 Gauss
				mag[0] = ((read(MAG_DEVICE, OUT_X_H_M) << 8) | (read(MAG_DEVICE, OUT_X_L_M) & 0x000000FF)) * MAG_SCALE_16;
				mag[1] = ((read(MAG_DEVICE, OUT_Y_H_M) << 8) | (read(MAG_DEVICE, OUT_Y_L_M) & 0x000000FF)) * MAG_SCALE_16;
				mag[2] = ((read(MAG_DEVICE, OUT_Z_H_M) << 8) | (read(MAG_DEVICE, OUT_Z_L_M) & 0x000000FF)) * MAG_SCALE_16;				
			}
			
			mag[0] = -mag[0];
			mag[2] = -mag[2];
			
			calibrateMagnetometer(mag);
			
			////s_logger.error.error("Unable to read to I2C device.", e);

		// Swap X and Y axis to match SenseHat library
		double[] Compass = new double[3];
		Compass[0] = CompassAverage[1];
		Compass[1] = CompassAverage[0];
		Compass[2] = CompassAverage[2];
		
		return Compass;
	}

	public void getGyroscope() {
		// Gets the orientation in degrees from the gyroscope only
		//s_logger.error.info("Method not yet implemented");
	}

	public double[] getGyroscopeRaw() {
		
		// Gyroscope x y z raw data in radians per second
		double[] gyro = new double[3];
        float scaleFactor = 0;
		int gyroFSR = 0;
		gyroFSR = read(ACC_DEVICE, CTRL_REG1_G) & 0x00000018;
		if (gyroFSR == 0x00000000) { scaleFactor = GYRO_SCALE_250; }// 250
        else if (gyroFSR == 0x00000008) {scaleFactor = GYRO_SCALE_500; }// 500
        else if (gyroFSR == 0x00000018) {scaleFactor = GYRO_SCALE_2000; }// 2000

        gyro[0] = ((read(ACC_DEVICE, OUT_X_H_G) << 8) | (read(ACC_DEVICE, OUT_X_L_G) & 0x000000FF)) ;
        gyro[1] = ((read(ACC_DEVICE, OUT_Y_H_G) << 8) | (read(ACC_DEVICE, OUT_Y_L_G) & 0x000000FF)) ;
        gyro[2] = ((read(ACC_DEVICE, OUT_Z_H_G) << 8) | (read(ACC_DEVICE, OUT_Z_L_G) & 0x000000FF)) ;

        gyro[0] = flipBits(gyro[0]) * scaleFactor;
        gyro[1] = flipBits(gyro[1]) * scaleFactor;
        gyro[2] = flipBits(gyro[2]) * scaleFactor;

        gyro[2] = -gyro[2];
		calibrateGyroscope(gyro);

		return gyro;
		
	}
	public void getAccelerometer() {
		// Gets the orientation in degrees from the accelerometer only
		//s_logger.error.info("Method not yet implemented");
	}

	public double[] getAccelerometerRaw() {

		// Accelerometer x y z raw data in Gs
		double[] acc = new double[3];


        float scaleFactor = 0f;
        int accFS = 0;
        accFS = read(ACC_DEVICE, CTRL_REG6_XL) & 0x00000018; ///!!!!!!! remove +2

        if (accFS == 0x00000000) { scaleFactor = ACC_SCALE_2G;} // +/-2g
        else if (accFS == 0x00000010) { scaleFactor = ACC_SCALE_4G;}// +/-4g
        else if (accFS == 0x00000018) { scaleFactor = ACC_SCALE_8G;}// +/-8g
        else if (accFS == 0x00000008) { scaleFactor = ACC_SCALE_16G;}// +/-16g

        acc[0] = ((read(ACC_DEVICE, OUT_X_H_XL) << 8) | (read(ACC_DEVICE, OUT_X_L_XL) & 0x000000FF)) ;
        acc[1] = ((read(ACC_DEVICE, OUT_Y_H_XL) << 8) | (read(ACC_DEVICE, OUT_Y_L_XL) & 0x000000FF)) ;
        acc[2] = ((read(ACC_DEVICE, OUT_Z_H_XL) << 8) | (read(ACC_DEVICE, OUT_Z_L_XL) & 0x000000FF)) ;

        // TO CHECK TWOS COMPLEMENT
        if (debug) {
            boolean[] abits = getBits((byte) read(ACC_DEVICE, OUT_X_H_XL));
            boolean[] bbits = getBits((byte) read(ACC_DEVICE, OUT_X_L_XL));
            boolean[] shortybits = new boolean[16];
            for (int j = 0; j < 8; j++) {
                shortybits[j] = abits[j];
            }
            for (int j = 0; j < 8; j++) {
                shortybits[j + 8] = bbits[j];
            }
            System.out.print(acc[0] + " " + bits2String(shortybits) + " ");
        }

        // TWO'S COMPLEMENT DIDN'T WORK ORIGINALLY, SO ADDING THIS HERE.
        acc[0] = flipBits(acc[0]) * scaleFactor;
        acc[1] = flipBits(acc[1]) * scaleFactor;
        acc[2] = flipBits(acc[2]) * scaleFactor;

        if( debug){ System.out.print(acc[0] + " ");}

        // X and Y flipped then Swapped X and Y axis to match SenseHat library
        acc[0] = -acc[0];
        acc[1] = -acc[1];

        double accTemp = acc[1];
        acc[1] = acc[0];
        acc[0] = accTemp;

        if( debug){ System.out.println(acc[0] + " ");}

        calibrateAcceleration(acc);

		return acc;

	}

    private double flipBits(double input) {
        double output = (float) input;
        if (input > 32678) {
            output = input - 65536;
        }
        return output;
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

	public static void enableAccelerometer() {

		// Enable accelerometer with default settings (ODR=119Hz, BW=50Hz, FS=+/-8g)
		try {
			disableAccelerometer();
			Thread.sleep(100);
			byte value = 0x7B;
			write(ACC_DEVICE, CTRL_REG6_XL, value);
		//} catch (KuraException e) {
		//	System.out.println("Unable to write to I2C device.");
		} catch (InterruptedException e) {
			System.out.println(e.toString());
		}
	}
//
	public static void disableAccelerometer() {

		int ctrl_reg = 0x00000000;
			ctrl_reg = read(ACC_DEVICE, CTRL_REG6_XL) & 0x000000FF;
			int value = ctrl_reg & 0x0000001F;
			byte[] valueBytes = ByteBuffer.allocate(4).putInt(value).array();
			write(ACC_DEVICE, CTRL_REG6_XL, valueBytes, 0, 4);
			////s_logger.error.error("Unable to write to I2C device.", e);

	}

	public static void enableGyroscope() {

		// Enable gyroscope with default settings (ODR=119Hz, BW=31Hz, FSR=500, HPF=0.5Hz)
		try {
			disableGyroscope();
			Thread.sleep(1000);
			byte value = 0x69;
			write(ACC_DEVICE, CTRL_REG1_G, value);
			value = 0x44;
			write(ACC_DEVICE, CTRL_REG3_G, value);
			gyroSampleRate = 119;
//		} catch (KuraException e){
//			////s_logger.error.error("Unable to write to I2C device.", e);
		} catch (InterruptedException e) {
			////s_logger.error.error(e.toString());
		}

	}

	public static void disableGyroscope() {

		int ctrl_reg = 0x00000000;
		try {
			ctrl_reg = read(ACC_DEVICE, CTRL_REG1_G) & 0x000000FF;
			int value = ctrl_reg & 0x0000001F;
			byte[] buffer = ByteBuffer.allocate(4).putInt(value).array();
			write(ACC_DEVICE, CTRL_REG1_G, buffer, 0, 4);
		} catch (Exception e) {
			////s_logger.error.error("Can't write to the device.", e);
		}

	}

	public static void enableMagnetometer() {

		// Enable magnetometer with default settings (TEMP_COMP=0, DO=20Hz, FS=+/-400uT)
		try {
			disableMagnetometer();
			Thread.sleep(1000);
			byte value = 0x14;
			write(MAG_DEVICE, CTRL_REG1_M, value);
			value = 0x00;
			write(MAG_DEVICE, CTRL_REG2_M, value);
			write(MAG_DEVICE, CTRL_REG3_M, value);
//		} catch (KuraException e) {
			////s_logger.error.error("Unable to write to I2C device.", e);
		} catch (InterruptedException e) {
			////s_logger.error.error(e.toString());
		}

	}

	public static void disableMagnetometer() {

		try {
			byte value = 0x03;
			write(MAG_DEVICE, CTRL_REG3_M, value);
		} catch (Exception e) {
			////s_logger.error.error("Unable to write to I2C device.", e);
		}

	}

	
	private void calibrateAcceleration(double[] acc) {
		
	    if (acc[0] >= 0.0)
	    	acc[0] = acc[0] / ACCEL_CAL_MAX_X;
	    else
	    	acc[0] = acc[0] / (-ACCEL_CAL_MIN_X);

	    if (acc[1] >= 0.0)
	    	acc[1] = acc[1] / ACCEL_CAL_MAX_Y;
	    else
	    	acc[1] = acc[1] / (-ACCEL_CAL_MIN_Y);
	    
	    if (acc[2] >= 0.0)
	    	acc[2] = acc[2] / ACCEL_CAL_MAX_Z;
	    else
	    	acc[2] = acc[2] / (-ACCEL_CAL_MIN_Z);
	    
	}
	
	private void calibrateGyroscope(double[] gyro) {
		
		double[] deltaAcceleration = {0, 0, 0};
		deltaAcceleration[0] = previousAcceleration[0];
		deltaAcceleration[1] = previousAcceleration[1];
		deltaAcceleration[2] = previousAcceleration[2];
		
		double[] currentAcceleration = getAccelerometerRaw();
		deltaAcceleration[0] -= currentAcceleration[0];
		deltaAcceleration[1] -= currentAcceleration[1];
		deltaAcceleration[2] -= currentAcceleration[2];

		previousAcceleration = currentAcceleration;
		
		float accVectorLength = (float) Math.sqrt(Math.pow(deltaAcceleration[0], 2) + Math.pow(deltaAcceleration[1], 2) + Math.pow(deltaAcceleration[2], 2));
		float gyroVectorLength = (float) Math.sqrt(Math.pow(gyro[0], 2) + Math.pow(gyro[1], 2) + Math.pow(gyro[2], 2));
		if (accVectorLength < ACC_ZERO && gyroVectorLength < GYRO_ZERO) {
			// Correct the initial bias with real measures
			if (gyroSampleCount < (5 * gyroSampleRate)) {
				gyroBiasX = (1.0F - GYRO_LEARNING_ALPHA) * gyroBiasX + GYRO_LEARNING_ALPHA * gyro[0];
				gyroBiasY = (1.0F - GYRO_LEARNING_ALPHA) * gyroBiasY + GYRO_LEARNING_ALPHA * gyro[1];
				gyroBiasZ = (1.0F - GYRO_LEARNING_ALPHA) * gyroBiasZ + GYRO_LEARNING_ALPHA * gyro[2];
				
				gyroSampleCount++;
			}
			else {
				gyroBiasX = (1.0F - GYRO_CONTINIOUS_ALPHA) * gyroBiasX + GYRO_CONTINIOUS_ALPHA * gyro[0];
				gyroBiasY = (1.0F - GYRO_CONTINIOUS_ALPHA) * gyroBiasY + GYRO_CONTINIOUS_ALPHA * gyro[1];
				gyroBiasZ = (1.0F - GYRO_CONTINIOUS_ALPHA) * gyroBiasZ + GYRO_CONTINIOUS_ALPHA * gyro[2];
			}
		}
		
		gyro[0] -= gyroBiasX;
		gyro[1] -= gyroBiasY;
		gyro[2] -= gyroBiasZ;
	}
	
	public void setGyroSampleRate(int sampleRate) {
		gyroSampleRate = sampleRate;
	}
	
	private void calibrateMagnetometer(double[] mag) {
		
		mag[0] = (mag[0] - compassOffsetX) * compassScaleX;
		mag[1] = (mag[1] - compassOffsetY) * compassScaleY;
		mag[2] = (mag[2] - compassOffsetZ) * compassScaleZ;
		
		mag[0] -= COMPASS_ELLIPSOID_OFFSET_X;
		mag[1] -= COMPASS_ELLIPSOID_OFFSET_Y;
		mag[2] -= COMPASS_ELLIPSOID_OFFSET_Z;
		
		mag[0] = mag[0] * COMPASS_ELLIPSOID_CORR_11 +
				 mag[1] * COMPASS_ELLIPSOID_CORR_12 +
				 mag[2] * COMPASS_ELLIPSOID_CORR_13;
		
		mag[1] = mag[0] * COMPASS_ELLIPSOID_CORR_21 +
				 mag[1] * COMPASS_ELLIPSOID_CORR_22 +
				 mag[2] * COMPASS_ELLIPSOID_CORR_23;
		
		mag[2] = mag[0] * COMPASS_ELLIPSOID_CORR_31 +
				 mag[1] * COMPASS_ELLIPSOID_CORR_32 +
				 mag[2] * COMPASS_ELLIPSOID_CORR_33;
		
		CompassAverage[0] = mag[0] * COMPASS_ALPHA + CompassAverage[0] * (1.0F - COMPASS_ALPHA);
		CompassAverage[1] = mag[1] * COMPASS_ALPHA + CompassAverage[1] * (1.0F - COMPASS_ALPHA);
		CompassAverage[2] = mag[2] * COMPASS_ALPHA + CompassAverage[2] * (1.0F - COMPASS_ALPHA);
		
	}
	
	private void setCalibrationData() {
		
		gyroBiasX = GYRO_BIAS_X_INIT;
		gyroBiasY = GYRO_BIAS_Y_INIT;
		gyroBiasZ = GYRO_BIAS_Z_INIT;
		
		float compassSwingX = COMPASS_MAX_X - COMPASS_MIN_X;
		float compassSwingY = COMPASS_MAX_Y - COMPASS_MIN_Y;
		float compassSwingZ = COMPASS_MAX_Z - COMPASS_MIN_Z;
		
		float maxCompassSwing = Math.max(compassSwingX, Math.max(compassSwingY, compassSwingZ)) / 2.0F;
		
		compassScaleX = maxCompassSwing / (compassSwingX / 2.0F);
		compassScaleY = maxCompassSwing / (compassSwingY / 2.0F);
		compassScaleZ = maxCompassSwing / (compassSwingZ / 2.0F);

		compassOffsetX = (COMPASS_MAX_X + COMPASS_MIN_X) / 2.0F;
		compassOffsetY = (COMPASS_MAX_Y + COMPASS_MIN_Y) / 2.0F;
		compassOffsetZ = (COMPASS_MAX_Z + COMPASS_MIN_Z) / 2.0F;
		
	}


    private void start() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                while(true) {
                    gyroData = getGyroscopeRaw();
                    accelData = getAccelerometerRaw();
                    magData = getCompassRaw();
                    //pass data on to listeners
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

    @Override
    public double[] getGyroscopeData() {
        return accelData;
    }

    @Override
    public double[] getAccelerometerData() {
        double[] accDouble = (double[]) gyroData;
        return accDouble;
    }

    @Override
    public double[] getMagnetometerData() {
        double[] mag2 = (double[]) magData;
        return mag2;
    }
	
}
