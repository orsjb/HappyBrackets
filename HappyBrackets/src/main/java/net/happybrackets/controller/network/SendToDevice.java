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

	public static void send(String full_class_name, List<LocalDeviceRepresentation> devices) throws Exception {
		String simple_class_name = new File(full_class_name).getName();
		String package_path = new File(full_class_name).getParent();
//		sendOLD(package_path, simpleClassName, hostnames);
		send(package_path, simple_class_name, devices);
	}

	public static void send(String package_path, String class_name, List<LocalDeviceRepresentation> devices) throws Exception {
		File package_dir = new File(package_path);
		File[] contents = package_dir.listFiles(); //This used to have a hard codded bin/ prepended to it but this is incompatible with the composition path being configurable now
		ArrayList<byte[][]> all_files_as_bytes = new ArrayList<byte[][]>();
		logger.debug("The following files are being sent:");
		for(File f : contents) {
			logger.debug("    {}", f);
			String fname = f.getName();
			if((
					fname.startsWith(class_name + "$") ||
					fname.toLowerCase().contains("hbperm")	//this is a trick to solve dependencies issues. If you name a class with HBPerm in it then it will always get sent to the device along with any HBAction classes when something else from that package gets sent.
				) && fname.endsWith(".class")) {
				all_files_as_bytes.add(getClassFileAsEncryptedByteArray(package_path + "/" + fname));
			}
		}

		all_files_as_bytes.add(getClassFileAsEncryptedByteArray(package_path + "/" + class_name + ".class"));
		//now we have all the files as byte arrays
		//time to send
		for(LocalDeviceRepresentation device : devices) {
        	try {
				//send all of the files to this hostname
				for (byte[][] bytes : all_files_as_bytes) {
					device.send(bytes);
				}
				logger.debug("SendToDevice: sent to {}", device);
        	} catch(Exception e) {
        		logger.error("SendToDevice: unable to send to {}", device, e);
        	}
        }
	}

	public static byte[] getClassFileAsByteArray(String full_class_fileName) throws Exception {
		FileInputStream fis = new FileInputStream(new File(full_class_fileName)); // removed static attachment of bin/ to path
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

	public static byte[][] getClassFileAsEncryptedByteArray(String full_class_file_name) throws Exception {
		byte[] bytes = getClassFileAsByteArray(full_class_file_name);

		MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
		byte[] hash = sha256.digest(bytes);

		byte[][] iv_and_enc_data = Encryption.encrypt(ControllerConfig.getInstance().getEncryptionKey(), bytes, 0, bytes.length);

		return new byte[][] {hash, iv_and_enc_data[0], iv_and_enc_data[1]};
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
