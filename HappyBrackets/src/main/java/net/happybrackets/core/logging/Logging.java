package net.happybrackets.core.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.FileAppender;

/**
 * Created by Samg on 30/10/2016.
 */
public class Logging {

    /**
     * Add a new log file to a logger.
     * The new log file will be setup with append false, so it will overwrite old log content.
     * @param logger
     * @param appenderName
     * @param outFilePath
     * @param logLevel
     */
    public static void AddFileAppender(Logger logger, String appenderName, String outFilePath, Level logLevel) {

        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        PatternLayoutEncoder ple = new PatternLayoutEncoder();

        ple.setPattern("%date %level [%thread] %logger{10} [%file:%line] %msg%n");
        ple.setContext(lc);
        ple.start();
        FileAppender<ILoggingEvent> fileAppender = new FileAppender<ILoggingEvent>();
        fileAppender.setFile(outFilePath);
        fileAppender.setAppend(false);
        fileAppender.setEncoder(ple);
        fileAppender.setContext(lc);
        fileAppender.start();

        logger.addAppender(fileAppender);
        logger.setLevel(logLevel);
        logger.setAdditive(true); /* set to true if root should log too */
}
}
