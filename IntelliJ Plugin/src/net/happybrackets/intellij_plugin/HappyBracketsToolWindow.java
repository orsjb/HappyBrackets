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

import ch.qos.logback.classic.Level;
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
import net.happybrackets.core.BuildVersion;
import net.happybrackets.core.Synchronizer;
import net.happybrackets.core.config.DefaultConfig;
import net.happybrackets.core.logging.Logging;
import net.happybrackets.intellij_plugin.controller.ControllerEngine;
import net.happybrackets.intellij_plugin.controller.config.ControllerSettings;
import net.happybrackets.intellij_plugin.controller.network.ControllerAdvertiser;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.SwingUtilities;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by ollie on 22/04/2016.
 * <p>
 * Performs initialisation of static settings and components (eg {@link IntelliJControllerConfig} and
 * {@link ControllerAdvertiser}) and then launches the GUI {@link IntelliJPluginGUIManager}
 * <p>
 * Thank god for this site: http://alblue.bandlem.com/2011/08/intellij-plugin-development.html
 * Not much else out there about setting up an IntelliJ gui tool!
 * <p>
 * TODO:
 * * redesign gui, perhaps with FXML.
 * * deal with network connection issues, including being on two networks at the same time.
 * * reload when moved (e.g., when moved from pinned to floating mode, the content currently disappears).
 * * deal with finding the config dolder, and also setting it (thus know where the plugin folder lives).
 * * deal with finding the compositions folder. It is possible we can make this context aware -- i.e., it looks at the build folder for the current project.
 */
//@SuppressWarnings("ALL")
public class HappyBracketsToolWindow implements ToolWindowFactory {
    // Whether to use the new Swing UI or the legacy JFX UI.
    final static Logger logger = LoggerFactory.getLogger(HappyBracketsToolWindow.class);
    static final Object advertiseStopLock = new Object();
    static protected IntelliJControllerConfig config;
    static boolean staticSetup = false;
    static Synchronizer synchronizer;                               //runs independently, no interaction needed
    static int numberStartingToolwindows = 0;
    private static volatile boolean javafxInitialized = false;
    // define how long to wait before starting thread
    final int AUTO_PROBE_WAIT_PERIOD = 10000;
    private JComponent rootComponent;
    private Scene scene;


    /**
     * Create a simple tally to determine how many toolwindws are currently in the create process
     *
     * @param tally increment while creating and decrement when leaving
     * @return the crrent number of numberStartingToolwindows
     */
    static int updateNumToolwindowsCreated(int tally) {
        numberStartingToolwindows += tally;
        return numberStartingToolwindows;
    }

    /**
     * Load singletons when loading first project
     *
     * @param project_dir
     */
    synchronized static void loadSingletons(String project_dir, Class calling_class) {
        if (!staticSetup) {          //only run this stuff once per JVM
            logger.info("Running static setup (first instance of HappyBrackets)");

            ControllerEngine controller_engine = ControllerEngine.getInstance();

            logger.info("Loading plugin settings from " + ControllerSettings.getDefaultSettingsLocation());
            ControllerSettings settings = controller_engine.loadSettings(getPluginLocation());
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
            } else {
                logger.debug("Loading config from plugin jar.");
                InputStream input = calling_class.getResourceAsStream("/config/controller-config.json");

                String config_JSON = new Scanner(input).useDelimiter("\\Z").next();
                logger.info("Loaded config: {}", config_JSON);
                setConfig(config_JSON, getDefaultConfigFolder());
            }

            // test code: you can create a test pi if you don't have a real pi...
            createTestDevices();

            //using synchronizer is optional, TODO: switch to control this, leave it on for now
            synchronizer = Synchronizer.getInstance();
            staticSetup = true;
        } else {
            logger.info("HappyBrackets static setup already completed previously.");
        }
    }

    static void createTestDevices() {
        ControllerEngine.getInstance().getDeviceConnection().createFakeTestDevices();
    }

    /**
     * Loads the specified configuration file, resets the statically stored
     * configuration to the newly loaded config, and recreates or resets the
     * relevant components (DeviceConnection, FileServer, ControllerAdvertiser).
     * <p>
     * TODO This will not update the IntelliJPluginGUIManager(s), need to
     * determine how to handle this (do we support multiple GUIs? Should they
     * have project specific or global settings?) At the moment we assume the
     * IntelliJPluginGUIManager initiated loading the new config and will
     * reset itself accordingly.
     *
     * @param configFilePath Path to the config file.
     * @throws IOException If there is an error reading the given config file.
     */
    static void setConfigFromFile(String config_file_path) throws IOException {
        File new_config_file = new File(config_file_path);
        if (!new_config_file.isFile() || !new_config_file.canRead()) {
            throw new IllegalArgumentException("Specified configuration file does not exist or is not readable, path is: " + config_file_path);
        }

        logger.error("Loading config from: {}", new_config_file);
        String config_JSON = new Scanner(new_config_file).useDelimiter("\\Z").next();
        logger.info("Loaded config: {}", config_JSON);

        setConfig(config_JSON, new_config_file.getParent());
    }

    /**
     * Resets the statically stored configuration to the provided config, and
     * recreates or resets the relevant components (DeviceConnection,
     * FileServer, ControllerAdvertiser).
     * <p>
     * TODO This will not update the IntelliJPluginGUIManager(s), need to
     * determine how to handle this (do we support multiple GUIs? Should they
     * have project specific or global settings?) At the moment we assume the
     * IntelliJPluginGUIManager initiated loading the new config and will
     * reset itself accordingly.
     *
     * @param configJSON The new configuration, in JSON format.
     * @param configDir  The folder containing the configuration, if the configuration was loaded from a file (null otherwise).
     * @throws IllegalArgumentException If there is an error loading or parsing the given config file.
     */
    static void setConfig(String config_JSON, String config_dir) {
        ControllerEngine controller_engine = ControllerEngine.getInstance();

        try {
            config = IntelliJControllerConfig.loadFromString(config_JSON);
            controller_engine.setControllerConfig(config);
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException("Could not parse specified configuration.", e);
        }

        controller_engine.setCurrentConfigString(config_JSON);

        config.setConfigDir(config_dir);

        // If a custom known devices path has been loaded/saved previously, reload it and apply/override that possibly specified in the controller config file.
        String known_devices_path = ControllerEngine.getInstance().getSettings().getString("knownDevicesPath");
        if (known_devices_path == null) {
            // Otherwise get default known devices file.
            known_devices_path = HappyBracketsToolWindow.getDefaultKnownDevicesPath();
        }

        config.setKnownDevicesFile(known_devices_path);
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

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow tool_window) {

        ControllerEngine controller_engine = ControllerEngine.getInstance();

        synchronized (advertiseStopLock) {
            // Increment our count
            updateNumToolwindowsCreated(1);

            if (controller_engine.getDeviceConnection() != null) {
                controller_engine.getDeviceConnection().setDisableAdvertise(true);
            }
        }

        //awful hack but we need to prompt JavaFX to initialise itself, this will do it
        if (!GlobalConfigurationFlags.useSwingUI) {
            new JFXPanel();
        }

        Logging.AddFileAppender((ch.qos.logback.classic.Logger) LoggerFactory.getLogger("root"), "Plugin", getPluginLocation() + "/controller.log", Level.DEBUG);

        logger.info("*** HappyBrackets IntelliJ Plugin launching ***");
        Platform.setImplicitExit(false);    //<-- essential voodoo (http://stackoverflow.com/questions/17092607/use-javafx-to-develop-intellij-idea-plugin-ui)

        String project_dir = project.getBaseDir().getCanonicalPath();
        loadSingletons(project_dir, this.getClass());

        rootComponent = GlobalConfigurationFlags.useSwingUI ? createSwingUI(project) : createLegacyJavaFxUI(project);
        ContentFactory content_factory = ContentFactory.SERVICE.getInstance();
        Content content = content_factory.createContent(rootComponent, "", false);
        tool_window.getContentManager().addContent(content);

        String version_text = BuildVersion.getVersionBuildText();
        tool_window.setTitle(" - " + version_text);

        // Load our known devces based on project
        loadProjectKnownDevices(project_dir);

        synchronized (advertiseStopLock) {
            // Increment our count
            int new_active_creates = updateNumToolwindowsCreated(-1);

            if (controller_engine.getDeviceConnection() != null) {
                controller_engine.getDeviceConnection().setDisableAdvertise(new_active_creates > 0);
            }
        }

        // Now let us Wait a certain period and automaticaly start the proble for devices
        new Thread(() -> {
            try {
                Thread.sleep(AUTO_PROBE_WAIT_PERIOD);
                SwingUtilities.invokeLater(() -> {
                    // Assuming ControllerEngine.getInstance().doProbe() is thread-safe and can be called like this.
                    // Otherwise, ensure thread-safety within doProbe() or its Swing equivalent.
                    ControllerEngine.getInstance().doProbe();
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore the interrupted status
            }
        }).start();/* End threadFunction */


    }

    public static void checkAndSetJavaFXInitialized() {
        try {
            Platform.runLater(() -> {});
            javafxInitialized = true; // No exception, platform is initialized
//            System.out.println("state = " + javafxInitialized);
        } catch (IllegalStateException e) {
            javafxInitialized = false; // Exception caught, platform not initialized
//            System.out.println("HB toolwindow thread waiting...state = " +javafxInitialized);

        }
    }


    JComponent createSwingUI(Project project) {
        IntellijPluginSwingGUIManager guiManager = new IntellijPluginSwingGUIManager();
        System.out.println("calling root component");
        return guiManager.getRootComponent();
    }

    JComponent createLegacyJavaFxUI(Project project) {
        IntelliJPluginGUIManager gui_manager = new IntelliJPluginGUIManager(project);
        JFXPanel jfxPanel = new JFXPanel();
        System.out.println("calling root component");

        scene = gui_manager.setupGUI();
        jfxPanel.setScene(scene);
        return jfxPanel;
    }

    /**
     * Loads the config file in the project config path
     *
     * @param project_dir The project Directory
     * @return true if a known config file was found
     */
    boolean loadProjectKnownDevices(String project_dir) {
        boolean ret = false;

        String full_file_path = project_dir + DefaultConfig.CONFIG_DIRECTORY + DefaultConfig.KNOWN_DEVICES_FILE;

        try {
            Scanner s = new Scanner(new File(full_file_path));
            List<String> lines = new ArrayList<>();
            while (s.hasNext()) {
                lines.add(s.nextLine());
            }
            s.close();
            ControllerEngine.getInstance().getDeviceConnection().setKnownDevices(lines.toArray(new String[0]));
            ret = true;
        } catch (Exception ex) {
        }
        return ret;
    }
}
