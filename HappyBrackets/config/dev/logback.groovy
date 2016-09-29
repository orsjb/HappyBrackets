
statusListener(OnConsoleStatusListener)

appender("LASTRUN", FileAppender) {
    file    = "logs/last-run.txt"
    append  = false
    encoder(PatternLayoutEncoder) {
        pattern = "%level %logger - %msg%n"
    }
}

appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%level %logger - %msg%n"
    }
}

root(DEBUG, ["STDOUT", "LASTRUN"])