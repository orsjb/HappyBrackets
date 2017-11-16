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

package net.happybrackets.intellij_plugin;

import com.google.gson.JsonSyntaxException;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import net.happybrackets.controller.http.FileServer;
import net.happybrackets.controller.network.ControllerAdvertiser;
import net.happybrackets.controller.network.DeviceConnection;
import net.happybrackets.core.BroadcastManager;
import net.happybrackets.core.BuildVersion;
import net.happybrackets.core.Synchronizer;
import net.happybrackets.core.logging.Logging;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.DatagramSocket;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;

/**
 * Created by ollie on 22/04/2016.
 *
 * Performs initialisation of static settings and components (eg {@link IntelliJControllerConfig} and
 * {@link ControllerAdvertiser}) and then launches the GUI {@link IntelliJPluginGUIManager}
 *
 * Thank god for this site: http://alblue.bandlem.com/2011/08/intellij-plugin-development.html
 * Not much else out there about setting up an IntelliJ gui tool!
 *
 * TODO:
 *    * redesign gui, perhaps with FXML.
 *    * deal with network connection issues, including being on two networks at the same time.
 *    * reload when moved (e.g., when moved from pinned to floating mode, the content currently disappears).
 *    * deal with finding the config dolder, and also setting it (thus know where the plugin folder lives).
 *    * deal with finding the compositions folder. It is possible we can make this context aware -- i.e., it looks at the build folder for the current project.
 *
 */
@SuppressWarnings("ALL")
public class HappyBracketsToolWindow implements ToolWindowFactory {

    static boolean staticSetup = false;
    static String currentConfigString;
    static IntelliJPluginSettings settings;
    static DeviceConnection deviceConnection = null;
    static Synchronizer synchronizer;                               //runs independently, no interaction needed
    static private FileServer httpServer;
    static protected IntelliJControllerConfig config;
    static protected ControllerAdvertiser controllerAdvertiser;     //runs independently, no interaction needed
    static private boolean controllerStarted = false;

    static protected BroadcastManager broadcastManager;
    private JFXPanel jfxp;
    private Scene scene;
    final static Logger logger = LoggerFactory.getLogger(HappyBracketsToolWindow.class);


    static int numberStartingToolwindows = 0;
    static final Object advertiseStopLock = new Object();

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow tool_window) {

        synchronized (advertiseStopLock) {
            // Increment our count
            updateNumToolwindowsCreated(1);

            if (deviceConnection != null) {
                deviceConnection.setDisableAdvertise(true);
            }
        }

        //awful hack but we need to prompt JavaFX to initialise itself, this will do it
        new JFXPanel();
        Logging.AddFileAppender( (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("root"), "Plugin", getPluginLocation() + "/controller.log", Level.DEBUG);

        logger.info("*** HappyBrackets IntelliJ Plugin launching ***");
        Platform.setImplicitExit(false);    //<-- essential voodoo (http://stackoverflow.com/questions/17092607/use-javafx-to-develop-intellij-idea-plugin-ui)

        String project_dir = project.getBaseDir().getCanonicalPath();
        loadSingletons(project_dir, this.getClass());

        IntelliJPluginGUIManager gui_manager = new IntelliJPluginGUIManager(project);
        jfxp = new JFXPanel();
        scene = gui_manager.setupGUI();
        jfxp.setScene(scene);
        ContentFactory content_factory = ContentFactory.SERVICE.getInstance();
        Content content = content_factory.createContent(jfxp, "", false);
        tool_window.getContentManager().addContent(content);

        String version_text = BuildVersion.getVersionText();
        tool_window.setTitle(" - " + version_text);

        // Do not start until we are at the end, otherwise, we are going to be getting messages before we are really ready for them
        startAdvertiser();

        synchronized (advertiseStopLock) {
            // Increment our count
            int new_active_creates = updateNumToolwindowsCreated(-1);

            if (deviceConnection != null) {
                deviceConnection.setDisableAdvertise(new_active_creates > 0);
            }
        }
    }

    /**
     * Create a simple tally to determine how many toolwindws are currently in the create process

     * @param tally increment while creating and decrement when leaving
     * @return the crrent number of numberStartingToolwindows
     */
    static int updateNumToolwindowsCreated(int tally){
        numberStartingToolwindows += tally;
        return numberStartingToolwindows;
    }
    /**
     * Start the Advertiser if it has not been started yet
     * WE MUST ONLY START ONCE
     */
    synchronized static void startAdvertiser()
    {
        if (controllerAdvertiser != null && !controllerStarted)
        {
            controllerStarted = true;
            controllerAdvertiser.start();
        }
    }
    /**
     * Load singletons when loading first project
     * @param project_dir
     */
    synchronized static  void loadSingletons(String project_dir, Class calling_class)
    {
        if(!staticSetup) {          //only run this stuff once per JVM
            logger.info("Running static setup (first instance of HappyBrackets)");


            logger.info("Loading plugin settings from " + IntelliJPluginSettings.getDefaultSettingsLocation());
            settings = IntelliJPluginSettings.load();
            // Save settings on exit.
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    settings.save();
                }
            });


            //TODO this is still buggy. We are doing this statically meaning it works only for the first loaded project.
            //all of the below concerns the set up of singletons

            String config_file_path = settings.getString("controllerConfigPath");
            if (config_file_path == null) {
                config_file_path = getDefaultControllerConfigPath();
                settings.set("controllerConfigPath", config_file_path);
            }

            if (new File(config_file_path).exists()) {
                logger.debug("Found config file: {}", config_file_path);

                try {
                    setConfigFromFile(config_file_path);
                } catch (IOException e) {
                    logger.error("Could not read the configuration file at {}", config_file_path);
                    config = new IntelliJControllerConfig();
                }
            }
            else {
                logger.debug("Loading config from plugin jar.");
                //String jarPath = PathUtil.getJarPathForClass(this.getClass());
                InputStream input = calling_class.getResourceAsStream("/config/controller-config.json");

                String config_JSON = new Scanner(input).useDelimiter("\\Z").next();
                logger.info("Loaded config: {}", config_JSON);
                setConfig(config_JSON, getDefaultConfigFolder());
            }

            //test code: you can create a test pi if you don't have a real pi...
            //deviceConnection.createTestDevice();
            //deviceConnection.createTestDevice();
            //using synchronizer is optional, TODO: switch to control this, leave it on for now
            synchronizer = Synchronizer.getInstance();
            staticSetup = true;
        } else {
            logger.info("HappyBrackets static setup already completed previously.");
        }

    }
    /**
     * Loads the specified configuration file, resets the statically stored
     * configuration to the newly loaded config, and recreates or resets the
     * relevant components (DeviceConnection, FileServer, ControllerAdvertiser).
     *
     * TODO This will not update the IntelliJPluginGUIManager(s), need to
     * determine how to handle this (do we support multiple GUIs? Should they
     * have project specific or global settings?) At the moment we assume the
     * IntelliJPluginGUIManager initiated loading the new config and will
     * reset itself accordingly.
     *
     * @param configFilePath Path to the config file.
     *
     * @throws IOException If there is an error reading the given config file.
     */
    static void setConfigFromFile(String config_file_path) throws IOException {
        File new_config_file = new File(config_file_path);
        if (!new_config_file.isFile() || !new_config_file.canRead()) {
            throw new IllegalArgumentException("Specified configuration file does not exist or is not readable, path is: " + config_file_path);
        }

        String config_JSON = new Scanner(new_config_file).useDelimiter("\\Z").next();
        logger.info("Loaded config: {}", config_JSON);
        setConfig(config_JSON, new_config_file.getParent());
    }

    /**
     * Resets the statically stored configuration to the provided config, and
     * recreates or resets the relevant components (DeviceConnection,
     * FileServer, ControllerAdvertiser).
     *
     * TODO This will not update the IntelliJPluginGUIManager(s), need to
     * determine how to handle this (do we support multiple GUIs? Should they
     * have project specific or global settings?) At the moment we assume the
     * IntelliJPluginGUIManager initiated loading the new config and will
     * reset itself accordingly.
     *
     * @param configJSON The new configuration, in JSON format.
     * @param configDir The folder containing the configuration, if the configuration was loaded from a file (null otherwise).
     *
     * @throws IllegalArgumentException If there is an error loading or parsing the given config file.
     */
    static void setConfig(String config_JSON, String config_dir) {
        try {
            config = IntelliJControllerConfig.loadFromString(config_JSON);
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException("Could not parse specified configuration.", e);
        }

        currentConfigString = config_JSON;

        config.setConfigDir(config_dir);

        // If a custom known devices path has been loaded/saved previously, reload it and apply/override that possibly specified in the controller config file.
        String known_devices_path = HappyBracketsToolWindow.getSettings().getString("knownDevicesPath");
        if (known_devices_path == null) {
            // Otherwise get default known devices file.
            known_devices_path = HappyBracketsToolWindow.getDefaultKnownDevicesPath();
        }
        config.setKnownDevicesFile(known_devices_path);

        // Dispose of previous components if necessary.
        if (httpServer != null) {
            logger.debug("Stopping FileServer.");
            httpServer.stop();
        }
        if (controllerAdvertiser != null) {
            logger.debug("Stopping ControllerAdvertiser");
            controllerAdvertiser.stop();
        }
        if (broadcastManager != null) {
            logger.debug("Disposing of BroadcastManager");
            broadcastManager.dispose();
        }
        if (deviceConnection != null) {
            logger.debug("Disposing of DeviceConnection");
            deviceConnection.dispose();
        }

        logger.debug("Compositions path: {}", config.getCompositionsPath());


        int listen_port = config.getBroadcastPort();

        try {
            //setup controller broadcast
            DatagramSocket alive_socket = new DatagramSocket();
            alive_socket.setReuseAddress(true);
            listen_port = alive_socket.getLocalPort();
        }
        catch (Exception ex)
        {
            logger.error("Unable to create unique socket ", ex );
        }

        logger.info("Starting ControllerAdvertiser");
        broadcastManager = new BroadcastManager(config.getMulticastAddr(), listen_port);
        broadcastManager.startRefreshThread();

        //set up device connection
        deviceConnection = new DeviceConnection(config, broadcastManager);

        controllerAdvertiser = new ControllerAdvertiser(config.getMulticastAddr(), config.getBroadcastPort(), listen_port);

        // Do Not start until we are at the end of initialisation
        //controllerAdvertiser.start();

        //setup http httpServer
        try {
            httpServer = new FileServer(config);
        } catch (IOException e) {
            logger.error("Unable to start HTTP server!", e);
        }
    }


    public static IntelliJPluginSettings getSettings() {
        return settings;
    }


    /**
     * Returns the JSON String used to create the current {@link IntelliJControllerConfig} for this HappyBracketsToolWindow.
     */
    public static String getCurrentConfigString() {
        return currentConfigString;
    }

    /**
     * Returns the absolute path to the plugin folder.
     */
    public static String getPluginLocation() {
        return PluginManager.getPlugin(
                PluginId.getId("net.happybrackets.intellij_plugin.HappyBracketsToolWindow")
        ).getPath().getAbsolutePath().toString();
    }

    /**
     * Returns the path to the default configuration folder, where files as such as controller-config.json
     * will typically reside.
     */
    public static String getDefaultConfigFolder() {
        String plugin_location = getPluginLocation();
        // IntelliJ doesn't provide a way of determining whether we're running in sandbox, or a nice way of including,
        // for example, the controller-config.json file outside of the jar file for the non-sandbox version.
        // So we just use the root plugin location if we don't find the /classes/config folder.
        if ((new File(plugin_location + "/classes/config")).exists()) {
            return plugin_location + "/classes/config";
        }
        return plugin_location;
    }

    public static String getDefaultControllerConfigPath() {
        return getDefaultConfigFolder() + "/controller-config.json";
    }

    public static String getDefaultKnownDevicesPath() {
        return getDefaultConfigFolder() + "/known_devices";
    }
}
