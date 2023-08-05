package net.happybrackets.intellij_plugin;

import com.intellij.notification.*;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;


/// The settings dialog for the HappyBrackets plugin.
///
/// Based on the example at https://plugins.jetbrains.com/docs/intellij/dialog-wrapper.html#example
public class ConfigurationScreenSwing extends DialogWrapper implements ConfigurationScreenModel.PopupPresenter {
    ConfigurationScreenModel model;

    final static Logger logger = LoggerFactory.getLogger(ConfigurationScreenSwing.class);

    public ConfigurationScreenSwing(ConfigurationScreenModel model) {
        super(true); // use current window as parent
        this.model = model;
        setTitle("HappyBrackets Settings");
        init();
    }

    /// Creates the main panel in the settings dialog.
    @Override
    protected JComponent createCenterPanel() {
        JPanel dialogPanel = new JPanel();
        dialogPanel.setLayout(new BoxLayout(dialogPanel, BoxLayout.PAGE_AXIS));
        dialogPanel.setPreferredSize(new Dimension(500, 600));

        dialogPanel.add(createSubPanel("Configuration", ConfigurationScreenModel.ConfigurationEditorType.config));

        dialogPanel.add(Box.createRigidArea(new Dimension(0,8)));

        dialogPanel.add(createSubPanel("Known devices", ConfigurationScreenModel.ConfigurationEditorType.devices));

        return dialogPanel;
    }

    /// Creates a new panel containing one of the two sections of the dialog.
    private JComponent createSubPanel(String name, ConfigurationScreenModel.ConfigurationEditorType configurationEditorType) {
        BorderLayout mainLayout = new BorderLayout();
        mainLayout.setVgap(5);

        JPanel panel = new JPanel (mainLayout);

        // Label at the top
        JLabel label = new JLabel(name);
        panel.add(label, BorderLayout.NORTH);

        // Text area for editing
        JTextArea textArea = new JTextArea(13, 60);
        textArea.setText(model.getInitialConfig(configurationEditorType));

        JBScrollPane scrollPane = new JBScrollPane(textArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Action buttons at the bottom of the section.
        JPanel actionButtonsPanel = new JPanel ();
        actionButtonsPanel.setLayout(new BoxLayout(actionButtonsPanel, BoxLayout.LINE_AXIS));

        ConfigurationScreenSwing screen = this;

        JButton loadButton = new JButton("Load");
        loadButton.addActionListener((ActionEvent e) ->
            textArea.setText(model.load(configurationEditorType, screen))
        );
        actionButtonsPanel.add(loadButton);

        actionButtonsPanel.add(Box.createRigidArea(new Dimension(5,0)));

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener((ActionEvent e) -> model.save(textArea.getText(), screen, configurationEditorType));

        actionButtonsPanel.add(saveButton);
        actionButtonsPanel.add(Box.createRigidArea(new Dimension(5,0)));
        JButton applyButton = new JButton("Apply");
        applyButton.setEnabled(false);
        applyButton.addActionListener((ActionEvent e) -> {
            model.apply(textArea.getText(), configurationEditorType);
            // You can't apply exactly the same config multiple times.
            applyButton.setEnabled(false);
        });
        actionButtonsPanel.add(applyButton);

        // If the text changes, enable the 'apply' button.
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyButton.setEnabled(true);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyButton.setEnabled(true);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyButton.setEnabled(true);
            }
        });

        panel.add(actionButtonsPanel, BorderLayout.SOUTH);

        return panel;
    }

    /// Shows a popup notification.
    public void showPopup(String text) {
        Notification notification = NOTIFICATION_GROUP.createNotification(text, NotificationType.ERROR);
        Notifications.Bus.notify(notification);
    }

    /// A notification group used to display error messages.
    private static final NotificationGroup NOTIFICATION_GROUP =
            new NotificationGroup("HappyBrackets Settings Notifications",
                    NotificationDisplayType.BALLOON, true);


    @NotNull
    @Override
    protected Action[] createActions() {
        // Override default behaviour which places 'cancel' and 'ok' buttons at the bottom of the dialog.
        return new Action[] { };
    }
}
