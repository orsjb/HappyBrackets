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

package net.happybrackets.controller.network;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import net.happybrackets.controller.config.ControllerConfig;

import net.happybrackets.core.Encryption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class SendToDevice {

	final static Logger logger = LoggerFactory.getLogger(SendToDevice.class);

	public static void send(String fullClassName, List<LocalDeviceRepresentation> devices) throws Exception {
		String simpleClassName = new File(fullClassName).getName();
		String packagePath = new File(fullClassName).getParent();
//		sendOLD(packagePath, simpleClassName, hostnames);
		send(packagePath, simpleClassName, devices);
	}

	public static void send(String packagePath, String className, List<LocalDeviceRepresentation> devices) throws Exception {
		File packageDir = new File(packagePath);
		File[] contents = packageDir.listFiles(); //This used to have a hard codded bin/ prepended to it but this is incompatible with the composition path being configurable now
		ArrayList<byte[][]> allFilesAsBytes = new ArrayList<byte[][]>();
		logger.debug("The following files are being sent:");
		for(File f : contents) {
			logger.debug("    {}", f);
			String fname = f.getName();
			if((
					fname.startsWith(className + "$") ||
					fname.toLowerCase().contains("hbperm")	//this is a trick to solve dependencies issues. If you name a class with HBPerm in it then it will always get sent to the device along with any HBAction classes when something else from that package gets sent.
				) && fname.endsWith(".class")) {
				allFilesAsBytes.add(getClassFileAsEncryptedByteArray(packagePath + "/" + fname));
			}
		}

		allFilesAsBytes.add(getClassFileAsEncryptedByteArray(packagePath + "/" + className + ".class"));
		//now we have all the files as byte arrays
		//time to send
		for(LocalDeviceRepresentation device : devices) {
        	try {
				//send all of the files to this hostname
				for (byte[][] bytes : allFilesAsBytes) {
					device.send(bytes);
				}
				logger.debug("SendToDevice: sent to {}", device);
        	} catch(Exception e) {
        		logger.error("SendToDevice: unable to send to {}", device, e);
        	}
        }
	}

	public static byte[] getClassFileAsByteArray(String fullClassFileName) throws Exception {
		FileInputStream fis = new FileInputStream(new File(fullClassFileName)); // removed static attachment of bin/ to path
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int data = fis.read();
		while (data != -1) {
			buffer.write(data);
			data = fis.read();
		}
		fis.close();
		byte[] bytes = buffer.toByteArray();
		buffer.close();
		return bytes;
	}

	public static byte[][] getClassFileAsEncryptedByteArray(String fullClassFileName) throws Exception {
		byte[] bytes = getClassFileAsByteArray(fullClassFileName);

		MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
		byte[] hash = sha256.digest(bytes);

		byte[][] ivAndEncData = Encryption.encrypt(ControllerConfig.getInstance().getEncryptionKey(), bytes, 0, bytes.length);

		return new byte[][] {hash, ivAndEncData[0], ivAndEncData[1]};
	}

	public static byte[] objectToByteArray(Object object) {
		byte[] bytes = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream out = null;
		try {
		  out = new ObjectOutputStream(bos);
		  out.writeObject(object);
		  bytes = bos.toByteArray();
		  out.close();
		  bos.close();
		} catch(Exception e) {
			logger.error("Unable to write object to byte array!", e);
		}
		return bytes;
	}
}
