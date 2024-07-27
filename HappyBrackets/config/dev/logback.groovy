
statusListener(OnConsoleStatusListener)

appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d{yyyy-MM-dd HH:mm:ss} %-5level %logger{36} - %msg%n"
    }
}


root(DEBUG, ["STDOUT"])