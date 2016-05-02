package pi;

import core.LoadableConfig;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import pi.dynamic.DynamoPI;
import core.AudioSetup;
import core.PIConfig;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Entry point for PI code.
 * 
 * //@param args
 */
public class PIMain { 
	
	public static void main(String[] args) throws Exception {
        //manage configuration files;
		String configFile = "config/pi-config.json";
        System.out.println("Reading config file: " + configFile);
		PIConfig config = LoadableConfig.load(configFile, new PIConfig());

        //get fresh config file
        String configUrl = "http://" + config.getControllerHostname() + ":" + config.getControllerHTTPPort() + "/config/pi-config.json";
        System.out.println("GET config file: " + configUrl);
        OkHttpClient client = new OkHttpClient();
        Request request = new okhttp3.Request.Builder()
                .url(configUrl)
                .build();
        Response response = client.newCall(request).execute();

        System.out.println("Saving new config file: " + configFile);
        Files.write(Paths.get(configFile), response.body().string().getBytes());

        //reload config from file again after pulling in updates
        System.out.println("Reloading config file: " + configFile);
        config = LoadableConfig.load(configFile, config);

		DynamoPI pi = new DynamoPI(AudioSetup.getAudioContext(args), LoadableConfig.load(configFile, config));
		if(args.length > 5) {
			boolean autostart = Boolean.parseBoolean(args[5]);
			if(autostart) {
				System.out.println("Detected autostart. Starting audio right away.");
				pi.startAudio();
			}
		}
	}
}
