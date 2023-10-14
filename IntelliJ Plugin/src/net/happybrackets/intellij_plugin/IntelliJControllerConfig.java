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

package net.happybrackets.intellij_plugin;


import net.happybrackets.core.config.LoadableConfig;
import net.happybrackets.intellij_plugin.controller.config.ControllerConfig;

/**
 * Created by ollie on 13/05/2016.
 */
public class IntelliJControllerConfig extends ControllerConfig {
    public static IntelliJControllerConfig getInstance() {
        return (IntelliJControllerConfig) (LoadableConfig.getInstance());
    }

    public static IntelliJControllerConfig load(String configFile) {
        return (IntelliJControllerConfig) (LoadableConfig.load(configFile, new IntelliJControllerConfig()));
    }

    public static IntelliJControllerConfig loadFromString(String configJSON) {
        return LoadableConfig.loadFromString(configJSON, IntelliJControllerConfig.class);
    }
}
