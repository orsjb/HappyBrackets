package net.happybrackets.intellij_plugin.menu;

import  com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import javafx.application.Platform;
import net.happybrackets.intellij_plugin.ConfigurationScreen;
import net.happybrackets.intellij_plugin.ConfigurationScreenModel;
import net.happybrackets.intellij_plugin.ConfigurationScreenSwing;
import net.happybrackets.intellij_plugin.controller.ControllerEngine;
//import org.slf4j.Logger;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.Logger;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


import org.slf4j.LoggerFactory;

import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.status.OnConsoleStatusListener;

import javax.swing.*;

public class SettingsMenu extends AnAction {
//    final static Logger logger = (Logger) LoggerFactory.getLogger(SettingsMenu.class);

  @Override
    public void actionPerformed(AnActionEvent e) {
        try {
//            logger.debug("action performed");
            DataContext dataContext = e.getDataContext();
            Project project = DataKeys.PROJECT.getData(dataContext);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    ConfigurationScreen settings = new ConfigurationScreen(project);
                    settings.show();

                }
            });

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    ConfigurationScreenSwing settings = new ConfigurationScreenSwing(new ConfigurationScreenModel(ControllerEngine.getInstance(),project));
                    settings.show();
                }
            });
//            logger.debug("creating configuration screen");


//            logger.debug("creating configuration screen");

            //new ConfigurationScreenSwing().showAndGet();
        } catch (Exception ex) {
            ex.printStackTrace();
//            logger.error(ex.getStackTrace().toString());
        }

    }


}
