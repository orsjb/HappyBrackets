package net.happybrackets.intellij_plugin;


import net.happybrackets.controller.config.ControllerConfig;
import net.happybrackets.core.config.LoadableConfig;

import java.io.File;

/**
 * Created by ollie on 13/05/2016.
 */
public class IntelliJControllerConfig extends ControllerConfig {
    String pluginFolder;

    public static IntelliJControllerConfig getInstance() {
        return (IntelliJControllerConfig)(LoadableConfig.getInstance());
    }

    public static IntelliJControllerConfig load(String configFile) {
        return (IntelliJControllerConfig)(LoadableConfig.load(configFile, new IntelliJControllerConfig()));
    }

    public static IntelliJControllerConfig loadFromString(String configJSON) {
        return LoadableConfig.loadFromString(configJSON, IntelliJControllerConfig.class);
    }
}
