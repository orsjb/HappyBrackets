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

import net.happybrackets.core.config.LoadableConfig;

public class ControllerConfig extends LoadableConfig {

    private String compositionsPath = null;
    private String configDir;
    private Boolean useHostname;

    public String getCompositionsPath() {
        if (compositionsPath != null) {
            return compositionsPath;
        }
//        System.err.println("No composition path set in configuration.");
        return getWorkingDir();
    }

    public void setConfigDir(String configDir) {
        this.configDir = configDir;
    }

    public String getConfigDir() {
        return configDir;
    }

    public static ControllerConfig getInstance() {
        return (ControllerConfig)(LoadableConfig.getInstance());
    }

    public static ControllerConfig load(String configFile) {
        return (ControllerConfig)(LoadableConfig.load(configFile, new ControllerConfig()));
    }

    public static void setInstance(ControllerConfig instance) {
        singletonInstance = instance;
    }

    public boolean useHostname() {
        if(useHostname == null) {
            return true;
        }
        return useHostname;
    }

}
