package net.happybrackets.device;

import net.happybrackets.device.config.DeviceConfig;
import net.happybrackets.device.dynamic.HB;
import net.happybrackets.core.AudioSetup;

/**
 * Entry point for PI code.
 * 
 * //@param args
 */
public class DeviceMain {
	
	public static void main(String[] args) throws Exception {
        //manage configuration files;
		String configFile = "config/device-config.json";
        System.out.println("Reading config file: " + configFile);
		DeviceConfig config = DeviceConfig.load(configFile);

		//Ollie - commenting this out here because we don't want to do it unless requested from controller.
//        //getInstance fresh config file
//        String configUrl = "http://" + config.getControllerHostname() + ":" + config.getControllerHTTPPort() + "/config/device-config.json";
//        System.out.println("GET config file: " + configUrl);
//        OkHttpClient client = new OkHttpClient();
//        Request request = new okhttp3.Request.Builder()
//                .url(configUrl)
//                .build();
//        Response response = client.newCall(request).execute();
//
//        System.out.println("Saving new config file: " + configFile);
//        Files.write(Paths.get(configFile), response.body().string().getBytes());
//
//        //reload config from file again after pulling in updates
//        System.out.println("Reloading config file: " + configFile);
//        config = LoadableConfig.load(configFile, config);

		HB hb = new HB(AudioSetup.getAudioContext(args));
		if(args.length > 5) {
			boolean autostart = Boolean.parseBoolean(args[5]);
			if(autostart) {
				System.out.println("Detected autostart. Starting audio right away.");
				hb.startAudio();
			}
		}
	}
}
