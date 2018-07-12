package net.happybrackets.device.network;

// placeholder class till we remove OSC
import java.net.Socket;

public class ControllerServerThread extends Thread {
    private Socket controllerSocket;

    /**
     * Create a thread to connect to controller
     * @param socket socket to connect with
     */
    public ControllerServerThread(Socket socket) {
        controllerSocket = socket;
    }
}
