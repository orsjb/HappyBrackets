package net.happybrackets.controller.network;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import net.happybrackets.controller.config.ControllerConfig;


public class SendToDevice {

	public static void send(String fullClassName, String[] hostnames) throws Exception {
		String simpleClassName = new File(fullClassName).getName();
		String packagePath = new File(fullClassName).getParent();
//		sendOLD(packagePath, simpleClassName, hostnames);
		send(packagePath, simpleClassName, hostnames);
	}
	
	public static void send(String packagePath, String className, String[] hostnames) throws Exception {
		File packageDir = new File(packagePath);
		File[] contents = packageDir.listFiles(); //This used to have a hard codded bin/ prepended to it but this is incompatible with the composition path being configurable now
		ArrayList<byte[]> allFilesAsBytes = new ArrayList<byte[]>();
		System.out.println("The following files are being sent:");
		for(File f : contents) {
			System.out.println("    " + f);
			String fname = f.getName();
			if((
					fname.startsWith(className + "$") ||
					fname.toLowerCase().contains("hbperm")	//this is a trick to solve dependencies issues. If you name a class with HBPerm in it then it will always get sent to the Pi along with any HBAction classes.
				) && fname.endsWith(".class")) {
				allFilesAsBytes.add(getClassFileAsByteArray(packagePath + "/" + fname));
			}
		}
		allFilesAsBytes.add(getClassFileAsByteArray(packagePath + "/" + className + ".class"));
		//now we have all the files as byte arrays
		//time to send
		for(String hostname : hostnames) {
        	try {
				//send all of the files to this hostname
				for(byte[] bytes : allFilesAsBytes) {
					Socket s = new Socket(hostname, ControllerConfig.getInstance().getCodeToDevicePort());
					s.getOutputStream().write(bytes);
					s.close();
				}
				System.out.println("SendToDevice: sent to " + hostname);
        	} catch(Exception e) {
        		System.out.println("SendToDevice: unable to send to " + hostname);
        	}
        } 
	}

	public static byte[] getClassFileAsByteArray(String fullClassFileName) throws Exception {
		FileInputStream fis = new FileInputStream(new File(fullClassFileName)); // removed static attachment of bin/ to path
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int data = fis.read();
        while(data != -1){
            buffer.write(data);
            data = fis.read();
        }
        fis.close();
        byte[] bytes = buffer.toByteArray();
        buffer.close();
        return bytes;
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
			e.printStackTrace();
		} 
		return bytes;
	}
}

