package net.happybrackets.intellij_plugin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import net.happybrackets.controller.ControllerMain;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * Created by z3502805 on 22/04/2016.
 *
 * Thank god for this site: http://alblue.bandlem.com/2011/08/intellij-plugin-development.html
 * Not much else out there about setting up an IntelliJ gui tool!
 *
 */
public class HappyBracketsPlugin implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        Component component = toolWindow.getComponent();
        if(component != null) component.getParent().add(new JLabel("Hello, World!"));

        JFXPanel jfxp = new JFXPanel();

//        ControllerMain

        Button b = new Button();

        Group root = new Group();
        Scene scene = new Scene(root, 540, 210);
        root.getChildren().add(b);

        jfxp.setScene(scene);
        component.getParent().add(jfxp);

    }
}
