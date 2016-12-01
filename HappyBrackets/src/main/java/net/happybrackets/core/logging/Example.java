package net.happybrackets.core.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * Created by Samg on 29/09/2016.
 */
public class Example {
    final static Logger logger = LoggerFactory.getLogger(Example.class);

    public static void main(String[] args) {
        logger.info("Msg #1");
        logger.warn("Msg #2");
        logger.error("Msg #3");
        logger.debug("Msg #4");

        // Give the current logging setup status
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        StatusPrinter.print(lc);
    }
}
