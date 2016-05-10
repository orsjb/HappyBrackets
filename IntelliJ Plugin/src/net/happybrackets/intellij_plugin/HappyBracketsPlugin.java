package net.happybrackets.intellij_plugin;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import net.happybrackets.controller.gui.GUIManager;
import net.happybrackets.controller.http.FileServer;
import net.happybrackets.controller.network.DeviceConnection;
import net.happybrackets.core.ControllerAdvertiser;
import net.happybrackets.core.ControllerConfig;
import net.happybrackets.core.LoadableConfig;
import net.happybrackets.core.Synchronizer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;

/**
 * Created by z3502805 on 22/04/2016.
 *
 * Thank god for this site: http://alblue.bandlem.com/2011/08/intellij-plugin-development.html
 * Not much else out there about setting up an IntelliJ gui tool!
 *
 * TODO:
 *    * fix icons.
 *    * redesign gui, perhaps with FXML.
 *    * deal with network connection issues, including being on two networks at the same time.
 *    * reload when moved (e.g., when moved from pinned to floating mode, the content currently disappears).
 *    * deal with finding the config dolder, and also setting it (thus know where the plugin folder lives).
 *    * deal with finding the compositions folder. It is possible we can make this context aware -- i.e., it looks at the build folder for the current project.
 *
 */
public class HappyBracketsPlugin implements ToolWindowFactory {

    DeviceConnection piConnection;
    Synchronizer synchronizer;
    String currentPIPO = "";
    protected ControllerConfig config;
    protected ControllerAdvertiser controllerAdvertiser;
    private FileServer httpServer;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        Component component = toolWindow.getComponent();
        JFXPanel jfxp = new JFXPanel();



        //TODO: what can we do here? Temp code.
        toolWindow.getComponent().add(new JLabel("DEBUG"));
        toolWindow.getComponent().add(new JLabel(project.getName()));
        toolWindow.getComponent().add(new JLabel(project.getBaseDir().getCanonicalPath()));

        String dir = PluginManager.getPlugin(PluginId.getId("net.happybrackets.intellij_plugin.HappyBracketsPlugin")).getPath().toString();
        System.out.println("Plugin lives at: " + dir);

        String configFilePath = dir + "/classes/config/controller-config.json";
        if(new File(configFilePath).exists()) System.out.println("Config file exists!");

        //TODO: use plugin path here. How?
        config = LoadableConfig.load(configFilePath, new ControllerConfig());
        if(config == null) config = new ControllerConfig();
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
        GUIManager guiManager = new GUIManager();
        Scene scene = guiManager.setupGUI(piConnection, config);
        //you can create a test pi if you don't have a real pi...
//    	piConnection.createTestPI();
        synchronizer = Synchronizer.get();
        //get normal desktop application behaviour - closing the stage terminates the app
        jfxp.setScene(scene);
        component.getParent().add(jfxp);
    }
}
