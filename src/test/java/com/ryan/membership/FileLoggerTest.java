package com.ryan.membership;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class FileLoggerTest {

    static final Logger logger = Logger.getLogger("FileLoggerTest");

    public static void main(String[] args) throws IOException {
//        String logFileStr = "iDunno_logs/logger_test.log";
        String logFileStr = "log/logger_test.log";
        Path logFilePath = Path.of(logFileStr);
        if (Files.notExists(logFilePath))
            Files.createFile(logFilePath);

        // setup and config logger
        Handler fh = new FileHandler(logFileStr);
        fh.setFormatter(new SimpleFormatter());

        logger.setUseParentHandlers(false);
        logger.addHandler(fh);

        logger.info("This is message 1");
        logger.warning("This is a warning");
    }
}
