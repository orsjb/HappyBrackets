
statusListener(OnConsoleStatusListener)

appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%level %logger - %msg%n"
    }
}

root(DEBUG, ["STDOUT"])