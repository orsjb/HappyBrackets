package net.happybrackets.core.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import net.happybrackets.device.DeviceMain;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

/**
 * Created by Samg on 30/10/2016.
 */
public class LoggingTests {

    final static Logger logger = (Logger) LoggerFactory.getLogger(LoggingTests.class);
    final static String logFile = "build/tmp/testing/TestAppender";

    @Before
    public void setup() {
        logger.info("Started test setup");

        try {
            Files.deleteIfExists(Paths.get(logFile));
        } catch (IOException e) {
            logger.error("Unable to remove old {} on startup!", logFile, e);
        }
    }

    @Test
    public void addAppender() {
        String logMessage = "TEST_DEBUG_MSG";
        logger.info("Started addAppender test");

        Logging.AddFileAppender( (Logger) LoggerFactory.getLogger("root"), "TestAppender", logFile, Level.ALL);

        logger.info("Emitting test message");
        logger.debug(logMessage);

        logger.info("Searching {} for test message: {}", logFile, logMessage);
        boolean messageFound = false;
        try {
            FileInputStream fstream = new FileInputStream(logFile);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                if (strLine.contains(logMessage)) {
                    messageFound = true;
                }
            }
        } catch (FileNotFoundException e) {
            logger.error("Unable to read test appender file {}, error:", logFile, e);
        } catch (IOException e) {
            logger.error("Unable to read line from test appender file {}, error:", logFile, e);
        }

        logger.info("Found test message in appender file? status: {}", messageFound);
        assertTrue(messageFound);
    }

    @After
    public void cleanup() {
        logger.info("Started test cleanup");

//        This will have to be removed when we run the test again as it will not be released from the logger yet
//        try {
//            Files.delete(Paths.get(logFile));
//        } catch (IOException e) {
//            logger.error("Unable to remove {} after testing!", logFile, e);
//        }
    }

}
