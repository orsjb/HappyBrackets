package net.happybrackets.device.network;

// THis is a placeholder class for when we remove OSC
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ControllerServer {
    private static ControllerServer ourInstance = null;

    private ServerSocket serverSocket;
    private static int serverPort = 0;

    /**
     * Get the server port we have opened
     * @return returns the server port
     */
    public static int getServerPort(){
        return serverPort;
    }

    public static synchronized ControllerServer getInstance() {

        if (ourInstance == null){
            try {
                ourInstance  = new ControllerServer();
                serverPort = ourInstance.serverSocket.getLocalPort();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return ourInstance;
    }

    /**
     * Open TCP Port on first available port
     * @throws IOException
     */
    private ControllerServer() throws IOException {
        serverSocket = new ServerSocket(0x0);

        /***********************************************************
         * Create a runnable thread object
         * simply type threadFunction to generate this code
         ***********************************************************/
        Thread thread = new Thread(() -> {

            while (true) {
                /*** write your code below this line ***/

                try (Socket socket = serverSocket.accept()) {
                    System.out.println("ControllerServer New client connected");

                    new ControllerServerThread(socket).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }


                /*** write your code above this line ***/

            }
        });

        /*** write your code you want to execute before you start the thread below this line ***/

        /*** write your code you want to execute before you start the thread above this line ***/

        thread.start();
        /****************** End threadFunction **************************/
    }
}
