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

package net.happybrackets.controller.config;

import java.io.*;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains settings pertaining to the plugin, stored as mappings from String keys to values.
 *
 * TODO Add get methods as necessary for data types other than String.
 *
 * Created by oliver on 26/09/16.
 */
public class ControllerSettings {

    static String defaultSettingsFolder = "";

    protected Properties props = new Properties();
    final static Logger logger = LoggerFactory.getLogger(ControllerSettings.class);

    /**
     * Get settings object with default settings.
     * @return The Default Settings
     */
    public static ControllerSettings getDefaultSettings() {
        ControllerSettings settings = new ControllerSettings();
        return settings;
    }

    /**
     * Load settings from default location.
     * @return The Settings
     */
    public static ControllerSettings load() {
        return load(getDefaultSettingsLocation());
    }

    /**
     * Load settings from specified location. If the location does not exist the default settings with be returned.
     * @param path The path we are loading from
     * @return The Settings
     */
    public static ControllerSettings load(String path) {
        File file = new File(path);

        if (!file.exists()) {
            return getDefaultSettings();
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file.getAbsolutePath()))) {
            ControllerSettings settings = new ControllerSettings();
            settings.props.load(reader);
            return settings;
        }
        catch (Exception ex) {
            logger.warn("Error loading settings file. Using defaults. Error: {}", ex.getMessage());
            return getDefaultSettings();
        }
    }

    /**
     * Save settings to default location (overwriting if already present).
     */
    public void save() {
        save(getDefaultSettingsLocation());
    }

    /**
     * Save settings to specified location (overwriting if already present).
     */
    public void save(String path) {
        File file = new File(path);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file.getAbsolutePath(), false))) {
            props.store(writer, "Settings for the Happy Brackets IntelliJ Plugin.");
        }
        catch (Exception ex) {
            System.err.println("Unable to save settings. Error: " + ex.getMessage());
        }
    }

    /**
     * Set The folder where default settings will be stored
     * @param default_settings_folder the folder
     */
    public static void setDefaultSettingsFolder(String default_settings_folder) {
        defaultSettingsFolder = default_settings_folder;
    }


    /**
     * Returns the path to the default settings file. This is currently HappyBracketsToolWindow.getPluginLocation() + "/settings".
     */
    public static String getDefaultSettingsLocation() {
        return defaultSettingsFolder + "/settings";
    }

    /**
     * Removes the specified setting from these settings (if it exists).
     */
    public void clear(String setting) {
        props.remove(setting);
    }

    /**
     * Get the String value for the specified setting. Returns null if the setting has not been set.
     */
    public String getString(String setting) {
        return (String) props.get(setting);
    }

    /**
     * Get the String value for the specified setting. Returns defaultValue if the setting has not been set.
     */
    public String getString(String setting, String default_value) {
        if (props.containsKey(setting)) {
            return (String) props.get(setting);
        }
        return default_value;
    }

    /**
     * Set the value for the specified setting.
     */
    public void set(String setting, String value) {
        props.put(setting, value);
    }
}
