package net.happybrackets.core;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Class that enables us to execute shell commands and retrieve shell result
 */
public class ShellExecute {

    /**
     * Event to signify process has completed
     */
    public interface ProcessComplete {
        void hasCompleted(ShellExecute shellExecute, int exit_value);
    }

    // Listeners of process being complete
    private List<ProcessComplete> processCompleteList = new ArrayList<>();

    // Our actual process
    Process shellProcess = null;

    // The text that the process produces as its output
    String processText = "";

    // error messages generated by the process
    String errorText = "";


    /**
     * Add listeners to be notified when the process is complete
     * @param listener  the listener to be notified when the process completes
     * @return this object
     */
    public ShellExecute addProcessCompleteListener(ProcessComplete listener){
        processCompleteList.add(listener);
        return this;
    }


    /**
     * Kill the running process
     */
    public void killProcess()
    {
        if (shellProcess != null)
        {
            shellProcess.destroy();
        }
    }

    /**
     * Test if the process is alive
     * @return true if process is not null and running
     */
    public boolean isAlive(){
        boolean ret = false;

        if (shellProcess != null)
        {
            ret = shellProcess.isAlive();
        }
        return ret;
    }
    /**
     * Gets the output of the process
     * @return the text from the process
     */
    public String getProcessText() {
        return processText;
    }

    /**
     * Gets any error text from the process
     * @return the text from error
     */
    public String getErrorText() {
        return errorText;
    }

    /**
     * Execute a single commandline string
     * @param cmd commandline to execute
     * @throws IOException thrown exception if unable to run command
     */
    public void executeCommand(String cmd) throws IOException {
        shellProcess = Runtime.getRuntime().exec(cmd);
        startProcessMonitor();
    }

    /**
     * Create a process based on command arguments
     * @param args the arguments to start process
     * @throws IOException thrown exception if unable to create the process
     */
    public void runProcess(String... args) throws IOException{
        ProcessBuilder pb = new ProcessBuilder(args);
        // To capture output from the shell
        shellProcess = pb.start();
        startProcessMonitor();
    }

    /**
     * Monitors the process and reads output text and stores to processText
     */
    private void startProcessMonitor(){

   
        /***********************************************************
         * Create a runnable thread object
         * simply type threadFunction to generate this code
         ***********************************************************/
        Thread thread = new Thread(() -> {

            // Let us connect up our Stream monitoring

            InputStream input_stream = shellProcess.getInputStream();

            Scanner input_scanner = new java.util.Scanner(input_stream).useDelimiter("\\A");
            if (input_scanner.hasNext()) {
                processText = input_scanner.next();
            }

            InputStream error_stream = shellProcess.getErrorStream();
            Scanner error_scanner = new java.util.Scanner(error_stream).useDelimiter("\\A");
            if (error_scanner.hasNext()) {
                errorText = error_scanner.next();
            }



            while(shellProcess.isAlive())
            {
                try {
                    // wait 10 ms for thread to finish, otherwise, we will get an error
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    /*** remove the break below to just resume thread or add your own action***/
                    break;
                    /*** remove the break above to just resume thread or add your own action ***/

                }
            }
            // let us notify our exit
            int exit_value = shellProcess.exitValue();

            for(ProcessComplete listener : processCompleteList){
                listener.hasCompleted(this, exit_value);
            }
        });

        thread.start();
    }

    /**
     * Read the input stream from the process
     * This is not implemented. It looks like the process is run int the context of this process,
     * so we won't get feedback until process had ended anyway
     */
    void readInputStream() {
        java.io.InputStream inputStream = shellProcess.getInputStream();

        /***********************************************************
         * Create a runnable thread object
         * simply type threadFunction to generate this code
         ***********************************************************/
        Thread thread = new Thread(() -> {
            Writer writer = new StringWriter();
            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {

                    writer.write(buffer, 0, n);
                    String current_string = writer.toString();
                    // maybe we can
                    
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


        thread.start();
        /****************** End threadFunction **************************/

    }

}
