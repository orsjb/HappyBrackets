package net.happybrackets.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

public abstract class EZShell {

	public static String call(String... args) {
		String response = null;
		try {
			ProcessBuilder pb = new ProcessBuilder(args);
			// To capture output from the shell
			Process shell = pb.start();
			InputStream shellIn = shell.getInputStream();
			InputStream shellError = shell.getErrorStream();
			// Wait for the shell to finish and get the return code
			response = convertStreamToStr(shellIn);
			String error = convertStreamToStr(shellError);
			shellIn.close();
			shellError.close();
			if(error.length() != 0) {
				String errorArgs = "";
				for(String s : args) {
					errorArgs += " " + s;
				}
				throw new IOException("EZShell ERROR! Calling: " + errorArgs + "\nShell error is: " + error);
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
		return response;
	}
	
	public static void callNoResult(String... args) {
		try {
			ProcessBuilder pb = new ProcessBuilder(args);
			// To capture output from the shell
			pb.start();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/*
	 * Code taken from http://singztechmusings.wordpress.com/2011/06/21/getting-started-with-javas-processbuilder-a-sample-utility-class-to-interact-with-linux-from-java-program/
	 * 
	 * To convert the InputStream to String we use the Reader.read(char[]
	 * buffer) method. We iterate until the Reader return -1 which means
	 * there's no more data to read. We use the StringWriter class to
	 * produce the string.
	 */
	public static String convertStreamToStr(InputStream is) throws IOException {
		if (is != null) {
			Writer writer = new StringWriter();
			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}
			return writer.toString();
		}
		else {
			return "";
		}
	}
}
