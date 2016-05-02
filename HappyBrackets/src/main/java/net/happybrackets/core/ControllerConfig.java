package net.happybrackets.core;

public class ControllerConfig extends LoadableConfig implements EnvironmentConf {
    private String compositionsPath;

    public String getCompositionsPath() {
        if (compositionsPath != null) {
            return compositionsPath;
        }
        System.err.println("No composition path set in configuration!");
        return ".";
    }
}
