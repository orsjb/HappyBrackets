package net.happybrackets.controller.http;

import net.happybrackets.controller.config.ControllerConfig;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

/**
 * Created by Sam on 26/04/2016.
 */
public class FileServerTest {
    private FileServer server;
    private ControllerConfig config;

    @Before
    public void setUp() throws Exception {
        config = new ControllerConfig();
        System.out.println("Starting Server");
        server = new FileServer(config);
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("Terminating Server");
        server.stop();
    }

    @Test
    public void readFile() throws Exception {
        System.out.println("------------ Starting readFile() test -------------");
        //add some diagnostics for current path
        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();
        System.out.println("Current relative path is: " + s);

        String json = server.readFile("src/test/config/test-device-config.json", "utf8");
        assertFalse( json == null );
        assertFalse( json.isEmpty() );
        assertTrue(  json.contains("useHostname") );
        System.out.println("------------ Finished readFile() test -------------");
    }

    @Test
    public void getPage() {
        System.out.println("------------ Starting getPage() test -------------");
        OkHttpClient client = new OkHttpClient();
        Request request = new okhttp3.Request.Builder()
                .url("http://localhost:" + config.getControllerHTTPPort() + "/config/device-config.json" + "\n")
                .build();

        String json = null;
        try {
            System.out.println("Executing getInstance request...");
            Response response = client.newCall(request).execute();

            System.out.println("Extracting page body...");
            json = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertFalse( json == null );
        assertFalse( json.isEmpty() );
        assertTrue(  json.contains("useHostname") );
        System.out.println("------------ Finished getPage() test -------------");
    }

}