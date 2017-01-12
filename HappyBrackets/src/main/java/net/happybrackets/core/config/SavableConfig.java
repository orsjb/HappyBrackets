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
