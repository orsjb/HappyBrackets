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

package net.happybrackets.device.config;

import net.happybrackets.device.config.template.NetworkInterfaces;

import java.io.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class holds various administrative functions for managing devices.
 *
 */
public class LocalConfigManagement {

    final static Logger logger = LoggerFactory.getLogger(LocalConfigManagement.class);

    public static boolean updateInterfaces(String interfacesPath, String ssid, String psk) {

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(interfacesPath), "utf-8"))) {
            writer.write( NetworkInterfaces.newConfig(ssid, psk) );
            return true;
        } catch (UnsupportedEncodingException e) {
            logger.error("Unable to output {} in specified encoding!", interfacesPath, e);
        } catch (FileNotFoundException e) {
            logger.error("Unable to output to {}!", interfacesPath, e);
        } catch (IOException e) {
            logger.error("Error writing to {}!", interfacesPath, e);
        }
        return false;
    }
}
