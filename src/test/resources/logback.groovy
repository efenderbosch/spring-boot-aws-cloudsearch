appender("CONSOLE", ConsoleAppender) {
  encoder(PatternLayoutEncoder) {
    pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
  }
}

logger("net.fender.springboot.aws.cloudsearch", DEBUG)

root(INFO, ["CONSOLE"])