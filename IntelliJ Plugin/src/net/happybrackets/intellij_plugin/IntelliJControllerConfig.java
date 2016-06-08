package net.happybrackets.intellij_plugin;


import net.happybrackets.controller.config.ControllerConfig;
import net.happybrackets.core.config.LoadableConfig;

/**
 * Created by ollie on 13/05/2016.
 */
public class IntelliJControllerConfig extends ControllerConfig {

    public static IntelliJControllerConfig getInstance() {
        return (IntelliJControllerConfig)(LoadableConfig.getInstance());
    }


    public static IntelliJControllerConfig load(String configFile) {
        return (IntelliJControllerConfig)(LoadableConfig.load(configFile, new IntelliJControllerConfig()));
    }

}
