package net.happybrackets.intellij_plugin;

import net.happybrackets.intellij_plugin.controller.network.DeviceConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * A class which is responsible for managing the sending and history of custom commands.
 *
 * You associate it with a text field for the command and send button via 'processKeyPress', 'setUpdateCommandDelegate'
 * and 'sendCommand' and it handles the necessary business logic.
 */
class CommandManager {
    static final int ALL = -1; // Send to all devices.
    static final int SELECTED = -2; // Send to selected device(s).

    private static final int UP_ARROW_KEY_CODE = 38;
    private static final int DOWN_ARROW_KEY_CODE = 40;

    private CommandHistoryManager historyManager = new CommandHistoryManager();
    private IntellijPluginSwingGUIManager.UpdateCommandDelegate updateCommandDelegate;
    private DeviceConnection deviceConnection;
    private final Logger logger = LoggerFactory.getLogger(CommandManager.class);

    // TODO pass device list view here
    CommandManager(DeviceConnection deviceConnection) {
        this.deviceConnection = deviceConnection;
    }

    /** Process a new key press in the text field. */
    void processKeyPress(int code) {
        String commandToUpdateTo = null;
        if (code == UP_ARROW_KEY_CODE) {
            logger.debug("Previous command");
            commandToUpdateTo = historyManager.previousCommand();
        } else if (code == DOWN_ARROW_KEY_CODE) {
            logger.debug("Next command");
            commandToUpdateTo = historyManager.nextCommand();
        }
        if (commandToUpdateTo != null && updateCommandDelegate != null) {
            updateCommandDelegate.updateCommand(commandToUpdateTo);
        }
    }

    /** Sends a command. Trigger this from a 'send' button. */
    // TODO(later): Refactor confusing devicesOrGroup to a better data structure.
    void sendCommand(String text,  int devicesOrGroup) {
        logger.debug("Sending command: " + text);
        String trimmedText = text.trim();

        historyManager.addCommand(trimmedText);

        String[] commands = trimmedText.split("[;]");    //different commands separated by ';'
        for (String command : commands) {
            command = command.trim();
            String[] elements = command.split("[ ]");
            String msg = elements[0];
            Object[] args = new Object[elements.length - 1];
            for (int i = 0; i < args.length; i++) {
                String s = elements[i + 1];
                try {
                    args[i] = Integer.parseInt(s);
                } catch (Exception ex) {
                    try {
                        args[i] = Double.parseDouble(s);
                    } catch (Exception exx) {
                        args[i] = s;
                    }
                }
            }
            if (devicesOrGroup == ALL) {
                deviceConnection.sendToAllDevices(msg, args);
            } else if (devicesOrGroup == SELECTED) {
                // TODO: Implement device selection and make this work.
                //deviceConnection.sendToDeviceList(deviceListView.getSelectionModel().getSelectedItems(), msg, args);
            } else {
                deviceConnection.sendToDeviceGroup(devicesOrGroup, msg, args);
            }
        }
    }

    /** Sets a listener for when the displayed command in the text field should be updated. */
    void setUpdateCommandDelegate(IntellijPluginSwingGUIManager.UpdateCommandDelegate delegate) {
        updateCommandDelegate = delegate;
    }

    /** A class to manage the command history. */
    class CommandHistoryManager {
        private List<String> commandHistory = new ArrayList<>();
        private int positionInCommandHistory = 0;


        // Navigates to the previous command in the history.
        String previousCommand() {
            positionInCommandHistory--;
            if (positionInCommandHistory < 0) positionInCommandHistory = 0;
            if (commandHistory.size() > 0) {
                String command = commandHistory.get(positionInCommandHistory);
                return command;
            }
            return null;
        }

        // Navigates to the next command in the history.
        String nextCommand() {
            positionInCommandHistory++;
            if (positionInCommandHistory >= commandHistory.size())
                positionInCommandHistory = commandHistory.size() - 1;
            if (commandHistory.size() > 0) {
                String command = commandHistory.get(positionInCommandHistory);
                if (command != null) {
                    return command;
                }
            }
            return null;
        }

        // Adds a command to the end of the history.
        void addCommand(String command) {
            commandHistory.add(command);
            positionInCommandHistory = commandHistory.size() - 1;
        }
    }
}
