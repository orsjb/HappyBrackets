package net.happybrackets.controller.config;

import net.happybrackets.core.config.LoadableConfig;

public class ControllerConfig extends LoadableConfig {

    private String compositionsPath = null;
    private String workingDir;
    private String configDir;
    private Boolean useHostname;

    public String getCompositionsPath() {
        if (compositionsPath != null) {
            return compositionsPath;
        }
//        System.err.println("No composition path set in configuration.");
        return getWorkingDir();
    }

    public void setWorkingDir(String dir) {
        this.workingDir = dir;
    }

    public String getWorkingDir() {
        if(workingDir == null) {
            return super.getWorkingDir();
        }
        return workingDir;
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

    @Override
    public String getMyAddress() {
        if (useHostname()) {
            return getMyHostName();
        }
        return super.getMyAddress();
    }
}
