package net.happybrackets.device.network;

import de.sciss.net.OSCMessage;
import de.sciss.net.OSCServer;
import net.happybrackets.core.OSCVocabulary;
import net.happybrackets.core.scheduling.HBScheduler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

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

    String currentTargetFile = "";
    String currentSourceFile = "";
    FileOutputStream tempFile;

    /**
     * Get the OSC port we are using for File reception
     * @return the tcpPort
     */
    public int getReceiverPort(){
        return oscPort;
    }

    /**
     * Constructor opens first available TCP port for OSC Messages
     */
    public FileReceiver(){
        try {
            controllerOscServer =  OSCServer.newUsing(OSCServer.TCP, 0);
            oscPort =  controllerOscServer.getLocalAddress().getPort();

            // Now add a listener
            controllerOscServer.addOSCListener((msg, sender, time) -> {

                if (OSCVocabulary.match(msg, OSCVocabulary.FileSendMessage.WRITE)){
                    performWriteMessage(msg);
                }
                else if (OSCVocabulary.match(msg, OSCVocabulary.FileSendMessage.COMPLETE)){
                    performCompleteMessage(msg);
                }
                else if (OSCVocabulary.match(msg, OSCVocabulary.FileSendMessage.CANCEL)){
                    cancelTransfer();
                }

                System.out.println("File Send Message " + msg.getName());
            });

            controllerOscServer.start();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Cancel transfer of current messgae
     */
    void cancelTransfer(){
        System.out.println("Cancel");
    }
    /**
     * Perform a write message
     * @param msg the OSC Message with the data
     */
    boolean performWriteMessage(OSCMessage msg){
        boolean ret = false;
        currentTargetFile = (String) msg.getArg(0);
        byte [] data = (byte []) msg.getArg(1);

        System.out.println("Received " + data.length + " bytes");
        if (tempFile == null){
            currentSourceFile = TEMP_PATH + HBScheduler.getGlobalScheduler().getCalcTime();

            try {
                tempFile = new FileOutputStream(currentSourceFile);
            } catch (FileNotFoundException e) {
                currentSourceFile = "";
            }
        }

        if (tempFile != null){
            try {
                tempFile.write(data);
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    tempFile.close();
                    currentSourceFile = "";
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return ret;
    }
    /**
     * Perform a complete message
     * @param msg the OSC Message with the data
     */
    void performCompleteMessage(OSCMessage msg){
        String filename = (String) msg.getArg(0);

        System.out.println("Complete File " + filename);
        if (tempFile != null){
            try {
                tempFile.close();
                tempFile = null;

                Files.move(new File(currentSourceFile).toPath(), new File(currentTargetFile).toPath(), StandardCopyOption.REPLACE_EXISTING);
                currentSourceFile = "";
                currentTargetFile = "";

            } catch (IOException e) {
                e.printStackTrace();
                tempFile = null;
                currentSourceFile = "";
            }
        }


    }

}
