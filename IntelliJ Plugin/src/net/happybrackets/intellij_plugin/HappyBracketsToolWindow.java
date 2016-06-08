package net.happybrackets.intellij_plugin;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import net.happybrackets.controller.gui.GUIManager;
import net.happybrackets.controller.http.FileServer;
import net.happybrackets.controller.network.ControllerAdvertiser;
import net.happybrackets.controller.network.DeviceConnection;
import net.happybrackets.core.Synchronizer;
import net.happybrackets.core.config.LoadableConfig;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.CountDownLatch;

/**
 * Created by z3502805 on 22/04/2016.
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
public class HappyBracketsToolWindow implements ToolWindowFactory {

    static boolean staticSetup = false;
    static DeviceConnection piConnection;
    static Synchronizer synchronizer;                               //runs independently, no interaction needed
    static private FileServer httpServer;
    static protected IntelliJControllerConfig config;
    static protected ControllerAdvertiser controllerAdvertiser;     //runs independently, no interaction needed
    private JFXPanel jfxp;
    private Scene scene;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        Platform.setImplicitExit(false);    //<-- essential voodoo (http://stackoverflow.com/questions/17092607/use-javafx-to-develop-intellij-idea-plugin-ui)
        jfxp = new JFXPanel();
        if(!staticSetup) {          //only run this stuff once per JVM
            String projectDir = project.getBaseDir().getCanonicalPath();
            String dir = PluginManager.getPlugin(
                    PluginId.getId("net.happybrackets.intellij_plugin.HappyBracketsToolWindow")
            ).getPath().toString();
            System.out.println("Plugin lives at: " + dir);
            String configFilePath = dir + "/classes/config/controller-config.json";
            if(new File(configFilePath).exists()) System.out.println("Config file exists!");
            //all of the below concerns the set up of singletons
            //TODO: use plugin path here. How?
            config = LoadableConfig.load(configFilePath, new IntelliJControllerConfig());
            if (config == null) config = new IntelliJControllerConfig();
            //set up config relevant directories
            config.setConfigDir(dir + "/classes/config");
            piConnection = new DeviceConnection(config);
            //setup controller broadcast
            try {
                controllerAdvertiser = new ControllerAdvertiser(config);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            controllerAdvertiser.start();
            //setup http httpServer
            try {
                httpServer = new FileServer(config);
            } catch (IOException e) {
                System.err.println("Unable to start http httpServer!");
                e.printStackTrace();
            }
            //test code: you can create a test pi if you don't have a real pi...
//    	    piConnection.createTestPI();
            //using synchronizer is optional, TODO: switch to control this, leave it on for now
            synchronizer = Synchronizer.getInstance();
            staticSetup = true;
        }
        //TODO: we may want to make a copy of the config so that we can set different aspects here
        IntelliJPluginGUIManager guiManager = new IntelliJPluginGUIManager(
                config, project, piConnection
        );
        scene = guiManager.setupGUI();
        jfxp.setScene(scene);
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(jfxp, "", false);
        toolWindow.getContentManager().addContent(content);
    }
}
