package net.happybrackets.device.network;

import de.sciss.net.OSCMessage;
import de.sciss.net.OSCServer;
import net.happybrackets.core.OSCVocabulary;
import net.happybrackets.core.scheduling.HBScheduler;
import net.happybrackets.device.HB;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Hashtable;
import java.util.Map;

/**
 * Class that receives files from network so we can store them
 * onto our filesystem instead of using scp
 *
 */
public class FileReceiver {
    // A TCP server to get our file send data from
    private OSCServer controllerOscServer;
    private int oscPort = 0;

    final static String TEMP_PATH = "ramfs" + File.separatorChar;

    private class ClientFileTransfer {
        String currentTargetFile = "";
        String currentSourceFile = "";
        FileOutputStream tempFile;
        boolean started = false;
    }

    Map<String, ClientFileTransfer> clientFileTransferMap = new Hashtable<>();


    /**
     * Get the OSC port we are using for File reception
     * @return the tcpPort
     */
    public int getReceiverPort(){
        return oscPort;
    }

    /**
     * Constructor opens first available TCP port for OSC Messages
     * @param oscServer the OSC server to receieve files through
     */
    public FileReceiver(OSCServer oscServer){
        try {
            controllerOscServer =  oscServer;
            oscPort =  controllerOscServer.getLocalAddress().getPort();

            // Now add a listener
            controllerOscServer.addOSCListener((msg, sender, time) -> {

                try {
                    if (OSCVocabulary.match(msg, OSCVocabulary.FileSendMessage.WRITE)) {
                        performWriteMessage(msg, sender);

                    }
                    else if (OSCVocabulary.match(msg, OSCVocabulary.FileSendMessage.START)){
                        performStart(sender);
                    }
                    else if (OSCVocabulary.match(msg, OSCVocabulary.FileSendMessage.COMPLETE)) {
                        performCompleteMessage(msg, sender);

                    }

                    else if (OSCVocabulary.match(msg, OSCVocabulary.FileSendMessage.CANCEL)) {
                        try {
                            cancelTransfer(sender);

                        } catch (IOException e) {
                            e.printStackTrace();
                            controllerOscServer.send(HB.createOSCMessage(OSCVocabulary.FileSendMessage.ERROR, ""), sender);

                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //System.out.println("File Send Message " + msg.getName());
            });


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void performStart(SocketAddress sender) {
        ClientFileTransfer client;
        //reset files and allow data to be received
        if (clientFileTransferMap.containsKey(sender.toString())){
            client = clientFileTransferMap.get(sender.toString());

        }
        else
        {
            client = new ClientFileTransfer();
            clientFileTransferMap.put(sender.toString(), client);
        }
        client.tempFile = null;
        client.started = true;

    }

    /**
     * Cancel transfer of current message
     * @param sender
     */
    void cancelTransfer(SocketAddress sender) throws IOException {
        System.out.println("Cancel Transfer");
        if (clientFileTransferMap.containsKey(sender.toString())) {
            ClientFileTransfer client = clientFileTransferMap.get(sender.toString());
            client.started = false;

            controllerOscServer.send(HB.createOSCMessage(OSCVocabulary.FileSendMessage.CANCEL, client.currentTargetFile), sender);
            if (client.tempFile != null) {
                client.tempFile.close();
                client.tempFile = null;
                Files.deleteIfExists(new File(client.currentSourceFile).toPath());
                client.currentSourceFile = null;
            }
        }
    }
    /**
     * Perform a write message
     * @param msg the OSC Message with the data
     * @param sender
     */
    boolean performWriteMessage(OSCMessage msg, SocketAddress sender){
        boolean ret = false;
        String currentTargetFile = "";

        String client_key = sender.toString();

        if (clientFileTransferMap.containsKey(client_key)) {
            ClientFileTransfer client = clientFileTransferMap.get(sender.toString());
            if (client.started) {
                client.currentTargetFile = (String) msg.getArg(0);

                currentTargetFile = client.currentTargetFile;
                byte[] data = (byte[]) msg.getArg(1);

                // If we don't have a temp file, create one
                if (client.tempFile == null) {
                    client.currentSourceFile = TEMP_PATH + HBScheduler.getGlobalScheduler().getCalcTime();

                    try {
                        client.tempFile = new FileOutputStream(client.currentSourceFile);
                        controllerOscServer.send(HB.createOSCMessage(OSCVocabulary.FileSendMessage.WRITE, client.currentTargetFile), sender);
                        System.out.println("Start write file " + currentTargetFile);
                    } catch (FileNotFoundException e) {
                        System.out.println(e.getMessage());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                // Now Write data to file
                if (client.tempFile != null) {
                    try {
                        client.tempFile.write(data);
                        ret = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                        try {
                            client.tempFile.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        }
        if (!ret){
            try {
                controllerOscServer.send(HB.createOSCMessage(OSCVocabulary.FileSendMessage.ERROR, currentTargetFile), sender);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }
    /**
     * Perform a complete message
     * @param msg the OSC Message with the data
     * @param sender
     */
    boolean performCompleteMessage(OSCMessage msg, SocketAddress sender){
        String filename = (String) msg.getArg(0);
        String currentTargetFile = "";

        System.out.println("Complete File " + filename);
        boolean ret = false;
        if (clientFileTransferMap.containsKey(sender.toString())) {
            ClientFileTransfer client = clientFileTransferMap.get(sender.toString());
            currentTargetFile = client.currentTargetFile;

            if (client.started) {
                if (client.tempFile != null) {
                    try {
                        client.tempFile.close();
                        client.tempFile = null;

                        // we ned to also create directory for file to go into
                        File target_path = new File(client.currentTargetFile);

                        String path = target_path.getParentFile().getAbsolutePath();

                        System.out.println("Create path " + path);
                        new File(path).mkdir();

                        Files.move(new File(client.currentSourceFile).toPath(), target_path.toPath(), StandardCopyOption.REPLACE_EXISTING);


                        controllerOscServer.send(HB.createOSCMessage(OSCVocabulary.FileSendMessage.COMPLETE, client.currentTargetFile), sender);
                        System.out.println("Complete write file " + currentTargetFile);
                        ret = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                        client.tempFile = null;
                    }
                }
            }

        }

        if (!ret)
        {
            try {
                controllerOscServer.send(HB.createOSCMessage(OSCVocabulary.FileSendMessage.ERROR, currentTargetFile), sender);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

}
