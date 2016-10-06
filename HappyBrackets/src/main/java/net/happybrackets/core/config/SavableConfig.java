package net.happybrackets.core.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.lang.reflect.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Class exists purely to hold static methods relating to the saving of configuration classes.
 * The static method save mirrors LoadableConfig.load() method used for loading config files.
 *
 * Created by Samg on 19/05/2016.
 */
public class SavableConfig {

   final static Logger logger = LoggerFactory.getLogger(SavableConfig.class);

    public static <T> boolean save(String fileName, T config) {
        logger.info("Saving: " + fileName);

        if (config == null) {
            logger.error("Argument 2, Config must be an instantiated object!");
            return false;
        }

        Gson gson = new GsonBuilder().create();

        try (Writer configFile = new FileWriter(fileName)) {
            gson.toJson(config, (Type) config.getClass(), configFile);
        } catch (IOException e) {
            logger.error("Unable to write to file: {}", fileName, e);
        }

        return true;
    }
}
