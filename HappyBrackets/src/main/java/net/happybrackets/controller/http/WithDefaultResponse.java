package net.happybrackets.controller.http;

/**
 * A response object which wraps up the FileServer.readFile method and supplies a default suffix.
 * This method is designed for serving config files which fall back to a (file_name).suffix convention when (file_name) cannot be found.
 *
 * Created by Samg on 19/05/2016.
 */
public class WithDefaultResponse implements PathResponse {

    private String configPath;
    private String defaultSuffix;

    public WithDefaultResponse(String configPath, String defaultSuffix) {
        this.configPath = configPath;
        this.defaultSuffix = defaultSuffix;
    }

    @Override
    public String response() {
        return FileServer.readFile(configPath, "utf8", defaultSuffix);
    }
}
