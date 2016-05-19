package net.happybrackets.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.lang.reflect.Type;

/**
 * This Class exists purely to hold static methods relating to the saving of configuration classes.
 * The static method save mirrors LoadableConfig.load() method used for loading config files.
 *
 * Created by Samg on 19/05/2016.
 */
public class SavableConfig {

    public static <T> boolean save(String fileName, T config) {
        System.out.println("Loading: " + fileName);

        if (config == null) {
            System.err.println("Argument 2, Config must be an instantiated object!");
            return false;
        }

        Gson gson = new GsonBuilder().create();

        try (Writer configFile = new FileWriter(fileName)) {
            gson.toJson(config, (Type) config.getClass(), configFile);
        } catch (IOException e) {
            System.err.println("Unable to write to file: " + fileName);
            e.printStackTrace();
        }

        return true;
    }
}
