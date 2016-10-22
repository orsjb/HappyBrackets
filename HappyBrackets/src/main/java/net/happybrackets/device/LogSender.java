package net.happybrackets.device;

import net.happybrackets.controller.network.DeviceConnection;
import net.happybrackets.core.Device;
import net.happybrackets.core.Synchronizer;
import net.happybrackets.device.network.NetworkCommunication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Manages sending log messages to the controller as they appear in a log file.
 * A record is maintained of which log messages have been sent so far.
 * Upon starting only new log messages will be sent.
 * As new log messages appear they will be sent to the controller (via a separate thread).
 *
 * @see {@link NetworkCommunication#sendLogs(boolean)}
 */
public class LogSender {
    final static Logger logger = LoggerFactory.getLogger(LogSender.class);

    private NetworkCommunication networkCommunication;
    private File logPath; // path to log file.
    private File logPathDir; // path to log file folder.

    private volatile boolean send;
    private volatile boolean dispose = false;

    RandomAccessFile fileReader;

    private WatchService watcher; // Service to monitor the file for changes.

    private Sender sender;

    public LogSender(NetworkCommunication networkCommunication, String logLocation) {
        this.networkCommunication = networkCommunication;
        send = false;

        try {
            // Set-up a file change monitor.
            watcher = FileSystems.getDefault().newWatchService();
            logPath = new File(logLocation);

            if (!logPath.exists() || !logPath.isFile()) {
                logger.error("An error occurred monitoring the log file " + logPath.getAbsolutePath() + " for changes: it does not exist or is a directory.");
                return;
            }

            fileReader = new RandomAccessFile(logPath, "r");

            logPathDir = logPath.getParentFile();
            logPathDir.toPath().register(watcher, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);

            // Set-up the monitor handler/log sender.
            sender = new Sender();
            sender.start();

            logger.info("LogSender initialised for log file " + logPath.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Unable to monitor file for changes. Error occurred: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * @param send true to start sending log messages to the controller.
     */
    public void setSend(boolean send) {
        this.send = send;
        synchronized (sender) {
            sender.notify();
        }
    }

    /**
     * Dispose of the thread handling sending of messages.
     */
    public void dispose() {
        send = false;
        dispose = true;
        synchronized (sender) {
            sender.notify();
        }
    }

    private class Sender extends Thread {
        public void run() {
            // Flag to indicate if we've sent the existing contents, if any.
            boolean sentInitial = false;

            while (!dispose) {
                while (send) {
                    // Send initial file contents if we haven't already.
                    if (!sentInitial) {
                        sendLatest();
                        sentInitial = true;
                    }

                    try {
                        WatchKey key = watcher.take();

                        // For each changed/deleted file in the log file directory (if any).
                        for (WatchEvent<?> event : key.pollEvents()) {
                            File modifiedFile = logPathDir.toPath().resolve((Path) event.context()).toFile();

                            // If the log file has changed.
                            if (modifiedFile.equals(logPath)) {
                                if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                                    logger.info("The log file was deleted.");
                                    return;
                                } else if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                                    sendLatest();
                                }
                            }
                        }

                        boolean valid = key.reset();
                        if (!valid) {
                            throw new IllegalStateException("The watch service key has become invalid.");
                        }
                    }
                    catch (ClosedWatchServiceException cwse) {
                        // This is expected if the watch service is closed when terminate() is called.
                    }
                    catch (Exception ex) {
                        logger.error("An error occurred monitoring the log file for changes.", ex);
                        return;
                    }
                }

                synchronized (this) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private void sendLatest() {
            try {
                StringBuilder sb = new StringBuilder();
                // Send the new lines in the log file.
                String line;
                while ((line = fileReader.readLine()) != null) {
                    sb.append(line);
                }

                networkCommunication.send("/device/log",
                        new Object[] {
                                networkCommunication.getID(),
                                sb.toString()
                        });
            }
            catch (Exception ex) {
                logger.error("Error sending new log message.", ex);
            }
        }
    }
}
