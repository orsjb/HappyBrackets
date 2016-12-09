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

package net.happybrackets.controller.http;

/**
 *  FileServer implements a simple http server daemon to allow attached PIs to request files from the controller.
 *
 *  TODO: Create an interface for adding files to our file server
 *
 * Created by Sam on 26/04/2016.
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import net.happybrackets.controller.config.ControllerConfig;
import fi.iki.elonen.NanoHTTPD;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileServer extends NanoHTTPD {

   final static Logger logger = LoggerFactory.getLogger(FileServer.class);

    protected static String readFile(String path, String encoding) {
        Scanner scanner = null;
        String text = null;
        try {
            scanner = new Scanner( new File(path), encoding );
            text = scanner.useDelimiter("\\A").next();
        }
        catch (FileNotFoundException e) {
            logger.error("Unable to access: {}", path, e);
        }

        if (scanner != null) {
            scanner.close();
        }

        return text;
    }
    protected static String readFile(String path, String encoding, String defaultSuffix) {
        String text = readFile(path, encoding);
        if (text == null) {
            logger.debug("Trying default: {}{}", path, defaultSuffix);
            text = readFile(path + defaultSuffix, encoding);
        }

        return text;
    }

    private ControllerConfig config;
    private PathMapper pathMap;

    //server status:
    private static Response.IStatus statusOK = new Response.IStatus() {
        @Override
        public String getDescription() { return "200 OK\n"; }
        @Override
        public int getRequestStatus() {  return 200; }
    };
    private static Response.IStatus statusError = new Response.IStatus() {
        @Override
        public String getDescription() { return "500 Error\n"; }
        @Override
        public int getRequestStatus() {  return 500; }
    };

    public FileServer(ControllerConfig config) throws IOException {
        super(config.getControllerHTTPPort());
        this.config = config;
        this.pathMap = new PathMapper();
        //setup required paths
        pathMap.addPath("/config/device-config.json", new WithDefaultResponse("config/device-config.json", ".default"));
        pathMap.addPath("/config/device-wifi-config.json", new WithDefaultResponse("config/device-wifi-config.json", ".default"));

        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        logger.info("Running! Point your browser to http://localhost:{}/", config.getControllerHTTPPort());
    }

    public static void main(String[] args) {
        try {
            new FileServer(new ControllerConfig());
        } catch (IOException ioe) {
            logger.error("Couldn't start server!", ioe);
        }
    }

    @Override
    public Response serve(IHTTPSession session) {
//        String msg = "<html><body><h1>Hello server</h1>\n";
//        Map<String, String> parms = session.getParms();
//        if (parms.getInstance("username") == null) {
//            msg += "<form action='?' method='getInstance'>\n  <p>Your name: <input type='text' name='username'></p>\n" + "</form>\n";
//        } else {
//            msg += "<p>Hello, " + parms.getInstance("username") + "!</p>";
//        }
//        return newFixedLengthResponse(msg + "</body></html>\n");

        logger.debug("Request: {}", session.getUri());
        String response = pathMap.respond(session.getUri());

        if (response != null) {
            return newFixedLengthResponse(statusOK, "text/json", response);
        }
        else {
            logger.debug("Serving 500 error");
            return newFixedLengthResponse(statusError, "text/plain; charset=UTF-8", "Unable to resolve path: " + session.getUri() + "\n");
        }
    }
}
