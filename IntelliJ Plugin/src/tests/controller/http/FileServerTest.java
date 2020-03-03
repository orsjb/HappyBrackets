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

package tests.controller.http;

//import net.happybrackets.controller.config.ControllerConfig;

import net.happybrackets.intellij_plugin.controller.config.ControllerConfig;
import net.happybrackets.intellij_plugin.controller.http.FileServer;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Sam on 26/04/2016.
 */
public class FileServerTest {
    private FileServer server;
    private ControllerConfig config;

    @Before
    public void setUp() throws Exception {
        config = new ControllerConfig();
        config = ControllerConfig.load("src/test/config/test-controller-config.json", config);
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
