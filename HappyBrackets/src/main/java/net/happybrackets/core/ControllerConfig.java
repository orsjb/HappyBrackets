package net.happybrackets.core;

public class ControllerConfig extends LoadableConfig {

    private String compositionsPath = null;
    private String workingDir;
    private String configDir;

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

}
