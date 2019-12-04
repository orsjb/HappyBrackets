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

	public static final String CLASS_EXT = ".class";

	final static Logger logger = LoggerFactory.getLogger(SendToDevice.class);

	public static void send(String full_class_name, List<LocalDeviceRepresentation> devices) throws Exception {
		String simple_class_name = new File(full_class_name).getName();
		String package_path = new File(full_class_name).getParent();
//		sendOLD(package_path, simpleClassName, hostnames);
		send(package_path, simple_class_name, devices);
	}


	/**
	 * Return al the files required to send this file and dependencies
	 * @param package_path The package path
	 * @param class_name The name of the class we want to get
	 * @return and array list of all the files required for this class
	 */
	public static ArrayList<String> allFilenames(String package_path, String class_name){
		ArrayList<String> ret = new ArrayList<String>();
		File package_dir = new File(package_path);
		File[] contents = package_dir.listFiles(); //This used to have a hard codded bin/ prepended to it but this is incompatible with the composition path being configurable now

		for(File f : contents) {
			String fname = f.getName();
			if (fname.endsWith(CLASS_EXT)) {
				boolean add_name = false;
				if (fname.startsWith(class_name + "$")) {
					add_name = true;
				} else if (fname.toLowerCase().contains("hbperm")) {
					add_name = true;
				} else if (fname.equals(class_name + CLASS_EXT)) {
					add_name = true;
				}
				//Using hbperm is a trick to solve dependencies issues. If you name a class with HBPerm in it then it will always get sent to the device along with any HBAction classes when something else from that package gets sent.
				if (add_name) {
					ret.add(fname);
				}
			}
		}

		return ret;
	}


	private static ArrayList<byte[][]> allFilesAsBytes(String package_path, String class_name, boolean encrypted)throws Exception	{
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
			) && fname.endsWith(CLASS_EXT))
			{
				all_files_as_bytes.add(encrypted? getClassFileAsEncryptedByteArray(package_path + "/" + fname): getClassFileAsUnencryptedByteArray(package_path + "/" + fname));
			}
		}

		all_files_as_bytes.add(encrypted? getClassFileAsEncryptedByteArray(package_path + "/" + class_name + CLASS_EXT): getClassFileAsUnencryptedByteArray(package_path + "/" + class_name + CLASS_EXT));

		return  all_files_as_bytes;
	}

	public static void send(String package_path, String class_name, List<LocalDeviceRepresentation> devices) throws Exception {
		//File package_dir = new File(package_path);
		//File[] contents = package_dir.listFiles(); //This used to have a hard codded bin/ prepended to it but this is incompatible with the composition path being configurable now

		ArrayList<byte[][]> encrypted_all_files_as_bytes = null;
		ArrayList<byte[][]> unencrypted_all_files_as_bytes = null;

		// first see if we are going to have to do encryption or not
		for(LocalDeviceRepresentation device : devices){
			if (device.isEncryptionEnabled()) {
				// see if we have already packed encrypted
				if (encrypted_all_files_as_bytes == null) {
					encrypted_all_files_as_bytes = allFilesAsBytes(package_path, class_name, true);
				}
			}
			else {
				// see if we have already packed unencrypted
				if (unencrypted_all_files_as_bytes == null)
				{
					unencrypted_all_files_as_bytes = allFilesAsBytes(package_path, class_name, false);
				}
			}
		}
		//now we have all the files as byte arrays
		//time to send
		for(LocalDeviceRepresentation device : devices) {
			ArrayList<byte[][]> finalEncrypted_all_files_as_bytes = encrypted_all_files_as_bytes;
			ArrayList<byte[][]> finalUnencrypted_all_files_as_bytes = unencrypted_all_files_as_bytes;

			Thread thread = new Thread(() -> {
					try {
						if (device.getIsConnected() || DeviceConnection.getDisabledAdvertise()) {
							ArrayList<byte[][]> all_files_as_bytes;
							if (device.isEncryptionEnabled())
							{
								all_files_as_bytes = finalEncrypted_all_files_as_bytes;
							}
							else
							{
								all_files_as_bytes = finalUnencrypted_all_files_as_bytes;

							}

							try {
								//send all of the files to this hostname
								device.setStatus("Sending " + class_name);
								for (byte[][] bytes : all_files_as_bytes) {
									device.send(bytes);
								}

								device.setStatus("Sent " + class_name);
								logger.debug("SendToDevice: sent to {}", device);
							} catch (Exception e) {
								device.setStatus("Send fail " + class_name);
							}
						}
						else
						{
							device.setStatus("Send fail connection" + class_name);
						}

					} catch (Exception e) {// remove the break below to just resume thread or add your own action

					}
			});

			//  write your code you want to execute before you start the thread below this line

			// write your code you want to execute before you start the thread above this line

			thread.start();// End threadFunction
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

	public static byte[][] getClassFileAsUnencryptedByteArray(String full_class_file_name) throws Exception {
		byte[] bytes = getClassFileAsByteArray(full_class_file_name);

		return new byte[][] {bytes};
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
