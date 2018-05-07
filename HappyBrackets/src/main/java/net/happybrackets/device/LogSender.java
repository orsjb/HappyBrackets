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

package net.happybrackets.device;

import net.happybrackets.core.Device;
import net.happybrackets.core.OSCVocabulary;
import net.happybrackets.device.network.NetworkCommunication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;

/**
 * Manages sending log messages to the controller as they appear in a log file.
 * A record is maintained of which log messages have been sent so far.
 * Upon starting only new log messages will be sent.
 * As new log messages appear they will be sent to the controller (via a separate thread).
 *
 * See {@link NetworkCommunication#sendLogs(boolean)}.
 */
public class LogSender {
    final static Logger logger = LoggerFactory.getLogger(LogSender.class);

    private NetworkCommunication networkCommunication;
    private File logPath; // path to log file.
    private File logPathDir; // path to log file folder.

    private volatile boolean sendEnabled;
    private volatile boolean dispose = false;

    RandomAccessFile fileReader;

    private WatchService watcher; // Service to monitor the file for changes.

    private Sender sender;

    public LogSender(NetworkCommunication networkCommunication, String logLocation) {
        this.networkCommunication = networkCommunication;
        sendEnabled = false;

        try {
            // Set-up a file change monitor.
            watcher = FileSystems.getDefault().newWatchService();
            logPath = new File(logLocation).getAbsoluteFile();

            if (!logPath.exists() || !logPath.isFile()) {
                logger.error("An error occurred monitoring the log file " + logPath.getAbsolutePath() + " for changes: it does not exist or is a directory.");
                return;
            }

            fileReader = new RandomAccessFile(logPath, "r");

            logPathDir = logPath.getAbsoluteFile().getParentFile();
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
     * @param send_enabled true to start sending log messages to the controller.
     */
    public void setSend(boolean send_enabled) {
        this.sendEnabled = send_enabled;
        synchronized (sender) {
            sender.notify();
        }
    }

    /**
     * Whether we have logging enabled
     * @return whether send is enabled
     */
    public boolean getSend(){
        return sendEnabled;
    }

    /**
     * Dispose of the thread handling sending of messages.
     */
    public void dispose() {
        sendEnabled = false;
        dispose = true;
        synchronized (sender) {
            sender.notify();
        }
    }

    private class Sender extends Thread {
        @Override
        public void run() {
            // Flag to indicate if we've sent the existing contents, if any.
            boolean sentInitial = false;

            while (!dispose) {
                while (sendEnabled) {
                    // Send initial file contents if we haven't already.
                    if (!sentInitial) {
                        sendLatest();
                        sentInitial = true;
                    }

                    try {
                        WatchKey key = watcher.take();

                        // For each changed/deleted file in the log file directory (if any).
                        for (WatchEvent<?> event : key.pollEvents()) {
                            File modifiedFile = logPathDir.toPath().resolve((Path) event.context()).toFile().getAbsoluteFile();

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
            // We need to limit the number of lines because too large
            // a log will cause an exception with OSC
            final int MAX_LINES_PER_MESSAGE = 100;

            boolean complete = false;
            do {
                try {

                    int num_lines = 0;

                    StringBuilder sb = new StringBuilder();
                    // Send the new lines in the log file.
                    String line;
                    while ((line = fileReader.readLine()) != null) {
                        sb.append(line).append("\n");
                        num_lines++;
                        // if we have reached our lines then we will send whatr we have so far
                        if (num_lines > MAX_LINES_PER_MESSAGE){
                            break;
                        }
                    }

                    complete = line == null;

                    networkCommunication.send(OSCVocabulary.Device.LOG,
                            new Object[]{
                                    Device.getDeviceName(),
                                    sb.toString()
                            });
                } catch (Exception ex) {
                    logger.error("Error sending new log message.", ex);
                }
            } while (!complete);
        }
    }
}
