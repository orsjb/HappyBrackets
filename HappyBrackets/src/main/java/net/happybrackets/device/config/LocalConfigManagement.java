package net.happybrackets.device.config;

import net.happybrackets.device.config.template.NetworkInterfaces;

import java.io.*;

/**
 * This class holds various administrative functions for managing devices.
 *
 * Should this just be in the HB class? Or is it good to have a place to keep some functions outside of the API?
 */
public class LocalConfigManagement {

    public static boolean updateInterfaces(String interfacesPath, String ssid, String psk) {
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(interfacesPath), "utf-8"))) {
            writer.write( NetworkInterfaces.newConfig(ssid, psk) );
            return true;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
