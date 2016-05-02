package pi.test;
/*
 * Java Embedded Raspberry Pi GPIO Input app
 */

import java.io.File;
import java.io.FileWriter;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 *
 * @author hinkmond
 */
public class JerpiGPIOInput {
    
    static final String GPIO_IN = "in";
    
    // Add which GPIO ports to read here
    static String[] GpioChannels = { "1", "2", "3", "4", "5", "6", "7" };
       
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        
        try {
            
            /*** Init GPIO port(s) for input ***/
            
            // Open file handles to GPIO port unexport and export controls
            FileWriter unexportFile = new FileWriter("/sys/class/gpio/unexport");
            FileWriter exportFile = new FileWriter("/sys/class/gpio/export");

            for (String gpioChannel : GpioChannels) {
                System.out.println(gpioChannel);
    
                // Reset the port
                File exportFileCheck = new File("/sys/class/gpio/gpio"+gpioChannel);
                if (exportFileCheck.exists()) {
                    unexportFile.write(gpioChannel);
                    unexportFile.flush();
                }
            
            
                // Set the port for use
                exportFile.write(gpioChannel);   
                exportFile.flush();

                // Open file handle to input/output direction control of port
                FileWriter directionFile = new FileWriter("/sys/class/gpio/gpio" + gpioChannel + "/direction");
            
                // Set port for input
                directionFile.write(GPIO_IN);
            }
            
            /*** Read data from each GPIO port ***/
            RandomAccessFile[] raf = new RandomAccessFile[GpioChannels.length];
            
            int sleepPeriod = 10;
            final int MAXBUF = 256;
            
            byte[] inBytes = new byte[MAXBUF]; 
            String inLine;
            
            int zeroCounter = 0;
            
            // Get current timestamp with Calendar()
            Calendar cal;
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
            String dateStr;
            
            // Open RandomAccessFile handle to each GPIO port
            for (int channum=0; channum < raf.length; channum++) {
                raf[channum] = new RandomAccessFile("/sys/class/gpio/gpio" + GpioChannels[channum] + "/value", "r");
            }
            
            
            // Loop forever
            while (true) {
                
                // Get current timestamp for latest event
                cal = Calendar.getInstance();
                dateStr = dateFormat.format(cal.getTime());
        
                // Use RandomAccessFile handle to read in GPIO port value
                for (int channum=0; channum < raf.length; channum++) {
                    
                    // Reset file seek pointer to read latest value of GPIO port
                    raf[channum].seek(0);
                    raf[channum].read(inBytes);
                    inLine = new String(inBytes);
                    
                    // Check if any value was read
                    if (inLine != null) {
                        
                        // Compress 0 values so we don't see too many 
                        //   unimportant lines
                        if (inLine.startsWith("0")) {
                            if (zeroCounter < 1000) {
                                zeroCounter++;
                            } else {
                                System.out.print(dateStr + ": " + inLine);
                                zeroCounter = 0;
                            }
                        } else {
                            // Else, specially mark value non-zero value
                            System.out.print("*** Channel: " + channum + ":: " + dateStr + ": Data:: " + inLine);
                            zeroCounter = 0;
                        }
                    }

                    // Wait for a while
                    java.lang.Thread.sleep(sleepPeriod);
        
                }
            }

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}