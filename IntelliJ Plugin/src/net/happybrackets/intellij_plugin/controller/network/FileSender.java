package net.happybrackets.intellij_plugin.controller.network;

import de.sciss.net.OSCClient;
import de.sciss.net.OSCMessage;
import net.happybrackets.core.OSCVocabulary;
import net.happybrackets.device.HB;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class for sending files to device
 */
public class FileSender {

    public interface FileSendStatusListener {
        void writingFile(String filename);
        void writeSuccess(String filename);
        void writeError(String filename);
    }

    // Create a list of listeners for file messages
    private List<FileSendStatusListener> writeStatusListenerList = new ArrayList<>();
    private Object writeStatusListenerListLock = new Object();

    Set<FileSendStreamer> fileSendStreamerList = new HashSet<>();
    private final Object fileStreamerLock = new Object();
    FileSendStreamer currentFile = null;

    // we will synchronise this object
    final Object fileSendEvent = new Object();

    private final Object filePortLock = new Object();
    int fileSendPort =  0; // This is TCP port for sending files to device
    String fileSendAddress = "";

    private OSCClient fileSendClient = null;

    boolean exitThread = false;
    boolean cancelSend = false;

    final int MAX_FILE_DATA = 1024 * 7;

    /**
     * Add listener for FileSend Status
     * @param listener listener
     */
    public void addWriteStatusListener(FileSendStatusListener listener){
        synchronized (writeStatusListenerListLock){
            writeStatusListenerList.add(listener);
        }
    }

    /**
     * Flag to indicate whether we are sending by looking inside the file queue
     * @return true if files in queue
     */
    public boolean isSending(){
        boolean ret = false;

        synchronized (fileStreamerLock) {
            ret = fileSendStreamerList.size() > 0;
        }
         return ret;
    }

    /**
     * Remove listener for FileSend Status
     * @param listener listener
     */
    public void removeWriteStatusListener(FileSendStatusListener listener){
        synchronized (writeStatusListenerListLock){
            writeStatusListenerList.remove(listener);
        }
    }

    /**
     * Erase all listeners
     */
    public void clearWriteStatusListeners(){
        synchronized (writeStatusListenerListLock){
            writeStatusListenerList.clear();
        }
    }

    FileSender(OSCClient oscClient){

        fileSendClient = oscClient;

        fileSendClient.addOSCListener((msg, sender, time) -> {
            incomingFileMessage(msg, sender);
        });

        Thread thread = new Thread(() -> {

            while (!exitThread) {/* write your code below this line */
                synchronized (fileSendEvent){
                    try {
                        // Put a 5 second timer in as there is sometimes a race condition
                        // where the event gets set before the thread starts
                        fileSendEvent.wait(5000);

                        while (sendNextFileData() && !cancelSend)
                        {
                            fileSendEvent.notify();
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        });

        thread.start();/* End threadFunction */
    }

    /**
     * Send File Data to the device
     * @return true if we still have more data to send
     */
    private boolean sendNextFileData(){
        //System.out.println("Read Next File");
        if (currentFile == null){
            synchronized (fileStreamerLock) {
                if (fileSendStreamerList.size() > 0) {
                    currentFile = fileSendStreamerList.iterator().next();
                }
            }
        }
        if (currentFile != null){
            try {
                if (testClientOpen()) {
                    byte[] file_data = currentFile.readData(MAX_FILE_DATA);
                    OSCMessage message = HB.createOSCMessage(OSCVocabulary.FileSendMessage.WRITE, currentFile.targetFilename, file_data);
                    //System.out.println("Sent " + file_data.length + " bytes");

                    fileSendClient.send(message);
                    if (currentFile.complete) {
                        message = HB.createOSCMessage(OSCVocabulary.FileSendMessage.COMPLETE, currentFile.targetFilename);
                        fileSendClient.send(message);
                        synchronized (fileStreamerLock) {
                            fileSendStreamerList.remove(currentFile);
                            currentFile = null;
                        }
                    }
                }
                else {
                    System.out.println("Client Not Open on Send Next file");
                }

            } catch (Exception e) {
                e.printStackTrace();
                synchronized (fileStreamerLock) {
                    fileSendStreamerList.remove(currentFile);
                    currentFile = null;
                }
            }

        }

        if (currentFile == null) {
            synchronized (fileStreamerLock) {
                if (fileSendStreamerList.size() > 0) {
                    currentFile = fileSendStreamerList.iterator().next();
                }
            }
        }
        return currentFile != null;
    }
    /**
     * Add a file to be sent to device
     * After adding, will automatically start to send to device
     * @param source_file source file
     * @param target_file target file
     * @return true if we start the send
     */
    boolean addFile (String source_file, String target_file){
       boolean ret = false;

       System.out.println("Send " + source_file + " to " + target_file);
       FileSendStreamer fileSendStreamer = new FileSendStreamer(source_file, target_file);
       cancelSend = false;

       boolean do_notify = false;

        if (testClientOpen()) {
            System.out.println("Client Open");
            synchronized (fileStreamerLock) {

                do_notify = fileSendStreamerList.size() < 1;

                if (!fileSendStreamerList.contains(fileSendStreamer)) {
                    fileSendStreamerList.add(fileSendStreamer);
                    ret = true;
                }
            }

            if (do_notify)
            {
                if (testClientOpen()) {
                        // we will run in a separate thread to prevent as lockup on GUI due to locked up TCP
                        Thread thread = new Thread(() -> {
                            try {
                                fileSendClient.send(HB.createOSCMessage(OSCVocabulary.FileSendMessage.START));
                                synchronized (fileSendEvent){
                                    fileSendEvent.notify();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        });

                        thread.start();// End threadFunction

                }
                else {
                    System.out.println("Client Not Open after notify");
                }

            }
        }
        else {
            System.out.println("Unable to Open Client");
        }


       return ret;
    }

    public void cancelSend(){
        cancelSend = true;
        synchronized (fileStreamerLock) {
            fileSendStreamerList.clear();
        }

        if (testClientOpen()) {
            // we will run in a separate thread to prevent as lockup on GUI due to locked up TCP
            Thread thread = new Thread(() -> {
                try {
                    fileSendClient.send(HB.createOSCMessage(OSCVocabulary.FileSendMessage.CANCEL));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });


            thread.start();// End threadFunction
        }
    }


    /**
     * Check if our TCP port is assigned and connected
     * @return true if connected
     */
    boolean testClientOpen() {
        boolean ret = false;
        synchronized (filePortLock) {
            if (fileSendClient != null)
            {
                ret = fileSendClient.isConnected();
            }

        }
        return ret;

    }


    /**
     * Perform actions on File Messages
     * @param m The OSC message containing message
     * @param addr address of sender
     */
    void incomingFileMessage(OSCMessage m, SocketAddress addr) {

        System.out.println(m.getName());
        try {
            if (OSCVocabulary.match(m, OSCVocabulary.FileSendMessage.WRITE)) {
                String filename = (String) m.getArg(0);
                synchronized (writeStatusListenerListLock){
                    for (FileSendStatusListener listener:
                            writeStatusListenerList) {
                        listener.writingFile(filename);
                    }
                }

            }
            else if (OSCVocabulary.match(m, OSCVocabulary.FileSendMessage.COMPLETE)) {
                String filename = (String) m.getArg(0);
                synchronized (writeStatusListenerListLock){
                    for (FileSendStatusListener listener:
                            writeStatusListenerList) {
                        listener.writeSuccess(filename);
                    }
                }

            }
            else if (OSCVocabulary.match(m, OSCVocabulary.FileSendMessage.ERROR)) {
                String filename = (String) m.getArg(0);
                synchronized (writeStatusListenerListLock){
                    for (FileSendStatusListener listener:
                            writeStatusListenerList) {
                        listener.writeError(filename);
                    }
                }
            }

        } catch (Exception ex) {
        }
    }

    /**
     * Set the port we need to connect our File sender to to communicate via TCP
     * @param address the address we need to send messages to
     * @param port remote port number
     */
    public synchronized void setFileSendServerPort(String address, int port){

        if (fileSendPort != port || !fileSendAddress.equalsIgnoreCase(address)){

            // if we are switching ports from localhost to network, this becomes a problem
            // let us just leave teh connection open if it has one
            // If it fails, then let open use the new parameters
            //closeFileSendClientPort();

            fileSendPort = port;
            fileSendAddress = address;
        }

    }

}
